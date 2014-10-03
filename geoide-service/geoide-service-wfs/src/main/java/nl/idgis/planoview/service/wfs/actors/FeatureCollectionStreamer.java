package nl.idgis.planoview.service.wfs.actors;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.io.WKBWriter;

import play.Logger;
import nl.idgis.ogc.client.wfs.Feature;
import nl.idgis.ogc.client.wfs.FeatureCollectionReader;
import nl.idgis.ogc.util.MimeContentType;
import nl.idgis.planoview.commons.domain.QName;
import nl.idgis.planoview.commons.domain.geometry.Srs;
import nl.idgis.planoview.commons.domain.geometry.wkb.WkbGeometry;
import nl.idgis.planoview.service.messages.ProducerMessage;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class FeatureCollectionStreamer extends UntypedActor {

	private FeatureCollectionReader reader = null;
	private InputStream inputStream = null;
	private Iterator<Feature> iterator = null;
	private Cancellable killSchedule = null;
	
	public final static Props props () {
		return Props.create (FeatureCollectionStreamer.class);
	}
	
	@Override
	public void preStart () throws Exception {
		scheduleTimeout ();
	}
	
	@Override
	public void postStop () throws Exception {
		close ();
	}
	
	@Override
	public void onReceive (final Object msg) throws Exception {
		if (msg instanceof ConsumeStream) {
			handleConsumeStream ((ConsumeStream) msg);
		} else if (msg instanceof ProducerMessage.Request) {
			handleRequest ((ProducerMessage.Request) msg);
		} else {
			unhandled (msg);
		}
	}
	
	private void handleRequest (final ProducerMessage.Request request) throws Exception {
		clearTimeout ();
		
		if (this.iterator == null) {
			return;
		}
		
		for (int i = 0; i < request.getN (); ++ i) {
			// Signal end:
			if (!iterator.hasNext ()) {
				close ();
				sender ().tell (new ProducerMessage.EndOfStream (), self ());
				self ().tell (PoisonPill.getInstance (), self ());
				return;
			}
			
			// Send the next item:
			sender ().tell (createItem (iterator.next ()), self ());
		}
		
		scheduleTimeout ();
	}
	
	private ProducerMessage.NextItem createItem (final Feature feature) throws Exception {
		final Map<String, Object> properties = new HashMap<> ();
		
		for (final Map.Entry<String, Object> entry: feature.properties ().entrySet ()) {
			final Object value = entry.getValue ();
			
			if (value instanceof org.deegree.geometry.Geometry) {
				final org.deegree.geometry.Geometry deegreeGeometry = (org.deegree.geometry.Geometry) value;

				final ICRS crs = deegreeGeometry.getCoordinateSystem ();
				final Srs srs;
				if (crs != null) {
					srs = new Srs (crs.getCode ().getCode ());
				} else {
					srs = null;
				}
				
				properties.put (entry.getKey (), new WkbGeometry (srs, WKBWriter.write (deegreeGeometry)));
			} else {
				properties.put (entry.getKey (), entry.getValue ());
			}
		}
		
		final nl.idgis.planoview.commons.domain.feature.Feature item = new nl.idgis.planoview.commons.domain.feature.Feature (
				new QName (feature.featureTypeName (), feature.featureTypeNamespace ()),
				feature.id (),
				properties
			);
		
		return new ProducerMessage.NextItem (item);
	}
	
	private void handleConsumeStream (final ConsumeStream msg) throws Exception {
		if (inputStream != null) {
			throw new IllegalStateException ("Already consuming a feature collection");
		}
		
		Logger.debug ("Starting to consume feature collection: " + msg.getContentType ());

		this.reader = new FeatureCollectionReader (msg.getContentType ());
		this.inputStream = msg.getInputStream ();
		
		if (this.inputStream != null) {
			this.iterator = reader.parseCollection (this.inputStream).iterator ();
		}
		
		scheduleTimeout ();
	}
	
	private void close () throws Exception {
		if (inputStream != null) {
			inputStream.close ();
		}
		
		reader = null;
		iterator = null;
		inputStream = null;
	}

	private void clearTimeout () {
		if (killSchedule != null) {
			killSchedule.cancel ();
			killSchedule = null;
		}
	}
	
	private void scheduleTimeout () throws Exception {
		final ActorRef self = self ();

		clearTimeout ();
		
		killSchedule = context ().system().scheduler().scheduleOnce (new FiniteDuration (30, TimeUnit.SECONDS), new Runnable () {
			@Override
			public void run () {
				Logger.debug ("Terminating feature collection reader due to timeout");
				self.tell (PoisonPill.getInstance (), self);
			}
		}, context ().dispatcher ());
	}
	
	public final static class ConsumeStream {
		private final MimeContentType contentType;
		private final InputStream inputStream;
		
		public ConsumeStream (final MimeContentType contentType, final InputStream inputStream) {
			this.contentType = contentType;
			this.inputStream = inputStream;
		}
		
		public InputStream getInputStream () {
			return this.inputStream;
		}
		
		public MimeContentType getContentType () {
			return contentType;
		}
	}
}

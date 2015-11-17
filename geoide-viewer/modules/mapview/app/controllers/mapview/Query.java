package controllers.mapview;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import nl.idgis.geoide.commons.domain.FeatureQuery;
import nl.idgis.geoide.commons.domain.FeatureType;
import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.commons.domain.api.MapQuery;
import nl.idgis.geoide.commons.domain.api.ServiceProviderApi;
import nl.idgis.geoide.commons.domain.feature.Feature;
import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.service.messages.ProducerMessage;
import nl.idgis.geoide.commons.domain.service.messages.QueryFeatures;
import nl.idgis.geoide.commons.domain.service.messages.QueryFeaturesResponse;
import nl.idgis.geoide.commons.domain.service.messages.ServiceError;
import nl.idgis.geoide.util.Promises;
import play.Logger;
import play.libs.Akka;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.duration.FiniteDuration;

public class Query extends Controller {
	private final static long queryFeaturesTimeout = 20000;
	
	private final ServiceProviderApi serviceProvider;
	private final MapQuery mapQuery;
	private final ActorRef serviceManager;
	private final WSClient ws;
	
	@Inject
	public Query (final MapQuery mapQuery, final ActorRef serviceManager, final ServiceProviderApi serviceProvider, final WSClient ws) {
		this.mapQuery = mapQuery;
		this.serviceManager = serviceManager;
		this.serviceProvider = serviceProvider;
		this.ws = ws;
	}
	
	public Promise<Result> query () {
		return Promises.asPromise (mapQuery.prepareQuery (JsonFactory.externalize (request ().body ().asJson ())))
			.flatMap ((queryInfo) -> {
				Logger.debug ("Querying " + queryInfo.getLayerInfos ().size () + " layers");
				
				return Promises.asPromise (mapQuery.prepareFeatureTypes (queryInfo))
					.flatMap ((featureTypes) -> {
						Logger.debug ("Querying " + featureTypes.size () + " feature types");

						return queryFeatureTypes (queryInfo.getFeatureQuery (), featureTypes);
					});
			});
	}
	
	public Promise<Result> serviceRequestWithFeatureType (final String serviceId, final String localName, final String namespace, final String bbox) {
		return Promises.asPromise (serviceProvider.findService (serviceId))
			.flatMap ((service) -> {
				Logger.info ("Requesting from service: " + service.getServiceEndpoint ());
				
				final String url = service.getServiceEndpoint ();
				
				WSRequest request = requestForEndpoint (service.getServiceEndpoint (), 10000)
					.setQueryParameter ("service", "WFS")
					.setQueryParameter ("version", service.getServiceVersion ())
					.setQueryParameter ("request", "GetFeature")
					.setQueryParameter ("bbox", bbox)
					.setQueryParameter ("srsname", "EPSG:28992");
				
				if (namespace == null || namespace.isEmpty ()) {
					request = request.setQueryParameter ("typename", localName);
				} else {
					request = request
						.setQueryParameter ("typename", "app:" + localName)
						.setQueryParameter ("namespace", String.format("xmlns(app=%s)", namespace));
				}
				
				return request
					.get ()
					.map ((response) -> {
						return ok (response.asByteArray ()).as (response.getHeader ("Content-Type"));
					});
			});
	}
	
	private WSRequest requestForEndpoint (final String endpoint, final int timeout) {
		final String url;
		final String query;
		final URI uri;
		
		// Split the endpoint:
		final int offset = endpoint.indexOf ('?');
		if (offset >= 0) {
			url = endpoint.substring (0, offset);
			query = endpoint.substring (offset + 1);
		} else {
			url = endpoint;
			query = "";
		}
		
		// Create the request holder:
		WSRequest holder = ws.url (url).setRequestTimeout (timeout);
		
		// Add parameters from the endpoint:
		if (!query.isEmpty ()) {
			try {
				uri = new URI (endpoint);
				for (final NameValuePair nvp: URLEncodedUtils.parse (uri, "UTF-8")) {
					holder = holder.setQueryParameter (nvp.getName (), nvp.getValue ());
				}
			} catch (URISyntaxException e) {
			}
		}
		
		return holder;
	}
	
	private Promise<Result> queryFeatureTypes (final Optional<FeatureQuery> query, final List<ParameterizedFeatureType<?>> featureTypes) {
		final List<Promise<Object>> promises = new ArrayList<> (featureTypes.size ());
		
		for (final ParameterizedFeatureType<?> featureType: featureTypes) {
			promises.add (Promise.wrap (Patterns.ask (
				serviceManager, 
				new QueryFeatures (featureType, query), 
				queryFeaturesTimeout
			)));
		}
		
		return Promise.sequence (promises).map (new Function<List<Object>, Result> () {
			@Override
			public Result apply(List<Object> responses) throws Throwable {
				final List<FeatureProducer> producers = new ArrayList<> ();
				final List<String> errors = new ArrayList<> ();
				
				for (int i = 0; i < responses.size (); ++ i) {
					final Object response = responses.get (i);
					
					if (response instanceof QueryFeaturesResponse) {
						producers.add (new FeatureProducer (((QueryFeaturesResponse) response).producer (), featureTypes.get (i).getFeatureType ()));
					} else if (response instanceof ServiceError) {
						errors.add (((ServiceError) response).message ());
					} else {
						throw new IllegalArgumentException ("Invalid response message type: " + response.getClass ().getCanonicalName ());
					}
				}
				
				// Report errors if one of the GetFeature requests failed:
				if (!errors.isEmpty ()) {
					final ObjectNode errorResponse = Json.newObject ();
					
					errorResponse.put ("result", "failed");
					errorResponse.put ("messages", Json.toJson (errors));
					
					return badRequest (errorResponse);
				}

				// Send chunked output for the given features:
				final Chunks<String> chunks = new StringChunks () {
					@Override
					public void onReady (final Out<String> out) {
						final ActorRef writerActor = Akka.system ().actorOf (FeatureWriter.props (producers, out));
						writerActor.tell ("start", writerActor);
					}
				};
				
				return ok (chunks).as ("application/json");
			}
		});
	}
	
	public final static class FeatureProducer {
		public final ActorRef producer;
		public final FeatureType featureType;
		
		public FeatureProducer (final ActorRef producer, final FeatureType featureType) {
			this.producer = producer;
			this.featureType = featureType;
		}
	}
	
	public static class FeatureWriter extends UntypedActor {
		private final Chunks.Out<String> out;
		private final List<FeatureProducer> producers;
		private Cancellable killTimeout = null;
		private final Set<FeatureProducer> completedProducers = new HashSet<> ();
		private boolean hasFeatures = false;
		private Envelope envelope = null;
		
		public static Props props (final List<FeatureProducer> producers, final Chunks.Out<String> out) {
			return Props.create (FeatureWriter.class, producers, out);
		}
		
		public FeatureWriter (final List<FeatureProducer> producers, final Chunks.Out<String> out) {
			if (producers == null) {
				throw new NullPointerException ("producer cannot be null");
			}
			if (out == null) {
				throw new NullPointerException ("out cannot be null");
			}
			
			this.out = out;
			this.producers = new ArrayList<> (producers);
		}

		@Override
		public void postStop () throws Exception {
			handleStop ();
		}
		
		@Override
		public void onReceive (final Object msg) throws Exception {
			if ("start".equals (msg)) {
				handleStart ();
			} else if (msg instanceof ProducerMessage.NextItem) {
				handleNextItem (findProducer (sender ()), (Feature)((ProducerMessage.NextItem) msg).getItem ());
			} else if (msg instanceof ProducerMessage.EndOfStream) {
				handleEndOfStream (findProducer (sender ()));
			} else {
				unhandled (msg);
			}
		}
		
		private FeatureProducer findProducer (final ActorRef ref) {
			for (final FeatureProducer producer: producers) {
				if (producer.producer.equals (ref)) {
					return producer;
				}
			}
			
			throw new IllegalStateException ("Producer not found: " + ref);
		}
		
		private void handleNextItem (final FeatureProducer producer, final Feature feature) {
			clearTimeout ();
			
			// Send the feature:
			out.write ((hasFeatures ? ",\n" : "\n") + Json.stringify (Json.toJson (feature)));
			hasFeatures = true;
			
			// Update the envelope:
			for (final Map.Entry<String, Object> entry: feature.getProperties ().entrySet ()) {
				if (entry.getValue () instanceof Geometry) {
					final Geometry geometry = (Geometry) entry.getValue ();
					
					envelope = envelope == null ? geometry.getRawEnvelope () : envelope.combine (geometry.getRawEnvelope ());
				}
			}
			
			// Request the next feature from the producer:
			producer.producer.tell (new ProducerMessage.Request (1), self ());
			
			scheduleTimeout ();
		}
		
		private void handleEndOfStream (final FeatureProducer producer) {
			clearTimeout ();
			
			// Ignore duplicate messages:
			if (completedProducers.contains (producer)) {
				scheduleTimeout ();
				return;
			}
			
			// Add the producer to the completed set:
			completedProducers.add (producer);
			
			// Terminate or continue:
			if (completedProducers.size () == producers.size ()) {
				self ().tell (PoisonPill.getInstance (), self ());
			} else {
				scheduleTimeout ();
			}
		}
		
		private void handleStart () {
			// Write JSON preamble:
			out.write ("{\n\"features\":[");
			
			// Terminate immediately if there's nothing to consume:
			if (producers.isEmpty ()) {
				self ().tell (PoisonPill.getInstance (), self ());
				return;
			}
				
			// Start consuming the output of each producer:
			for (final FeatureProducer p: producers) {
				p.producer.tell (new ProducerMessage.Request (1), self ());
			}
			
			// Schedule a timeout:
			scheduleTimeout ();
		}
		
		private void handleStop () {
			clearTimeout ();
			
			final StringBuilder builder = new StringBuilder ();
			
			builder.append ("\n],\n");
			
			// Write envelope:
			if (envelope != null) {
				builder.append ("\"envelope\":" + Json.stringify (Json.toJson (envelope)) + ",\n");
			}
			
			// Write status:
			builder.append ("\"result\":\"ok\"\n");
			
			builder.append ("}");
			
			out.write (builder.toString ());
			out.close ();
			
			Logger.debug ("Feature writer terminated");
		}
		
		private void clearTimeout () {
			if (killTimeout != null) {
				killTimeout.cancel ();
				killTimeout = null;
			}
		}
		
		private void scheduleTimeout () {
			clearTimeout ();
			
			final ActorRef self = self ();
			
			killTimeout = context ().system ().scheduler ().scheduleOnce (
					new FiniteDuration (30, TimeUnit.SECONDS), 
					new Runnable () {
						@Override
						public void run () {
							Logger.debug ("Terminating feature writer due to timeout");
							self.tell (PoisonPill.getInstance (), self);
						}
					}, 
					context ().dispatcher ());
		}
	}
}
package nl.idgis.geoide.commons.remote.transport;

import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Publisher;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import nl.idgis.geoide.commons.remote.RemoteMethodCall;
import nl.idgis.geoide.commons.remote.RemoteMethodServer;
import nl.idgis.geoide.commons.remote.transport.messages.RemoteMethodCallFailure;
import nl.idgis.geoide.util.streams.AkkaSerializablePublisher;
import nl.idgis.geoide.util.streams.SerializablePublisherActor;

public class RemoteMethodServerActor extends UntypedActor {
	
	private LoggingAdapter log = Logging.getLogger (getContext ().system (), this);

	private final RemoteMethodServer server;
	
	public RemoteMethodServerActor (final RemoteMethodServer server, final String name) {
		this.server = server;
	}
	
	public static Props props (final RemoteMethodServer server, final String name) {
		if (server == null) {
			throw new NullPointerException ("server cannot be null");
		}
		if (name == null) {
			throw new NullPointerException ("name cannot be null");
		}
		
		return Props.create (RemoteMethodServerActor.class, server, name);
	}
	
	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof RemoteMethodCall) {
			final RemoteMethodCall remoteMethodCall = (RemoteMethodCall) message;
			final ActorRef sender = sender ();
			final ActorRef self = self ();
			final CompletableFuture<?> future;
			
			try {
				future = server.invokeMethod (remoteMethodCall);
			} catch (Throwable t) {
				log.error (t, "Remote method call " + remoteMethodCall + " has thrown an exception");
				sender.tell (new RemoteMethodCallFailure (t), self);
				return;
			}
			
			future.whenComplete ((value, throwable) -> {
				if (throwable != null) {
					log.error (throwable, "Remote method call " + remoteMethodCall + " completed exceptionally");
					sender.tell (new RemoteMethodCallFailure (throwable), self);
				} else {
					if (value instanceof Publisher && ! (value instanceof AkkaSerializablePublisher)) {
						// Wrap publisher in a serializable publisher:
						@SuppressWarnings({ "unchecked", "rawtypes" })
						final ActorRef publisherActor = getContext ().actorOf (SerializablePublisherActor.props ((Publisher) value, 5000));
						sender.tell (new AkkaSerializablePublisher<Object> (getContext (), CompletableFuture.completedFuture (publisherActor)), self);
					} else {
						sender.tell (value, self);
					}
				}
			});
		} else {
			unhandled (message);
		}
	}
}

package nl.idgis.geoide.commons.remote.transport;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Publisher;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.util.ByteString;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.remote.RemoteMethodCall;
import nl.idgis.geoide.commons.remote.RemoteMethodServer;
import nl.idgis.geoide.commons.remote.transport.messages.RemoteMethodCallFailure;
import nl.idgis.geoide.util.streams.AkkaSerializablePublisher;
import nl.idgis.geoide.util.streams.SerializablePublisherActor;

public class RemoteMethodServerActor extends UntypedActor {

	private final RemoteMethodServer server;
	private final long streamTimeoutInMillis;
	
	public RemoteMethodServerActor (final RemoteMethodServer server, final String name, final long streamTimeoutInMillis) {
		this.server = server;
		this.streamTimeoutInMillis = streamTimeoutInMillis;
	}
	
	public static Props props (final RemoteMethodServer server, final String name, final long streamTimeoutInMillis) {
		if (server == null) {
			throw new NullPointerException ("server cannot be null");
		}
		if (name == null) {
			throw new NullPointerException ("name cannot be null");
		}
		
		return Props.create (RemoteMethodServerActor.class, server, name, streamTimeoutInMillis);
	}
	
	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof RemoteMethodCall) {
			final RemoteMethodCall remoteMethodCall = (RemoteMethodCall) message;
			final ActorRef sender = sender ();
			final ActorRef self = self ();
			
			final CompletableFuture<?> future = server.invokeMethod (remoteMethodCall);
			
			future.whenComplete ((value, throwable) -> {
				if (throwable != null) {
					sender.tell (new RemoteMethodCallFailure (throwable), self);
				} else {
					if (value instanceof Document) {
						final Document document = (Document) value;
						final ActorRef streamActor = getContext ().actorOf (SerializablePublisherActor.props (document.getBody (), streamTimeoutInMillis));
						
						try {
							sender.tell (new SerializableDocument (
									document.getUri (), 
									document.getContentType (), 
									new AkkaSerializablePublisher<> (getContext (), CompletableFuture.completedFuture (streamActor))
								), self);
						} catch (URISyntaxException e) {
							sender.tell (new RemoteMethodCallFailure (e), self);
						}
					} else {
						sender.tell (value, self);
					}
				}
			});
		} else {
			unhandled (message);
		}
	}
	
	private static class SerializableDocument implements Document, Serializable {
		private static final long serialVersionUID = -37444201173006054L;
		
		private final URI uri;
		private final MimeContentType contentType;
		private final Publisher<ByteString> body;
		
		public SerializableDocument (final URI uri, final MimeContentType contentType, final Publisher<ByteString> body) {
			this.uri = uri;
			this.contentType = contentType;
			this.body = body;
		}
		
		@Override
		public URI getUri () throws URISyntaxException {
			return uri;
		}

		@Override
		public MimeContentType getContentType () {
			return contentType;
		}

		@Override
		public Publisher<ByteString> getBody () {
			return body;
		}
	}
}

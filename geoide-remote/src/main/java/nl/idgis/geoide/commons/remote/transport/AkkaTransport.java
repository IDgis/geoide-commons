package nl.idgis.geoide.commons.remote.transport;

import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteMethodServer;
import nl.idgis.geoide.commons.remote.transport.messages.AddListener;
import nl.idgis.geoide.commons.remote.transport.messages.PerformMethodCall;
import nl.idgis.geoide.commons.remote.transport.messages.RemoteMethodCallFailure;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSelection;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;

public class AkkaTransport {

	private final ActorRefFactory actorRefFactory;
	private final ActorRef transportActor;
	private final long timeoutInMillis;
	
	public AkkaTransport (final ActorRefFactory actorRefFactory, final String actorName, final long timeoutInMillis) {
		if (actorRefFactory == null) {
			throw new NullPointerException ("actorRefFactory cannot be null");
		}
		if (actorName == null) {
			throw new NullPointerException ("actorName cannot be null");
		}
		
		this.actorRefFactory = actorRefFactory;
		this.transportActor = actorRefFactory.actorOf (TransportActor.props (timeoutInMillis), actorName);
		this.timeoutInMillis = timeoutInMillis;
	}
	
	public RemoteMethodClient connect (final String remoteAddress, final String serverName) {
		final ActorSelection selection = actorRefFactory.actorSelection (remoteAddress);

		return (remoteMethodCall) -> {
			final CompletableFuture<Object> future = new CompletableFuture<> ();
			final Future<Object> akkaFuture = Patterns.ask (selection, new PerformMethodCall (serverName, remoteMethodCall), timeoutInMillis);
			
			akkaFuture.onComplete (new OnComplete<Object> () {
				@Override
				public void onComplete (final Throwable ex, final Object result) throws Throwable {
					if (result instanceof RemoteMethodCallFailure) {
						future.completeExceptionally (((RemoteMethodCallFailure) result).getCause ());
					} else {
						future.complete (result);
					}
				}
			}, actorRefFactory.dispatcher ());
			
			return future;
		};
	}
	
	public void listen (final RemoteMethodServer server, final String name) {
		transportActor.tell (new AddListener (server, name), transportActor);
	}
}

package nl.idgis.geoide.commons.remote.transport;

import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteMethodServer;
import scala.Function1;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSelection;
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
		this.transportActor = actorRefFactory.actorOf (TransportActor.props (), actorName);
		this.timeoutInMillis = timeoutInMillis;
	}
	
	public RemoteMethodClient connect (final String remoteAddress) {
		final ActorSelection selection = actorRefFactory.actorSelection (remoteAddress);

		return (remoteMethodCall) -> {
			final CompletableFuture<?> future = new CompletableFuture<> ();
			final Future<Object> akkaFuture = Patterns.ask (selection, remoteMethodCall, timeoutInMillis);
			
			akkaFuture.onComplete (new Function1<Try<Object>, U> () {
			}, actorRefFactory.dispatcher ());
			
			return null;
		};
	}
	
	public void listen (final RemoteMethodServer server, final String name) {
	}
}

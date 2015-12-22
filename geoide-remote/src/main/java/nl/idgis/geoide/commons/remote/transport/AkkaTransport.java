package nl.idgis.geoide.commons.remote.transport;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.idgis.geoide.commons.remote.RemoteMethodCall;
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

/**
 * A transport for remote method calls that uses Akka to transfer messages between client and server.
 */
public class AkkaTransport {
	
	private final Logger log = LoggerFactory.getLogger (AkkaTransport.class);

	private final ActorRefFactory actorRefFactory;
	private final ActorRef transportActor;
	private final long timeoutInMillis;

	/**
	 * Constructs a new AkkaTransport.
	 * 
	 * @param actorRefFactory	An Akka {@link ActorRefFactory} to use for creating actors used by the transport. Cannot be null.
	 * @param actorName			The name to use for the base actor of the transport. Cannot be null.
	 * @param timeoutInMillis	The timeout to use for communication over the Akka channel. In milliseconds.
	 */
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

	/**
	 * Creates a {@link RemoteMethodClient} by connecting to a remote transport identified by the remoteAddress
	 * (an actor reference) and the given server name.
	 * 
	 * @param remoteAddress	The address of the remote transport actor to connect with.
	 * @param serverName	The name of the server to connect with.
	 * @return				A {@link RemoteMethodClient} that dispatches method invocations to the remote actor.
	 */
	public RemoteMethodClient connect (final String remoteAddress, final String serverName) {
		final ActorSelection selection = actorRefFactory.actorSelection (remoteAddress);

		return (remoteMethodCall) -> {
			final CompletableFuture<Object> future = new CompletableFuture<> ();
			final Future<Object> akkaFuture = Patterns.ask (selection, new PerformMethodCall (serverName, remoteMethodCall), timeoutInMillis);
			
			akkaFuture.onComplete (new OnComplete<Object> () {
				@Override
				public void onComplete (final Throwable ex, final Object result) throws Throwable {
					if (result instanceof RemoteMethodCallFailure) {
						log.debug ("Remote method call to " + remoteMethodCall + " resulted in exception", ((RemoteMethodCallFailure) result).getCause ());
						future.completeExceptionally (((RemoteMethodCallFailure) result).getCause ());
					} else if (ex != null) {
						log.debug ("Exception while performing remote method call " + remoteMethodCall, ex);
						future.completeExceptionally (ex);
					} else {
						future.complete (result);
					}
				}
			}, actorRefFactory.dispatcher ());
			
			return future;
		};
	}
	
	/**
	 * Starts listening for method calls and dispatches them to the given {@link RemoteMethodServer}.
	 * The provided name is used to identify the {@link RemoteMethodServer}.
	 * 
	 * @param server	The server that accepts {@link RemoteMethodCall}'s.
	 * @param name		The name of the server.
	 */
	public void listen (final RemoteMethodServer server, final String name) {
		transportActor.tell (new AddListener (server, name), transportActor);
	}
}

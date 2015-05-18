package nl.idgis.geoide.commons.remote.transport;

import java.util.concurrent.CompletableFuture;


import nl.idgis.geoide.commons.remote.RemoteMethodCall;
import nl.idgis.geoide.commons.remote.RemoteMethodServer;
import nl.idgis.geoide.commons.remote.transport.messages.RemoteMethodCallFailure;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class RemoteMethodServerActor extends UntypedActor {

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
			
			final CompletableFuture<?> future = server.invokeMethod (remoteMethodCall);
			
			future.whenComplete ((value, throwable) -> {
				if (throwable != null) {
					sender.tell (new RemoteMethodCallFailure (throwable), self);
				} else {
					sender.tell (value, self);
				}
			});
		} else {
			unhandled (message);
		}
	}
}

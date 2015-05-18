package nl.idgis.geoide.commons.remote.transport;

import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.commons.remote.transport.messages.AddListener;
import nl.idgis.geoide.commons.remote.transport.messages.PerformMethodCall;
import nl.idgis.geoide.commons.remote.transport.messages.RemoteMethodCallFailure;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class TransportActor extends UntypedActor {

	private final Map<String, ActorRef> listeners = new HashMap<> ();
	
	public static Props props () {
		return Props.create (TransportActor.class);
	}
	
	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof AddListener) {
			final AddListener addListener = (AddListener) message;
			
			listeners
				.put (
					addListener.getName (), 
					context ().actorOf (RemoteMethodServerActor.props (addListener.getServer (), addListener.getName ()), addListener.getName ())
				);
		} else if (message instanceof PerformMethodCall) {
			final PerformMethodCall performMethodCall = (PerformMethodCall) message;
			final ActorRef listenerActorRef = listeners.get (performMethodCall.getServerName ());
			
			if (listenerActorRef == null) {
				sender ().tell (new RemoteMethodCallFailure (new IllegalStateException ("No such listener: " + performMethodCall.getServerName ())), self ());
				return;
			} 
			
			listenerActorRef.tell (performMethodCall.getMethodCall (), sender ());
		} else {
			unhandled (message);
		}
	}

}

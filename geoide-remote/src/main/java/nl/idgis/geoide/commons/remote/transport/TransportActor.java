package nl.idgis.geoide.commons.remote.transport;

import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.commons.remote.transport.messages.AddListener;
import nl.idgis.geoide.commons.remote.transport.messages.PerformMethodCall;
import nl.idgis.geoide.commons.remote.transport.messages.RemoteMethodCallFailure;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class TransportActor extends UntypedActor {

	private final LoggingAdapter log = Logging.getLogger (getContext ().system (), this);
	
	private final Map<String, ActorRef> listeners = new HashMap<> ();
	private final long streamTimeoutInMillis;
	
	public TransportActor (final long streamTimeoutInMillis) {
		this.streamTimeoutInMillis = streamTimeoutInMillis;
	}
	
	public static Props props (final long streamTimeoutInMillis) {
		return Props.create (TransportActor.class, streamTimeoutInMillis);
	}
	
	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof AddListener) {
			final AddListener addListener = (AddListener) message;
	
			final ActorRef ref = context ().actorOf (RemoteMethodServerActor.props (addListener.getServer (), addListener.getName (), streamTimeoutInMillis), addListener.getName ());
			
			listeners
				.put (
					addListener.getName (), 
					ref
				);
			
			log.info ("Registering remote method listener: " + addListener.getName () + " (" + ref + ")");
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

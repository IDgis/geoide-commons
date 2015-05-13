package nl.idgis.geoide.commons.remote.transport;

import akka.actor.Props;
import akka.actor.UntypedActor;

public class TransportActor extends UntypedActor {

	public static Props props () {
		return Props.create (TransportActor.class);
	}
	
	
	@Override
	public void onReceive (final Object message) throws Exception {
		// TODO Auto-generated method stub
		
	}

}

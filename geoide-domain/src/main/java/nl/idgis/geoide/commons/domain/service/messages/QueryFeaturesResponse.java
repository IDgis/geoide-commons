package nl.idgis.geoide.commons.domain.service.messages;

import nl.idgis.geoide.commons.domain.ServiceIdentification;
import akka.actor.ActorRef;

public class QueryFeaturesResponse extends ServiceMessage {
	private static final long serialVersionUID = -6147986331229927464L;
	
	private final ActorRef producer;
	
	public QueryFeaturesResponse (final ServiceIdentification serviceIdentification, final ActorRef producer) {
		super(serviceIdentification);
		
		if (producer == null) {
			throw new NullPointerException ("producer cannot be null");
		}
		
		this.producer = producer;
	}
	
	public ActorRef producer () {
		return producer;
	}
}

package nl.idgis.planoview.service.messages;

import akka.actor.ActorRef;

public final class ServiceMessageContext {
	private final ActorRef sender;
	private final ServiceMessage originalMessage;
	
	public ServiceMessageContext (final ActorRef sender, final ServiceMessage originalMessage) {
		if (sender == null) {
			throw new NullPointerException ("sender cannot be null");
		}
		
		this.sender = sender;
		this.originalMessage = originalMessage;
	}
	
	public ActorRef sender () {
		return sender;
	}
	
	public ServiceMessage originalMessage () {
		return originalMessage;
	}
}
package nl.idgis.geoide.util.streams;

import java.util.Objects;

import akka.actor.ActorRef;

public class AkkaPublisherReference<T> implements PublisherReference<T> {
	private static final long serialVersionUID = 7637290434016708053L;
	
	private final ActorRef actorRef;
	
	public AkkaPublisherReference (final ActorRef actorRef) {
		this.actorRef = Objects.requireNonNull (actorRef, "actorRef cannot be null");
	}
	
	public ActorRef getActorRef () {
		return actorRef;
	}
}

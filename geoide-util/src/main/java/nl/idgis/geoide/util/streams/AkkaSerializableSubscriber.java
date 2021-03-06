package nl.idgis.geoide.util.streams;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import nl.idgis.geoide.util.streams.messages.SubscriberComplete;
import nl.idgis.geoide.util.streams.messages.SubscriberError;

public class AkkaSerializableSubscriber<T> implements Subscriber<T> {
	private final ActorRef actorRef;
	private final ActorRefFactory factory;
	
	public AkkaSerializableSubscriber (final ActorRefFactory factory, final ActorRef actor) {
		this.factory = factory;
		this.actorRef = actor;
	}
	
	public ActorRef getActorRef () {
		return actorRef;
	}
	
	public ActorRefFactory getActorRefFactory () {
		return factory;
	}
	
	@Override
	public void onComplete () {
		actorRef.tell (new SubscriberComplete (), actorRef);
	}

	@Override
	public void onError (final Throwable t) {
		actorRef.tell (new SubscriberError (t), actorRef);
	}

	@Override
	public void onNext (final T t) {
		actorRef.tell (t, actorRef);
	}

	@Override
	public void onSubscribe (final Subscription subscription) {
		actorRef.tell (subscription, actorRef);
	}
}
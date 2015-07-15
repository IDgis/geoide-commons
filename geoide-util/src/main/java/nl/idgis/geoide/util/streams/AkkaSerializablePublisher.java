package nl.idgis.geoide.util.streams;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;

public class AkkaSerializablePublisher<T> implements Publisher<T> {
	private final ActorRef actor;
	private final ActorRefFactory factory;
	
	public AkkaSerializablePublisher (final ActorRefFactory factory, final CompletableFuture<ActorRef> actor) {
		if (factory == null) {
			throw new NullPointerException ("factory cannot be null");
		}
		if (actor == null) {
			throw new NullPointerException ("actor cannot be null");
		}
		
		try {
			this.actor = actor.get ();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException (e);
		}
		
		this.factory = factory;
	}
	
	public ActorRef getActorRef () {
		return actor;
	}
	
	public ActorRefFactory getActorRefFactory () {
		return factory;
	}
	
	@Override
	public void subscribe (final Subscriber<? super T> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}
		
		final ActorRef subscriberActor = factory.actorOf (AkkaSerializableSubscriberActor.props (subscriber));
		final Subscriber<? super T> serializableSubscriber = new AkkaSerializableSubscriber<> (factory, subscriberActor);
				
		actor.tell (serializableSubscriber, actor);
	}
}

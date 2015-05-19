package nl.idgis.geoide.util.streams;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.reactivestreams.Subscriber;

import akka.actor.ActorRef;

public class AkkaSerializablePublisher<T> implements SerializablePublisher<T> {
	private static final long serialVersionUID = -9152887082148053721L;
	
	private final ActorRef actor;
	
	public AkkaSerializablePublisher (final CompletableFuture<ActorRef> actor) {
		if (actor == null) {
			throw new NullPointerException ("actor cannot be null");
		}
		
		try {
			this.actor = actor.get ();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException (e);
		}
	}
	
	@Override
	public void subscribe (final Subscriber<? super T> subscriber) {
		actor.tell (subscriber, actor);
	}
}

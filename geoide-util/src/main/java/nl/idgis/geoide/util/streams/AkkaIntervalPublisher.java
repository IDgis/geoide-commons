package nl.idgis.geoide.util.streams;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Subscriber;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.pattern.Patterns;
import nl.idgis.geoide.util.AkkaFutures;

public class AkkaIntervalPublisher implements IntervalPublisher {
	private final ActorRefFactory factory;
	private final ActorRef actor;
	
	public AkkaIntervalPublisher (final ActorRefFactory factory, final long intervalInMillis) {
		if (intervalInMillis <= 0) {
			throw new IllegalArgumentException ("intervalInMillis should be > 0");
		}
		
		this.factory = Objects.requireNonNull (factory, "factory cannot be null");
		this.actor = factory.actorOf (AkkaIntervalPublisherActor.props (intervalInMillis));
	}
	
	@Override
	public void subscribe (final Subscriber<? super Long> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}
		
		actor.tell (subscriber, actor);
	}

	@Override
	public CompletableFuture<Void> stop () {
		return AkkaFutures.asCompletableFuture (Patterns.ask (actor, new AkkaIntervalPublisherActor.Stop (), 1000), factory.dispatcher ())
				.thenApply ((obj) -> null);
	}

}

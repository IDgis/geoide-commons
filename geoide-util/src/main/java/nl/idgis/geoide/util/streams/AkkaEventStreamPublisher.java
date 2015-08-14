package nl.idgis.geoide.util.streams;

import java.util.Objects;

import org.reactivestreams.Subscriber;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;

public class AkkaEventStreamPublisher<T> implements EventStreamPublisher<T> {
	private final ActorRef actor;
	
	public AkkaEventStreamPublisher (final ActorRefFactory factory, final int windowSize) {
		actor = factory.actorOf (AkkaEventStreamPublisherActor.props (windowSize));
	}

	@Override
	public void subscribe (final Subscriber<? super T> subscriber) {
		actor.tell (subscriber, actor);
	}

	@Override
	public void publish (final T event) {
		actor.tell (new Publish (Objects.requireNonNull (event, "event cannot be null")), actor);
	}

	@Override
	public void complete() {
		actor.tell (new Complete (), actor);
	}

	public static class Publish {
		private final Object event;
		
		public Publish (final Object event) {
			this.event = event;
		}
		
		public Object getEvent () {
			return event;
		}
	}
	
	public static class Complete { 
	}
	
	public static class Terminate {
	}
}

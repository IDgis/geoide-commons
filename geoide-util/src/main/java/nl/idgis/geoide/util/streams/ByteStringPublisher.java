package nl.idgis.geoide.util.streams;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import akka.actor.ActorRef;
import akka.util.ByteString;

public class ByteStringPublisher implements Publisher<ByteString> {

	private final ActorRef actor;
	
	public ByteStringPublisher (final ActorRef actor) {
		if (actor == null) {
			throw new NullPointerException ("actor cannot be null");
		}
		
		this.actor = actor;
	}
	
	@Override
	public void subscribe (final Subscriber<? super ByteString> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}

		actor.tell (subscriber, actor);
	}
}

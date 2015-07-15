package nl.idgis.geoide.util.streams;

import org.reactivestreams.Subscriber;

import akka.actor.Props;
import akka.actor.UntypedActor;

class AkkaSerializableSubscriberActor extends UntypedActor {
	private final Subscriber<?> wrappedSubscriber;
	
	public AkkaSerializableSubscriberActor (final Subscriber<?> wrappedSubscriber) {
		this.wrappedSubscriber = wrappedSubscriber;
	}
	
	public static Props props (final Subscriber<?> wrappedSubscriber) {
		return Props.create (AkkaSerializableSubscriberActor.class, wrappedSubscriber);
	}

	@Override
	public void onReceive (final Object message) throws Exception {
		unhandled (message);
	}
}
package nl.idgis.geoide.util.streams;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.dungeon.ReceiveTimeout;
import nl.idgis.geoide.util.streams.messages.SubscriberComplete;
import nl.idgis.geoide.util.streams.messages.SubscriberError;
import scala.concurrent.duration.Duration;

class AkkaSerializableSubscriberActor extends UntypedActor {
	private final Subscriber<?> wrappedSubscriber;
	private final long timeoutInMillis;
	
	public AkkaSerializableSubscriberActor (final Subscriber<?> wrappedSubscriber, final long timeoutInMillis) {
		this.wrappedSubscriber = wrappedSubscriber;
		this.timeoutInMillis = timeoutInMillis;
	}
	
	public static Props props (final Subscriber<?> wrappedSubscriber, final long timeoutInMillis) {
		return Props.create (AkkaSerializableSubscriberActor.class, wrappedSubscriber, timeoutInMillis);
	}
	
	@Override
	public void preStart () throws Exception {
		getContext ().setReceiveTimeout (Duration.create (timeoutInMillis, TimeUnit.MILLISECONDS));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof SubscriberComplete) {
			wrappedSubscriber.onComplete ();
			getContext ().stop (self ());
		} else if (message instanceof SubscriberError) {
			wrappedSubscriber.onError (((SubscriberError) message).getThrowable ());
			getContext ().stop (self ());
		} else if (message instanceof Subscription) {
			wrappedSubscriber.onSubscribe ((Subscription) message);
		} else if (message instanceof ReceiveTimeout) {
			wrappedSubscriber.onError (new TimeoutException ("Subscriber didn't receive any messages for " + timeoutInMillis + " milliseconds"));
			getContext ().stop (self ());
		} else {
			((Subscriber) wrappedSubscriber).onNext (message);
		}
	}
}
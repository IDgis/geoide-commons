package nl.idgis.geoide.util.streams;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.dungeon.ReceiveTimeout;
import nl.idgis.geoide.util.streams.messages.SubscriberCancelled;
import nl.idgis.geoide.util.streams.messages.SubscriberComplete;
import nl.idgis.geoide.util.streams.messages.SubscriberError;
import scala.concurrent.duration.Duration;

class AkkaSerializableSubscriberActor extends UntypedActor {
	private Subscriber<?> wrappedSubscriber;
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
		if (wrappedSubscriber == null) {
			unhandled (message);
			return;
		}
		
		if (message instanceof SubscriberComplete) {
			wrappedSubscriber.onComplete ();
			getContext ().stop (self ());
			wrappedSubscriber = null;
		} else if (message instanceof SubscriberError) {
			wrappedSubscriber.onError (((SubscriberError) message).getThrowable ());
			getContext ().stop (self ());
			wrappedSubscriber = null;
		} else if (message instanceof Subscription) {
			final Subscription subscription = (Subscription) message;
			final ActorRef self = self ();
			
			wrappedSubscriber.onSubscribe (new Subscription () {
				@Override
				public void request (final long count) {
					subscription.request (count);
				}
				
				@Override
				public void cancel () {
					subscription.cancel ();
					self.tell (new SubscriberCancelled (), self);
				}
			});
		} else if (message instanceof SubscriberCancelled) {
			getContext ().stop (self ());
			wrappedSubscriber = null;
		} else if (message instanceof ReceiveTimeout) {
			wrappedSubscriber.onError (new TimeoutException ("Subscriber didn't receive any messages for " + timeoutInMillis + " milliseconds"));
			getContext ().stop (self ());
			wrappedSubscriber = null;
		} else {
			((Subscriber) wrappedSubscriber).onNext (message);
		}
	}
}
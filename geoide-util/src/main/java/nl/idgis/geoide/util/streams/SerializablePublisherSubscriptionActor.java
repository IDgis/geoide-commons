package nl.idgis.geoide.util.streams;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.idgis.geoide.util.streams.messages.PublisherCancel;
import nl.idgis.geoide.util.streams.messages.PublisherRequest;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import scala.concurrent.duration.Duration;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.Identify;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import akka.japi.Procedure;

public class SerializablePublisherSubscriptionActor extends UntypedActor {

	private final ActorRef publisherActor;
	private final Publisher<? extends Serializable> publisher;
	private final Subscriber<?> subscriber;
	private final long timeoutInMillis;
	
	private static enum Messages {
		COMPLETE
	}
	
	public SerializablePublisherSubscriptionActor (final ActorRef publisherActor, final Publisher<? extends Serializable> publisher, final Subscriber<?> subscriber, final long timeoutInMillis) {
		this.publisherActor = publisherActor;
		this.publisher = publisher;
		this.subscriber = subscriber;
		this.timeoutInMillis = timeoutInMillis;
	}
	
	public static Props props (final ActorRef publisherActor, final Publisher<? extends Serializable> publisher, final Subscriber<?> subscriber, final long timeoutInMillis) {
		if (publisherActor == null) {
			throw new NullPointerException ("publisherActor cannot be null");
		}
		if (publisher == null) {
			throw new NullPointerException ("publisher cannot be null");
		}
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}
		
		return Props.create (SerializablePublisherSubscriptionActor.class, publisherActor, publisher, subscriber, timeoutInMillis);
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();

		final ActorRef self = self ();
		
		publisher.subscribe (new Subscriber<Object> () {
			@Override
			public void onComplete () {
				self.tell (Messages.COMPLETE, self ());
			}

			@Override
			public void onError (final Throwable throwable) {
				self.tell (throwable, self ());
			}

			@Override
			public void onNext (final Object value) {
				self.tell (value, self ());
			}

			@Override
			public void onSubscribe (final Subscription subscription) {
				self.tell (subscription, self ());
			}
		});
		
		getContext ().setReceiveTimeout (Duration.create (timeoutInMillis, TimeUnit.MILLISECONDS));
	}
	
	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof ReceiveTimeout) {
			subscriber.onError (new TimeoutException ());
			getContext ().stop (self ());
		} else if (message instanceof ActorIdentity) {
			// Ignore identity messages.
			return;
		} else if (message instanceof Throwable) {
			subscriber.onError ((Throwable) message);
			getContext ().stop (self ());
		} else if (Messages.COMPLETE.equals (message)) {
			subscriber.onComplete ();
			getContext ().stop (self ());
		} else if (message instanceof Subscription) {
			getContext ().become (subscribedToSource ((Subscription) message));
		} else if (message instanceof Serializable) {
			@SuppressWarnings("unchecked")
			final Subscriber<Object> untypedSubscriber = (Subscriber<Object>) this.subscriber;
			untypedSubscriber.onNext (message);
			publisherActor.tell (new Identify ("identify"), self ());
		} else {
			unhandled (message);
		}
	}
	
	private Procedure<Object> subscribedToSource (final Subscription sourceSubscription) {
		final ActorRef self = self ();
		
		subscriber.onSubscribe (new Subscription () {
			@Override
			public void request (final long count) {
				if (count <= 0) {
					subscriber.onError (new IllegalArgumentException ("Count < 0 (3.9)"));
					return;
				}
				self.tell (new PublisherRequest (count), self);
			}
			
			@Override
			public void cancel () {
				self.tell (new PublisherCancel (), self);
			}
		});
		
		return (message) -> {
			if (message instanceof ReceiveTimeout) {
				sourceSubscription.cancel ();
				subscriber.onError (new TimeoutException ());
				getContext ().stop (self ());
			} else if (message instanceof PublisherRequest) {
				sourceSubscription.request (((PublisherRequest) message).getCount ());
			} else if (message instanceof PublisherCancel) {
				sourceSubscription.cancel ();
				getContext ().stop (self ());
			} else {
				onReceive (message);
			}
		};
	}
}

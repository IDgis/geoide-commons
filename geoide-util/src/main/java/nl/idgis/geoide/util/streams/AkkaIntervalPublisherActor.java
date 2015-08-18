package nl.idgis.geoide.util.streams;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.Procedure;
import scala.concurrent.duration.Duration;

public class AkkaIntervalPublisherActor extends UntypedActor {

	private final long intervalInMillis;
	private final Set<ActorRef> children = new HashSet<> ();
	
	public AkkaIntervalPublisherActor (final long intervalInMillis) {
		this.intervalInMillis = intervalInMillis;
	}
	
	public static Props props (final long intervalInMillis) {
		return Props.create (AkkaIntervalPublisherActor.class, intervalInMillis);
	}

	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof Subscriber) {
			@SuppressWarnings("unchecked")
			final Subscriber<? super Long> subscriber = (Subscriber<? super Long>) message;
			final ActorRef child = getContext ().actorOf (SubscriptionActor.props (subscriber, intervalInMillis));
			children.add (child);
			getContext ().watch (child);
		} else if (message instanceof Stop) {
			for (final ActorRef child: children) {
				child.tell (message, self ());
			}
			getContext ().become (stopping (sender ()));
		} else if (message instanceof Terminated) {
			children.remove (sender ());
		} else {
			unhandled (message);
		}
	}
	
	private Procedure<Object> stopping (final ActorRef notifyActor) {
		if (children.isEmpty ()) {
			getContext ().stop (self ());
			notifyActor.tell (new Stopped (), self ());
		}
		
		return (message) -> {
			if (message instanceof Terminated) {
				children.remove (sender ());
				getContext ().become (stopping (notifyActor));
			} else if (message instanceof Subscriber) {
				@SuppressWarnings("unchecked")
				final Subscriber<? super Long> subscriber = (Subscriber<? super Long>) message;
				subscriber.onError (new IllegalStateException ("No subscriptions are accepted when publisher is stopping"));
			} else {
				unhandled (message);
			}
		};
	}
	
	public final static class Stop { }
	public final static class Stopped { }
	
	private final static class SubscriptionActor extends UntypedActor {
		private final Subscriber<? super Long> subscriber;
		private final long intervalInMillis;
		private Cancellable cancellable = null;
		private long requested = 0;
		private long tickCount = 0;
		private boolean stopped = false;
		
		@SuppressWarnings("unused")
		public SubscriptionActor (final Subscriber<? super Long> subscriber, final long intervalInMillis) {
			this.subscriber = subscriber;
			this.intervalInMillis = intervalInMillis;
		}
		
		public static Props props (final Subscriber<? super Long> subscriber, final long intervalInMillis) {
			return Props.create (SubscriptionActor.class, subscriber, intervalInMillis);
		}
		
		@Override
		public void preStart () throws Exception {
			super.preStart();
			
			final ActorRef self = self ();
			
			subscriber.onSubscribe (new Subscription () {
				@Override
				public void request (final long count) {
					if (count <= 0) {
						throw new IllegalArgumentException ("count must be > 0");
					}

					self.tell (count, self);
				}
				
				@Override
				public void cancel () {
					self.tell (PoisonPill.getInstance (), self);
				}
			});
			
			cancellable = getContext ().system ().scheduler ().schedule (
					Duration.Zero (), 
					Duration.create (intervalInMillis, TimeUnit.MILLISECONDS),
					self (),
					"tick",
					getContext ().dispatcher (),
					self ()
				);
		}
		
		@Override
		public void postStop () throws Exception {
			super.postStop();

			if (cancellable != null) {
				cancellable.cancel ();
				cancellable = null;
			}
		}

		@Override
		public void onReceive (final Object message) throws Exception {
			if (message instanceof Long) {
				if (stopped) {
					return;
				}
				
				final long count = ((Long) message).longValue ();
				
				if (requested + count < 0) {
					requested = Long.MAX_VALUE;
				} else {
					requested += count;
				}
			} else if (message instanceof Stop) {
				stopped = true;
				subscriber.onComplete ();
				getContext ().stop (self ());
			} else if ("tick".equals (message)) {
				if (requested > 0) {
					subscriber.onNext (tickCount ++);
					-- requested;
				}
			} else {
				unhandled (message);
			}
		}
	}
}

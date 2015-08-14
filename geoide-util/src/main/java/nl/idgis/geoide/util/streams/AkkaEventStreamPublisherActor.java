package nl.idgis.geoide.util.streams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.Procedure;
import nl.idgis.geoide.util.IndexedRingBuffer;

public class AkkaEventStreamPublisherActor extends UntypedActor {
	private final Set<ActorRef> subscribers = new HashSet<> ();
	private final IndexedRingBuffer<Object>	buffer;
	private boolean completed = false;
	
	public AkkaEventStreamPublisherActor (final int windowSize) {
		this.buffer = new IndexedRingBuffer<> (windowSize);
	}
	
	public static Props props (final int windowSize) {
		return Props.create (AkkaEventStreamPublisherActor.class, windowSize);
	}

	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof Subscriber) {
			final ActorRef subscriberActor = getContext ().actorOf (SubscriptionActor.props ((Subscriber<?>) message));
			getContext ().watch (subscriberActor);
			subscribers.add (subscriberActor);
		} else if (message instanceof Terminated) {
			final ActorRef subscriberActor = sender ();
			subscribers.remove (subscriberActor);
		} else if (message instanceof AkkaEventStreamPublisher.Publish) {
			buffer.add (((AkkaEventStreamPublisher.Publish) message).getEvent ());
			for (final ActorRef subscriber: subscribers) {
				subscriber.tell (new HasMore (), self ());
			}
		} else if (message instanceof Request) {
			final Request request = (Request) message;
			final List<Object> items = new ArrayList<> ();
			final long startIndex = Math.max (request.getStartIndex (), buffer.getBaseIndex ());
			
			for (long i = startIndex; i < buffer.getSize (); ++ i) {
				items.add (buffer.get (i));
			}
			
			sender ().tell (new Response (items, startIndex, completed), self ());
		} else if (message instanceof AkkaEventStreamPublisher.Complete) {
			completed = true;
		} else {
			unhandled (message);
		}
	}

	public static class Request {
		private long startIndex;
		
		public Request (final long startIndex) {
			this.startIndex = startIndex;
		}
		
		public long getStartIndex () {
			return startIndex;
		}
	}
	
	public static class Response {
		private final List<Object> items;
		private final long index;
		private final boolean completed;
		
		public Response (final List<Object> items, final long index, final boolean completed) {
			this.items = items.isEmpty () ? Collections.emptyList () : new ArrayList<> (items);
			this.index = index;
			this.completed = completed;
		}
		
		public List<Object> getItems () {
			return Collections.unmodifiableList (items);
		}
		
		public long getIndex () {
			return index;
		}
		
		public boolean isCompleted () {
			return completed;
		}
	}
	
	public static class HasMore { }
	
	public static class SubscriptionActor extends UntypedActor {
		private final Subscriber<?> subscriber;
		private long requestCount = 0;
		private long startIndex = 0;
		
		public SubscriptionActor (final Subscriber<?> subscriber) {
			this.subscriber = subscriber;
		}
		
		public static Props props (final Subscriber<?> subscriber) {
			return Props.create (SubscriptionActor.class, subscriber);
		}

		@Override
		public void preStart () throws Exception {
			super.preStart();

			final ActorRef self = self ();
			
			subscriber.onSubscribe (new Subscription () {
				@Override
				public void request (final long count) {
					if (count <= 0) {
						throw new IllegalArgumentException ("count should be > 0");
					}
					
					self.tell (Long.valueOf (count), self);
				}
				
				@Override
				public void cancel () {
					self.tell (PoisonPill.getInstance (), self);
				}
			});
		}
		
		@Override
		public void onReceive (final Object message) throws Exception {
			if (message instanceof Long) {
				increment (((Long) message).longValue ());
				getContext ().become (request ());
			} else if (message instanceof HasMore) {
				if (requestCount > 0) {
					getContext ().become (request ());
				}
			} else {
				unhandled (message);
			}
		}
		
		private void increment (final long value) {
			if (requestCount + value < 0) {
				requestCount = Long.MAX_VALUE;
			} else {
				requestCount += value;
			}
		}
		
		private Procedure<Object> request () {
			getContext ().parent ().tell (new Request (startIndex), self ());
			
			return message -> {
				if (message instanceof Long) {
					increment (((Long) message).longValue ());
				} else if (message instanceof Response) {
					final Response response = (Response) message;
					final List<Object>items = response.getItems ();
					
					startIndex = response.getIndex ();
					
					for (int i = 0; i < items.size () && requestCount > 0; ++ i) {
						((Subscriber) subscriber).onNext (items.get (i));
						++ startIndex;
						-- requestCount;
					}
					
					if (requestCount > 0 && response.isCompleted ()) {
						// Terminate this subscriber if the event stream has completed:
						subscriber.onComplete ();
						getContext ().stop (self ());
					} else if (requestCount > 0) {
						// Request more items if there are pending requests:
						getContext ().become (request ());
					} else {
						// Return to the pending state±
						getContext ().unbecome ();
					}
				} else {
					unhandled (message);
				}
			};
		}
	}
}

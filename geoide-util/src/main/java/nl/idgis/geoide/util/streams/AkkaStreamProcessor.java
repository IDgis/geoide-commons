package nl.idgis.geoide.util.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import play.libs.F.Function2;
import play.libs.F.Promise;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

/**
 * An {@link StreamProcessor} implementation that uses an Akka actor system for scheduling.
 */
public class AkkaStreamProcessor implements StreamProcessor {

	private final ActorRefFactory actorRefFactory;

	/**
	 * Creates a new AkkaStreamProcessor by providing an ActorRefFectory on which
	 * new actor references can be created for scheduling purposes.
	 * 
	 * @param actorRefFactory The ActorRefFactory to use by this StreamProcessor.
	 */
	public AkkaStreamProcessor (final ActorRefFactory actorRefFactory) {
		if (actorRefFactory == null) {
			throw new NullPointerException ("actorRefFactory is null");
		}
		
		this.actorRefFactory = actorRefFactory;
	}
	
	/**
	 * Returns the ActorRefFactory used by this stream processor, set using the constructor.
	 * 
	 * @return The ActorRefFactory of this stream processor.
	 */
	public ActorRefFactory getActorRefFactory () {
		return actorRefFactory;
	}

	/**
	 * Uses {@link SingleValuePublisher} to create a publisher that produces a single value.
	 * 
	 * @see StreamProcessor#publishSinglevalue(Object)
	 */
	@Override
	public <T> Publisher<T> publishSinglevalue (final T value) {
		return new SingleValuePublisher<T> (value);
	}

	/**
	 * Doesn't use the scheduler, reduces elements in the stream when they arrive.
	 * 
	 * @see StreamProcessor#reduce(Publisher, Object, Function2)
	 */
	@Override
	public <T> Promise<T> reduce (final Publisher<T> publisher, final T initialValue, final Function2<T, T, T> reducer) {
        final scala.concurrent.Promise<T> scalaPromise = scala.concurrent.Promise$.MODULE$.<T>apply ();
        final long n = 1;
        
        publisher.subscribe (new Subscriber<T> () {
        	private Subscription subscription;
        	private T data = initialValue;
        	private long requestSize = 0;
        	
			@Override
			public void onSubscribe (final Subscription s) {
				this.subscription = s;
				request ();
			}

			@Override
			public void onNext (final T t) {
				try {
					data = reducer.apply (data, t);
				} catch (Throwable cause) {
					scalaPromise.failure (cause);
				}
				if (requestSize > 0) {
					-- requestSize;
				}
				request ();
			}

			@Override
			public void onError (final Throwable t) {
				scalaPromise.failure (t);
			}

			@Override
			public void onComplete () {
				scalaPromise.success (data);
			}
			
			private void request () {
				if (requestSize > 0) {
					return;
				}
				
				requestSize = n;
				subscription.request (n);
			}
		});
        
        return Promise.wrap (scalaPromise.future ());
	}

	/**
	 * Uses a temporary Akka actor for scheduling.
	 * 
	 * @see StreamProcessor#publishInputStream(InputStream, int, long)
	 */
	@Override
	public Publisher<ByteString> publishInputStream (final InputStream inputStream, final int maxBlockSize, final long timeoutInMillis) {
		if (inputStream == null) {
			throw new NullPointerException ("inputStream cannot be null");
		}
		
		final ActorRef actor = actorRefFactory.actorOf (InputStreamPublishActor.props (inputStream, timeoutInMillis, maxBlockSize));
		
		return new InputStreamPublisher (actor);
	}

	/**
	 * Uses a temporary Akk actor for scheduling.
	 * 
	 * @see StreamProcessor#asInputStream(Publisher, long)
	 */
	@Override
	public InputStream asInputStream (final Publisher<ByteString> publisher, final long timeoutInMillis) {
		if (publisher == null) {
			throw new NullPointerException ("publisher cannot be null");
		}
		
		final ActorRef actor = actorRefFactory.actorOf (ConsumeByteStringsActor.props (publisher, timeoutInMillis));
		
		return new ByteStringInputStream (actor, timeoutInMillis);
	}
	
	/**
	 * An InputStream implementation that reads from an Akka actor that in turn wraps a reactive
	 * streams publisher.
	 */
	private final static class ByteStringInputStream extends InputStream {
		private final ActorRef actor;
		private final long timeout;
		
		public ByteStringInputStream (final ActorRef actor, final long timeout) {
			this.actor = actor;
			this.timeout = timeout;
		}

		@Override
		public int read () throws IOException {
			final byte[] b = new byte[1];
			final int nread = read (b, 0, 1);
			
			if (nread <= 0) {
				return -1;
			}
			
			return b[0] & 0xff;
		}
		
		@Override
		public void close () throws IOException {
			actor.tell ("close", actor);
		}
		
		@Override
	    public int read (final byte b[], final int off, final int len) throws IOException {
	        if (b == null) {
	            throw new NullPointerException();
	        } else if (off < 0 || len < 0 || len > b.length - off) {
	            throw new IndexOutOfBoundsException();
	        } else if (len == 0) {
	            return 0;
	        }
			
	        try {
	        	final Object response = Promise.wrap (Patterns.ask (actor, Integer.valueOf (len), timeout)).get (timeout);
	        	
	        	if (response instanceof ByteString) {
	        		final ByteString byteString = (ByteString) response;
	        		
	        		byteString.copyToArray (b, off);
	        		
	        		return byteString.length ();
	        	} else if ("complete".equals (response)) {
	        		return -1;
	        	} else if (response instanceof Throwable) {
	        		throw (Throwable) response;
	        	} else {
	        		throw new IOException (new IllegalStateException ("Unrecognized response of type " + response.getClass ().getCanonicalName ()));
	        	}
	        } catch (Throwable t) {
	        	if (t instanceof IOException) {
	        		throw (IOException) t;
	        	} else {
	        		throw new IOException (t);
	        	}
	        }
		}
	}
	
	/**
	 * An actor that consumes bytes from a publisher of type ByteString and sends the bytes to the caller.
	 * This actor takes care of scheduling and reactive pull on the publisher.
	 */
	public final static class ConsumeByteStringsActor extends UntypedActor {

		private final long timeout;
		private final Publisher<ByteString> publisher;

		private Subscription subscription = null;
		
		private Cancellable timer = null;
		
		private ActorRef currentTarget = null;
		private int currentLength = 0;
		private boolean isComplete = false;
		private ByteString currentData = null;
		private Throwable currentException = null;
		
		public ConsumeByteStringsActor (final Publisher<ByteString> publisher, final long timeout) {
			this.publisher = publisher;
			this.timeout = timeout;
		}
		
		public static Props props (final Publisher<ByteString> publisher, final long timeout) {
			return Props.create (ConsumeByteStringsActor.class, publisher, timeout);
		}
		
		@Override
		public void preStart() throws Exception {
			final ActorRef self = self ();
			
			publisher.subscribe (new Subscriber<ByteString> () {
				@Override
				public void onSubscribe (final Subscription s) {
					self.tell (s, self);
				}

				@Override
				public void onNext (final ByteString t) {
					self.tell (t, self);
				}

				@Override
				public void onError (final Throwable t) {
					self.tell (t, self);
				}

				@Override
				public void onComplete() {
					self.tell ("complete", self);
				}
			});
			
			scheduleTimeout ();
		}
		
		@Override
		public void postStop() throws Exception {
			if (!isComplete) {
				if (subscription != null) {
					subscription.cancel ();
				}
			}
			
			if (timer != null) {
				timer.cancel ();
			}
		}
		
		@Override
		public void onReceive (final Object message) throws Exception {
			if (message instanceof Subscription) {
				final Subscription subscription = (Subscription) message;
				this.subscription = subscription;
				
				currentData = null;
				currentException = null;
				isComplete = false;
				
				if (currentTarget != null) {
					subscription.request (1);
				}
			} else if (message instanceof ByteString) {
				currentData = (ByteString) message;
				maybeSendResponse ();
			} else if (message instanceof Throwable) {
				currentException = (Throwable) message;
				maybeSendResponse ();
			} else if ("complete".equals (message)) {
				isComplete = true;
				maybeSendResponse ();
			} else if (message instanceof Integer) {
				final int n = ((Integer) message).intValue ();
				
				if (!sendResponse (sender (), n)) {
					// Request new data:
					currentTarget = sender ();
					currentLength = n;
					if (subscription != null) {
						subscription.request (1);
					}
				}
				
				scheduleTimeout ();
			} else if ("timeout".equals (message)) {
				currentException = new IOException ("A timeout has occured while producing content for InputStream");
				context ().stop (self ());
			} else {
				unhandled (message);
			}
		}
		
		private void maybeSendResponse () {
			if (currentTarget != null) {
				sendResponse (currentTarget, currentLength);
				currentTarget = null;
				currentLength = 0;
			}
		}
		
		private boolean sendResponse (final ActorRef target, final int length) {
			if (currentException != null) {
				target.tell (currentException, self ());
				currentException = null;
				return true;
			} else if (currentData != null) {
				final int n = Math.min (currentData.length (), length);
				target.tell (currentData.take (n), self ());
				currentData = currentData.drop (n);
				if (currentData.isEmpty ()) {
					currentData = null;
				}
				return true;
			} else if (isComplete) {
				target.tell ("complete", self ());
				return true;
			}
			
			return false;
		}
		
		private void scheduleTimeout () {
			if (timer != null) {
				timer.cancel ();
			}
			
			timer = context ().system ().scheduler ().scheduleOnce (
					Duration.create (timeout, TimeUnit.MILLISECONDS), 
					self (), 
					"timeout",
					context ().dispatcher (),
					null
				);
		}
	}
	
	/**
	 * Reactive streams publisher for input streams. Uses an Akka actor that wraps the input stream.
	 */
	private final static class InputStreamPublisher implements Publisher<ByteString> {
		private final ActorRef actor;
		
		public InputStreamPublisher (final ActorRef actor) {
			this.actor = actor;
		}

		@Override
		public void subscribe (final Subscriber<? super ByteString> subscriber) {
			actor.tell (subscriber, actor);
		}
	}

	/**
	 * Reactive streams subscription for input streams. Uses an Akka actor that wraps the input stream.
	 */
	public final static class InputStreamSubscription implements Subscription {
		private final ActorRef self;
		
		public InputStreamSubscription (final ActorRef self) {
			this.self = self;
		}
		
		@Override
		public void request (final long n) {
			self.tell (Long.valueOf (n), self);
		}

		@Override
		public void cancel () {
			self.tell ("stop", self);
		}
	}
	
	/**
	 * An Akka actor that publishes an InputStream as a stream of ByteStrings of configurable length.
	 */
	public final static class InputStreamPublishActor extends UntypedActor {
		private final long timeoutInMillis;
		private final int blockSize;
		private final InputStream inputStream;

		private boolean completed = false;
		private Subscriber<? super ByteString> subscriber = null;
		private Cancellable timer = null;
		
		public InputStreamPublishActor (final InputStream inputStream, final long timeoutInMillis, final int blockSize) {
			this.blockSize = blockSize;
			this.inputStream = inputStream;
			this.timeoutInMillis = timeoutInMillis;
		}
		
		public static Props props (final InputStream inputStream, final long timeoutInMillis, final int blockSize) {
			return Props.create (InputStreamPublishActor.class, inputStream, timeoutInMillis, blockSize);
		}

		@Override
		public void preStart () throws Exception {
			scheduleTimeout ();
		}
		
		@Override
		public void postStop () throws Exception {
			// Close the inputstream:
			inputStream.close ();
			
			if (timer != null) {
				timer.cancel ();
			}
		}
		
		@Override
		public void onReceive (final Object message) throws Exception {
			if (message instanceof Subscriber) {
				// Set the subscriber, or raise an exception if a subscription is already present:
				@SuppressWarnings("unchecked")
				final Subscriber<? super ByteString> subscriber = (Subscriber<? super ByteString>) message;
				
				if (this.subscriber != null) {
					subscriber.onError (new IOException ("A subscriber is already connected to this stream"));
					return;
				}
				
				this.subscriber = subscriber;
				subscriber.onSubscribe (new InputStreamSubscription (self ()));
				
				scheduleTimeout ();
			} else if (message instanceof Long) {
				final long n = ((Long) message).longValue ();
				
				if (n <= 0) {
					subscriber.onError (new IllegalArgumentException ("Count must be > 0 (specification rule 3.9)"));
					return;
				}
				
				if (completed) {
					return;
				}
				
				try {
					final byte[] data = new byte[blockSize];
					for (long i = 0; i < n; ++ i) {
						final int nread = inputStream.read (data);
						
						if (nread < 0) {
							subscriber.onComplete ();
							completed = true;
							break;
						}
						
						subscriber.onNext (ByteStrings.fromArray (data, 0, nread));
					}
				} catch (IOException e) {
					subscriber.onError (e);
					completed = true;
					self ().tell ("stop", self ());
				}
				
				scheduleTimeout ();
			} else if ("stop".equals (message)) {
				// Stop the actor:
				context ().stop (self ());
				completed = true;
			} else if ("timeout".equals (message)) {
				System.err.println ("Timeout on actor: " + self ().toString ());
				if (subscriber != null) {
					subscriber.onError (new IOException ("The IO operation has timed out after: " + timeoutInMillis + " ms"));
				}
				completed = true;
				self ().tell ("stop", self ());
			} else {
				unhandled (message);
			}
		}
		
		private void scheduleTimeout () {
			if (timer != null) {
				timer.cancel ();
				timer = null;
			}
			
			timer = context ().system ().scheduler ().scheduleOnce (
					Duration.create (timeoutInMillis, TimeUnit.MILLISECONDS), 
					self (), 
					"timeout",
					context ().dispatcher (),
					self ()
				);
		}
	}
}

package nl.idgis.geoide.util.streams;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

/**
 * An {@link StreamProcessor} implementation that uses an Akka actor system for scheduling.
 */
public class AkkaStreamProcessor implements StreamProcessor, Closeable {

	private final ActorRefFactory actorRefFactory;
	private final ActorRef publishInputStreamContainer;
	private final ActorRef asInputStreamContainer;
	private final ActorRef serializablePublisherManager;

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
		this.publishInputStreamContainer = actorRefFactory.actorOf (ContainerActor.props (), "stream-processor-publish-input-stream");
		this.asInputStreamContainer = actorRefFactory.actorOf (ContainerActor.props (), "stream-processor-as-input-stream");
		this.serializablePublisherManager = actorRefFactory.actorOf (ContainerActor.props (), "publisher-manager");
	}
	
	/**
	 * Destroys actors for this stream processor.
	 */
	@Override
	public void close () {
		actorRefFactory.stop (publishInputStreamContainer);
		actorRefFactory.stop (asInputStreamContainer);
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
	 * {@inheritDoc}
	 */
	@Override
	public IntervalPublisher createIntervalPublisher (final long intervalInMillis) {
		return new AkkaIntervalPublisher (actorRefFactory, intervalInMillis);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> EventStreamPublisher<T> createEventStreamPublisher (final int windowSize, final long timeoutInMillis) {
		return new AkkaEventStreamPublisher<> (actorRefFactory, windowSize, timeoutInMillis);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> PublisherReference<T> createPublisherReference (final Publisher<T> publisher, final long timeoutInMillis) {
		if (timeoutInMillis <= 0) {
			throw new IllegalArgumentException ("timeoutInMillis must be > 0");
		}
		
		final ActorRef actor = actorRefFactory.actorOf (SerializablePublisherActor.props (
				Objects.requireNonNull (publisher, "publisher cannot be null"), 
				timeoutInMillis
			));
		
		return new AkkaPublisherReference<> (actor);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Publisher<T> resolvePublisherReference (final PublisherReference<T> publisherReference, final long timeoutInMillis) {
		if (!(Objects.requireNonNull (publisherReference, "publisherReference cannot be null") instanceof AkkaPublisherReference)) {
			throw new IllegalArgumentException ("Expected instance of " + AkkaPublisherReference.class.getCanonicalName ());
		}
		
		return new AkkaSerializablePublisher<T> (
				actorRefFactory, 
				CompletableFuture.completedFuture (((AkkaPublisherReference<T>) publisherReference).getActorRef ())
			);
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
	public <T> CompletableFuture<T> reduce (final Publisher<T> publisher, final T initialValue, final BiFunction<T, T, T> reducer) {
        final CompletableFuture<T> future = new CompletableFuture<> ();
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
					future.completeExceptionally (cause);
					return;
				}
				if (requestSize > 0) {
					-- requestSize;
				}
				request ();
			}

			@Override
			public void onError (final Throwable t) {
				future.completeExceptionally (t);
			}

			@Override
			public void onComplete () {
				future.complete (data);
			}
			
			private void request () {
				if (requestSize > 0) {
					return;
				}
				
				requestSize = n;
				subscription.request (n);
			}
		});
        
        return future;
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
		
		// Ask the container actor to create a new actor to process this stream:
		
		final CompletableFuture<ActorRef> actorPromise = createActor (
			publishInputStreamContainer, 
			InputStreamPublishActor.props (
				inputStream, 
				timeoutInMillis, 
				maxBlockSize
			),
			timeoutInMillis
		);
		
		return new InputStreamPublisher (actorPromise);
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
		
		// Ask the container actor to create an actor to process this stream:
		final CompletableFuture<ActorRef> actorPromise = createActor (
			asInputStreamContainer, 
			ConsumeByteStringsActor.props (
				publisher, 
				timeoutInMillis
			), 
			timeoutInMillis
		);
		
		try {
			return new ByteStringInputStream (actorPromise, timeoutInMillis, actorRefFactory.dispatcher ());
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			throw new RuntimeException (e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Publisher<T> asSerializable (final Publisher<T> publisher) {
		final CompletableFuture<ActorRef> actorFuture = createActor (serializablePublisherManager, SerializablePublisherActor.props (publisher, 10000l), 5000);
		return new AkkaSerializablePublisher<> (actorRefFactory, actorFuture);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override 
	public Publisher<ByteString> publishByteString (final ByteString input, final int blockSize) {
		if (input == null) {
			throw new NullPointerException ("input cannot be null");
		}
		if (blockSize < 0) {
			throw new NullPointerException ("blockSize must be > 0");
		}
		
		final ActorRef actor = actorRefFactory.actorOf (ByteStringPublisherActor.props (input, blockSize, 5000));
		
		return new ByteStringPublisher (actor);
	}
	
	private CompletableFuture<ActorRef> createActor (final ActorRef containerActor, final Props props, final long timeoutInMillis) {
		final Future<Object> scalaFuture = Patterns.ask (
				containerActor,
				props,
				timeoutInMillis
			);
		
		final CompletableFuture<ActorRef> future = new CompletableFuture<> ();
		
		scalaFuture.onComplete (new OnComplete<Object> () {
			@Override
			public void onComplete (final Throwable throwable, final Object result) throws Throwable {
				if (throwable != null) {
					future.completeExceptionally (throwable);
				} else if (result instanceof ActorRef){
					future.complete ((ActorRef) result);
				} else {
					future.completeExceptionally (new IllegalArgumentException ("Unknown response type: " + result.getClass ().getCanonicalName ()));
				}
			}
		}, actorRefFactory.dispatcher ());
		
		return future;
	}
	
	private static <T> CompletableFuture<T> wrapFuture (final Future<T> future, final ExecutionContext context) {
		final CompletableFuture<T> completableFuture = new CompletableFuture<> ();
		
		future.onComplete (new OnComplete<T> () {
			@Override
			public void onComplete (final Throwable t, final T value) throws Throwable {
				if (t != null) {
					completableFuture.completeExceptionally (t);
				} else {
					completableFuture.complete (value);
				}
			}
		}, context);
		
		return completableFuture;
	}
	
	/**
	 * An InputStream implementation that reads from an Akka actor that in turn wraps a reactive
	 * streams publisher.
	 */
	private final static class ByteStringInputStream extends InputStream {
		private final ActorRef actor;
		private final long timeout;
		private final ExecutionContext context;
		
		public ByteStringInputStream (final CompletableFuture<ActorRef> actorPromise, final long timeout, final ExecutionContext context) throws TimeoutException, InterruptedException, ExecutionException {
			this.actor = actorPromise.get (timeout, TimeUnit.MILLISECONDS);
			this.timeout = timeout;
			this.context = context;
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
	        	final Object response = wrapFuture (Patterns.ask (actor, Integer.valueOf (len), timeout), context).get (timeout, TimeUnit.MILLISECONDS);
	        	
	        	if (response instanceof ByteString) {
	        		final ByteString byteString = (ByteString) response;
	        		
	        		byteString.copyToArray (b, off);
	        		
	        		return byteString.size ();
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
		private final Publisher<? extends ByteString> publisher;

		private Subscription subscription = null;
		
		private Cancellable timer = null;
		
		private ActorRef currentTarget = null;
		private int currentLength = 0;
		private boolean isComplete = false;
		private ByteString currentData = null;
		private Throwable currentException = null;
		
		public ConsumeByteStringsActor (final Publisher<? extends ByteString> publisher, final long timeout) {
			this.publisher = publisher;
			this.timeout = timeout;
		}
		
		public static Props props (final Publisher<? extends ByteString> publisher, final long timeout) {
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
				final int n = Math.min (currentData.size (), length);
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
		private final CompletableFuture<ActorRef> actorPromise;
		
		public InputStreamPublisher (final CompletableFuture<ActorRef> actorPromise) {
			this.actorPromise = actorPromise;
		}

		@Override
		public void subscribe (final Subscriber<? super ByteString> subscriber) {
			if (subscriber == null) {
				throw new NullPointerException ("subscriber cannot be null");
			}
			
			actorPromise.handle ((actorRef, throwable) -> {
				if (throwable != null) {
					subscriber.onError (throwable);
					return null;
				}
				
				actorRef.tell (subscriber, actorRef);
				return null;
			});
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
		private LoggingAdapter log = Logging.getLogger (context ().system (), this);
		
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
							self ().tell ("stop", self ());
							break;
						}
						
						subscriber.onNext (ByteStrings.fromArray (data, 0, nread).compact ());
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
				log.debug ("Timeout on actor: " + self ().toString ());
				if (subscriber != null && !completed) {
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
	
	public final static class ContainerActor extends UntypedActor {
		private long id = 1;
		
		public static Props props () {
			return Props.create (ContainerActor.class);
		}
		
		@Override
		public void onReceive (final Object message) throws Exception {
			if (message instanceof Props) {
				sender ().tell (context ().actorOf ((Props) message, "actor-" + (id ++)), self ());
			} else {
				unhandled (message);
			}
		}
	}
}

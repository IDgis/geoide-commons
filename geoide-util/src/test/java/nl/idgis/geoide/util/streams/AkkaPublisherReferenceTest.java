package nl.idgis.geoide.util.streams;

import static nl.idgis.geoide.util.streams.InputStreamPublisherTest.testInputStream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.util.ByteString;

public class AkkaPublisherReferenceTest extends PublisherVerification<ByteString> {

	private final static int BLOCK_SIZE = 10;
	
	private ActorSystem actorSystem = null;
	
	private AkkaStreamProcessor streamProcessor;
	
	public AkkaPublisherReferenceTest () {
		super (new TestEnvironment (300), 1000);
	}
	
	@BeforeMethod
	public void createStreamProcessor () {
		actorSystem = ActorSystem.create ();
		streamProcessor = new AkkaStreamProcessor (actorSystem);
	}
	
	@AfterMethod
	public void destroyStreamProcessor () {
		streamProcessor = null;
		JavaTestKit.shutdownActorSystem (actorSystem);
		actorSystem = null;
	}

	private Publisher<ByteString> wrapPublisher (final Publisher<ByteString> publisher) {
		final PublisherReference<ByteString> reference = streamProcessor.createPublisherReference (publisher, 500);
		final Publisher<ByteString> dereferencedPublisher = streamProcessor.resolvePublisherReference (reference, 500);

		return dereferencedPublisher;
	}
	
	@Override
	public Publisher<ByteString> createPublisher (long elements) {
		try {
			return wrapPublisher (streamProcessor.publishInputStream (testInputStream (BLOCK_SIZE, (int) elements), BLOCK_SIZE, 10000));
		} catch (Throwable e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException (e);
			}
		}
	}
	
	@Override
	public Publisher<ByteString> createFailedPublisher () {
		return wrapPublisher (new Publisher<ByteString> () {
			@Override
			public void subscribe (final Subscriber<? super ByteString> s) {
				final Subscription subscription = new Subscription () {
					@Override
					public void request (final long n) {
					}
					
					@Override
					public void cancel() {
					}
				};
				
				s.onSubscribe (subscription);
				s.onError (new RuntimeException ("Can't subscribe to subscriber"));
			}
		});
	}
	
	@Override
	public long maxElementsFromPublisher () {
		return 128;
	}
	
	@Test
	public void test () {
		
	}
}

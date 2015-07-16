package nl.idgis.geoide.util.streams;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

public class ByteStringPublisherTest  extends PublisherVerification<ByteString> {

	private final static int BLOCK_SIZE = 2;
	
	private ActorSystem actorSystem = null;
	
	private AkkaStreamProcessor streamProcessor;
	
	public ByteStringPublisherTest () {
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

	@Override
	public long maxElementsFromPublisher () {
		return 128;
	}
	
	@Override
	public Publisher<ByteString> createPublisher (final long elements) {
		final byte[] data = new byte[(int)elements * BLOCK_SIZE];
		
		for (int i = 0; i < (int)elements; ++ i) {
			for (int j = 0; j < BLOCK_SIZE; ++ j) {
				data[BLOCK_SIZE * i + j] = (byte)i;
			}
		}

		return streamProcessor.publishByteString (ByteStrings.fromArray (data), BLOCK_SIZE);
	}
	
	@Override
	public Publisher<ByteString> createFailedPublisher () {
		return new Publisher<ByteString> () {
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
		};
	}
	
	/**
	 * Verifies that the content of the stream matches the original.
	 */
	@Test
	public void testStreamContent () throws Throwable {
		final byte[] data = streamProcessor.reduce (createPublisher (10), ByteStrings.empty ().compact (), new BiFunction<ByteString, ByteString, ByteString> () {
			@Override
			public ByteString apply (final ByteString a, final ByteString b) {
				return a.concat (b).compact ();
			}
		}).get (1000, TimeUnit.MILLISECONDS).toArray ();

		for (int i = 0; i < 10 * BLOCK_SIZE; ++ i) {
			Assert.assertEquals ((byte)(i / BLOCK_SIZE), data[i]);
		}
	}
}

package nl.idgis.geoide.util.streams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import play.libs.F.Function2;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

public class InputStreamPublisherTest extends PublisherVerification<ByteString> {

	private final static int BLOCK_SIZE = 10;
	
	private ActorSystem actorSystem = null;
	
	private AkkaStreamProcessor streamProcessor;
	
	public InputStreamPublisherTest () {
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
	public Publisher<ByteString> createPublisher (long elements) {
		try {
			return streamProcessor.publishInputStream (testInputStream (BLOCK_SIZE, (int) elements), BLOCK_SIZE, 10000);
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
	
	@Override
	public long maxElementsFromPublisher () {
		return 128;
	}
	
	/**
	 * Verifies that the content of the stream matches the original.
	 */
	@Test
	public void testStreamContent () {
		final byte[] data = streamProcessor.reduce (createPublisher (10), ByteStrings.empty ().compact (), new Function2<ByteString, ByteString, ByteString> () {
			@Override
			public ByteString apply (final ByteString a, final ByteString b) throws Throwable {
				return a.concat (b).compact ();
			}
		}).get (1000).toArray ();
		
		Assert.assertEquals (testBytes (BLOCK_SIZE, 10), data);
	}
	
	@Test
	public void testStreamAdapter () throws Throwable {
		final Publisher<ByteString> publisher = createPublisher (100);
		final InputStream is = streamProcessor.asInputStream (publisher, 1000);
		final byte[] expectedBytes = testBytes (BLOCK_SIZE, 100);
		final byte[] resultingBytes = new byte[expectedBytes.length];

		for (int i = 0; i < resultingBytes.length; ++ i) {
			final int value = is.read ();
			Assert.assertTrue (value >= 0, String.format ("Value %d should be >= 0 (at index %d: %d)", value, i, expectedBytes[i]));
			resultingBytes[i] = (byte) value;
		}
		
		Assert.assertTrue (is.read () < 0);
		
		Assert.assertEquals (expectedBytes, resultingBytes);
	}
	
	@Test (expectedExceptions = IOException.class)
	public void testStreamAdapterTimeout () throws Throwable {
		final Publisher<ByteString> publisher = createPublisher (100);
		final InputStream is = streamProcessor.asInputStream (publisher, 1000);
		final byte[] expectedBytes = testBytes (BLOCK_SIZE, 100);
		final byte[] resultingBytes = new byte[expectedBytes.length];

		for (int i = 0; i < resultingBytes.length; ++ i) {
			final int value = is.read ();
			Assert.assertTrue (value >= 0);
			resultingBytes[i] = (byte) value;
			Thread.sleep (2000);
		}
		
		Assert.assertTrue (is.read () < 0);
		
		Assert.assertEquals (expectedBytes, resultingBytes);
	}
	
	public static byte[] testBytes (final int blockSize, final int numBlocks) {
		final byte[] data = new byte[blockSize * numBlocks];
		
		for (int i = 0; i < numBlocks; ++ i) {
			for (int j = 0; j < blockSize; ++ j) {
				data[blockSize * i + j] = (byte) (~(i & 0xff));
			}
		}

		return data;
	}
	
	public static InputStream testInputStream (final int blockSize, final int numBlocks) throws Throwable {
		return new ByteArrayInputStream (testBytes (blockSize, numBlocks));
	}
}

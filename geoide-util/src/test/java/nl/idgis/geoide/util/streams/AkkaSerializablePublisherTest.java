package nl.idgis.geoide.util.streams;

import static nl.idgis.geoide.util.streams.InputStreamPublisherTest.testInputStream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.util.CompactByteString;

public class AkkaSerializablePublisherTest extends PublisherVerification<CompactByteString> {

	private final static int BLOCK_SIZE = 10;
	
	private ActorSystem actorSystem = null;
	
	private AkkaStreamProcessor streamProcessor;
	
	public AkkaSerializablePublisherTest () {
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
	
	private SerializablePublisher<CompactByteString> wrapPublisher (final Publisher<CompactByteString> original) {
		return streamProcessor.asSerializable (original);
	}
	
	@Override
	public Publisher<CompactByteString> createPublisher (long elements) {
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
	public Publisher<CompactByteString> createErrorStatePublisher () {
		return wrapPublisher (new Publisher<CompactByteString> () {
			@Override
			public void subscribe (final Subscriber<? super CompactByteString> s) {
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

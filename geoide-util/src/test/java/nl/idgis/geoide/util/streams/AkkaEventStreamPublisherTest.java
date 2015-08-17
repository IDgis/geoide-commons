package nl.idgis.geoide.util.streams;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;

public class AkkaEventStreamPublisherTest extends PublisherVerification<Integer> {

	private final static int BUFFER_SIZE = 1000;
	
	private ActorSystem actorSystem = null;
	
	private AkkaStreamProcessor streamProcessor;
	
	public AkkaEventStreamPublisherTest() {
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
	public Publisher<Integer> createPublisher (final long elements) {
		final AkkaEventStreamPublisher<Integer> publisher = new AkkaEventStreamPublisher<> (actorSystem, BUFFER_SIZE, 500);
		
		for (int i = 0; i < (int)elements; ++ i) {
			publisher.publish (i);
		}
		
		publisher.complete ();
		
		return publisher;
	}

	@Override
	public Publisher<Integer> createFailedPublisher () {
		return null;
	}
	
	@Override
	public long maxElementsFromPublisher () {
		return BUFFER_SIZE;
	}
	
	@Test
	public void test () {
	}
}

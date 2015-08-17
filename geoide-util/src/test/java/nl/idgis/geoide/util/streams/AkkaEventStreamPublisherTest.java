package nl.idgis.geoide.util.streams;

import java.util.concurrent.TimeUnit;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.pattern.AskTimeoutException;
import akka.pattern.Patterns;
import akka.testkit.JavaTestKit;
import scala.concurrent.CanAwait;
import scala.concurrent.duration.Duration;

import static org.testng.Assert.*;

public class AkkaEventStreamPublisherTest extends PublisherVerification<Integer> {

	private final static int BUFFER_SIZE = 1000;
	
	private ActorSystem actorSystem = null;
	
	public AkkaEventStreamPublisherTest() {
		super (new TestEnvironment (300), 1000);
	}
	
	@BeforeMethod
	public void createStreamProcessor () {
		actorSystem = ActorSystem.create ();
	}
	
	@AfterMethod
	public void destroyStreamProcessor () {
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
	public void testActorTermination () throws Throwable {
		final AkkaEventStreamPublisher<Integer> publisher = new AkkaEventStreamPublisher<> (actorSystem, BUFFER_SIZE, 10);
		
		for (int i = 0; i < BUFFER_SIZE; ++ i) {
			publisher.publish (i);
		}
		
		publisher.complete ();

		Thread.sleep (50);
		try {
			Patterns.ask (publisher.getActor (), new Identify ("Test alive"), 1000).result (Duration.create (500, TimeUnit.MILLISECONDS), new CanAwait () { });
		} catch (AskTimeoutException e) {
			return;
		}
		
		fail ("Publisher actor did not terminate 5 seconds after completion");
	}
}

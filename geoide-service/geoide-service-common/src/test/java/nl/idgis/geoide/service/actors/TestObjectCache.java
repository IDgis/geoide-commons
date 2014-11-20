package nl.idgis.geoide.service.actors;

import nl.idgis.geoide.service.messages.CacheObject;
import nl.idgis.geoide.service.messages.CacheResponse;
import nl.idgis.geoide.service.messages.CachedObject;

import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.libs.Akka;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.TestActorRef;
import akka.testkit.TestKit;
import static org.junit.Assert.*;

public class TestObjectCache {
	
	private TestKit testKit;
	private TestActorRef<ObjectCache> cacheRef;
	private ObjectCache cache;
	
	@Before
	public void createCache () {
		testKit = new TestKit (ActorSystem.apply ());
		final Props props = ObjectCache.props ();
		cacheRef = TestActorRef.create (testKit.system (), props, "testCache");
		cache = cacheRef.underlyingActor ();
	}
	
	@Test
	public void testStore () throws Throwable {
		final Object obj = ask (new CacheObject ("Hello, World!", 5000));
		Thread.sleep (10);
		final Object obj2 = ask (new CacheObject ("Hello, World! 2", 5000));
		
		assertTrue (obj instanceof CacheResponse);
		
		final CacheResponse cr = (CacheResponse) obj;
		final CacheResponse cr2 = (CacheResponse) obj2;
		
		assertNotNull (cr.getKey ());
		assertNotNull (cr2.getKey ());
		assertNotNull (cr.getExpiryTime ());
		assertNotNull (cr2.getExpiryTime ());
		
		assertNotEquals (cr.getKey (), cr2.getKey ());
		
		assertTrue (cr.getExpiryTime ().compareTo (LocalDateTime.now ()) > 0);
		assertTrue (cr2.getExpiryTime ().compareTo (LocalDateTime.now ()) > 0);
		assertTrue (cr2.getExpiryTime ().compareTo (cr.getExpiryTime ()) > 0);
	}

	private <T> T ask (final Object message) throws Throwable {
		final Future<Object> future = Patterns.ask (cacheRef, new CacheObject ("Hello, World!", 5000), 1000);
		
		assertTrue (future.isCompleted ());
		
		final Object obj = Await.result (future, Duration.Zero ());
		
		assertNotNull (obj);
		
		@SuppressWarnings("unchecked")
		final T res = (T) obj;
		
		return res;
	}
}

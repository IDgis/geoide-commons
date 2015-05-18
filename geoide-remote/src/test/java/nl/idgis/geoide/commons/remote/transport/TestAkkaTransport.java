package nl.idgis.geoide.commons.remote.transport;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteMethodServer;
import nl.idgis.geoide.commons.remote.RemoteServiceFactory;
import nl.idgis.geoide.commons.remote.ServiceRegistration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import static org.junit.Assert.*;

public class TestAkkaTransport {
	
	private ActorSystem system;
	private RemoteServiceFactory factory;
	private AkkaTransport transport;
	private TestImpl serverObject;
	private RemoteMethodServer server;
	
	@Before
	public void createTransport () {
		system = ActorSystem.create ();
		transport = new AkkaTransport (system, "transport", 5000);
		factory = new RemoteServiceFactory ();
		serverObject = new TestImpl (system);
		server = factory.createRemoteMethodServer (new ServiceRegistration<TestInterface> (TestInterface.class, serverObject, null));
	}
	
	@After
	public void destroyTransport () {
		serverObject = null;
		factory = null;
		transport = null;
		JavaTestKit.shutdownActorSystem (system);
		system = null;
	}

	@Test
	public void testInvokeRemoteMethod () throws Throwable {
		transport.listen (server, "testInterface");
		final RemoteMethodClient client = transport.connect ("akka://default/user/transport", "testInterface");
		final TestInterface localObject = factory.createServiceReference (client, TestInterface.class);
		
		assertEquals (Integer.valueOf (42), localObject.returnValue (42).get ());
		assertEquals (Integer.valueOf (43), localObject.returnValueDelayed (43).get ());
	}

	@Test
	public void testInvokeRemoteMethodException () throws Throwable {
		transport.listen (server, "testInterface");
		final RemoteMethodClient client = transport.connect ("akka://default/user/transport", "testInterface");
		final TestInterface localObject = factory.createServiceReference (client, TestInterface.class);

		final CompletableFuture<Integer> future = localObject.throwException ();
		
		assertNotNull (future);
		
		try {
			future.get ();
		} catch (ExecutionException e) {
			assertNotNull (e.getCause ());
			assertTrue (e.getCause () instanceof RuntimeException);
			assertEquals ("throwException", ((RuntimeException) e.getCause ()).getMessage ());
			return;
		}
		
		fail ("Expected: RuntimeException");
	}
	
	@Test
	public void testInvokeRemoteMethodDelayedException () throws Throwable {
		transport.listen (server, "testInterface");
		final RemoteMethodClient client = transport.connect ("akka://default/user/transport", "testInterface");
		final TestInterface localObject = factory.createServiceReference (client, TestInterface.class);

		final CompletableFuture<Integer> future = localObject.throwExceptionDelayed ();
		
		assertNotNull (future);
		
		try {
			future.get ();
		} catch (ExecutionException e) {
			assertNotNull (e.getCause ());
			assertTrue (e.getCause () instanceof RuntimeException);
			assertEquals ("throwExceptionDelayed", ((RuntimeException) e.getCause ()).getMessage ());
			return;
		}
		
		fail ("Expected: RuntimeException");
	}
	
	public static interface TestInterface {
		CompletableFuture<Integer> returnValue (int value);
		CompletableFuture<Integer> returnValueDelayed (int value);
		CompletableFuture<Integer> throwException ();
		CompletableFuture<Integer> throwExceptionDelayed ();
	}
	
	public static class TestImpl implements TestInterface {
		private final ActorSystem system;
		
		public TestImpl (final ActorSystem system) {
			this.system = system;
		}
		
		@Override
		public CompletableFuture<Integer> returnValue (final int value) {
			return CompletableFuture.completedFuture (value);
		}
		
		@Override
		public CompletableFuture<Integer> returnValueDelayed (final int value) {
			final CompletableFuture<Integer> future = new CompletableFuture<> ();
			
			system
				.scheduler ()
				.scheduleOnce (
					new FiniteDuration (1, TimeUnit.SECONDS), 
					() -> future.complete (value), 
					system.dispatcher ()
				);
			
			return future;
		}
		
		@Override
		public CompletableFuture<Integer> throwException () {
			throw new RuntimeException ("throwException");
		}
		
		@Override
		public CompletableFuture<Integer> throwExceptionDelayed () {
			final CompletableFuture<Integer> future = new CompletableFuture<> ();
			
			system
				.scheduler ()
				.scheduleOnce (
					new FiniteDuration (1, TimeUnit.SECONDS), 
					() -> future.completeExceptionally (new RuntimeException ("throwExceptionDelayed")), 
					system.dispatcher ()
				);
			
			return future;
		}
	}
}

package nl.idgis.geoide.commons.remote.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteMethodServer;
import nl.idgis.geoide.commons.remote.RemoteServiceFactory;
import nl.idgis.geoide.commons.remote.ServiceRegistration;
import nl.idgis.geoide.util.streams.AkkaStreamProcessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.util.ByteString;
import akka.util.CompactByteString;

public class TestAkkaTransport {
	
	private ActorSystem system;
	private RemoteServiceFactory factory;
	private AkkaTransport transport;
	private TestImpl serverObject;
	private RemoteMethodServer server;
	private AkkaStreamProcessor streamProcessor;
	
	@Before
	public void createTransport () {
		system = ActorSystem.create ();
		transport = new AkkaTransport (system, "transport", 5000);
		factory = new RemoteServiceFactory ();
		streamProcessor = new AkkaStreamProcessor (system);
		serverObject = new TestImpl (system, streamProcessor);
		server = factory.createRemoteMethodServer (new ServiceRegistration<TestInterface> (TestInterface.class, serverObject, null));
	}
	
	@After
	public void destroyTransport () {
		server = null;
		serverObject = null;
		streamProcessor.close ();
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
	
	@Test
	public void testInvokeRemotePublisher () throws Throwable {
		transport.listen (server, "testInterface");
		final RemoteMethodClient client = transport.connect ("akka://default/user/transport", "testInterface");
		final TestInterface localObject = factory.createServiceReference (client, TestInterface.class);
		
		final ObjectWithPublisher object = localObject.returnPublisher ().get ();
	}
	
	public static interface TestInterface {
		CompletableFuture<Integer> returnValue (int value);
		CompletableFuture<Integer> returnValueDelayed (int value);
		CompletableFuture<Integer> throwException ();
		CompletableFuture<Integer> throwExceptionDelayed ();
		CompletableFuture<ObjectWithPublisher> returnPublisher ();
	}
	
	public static class TestImpl implements TestInterface {
		private final ActorSystem system;
		private final AkkaStreamProcessor streamProcessor;
		
		public TestImpl (final ActorSystem system, final AkkaStreamProcessor streamProcessor) {
			this.system = system;
			this.streamProcessor = streamProcessor;
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
		
		public CompletableFuture<ObjectWithPublisher> returnPublisher () {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
			try (final PrintWriter writer = new PrintWriter (bos)) {
				writer.println ("Hello, World!");
			}
			
			final Publisher<CompactByteString> publisher = streamProcessor.publishInputStream (new ByteArrayInputStream (bos.toByteArray ()), 2, 1000);
			
			return CompletableFuture.completedFuture (new ObjectWithPublisher (publisher));
		}
	}
	
	public static class ObjectWithPublisher {
		private final Publisher<CompactByteString> publisher;
		
		public ObjectWithPublisher (final Publisher<CompactByteString> publisher) {
			this.publisher = publisher;
		}
	}
}

package nl.idgis.geoide.commons.remote.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.remote.RemoteMethodClient;
import nl.idgis.geoide.commons.remote.RemoteMethodServer;
import nl.idgis.geoide.commons.remote.RemoteServiceFactory;
import nl.idgis.geoide.commons.remote.ServiceRegistration;
import nl.idgis.geoide.util.streams.AkkaStreamProcessor;
import scala.concurrent.duration.FiniteDuration;

public class TestAkkaTransportRemote {

	private ActorSystem[] actorSystems;
	private AkkaTransport[] transports;
	private RemoteServiceFactory[] factories;
	private AkkaStreamProcessor[] streamProcessors;
	private TestImpl serverObject;
	private RemoteMethodServer server;
	private RemoteMethodClient client;
	private TestInterface clientObject;
	
	@Before
	public void createTransports () {
		// Create two actorsystems, connected using TCP:
		actorSystems = new ActorSystem[] {
			ActorSystem.create ("actor-system-1", config (2552)),
			ActorSystem.create ("actor-system-2", config (2553))
		};
		
		transports = new AkkaTransport[] {
			new AkkaTransport (actorSystems[0], "transport", 5000),
			new AkkaTransport (actorSystems[1], "transport", 5000)
		};
		
		factories = new RemoteServiceFactory[] {
			new RemoteServiceFactory (),
			new RemoteServiceFactory ()
		};

		streamProcessors = new AkkaStreamProcessor[] {
			new AkkaStreamProcessor (actorSystems[0]),
			new AkkaStreamProcessor (actorSystems[1])
		};
		
		// Create server:
		serverObject = new TestImpl (actorSystems[0], streamProcessors[0]);
		server = factories[0].createRemoteMethodServer (new ServiceRegistration<TestInterface> (TestInterface.class, serverObject, null));
		transports[0].listen (server, "testInterface");
		
		// Create client:
		client = transports[1].connect ("akka.tcp://actor-system-1@127.0.0.1:2552/user/transport", "testInterface");
		clientObject = factories[1].createServiceReference (client, TestInterface.class);
	}
	
	@After
	public void destroyTransports () {
		Arrays
			.stream (actorSystems)
			.forEach (JavaTestKit::shutdownActorSystem);
	}
	
	@Test
	public void testReturnValue () throws Throwable {
		assertEquals (Integer.valueOf (42), clientObject.returnValue (42).get ());
		assertEquals (Integer.valueOf (43), clientObject.returnValueDelayed (43).get ());
	}
	
	@Test
	public void testInvokeRemoteMethodException () throws Throwable {
		final CompletableFuture<Integer> future = clientObject.throwException ();
		
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
		final CompletableFuture<Integer> future = clientObject.throwExceptionDelayed ();
		
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
	public void testInvokeRemoteDocument () throws Throwable {
		final Document document = clientObject.returnDocument ().get ();
		final InputStream is = streamProcessors[1].asInputStream (document.getBody (), 5000);
		final byte[] buf = new byte[128];
		ByteString byteString = ByteString.empty ();
		int n;
		
		while ((n = is.read (buf)) != -1) {
			if (n == 0) {
				continue;
			}
			
			byteString = byteString.concat (ByteStrings.fromArray (buf, 0, n));
		}
		
		final String result = new String (byteString.toArray ());
		
		assertEquals ("Hello, World!", result);
	}
	
	private static Config config (final int portNumber) {
		return ConfigFactory.parseString ("akka {\n"
			+ "io.tcp.windows-connection-abort-workaround-enabled = false\n"
			+ "actor {\n"
				+ "provider = \"akka.remote.RemoteActorRefProvider\"\n"
				+ "serializers {\n"
					+ "streams = \"nl.idgis.geoide.util.akka.serializers.StreamSerializer\"\n"
					+ "document = \"nl.idgis.geoide.commons.domain.serializers.DocumentSerializer\"\n"
				+ "}\n"
			
				+ "serialization-bindings {\n"
					+ "\"nl.idgis.geoide.util.streams.AkkaSerializablePublisher\" = streams\n"
					+ "\"nl.idgis.geoide.util.streams.AkkaSerializableSubscriber\" = streams\n"
					+ "\"nl.idgis.geoide.commons.domain.document.Document\" = document\n"
				+ "}\n"
			+ "}\n"
			+ "remote {\n"
				+ "enabled-transports = [\"akka.remote.netty.tcp\"]\n"
				+ "netty.tcp {\n"
					+ "hostname = \"127.0.0.1\"\n"
					+ "port = " + portNumber + "\n"
				+ "}\n"
			+ "}\n"
			+ "loggers = [\"akka.event.slf4j.Slf4jLogger\"]\n"		
			+ "loglevel = \"DEBUG\"\n" +
			"}");
	}

	public static interface TestInterface {
		CompletableFuture<Integer> returnValue (int value);
		CompletableFuture<Integer> returnValueDelayed (int value);
		CompletableFuture<Integer> throwException ();
		CompletableFuture<Integer> throwExceptionDelayed ();
		CompletableFuture<Document> returnDocument ();
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
		
		public CompletableFuture<Document> returnDocument () {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
			try (final PrintWriter writer = new PrintWriter (bos)) {
				writer.print ("Hello, World!");
			}
			
			final Publisher<ByteString> publisher = streamProcessor.publishInputStream (new ByteArrayInputStream (bos.toByteArray ()), 2, 1000);
			
			return CompletableFuture.completedFuture (new TestDocument (publisher));
		}
	}
	
	public static class TestDocument implements Document {
		private final Publisher<ByteString> publisher;
		
		public TestDocument (final Publisher<ByteString> publisher) {
			this.publisher = publisher;
		}
		
		@Override
		public URI getUri () throws URISyntaxException {
			return new URI ("http://www.idgis.nl");
		}

		@Override
		public MimeContentType getContentType () {
			return new MimeContentType ("text/html");
		}

		@Override
		public Publisher<ByteString> getBody () {
			return publisher;
		}
	}
}

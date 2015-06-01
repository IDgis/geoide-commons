package nl.idgis.geoide.commons.http.client.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.http.client.HttpResponse;
import nl.idgis.geoide.util.streams.AkkaStreamProcessor;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.reactivestreams.Publisher;

import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Abstract base class for testing HTTP clients. Deals with the boilerplate of setting
 * up a mock webserver (WireMock), a scheduler and a stream processor. Also provides
 * several standard tests each HTTP client should pass.
 */
public abstract class AbstractTestHttpClient {

	private final static int BODY_SIZE = 1000;
	
	private ActorSystem actorSystem;
	private StreamProcessor streamProcessor;
	private HttpClient client;

	/**
	 * This rule provides a mocked web server: WireMock. On port 8089.
	 */
	@Rule
	public WireMockRule wireMockRule = new WireMockRule (8089);

	/**
	 * Creates the scheduler, stream processor and the HTTP client.
	 */
	@Before
	public void createClient () {
		actorSystem = ActorSystem.create ();
		streamProcessor = new AkkaStreamProcessor (actorSystem);
		client = createHttpClient (actorSystem, streamProcessor);
		new DefaultHttpClient (streamProcessor, DefaultHttpClient.createWSClient (), 10, 1000);
	}

	/**
	 * Destroys the client and the scheduler.
	 */
	@After
	public void destroyClient () {
		client = null;
		streamProcessor = null;
		JavaTestKit.shutdownActorSystem (actorSystem);
	}

	/**
	 * Implementors should override this method to instantiate a specific instance of HttpClient that is subject to testing
	 * by this class.
	 * 
	 * @param actorSystem The Akka actor system created by this class, used for scheduling.
	 * @param streamProcessor The stream processor created by this class, used for processing requests and responses.
	 * @return Should return a non-null instance of HttpClient that is the subject of testing.
	 */
	protected abstract HttpClient createHttpClient (ActorRefFactory actorSystem, StreamProcessor streamProcessor);
	
	/**
	 * Performs a simple GET request using the client, without parameters. Tests whether the response body has the expected content.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testGet () throws Throwable {
		final byte[] bytes = generateBytes (BODY_SIZE);
		
		stubFor (get (urlEqualTo ("/resource"))
				.willReturn (aResponse ()
						.withStatus (200)
						.withHeader ("Content-Type", "application/binary")
						.withBody (bytes)));
		
		running (fakeApplication (), new Runnable () {
			@Override
			public void run () {
				final HttpResponse response;
				try {
					response = client.url ("http://localhost:8089/resource").get ().get (1000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					throw new RuntimeException (e);
				}
				
				assertEquals (200, response.getStatus ());
				assertNotNull (response.getStatusText ());
				assertEquals ("application/binary", response.getHeader ("Content-Type"));
				try {
					assertArrayEquals (bytes, readBody (response.getBody ()));
				} catch (Throwable e) {
					throw new RuntimeException (e);
				}
			}
		});
	}

	private byte[] readBody (final Publisher<ByteString> body) throws Throwable {
		if (body == null) {
			return new byte[0];
		}
		
		return streamProcessor.reduce (body, ByteStrings.empty (), (ByteString a, ByteString b) -> a.concat (b)).get (1000, TimeUnit.MILLISECONDS).toArray ();
	}
	
	private byte[] generateBytes (final int length) {
		final byte[] bytes = new byte[length];
		
		for (int i = 0; i < bytes.length; ++ i) {
			bytes[i] = (byte)((2 * i + 1) & 0xff);
		}
		
		return bytes;
	}
}

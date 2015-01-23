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
import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.http.client.HttpResponse;
import nl.idgis.geoide.util.streams.AkkaStreamProcessor;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.reactivestreams.Publisher;

import play.libs.F.Function2;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public abstract class AbstractTestHttpClient {

	private final static int BODY_SIZE = 1000;
	
	private ActorSystem actorSystem;
	private StreamProcessor streamProcessor;
	private HttpClient client;
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule (8089);
	
	@Before
	public void createClient () {
		actorSystem = ActorSystem.create ();
		streamProcessor = new AkkaStreamProcessor (actorSystem);
		client = createHttpClient (actorSystem, streamProcessor); new DefaultHttpClient (streamProcessor, 10, 1000);
	}
	
	@After
	public void destroyClient () {
		client = null;
		streamProcessor = null;
		JavaTestKit.shutdownActorSystem (actorSystem);
	}
	
	protected abstract HttpClient createHttpClient (ActorRefFactory actorSystem, StreamProcessor streamProcessor);
	
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
				final HttpResponse response = client.url ("http://localhost:8089/resource").get ().get (1000);
				
				assertEquals (200, response.getStatus ());
				assertNotNull (response.getStatusText ());
				assertEquals ("application/binary", response.getHeader ("Content-Type"));
				assertArrayEquals (bytes, readBody (response.getBody ()));
			}
		});
	}

	private byte[] readBody (final Publisher<ByteString> body) {
		if (body == null) {
			return new byte[0];
		}
		
		return streamProcessor.reduce (body, ByteStrings.empty (), new Function2<ByteString, ByteString, ByteString> () {
			@Override
			public ByteString apply(ByteString a, ByteString b) throws Throwable {
				return a.concat (b);
			}
		}).get (1000).toArray ();
	}
	
	private byte[] generateBytes (final int length) {
		final byte[] bytes = new byte[length];
		
		for (int i = 0; i < bytes.length; ++ i) {
			bytes[i] = (byte)((2 * i + 1) & 0xff);
		}
		
		return bytes;
	}
}

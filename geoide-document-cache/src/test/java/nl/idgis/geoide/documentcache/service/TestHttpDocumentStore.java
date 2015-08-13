package nl.idgis.geoide.documentcache.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.http.client.HttpClient;
import nl.idgis.geoide.commons.http.client.service.DefaultHttpClient;
import nl.idgis.geoide.util.streams.AkkaStreamProcessor;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Tests the HttpDocumentStore by performing a GET request.
 */
public class TestHttpDocumentStore {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule (8089);
	
	private ActorSystem actorSystem;
	private StreamProcessor streamProcessor;
	private HttpClient httpClient;
	
	/**
	 * Creates a HttpClient and streamProcessor.
	 */
	@Before
	public void createHttpClient () {
		actorSystem = ActorSystem.create ();
		streamProcessor = new AkkaStreamProcessor (actorSystem);
		httpClient = new DefaultHttpClient (streamProcessor, DefaultHttpClient.createWSClient (), 10, 500);
	}
	
	/**
	 * Destroys the HttpClient and stream processor.
	 */
	@After
	public void destroyHttpClient () {
		httpClient = null;
		streamProcessor = null;
		JavaTestKit.shutdownActorSystem (actorSystem);
	}

	/**
	 * Performs a fetch operation on the document store and validates the result.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testFetch () throws Throwable {
		stubFor (get (urlEqualTo ("/resource"))
				.willReturn (aResponse ()
						.withStatus (200)
						.withHeader ("Content-Type", "text/plain")
						.withBody ("Hello, World!")));
		
		running (fakeApplication (), new Runnable () {
			@Override
			public void run () {
				final HttpDocumentStore store = new HttpDocumentStore (httpClient, streamProcessor);
				
				final Document document;
				try {
					document = store.fetch (new URI ("http://localhost:8089/resource")).get (1000, TimeUnit.MILLISECONDS);
				} catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
					throw new RuntimeException (e);
				}
				
				assertEquals (new MimeContentType ("text/plain"), document.getContentType ());
				try {
					TestDefaultDocumentCache.assertContent ("Hello, World!", document, streamProcessor);
				} catch (IOException e) {
					throw new RuntimeException (e);
				}
			}
		});
	}
	
	/**
	 * Peforms a fetch operation with an URI that contains characters that must be escaped.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testFetchEscapedUri () throws Throwable {
		stubFor (get (urlEqualTo ("/resource?a=%3CStyledLayerDescriptor"))
				.willReturn (aResponse ()
						.withStatus (200)
						.withHeader ("Content-Type", "text/plain")
						.withBody ("Hello, World!")));
		
		running (fakeApplication (), new Runnable () {
			@Override
			public void run () {
				final HttpDocumentStore store = new HttpDocumentStore (httpClient, streamProcessor);
				
				final Document document;
				try {
					document = store.fetch (new URI ("http://localhost:8089/resource?a=%3CStyledLayerDescriptor")).get (1000, TimeUnit.MILLISECONDS);
				} catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
					throw new RuntimeException (e);
				}
				
				assertEquals (new MimeContentType ("text/plain"), document.getContentType ());
				try {
					TestDefaultDocumentCache.assertContent ("Hello, World!", document, streamProcessor);
				} catch (IOException e) {
					throw new RuntimeException (e);
				}
			}
		});
	}
	
	/**
	 * Tests wheter a document can be retrieved from the document cache by following a redirect (http 3xx status).
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testFetchWithRedirect () throws Throwable {
		stubFor (get (urlEqualTo ("/resource"))
				.willReturn (aResponse ()
						.withStatus (302)
						.withHeader ("Location", "/resource2")));
		stubFor (get (urlEqualTo ("/resource2"))
				.willReturn (aResponse ()
						.withStatus (200)
						.withHeader ("Content-Type", "text/plain")
						.withBody ("Hello, World!")));
				
		running (fakeApplication (), new Runnable () {
			@Override
			public void run () {
				final HttpDocumentStore store = new HttpDocumentStore (httpClient, streamProcessor);
				
				final Document document;
				try {
					document = store.fetch (new URI ("http://localhost:8089/resource")).get (1000, TimeUnit.MILLISECONDS);
				} catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
					throw new RuntimeException (e);
				}
				
				assertEquals (new MimeContentType ("text/plain"), document.getContentType ());
				try {
					TestDefaultDocumentCache.assertContent ("Hello, World!", document, streamProcessor);
				} catch (IOException e) {
					throw new RuntimeException (e);
				}
			}
		});
	}
}

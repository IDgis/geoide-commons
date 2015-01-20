package nl.idgis.geoide.documentcache.service;

import static org.junit.Assert.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static play.test.Helpers.*;

import java.net.URI;

import nl.idgis.geoide.documentcache.CachedDocument;
import nl.idgis.ogc.util.MimeContentType;

import org.junit.Rule;
import org.junit.Test;


import com.github.tomakehurst.wiremock.junit.WireMockRule;


public class TestHttpDocumentStore {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule (8089);
	
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
				try {
					final HttpDocumentStore store = new HttpDocumentStore ();
					
					final CachedDocument document = store.fetch (new URI ("http://localhost:8089/resource")).get (1000);
					
					assertEquals (new MimeContentType ("text/plain"), document.getContentType ());
					TestDefaultDocumentCache.assertContent ("Hello, World!", document);
				} catch (Throwable e) {
					throw new RuntimeException (e);
				}
			}
		});
	}
	
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
				try {
					final HttpDocumentStore store = new HttpDocumentStore ();
					
					final CachedDocument document = store.fetch (new URI ("http://localhost:8089/resource?a=%3CStyledLayerDescriptor")).get (1000);
					
					assertEquals (new MimeContentType ("text/plain"), document.getContentType ());
					TestDefaultDocumentCache.assertContent ("Hello, World!", document);
				} catch (Throwable e) {
					throw new RuntimeException (e);
				}
			}
		});
	}
}

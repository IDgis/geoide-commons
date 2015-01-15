package nl.idgis.geoide.documentcache.service;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;

import nl.idgis.geoide.documentcache.ByteStringCachedDocument;
import nl.idgis.geoide.documentcache.CachedDocument;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.documentcache.DocumentStore;
import nl.idgis.geoide.documentcache.DocumentCacheException.DocumentNotFoundException;
import nl.idgis.ogc.util.MimeContentType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.libs.F.Promise;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.util.ByteString.ByteStrings;

public class TestDefaultDocumentCache {

	private static ActorSystem actorSystem;
	private static int count = 0;

	private TestDocumentStore store;
	private DefaultDocumentCache cache;
	private DefaultDocumentCache cacheReadThrough;
	
	@BeforeClass
	public static void setup () {
		actorSystem = ActorSystem.create ();
	}
	
	@AfterClass
	public static void teardown () {
		JavaTestKit.shutdownActorSystem (actorSystem);
		actorSystem = null;
	}
	
	@Before
	public void createCache () throws Throwable {
		store = new TestDocumentStore ();
		cache = DefaultDocumentCache.createInMemoryCache (actorSystem, "test-cache-" + (++ count), 1, 0.5, null);
		cacheReadThrough = DefaultDocumentCache.createInMemoryCache (actorSystem, "test-cache-" + (++ count), 1, 0.5, store);
	}
	
	@After
	public void destroyCache () throws Throwable {
		cache.close ();
		store = null;
	}
	
	@Test
	public void testGetTtl () {
		assertEquals (Long.valueOf (1), cache.getTtl ().get (1000));
	}
	
	/**
	 * Fetching a document that can't be retrieved from a readthrough store should fail with
	 * a DocumentNotFoundException.
	 */
	@Test (expected = DocumentNotFoundException.class)
	public void testFetchNotFound () throws Throwable {
		cache.fetch (new URI ("http://idgis.nl/favicon.ico")).get (1000);
	}
	
	@Test
	public void testFetchReadThrough () throws Throwable {
		final CachedDocument document = cacheReadThrough.fetch (new URI ("http://idgis.nl")).get (1000);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document);
		assertEquals (1, store.count);
	}
	
	@Test (expected = DocumentNotFoundException.class)
	public void testFetchReadThroughNotFound () throws Throwable {
		cacheReadThrough.fetch (new URI ("http://idgis.nl/a")).get (1000);
		assertEquals (0, store.count);
	}

	/**
	 * Storing a document without data that can't be retrieved from a readthrough store should fail with
	 * a DocumentNotFoundException.
	 */
	@Test (expected = DocumentNotFoundException.class)
	public void testStoreDocumentNotFound () throws Throwable {
		cache.store (new URI ("http://idgis.nl/favicon.icon")).get (1000);
	}

	@Test
	public void testStoreDocumentReadThrough () throws Throwable {
		cacheReadThrough.store (new URI ("http://idgis.nl")).get (1000);
		assertEquals (1, store.count);
	}

	@Test (expected = DocumentNotFoundException.class)
	public void testStoreDocumentReadThroughNotFound () throws Throwable {
		cacheReadThrough.store (new URI ("http://idgis.nl/a")).get (1000);
		assertEquals (0, store.count);
	}
	
	@Test
	public void testStoreInputStream () throws Throwable {
		final CachedDocument document = cache.store (new URI ("http://idgis.nl"), new MimeContentType ("text/plain"), testStream ("Hello, World!")).get (1000);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document);
	}
	
	@Test
	public void testStoreByteArray () throws Throwable {
		final CachedDocument document = cache.store (new URI ("http://idgis.nl"), new MimeContentType ("text/plain"), testByteArray ("Hello, World!")).get (1000);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document);
	}
	
	@Test
	public void testStoreAndFetch () throws Throwable {
		cache.store (new URI ("http://idgis.nl"), new MimeContentType ("text/plain"), testStream ("Hello, World!")).get (1000);
		
		final CachedDocument document = cache.fetch (new URI ("http://idgis.nl")).get (1000);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document);
	}
	
	@Test
	public void testStoreAndFetchReadThrough () throws Throwable {
		cacheReadThrough.store (new URI ("http://idgis.nl")).get (1000);
		
		assertEquals (1, store.count);
		
		final CachedDocument document = cacheReadThrough.fetch (new URI ("http://idgis.nl")).get (1000);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document);
	}
	
	@Test (expected = DocumentNotFoundException.class)
	public void testStoreAndFetchEvicted () throws Throwable {
		cache.store (new URI ("http://idgis.nl"), new MimeContentType ("text/plain"), testStream ("Hello, World!")).get (1000);
		
		Thread.sleep (2000);
		
		final CachedDocument document =  cache.fetch (new URI ("http://idgis.nl")).get (1000);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document);
	}
	
	@Test
	public void testStoreAndFetchReadThroughEvicted () throws Throwable {
		cacheReadThrough.store (new URI ("http://idgis.nl")).get (1000);
		
		assertEquals (1, store.count);
		
		Thread.sleep (2000);
		
		final CachedDocument document =  cacheReadThrough.fetch (new URI ("http://idgis.nl")).get (1000);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document);
		assertEquals (2, store.count);
	}
	
	public static void assertContent (final String expected, final CachedDocument document) throws Throwable {
		final InputStream inputStream = document.asInputStream ();
		final byte[] data = new byte[4096];
		final int nRead = inputStream.read (data, 0, data.length);
		inputStream.close ();
		
		assertTrue (nRead > 0);
		
		final String content = new String (Arrays.copyOf (data, nRead));
		
		assertEquals (expected, content);
	}
	
	private static byte[] testByteArray (final String content) throws Throwable {
		final ByteArrayOutputStream os = new ByteArrayOutputStream ();
		final PrintWriter writer = new PrintWriter (os);
		
		writer.print (content);
		writer.close ();
		os.close ();

		return os.toByteArray ();
	}
	
	private static InputStream testStream (final String content) throws Throwable {
		
		return new ByteArrayInputStream (testByteArray (content));
	}
	
	private static class TestDocumentStore implements DocumentStore {
		public int count = 0;
		
		@Override
		public Promise<CachedDocument> fetch (final URI uri) {
			try {
				if (uri.equals (new URI ("http://idgis.nl"))) {
					++ count;
					return Promise.pure ((CachedDocument) new ByteStringCachedDocument (uri, new MimeContentType ("text/plain"), ByteStrings.fromArray (testByteArray ("Hello, World!"))));
				}
				
				return Promise.throwing (new DocumentCacheException.DocumentNotFoundException (uri));
			} catch (Throwable e) {
				return Promise.throwing (e);
			}
		}
	}
}

package nl.idgis.geoide.documentcache.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.documentcache.DocumentCacheException.DocumentNotFoundException;
import nl.idgis.geoide.documentcache.DocumentStore;
import nl.idgis.geoide.util.Futures;
import nl.idgis.geoide.util.streams.AkkaStreamProcessor;
import nl.idgis.geoide.util.streams.StreamProcessor;
import nl.idgis.ogc.util.MimeContentType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;

/**
 * Tests the default document cache.
 */
public class TestDefaultDocumentCache {

	private ActorSystem actorSystem;
	private int count = 0;

	private TestDocumentStore store;
	private DefaultDocumentCache cache;
	private DefaultDocumentCache cacheReadThrough;
	private StreamProcessor streamProcessor;

	/**
	 * Creates an in-memory cache instance for use during testing.
	 * 
	 * @throws Throwable
	 */
	@Before
	public void createCache () throws Throwable {
		actorSystem = ActorSystem.create ();
		streamProcessor = new AkkaStreamProcessor (actorSystem);
		store = new TestDocumentStore ();
		cache = DefaultDocumentCache.createInMemoryCache (actorSystem, streamProcessor, "test-cache-" + (++ count), 1, 0.5, null);
		cacheReadThrough = DefaultDocumentCache.createInMemoryCache (actorSystem, streamProcessor, "test-cache-" + (++ count), 1, 0.5, store);
	}
	
	/**
	 * Destroys the cache after testing and cleans up resources.
	 * 
	 * @throws Throwable
	 */
	@After
	public void destroyCache () throws Throwable {
		cache.close ();
		store = null;
		streamProcessor = null;
		JavaTestKit.shutdownActorSystem (actorSystem);
		actorSystem = null;
	}

	/**
	 * The cache should return its TTL value.
	 */
	@Test
	public void testGetTtl () throws Throwable {
		assertEquals (Long.valueOf (1), cache.getTtl ().get (1000, TimeUnit.MILLISECONDS));
	}
	
	/**
	 * Fetching a document that can't be retrieved from a readthrough store should fail with
	 * a DocumentNotFoundException.
	 */
	@Test (expected = DocumentNotFoundException.class)
	public void testFetchNotFound () throws Throwable {
		cache.fetch (new URI ("http://idgis.nl/favicon.ico")).get (1000, TimeUnit.MILLISECONDS);
	}

	/**
	 * Fetching a document that can't be retrieved should access the readthrough store.
	 * @throws Throwable
	 */
	@Test
	public void testFetchReadThrough () throws Throwable {
		final Document document = cacheReadThrough.fetch (new URI ("http://idgis.nl")).get (1000, TimeUnit.MILLISECONDS);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document, streamProcessor);
		assertEquals (1, store.count);
	}

	/**
	 * Fetching a document that can't be retrieved and that doesn't exist in the readthrough store
	 * should raise a DocumentNotFoundException.
	 * 
	 * @throws Throwable
	 */
	@Test (expected = DocumentNotFoundException.class)
	public void testFetchReadThroughNotFound () throws Throwable {
		cacheReadThrough.fetch (new URI ("http://idgis.nl/a")).get (1000, TimeUnit.MILLISECONDS);
		assertEquals (0, store.count);
	}

	/**
	 * Storing a document without data that can't be retrieved from a readthrough store should fail with
	 * a DocumentNotFoundException.
	 */
	@Test (expected = DocumentNotFoundException.class)
	public void testStoreDocumentNotFound () throws Throwable {
		cache.store (new URI ("http://idgis.nl/favicon.icon")).get (1000, TimeUnit.MILLISECONDS);
	}

	/**
	 * Storing a document by providing only an URI should fetch the document from the readthrough store
	 * and store it in the cache.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testStoreDocumentReadThrough () throws Throwable {
		cacheReadThrough.store (new URI ("http://idgis.nl")).get (1000, TimeUnit.MILLISECONDS);
		assertEquals (1, store.count);
	}

	/**
	 * Storing a document by providing only an URI should raise a DocumentNotFoundException if
	 * the document doesn't exist in the readthrough store.
	 * 
	 * @throws Throwable
	 */
	@Test (expected = DocumentNotFoundException.class)
	public void testStoreDocumentReadThroughNotFound () throws Throwable {
		cacheReadThrough.store (new URI ("http://idgis.nl/a")).get (1000, TimeUnit.MILLISECONDS);
		assertEquals (0, store.count);
	}
	
	/**
	 * Test whether a document can be stored by providing an input stream.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testStoreInputStream () throws Throwable {
		final Document document = cache.store (new URI ("http://idgis.nl"), new MimeContentType ("text/plain"), testStream ("Hello, World!")).get (1000, TimeUnit.MILLISECONDS);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document, streamProcessor);
	}
	
	/**
	 * Test whether a document can be stored by providing a byte array.
	 * @throws Throwable
	 */
	@Test
	public void testStoreByteArray () throws Throwable {
		final Document document = cache.store (new URI ("http://idgis.nl"), new MimeContentType ("text/plain"), testByteArray ("Hello, World!")).get (1000, TimeUnit.MILLISECONDS);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document, streamProcessor);
	}

	/**
	 * Test whether a document that has previously been stored in the cache can later be fetched
	 * by providing the same URI as input.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testStoreAndFetch () throws Throwable {
		cache.store (new URI ("http://idgis.nl"), new MimeContentType ("text/plain"), testStream ("Hello, World!")).get (1000, TimeUnit.MILLISECONDS);
		
		final Document document = cache.fetch (new URI ("http://idgis.nl")).get (1000, TimeUnit.MILLISECONDS);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document, streamProcessor);
	}
	
	/**
	 * Test whether a document that has previously been fetched and stored from the readthrough store
	 * can later be fetched directly from the cache by providing the same URI as input.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testStoreAndFetchReadThrough () throws Throwable {
		cacheReadThrough.store (new URI ("http://idgis.nl")).get (1000, TimeUnit.MILLISECONDS);
		
		assertEquals (1, store.count);
		
		final Document document = cacheReadThrough.fetch (new URI ("http://idgis.nl")).get (1000, TimeUnit.MILLISECONDS);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document, streamProcessor);
	}

	/**
	 * Test whether a document that can be fetched directly after being stored raises a DocumentNotFoundException
	 * when it is fetched after the TTL expires.
	 * 
	 * @throws Throwable
	 */
	@Test (expected = DocumentNotFoundException.class)
	public void testStoreAndFetchEvicted () throws Throwable {
		cache.store (new URI ("http://idgis.nl"), new MimeContentType ("text/plain"), testStream ("Hello, World!")).get (1000, TimeUnit.MILLISECONDS);
		
		Thread.sleep (2000);
		
		final Document document =  cache.fetch (new URI ("http://idgis.nl")).get (1000, TimeUnit.MILLISECONDS);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document, streamProcessor);
	}
	
	/**
	 * Test whether a document that is fetched and cached from the readthrough store is fetched again
	 * when the TTL expires.
	 *  
	 * @throws Throwable
	 */
	@Test
	public void testStoreAndFetchReadThroughEvicted () throws Throwable {
		cacheReadThrough.store (new URI ("http://idgis.nl")).get (1000, TimeUnit.MILLISECONDS);
		
		assertEquals (1, store.count);
		
		Thread.sleep (2000);
		
		final Document document =  cacheReadThrough.fetch (new URI ("http://idgis.nl")).get (1000, TimeUnit.MILLISECONDS);
		
		assertEquals (new MimeContentType ("text/plain"), document.getContentType());
		assertContent ("Hello, World!", document, streamProcessor);
		assertEquals (2, store.count);
	}

	/**
	 * Asserts that the content of a document has a certain value.
	 * 
	 * @param expected			The expected value of the document body.
	 * @param document			The document to test.
	 * @param streamProcessor	The stream processor to use when consuming the stream.
	 * @throws IOException
	 */
	public static void assertContent (final String expected, final Document document, final StreamProcessor streamProcessor) throws IOException {
		final InputStream inputStream = streamProcessor.asInputStream (document.getBody (), 5000);
		
		final byte[] buffer = new byte[4096];
		ByteString data = ByteStrings.empty ();
		int nRead;
		while ((nRead = inputStream.read (buffer, 0, buffer.length)) >= 0) {
			data = data.concat (ByteStrings.fromArray (buffer, 0, nRead));
		}
		inputStream.close ();
		
		assertTrue (data.size () > 0);
		
		final String content = new String (data.toArray ());
		
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
	
	private class TestDocumentStore implements DocumentStore {
		public int count = 0;
		
		@Override
		public CompletableFuture<Document> fetch (final URI uri) {
			try {
				if (uri.equals (new URI ("http://idgis.nl"))) {
					++ count;
					final Document document = new Document () {
						@Override
						public URI getUri () {
							return uri;
						}
						
						@Override
						public MimeContentType getContentType () {
							return new MimeContentType ("text/plain");
						}
						
						@Override
						public Publisher<ByteString> getBody () {
							try {
								return streamProcessor.<ByteString>publishSinglevalue (ByteStrings.fromArray (testByteArray ("Hello, World!")).compact ());
							} catch (Throwable e) {
								throw new RuntimeException (e);
							}
						}
					};
					
					return CompletableFuture.completedFuture (document);
				}
				
				return Futures.throwing (new DocumentCacheException.DocumentNotFoundException (uri));
			} catch (Throwable e) {
				return Futures.throwing (e);
			}
		}
	}
}

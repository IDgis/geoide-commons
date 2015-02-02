package nl.idgis.geoide.documentcache;

import java.io.InputStream;
import java.net.URI;

import nl.idgis.ogc.util.MimeContentType;

import org.reactivestreams.Publisher;

import play.libs.F.Promise;
import akka.util.ByteString;

/**
 * Implementations of DocumentCache have the ability to store documents for a preconfigured amount of time.
 * This interface extends {@link DocumentStore}: documents that are stored in a cache can be fetched by using
 * that interface. 
 * 
 * A cache stores a document for at least the configured amount of time. After that time expires the document
 * is not guaranteed to be available in the cache, but it is not guaranteed to be unavailable either. Cache
 * implementations are free to implement their own cache eviction strategy on top of the minimal required time.
 */
public interface DocumentCache extends DocumentStore {
	/**
	 * Returns a promise that resolves to the "time to live" for documents stored in the cache in milliseconds.
	 * After storing a document it is not guaranteed to be present in the cache after this time expires.
	 * 
	 * @return A promise that resolves to the cache TTL in milliseconds.
	 */
	Promise<Long> getTtl ();
	
	/**
	 * Stores a document with the given URI in the cache. This method only works if the cache implementation
	 * has the ability to read documents from a read-through store in case of a cache miss. If read-through
	 * capabilities are not present the returned promise always raises a {@link DocumentCacheException}. If
	 * a document with that URI is previously cached, it is overwritten.
	 * 
	 * @param uri The URI of the document to store.
	 * @return A promise that resolves to the stored document, or a {@link DocumentCacheException} if the
	 * 	document couldn't be fetched or storing the document failed.
	 */
	Promise<Document> store (URI uri);
	
	/**
	 * Stores a document in the cache by providing a byte array containing the document body. If a document
	 * with that URI previously exists it gets overwritten.
	 * 
	 * @param uri			The unique URI of the document to store.
	 * @param contentType	The content type of the document.
	 * @param data			The document body.
	 * @return				A promise that resolves to the stored document, or raises a {@link DocumentCacheException} if the
	 * 						document couldn't be stored.
	 */
	Promise<Document> store (URI uri, MimeContentType contentType, byte[] data);
	
	/**
	 * Stores a document in the cache by providing an InputStream containing the document body. If a document
	 * with that URI previously exists it gets overwritten. The entire InputStream is consumed and closed after
	 * use.
	 * 
	 * @param uri			The unique URI of the document to store.
	 * @param contentType	The content type of the document.
	 * @param inputStream	The document body.
	 * @return				A promise that resolves to the stored document, or raises a {@link DocumentCacheException} if the
	 * 						document couldn't be stored.
	 */
	Promise<Document> store (URI uri, MimeContentType contentType, InputStream inputStream);
	
	/**
	 * Stores a document in the cache by providing an publisher that produces ByteStrings containing the document body. If a document
	 * with that URI previously exists it gets overwritten. The entire producer is consumed and closed after
	 * use.
	 * 
	 * @param uri			The unique URI of the document to store.
	 * @param contentType	The content type of the document.
	 * @param body			The document body.
	 * @return				A promise that resolves to the stored document, or raises a {@link DocumentCacheException} if the
	 * 						document couldn't be stored.
	 */
	Promise<Document> store (URI uri, MimeContentType contentType, Publisher<ByteString> body);
}

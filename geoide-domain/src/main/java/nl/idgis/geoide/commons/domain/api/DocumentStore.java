package nl.idgis.geoide.commons.domain.api;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.document.Document;

/**
 * A document store provides access to documents that are identified by a unique URI. The DocumentStore
 * interface is implemented by {@link DocumentCache}, also implementations that provide access to remote
 * stores can exist.
 */
public interface DocumentStore {
	
	/**
	 * Retrieves a document from the store identified by the given URI.
	 * 
	 * @param uri	The URI identification of the document.
	 * @return 		A promise that resolves to the document, or raises a {@link DocumentCacheException} if the document
	 * 				couldn't be found or if an error occured during retrieval.
	 */
	CompletableFuture<Document> fetch (final URI uri);
}

package nl.idgis.geoide.documentcache.service;


import java.net.URI;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.api.DocumentStore;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.document.DocumentCacheException;
import nl.idgis.geoide.util.Futures;


/**
 * An implementation of {@link DocumentStore} that delegates the requests to (one of) the document stores in stores
 * 
 */

public class DelegatingStore implements DocumentStore {

	
	private final DocumentStore[] stores;


	/**
	 * Creates a new delegating document store
	 * 
	 * @param stores An array of stores where to delegate to=
	 */
	public DelegatingStore(DocumentStore... stores) {
		this.stores = stores;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletableFuture<Document> fetch(URI uri) {
		return fetch(uri,0);			
	
	}
	
	private CompletableFuture<Document> fetch(URI uri, int n) {
		if(n >=stores.length ) {
			return Futures.throwing (new DocumentCacheException.DocumentNotFoundException (uri));
		}
		CompletableFuture<Document> doc = stores[n].fetch(uri);
		
		return doc
			.handle ((document, throwable) -> {
				if (throwable != null) {
					return fetch (uri, n + 1);
				} else {
					return CompletableFuture.<Document>completedFuture (document);
				}
			})
			.thenCompose((f) -> f);
	}
	

}
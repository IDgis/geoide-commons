package nl.idgis.geoide.documentcache.service;


import java.net.URI;

import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.documentcache.DocumentStore;
import play.libs.F.Promise;


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
	public Promise<Document> fetch(URI uri) {
		return fetch(uri,0);			
	
	}
	
	private Promise<Document> fetch(URI uri, int n) {
		if(n >=stores.length ) {
			return Promise.throwing(new DocumentCacheException.DocumentNotFoundException (uri));
		}
		Promise<Document> doc = stores[n].fetch(uri);
		return doc
			.recoverWith ((t) -> fetch (uri, n + 1))
			.map ((d) -> d);
		
	}
	

}
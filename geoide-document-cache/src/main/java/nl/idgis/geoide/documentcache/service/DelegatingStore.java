package nl.idgis.geoide.documentcache.service;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;

import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.documentcache.DocumentStore;
import play.libs.F.Promise;

public class DelegatingStore implements DocumentStore {

	
	private final DocumentStore[] stores;


	public DelegatingStore(DocumentStore... stores) {
		this.stores = stores;
	}
	
	@Override
	public Promise<Document> fetch(URI uri) {
		return fetch(uri,0);			
	
	}
	
	private Promise<Document> fetch(URI uri, int n) {
		if(n >=stores.length ) {
			return Promise.throwing(new DocumentCacheException.DocumentNotFoundException (uri));
		}
		Promise<Document> doc = stores[n].fetch(uri);
		doc.recoverWith ((t) -> fetch (uri, n + 1));
		return doc.map ((d) -> (Document)doc);
		
	}
	
	/*public String[] list() {
		return list(0);
	}
	
	private String[] list(int n) {
		if(n >=stores.length ) {
			return null;
		} 
		if(stores[n].getClass() == FileStore.class) {
			return ((FileStore) stores[n]).getFileList();
		} else {
			
			list (n+1);
		}
		return null;
		
	}
	*/
	
	


}
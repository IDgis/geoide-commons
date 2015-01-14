package nl.idgis.geoide.documentcache;

import java.io.IOException;
import java.net.URI;

public class DocumentCacheException extends RuntimeException {
	
	private static final long serialVersionUID = 8953741331313966242L;

	public DocumentCacheException (final String message) {
		super (message);
	}
	
	public DocumentCacheException (final String message, final Throwable cause) {
		super (message);
	}
	
	public DocumentCacheException (final Throwable cause) {
		super (cause);
	}

	public static class IOError extends DocumentCacheException {
		private static final long serialVersionUID = 9168591845186827128L;

		public IOError (final IOException cause) {
			super (cause);
		}
	}
	
	public static class DocumentNotFoundException extends DocumentCacheException {
		private static final long serialVersionUID = -1679252489368027736L;
		
		private final URI uri;
		
		public DocumentNotFoundException (final URI uri) {
			this (uri, null);
		}
		
		public DocumentNotFoundException (final URI uri, final Throwable cause) {
			super (String.format ("Document not found: %s", uri.toString ()), cause);
			
			this.uri = uri;
		}
		
		public URI getUri () {
			return uri;
		}
	}
	
	public static class CacheNotFoundException extends DocumentCacheException {
		private static final long serialVersionUID = 8133627373783585356L;
		
		private final String cacheName;
		
		public CacheNotFoundException (final String cacheName) {
			super (String.format ("Cache not found: %s", cacheName));
			
			this.cacheName = cacheName;
		}
		
		public String getCacheName () {
			return cacheName;
		}
	}
}

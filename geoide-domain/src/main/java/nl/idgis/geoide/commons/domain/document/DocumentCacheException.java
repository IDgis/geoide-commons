package nl.idgis.geoide.commons.domain.document;

import java.io.IOException;
import java.net.URI;

import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.DocumentStore;

/**
 * Base exception class for exceptions raised on error conditions from the {@link DocumentCache} or {@link DocumentStore}.
 */
public class DocumentCacheException extends RuntimeException {
	
	private static final long serialVersionUID = 8953741331313966242L;

	/**
	 * @param message The error message.
	 */
	public DocumentCacheException (final String message) {
		super (message);
	}
	
	/**
	 * @param message The error message
	 * @param cause A root cause for the exception. Used when wrapping other exceptions.
	 */
	public DocumentCacheException (final String message, final Throwable cause) {
		super (message);
	}
	
	/**
	 * @param cause A root cause for the exception. Used when wrapping other exceptions.
	 */
	public DocumentCacheException (final Throwable cause) {
		super (cause);
	}

	/**
	 * Raised by a {@link DocumentStore} or {@link DocumentCache} when an IO error occurs when reading
	 * or writing a document. This exception always provides a root cause that indicates the true cause
	 * of the error. 
	 */
	public static class IOError extends DocumentCacheException {
		private static final long serialVersionUID = 9168591845186827128L;

		/**
		 * @param cause Provides the root cause of the exception. Required non-null attribute.
		 */
		public IOError (final IOException cause) {
			super (cause);
		}
	}
	
	/**
	 * Raised by a {@link DocumentStore} or {@link DocumentCache} when a requested document couldn't be found in the
	 * cache or the store. The exception contains the URI of the requested document that failed.
	 */
	public static class DocumentNotFoundException extends DocumentCacheException {
		private static final long serialVersionUID = -1679252489368027736L;
		
		private final URI uri;

		/**
		 * @param uri The URI of the document that couldn't be found.
		 */
		public DocumentNotFoundException (final URI uri) {
			this (uri, null);
		}
		
		/**
		 * @param uri The URI of the document that couldn't be found.
		 * @param cause The root cause of the exception. Can be null. 
		 */
		public DocumentNotFoundException (final URI uri, final Throwable cause) {
			super (String.format ("Document not found: %s", uri.toString ()), cause);
			
			this.uri = uri;
		}

		/**
		 * Returns the URI of the document that couldn't be found.
		 * 
		 * @return The URI of the document.
		 */
		public URI getUri () {
			return uri;
		}
	}
}

package nl.idgis.geoide.commons.domain.print;

import java.io.Serializable;
import java.net.URI;

import nl.idgis.geoide.commons.domain.MimeContentType;

/**
 * Describes a document for a print service.
 */
public final class DocumentReference implements Serializable {

	private static final long serialVersionUID = -5151232571097664742L;
	
	private final MimeContentType contentType;
	private final URI uri;

	/**
	 * Constructs a new document.
	 * 
	 * @param contentType The content type of the document. Cannot be null.
	 */
	public DocumentReference (final MimeContentType contentType, final URI uri) {
		if (contentType == null) {
			throw new NullPointerException ("contentType cannot be null");
		}
		if (uri == null) {
			throw new NullPointerException ("uri cannot be null");
		}
		
		this.contentType = contentType;
		this.uri = uri;
	}
	
	/**
	 * Returns the content type of the document.
	 * 
	 * @return The content type of the document.
	 */
	public MimeContentType getContentType () {
		return contentType;
	}

	/**
	 * Returns the URI of the document.
	 * 
	 * @return The URI of the document.
	 */
	public URI getUri () {
		return uri;
	}
}

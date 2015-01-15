package nl.idgis.geoide.commons.print.common;

import java.io.Serializable;
import java.net.URI;

import nl.idgis.ogc.util.MimeContentType;

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
	
	public MimeContentType getContentType () {
		return contentType;
	}

	public URI getUri () {
		return uri;
	}
}

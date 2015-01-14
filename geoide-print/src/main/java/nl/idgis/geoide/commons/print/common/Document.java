package nl.idgis.geoide.commons.print.common;

import java.io.Serializable;

import nl.idgis.ogc.util.MimeContentType;

/**
 * Describes a document for a print service.
 */
public final class Document implements Serializable {

	private static final long serialVersionUID = -5151232571097664742L;
	
	private final MimeContentType contentType;

	/**
	 * Constructs a new document.
	 * 
	 * @param contentType The content type of the document. Cannot be null.
	 */
	public Document (final MimeContentType contentType) {
		if (contentType == null) {
			throw new NullPointerException ("contentType cannot be null");
		}
		
		this.contentType = contentType;
	}

	/**
	 * Returns the content type of the document.
	 * 
	 * @return The (non-null) content type of the document.
	 */
	public MimeContentType getContentType () {
		return this.contentType;
	}
}

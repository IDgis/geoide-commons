package nl.idgis.geoide.commons.domain.document;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import akka.util.ByteString;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.DocumentStore;
import nl.idgis.geoide.util.streams.PublisherReference;

/**
 * Describes a document that is stored in a {@link DocumentStore} or a {@link DocumentCache}.
 * Documents are identified within a store or cache with a unique URI and they have a contentType
 * and binary body.
 */
public final class Document implements Serializable {
	private static final long serialVersionUID = 5771471259805625424L;
	
	private final URI uri;
	private final MimeContentType contentType;
	private final PublisherReference<ByteString> body;
	
	public Document (final URI uri, final MimeContentType contentType, final PublisherReference<ByteString> body) {
		this.uri = Objects.requireNonNull (uri, "uri cannot be null");
		this.contentType = Objects.requireNonNull (contentType, "contentType cannot be null");
		this.body = Objects.requireNonNull (body, "body cannot be null");
	}
	
	/**
	 * Returns the URI of the document. Each document in a store or a cache is identified with a unique URI.
	 *  
	 * @return The URI of the document.
	 * @throws URISyntaxException 
	 */
	public URI getUri () {
		return uri;
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
	 * Returns a reactive streams publisher that produces the document body in the form of a stream of ByteStrings.
	 * The body can be further processed using a StreamProcessor.
	 * 
	 * @return The document body as a stream.
	 */
	public PublisherReference<ByteString> getBody () {
		return body;
	}
}

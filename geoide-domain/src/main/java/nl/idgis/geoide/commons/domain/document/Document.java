package nl.idgis.geoide.commons.domain.document;

import java.net.URI;
import java.net.URISyntaxException;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.DocumentStore;

import org.reactivestreams.Publisher;

import akka.util.ByteString;

/**
 * Describes a document that is stored in a {@link DocumentStore} or a {@link DocumentCache}.
 * Documents are identified within a store or cache with a unique URI and they have a contentType
 * and binary body.
 */
public interface Document {
	
	/**
	 * Returns the URI of the document. Each document in a store or a cache is identified with a unique URI.
	 *  
	 * @return The URI of the document.
	 * @throws URISyntaxException 
	 */
	URI getUri () throws URISyntaxException;
	
	/**
	 * Returns the content type of the document.
	 * 
	 * @return The content type of the document.
	 */
	MimeContentType getContentType ();
	
	/**
	 * Returns a reactive streams publisher that produces the document body in the form of a stream of ByteStrings.
	 * The body can be further processed using a StreamProcessor.
	 * 
	 * @return The document body as a stream.
	 */
	Publisher<ByteString> getBody ();
}

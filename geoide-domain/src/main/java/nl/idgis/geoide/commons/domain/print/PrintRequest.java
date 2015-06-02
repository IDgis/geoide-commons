package nl.idgis.geoide.commons.domain.print;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.MimeContentType;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Contains a request to convert an input document (e.g. HTML), optionally containing references to other
 * media such as SVG, JPEG, PNG to "printable" media (e.g. PDF, PostScript).
 */
public final class PrintRequest implements Serializable {
	
	private static final long serialVersionUID = -7949604053058726181L;
	
	private final DocumentReference inputDocument;
	private final MimeContentType outputFormat;
	private final URI baseUri;
	private final Map<String, Object> layoutParameters;

	/**
	 * Constructs a new print request.
	 * 
	 * @param inputDocument	The input document to convert using the print service. Cannot be null.
	 * @param outputFormat	The requested output format, must be one of the supported formats of the service. Cannot be null.
	 */
	public PrintRequest (final DocumentReference inputDocument, final MimeContentType outputFormat, final URI baseUri) {
		this (inputDocument, outputFormat, baseUri, null);
	}
	
	/**
	 * Constructs a new print request.
	 * 
	 * @param inputDocument		The input document to convert using the print service. Cannot be null.
	 * @param outputFormat		The requested output format, must be one of the supported formats of the service. Cannot be null.
	 * @param layoutParameters	A map containing parameters to be used by the layout engine. Can be empty. 
	 */
	public PrintRequest (final DocumentReference inputDocument, final MimeContentType outputFormat, final URI baseUri, final Map<String, Object> layoutParameters) {
		if (inputDocument == null) {
			throw new NullPointerException ("inputDocument cannot be null");
		}
		if (outputFormat == null) {
			throw new NullPointerException ("outputFormat cannot be null");
		}
		
		this.inputDocument = inputDocument;
		this.outputFormat = outputFormat;
		this.baseUri = baseUri;
		this.layoutParameters = externalizeParameters (layoutParameters);
	}
	
	private Map<String, Object> externalizeParameters (final Map<String, Object> parameters) {
		if (parameters == null || parameters.isEmpty ()) {
			return Collections.emptyMap ();
		}
		
		final Map<String, Object> result = new HashMap<> ();
		
		for (final Map.Entry<String, Object> entry: parameters.entrySet ()) {
			if (entry.getValue () == null) {
				result.put (entry.getKey (), null);
			} else if (entry.getValue () instanceof JsonNode) {
				result.put (entry.getKey (), JsonFactory.externalize ((JsonNode) entry.getValue ()));
			} else {
				result.put (entry.getKey (), entry.getValue ());
			}
		}
		
		return result;
	}

	/**
	 * Returns the input document of the print request.
	 * 
	 * @return The (non-null) input document of the print request.
	 */
	public DocumentReference getInputDocument () {
		return inputDocument;
	}

	/**
	 * Returns the requested output format of the print request.
	 * 
	 * @return The (non-null) requested output format.
	 */
	public MimeContentType getOutputFormat () {
		return outputFormat;
	}
	
	/**
	 * Returns the base URI to use when printing. The base URI is used when the print service needs to resolve
	 * a relative URI that points to related content (such as an image in HTML).
	 * 
	 * @return The base URI of the print request.
	 */
	public URI getBaseUri () {
		return baseUri;
	}

	/**
	 * Returns a map containing parameters that should are used by the layout engine.
	 * 
	 * @return A (possibly empty) map containing the layout parameters.
	 */
	public Map<String, Object> getLayoutParameters () {
		return Collections.unmodifiableMap (layoutParameters);
	}
}

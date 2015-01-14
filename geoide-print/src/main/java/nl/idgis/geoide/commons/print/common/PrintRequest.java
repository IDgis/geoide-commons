package nl.idgis.geoide.commons.print.common;

import java.io.Serializable;

import nl.idgis.ogc.util.MimeContentType;

/**
 * Contains a request to convert an input document (e.g. HTML), optionally containing references to other
 * media such as SVG, JPEG, PNG to "printable" media (e.g. PDF, PostScript).
 */
public final class PrintRequest implements Serializable {
	
	private static final long serialVersionUID = -7949604053058726181L;
	
	private final Document inputDocument;
	private final MimeContentType outputFormat;
	
	/**
	 * Constructs a new print request.
	 * 
	 * @param inputDocument	The input document to convert using the print service. Cannot be null.
	 * @param outputFormat	The requested output format, must be one of the supported formats of the service. Cannot be null.
	 */
	public PrintRequest (final Document inputDocument, final MimeContentType outputFormat) {
		if (inputDocument == null) {
			throw new NullPointerException ("inputDocument cannot be null");
		}
		if (outputFormat == null) {
			throw new NullPointerException ("outputFormat cannot be null");
		}
		
		this.inputDocument = inputDocument;
		this.outputFormat = outputFormat;
	}

	/**
	 * Returns the input document of the print request.
	 * 
	 * @return The (non-null) input document of the print request.
	 */
	public Document getInputDocument () {
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
}

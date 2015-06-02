package nl.idgis.geoide.commons.print.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nl.idgis.geoide.commons.domain.MimeContentType;

/**
 * Describes the capabilities of a print-service. A print service has a set of supported
 * inputFormats (which in turn can be converted to various output formats) and has a queue
 * length. The queue length is an approximation of the length of the queue at the time the
 * capabilities were retrieved from the service and can be used for load-balancing. 
 */
public final class Capabilities implements Serializable {
	
	private static final long serialVersionUID = 5019570204375852715L;
	
	private final Set<InputFormat> inputFormats;
	private final long queueLength;

	/**
	 * Constructs a capabilities object for the given formats and queue length.
	 * 
	 * @param inputFormats	All input formats that are supported by the described service.
	 * @param queueLength	The (approximate) length of the print job queue for the described service.
	 */
	public Capabilities (final Collection<InputFormat> inputFormats, final long queueLength) {
		this.inputFormats = inputFormats == null ? Collections.<InputFormat>emptySet () : new HashSet<> (inputFormats);
		this.queueLength = queueLength;
	}

	/**
	 * Returns all supported input formats by the described service and their supported output formats.
	 * 
	 * @return A set of supported input formats for the described service.
	 */
	public Set<InputFormat> getInputFormats () {
		return Collections.unmodifiableSet (inputFormats);
	}

	/**
	 * Looks up a specific input format for the described print service. The input format
	 * must have the given content type as input. Returns null if no matching input format
	 * exists.
	 * 
	 * @param contentType	The content type of the requested input format.
	 * @return				The input format, or null if it doesn't exist.
	 */
	public InputFormat getInputFormat (final MimeContentType contentType) {
		for (final InputFormat inf: inputFormats) {
			if (inf.getContentType().equals (contentType)) {
				return inf;
			}
		}
		
		return null;
	}

	/**
	 * Returns the approximate length of the job queue for the described service. The
	 * queue length is an indication of the number of print jobs that are scheduled for
	 * execution. The queue length can be used for load balancing over multiple service,
	 * the DelegatingPrintService takes queue length into account when selecting a
	 * processor for a print request.
	 * 
	 * @return The approximate queue length
	 */
	public long getQueueLength () {
		return this.queueLength;
	}

	/**
	 * Describes an input format of a print service. An input format consists of a
	 * content type and a set of supported output formats to which the service can
	 * convert the document. The capabilities can also list supported "linked media", for
	 * input formats where this is relevant. A linked media can for example be an SVG
	 * image which is linked from an HTML input.
	 */
	public final static class InputFormat implements Serializable {
		private static final long serialVersionUID = 1403347027993762777L;
		
		private final MimeContentType contentType;
		private final Set<MimeContentType> supportedLinkedMedia;
		private final Set<MimeContentType> outputFormats;

		/**
		 * Constructs a new input format.
		 * 
		 * @param contentType			The content type, cannot be null.
		 * @param supportedLinkedMedia	The set of supported linked media. Can be null or empty.
		 * @param outputFormats			The set of supported output formats. At least one output format must be provided.
		 */
		public InputFormat (final MimeContentType contentType, final Collection<MimeContentType> supportedLinkedMedia,
				final Collection<MimeContentType> outputFormats) {
			if (contentType == null) {
				throw new NullPointerException ("contentType cannot be null");
			}
			if (outputFormats == null || outputFormats.isEmpty ()) {
				throw new IllegalArgumentException ("Must provide at least one output format");
			}
			
			this.contentType = contentType;
			this.supportedLinkedMedia = supportedLinkedMedia == null ? Collections.<MimeContentType>emptySet () : new HashSet<> (supportedLinkedMedia);
			this.outputFormats = outputFormats == null ? Collections.<MimeContentType>emptySet () : new HashSet<> (outputFormats);
		}

		/**
		 * Returns the content type associated with this input format.
		 * 
		 * @return The content type.
		 */
		public MimeContentType getContentType () {
			return contentType;
		}

		/**
		 * Returns all supported linked media, or an empty set. Linked media are
		 * only relevant for content types that support linked media such as HTML.
		 *  
		 * @return The set of supported linked media, potentially empty.
		 */
		public Set<MimeContentType> getSupportedLinkedMedia () {
			return Collections.unmodifiableSet (supportedLinkedMedia);
		}
		
		/**
		 * Returns all supported output formats. 
		 * 
		 * @return All supported output formats in a set containing at least one element.
		 */
		public Set<MimeContentType> getOutputFormats () {
			return Collections.unmodifiableSet (outputFormats);
		}
	}
}

package nl.idgis.geoide.commons.domain.print;

import java.util.Objects;

import nl.idgis.geoide.commons.domain.MimeContentType;

/**
 * A print exception is raised by print services when performing print requests or requesting capabilities. 
 */
public class PrintException extends RuntimeException {

	private static final long serialVersionUID = -8775400804521284943L;
	
	/**
	 * @see RuntimeException#RuntimeException(String)
	 */
	public PrintException (final String message) {
		super (message);
	}
	
	/**
	 * @see RuntimeException#RuntimeException(String,Throwable)
	 */
	public PrintException (final String message, final Throwable cause) {
		super (message, cause);
	}
	
	/**
	 * @see RuntimeException#RuntimeException(Throwable)
	 */
	public PrintException (final Throwable cause) {
		super (cause);
	}

	/**
	 * This error is raised by a print service when the requested format conversion is not possible.
	 */
	public static class UnsupportedFormat extends PrintException {
		private static final long serialVersionUID = 5171759063578599406L;

		private final MimeContentType inputFormat;
		private final MimeContentType outputFormat;

		/**
		 * Constructs a new UnsupportedFormat exception for the given input- and outputformat.
		 * 
		 * @param inputFormat The input format.
		 * @param outputFormat	The output format.
		 */
		public UnsupportedFormat (final MimeContentType inputFormat, final MimeContentType outputFormat) {
			super (String.format ("Unsupported format conversion: %s -> %s", inputFormat.toString (), outputFormat.toString ()));
			
			this.inputFormat = inputFormat;
			this.outputFormat = outputFormat;
		}

		/**
		 * Returns the input format.
		 * 
		 * @return The input format.
		 */
		public MimeContentType getInputFormat () {
			return inputFormat;
		}

		/**
		 * Returns the output format.
		 * 
		 * @return The output format.
		 */
		public MimeContentType getOutputFormat () {
			return outputFormat;
		}
	}
	
	/**
	 * This exception is raised when a resource required for printing could not be found.
	 */
	public static class ResourceNotFound extends PrintException {
		private static final long serialVersionUID = 434753950528412176L;
		
		private final String resourceReference;
		
		public ResourceNotFound (final String resourceReference) {
			this (resourceReference, "Resource not found: " + resourceReference, null);
		}
		
		public ResourceNotFound (final String resourceReference, final Throwable cause) {
			this (resourceReference, "Resource not found: " + resourceReference, cause);
			
		}
		
		public ResourceNotFound (final String resourceReference, final String message) {
			this (resourceReference, message, null);
		}
		
		public ResourceNotFound (final String resourceReference, final String message, final Throwable cause) {
			super (message, cause);
			
			this.resourceReference = Objects.requireNonNull (resourceReference, "resourceReference cannot be null");
		}

		public String getResourceReference() {
			return resourceReference;
		}
		
		
	}
}

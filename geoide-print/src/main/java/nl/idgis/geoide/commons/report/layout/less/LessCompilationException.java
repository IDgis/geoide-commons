package nl.idgis.geoide.commons.report.layout.less;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This exception is thrown by {@link LessCompiler} when compilation fails. Error details are included in the
 * exception: filename, source location and an extract from the source around the error.
 */
public class LessCompilationException extends Exception {

	private static final long serialVersionUID = 5038178287394123404L;
	
	private final String filename;
	private final int line;
	private final int column;
	private final String[] extract;

	/**
	 * Creates a new LessCompilationException.
	 *  
	 * @param message	The error message.
	 * @param filename	The filename of the less source.
	 * @param line		The line number in the source file.
	 * @param column	The zero-based column index in the source file.
	 * @param extract	An array containing at most 3 lines from the source file around the error.
	 */
	public LessCompilationException (final String message, final String filename, final int line, final int column, final String[] extract) {
		super (message);
		
		this.filename = filename;
		this.line = line;
		this.column = column;
		this.extract = extract == null ? new String[0] : Arrays.copyOf (extract, extract.length); 
	}

	/**
	 * @return The name of the source file that contains the error. Or -1 if the error couldn't be related
	 * to a specific location in the source.
	 */
	public String getFilename () {
		return filename;
	}

	/**
	 * @return The line number of the error. Or -1 if the error couldn't be related to a specicif location
	 * in the source.
	 */
	public int getLine () {
		return line;
	}

	/**
	 * @return The zero-based column index of the error.
	 */
	public int getColumn () {
		return column;
	}

	/**
	 * Returns a list containing at most three lines from the source file around the error. If three
	 * lines are provided the error is on the second line. If two lines are provided, the error is on 
	 * the first or the second line depending on whether the error occured on the first line of the source.
	 * The list contains a single item if the source file has only one line. The list is empty if the error
	 * could not be related to a specific source location.
	 * 
	 * @return A list containing lines from the source file around the error.
	 */
	public List<String> getExtract () {
		return Collections.unmodifiableList (Arrays.asList (extract));
	}
}

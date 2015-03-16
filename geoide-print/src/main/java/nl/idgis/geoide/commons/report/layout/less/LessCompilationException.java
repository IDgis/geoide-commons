package nl.idgis.geoide.commons.report.layout.less;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LessCompilationException extends Exception {

	private static final long serialVersionUID = 5038178287394123404L;
	
	private final String filename;
	private final int line;
	private final int column;
	private final String[] extract;
	
	public LessCompilationException (final String message, final String filename, final int line, final int column, final String[] extract) {
		super (message);
		
		this.filename = filename;
		this.line = line;
		this.column = column;
		this.extract = extract == null ? new String[0] : Arrays.copyOf (extract, extract.length); 
	}

	public String getFilename () {
		return filename;
	}

	public int getLine () {
		return line;
	}

	public int getColumn () {
		return column;
	}

	public List<String> getExtract () {
		return Collections.unmodifiableList (Arrays.asList (extract));
	}
}

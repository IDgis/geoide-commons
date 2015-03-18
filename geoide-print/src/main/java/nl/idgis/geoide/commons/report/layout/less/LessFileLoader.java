package nl.idgis.geoide.commons.report.layout.less;

import java.util.Optional;

/**
 * File loader for the less compiler. The loadFile method is invoked (synchronously) whenever the compiler 
 * needs to load an import. 
 */
@FunctionalInterface
public interface LessFileLoader {
	
	/**
	 * 
	 * @param filename				The filename, as found in the import directive in the less source.
	 * @param currentDirectory		The current directory, or null. Passed by the compiler.
	 * @return						Should return the LESS-content of the import, if the file could be found.
	 */
	Optional<String> loadFile (String filename, String currentDirectory);
}
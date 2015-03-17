package nl.idgis.geoide.commons.report.layout.less;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Provides a Java interface to the less CSS compiler (http://lesscss.org). Uses the Nashorn JavaScript
 * engine in combination with the less-node WebJar.
 * 
 * The less compiler has no support for imports.
 * 
 * Usage of a Less compiler is not thread safe.
 */
public class LessCompiler {

	private final static String LESS_PATH = "META-INF/resources/webjars/less-node/%s/lib/";

	private final String lessPath;
	private final ScriptEngineManager scriptEngineManager;
	private final ScriptEngine scriptEngine;
	
	/**
	 * Creates a new less compiler. The constructor looks for the less-node webjar and precompiles all
	 * JavaScript dependencies. Due to the precompilation, a less compiler should be reused as much as possible.
	 */
	public LessCompiler () {
		final String lessVersion;
		
		// Determine the less version:
		final Properties properties = new Properties ();
		try (final InputStream propertiesStream = LessCompiler.class.getClassLoader ().getResourceAsStream ("META-INF/maven/org.webjars/less-node/pom.properties")) {
			properties.load (propertiesStream);
			
			lessVersion = properties.getProperty ("version");
			if (lessVersion == null) {
				throw new IllegalStateException ("Unable to determine the less version. Is the less-node webjar on the classpath?");
			}
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
		
		// Create a JavaScript engine:
		scriptEngineManager = new ScriptEngineManager ();
		
		scriptEngine = scriptEngineManager.getEngineByName ("JavaScript");
		
		lessPath = String.format (LESS_PATH, lessVersion);
		
		// Compile the main script:
		try  {
			scriptEngine.put ("loader", new Loader ());
			
			scriptEngine.getBindings (ScriptContext.ENGINE_SCOPE).put (ScriptEngine.FILENAME, "less-compiler.js");
			scriptEngine.eval (new InputStreamReader (LessCompiler.class.getClassLoader ().getResourceAsStream ("nl/idgis/geoide/commons/report/layout/less/less-compiler.js")));
		} catch (ScriptException e) {
			throw new RuntimeException (e);
		}
	}

	/**
	 * Compiles the given less input to a corresponding CSS document.
	 * 
	 * @param input	The less input.
	 * @return The CSS output after compilation.
	 * @throws LessCompilationException Thrown when an error is encountered during less compilation. Contains detailed
	 * 			information about the cause of the error and the location in the input.
	 */
	public String compile (final String input) throws LessCompilationException {
		return compile (input, null);
	}

	/**
	 * Compiles the given less input to a corresponding CSS document. Global variables can be passed to the less
	 * document.
	 * 
	 * @param input The less input.
	 * @param variables A map containging optional variables that are used during less compilation.
	 * @return The CSS output after compilation.
	 * @throws LessCompilationException Thrown when an error is encountered during less compilation. Contains detailed
	 * 			information about the cause of the error and the location in the input.
	 */
	public String compile (final String input, final Map<String, String> variables) throws LessCompilationException {
		try {
			final Object result = ((Invocable) scriptEngine).invokeFunction ("lessCompile", input, variables);
			if (result instanceof LessCompilationException) {
				throw (LessCompilationException) result;
			}
			return result == null ? null : result.toString ();
		} catch (ScriptException e) {
			throw new RuntimeException (e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException (e);
		}
	}
	
	/**
	 * Resource loader, used internally by the LESS compiler.
	 */
	public class Loader {
		
		public String[] load (final String path) {
			final String finalPath;
			final InputStream is;
			final InputStream unmodified = tryOpen (path);
			if (unmodified == null) {
				final String indexPath = path + (path.endsWith ("/") ? "" : "/") + "index";
				final InputStream index = tryOpen (indexPath);
				finalPath = indexPath;
				if (index != null) {
					is = index;
				} else {
					throw new RuntimeException ("Path not found: " + path);
				}
			} else {
				is = unmodified;
				finalPath = path;
			}
			
			try (final InputStream inputStream = is) {
				try (final Reader reader = new InputStreamReader (inputStream, Charset.forName ("UTF-8"))) {
					final char[] buffer = new char[1024];
					final StringBuffer content = new StringBuffer ();
					int n;
					
					while ((n = reader.read (buffer)) >= 0) {
						if (n == 0) {
							continue;
						}
						
						if (n == buffer.length) {
							content.append (buffer);
						} else {
							content.append (Arrays.copyOf (buffer, n));
						}
					}
			
					return new String[] { finalPath, content.toString () };
				}
			} catch (IOException e) {
				throw new RuntimeException (e);
			}
		}
		
		private InputStream tryOpen (final String path) {
			return LessCompiler.class.getClassLoader ().getResourceAsStream (lessPath + path + ".js");
		}
	}
}

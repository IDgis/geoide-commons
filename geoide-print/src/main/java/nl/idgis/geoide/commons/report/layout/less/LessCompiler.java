package nl.idgis.geoide.commons.report.layout.less;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.DatatypeConverter;

public class LessCompiler {

	private final static String LESS_PATH = "META-INF/resources/webjars/less-node/%s/lib/";

	private final String lessPath;
	private final ScriptEngineManager scriptEngineManager;
	private final ScriptEngine scriptEngine;
	
	private final Map<String, Object> requireJsCache = new HashMap<> ();
	
	public LessCompiler (final String lessVersion) {
		// Create a JavaScript engine:
		scriptEngineManager = new ScriptEngineManager ();
		
		scriptEngine = scriptEngineManager.getEngineByName ("JavaScript");
		
		lessPath = String.format (LESS_PATH, lessVersion);
		
		// Compile the main script:
		try  {
			scriptEngine.put ("req", this);
			scriptEngine.put ("env", new Environment ());
			scriptEngine.put ("fs", new Filesystem ());
			
			scriptEngine.getBindings (ScriptContext.ENGINE_SCOPE).put (ScriptEngine.FILENAME, "less-compiler.js");
			scriptEngine.eval (new InputStreamReader (LessCompiler.class.getClassLoader ().getResourceAsStream ("nl/idgis/geoide/commons/report/layout/less/less-compiler.js")));
			
			// scriptEngine.eval ("print (new less.Parser ().parser ('a { }'));");
			scriptEngine.eval ("less.render (\"\\na {\\n }\\n\", { processImports: false }, function (a, b) { print (a); print (b); });");
			//scriptEngine.eval ("var parser = new less.Parser ({ processImports: false }, new less.ImportManager ()); parser.parse ('a { }', function (e, r) { print (r); });");
		} catch (ScriptException e) {
			throw new RuntimeException (e);
		}
	}

	private String combine (String basePath, String path) {
		while (true) {
			if (path.startsWith ("./")) {
				path = path.substring (2);
			} else if (path.startsWith ("../")) {
				final int n = basePath.lastIndexOf ('/');
				if (n >= 0) {
					basePath = basePath.substring (0, n);
				}
				path = path.substring (3);
			} else {
				break;
			}
		}
		
		return basePath + "/" + path;
	}
	
	public Object require (final String path) {
		return require (path, null);
	}
	
	public Object require (final String path, final String basePath) {
		final String absolutePath;
		
		if (basePath != null && path.startsWith (".")) {
			final int n = basePath.lastIndexOf ('/');
			
			if (n >= 0) {
				absolutePath = combine (basePath.substring (0, n), path);
			} else {
				absolutePath = combine ("", path);
			}
		} else {
			absolutePath = path;
		}
		
		if (requireJsCache.containsKey (absolutePath)) {
			return requireJsCache.get (absolutePath);
		}
		
		final String finalPath;
		
		final InputStream is;
		final InputStream unmodified = tryOpen (absolutePath);
		if (unmodified == null) {
			final String indexPath = absolutePath + (absolutePath.endsWith ("/") ? "" : "/") + "index";
			final InputStream index = tryOpen (indexPath);
			if (index != null) {
				is = index;
				finalPath = indexPath;
			} else {
				throw new RuntimeException ("Path not found: " + path);
			}
		} else {
			is = unmodified;
			finalPath = absolutePath;
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
		
				scriptEngine.getBindings (ScriptContext.ENGINE_SCOPE).put (ScriptEngine.FILENAME, finalPath + ".js");
				
				final Object value = scriptEngine.eval ("(function () { var module = { }; var require = function (a) { return req.require (a, '" + finalPath + "'); }; " + content + "\nreturn module.exports; }) ();");

				requireJsCache.put (absolutePath, value);
				
				return value;
			}
		} catch (IOException e) {
			throw new RuntimeException (e);
		} catch (ScriptException e) {
			throw new RuntimeException (e);
		}
	}
	
	private InputStream tryOpen (final String path) {
		return LessCompiler.class.getClassLoader ().getResourceAsStream (lessPath + path + ".js");
	}
	
	// Must implement the api specified in environment-api.js:
	public static class Environment {
		public String encodeBase64 (final String input) {
			return DatatypeConverter.printBase64Binary (input.getBytes ());
		}
	}
	
	// Must implement the api specified in file-manager-api.js:
	public static class Filesystem {
		
	}
}

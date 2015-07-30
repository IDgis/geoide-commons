package nl.idgis.geoide.commons.report.layout.less;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.idgis.geoide.commons.domain.report.LessCompilationException;

/**
 * Test cases for {@link LessCompiler}.
 */
public class TestLessCompiler {

	private static LessCompiler compiler;
	private static LessFileLoader loader = new LessFileLoader () {
		@Override
		public Optional<String> loadFile (final String filename, final String currentDirectory) {
			return Optional.of ("a { display: block; }");
		}
	};

	/**
	 * Creates a new {@link LessCompiler}.
	 */
	@BeforeClass
	public static void createCompiler () {
		compiler = new LessCompiler ();
	}

	/**
	 * Verifies that a well-formed less document can be translated to CSS by the compiler.
	 */
	@Test
	public void testCompile () throws Throwable {
		assertEquals ("a{display:block}", compiler.compile ("a { display: block; }", loader));
	}

	/**
	 * Verifies that a well-formed less document with a variable reference is compiled
	 * when the value of the variable is passed to the compiler in a Java map.
	 */
	@Test
	public void testVariable () throws Throwable {
		final Map<String, String> variables = new HashMap<> ();
		
		variables.put ("test-color", "#012");
		
		assertEquals ("a{color:#012}", compiler.compile ("a { color: @test-color; }", variables, loader));
	}

	/**
	 * Verifies that the compiler throws an exception if a variable is referenced that is not defined.
	 */
	@Test (expected = LessCompilationException.class)
	public void testVariableNotFound () throws Throwable {
		final Map<String, String> variables = new HashMap<> ();
		
		variables.put ("test-color", "#012");
		
		compiler.compile ("a { color: @test-color2; }", variables, loader);
	}

	/**
	 * Verifies that compiler exceptions provide a source location.
	 */
	@Test
	public void testCompilationException () {
		try {
			compiler.compile ("a {\ndisplay:\n}\n", loader);
		} catch (LessCompilationException e) {
			assertEquals (3, e.getLine ());
			assertEquals (0, e.getColumn ());
			assertEquals ("input", e.getFilename ());
			assertEquals (3, e.getExtract ().size ());
			assertEquals ("display:", e.getExtract ().get (0));
			assertEquals ("}", e.getExtract ().get (1));
			assertEquals ("", e.getExtract ().get (2));
			return;
		}
		
		fail ("Expected exception");
	}
	
	/**
	 * Verifies that imports are processed by the compiler.
	 */
	@Test
	public void testImport () throws Throwable {
		assertEquals ("a{display:block}", compiler.compile ("@import \"test.less\";", loader));
	}
	
	/**
	 * Verifies that non-existing imports result in an error.
	 */
	@Test (expected = LessCompilationException.class)
	public void testImportNotFound () throws Throwable {
		compiler.compile ("@import \"test.less\";", (filename, directory) -> Optional.empty ());
	}
	
	/**
	 * Verifies that imports with syntax errors result in a compilation error.
	 */
	@Test
	public void testImportInvalidContent () throws Throwable {
		try {
			compiler.compile ("@import \"test.less\";", (filename, directory) -> Optional.of ("a { display: }"));
		} catch (LessCompilationException e) {
			assertEquals (1, e.getLine ());
			assertEquals (13, e.getColumn ());
			assertEquals ("test.less", e.getFilename ());
			assertEquals (3, e.getExtract ().size ());
			assertEquals ("a { display: }", e.getExtract ().get (1));
			return;
		}
		
		fail ("Expected exception");
	}
}

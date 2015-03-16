package nl.idgis.geoide.commons.report.layout.less;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestLessCompiler {

	private static LessCompiler compiler;
	
	@BeforeClass
	public static void createCompiler () {
		compiler = new LessCompiler ("2.3.1");
	}
	
	@Test
	public void testCompile () throws Throwable {
		assertEquals ("a{display:block}", compiler.compile ("a { display: block; }"));
	}
	
	@Test
	public void testVariable () throws Throwable {
		final Map<String, String> variables = new HashMap<> ();
		
		variables.put ("test-color", "#012");
		
		assertEquals ("a{color:#012}", compiler.compile ("a { color: @test-color; }", variables));
	}
	
	@Test (expected = LessCompilationException.class)
	public void testVariableNotFound () throws Throwable {
		final Map<String, String> variables = new HashMap<> ();
		
		variables.put ("test-color", "#012");
		
		compiler.compile ("a { color: @test-color2; }", variables);
	}
	
	@Test
	public void testCompilationException () {
		try {
			compiler.compile ("a {\ndisplay:\n}\n");
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
	
	@Test (expected = LessCompilationException.class)
	public void testImport () throws Throwable {
		compiler.compile ("@import \"test.less\";");
	}
}

package nl.idgis.geoide.commons.report.layout.less;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestLessCompiler {

	@Test
	public void testCreateCompiler () {
		final LessCompiler compiler = new LessCompiler ("2.3.1");
	}
}

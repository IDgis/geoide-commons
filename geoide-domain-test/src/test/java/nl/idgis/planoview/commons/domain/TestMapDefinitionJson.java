package nl.idgis.planoview.commons.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestMapDefinitionJson {

	@Test
	public void testMapDefinition () {
		assertNotNull (TestDomain.mapDefinition ());
	}
}

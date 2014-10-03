package nl.idgis.geoide.commons.domain;

import static org.junit.Assert.*;
import nl.idgis.geoide.commons.domain.TestDomain;

import org.junit.Test;

public class TestMapDefinitionJson {

	@Test
	public void testMapDefinition () {
		assertNotNull (TestDomain.mapDefinition ());
	}
}

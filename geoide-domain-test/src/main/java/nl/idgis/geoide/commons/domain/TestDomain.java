package nl.idgis.geoide.commons.domain;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.MapDefinition;

public class TestDomain {

	public static MapDefinition mapDefinition () {
		return JsonFactory.mapDefinition (TestDomain.class.getClassLoader ().getResourceAsStream ("nl/idgis/planoview/commons/domain/map-definition.json"));
	}
}

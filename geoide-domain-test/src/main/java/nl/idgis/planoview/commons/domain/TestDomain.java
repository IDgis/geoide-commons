package nl.idgis.planoview.commons.domain;

public class TestDomain {

	public static MapDefinition mapDefinition () {
		return JsonFactory.mapDefinition (TestDomain.class.getClassLoader ().getResourceAsStream ("nl/idgis/planoview/commons/domain/map-definition.json"));
	}
}

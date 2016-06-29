package nl.idgis.geoide.commons.domain.provider;
import nl.idgis.geoide.commons.domain.QueryDescription;

public interface QueryDescriptionProvider {
	QueryDescription getQueryDescription (String queryDescriptionId);
}

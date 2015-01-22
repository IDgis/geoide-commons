package nl.idgis.geoide.commons.domain.provider;

import nl.idgis.geoide.commons.domain.FeatureType;

public interface FeatureTypeProvider {
	FeatureType getFeatureType (String featureTypeId);
}

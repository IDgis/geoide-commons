package nl.idgis.geoide.commons.domain.provider;

import java.util.List;

import nl.idgis.geoide.commons.domain.FeatureType;
import nl.idgis.geoide.commons.domain.ServiceLayer;

public interface ServiceLayerProvider {
	ServiceLayer getServiceLayer (String serviceLayerId, String token);
	List<ServiceLayer> getServiceLayers (List<String> serviceLayerId, String token);
	FeatureType getFeatureType (String serviceLayerId, String token);
}

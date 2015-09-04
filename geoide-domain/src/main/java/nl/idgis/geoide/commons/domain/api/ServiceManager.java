package nl.idgis.geoide.commons.domain.api;

import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.service.Capabilities;
import nl.idgis.geoide.commons.domain.service.QueryFeaturesResponse;
import nl.idgis.geoide.commons.domain.service.ServiceResponse;

public interface ServiceManager {

	CompletableFuture<Capabilities> getCapabilities (
			ServiceIdentification serviceIdentification);
	
	CompletableFuture<QueryFeaturesResponse> queryFeatures (
			ServiceIdentification serviceIdentification, 
			ParameterizedFeatureType<?> featureType);

	CompletableFuture<ServiceResponse> serviceRequest (
			ServiceIdentification serviceIdentification,
			String layerName,
			String path);
}

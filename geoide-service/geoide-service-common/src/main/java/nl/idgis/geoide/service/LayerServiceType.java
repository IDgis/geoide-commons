package nl.idgis.geoide.service;

import java.util.List;

import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceRequest;

import com.fasterxml.jackson.databind.JsonNode;

public interface LayerServiceType {

	List<ServiceRequest> getServiceRequests (Service service, List<ParameterizedServiceLayer<?>> serviceLayers, ServiceRequestContext context);
	
	List<JsonNode> getLayerRequestUrls(ServiceRequest serviceRequest, JsonNode mapExtent, double resolution, int outputWidth, int outputHeight);

}

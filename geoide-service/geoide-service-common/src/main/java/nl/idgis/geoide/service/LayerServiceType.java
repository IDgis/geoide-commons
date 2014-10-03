package nl.idgis.geoide.service;

import java.util.List;

import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceRequest;

public interface LayerServiceType {

	List<ServiceRequest> getServiceRequests (Service service, List<ParameterizedServiceLayer<?>> serviceLayers, ServiceRequestContext context);
}

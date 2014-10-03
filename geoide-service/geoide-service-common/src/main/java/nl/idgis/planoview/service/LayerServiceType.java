package nl.idgis.planoview.service;

import java.util.List;

import nl.idgis.planoview.commons.domain.ParameterizedServiceLayer;
import nl.idgis.planoview.commons.domain.Service;
import nl.idgis.planoview.commons.domain.ServiceRequest;

public interface LayerServiceType {

	List<ServiceRequest> getServiceRequests (Service service, List<ParameterizedServiceLayer<?>> serviceLayers, ServiceRequestContext context);
}

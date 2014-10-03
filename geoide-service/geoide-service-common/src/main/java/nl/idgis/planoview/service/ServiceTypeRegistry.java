package nl.idgis.planoview.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ServiceTypeRegistry {
	
	private final Map<String, ServiceType> serviceTypes;
	
	public ServiceTypeRegistry (final Collection<ServiceType> serviceTypes) {
		this.serviceTypes = new HashMap<String, ServiceType> ();
		
		if (serviceTypes != null) {
			for (final ServiceType serviceType: serviceTypes) {
				this.serviceTypes.put (serviceType.getTypeName ().toLowerCase (), serviceType);
			}
		}
	}
	
	public ServiceType getServiceType (final String name) {
		if (name == null) {
			return null;
		}
		
		return serviceTypes.get (name.toLowerCase ());
	}
}

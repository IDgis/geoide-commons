package nl.idgis.geoide.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.commons.domain.traits.Traits;

public final class ServiceTypeRegistry {
	
	private final Map<String, Traits<ServiceType>> serviceTypes;
	
	public ServiceTypeRegistry (final Collection<Traits<ServiceType>> serviceTypes) {
		this.serviceTypes = new HashMap<String, Traits<ServiceType>> ();
		
		if (serviceTypes != null) {
			for (final Traits<ServiceType> serviceType: serviceTypes) {
				this.serviceTypes.put (serviceType.get ().getTypeName ().toLowerCase (), serviceType);
			}
		}
	}
	
	public Traits<ServiceType> getServiceType (final String name) {
		if (name == null) {
			return null;
		}
		
		return serviceTypes.get (name.toLowerCase ());
	}
}

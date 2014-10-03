package nl.idgis.planoview.service;

import java.util.HashMap;
import java.util.Map;

import nl.idgis.planoview.commons.domain.Service;

public final class ServiceRequestContext {

	private final Map<String, Integer> serviceCount = new HashMap<> ();
	
	public ServiceRequestContext () {
		
	}
	
	public String nextServiceIdentifier (final Service service, final String additionalKey) {
		final int currentCount;
		final String key = additionalKey != null ? String.format ("%s/%s", service.getId (), additionalKey) : service.getId ();
		
		if (!serviceCount.containsKey (key)) {
			currentCount = 0;
		} else {
			currentCount = serviceCount.get (key);
		}
		
		final String id = String.format ("%s/%s/%d", 
				service.getIdentification ().getServiceType (),
				key,
				currentCount
			);
		
		serviceCount.put (key, Integer.valueOf (currentCount + 1));
		
		return id;
	}
}

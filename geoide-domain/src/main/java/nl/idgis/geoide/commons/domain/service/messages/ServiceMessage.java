package nl.idgis.geoide.commons.domain.service.messages;

import java.io.Serializable;

import nl.idgis.geoide.commons.domain.ServiceIdentification;

public abstract class ServiceMessage implements Serializable {
	private static final long serialVersionUID = 3532272462881762088L;
	
	private final ServiceIdentification serviceIdentification;
	private final ServiceMessageContext context;
	
	public ServiceMessage (final ServiceIdentification serviceIdentification) {
		this (serviceIdentification, null);
	}
	
	public ServiceMessage (final ServiceIdentification serviceIdentification, final ServiceMessageContext context) {
		if (serviceIdentification == null) {
			throw new NullPointerException ("serviceIdentification cannot be null");
		}
		
		this.serviceIdentification = serviceIdentification;
		this.context = context;
	}
	
	public ServiceIdentification serviceIdentification () {
		return serviceIdentification;
	}
	
	public ServiceMessageContext context () {
		return context;
	}
}
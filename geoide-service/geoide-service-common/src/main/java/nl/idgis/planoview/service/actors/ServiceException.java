package nl.idgis.planoview.service.actors;

import nl.idgis.geoide.commons.domain.ServiceIdentification;

public final class ServiceException extends Exception {
	private static final long serialVersionUID = -4093759842527382662L;
	
	private final ServiceIdentification identification;
	
	public ServiceException (final ServiceIdentification identification, final String message) {
		super (message);
		
		this.identification = identification;
	}
	
	public ServiceIdentification getIdentification () {
		return identification;
	}
	
}
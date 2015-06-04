package nl.idgis.geoide.service.messages;

import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.service.Capabilities;
import nl.idgis.geoide.commons.domain.service.messages.ServiceMessage;

public final class ServiceCapabilities extends ServiceMessage {
	private static final long serialVersionUID = 2168282245978869270L;
	
	private final Capabilities capabilities;
	
	public ServiceCapabilities (final ServiceIdentification serviceIdentification, final Capabilities capabilities) {
		super (serviceIdentification);
		
		if (capabilities == null) {
			throw new NullPointerException ("capabilities cannot be null");
		}
		
		this.capabilities = capabilities;
	}
	
	public Capabilities capabilities () {
		return capabilities;
	}
}
package nl.idgis.geoide.service.messages;

import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.service.messages.ServiceMessage;

public final class GetServiceCapabilities extends ServiceMessage {
	private static final long serialVersionUID = -5645667763991974672L;

	public GetServiceCapabilities(final ServiceIdentification serviceIdentification) {
		super(serviceIdentification);
	}
}
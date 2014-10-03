package nl.idgis.planoview.service.messages;

import nl.idgis.planoview.commons.domain.ServiceIdentification;

public final class GetServiceCapabilities extends ServiceMessage {
	private static final long serialVersionUID = -5645667763991974672L;

	public GetServiceCapabilities(final ServiceIdentification serviceIdentification) {
		super(serviceIdentification);
	}
}
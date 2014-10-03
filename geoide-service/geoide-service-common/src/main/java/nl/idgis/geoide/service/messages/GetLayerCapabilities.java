package nl.idgis.geoide.service.messages;

import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.services.Capabilities;

public final class GetLayerCapabilities extends ServiceMessage {
	private static final long serialVersionUID = 5478904051782573596L;

	private final Capabilities.Layer layer;
	
	public GetLayerCapabilities (final ServiceIdentification serviceIdentification, final Capabilities.Layer layer) {
		super(serviceIdentification);
		
		if (layer == null) {
			throw new NullPointerException ("layer cannot be null");
		}
		
		this.layer = layer;
	}
	
	public Capabilities.Layer layer () {
		return layer;
	}
}
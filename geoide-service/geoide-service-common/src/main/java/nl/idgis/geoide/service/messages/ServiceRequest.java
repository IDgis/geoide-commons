package nl.idgis.geoide.service.messages;

import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.planoview.util.Assert;

public final class ServiceRequest extends ServiceMessage {

	private static final long serialVersionUID = -3737703804536316113L;

	private final String layerName;
	private final String path;
	
	public ServiceRequest (
			final ServiceIdentification identification,
			final String layerName,
			final String path) {
		super (identification);
		
		Assert.notNull (layerName, "layerName");
		Assert.notNull (path, "path");
		
		this.layerName = layerName;
		this.path = path;
	}
	
	public String layerName () {
		return this.layerName;
	}
	
	public String path () {
		return this.path;
	}
}

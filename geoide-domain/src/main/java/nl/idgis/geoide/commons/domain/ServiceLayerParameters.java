package nl.idgis.geoide.commons.domain;

import java.io.Serializable;

public class ServiceLayerParameters implements Serializable {
	private static final long serialVersionUID = -4063556131341908777L;
	
	private final boolean editable;
	
	public ServiceLayerParameters (final boolean editable) {
		this.editable = editable;
	}

	public boolean isEditable () {
		return editable;
	}
}

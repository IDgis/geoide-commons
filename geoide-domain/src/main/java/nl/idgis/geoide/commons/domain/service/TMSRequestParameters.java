package nl.idgis.geoide.commons.domain.service;

import java.io.Serializable;

public class TMSRequestParameters implements Serializable {
	private static final long serialVersionUID = 5824460737314719140L;
	
	private final String layer;
	
	public TMSRequestParameters (final String layer) {
		this.layer = layer;
	}

	public String getLayer () {
		return layer;
	}
}
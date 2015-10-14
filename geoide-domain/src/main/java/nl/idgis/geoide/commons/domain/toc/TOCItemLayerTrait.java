package nl.idgis.geoide.commons.domain.toc;

import nl.idgis.geoide.commons.domain.LayerRef;

public final class TOCItemLayerTrait implements TOCItemTrait {
	private static final long serialVersionUID = 5042289564333777461L;
	
	private final LayerRef layer; 
	
	public TOCItemLayerTrait(LayerRef layer) {
		this.layer = layer;
	}
	
	public LayerRef getLayer() {
		return layer;
	}

}

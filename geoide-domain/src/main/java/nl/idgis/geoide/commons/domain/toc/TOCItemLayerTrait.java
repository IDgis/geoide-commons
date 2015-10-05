package nl.idgis.geoide.commons.domain.toc;

import nl.idgis.geoide.commons.domain.Layer;

public final class TOCItemLayerTrait implements TOCItemTrait {
	private static final long serialVersionUID = 5042289564333777461L;
	
	private final Layer layer; 
	
	public TOCItemLayerTrait(Layer layer) {
		this.layer = layer;
	}
	
	public Layer getLayer() {
		return layer;
	}

}

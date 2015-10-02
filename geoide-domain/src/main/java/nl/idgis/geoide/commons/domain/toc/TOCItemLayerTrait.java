package nl.idgis.geoide.commons.domain.toc;

import nl.idgis.geoide.commons.domain.MapLayer;

public final class TOCItemLayerTrait implements TOCItemTrait {
	private static final long serialVersionUID = 5042289564333777461L;
	
	private final MapLayer layer; 
	
	public TOCItemLayerTrait(MapLayer layer) {
		this.layer = layer;
	}
	
	public MapLayer getLayer() {
		return layer;
	}

}

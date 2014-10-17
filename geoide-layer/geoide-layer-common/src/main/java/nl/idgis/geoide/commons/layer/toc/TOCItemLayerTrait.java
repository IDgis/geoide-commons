package nl.idgis.geoide.commons.layer.toc;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.toc.TOCItemTrait;

public final class TOCItemLayerTrait implements TOCItemTrait {
	private final Layer layer; 
	
	public TOCItemLayerTrait(Layer layer) {
		this.layer = layer;
	}
	
	public Layer getLayer() {
		return layer;
	}

}

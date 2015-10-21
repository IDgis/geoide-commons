package nl.idgis.geoide.commons.domain.toc;

import nl.idgis.geoide.commons.domain.LayerRef;

public final class TOCItemLayerRefTrait implements TOCItemTrait {
	private static final long serialVersionUID = 5042289564333777461L;
	
	private final LayerRef layerRef; 
	
	public TOCItemLayerRefTrait(LayerRef layerRef) {
		this.layerRef = layerRef;
	}
	
	public LayerRef getLayerRef() {
		return layerRef;
	}

}

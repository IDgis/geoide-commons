package nl.idgis.planoview.commons.layer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.commons.domain.Layer;

public class LayerTypeRegistry {

	private final Map<String, LayerType> layerTypes;
	
	public LayerTypeRegistry (final Collection<LayerType> layerTypes) {
		this.layerTypes = new HashMap<> ();
		if (layerTypes != null) {
			for (final LayerType layerType: layerTypes) {
				this.layerTypes.put (layerType.getTypeName (), layerType);
			}
		}
	}
	
	public LayerType getLayerType (final Layer layer) {
		return getLayerType (layer.getLayerType ());
	}
	
	public LayerType getLayerType (final String typeName) {
		if (typeName == null) {
			return null;
		}
		
		return this.layerTypes.get (typeName);
	}
}

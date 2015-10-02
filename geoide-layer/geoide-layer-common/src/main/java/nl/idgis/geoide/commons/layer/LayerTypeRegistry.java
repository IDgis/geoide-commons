package nl.idgis.geoide.commons.layer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.commons.domain.MapLayer;
import nl.idgis.geoide.commons.domain.traits.Traits;

public class LayerTypeRegistry {

	private final Map<String, Traits<LayerType>> layerTypes;
	
	public LayerTypeRegistry (final Collection<Traits<LayerType>> layerTypes) {
		this.layerTypes = new HashMap<> ();
		if (layerTypes != null) {
			for (final Traits<LayerType> layerType: layerTypes) {
				this.layerTypes.put (layerType.get ().getTypeName (), layerType);
			}
		}
	}
	
	public Traits<LayerType> getLayerType (final MapLayer layer) {
		return getLayerType (layer.getLayerType ());
	}
	
	public Traits<LayerType> getLayerType (final String typeName) {
		if (typeName == null) {
			return null;
		}
		
		return this.layerTypes.get (typeName);
	}
}

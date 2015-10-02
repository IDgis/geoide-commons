package nl.idgis.geoide.commons.domain.provider;

import java.util.List;

import nl.idgis.geoide.commons.domain.MapLayer;

public interface LayerProvider {
	MapLayer getLayer (String layer);
	List<MapLayer> getLayers (List<String> layerIds);
}

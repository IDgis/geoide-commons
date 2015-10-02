package nl.idgis.geoide.commons.domain.provider;


import java.util.List;

import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.MapLayer;

public interface MapProvider {
	MapDefinition getMapDefinition (String mapId);
	List<MapLayer> getLayers(String mapId);
	List<MapLayer> getRootLayers(String mapId);
}

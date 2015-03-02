package nl.idgis.geoide.commons.domain.provider;


import java.util.List;

import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.Layer;

public interface MapProvider {
	MapDefinition getMapDefinition (String mapId);
	List<Layer> getLayers(String mapId);
	List<Layer> getRootLayers(String mapId);
}

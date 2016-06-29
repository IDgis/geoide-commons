package nl.idgis.geoide.commons.domain.provider;


import java.util.List;

import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.QueryDescription;
import nl.idgis.geoide.commons.domain.LayerRef;

public interface MapProvider {
	MapDefinition getMapDefinition (String mapId);
	List<LayerRef> getLayers(String mapId);
	List<LayerRef> getRootLayers(String mapId);
	List<QueryDescription> getQueryDescriptions(String mapId);
}

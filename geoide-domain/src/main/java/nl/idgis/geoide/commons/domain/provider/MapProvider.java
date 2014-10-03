package nl.idgis.geoide.commons.domain.provider;

import java.util.List;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.Service;

public interface MapProvider {

	MapDefinition getMapDefinition (String mapId);
	Layer getLayer (String layer);
	List<Layer> getLayers (List<String> layerIds);
	Service getService (String serviceId);
}

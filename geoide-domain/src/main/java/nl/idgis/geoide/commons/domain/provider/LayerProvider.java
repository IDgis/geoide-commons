package nl.idgis.geoide.commons.domain.provider;

import java.util.List;

import nl.idgis.geoide.commons.domain.Layer;

public interface LayerProvider {
	Layer getLayer (String layer, String token);
	List<Layer> getLayers (List<String> layerIds, String token);
}

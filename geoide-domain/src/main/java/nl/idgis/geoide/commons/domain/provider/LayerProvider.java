package nl.idgis.geoide.commons.domain.provider;

import java.util.List;

import nl.idgis.geoide.commons.domain.Layer;

public interface LayerProvider {
	Layer getLayer (String layer);
	List<Layer> getLayers (List<String> layerIds);
}

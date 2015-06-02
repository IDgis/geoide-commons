package nl.idgis.geoide.commons.domain.layer;

import java.util.List;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.traits.Traits;

public interface LayerState {
	Layer getLayer ();
	boolean isVisible ();
	List<Traits<LayerState>> getParents ();
}

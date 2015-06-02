package nl.idgis.geoide.commons.domain.layer;

import java.io.Serializable;
import java.util.List;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.traits.Traits;

public interface LayerState extends Serializable {
	Layer getLayer ();
	boolean isVisible ();
	List<Traits<LayerState>> getParents ();
}

package nl.idgis.geoide.commons.domain.layer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.traits.Traits;

public final class LayerState implements Serializable {
	private static final long serialVersionUID = 3334487710609145169L;
	
	private final Layer layer;
	private final boolean visible;
	private final List<Traits<LayerState>> parents;
	
	public LayerState (final Layer layer, final boolean visible, final List<Traits<LayerState>> parents) {
		this.layer = layer;
		this.visible = visible;
		this.parents = parents == null || parents.isEmpty () ? Collections.emptyList () : new ArrayList<> (parents);
	}
	
	public Layer getLayer () {
		return layer;
	}
	
	public boolean isVisible () {
		return visible;
	}
	
	public List<Traits<LayerState>> getParents () {
		return Collections.unmodifiableList (parents);
	}
}

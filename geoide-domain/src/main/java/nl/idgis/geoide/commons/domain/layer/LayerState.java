package nl.idgis.geoide.commons.domain.layer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.traits.Traits;

import com.fasterxml.jackson.databind.JsonNode;

public final class LayerState implements Serializable {
	private static final long serialVersionUID = 3334487710609145169L;
	
	private final Layer layer;
	private final boolean visible;
	private final List<Traits<LayerState>> parents;
	private final ExternalizableJsonNode properties;
	
	public LayerState (final Layer layer, final boolean visible, final List<Traits<LayerState>> parents, final Optional<JsonNode> properties) {
		this.layer = layer;
		this.visible = visible;
		this.parents = parents == null || parents.isEmpty () ? Collections.emptyList () : new ArrayList<> (parents);
		this.properties = properties.isPresent () ? JsonFactory.externalize (properties.get ().deepCopy ()) : JsonFactory.externalize (JsonFactory.mapper ().createObjectNode ());
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
	
	public ExternalizableJsonNode getProperties () {
		return JsonFactory.externalize (properties.deepCopy ());
	}
}

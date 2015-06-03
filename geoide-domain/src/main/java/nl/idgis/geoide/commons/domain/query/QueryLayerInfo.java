package nl.idgis.geoide.commons.domain.query;

import java.io.Serializable;
import java.util.Optional;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.FeatureQuery;
import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.Layer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

public final class QueryLayerInfo implements Serializable {
	private static final long serialVersionUID = 7062965007858043754L;
	
	private final Layer layer;
	private final ExternalizableJsonNode state;
	private final FeatureQuery query;
	
	public QueryLayerInfo (final Layer layer, final Optional<JsonNode> state, final Optional<FeatureQuery> query) {
		if (layer == null) {
			throw new NullPointerException ("layer cannot be null");
		}
		if (state == null) {
			throw new NullPointerException ("state cannot be null");
		}
		if (query == null) {
			throw new NullPointerException ("query cannot be null");
		}
		
		this.layer = layer;
		this.state = state.isPresent () ? JsonFactory.externalize (state.get ().deepCopy ()) : JsonFactory.externalize (MissingNode.getInstance ());
		this.query = query.isPresent () ? query.get () : null;
	}

	public Layer getLayer () {
		return layer;
	}
	
	public ExternalizableJsonNode getState () {
		return state;
	}
	
	public Optional<FeatureQuery> getQuery () {
		return query == null ? Optional.empty () : Optional.of (query);
	}
}

package nl.idgis.geoide.commons.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.idgis.geoide.util.Assert;

public class LayerRef implements Serializable {

	private static final long serialVersionUID = 7884660135101590357L;
	private final Layer layer;
	private final List<LayerRef> layerRefs;
	private final LayerState state;
	
	@JsonCreator
	public LayerRef (
			final @JsonProperty("layer") Layer layer,
			final @JsonProperty("layers") List<LayerRef> layerRefs,
			final @JsonProperty("state") Map <String,JsonNode> state) {
				
		Assert.notNull (layer, "layer");
		
		this.layer = layer;
		this.layerRefs = layerRefs == null ? Collections.<LayerRef>emptyList () : new ArrayList<> (layerRefs);
		this.state = new LayerState (state);   
		
				
	}
	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode n = JsonFactory.mapper ().createObjectNode ();
		
		n.put ("layer", layer.serialize());
		
		if (!getLayerRefs ().isEmpty ()) {
			final ArrayNode layersNode = n.putArray ("layerRefs");
			
			for (final LayerRef layerRef: getLayerRefs ()) {
				layersNode.add (JsonFactory.mapper ().valueToTree (layerRef));
			}
		}
		
		n.put ("state", state.serialize());
		
		return n;
	}
	
	public Layer getLayer () {
		return layer;
	}

	public List<LayerRef> getLayerRefs () {
		return Collections.unmodifiableList (layerRefs);
	}
	
	public LayerState getLayerState () {
		return state;
	}
	
	
	public String getInitialStateValue (String stateProperty) {
		if(!state.getStateValue (stateProperty).equals ("")) {
			return state.getStateValue(stateProperty);
		} else {
			if(layer.getLayerState ().getStateValue (stateProperty)!=null) {
				return layer.getLayerState ().getStateValue (stateProperty);
			}
		}
		return "";
	}
	
}


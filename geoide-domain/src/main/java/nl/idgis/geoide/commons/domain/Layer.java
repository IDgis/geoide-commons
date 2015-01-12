package nl.idgis.geoide.commons.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.idgis.geoide.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Layer extends Entity {

	private static final long serialVersionUID = 5628518429669556235L;

	private final String layerType;
	private final String label;
	private final List<Layer> layers;
	private final List<ServiceLayer> serviceLayers;
	private final Map<String, JsonNode> state;
	
	@JsonCreator
	public Layer (
			final @JsonProperty("id") String id,
			final @JsonProperty("layerType") String layerType,
			final @JsonProperty("label") String label,
			final @JsonProperty("layers") List<Layer> layers,
			final @JsonProperty("serviceLayers") List<ServiceLayer> serviceLayers,
			final @JsonProperty("state") Map <String,JsonNode> state ) {
		super (id);
		
		Assert.notNull (label, "label");
		Assert.notNull (layerType, "layerType");
		
		this.layerType = layerType;
		this.label = label;
		this.layers = layers == null ? Collections.<Layer>emptyList () : new ArrayList<> (layers);
		this.serviceLayers = serviceLayers == null ? Collections.<ServiceLayer>emptyList () : new ArrayList<> (serviceLayers);
		this.state = state == null ? Collections.<String, JsonNode>emptyMap() : new HashMap<String, JsonNode> (state); 
	}
	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode n = JsonFactory.mapper ().createObjectNode ();
		
		n.put ("id", getId ());
		n.put ("label", getLabel ());
		n.put ("layerType", getLayerType ());
		
		if (!getLayers ().isEmpty ()) {
			final ArrayNode layersNode = n.putArray ("layers");
			
			for (final Layer layer: getLayers ()) {
				layersNode.add (JsonFactory.mapper ().valueToTree (layer));
			}
		}
		
		if (!state.isEmpty()) {
			final ObjectNode stateNode = n.putObject("state");
			stateNode.putAll(state);
		}
		
		if (!getServiceLayers ().isEmpty ()) {
			final ArrayNode serviceLayersNode = n.putArray ("serviceLayers");
			
			for (final ServiceLayer serviceLayer: getServiceLayers ()) {
				serviceLayersNode.add (serviceLayer.getId ());
			}
		}
		System.out.println(n);
		return n;
	}

	public String getLayerType () {
		return layerType;
	}

	public String getLabel () {
		return label;
	}

	public List<Layer> getLayers () {
		return Collections.unmodifiableList (layers);
	}

	public List<ServiceLayer> getServiceLayers () {
		return Collections.unmodifiableList (serviceLayers);
	}
	
	public String getInitialStateValue (String stateProperty) {
		System.out.println("get InitialState Value " + stateProperty + " = " + state.get(stateProperty));
		if(state.get(stateProperty)!=null){
			return state.get(stateProperty).asText();
		} 
		return "";
	}
}
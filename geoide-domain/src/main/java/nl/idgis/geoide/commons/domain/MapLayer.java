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

public class MapLayer extends Entity {

	private static final long serialVersionUID = 5628518429669556235L;

	private final String layerType;
	private final List<MapLayer> layers;
	private final List<ServiceLayer> serviceLayers;
	private final Map<String, ExternalizableJsonNode> state;
	private final Map<String, ExternalizableJsonNode> properties;
	
	@JsonCreator
	public MapLayer (
			final @JsonProperty("id") String id,
			final @JsonProperty("layerType") String layerType,
			final @JsonProperty("label") String label,
			final @JsonProperty("layers") List<MapLayer> layers,
			final @JsonProperty("serviceLayers") List<ServiceLayer> serviceLayers,
			final @JsonProperty("state") Map <String,JsonNode> state,
			final @JsonProperty("properties") Map <String,JsonNode> properties) {
		super (id, label);
		
		Assert.notNull (layerType, "layerType");
		
		this.layerType = layerType;
		this.layers = layers == null ? Collections.<MapLayer>emptyList () : new ArrayList<> (layers);
		this.serviceLayers = serviceLayers == null ? Collections.<ServiceLayer>emptyList () : new ArrayList<> (serviceLayers);
		this.state = externalizeProperties (state); 
		this.properties = externalizeProperties (properties); 
	}
	
	private static Map<String, ExternalizableJsonNode> externalizeProperties (final Map<String, JsonNode> input) {
		if (input == null || input.isEmpty ()) {
			return Collections.emptyMap ();
		}
		
		final Map<String, ExternalizableJsonNode> result = new HashMap<> ();
		
		for (final Map.Entry<String, JsonNode> entry: input.entrySet ()) {
			if (entry.getValue () == null) {
				result.put (entry.getKey (), null);
			} else {
				result.put (entry.getKey (), JsonFactory.externalize (entry.getValue ()));
			}
		}

		return result;
	}
	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode n = JsonFactory.mapper ().createObjectNode ();
		
		n.put ("id", getId ());
		n.put ("label", getLabel ());
		n.put ("layerType", getLayerType ());
		
		if (!getLayers ().isEmpty ()) {
			final ArrayNode layersNode = n.putArray ("layers");
			
			for (final MapLayer layer: getLayers ()) {
				layersNode.add (JsonFactory.mapper ().valueToTree (layer));
			}
		}
		
		if (!state.isEmpty()) {
			final ObjectNode stateNode = n.putObject("state");
			
			for (final Map.Entry<String, ExternalizableJsonNode> entry: state.entrySet ()) {
				stateNode.put (entry.getKey (), entry.getValue ().getJsonNode ());
			}
		}
		
		if (!properties.isEmpty()) {
			final ObjectNode propertiesNode = n.putObject("properties");
			
			for (final Map.Entry<String, ExternalizableJsonNode> entry: properties.entrySet ()) {
				propertiesNode.put (entry.getKey (), entry.getValue ().getJsonNode ());
			}
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

	public List<MapLayer> getLayers () {
		return Collections.unmodifiableList (layers);
	}

	public List<ServiceLayer> getServiceLayers () {
		return Collections.unmodifiableList (serviceLayers);
	}
	
	public String getInitialStateValue (String stateProperty) {
		System.out.println("get InitialState Value " + stateProperty + " = " + state.get(stateProperty));
		if(state.get(stateProperty)!=null){
			return state.get(stateProperty).getJsonNode ().asText();
		} 
		return "";
	}
}

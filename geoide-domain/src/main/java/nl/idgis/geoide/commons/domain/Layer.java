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
	private final List<ServiceLayer> serviceLayers;
	private final LayerState state;
	private final Map<String, ExternalizableJsonNode> properties;
	
	@JsonCreator
	public Layer (
			final @JsonProperty("id") String id,
			final @JsonProperty("layerType") String layerType,
			final @JsonProperty("label") String label,
			final @JsonProperty("serviceLayers") List<ServiceLayer> serviceLayers,
			final @JsonProperty("state") Map <String,JsonNode> state,
			final @JsonProperty("properties") Map <String,JsonNode> properties) {
		super (id, label);
		
		Assert.notNull (layerType, "layerType");
		
		this.layerType = layerType;
		this.serviceLayers = serviceLayers == null ? Collections.<ServiceLayer>emptyList () : new ArrayList<> (serviceLayers);
		this.state = new LayerState (state); 
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
		
		
		n.put ("state", state.serialize());
		
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


	public List<ServiceLayer> getServiceLayers () {
		return Collections.unmodifiableList (serviceLayers);
	}
	
	public LayerState getLayerState () {
		return state;
	}
	
}

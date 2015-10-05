package nl.idgis.geoide.commons.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LayerState {
	private final Map<String, ExternalizableJsonNode> state;
	
	public LayerState (final Map <String,JsonNode> state) {
		this.state = externalizeProperties (state); 
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
		final ObjectNode stateNode = JsonFactory.mapper ().createObjectNode ();
		if (!state.isEmpty()) {
			for (final Map.Entry<String, ExternalizableJsonNode> entry: state.entrySet ()) {
				stateNode.put (entry.getKey (), entry.getValue ().getJsonNode ());
			}
		}
		return stateNode;
	}
	
	
	
	public String getStateValue (String stateProperty) {
		System.out.println("get State Value " + stateProperty + " = " + state.get(stateProperty));
		if(state.get(stateProperty)!=null){
			return state.get(stateProperty).getJsonNode ().asText();
		} 
		return "";
	}
	
	
}
package controllers.core;


import java.util.Iterator;
import java.util.Map.Entry;

import javax.inject.Inject;

import nl.idgis.geoide.commons.domain.api.MapProviderApi;
import nl.idgis.geoide.util.Promises;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MapConfiguration extends Controller {
	
	private final MapProviderApi mapProvider;
	
	@Inject
	public MapConfiguration (final MapProviderApi mapProvider) {
		this.mapProvider = mapProvider;
	}

	public Promise<Result> mapStructure (final String mapId) {
		return Promises.asPromise (mapProvider.getMapDefinition (mapId)).map ((mapDefinition) -> {
			if (mapDefinition == null) {
				return notFound ("map not found");
			}
			
			final JsonNode node = Json.toJson (mapDefinition);

			return ok (filterLayer (node));
		});
	}
	
	private static JsonNode filterLayer (final JsonNode map) {
		final ObjectNode result = Json.newObject ();
			
		final JsonNode layerNode = map.path ("layer");
		if (!layerNode.isMissingNode ()) {
			result.set ("id", layerNode.path ("id"));
			result.set ("label", layerNode.path ("label"));
		} else {
			result.set ("id", map.path ("id"));
			result.set ("label", map.path ("label"));
		}
		final JsonNode initialExtent = map.path ("initial-extent");
		if (!initialExtent.isMissingNode ()) {
			result.set ("initial-extent", map.path ("initial-extent"));
		}	
		
		final JsonNode layerState = layerNode.path("state");
			
		final JsonNode state = map.path ("state");
		
		if (!layerState.isMissingNode ()) {
			if (state.isMissingNode ()) {
				result.set ("state", layerState);
			} else {
				ObjectNode combinedState = state.deepCopy();
				Iterator<Entry<String, JsonNode>> it = layerState.fields();
				while (it.hasNext()) {
					Entry<String, JsonNode> st = it.next();
					if (state.path (st.getKey ()).isMissingNode ()) {
						combinedState.set (st.getKey (), st.getValue ());
					} 
				}
				result.set ("state", combinedState);
			}
			
		} else {
			if (!state.isMissingNode ()) {
				result.set ("state", map.path("state"));
			}
		}
			
		final JsonNode properties = map.path ("properties");
		if (!properties.isMissingNode ()) {
			result.set ("properties", map.path("properties"));
		}
		
		
		final JsonNode layers = map.path ("layers");
		if (!layers.isMissingNode ()) {
			filterLayers (layers, result.putArray ("layers"));
		}

		return result;
	}
	
	private static void filterLayers (final JsonNode layers, final ArrayNode result) {
		for (final JsonNode layer: layers) {
			result.add (filterLayer (layer));
		}
	}
	

}

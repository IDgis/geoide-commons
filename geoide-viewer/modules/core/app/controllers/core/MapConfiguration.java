package controllers.core;

import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.provider.MapProvider;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MapConfiguration extends Controller {
	
	private final MapProvider mapProvider;
	
	public MapConfiguration (final MapProvider mapProvider) {
		this.mapProvider = mapProvider;
	}

	public Result mapStructure (final String mapId) {
		final MapDefinition mapDefinition = mapProvider.getMapDefinition (mapId);
		if (mapDefinition == null) {
			return notFound ("map not found");
		}
		
		final JsonNode node = Json.toJson (mapDefinition);
		
		return ok (filterLayer (node));
	}
	
	private static JsonNode filterLayer (final JsonNode map) {
		final ObjectNode result = Json.newObject ();
		
		result.put ("id", map.path ("id"));
		result.put ("label", map.path ("label"));
		
		final JsonNode state = map.path ("state");
		if (!state.isMissingNode ()) {
			result.put ("state", map.path("state"));
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

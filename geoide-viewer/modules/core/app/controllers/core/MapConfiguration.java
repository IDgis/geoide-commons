package controllers.core;


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

		result.put ("id", map.path ("id"));
		result.put ("label", map.path ("label"));
		
		final JsonNode initialExtent = map.path ("initial-extent");
		if (!initialExtent.isMissingNode ()) {
			result.put ("initial-extent", map.path ("initial-extent"));
		}	
		
		final JsonNode state = map.path ("state");
		if (!state.isMissingNode ()) {
			result.put ("state", map.path("state"));
		}
			
		final JsonNode properties = map.path ("properties");
		if (!properties.isMissingNode ()) {
			result.put ("properties", map.path("properties"));
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

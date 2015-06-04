package controllers.mapview;

import javax.inject.Inject;

import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.api.MapView;
import nl.idgis.geoide.util.Promises;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class View extends Controller {
	private final MapView mapView;
	
	@Inject
	public View (final MapView mapView) {
		this.mapView = mapView;
	}
	
	public Promise<Result> buildView () {
		final JsonNode viewerState = JsonFactory.externalize (request ().body ().asJson ());
		// Flatten the layer list in a depth-first fashion:
		try {
			return Promises.asPromise (mapView.flattenLayerList (viewerState))
				.flatMap ((layers) -> Promises.asPromise (mapView.getServiceRequests (layers)).map ((serviceRequests) -> {
					// Build response:
					final ObjectNode result = Json.newObject ();
					result.put ("result", "ok");
					result.put ("serviceRequests", Json.toJson (serviceRequests));
					
					return (Result) ok (result);
				}));
		} catch (IllegalArgumentException e) {
			final ObjectNode result = Json.newObject ();
			result.put ("result", "failed");
			result.put ("message", e.getMessage ());
			return Promise.pure (badRequest (result));
		}
	}
}

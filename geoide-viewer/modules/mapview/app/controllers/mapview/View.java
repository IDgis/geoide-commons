package controllers.mapview;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.api.MapView;
import nl.idgis.geoide.util.Promises;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class View extends Controller {
	private final MapView mapView;
	
	@Inject
	public View (final MapView mapView) {
		this.mapView = mapView;
	}
	
	public Promise<Result> buildView () {
		final ExternalizableJsonNode viewerState = JsonFactory.externalize (request ().body ().asJson ());
		// Flatten the layer list in a depth-first fashion:
		String token = request().cookies().get("configToken").value();
		try {
			return Promises.asPromise (mapView.flattenLayerList (viewerState, token))
				.flatMap ((layers) -> Promises.asPromise (mapView.getServiceRequests (layers)).map ((serviceRequests) -> {
					// Build response:
					final ObjectNode result = Json.newObject ();
					result.put ("result", "ok");
					result.set ("serviceRequests", Json.toJson (serviceRequests));
					
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

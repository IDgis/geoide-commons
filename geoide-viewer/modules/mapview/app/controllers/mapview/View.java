package controllers.mapview;

import java.util.List;

import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.layer.LayerState;
import nl.idgis.geoide.map.MapView;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class View extends Controller {
	private final MapView mapView;
	

	public View (final MapView mapView) {
		this.mapView = mapView;
	}
	
	public Result buildView () {
		final JsonNode viewerState = request ().body ().asJson ();
		// Flatten the layer list in a depth-first fashion:
		final List<Traits<LayerState>> layers;
		try {
			layers = mapView.flattenLayerList (viewerState);
		} catch (IllegalArgumentException e) {
			final ObjectNode result = Json.newObject ();
			result.put ("result", "failed");
			result.put ("message", e.getMessage ());
			return badRequest (result);
		}
		
		// Merge the service layer list into a list of concrete requests for the client to execute.
		final List<ServiceRequest> serviceRequests = mapView.getServiceRequests (layers);
		
		// Build response:
		final ObjectNode result = Json.newObject ();
		result.put ("result", "ok");
		result.put ("serviceRequests", Json.toJson (serviceRequests));
		
		return ok (result);
	}
	
	

}

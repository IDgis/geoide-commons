package controllers.mapview;

import java.util.List;

import nl.idgis.geoide.commons.domain.ServiceRequest;
<<<<<<< Upstream, based on master
import nl.idgis.geoide.commons.domain.provider.LayerProvider;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceRequestContext;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import play.Logger;
=======
import nl.idgis.geoide.map.MapView;
import nl.idgis.geoide.map.MapView.LayerWithState;
>>>>>>> f934e84 work in progress reporting
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class View extends Controller {
	private final MapView mapView;
	
<<<<<<< Upstream, based on master
	private final LayerTypeRegistry layerTypeRegistry;
	private final ServiceTypeRegistry serviceTypeRegistry;
	private final LayerProvider layerProvider;
	
	public View (final LayerTypeRegistry layerTypeRegistry, final ServiceTypeRegistry serviceTypeRegistry, final LayerProvider layerProvider) {
		this.layerTypeRegistry = layerTypeRegistry;
		this.serviceTypeRegistry = serviceTypeRegistry;
		this.layerProvider = layerProvider;
=======
	public View (final MapView mapView) {
		this.mapView = mapView;
>>>>>>> f934e84 work in progress reporting
	}
	
	public Result buildView () {
		final JsonNode viewerState = request ().body ().asJson ();
		// Flatten the layer list in a depth-first fashion:
		final List<LayerWithState> layers;
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
	
	
<<<<<<< Upstream, based on master
	private List<ParameterizedServiceLayer<?>> createServiceLayerList (final List<LayerWithState> layers) {
		final List<ParameterizedServiceLayer<?>> serviceLayers = new ArrayList<> ();
		
		for (final LayerWithState l: layers) {
			final Layer layer = l.layer ();
			final JsonNode state = l.state ();
			final Traits<LayerType> layerType = layerTypeRegistry.getLayerType (layer);
			
			serviceLayers.addAll (layerType.get ().getServiceLayers (layer, state));
		}
		
		return serviceLayers;
	}

	private List<LayerWithState> flattenLayerList (final JsonNode viewerState) {
		final List<LayerWithState> layers = new ArrayList<> ();
		final JsonNode layersNode = viewerState.path ("layers");
		
		if (layersNode.isMissingNode ()) {
			return layers;
		}
		
		for (final JsonNode layerNode: layersNode) {
			// Add the layer:
			layers.add (new LayerWithState (getLayer (layerNode.path ("id")), layerNode.path ("state")));
			
			// Add all sub-layers of this layer:
			layers.addAll (flattenLayerList (layerNode));
		}
		
		return layers;
	}
	
	private Layer getLayer (final JsonNode id) {
		if (id == null) {
			throw new IllegalArgumentException ("Missing layer ID");
		}
		
		final Layer layer = layerProvider.getLayer (id.asText ());
		if (layer == null) {
			throw new IllegalArgumentException ("No layer found with ID " + id.asText ());
		}
		
		return layer;
	}
	
	public final static class LayerWithState {
		private final Layer layer;
		private final JsonNode state;
		
		public LayerWithState (final Layer layer, final JsonNode state) {
			this.layer = layer;
			this.state = state;
		}
		
		public Layer layer () {
			return this.layer;
		}
		
		public JsonNode state () {
			return this.state;
		}
	}
=======
>>>>>>> f934e84 work in progress reporting
}

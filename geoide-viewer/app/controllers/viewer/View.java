package controllers.viewer;

import java.util.ArrayList;
import java.util.List;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceRequestContext;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class View extends Controller {
	
	private final LayerTypeRegistry layerTypeRegistry;
	private final ServiceTypeRegistry serviceTypeRegistry;
	private final MapProvider mapProvider;
	
	public View (final LayerTypeRegistry layerTypeRegistry, final ServiceTypeRegistry serviceTypeRegistry, final MapProvider mapProvider) {
		this.layerTypeRegistry = layerTypeRegistry;
		this.serviceTypeRegistry = serviceTypeRegistry;
		this.mapProvider = mapProvider;
	}
	
	public Result buildView () {
		final JsonNode viewerState = request ().body ().asJson ();
		
		// Flatten the layer list in a depth-first fashion:
		final List<LayerWithState> layers;
		try {
			layers = flattenLayerList (viewerState);
		} catch (IllegalArgumentException e) {
			final ObjectNode result = Json.newObject ();
			result.put ("result", "failed");
			result.put ("message", e.getMessage ());
			return badRequest (result);
		}

		// Turn the list of layers in a list of service layers:
		final List<ParameterizedServiceLayer<?>> serviceLayers = createServiceLayerList (layers);

		// Merge the service layer list into a list of concrete requests for the client to execute.
		final List<ServiceRequest> serviceRequests = createServiceRequests (serviceLayers);
		
		// Build response:
		final ObjectNode result = Json.newObject ();
		result.put ("result", "ok");
		result.put ("serviceRequests", Json.toJson (serviceRequests));
		
		return ok (result);
	}
	
	private List<ServiceRequest> createServiceRequests (final List<ParameterizedServiceLayer<?>> serviceLayers) {
		final List<ServiceRequest> serviceRequests = new ArrayList<> ();
		final List<ParameterizedServiceLayer<?>> serviceLayerBatch = new ArrayList<> ();
		final ServiceRequestContext context = new ServiceRequestContext ();
		Service currentService = null;
		
		Logger.debug ("Creating service requests for " + serviceLayers.size () + " service layers");
		
		for (final ParameterizedServiceLayer<?> l: serviceLayers) {
			final Service service = l.getServiceLayer ().getService ();
			
			if (currentService != null && !service.equals (currentService) && !serviceLayerBatch.isEmpty ()) {
				final ServiceType serviceType = serviceTypeRegistry.getServiceType (currentService.getIdentification ().getServiceType ());
				
				if (!(serviceType instanceof LayerServiceType)) {
					throw new IllegalStateException ("Service type must be a LayerServiceType");
				}
				
				serviceRequests.addAll (((LayerServiceType) serviceType).getServiceRequests (currentService, serviceLayerBatch, context));
				serviceLayerBatch.clear ();
			}
			
			currentService = service;
			serviceLayerBatch.add (l);
		}
		
		if (currentService != null && !serviceLayerBatch.isEmpty ()) {
			final ServiceType serviceType = serviceTypeRegistry.getServiceType (currentService.getIdentification ().getServiceType ());
			
			if (!(serviceType instanceof LayerServiceType)) {
				throw new IllegalStateException ("Service type must be a LayerServiceType");
			}
			
			serviceRequests.addAll (((LayerServiceType) serviceType).getServiceRequests (currentService, serviceLayerBatch, context));
		}

		return serviceRequests;
	}
	
	private List<ParameterizedServiceLayer<?>> createServiceLayerList (final List<LayerWithState> layers) {
		final List<ParameterizedServiceLayer<?>> serviceLayers = new ArrayList<> ();
		
		for (final LayerWithState l: layers) {
			final Layer layer = l.layer ();
			final JsonNode state = l.state ();
			final LayerType layerType = layerTypeRegistry.getLayerType (layer);
			
			serviceLayers.addAll (layerType.getServiceLayers (layer, state));
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
		
		final Layer layer = mapProvider.getLayer (id.asText ());
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
}

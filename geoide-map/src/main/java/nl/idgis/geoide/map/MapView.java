package nl.idgis.geoide.map;

import java.util.ArrayList;
import java.util.List;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceRequestContext;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;


import com.fasterxml.jackson.databind.JsonNode;



public class MapView {
	private final LayerTypeRegistry layerTypeRegistry;
	private final ServiceTypeRegistry serviceTypeRegistry;
	private final MapProvider mapProvider;
	
	public MapView (final LayerTypeRegistry layerTypeRegistry, final ServiceTypeRegistry serviceTypeRegistry, final MapProvider mapProvider) {
		this.layerTypeRegistry = layerTypeRegistry;
		this.serviceTypeRegistry = serviceTypeRegistry;
		this.mapProvider = mapProvider;
	}

	public List<ServiceRequest> getServiceRequests (List<LayerWithState> layers) {
		
		// Turn the list of layers in a list of service layers:
		final List<ParameterizedServiceLayer<?>> serviceLayers = createServiceLayerList (layers);

		// Merge the service layer list into a list of concrete requests for the client to execute.
		return createServiceRequests (serviceLayers);
	}
	
	
	private List<ServiceRequest> createServiceRequests (final List<ParameterizedServiceLayer<?>> serviceLayers) {
		final List<ServiceRequest> serviceRequests = new ArrayList<> ();
		final List<ParameterizedServiceLayer<?>> serviceLayerBatch = new ArrayList<> ();
		final ServiceRequestContext context = new ServiceRequestContext ();
		Service currentService = null;
	
	
		for (final ParameterizedServiceLayer<?> l: serviceLayers) {
			final Service service = l.getServiceLayer ().getService ();
			
			if (currentService != null && !service.equals (currentService) && !serviceLayerBatch.isEmpty ()) {
				final Traits<ServiceType> serviceType = serviceTypeRegistry.getServiceType (currentService.getIdentification ().getServiceType ());
				
				if (!(serviceType.get () instanceof LayerServiceType)) {
					throw new IllegalStateException ("Service type must be a LayerServiceType");
				}
				
				serviceRequests.addAll (((LayerServiceType) serviceType.get ()).getServiceRequests (currentService, serviceLayerBatch, context));
				serviceLayerBatch.clear ();
			}
			
			currentService = service;
			serviceLayerBatch.add (l);
		}
	
		if (currentService != null && !serviceLayerBatch.isEmpty ()) {
			final Traits<ServiceType> serviceType = serviceTypeRegistry.getServiceType (currentService.getIdentification ().getServiceType ());
			
			if (!(serviceType.get () instanceof LayerServiceType)) {
				throw new IllegalStateException ("Service type must be a LayerServiceType");
			}
			
			serviceRequests.addAll (((LayerServiceType) serviceType.get ()).getServiceRequests (currentService, serviceLayerBatch, context));
		}
	
		return serviceRequests;
	}
	
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
	
	public List<LayerWithState> flattenLayerList (final JsonNode viewerState) {
		final List<LayerWithState> layers = new ArrayList<> ();
		final JsonNode layersNode = viewerState.path ("layers");
		
		System.out.println(viewerState.toString());
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

	public ServiceType getServiceType(Service currentService) {	
		final ServiceType serviceType = serviceTypeRegistry.getServiceType (currentService.getIdentification ().getServiceType ()).get();
		return serviceType;
	}
	
}

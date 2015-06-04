package nl.idgis.geoide.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.ParameterizedServiceLayer;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.api.MapView;
import nl.idgis.geoide.commons.domain.layer.LayerState;
import nl.idgis.geoide.commons.domain.provider.LayerProvider;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;
import nl.idgis.geoide.service.LayerServiceType;
import nl.idgis.geoide.service.ServiceRequestContext;
import nl.idgis.geoide.service.ServiceType;
import nl.idgis.geoide.service.ServiceTypeRegistry;

import com.fasterxml.jackson.databind.JsonNode;

public class DefaultMapView implements MapView {
	private final LayerTypeRegistry layerTypeRegistry;
	private final ServiceTypeRegistry serviceTypeRegistry;
	private final LayerProvider layerProvider;
	
	public DefaultMapView (final LayerTypeRegistry layerTypeRegistry, final ServiceTypeRegistry serviceTypeRegistry, final LayerProvider layerProvider) {
		this.layerTypeRegistry = layerTypeRegistry;
		this.serviceTypeRegistry = serviceTypeRegistry;
		this.layerProvider = layerProvider;
	}

	/* (non-Javadoc)
	 * @see nl.idgis.geoide.map.MapView#getServiceRequests(java.util.List)
	 */
	@Override
	public CompletableFuture<List<ServiceRequest>> getServiceRequests (final List<Traits<LayerState>> layerStates) {
		
		// Turn the list of layers in a list of service layers:
		final List<ParameterizedServiceLayer<?>> serviceLayers = createServiceLayerList (layerStates);

		// Merge the service layer list into a list of concrete requests for the client to execute.
		return CompletableFuture.completedFuture (createServiceRequests (serviceLayers));
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
	
	private List<ParameterizedServiceLayer<?>> createServiceLayerList (final List<Traits<LayerState>> layerStates) {
		final List<ParameterizedServiceLayer<?>> serviceLayers = new ArrayList<> ();
	
		for (final Traits<LayerState> layerState: layerStates) {
			final Layer layer = layerState.get ().getLayer ();
			final Traits<LayerType> layerType = layerTypeRegistry.getLayerType (layer);
			
			serviceLayers.addAll (layerType.get ().getServiceLayers (layerState));
		}
		
		return serviceLayers;
	}
	
	/* (non-Javadoc)
	 * @see nl.idgis.geoide.map.MapView#flattenLayerList(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public CompletableFuture<List<Traits<LayerState>>> flattenLayerList (final JsonNode viewerState) {
		return CompletableFuture.completedFuture (flattenLayerList (viewerState, Collections.emptyList ()));
	}
	
	private List<Traits<LayerState>> flattenLayerList (final JsonNode viewerState, final List<Traits<LayerState>> parents) {
		final List<Traits<LayerState>> layers = new ArrayList<> ();
		final JsonNode layersNode = viewerState.path ("layers");
		
		if (layersNode.isMissingNode ()) {
			return layers;
		}

		for (final JsonNode layerNode: layersNode) {
			final Layer layer = getLayer (layerNode.path ("id"));
			final Traits<LayerType> layerType = layerTypeRegistry.getLayerType (layer);
			
			if (layerType == null) {
				throw new IllegalArgumentException ("Unable to find layer type for " + layer.getLayerType ());
			}

			final Traits<LayerState> layerState = layerType.get ().createLayerState (layer, layerNode.path ("state"), parents);

			// Add the layer:
			layers.add (layerState);
			
			// Add all sub-layers of this layer:
			layers.addAll (flattenLayerList (
				layerNode, 
				Stream.concat (
					parents.stream (), 
					Stream.of (layerState))
						.collect (Collectors.toList ())));
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

	public ServiceType getServiceType(Service currentService) {	
		final ServiceType serviceType = serviceTypeRegistry.getServiceType (currentService.getIdentification ().getServiceType ()).get();
		return serviceType;
	}
	
}

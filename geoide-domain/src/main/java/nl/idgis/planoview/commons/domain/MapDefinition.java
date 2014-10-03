package nl.idgis.planoview.commons.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MapDefinition extends Entity {

	private static final long serialVersionUID = -5946086390948660716L;
	
	private final List<Layer> rootLayers;
	private final String label;
	
	private final Map<String, Service> services = new HashMap<> ();
	private final Map<String, ServiceLayer> serviceLayers = new HashMap<> ();
	private final Map<String, FeatureType> featureTypes = new HashMap<> ();
	private final Map<String, Layer> layers = new HashMap<> ();
	
	public MapDefinition (final String id, final String label, final List<Layer> rootLayers) {
		super (id);
		
		this.label = label;
		this.rootLayers = rootLayers == null ? Collections.<Layer>emptyList () : new ArrayList<> (rootLayers);
		
		// Scan the layers and fill the indices:
		scanLayers (this.rootLayers);
	}

	@JsonCreator
	public static MapDefinition parse (final JsonNode node) {
		final JsonNode id = node.path ("id"); 
		final JsonNode label = node.path ("label");
		final JsonNode services = node.path ("services");
		final JsonNode serviceLayers = node.path ("serviceLayers");
		final JsonNode featureTypes = node.path ("featureTypes");
		final JsonNode layers = node.path ("layers");
		
		if (id.isMissingNode () || id.asText ().isEmpty ()) {
			throw new IllegalArgumentException ("Missing property: id");
		}
		if (label.isMissingNode ()) {
			throw new IllegalArgumentException ("Missing property: label");
		}
		
		// Parse services:
		final Map<String, Service> serviceMap = new HashMap<> ();
		for (final JsonNode serviceNode: services) {
			final Service service = JsonFactory.service (serviceNode);
			
			serviceMap.put (service.getId (), service);
		}
		
		// Parse feature types:
		final Map<String, FeatureType> featureTypeMap = new HashMap<> ();
		for (final JsonNode featureTypeNode: featureTypes) {
			final FeatureType featureType = JsonFactory.featureType (featureTypeNode, serviceMap);
			
			featureTypeMap.put (featureType.getId (), featureType);
		}
		
		// Parse service layers:
		final Map<String, ServiceLayer> serviceLayerMap = new HashMap<> ();
		for (final JsonNode serviceLayerNode: serviceLayers) {
			final ServiceLayer serviceLayer = JsonFactory.serviceLayer (serviceLayerNode, serviceMap, featureTypeMap);
			
			serviceLayerMap.put (serviceLayer.getId (), serviceLayer);
		}
		
		// Parse layers:
		final List<Layer> layerList = new ArrayList<> ();
		for (final JsonNode layerNode: layers) {
			final Layer layer = JsonFactory.layer (layerNode, serviceLayerMap);
			
			layerList.add (layer);
		}
		
		return new MapDefinition (id.asText (), label.asText (), layerList);
	}
	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode obj = JsonFactory.mapper ().createObjectNode ();
		
		obj.put ("id", getId ());
		obj.put ("label", getLabel ());
		
		// Write services:
		if (!getServices ().values ().isEmpty ()) {
			final ArrayNode servicesNode = obj.putArray ("services");
			
			for (final Service service: getServices ().values ()) {
				servicesNode.add (JsonFactory.mapper ().valueToTree (service));
			}
		}
		
		// Write service layers:
		if (!getServiceLayers ().values ().isEmpty ()) {
			final ArrayNode serviceLayersNode = obj.putArray ("serviceLayers");
			
			for (final ServiceLayer serviceLayer: getServiceLayers ().values ()) {
				serviceLayersNode.add (JsonFactory.mapper ().valueToTree (serviceLayer));
			}
		}
		
		// Write feature types:
		if (!getFeatureTypes ().values ().isEmpty ()) {
			final ArrayNode featureTypesNode = obj.putArray ("featureTypes");
			
			for (final FeatureType featureType: getFeatureTypes ().values ()) {
				featureTypesNode.add (JsonFactory.mapper ().valueToTree (featureType));
			}
		}
		
		// Write layers:
		if (!getRootLayers ().isEmpty ()) {
			final ArrayNode layersNode = obj.putArray ("layers");
			
			for (final Layer layer: getRootLayers ()) {
				layersNode.add (JsonFactory.mapper ().valueToTree (layer));
			}
		}

		return obj;
	}
	
	public List<Layer> getRootLayers () {
		return Collections.unmodifiableList (rootLayers);
	}

	public String getLabel () {
		return label;
	}

	public Map<String, Service> getServices () {
		return Collections.unmodifiableMap (services);
	}

	public Map<String, ServiceLayer> getServiceLayers() {
		return Collections.unmodifiableMap (serviceLayers);
	}

	public Map<String, FeatureType> getFeatureTypes () {
		return Collections.unmodifiableMap (featureTypes);
	}
	
	public Map<String, Layer> getLayers () {
		return Collections.unmodifiableMap (layers);
	}

	private void scanLayers (final Collection<Layer> layers) {
		final LinkedList<Layer> fringe = new LinkedList<> (layers);
		
		while (!fringe.isEmpty ()) {
			final Layer layer = fringe.poll ();
			
			this.layers.put (layer.getId (), layer);
			
			for (final ServiceLayer serviceLayer: layer.getServiceLayers ()) {
				this.serviceLayers.put (serviceLayer.getId (), serviceLayer);
				this.services.put (serviceLayer.getService ().getId (), serviceLayer.getService ());
				
				if (serviceLayer.getFeatureType () != null) {
					this.featureTypes.put (serviceLayer.getFeatureType ().getId (), serviceLayer.getFeatureType ());
					this.services.put (serviceLayer.getFeatureType ().getService ().getId (), serviceLayer.getFeatureType ().getService ());
				}
			}
			
			fringe.addAll (layer.getLayers ());
		}
	}
}

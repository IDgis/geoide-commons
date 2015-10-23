package nl.idgis.geoide.commons.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MapDefinition extends Entity {
	private static final long serialVersionUID = -5946086390948660716L;
	private final List<LayerRef> rootLayers;
	private final String initialExtent;
	
	private final Map<String, Service> services = new HashMap<> ();
	private final Map<String, ServiceLayer> serviceLayers = new HashMap<> ();
	private final Map<String, FeatureType> featureTypes = new HashMap<> ();
	private final Map<String, LayerRef> layerRefs = new HashMap<> ();
	private final List<QueryDescription> queryDescriptions;

	public MapDefinition (
			final String id, 
			final String label, 
			final String initialExtent, 
			final List<LayerRef> rootLayers,
			final List<QueryDescription> queryDescriptions) {
		super (id, label);
		this.initialExtent = initialExtent;
		this.rootLayers = rootLayers == null ? Collections.<LayerRef>emptyList () : new ArrayList<> (rootLayers);
		this.queryDescriptions = queryDescriptions;
		// Scan the layers and fill the indices:
		scanLayerRefs (this.rootLayers);
	}
	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode obj = JsonFactory.mapper ().createObjectNode ();
		obj.put ("id", getId ());
		obj.put ("label", getLabel ());
		obj.put ("initial-extent", getInitialExtent());
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
			final ArrayNode layerRefsNode = obj.putArray ("layerRefs");
			for (final LayerRef layerRef: getRootLayers ()) {
				layerRefsNode.add (JsonFactory.mapper ().valueToTree (layerRef));
			}
		}
		return obj;
	}
	
	public List<LayerRef> getRootLayers () {
		return Collections.unmodifiableList (rootLayers);
	}
	
	public String getInitialExtent () {
		return initialExtent;
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
	
	public Map<String, LayerRef> getLayerRefs () {
		return Collections.unmodifiableMap (layerRefs);
	}
	
	public List<QueryDescription> getQueryDescriptions() {
		return queryDescriptions;
	}
	
	private void scanLayerRefs (final Collection<LayerRef> layerRefs) {
		final LinkedList<LayerRef> fringe = new LinkedList<> (layerRefs);
		
		while (!fringe.isEmpty ()) {
			final LayerRef layerRef = fringe.poll ();
			this.layerRefs.put (layerRef.getLayer().getId (), layerRef);
		
			for (final ServiceLayer serviceLayer: layerRef.getLayer().getServiceLayers ()) {
				this.serviceLayers.put (serviceLayer.getId (), serviceLayer);
				this.services.put (serviceLayer.getService ().getId (), serviceLayer.getService ());
				if (serviceLayer.getFeatureType () != null) {
					this.featureTypes.put (serviceLayer.getFeatureType ().getId (), serviceLayer.getFeatureType ());
					this.services.put (serviceLayer.getFeatureType ().getService ().getId (), serviceLayer.getFeatureType ().getService ());
				}
			}
			
			fringe.addAll (layerRef.getLayerRefs ());
		}
	}


}
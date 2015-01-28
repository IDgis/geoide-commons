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
	private final List<Layer> rootLayers;
	private final String label;
	private final String initialExtent;
	private final Map<String, Service> services = new HashMap<> ();
	private final Map<String, ServiceLayer> serviceLayers = new HashMap<> ();
	private final Map<String, FeatureType> featureTypes = new HashMap<> ();
	private final Map<String, Layer> layers = new HashMap<> ();

	public MapDefinition (final String id, final String label, final String initialExtent, final List<Layer> rootLayers) {
		super (id);
		this.label = label;
		this.initialExtent = initialExtent;
		this.rootLayers = rootLayers == null ? Collections.<Layer>emptyList () : new ArrayList<> (rootLayers);
		// Scan the layers and fill the indices:
		scanLayers (this.rootLayers);
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

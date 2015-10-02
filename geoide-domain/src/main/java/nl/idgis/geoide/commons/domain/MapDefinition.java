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
	private final List<MapLayer> rootLayers;
	private final String prefix;
	private final String initialExtent;
	private final Map<String, Service> services = new HashMap<> ();
	private final Map<String, ServiceLayer> serviceLayers = new HashMap<> ();
	private final Map<String, FeatureType> featureTypes = new HashMap<> ();
	private final Map<String, MapLayer> layers = new HashMap<> ();

	public MapDefinition (final String id, final String label, final String prefix, final String initialExtent, final List<MapLayer> rootLayers) {
		super (id, label);
		this.prefix = prefix;
		this.initialExtent = initialExtent;
		this.rootLayers = rootLayers == null ? Collections.<MapLayer>emptyList () : new ArrayList<> (rootLayers);
		// Scan the layers and fill the indices:
		scanLayers (this.rootLayers);
	}
	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode obj = JsonFactory.mapper ().createObjectNode ();
		obj.put ("id", getId ());
		obj.put ("label", getLabel ());
		obj.put ("prefix", getPrefix ());
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
			for (final MapLayer layer: getRootLayers ()) {
				layersNode.add (JsonFactory.mapper ().valueToTree (layer));
			}
		}
		return obj;
	}
	
	public List<MapLayer> getRootLayers () {
		return Collections.unmodifiableList (rootLayers);
	}
	
	public String getPrefix () {
		return prefix;
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
	
	public Map<String, MapLayer> getLayers () {
		return Collections.unmodifiableMap (layers);
	}
	
	private void scanLayers (final Collection<MapLayer> layers) {
		final LinkedList<MapLayer> fringe = new LinkedList<> (layers);
		
		while (!fringe.isEmpty ()) {
			final MapLayer layer = fringe.poll ();
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

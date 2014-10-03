package nl.idgis.planoview.commons.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.idgis.planoview.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Layer extends Entity {

	private static final long serialVersionUID = 5628518429669556235L;

	private final String layerType;
	private final String label;
	private final List<Layer> layers;
	private final List<ServiceLayer> serviceLayers;
	
	@JsonCreator
	public Layer (
			final @JsonProperty("id") String id,
			final @JsonProperty("layerType") String layerType,
			final @JsonProperty("label") String label,
			final @JsonProperty("layers") List<Layer> layers,
			final @JsonProperty("serviceLayers") List<ServiceLayer> serviceLayers) {
		super (id);
		
		Assert.notNull (label, "label");
		Assert.notNull (layerType, "layerType");
		
		this.layerType = layerType;
		this.label = label;
		this.layers = layers == null ? Collections.<Layer>emptyList () : new ArrayList<> (layers);
		this.serviceLayers = serviceLayers == null ? Collections.<ServiceLayer>emptyList () : new ArrayList<> (serviceLayers);
	}
	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode n = JsonFactory.mapper ().createObjectNode ();
		
		n.put ("id", getId ());
		n.put ("label", getLabel ());
		n.put ("layerType", getLayerType ());
		
		if (!getLayers ().isEmpty ()) {
			final ArrayNode layersNode = n.putArray ("layers");
			
			for (final Layer layer: getLayers ()) {
				layersNode.add (JsonFactory.mapper ().valueToTree (layer));
			}
		}
		
		if (!getServiceLayers ().isEmpty ()) {
			final ArrayNode serviceLayersNode = n.putArray ("serviceLayers");
			
			for (final ServiceLayer serviceLayer: getServiceLayers ()) {
				serviceLayersNode.add (serviceLayer.getId ());
			}
		}
		
		return n;
	}

	public String getLayerType () {
		return layerType;
	}

	public String getLabel () {
		return label;
	}

	public List<Layer> getLayers () {
		return Collections.unmodifiableList (layers);
	}

	public List<ServiceLayer> getServiceLayers () {
		return Collections.unmodifiableList (serviceLayers);
	}
}

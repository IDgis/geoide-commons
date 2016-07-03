package nl.idgis.geoide.commons.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.idgis.geoide.util.Assert;

public class SearchTemplate extends Entity {
	// Eerst alleen één queryTerm -> featuretype en attribuut info in deze klasse
	private static final long serialVersionUID = -6404778430711796916L;
	
	//private final List<QueryTerm> queryTerms;
	private final QName attribute;
	private final FeatureType valueFeatureType;
	private final ServiceLayer layer;

	public SearchTemplate(
		final @JsonProperty("id") String id,
		final @JsonProperty("label") String label,
		//final @JsonProperty("queryTerms") List<QueryTerm> queryTerms,
		final @JsonProperty("featuretype") FeatureType featureType,
		final @JsonProperty ("attribute") QName attribute,
		final @JsonProperty("serviceLayer") ServiceLayer serviceLayer) {
		
		super(id, label);
		
		Assert.notNull (featureType, "featureType");
		Assert.notNull (attribute, "attribute");
		
		this.attribute = attribute;
		this.valueFeatureType = featureType;
		//serviceLayer mag null zijn (alleen zoomen naar bbox uit ft en geen highlighting)
		//Assert.notNull (serviceLayer, "serviceLayer");
		
		//this.queryTerms = new ArrayList<> (queryTerms);
		this.layer = serviceLayer;
		
	}

	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode n = JsonFactory.mapper ().createObjectNode ();
		
		n.put ("id", getId ());
		n.put ("label", getLabel ());
		
		final ObjectNode a = JsonFactory.mapper ().createObjectNode ();
		a.put("localname", getAttribute().getLocalName());
		a.put("namespace", getAttribute().getNamespace());
		
		n.put ("attribute", a);
		
		n.put ("featuretype", getFeatureType().serialize());
		
	
		n.put("layer", getLayer().serialize());

		return n;
	}
	
	public QName getAttribute () {
		return attribute;
	}
	
	public ServiceLayer getLayer () {
		return layer;
	}
	
	public FeatureType getFeatureType () {
		return valueFeatureType;
	}
	
	


}

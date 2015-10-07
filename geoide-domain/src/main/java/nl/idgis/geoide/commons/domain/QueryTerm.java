package nl.idgis.geoide.commons.domain;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;

import nl.idgis.geoide.util.Assert;

public class QueryTerm implements Serializable {

	private static final long serialVersionUID = -3093182701753119037L;
	
	private final QName attribute;
	private final String label;
	private final FeatureType valueFeatureType;
	
	@JsonCreator
	public QueryTerm (
			final @JsonProperty ("attribute") QName attribute,
			final @JsonProperty ("label") String label,
			final @JsonProperty ("featuretype") FeatureType featureType){
		
		Assert.notNull (featureType, "featuretype");
		Assert.notNull (attribute, "attribute");
		
		this.attribute = attribute;
		this.valueFeatureType = featureType;

		this.label = label == null ? this.valueFeatureType.getName().getLocalName() + ":" + this.attribute.getLocalName() : label;
		
	}
	
	@JsonValue
	public JsonNode serialize () {
		//TODO
		return null;
	}
	

	public String getLabel () {
		return label;
	}
	
	public QName getAtttribute () {
		return attribute;
	}
	
	public FeatureType getFeatureType () {
		return valueFeatureType;
	}
	
	

}

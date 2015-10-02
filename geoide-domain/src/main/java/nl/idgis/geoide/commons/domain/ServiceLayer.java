package nl.idgis.geoide.commons.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.idgis.geoide.util.Assert;

public final class ServiceLayer extends Entity {

	private static final long serialVersionUID = -1751576147573777244L;

	private final Service service;
	private final QName name;
	private final FeatureType featureType;

	
	@JsonCreator
	public ServiceLayer (
			final @JsonProperty ("id") String id, 
			final @JsonProperty ("service") Service service, 
			final @JsonProperty ("name") QName name,
			final @JsonProperty ("label") String label,
			final @JsonProperty ("featureType") FeatureType featureType ) {
		super (id, label);
		
		Assert.notNull (service, "service");
		Assert.notNull (name, "name");
		
		this.service = service;
		this.name = name;	
		
		this.featureType = featureType;
	}
	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode n = JsonFactory.mapper ().createObjectNode ();
		
		n.put ("id", getId ());
		n.put ("service", service.getId ());
		n.put ("name", JsonFactory.mapper ().valueToTree (name));
		n.put ("label", getLabel ());
		
		if (getFeatureType () != null) {
			n.put ("featureType", getFeatureType ().getId ());
		}
		
		return n;
	}

	public Service getService () {
		return service;
	}

	public QName getName () {
		return name;
	}
	
	public FeatureType getFeatureType () {
		return featureType;
	}
}

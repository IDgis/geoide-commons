package nl.idgis.planoview.commons.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ServiceLayer extends NamedServiceEntity {

	private static final long serialVersionUID = -1751576147573777244L;

	private final FeatureType featureType;
	
	@JsonCreator
	public ServiceLayer (
			final @JsonProperty ("id") String id, 
			final @JsonProperty ("service") Service service, 
			final @JsonProperty ("name") QName name,
			final @JsonProperty ("label") String label,
			final @JsonProperty ("featureType") FeatureType featureType ) {
		super (id, service, name, label);
		
		this.featureType = featureType;
	}
	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode n = JsonFactory.mapper ().createObjectNode ();
		
		n.put ("id", getId ());
		n.put ("service", getService ().getId ());
		n.put ("name", JsonFactory.mapper ().valueToTree (getName ()));
		n.put ("label", getLabel ());
		
		if (getFeatureType () != null) {
			n.put ("featureType", getFeatureType ().getId ());
		}
		
		return n;
	}

	public FeatureType getFeatureType () {
		return featureType;
	}
}

package nl.idgis.geoide.commons.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class FeatureType extends NamedServiceEntity {

	private static final long serialVersionUID = 6708815788023537853L;

	@JsonCreator
	public FeatureType (
			final @JsonProperty ("id") String id, 
			final @JsonProperty ("service") Service service, 
			final @JsonProperty ("name") QName name,
			final @JsonProperty ("label") String label) {
		super (id, service, name, label);
	}
	
	@JsonValue
	public JsonNode serialize () {
		final ObjectNode n = JsonFactory.mapper ().createObjectNode ();
		
		n.put ("id", getId ());
		n.put ("service", getService ().getId ());
		n.put ("name", JsonFactory.mapper ().valueToTree (getName ()));
		n.put ("label", getLabel ());
		
		return n;
	}
}

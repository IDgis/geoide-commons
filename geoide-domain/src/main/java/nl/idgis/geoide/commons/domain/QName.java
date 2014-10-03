package nl.idgis.geoide.commons.domain;

import java.io.Serializable;

import nl.idgis.geoide.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class QName implements Serializable {

	private static final long serialVersionUID = -6341815667051684849L;
	
	private final String localName;
	private final String namespace;
	
	@JsonCreator
	public QName (final @JsonProperty("localName") String localName, final @JsonProperty(value = "namespace", required = false) String namespace) {
		Assert.notNull (localName, "localName");
		
		this.localName = localName;
		this.namespace = namespace;
	}
	
	@JsonValue
	public JsonNode serialize () {
		if (namespace == null) {
			return JsonFactory.mapper().valueToTree (localName);
		} else {
			final ObjectNode n = JsonFactory.mapper ().createObjectNode ();
			
			n.put ("localName", localName);
			n.put ("namespace", namespace);
			
			return n;
		}
	}
	
	public QName (final String localName) {
		this (localName, null);
	}

	public String getLocalName() {
		return localName;
	}

	public String getNamespace() {
		return namespace;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((localName == null) ? 0 : localName.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QName other = (QName) obj;
		if (localName == null) {
			if (other.localName != null)
				return false;
		} else if (!localName.equals(other.localName))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		return true;
	}
}

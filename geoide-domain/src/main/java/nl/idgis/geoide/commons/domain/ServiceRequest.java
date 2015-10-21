package nl.idgis.geoide.commons.domain;

import nl.idgis.geoide.util.Assert;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ServiceRequest implements Serializable {
	
	private static final long serialVersionUID = 2821223114900949315L;
	
	private final String id;
	private final Service service;
	private final Object parameters;
	
	public ServiceRequest (final String id, final Service service, final Object parameters) {
	
		Assert.notNull (id, "id");
		Assert.notNull (service, "service");
		
		this.id = id;
		this.service = service;
		this.parameters = parameters;
	}

	@JsonValue
	public JsonNode serialize () {
		final ObjectNode obj = JsonFactory.mapper ().createObjectNode ();

		obj.put ("id", id);
		obj.put ("serviceId", JsonFactory.asJson (getService ().getId ()));
		obj.put ("serviceIdentification", JsonFactory.asJson (getService ().getIdentification ()));
		if (getParameters () != null) {
			obj.put ("parameters", JsonFactory.asJson (getParameters ()));
		}
		
		return obj;
	}
	
	public Service getService () {
		return service;
	}
	
	public Object getParameters () {
		return parameters;
	}
	
	public String getId () {
		return id;
	}
}

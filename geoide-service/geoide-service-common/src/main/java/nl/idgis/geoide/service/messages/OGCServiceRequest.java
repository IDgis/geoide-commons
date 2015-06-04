package nl.idgis.geoide.service.messages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.service.messages.ServiceMessage;
import nl.idgis.geoide.commons.domain.service.messages.ServiceMessageContext;

public final class OGCServiceRequest extends ServiceMessage {
	private static final long serialVersionUID = 7495192196103441861L;
	
	private final String request;
	private final Map<String, String[]> parameters;
	
	public OGCServiceRequest (final ServiceIdentification identification, final String request, final Map<String, String[]> parameters) {
		this (identification, request, parameters, null);
	}
	
	public OGCServiceRequest (final ServiceIdentification identification, final String request, final Map<String, String[]> parameters, final ServiceMessageContext context) {
		super (identification, context);
		
		if (request == null) {
			throw new NullPointerException ("request cannot be null");
		}
		
		this.request = request;
		this.parameters = parameters == null ? Collections.<String, String[]>emptyMap () : new HashMap<String, String[]> (parameters);
	}
	
	public String request () {
		return request;
	}
	
	public Map<String, String[]> parameters () {
		return Collections.unmodifiableMap (parameters);
	}
}
package nl.idgis.geoide.service.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.idgis.geoide.commons.domain.service.messages.ServiceError;

public final class LogResponse implements Serializable {
	
	private static final long serialVersionUID = 2790461259414378477L;
	
	private final List<ServiceError> errors;
	private final int start;
	private final int totalCount;
	
	public LogResponse (final List<ServiceError> errors, final int start, final int totalCount) {
		if (errors == null) {
			throw new NullPointerException ("errors cannot be null");
		}
		
		this.errors = new ArrayList<> (errors);
		this.start = start;
		this.totalCount = totalCount;
	}
	
	public List<ServiceError> errors () {
		return Collections.unmodifiableList (errors);
	}
	
	public int start () {
		return start;
	}
	
	public int totalCount () {
		return totalCount;
	}
}
package nl.idgis.services;

import java.io.Serializable;
import java.util.Collection;

import nl.idgis.geoide.commons.domain.service.Capabilities;

public abstract class OGCCapabilities extends Capabilities {
	private static final long serialVersionUID = -3092853516309523737L;

	public abstract Collection<? extends Operation> operations ();
	public abstract Operation operation (OperationType operationType);
	public abstract boolean hasOperation (OperationType operationType);
	public abstract Operation operationByName (String operationName);
	
	public static interface OperationType extends Serializable {
		String operationName ();
	}
	
	public static interface Operation extends Serializable {
		OperationType operationType ();
		String httpGet ();
		String httpPost ();
	}
}

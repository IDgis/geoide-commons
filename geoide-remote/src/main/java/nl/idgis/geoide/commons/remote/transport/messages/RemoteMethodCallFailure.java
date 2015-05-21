package nl.idgis.geoide.commons.remote.transport.messages;

import java.io.Serializable;

public class RemoteMethodCallFailure implements Serializable {
	
	private static final long serialVersionUID = 3421107312703598085L;
	
	private final Throwable cause;
	
	public RemoteMethodCallFailure (final Throwable cause) {
		if (cause == null) {
			throw new NullPointerException ("cause cannot be null");
		}
		
		this.cause = cause;
	}

	public Throwable getCause () {
		return cause;
	}
}

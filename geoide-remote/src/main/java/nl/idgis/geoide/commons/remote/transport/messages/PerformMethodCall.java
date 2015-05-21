package nl.idgis.geoide.commons.remote.transport.messages;

import java.io.Serializable;

import nl.idgis.geoide.commons.remote.RemoteMethodCall;

public class PerformMethodCall implements Serializable {
	
	private static final long serialVersionUID = -1940159053829642582L;
	
	private final String serverName;
	private final RemoteMethodCall methodCall;
	
	public PerformMethodCall (final String serverName, final RemoteMethodCall methodCall) {
		if (serverName == null) {
			throw new NullPointerException ("serverName cannot be null");
		}
		if (methodCall == null) {
			throw new NullPointerException ("methodCall cannot be null");
		}
		
		this.serverName = serverName;
		this.methodCall = methodCall;
	}

	public String getServerName () {
		return serverName;
	}

	public RemoteMethodCall getMethodCall () {
		return methodCall;
	}
}

package nl.idgis.geoide.commons.remote.transport.messages;

import nl.idgis.geoide.commons.remote.RemoteMethodServer;

public class AddListener {
	private final RemoteMethodServer server;
	private final String name;
	
	public AddListener (final RemoteMethodServer server, final String name) {
		if (server == null) {
			throw new NullPointerException ("server cannot be null");
		}
		if (name == null) {
			throw new NullPointerException ("name cannot be null");
		}
		
		this.server = server;
		this.name = name;
	}
	
	public RemoteMethodServer getServer () {
		return server;
	}
	
	public String getName () {
		return name;
	}
}

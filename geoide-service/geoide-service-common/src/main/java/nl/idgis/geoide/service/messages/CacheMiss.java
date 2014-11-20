package nl.idgis.geoide.service.messages;

import java.io.Serializable;

import nl.idgis.geoide.util.Assert;

public class CacheMiss implements Serializable {
	private static final long serialVersionUID = 2222789182306148618L;
	
	private final String key;
	
	public CacheMiss (final String key) {
		Assert.notNull (key, "key");
		
		this.key = key;
	}

	public String getKey () {
		return key;
	}
}
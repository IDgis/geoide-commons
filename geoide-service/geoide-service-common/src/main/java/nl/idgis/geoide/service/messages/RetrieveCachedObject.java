package nl.idgis.geoide.service.messages;

import java.io.Serializable;

import nl.idgis.geoide.util.Assert;

public class RetrieveCachedObject implements Serializable {
	private static final long serialVersionUID = 503964864009326315L;
	
	private final String key;
	
	public RetrieveCachedObject (final String key) {
		Assert.notNull (key, "key");
		
		this.key = key;
	}

	public String getKey () {
		return key;
	}
}
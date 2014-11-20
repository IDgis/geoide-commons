package nl.idgis.geoide.service.messages;

import java.io.Serializable;

import org.joda.time.LocalDateTime;

import nl.idgis.geoide.util.Assert;

public class CachedObject implements Serializable {
	private static final long serialVersionUID = -8092941982212166171L;
	
	private final String key;
	private final LocalDateTime expiryTime;
	private final Serializable object;
	
	public CachedObject (final String key, final LocalDateTime expiryTime, final Serializable object) {
		Assert.notNull (key, "key");
		Assert.notNull (object, "object");
		Assert.notNull (expiryTime, "expiryTime");
		
		this.key = key;
		this.object = object;
		this.expiryTime = expiryTime;
	}

	public String getKey () {
		return key;
	}

	public Serializable getObject () {
		return object;
	}

	public LocalDateTime getExpiryTime () {
		return expiryTime;
	}
}
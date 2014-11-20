package nl.idgis.geoide.service.messages;

import java.io.Serializable;

import org.joda.time.LocalDateTime;

public final class CacheResponse implements Serializable {

	private static final long serialVersionUID = 1115682490337625227L;
	
	private final String key;
	private final LocalDateTime expiryTime;
	
	public CacheResponse (final String key, final LocalDateTime expiryTime) {
		if (key == null) {
			throw new NullPointerException ("key cannot be null");
		}
		if (expiryTime == null) {
			throw new NullPointerException ("expiryTime cannot be null");
		}
		
		this.key = key;
		this.expiryTime = expiryTime;
	}
	
	public String getKey () {
		return key;
	}
	
	public LocalDateTime getExpiryTime () {
		return expiryTime;
	}
}

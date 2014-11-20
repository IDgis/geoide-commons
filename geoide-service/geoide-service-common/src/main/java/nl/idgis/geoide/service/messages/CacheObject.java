package nl.idgis.geoide.service.messages;

import java.io.Serializable;

public final class CacheObject implements Serializable {
	private static final long serialVersionUID = -5823424701066581008L;
	
	private final Serializable object;
	private final int ttlMillis;
	
	public CacheObject (final Serializable object, final int ttlMillis) {
		if (object == null) {
			throw new NullPointerException ("object cannot be null");
		}
		
		this.object = object;
		this.ttlMillis = ttlMillis;
	}

	public Serializable getObject () {
		return object;
	}

	public int getTtl () {
		return ttlMillis;
	}
}

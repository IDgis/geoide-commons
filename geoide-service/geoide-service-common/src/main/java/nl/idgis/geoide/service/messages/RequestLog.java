package nl.idgis.geoide.service.messages;

import java.io.Serializable;

public final class RequestLog implements Serializable {

	private static final long serialVersionUID = 7353284026212240437L;
	
	private final int start;
	private final int count;
	
	public RequestLog (final int start, final int count) {
		this.start = start;
		this.count = count;
	}
	
	public int start () {
		return start;
	}
	
	public int count () {
		return count;
	}
}
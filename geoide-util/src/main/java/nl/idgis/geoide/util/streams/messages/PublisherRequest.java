package nl.idgis.geoide.util.streams.messages;

import java.io.Serializable;

public class PublisherRequest implements Serializable {

	private static final long serialVersionUID = -646485514184699568L;
	
	private final long count;
	
	public PublisherRequest (final long count) {
		this.count = count;
	}
	
	public long getCount () {
		return count;
	}
}

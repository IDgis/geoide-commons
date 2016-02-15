package nl.idgis.geoide.commons.domain.document;

import java.io.Serializable;
import java.net.URI;


public final class StoredImage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7900674703825053663L;
	private final URI uri;
	private final byte[] image;
	
	public StoredImage (
			final byte[] image,
			final URI uri
			) {
		this.image = image;
		this.uri = uri;
		
	}
	
	public URI getUri () {
		return uri;
	}
		
	
	public byte[] getImage () {
		return image;
	}
	
	
	
}
	


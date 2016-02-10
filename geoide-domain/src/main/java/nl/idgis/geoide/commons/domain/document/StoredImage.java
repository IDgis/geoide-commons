package nl.idgis.geoide.commons.domain.document;

import java.io.Serializable;
import java.net.URI;

import javax.swing.ImageIcon;

public final class StoredImage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7900674703825053663L;
	private final URI uri;
	private final ImageIcon image;
	
	public StoredImage (
			final ImageIcon image,
			final URI uri
			) {
		this.image = image;
		this.uri = uri;
		
	}
	
	public URI getUri () {
		return uri;
	}
		
	
	public ImageIcon getImage () {
		return image;
	}
	
	
	
}
	


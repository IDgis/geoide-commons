package nl.idgis.geoide.commons.domain.document;

import java.awt.Image;
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
			final ImageIcon imageIcon,
			final URI uri
			) {
		this.image = imageIcon;
		this.uri = uri;
		
		
	}
	
	public URI getUri () {
		return uri;
	}
		
	
	public ImageIcon getImage () {
		return image;
	}
	
	
	
}
	


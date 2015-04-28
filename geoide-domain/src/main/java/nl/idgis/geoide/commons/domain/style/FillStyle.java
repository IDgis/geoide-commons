package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;

public class FillStyle implements Serializable {
	private static final long serialVersionUID = 6757679155919063065L;
	
	private final Color color;
	
	public FillStyle (final Color color) {
		if (color == null) {
			throw new NullPointerException ("color cannot be null");
		}
		
		this.color = color;
	}
	
	public Color getColor () {
		return color;
	}
}

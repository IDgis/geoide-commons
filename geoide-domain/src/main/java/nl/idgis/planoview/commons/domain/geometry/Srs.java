package nl.idgis.planoview.commons.domain.geometry;

import java.io.Serializable;

public class Srs implements Serializable {
	private static final long serialVersionUID = 495151829651821287L;
	
	private final String code;
	
	public Srs (final String code) {
		if (code == null) {
			throw new NullPointerException ("code cannot be null");
		}
		
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}

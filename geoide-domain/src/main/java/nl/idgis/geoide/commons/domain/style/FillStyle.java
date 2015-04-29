package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties (ignoreUnknown = true)
public class FillStyle implements Serializable {
	private static final long serialVersionUID = 6757679155919063065L;
	
	private final Color color;
	
	@JsonCreator
	public FillStyle (final @JsonProperty ("color") Color color) {
		if (color == null) {
			throw new NullPointerException ("color cannot be null");
		}
		
		this.color = color;
	}
	
	public Color getColor () {
		return color;
	}
}

package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a fill style consisting of a single color.
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class FillStyle implements Serializable {
	private static final long serialVersionUID = 6757679155919063065L;
	
	private final Color color;

	/**
	 * Constructs a fill style by providing a single {@link Color}.
	 * 
	 * @param color	The color of this fill style, cannot be null.
	 */
	@JsonCreator
	public FillStyle (final @JsonProperty ("color") Color color) {
		if (color == null) {
			throw new NullPointerException ("color cannot be null");
		}
		
		this.color = color;
	}

	/**
	 * Returns the color of this fill style.
	 * 
	 * @return	The color of this fill style. Is never null.
	 */
	public Color getColor () {
		return color;
	}
}

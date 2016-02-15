package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a feature style. This class mimics a subset of the style class that is found in OpenLayers 3.
 * 
 * A style can consist of:
 * - A fill style
 * - A stroke style
 * - An image style (for points)
 * - A text style
 * 
 * All styles are optional. There is no conditional styling, this class always represents an "effective"
 * style for a feature.
 * 
 * Styles are serializable to and from JSON using Jackson.
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class Style implements Serializable {
	private static final long serialVersionUID = 463410720462853690L;
	
	private final FillStyle fill;
	private final StrokeStyle stroke;
	private final ImageStyle image;
	private final TextStyle text;
	
	/**
	 * Constructs a new style.
	 * 
	 * @param fill		The fill style.
	 * @param stroke	The stroke style.
	 * @param image		The image style.
	 * @param text		The text style.
	 */
	@JsonCreator
	public Style (
			final @JsonProperty ("fill") FillStyle fill, 
			final @JsonProperty ("stroke") StrokeStyle stroke, 
			final @JsonProperty ("image") ImageStyle image, 
			final @JsonProperty ("text") TextStyle text) {
		
		this.fill = fill;
		this.stroke = stroke;
		this.image = image;
		this.text = text;
	}

	/**
	 * Returns the fill style.
	 * 
	 * @return	The fill style, or null if there is no fill style.
	 */
	public FillStyle getFill () {
		return fill;
	}

	/**
	 * Returns the stroke style.
	 * 
	 * @return	The stroke style, or null if there is no stroke style.
	 */
	public StrokeStyle getStroke () {
		return stroke;
	}

	/**
	 * Returns the image style.
	 * 
	 * @return	The image style, or null if there is no image style.
	 */
	public ImageStyle getImage () {
		return image;
	}

	/**
	 * Returns the text style.
	 * 
	 * @return	The text style, or null if there is no text style.
	 */
	public TextStyle getText () {
		return text;
	}
}

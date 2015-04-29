package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties (ignoreUnknown = true)
public class Style implements Serializable {
	private static final long serialVersionUID = 463410720462853690L;
	
	private final FillStyle fill;
	private final StrokeStyle stroke;
	private final ImageStyle image;
	private final TextStyle text;
	
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

	public FillStyle getFill () {
		return fill;
	}

	public StrokeStyle getStroke () {
		return stroke;
	}

	public ImageStyle getImage () {
		return image;
	}

	public TextStyle getText () {
		return text;
	}
}

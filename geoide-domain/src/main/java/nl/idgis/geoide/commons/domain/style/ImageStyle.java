package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties (ignoreUnknown = true)
public class ImageStyle implements Serializable {
	private static final long serialVersionUID = 129636213032059659L;
	
	private final String type;
	private final Double opacity;
	private final Boolean rotateWithView;
	private final Double rotation;
	private final Double scale;
	private final Boolean snapToPixel;
	private final Double radius;
	private final FillStyle fill;
	private final StrokeStyle stroke;
	
	@JsonCreator
	public ImageStyle (
			final @JsonProperty ("type") String type,
			final @JsonProperty ("opacity") Double opacity,
			final @JsonProperty ("rotateWithView") Boolean rotateWithView,
			final @JsonProperty ("rotation") Double rotation,
			final @JsonProperty ("scale") Double scale,
			final @JsonProperty ("snapToPixel") Boolean snapToPixel,
			final @JsonProperty ("radius") Double radius,
			final @JsonProperty ("fill") FillStyle fill,
			final @JsonProperty ("stroke") StrokeStyle stroke) {
		
		this.type = type;
		this.opacity = opacity;
		this.rotateWithView = rotateWithView;
		this.rotation = rotation;
		this.scale = scale;
		this.snapToPixel = snapToPixel;
		this.radius = radius;
		this.fill = fill;
		this.stroke = stroke;
	}

	public String getType () {
		return type;
	}

	public Double getOpacity () {
		return opacity;
	}

	public Boolean getRotateWithView () {
		return rotateWithView;
	}

	public Double getRotation () {
		return rotation;
	}

	public Double getScale () {
		return scale;
	}

	public Boolean getSnapToPixel () {
		return snapToPixel;
	}

	public Double getRadius () {
		return radius;
	}

	public FillStyle getFill () {
		return fill;
	}

	public StrokeStyle getStroke () {
		return stroke;
	}
}

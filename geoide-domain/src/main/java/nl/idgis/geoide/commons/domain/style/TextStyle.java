package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties (ignoreUnknown = true)
public class TextStyle implements Serializable {
	private static final long serialVersionUID = -756522132125217223L;
	
	private final String font;
	private final String text;
	private final double offsetX;
	private final double offsetY;
	private final Double rotation;
	private final Double scale;
	private final StrokeStyle stroke;
	private final FillStyle fill;
	private final String textAlign;
	private final String textBaseline;

	@JsonCreator
	public TextStyle (
			final @JsonProperty ("font") String font,
			final @JsonProperty ("text") String text,
			final @JsonProperty ("offsetX") double offsetX,
			final @JsonProperty ("offsetY") double offsetY,
			final @JsonProperty ("rotation") Double rotation,
			final @JsonProperty ("scale") Double scale,
			final @JsonProperty ("stroke") StrokeStyle stroke,
			final @JsonProperty ("fill") FillStyle fill,
			final @JsonProperty ("textAlign") String textAlign,
			final @JsonProperty ("textBaseline") String textBaseline) {
		this.font = font;
		this.text = text;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.rotation = rotation;
		this.scale = scale;
		this.stroke = stroke;
		this.fill = fill;
		this.textAlign = textAlign;
		this.textBaseline = textBaseline;
	}

	public String getFont () {
		return font;
	}

	public String getText () {
		return text;
	}

	public double getOffsetX () {
		return offsetX;
	}

	public double getOffsetY () {
		return offsetY;
	}

	public Double getRotation () {
		return rotation;
	}

	public Double getScale () {
		return scale;
	}

	public StrokeStyle getStroke () {
		return stroke;
	}

	public FillStyle getFill () {
		return fill;
	}

	public String getTextAlign () {
		return textAlign;
	}

	public String getTextBaseline () {
		return textBaseline;
	}
}

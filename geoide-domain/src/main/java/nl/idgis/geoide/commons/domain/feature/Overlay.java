package nl.idgis.geoide.commons.domain.feature;

import java.io.Serializable;

import nl.idgis.geoide.commons.domain.geometry.geojson.GeoJsonPosition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Overlay implements Serializable {
	private static final long serialVersionUID = 881589533102949934L;
	
	private final double arrowDistance;
	private final double arrowLength;
	private final double arrowWidth;
	private final double borderWidth;
	private final double height;
	private final GeoJsonPosition offset;
	private final String text;
	private final double width;
	
	@JsonCreator
	public Overlay (
			final @JsonProperty ("arrowDistance") double arrowDistance,
			final @JsonProperty ("arrowLength") double arrowLength,
			final @JsonProperty ("arrowWidth") double arrowWidth,
			final @JsonProperty ("borderWidth") double borderWidth,
			final @JsonProperty ("height") double height,
			final @JsonProperty ("offset") GeoJsonPosition offset,
			final @JsonProperty ("text") String text,
			final @JsonProperty ("width") double width
		) {
		
		this.arrowDistance = arrowDistance;
		this.arrowLength = arrowLength;
		this.arrowWidth = arrowWidth;
		this.borderWidth = borderWidth;
		this.height = height;
		this.offset = offset;
		this.text = text;
		this.width = width;
	}

	public double getArrowDistance () {
		return arrowDistance;
	}

	public double getArrowLength () {
		return arrowLength;
	}

	public double getArrowWidth () {
		return arrowWidth;
	}

	public double getBorderWidth () {
		return borderWidth;
	}

	public double getHeight () {
		return height;
	}

	public GeoJsonPosition getOffset () {
		return offset;
	}

	public String getText () {
		return text;
	}

	public double getWidth () {
		return width;
	}
}

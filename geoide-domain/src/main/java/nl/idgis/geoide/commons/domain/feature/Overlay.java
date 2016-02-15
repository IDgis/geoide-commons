package nl.idgis.geoide.commons.domain.feature;

import java.io.Serializable;

import nl.idgis.geoide.commons.domain.geometry.geojson.GeoJsonPosition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Text overlay feature. Has a position and an offset for the text box. The overlay class is serializable
 * to and from JSON using Jackson.
 */
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

	/**
	 * Creates a new overlay.
	 * 
	 * @param arrowDistance		Distance between the anchorpoint and the start of the arrow (in pixels).
	 * @param arrowLength		Length of the arrow tip (in pixels).
	 * @param arrowWidth		Width of the arrow tip in pixels
	 * @param borderWidth		Width of the border in pixels.
	 * @param height			Height of the box in pixels.
	 * @param offset			Offset of the box with respect to the anchorpoint.
	 * @param text				Text to display in the box.
	 * @param width				Width of the box in pixels.
	 */
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

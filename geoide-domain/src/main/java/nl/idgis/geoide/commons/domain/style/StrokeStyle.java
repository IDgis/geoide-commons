package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a stroke style for a line or polygon geometry.
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class StrokeStyle implements Serializable {
	private static final long serialVersionUID = 4256687646185356945L;
	
	private final Color color;
	private final String lineCap;
	private final List<Double> lineDash;
	private final String lineJoin;
	private final Double miterLimit;
	private final Double width;

	/**
	 * Constructs a new stroke style.
	 * 
	 * @param color			The color of the stroke, cannot be null.
	 * @param width			The width of the stroke, or null if not set.
	 * @param lineCap		The lineCap style, or null if not set.
	 * @param lineDash		The dash style, or null of not set.
	 * @param lineJoin		The line join style, or null of not set.
	 * @param miterLimit	The miter limit, or null of not set.
	 */
	@JsonCreator
	public StrokeStyle (
		final @JsonProperty ("color") Color color,
		final @JsonProperty ("width") Double width,
		final @JsonProperty ("lineCap") String lineCap,
		final @JsonProperty ("lineDash") List<Double> lineDash,
		final @JsonProperty ("lineJoin") String lineJoin,
		final @JsonProperty ("miterLimit") Double miterLimit) {
		
		if (color == null) {
			throw new NullPointerException ("color cannot be null");
		}
		
		this.color = color;
		this.width = width;
		this.lineCap = lineCap;
		this.lineDash = lineDash == null ? null : new ArrayList<> (lineDash);
		this.lineJoin = lineJoin;
		this.miterLimit = miterLimit;
	}

	/**
	 * @return The stroke color. This value is never null.
	 */
	public Color getColor () {
		return color;
	}

	/**
	 * @return The line cap style, or null if not set.
	 */
	public String getLineCap () {
		return lineCap;
	}

	/**
	 * @return The line dash style, or null if not set.
	 */
	public List<Double> getLineDash () {
		return lineDash == null ? null : Collections.unmodifiableList (lineDash);
	}

	/**
	 * @return The line join style, or null if not set.
	 */
	public String getLineJoin () {
		return lineJoin;
	}

	/**
	 * @return The miter limit, or null if not set.
	 */
	public Double getMiterLimit () {
		return miterLimit;
	}

	/**
	 * @return The stroke width, or null if not set.
	 */
	public Double getWidth () {
		return width;
	}
}

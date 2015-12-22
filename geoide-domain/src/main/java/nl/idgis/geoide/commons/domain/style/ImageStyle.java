package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a image style, or point symbolizer.
 */
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

	/**
	 * Constructs a new image style. All attributes are optional except type.
	 * 
	 * @param type				The type of this image style. Cannot be null.
	 * @param opacity			The opacity of the symbol.
	 * @param rotateWithView	Whether the symbol should be rotated with the view, or should remain fixed with respect to the viewport.
	 * @param rotation			Rotation of the symbol.
	 * @param scale				Scale of the symbol.
	 * @param snapToPixel		Whether the symbol should be snapped to pixel boundaries before rendering.
	 * @param radius			The radius of the symbol.
	 * @param fill				An optional fill style for the point.
	 * @param stroke			An optional stroke style for the point.
	 */
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

	/**
	 * @return	The type of the image style.
	 */
	public String getType () {
		return type;
	}

	/**
	 * @return The opacity of the symbol, or null.
	 */
	public Double getOpacity () {
		return opacity;
	}

	/**
	 * @return Whether the symbol should be rotated with the map view, or null if not set.
	 */
	public Boolean getRotateWithView () {
		return rotateWithView;
	}

	/**
	 * @return The rotation of the symbol, or null if not set.
	 */
	public Double getRotation () {
		return rotation;
	}

	/**
	 * @return The scale of the symbol, or null if not set.
	 */
	public Double getScale () {
		return scale;
	}

	/**
	 * @return Whether the symbol should be snapped to pixel boundaries when rendering, or null if not set.
	 */
	public Boolean getSnapToPixel () {
		return snapToPixel;
	}

	/**
	 * @return The circle radius of the symbol, or null if not set.
	 */
	public Double getRadius () {
		return radius;
	}

	/**
	 * @return The fill style of the symbol, or null if not set.
	 */
	public FillStyle getFill () {
		return fill;
	}

	/**
	 * @return The stroke style of the symbol, or null if not set.
	 */
	public StrokeStyle getStroke () {
		return stroke;
	}
}

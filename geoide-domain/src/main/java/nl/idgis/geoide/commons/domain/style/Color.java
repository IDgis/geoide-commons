package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A representation of a color value (RGBA). Colors can be represented in different forms:
 *
 *  - #RGB
 *  - #RRGGBB
 *  - rgb(r, g, b)
 *  - rgba(r, g, b, a)
 *  
 * Color values can be serialized to and from JSON. Accepts an array of doubles as input, or a string.
 * Color values are always serialized as a string in the form "rgba(r, g, b, a)"
 */
public class Color implements Serializable {
	private static final long serialVersionUID = 7597554911147626562L;
	
	private final static Pattern hexColorPattern = Pattern.compile ("^#(?:[0-9a-f]{3}){1,2}$", Pattern.CASE_INSENSITIVE);
	private final static Pattern rgbColorPattern = Pattern.compile ("^(?:rgb)?\\((0|[1-9]\\d{0,2}),\\s?(0|[1-9]\\d{0,2}),\\s?(0|[1-9]\\d{0,2})\\)$", Pattern.CASE_INSENSITIVE);
	private final static Pattern rgbaColorPattern = Pattern.compile ("^(?:rgba)?\\((0|[1-9]\\d{0,2}),\\s?(0|[1-9]\\d{0,2}),\\s?(0|[1-9]\\d{0,2}),\\s?(0|1|0\\.\\d{0,10})\\)$", Pattern.CASE_INSENSITIVE);
	
	private final double[] rgba;
	
	/**
	 * Creates a new color value from an array of doubles. The array must have 1, 3 or 4 elements.
	 * 
	 * @param rgba	An array of RGBA values consisting of 1, 3 or 4 elements.
	 */
	@JsonCreator
	public Color (final double[] rgba) {
		if (rgba == null) {
			throw new NullPointerException ("rgba cannot be null");
		}
		
		if (rgba.length == 3) {
			this.rgba = createColor (rgba[0], rgba[1], rgba[2], 1.0);
		} else if (rgba.length == 1) {
			this.rgba = createColor (rgba[0], rgba[0], rgba[0], 1.0);
		} else if (rgba.length == 4) {
			this.rgba = createColor (rgba[0], rgba[1], rgba[2], rgba[3]);
		} else {
			throw new IllegalArgumentException ("rgba must have 1, 3 or 4 elements");
		}
	}
	
	private static double[] createColor (final double r, final double g, final double b, final double a) {
		if (r < 1.0 || g < 1.0 || b < 1.0) {
			return new double[] { r, g, b, a };
		} else {
			return new double[] { r / 255.0, g / 255.0, b / 255.0, a };
		}
	}
	
	/**
	 * Creates a new color value by parsing a string. The string can be in the form:
	 * - #RGB
	 * - #RGBA
	 * - rgb(r, g, b)
	 * - rgba(r, g, b, a)
	 * 
	 * @param color	The color value as a string.
	 */
	@JsonCreator
	public Color (final String color) {
		if (color == null) {
			throw new NullPointerException ("color cannot be null");
		}

		final Matcher hexColorMatcher = hexColorPattern.matcher (color);
		if (hexColorMatcher.matches ()) {
			final int n = color.length () - 1;
			final int d = n == 3 ? 1 : 2;
			
			if (n != 3 && n != 6) {
				throw new IllegalArgumentException ("Color string length should be 3 or 6");
			}
			
			final int r = Integer.parseUnsignedInt (color.substring (1 + 0 * d, 1 + 0 * d + d), 16);
			final int g = Integer.parseUnsignedInt (color.substring (1 + 1 * d, 1 + 1 * d + d), 16);
			final int b = Integer.parseUnsignedInt (color.substring (1 + 2 * d, 1 + 2 * d + d), 16);
			
			if (d == 1) {
				this.rgba = new double[] {
					((double) ((r << 4) | r)) / 255.0,
					((double) ((g << 4) | g)) / 255.0,
					((double) ((b << 4) | b)) / 255.0,
					1.0
				};
			} else {
				this.rgba = new double[] {
					((double) r) / 255.0,
					((double) g) / 255.0,
					((double) b) / 255.0,
					1.0
				};
			}
			
			return;
		}
		
		final Matcher rgbMatcher = rgbColorPattern.matcher (color);
		if (rgbMatcher.matches ()) {
			this.rgba = new double[] {
				(double) Integer.parseInt (rgbMatcher.group (1)) / 255.0,
				(double) Integer.parseInt (rgbMatcher.group (2)) / 255.0,
				(double) Integer.parseInt (rgbMatcher.group (3)) / 255.0,
				1.0
			};
			
			return;
		}
		
		final Matcher rgbaMatcher = rgbaColorPattern.matcher (color);
		if (rgbaMatcher.matches ()) {
			this.rgba = new double[] {
					(double) Integer.parseInt (rgbaMatcher.group (1)) / 255.0,
					(double) Integer.parseInt (rgbaMatcher.group (2)) / 255.0,
					(double) Integer.parseInt (rgbaMatcher.group (3)) / 255.0,
					Double.parseDouble (rgbaMatcher.group (4))
				};

			return;
		}
		
		throw new IllegalArgumentException ("Invalid color: " + color);
	}
	
	/**
	 * Returns the color value as a string in the form "rgba(r, g, b, a)".
	 * 
	 * @return	The current color value as a string.
	 */
	@JsonValue
	public String getRgbaAsString () {
		return String.format (
			"rgba(%d,%d,%d,%f)", 
			(int)(rgba[0] * 255.0), 
			(int)(rgba[1] * 255.0), 
			(int)(rgba[2] * 255.0), 
			rgba[3]
		);
	}

	/**
	 * @return The red component (0-1).
	 */
	public double getR () {
		return rgba[0];
	}
	
	/**
	 * @return The green component (0-1).
	 */
	public double getG () {
		return rgba[1];
	}
	
	/**
	 * @return The blue component (0-1).
	 */
	public double getB () {
		return rgba[2];
	}
	
	/**
	 * @return The alpha component (0-1).
	 */
	public double getA () {
		return rgba[3];
	}
}

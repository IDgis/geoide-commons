package nl.idgis.geoide.commons.domain.style;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties (ignoreUnknown = true)
public class StrokeStyle implements Serializable {
	private static final long serialVersionUID = 4256687646185356945L;
	
	private final Color color;
	private final String lineCap;
	private final List<Double> lineDash;
	private final String lineJoin;
	private final Double miterLimit;
	private final Double width;

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

	public Color getColor () {
		return color;
	}

	public String getLineCap () {
		return lineCap;
	}

	public List<Double> getLineDash () {
		return lineDash == null ? null : Collections.unmodifiableList (lineDash);
	}

	public String getLineJoin () {
		return lineJoin;
	}

	public Double getMiterLimit () {
		return miterLimit;
	}

	public Double getWidth () {
		return width;
	}
}

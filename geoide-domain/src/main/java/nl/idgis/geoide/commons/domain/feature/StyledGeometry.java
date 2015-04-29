package nl.idgis.geoide.commons.domain.feature;

import java.io.Serializable;

import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.geojson.AbstractGeoJsonGeometry;
import nl.idgis.geoide.commons.domain.style.Style;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StyledGeometry implements Serializable {
	private static final long serialVersionUID = -570918813312638454L;
	
	private final Style style;
	private final AbstractGeoJsonGeometry geometry;
	
	@JsonCreator
	public StyledGeometry (
			final @JsonProperty ("style") Style style, 
			final @JsonProperty ("geometry") AbstractGeoJsonGeometry geometry) {
		if (style == null) {
			throw new NullPointerException ("style cannot be null");
		}
		if (geometry == null) {
			throw new NullPointerException ("geometry cannot be null");
		}
		
		this.style = style;
		this.geometry = geometry;
	}

	public Style getStyle () {
		return style;
	}

	public Geometry getGeometry () {
		return geometry;
	}
}

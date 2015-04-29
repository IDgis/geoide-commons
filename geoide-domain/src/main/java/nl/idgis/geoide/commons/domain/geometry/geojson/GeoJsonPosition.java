package nl.idgis.geoide.commons.domain.geometry.geojson;

import java.io.Serializable;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public class GeoJsonPosition implements Serializable {
	private static final long serialVersionUID = -3942501718736867935L;
	
	private final double[] values;
	
	@JsonCreator
	public GeoJsonPosition (final double[] values) {
		if (values == null) {
			throw new NullPointerException ("values cannot be null");
		}
		if (values.length < 2) {
			throw new NullPointerException ("there should be at least 2 values");
		}
		
		this.values = Arrays.copyOf (values, values.length);
	}
	
	@JsonValue
	public double[] getValues () {
		return Arrays.copyOf (values, values.length);
	}

	@JsonIgnore
	public int size () {
		return values.length;
	}
	
	@JsonIgnore
	public double get (final int i) {
		return values[i];
	}
}

package nl.idgis.geoide.commons.domain.geometry.geojson;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.GeometryType;
import nl.idgis.geoide.commons.domain.geometry.Srs;
import nl.idgis.geoide.commons.domain.geometry.GeometryType.Type;

public abstract class AbstractGeoJsonGeometry implements Geometry {
	private static final long serialVersionUID = -5590461394080222106L;

	/**
	 * JSON getter that returns the geometry type as a string.
	 * 
	 * @return The geometry type as a string, for JSON serialization.
	 */
	@JsonGetter ("type")
	public String getTypeName () {
		return getType ().getName ();
	}
	
	@Override
	@JsonIgnore
	public Srs getSrs () {
		return null;
	}
	
	@Override
	@JsonIgnore
	public boolean is (final GeometryType type) {
		return getType ().equals (type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@JsonIgnore
	public <T extends Geometry> T as (final Type<T> type) {
		return (T) this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@JsonIgnore
	public Geometry as (final GeometryType type) {
		if (!is (type)) {
			return null;
		}
		
		return as ((GeometryType.Type) type);
	}
}

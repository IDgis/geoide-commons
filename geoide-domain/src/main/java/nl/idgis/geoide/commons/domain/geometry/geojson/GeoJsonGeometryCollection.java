package nl.idgis.geoide.commons.domain.geometry.geojson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.GeometryCollection;
import nl.idgis.geoide.commons.domain.geometry.GeometryType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoJsonGeometryCollection extends AbstractGeoJsonGeometry implements GeometryCollection<Geometry> {
	private static final long serialVersionUID = 887449597290888897L;
	
	private final List<AbstractGeoJsonGeometry> geometries;
	
	@JsonCreator
	public GeoJsonGeometryCollection (final @JsonProperty ("geometries") List<AbstractGeoJsonGeometry> geometries) {
		this.geometries = geometries == null || geometries.isEmpty () ? Collections.emptyList () : new ArrayList<> (geometries);
	}
	
	public List<AbstractGeoJsonGeometry> getGeometries () {
		return Collections.unmodifiableList (geometries);
	}
	
	@Override
	@JsonIgnore
	public Geometry getEnvelope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonIgnore
	public GeometryType getType () {
		return GeometryType.GEOMETRY_COLLECTION;
	}

	@Override
	@JsonIgnore
	public byte[] asBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonIgnore
	public String asText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonIgnore
	public Envelope getRawEnvelope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonIgnore
	public int getNumGeometries () {
		return geometries.size ();
	}

	@Override
	@JsonIgnore
	public Geometry getGeometryN (final int n) {
		return geometries.get (n);
	}
}

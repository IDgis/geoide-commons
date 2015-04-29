package nl.idgis.geoide.commons.domain.geometry.geojson;

import java.util.List;
import java.util.stream.Collectors;

import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.GeometryType;
import nl.idgis.geoide.commons.domain.geometry.MultiPoint;
import nl.idgis.geoide.commons.domain.geometry.Point;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoJsonMultiPoint extends AbstractGeoJsonGeometry implements MultiPoint {
	private static final long serialVersionUID = -377206654681987762L;
	
	private final List<GeoJsonPoint> points;
	
	@JsonCreator
	public GeoJsonMultiPoint (final @JsonProperty ("coordinates") List<GeoJsonPosition> coordinates) {
		if (coordinates == null) {
			throw new NullPointerException ("coordinates cannot be null");
		}
		
		this.points = coordinates.stream ().map (GeoJsonPoint::new).collect (Collectors.toList ()); 
	}
	
	public List<GeoJsonPosition> getCoordinates () {
		return points.stream ().map (GeoJsonPoint::getCoordinates).collect (Collectors.toList ());
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
		return GeometryType.MULTI_POINT;
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
		return points.size ();
	}

	@Override
	@JsonIgnore
	public Point getGeometryN (final int n) {
		return points.get (n);
	}
}

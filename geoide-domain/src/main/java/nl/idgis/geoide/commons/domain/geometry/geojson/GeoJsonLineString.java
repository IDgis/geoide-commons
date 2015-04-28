package nl.idgis.geoide.commons.domain.geometry.geojson;

import java.util.List;
import java.util.stream.Collectors;

import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.GeometryType;
import nl.idgis.geoide.commons.domain.geometry.LineString;
import nl.idgis.geoide.commons.domain.geometry.Point;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoJsonLineString extends AbstractGeoJsonGeometry implements LineString {
	private static final long serialVersionUID = -5227563585398161992L;
	
	private final List<GeoJsonPoint> points;
	
	@JsonCreator
	public GeoJsonLineString (final @JsonProperty("coordinates") List<GeoJsonPosition> coordinates) {
		if (coordinates == null) {
			throw new NullPointerException ("coordinates cannot be null");
		}
		if (coordinates.size () < 2) {
			throw new IllegalArgumentException ("coordinates should contain at least 2 points");
		}
		
		for (final GeoJsonPosition position: coordinates) {
			if (position == null) {
				throw new NullPointerException ("position cannot be null");
			}
		}
		
		this.points = coordinates.stream ().map ((position) -> new GeoJsonPoint (position)).collect (Collectors.toList ()); 
	}

	public List<GeoJsonPosition> getCoordinates () {
		return points.stream ().map ((point) -> point.getCoordinates ()).collect (Collectors.toList ());
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
		return GeometryType.LINE_STRING;
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
	public Point getStartPoint () {
		return points.get (0);
	}

	@Override
	@JsonIgnore
	public Point getEndPoint () {
		return points.get (points.size () - 1);
	}

	@Override
	@JsonIgnore
	public int getNumPoints () {
		return points.size ();
	}

	@Override
	@JsonIgnore
	public Point getPointN (final int n) {
		return points.get (n);
	}
}

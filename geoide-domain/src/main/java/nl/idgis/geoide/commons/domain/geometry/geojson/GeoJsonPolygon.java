package nl.idgis.geoide.commons.domain.geometry.geojson;

import java.util.List;
import java.util.stream.Collectors;

import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.GeometryType;
import nl.idgis.geoide.commons.domain.geometry.LineString;
import nl.idgis.geoide.commons.domain.geometry.Polygon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoJsonPolygon extends AbstractGeoJsonGeometry implements Polygon {
	private static final long serialVersionUID = 795722705036594622L;
	
	private final List<GeoJsonLineString> rings;
	
	@JsonCreator
	public GeoJsonPolygon (final @JsonProperty("coordinates") List<List<GeoJsonPosition>> coordinates) {
		if (coordinates == null) {
			throw new NullPointerException ("coordinates cannot be null");
		}
		if (coordinates.size () < 1) {
			throw new IllegalArgumentException ("polygon must have at least one ring");
		}

		this.rings = coordinates
			.stream ()
			.map (GeoJsonLineString::new)
			.collect (Collectors.toList ());
	}
	
	public List<List<GeoJsonPosition>> getCoordinates () {
		return rings.stream ().map (GeoJsonLineString::getCoordinates).collect (Collectors.toList ());
	}
	
	@Override
	@JsonIgnore
	public Geometry getEnvelope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonIgnore
	public GeometryType getType() {
		return GeometryType.POLYGON;
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
	public LineString getExteriorRing () {
		return rings.get (0);
	}

	@Override
	@JsonIgnore
	public int getNumInteriorRing () {
		return rings.size () - 1;
	}

	@Override
	@JsonIgnore
	public LineString getInteriorRingN (final int n) {
		return rings.get (n + 1);
	}
}

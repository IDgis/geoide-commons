package nl.idgis.geoide.commons.domain.geometry.geojson;

import java.util.List;
import java.util.stream.Collectors;

import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.GeometryType;
import nl.idgis.geoide.commons.domain.geometry.MultiPolygon;
import nl.idgis.geoide.commons.domain.geometry.Polygon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoJsonMultiPolygon extends AbstractGeoJsonGeometry implements MultiPolygon {
	private static final long serialVersionUID = 3672885364389680130L;
	
	private final List<GeoJsonPolygon> polygons;
	
	@JsonCreator
	public GeoJsonMultiPolygon (final @JsonProperty("coordinates") List<List<List<GeoJsonPosition>>> coordinates) {
		if (coordinates == null) {
			throw new NullPointerException ("coordinates cannot be null");
		}
		
		for (final List<List<GeoJsonPosition>> polygon: coordinates) {
			if (polygon == null) {
				throw new NullPointerException ("polygon cannot be null");
			}
			if (polygon.size () < 1) {
				throw new IllegalArgumentException ("polygon must have at least one ring");
			}
			
			for (final List<GeoJsonPosition> ring: polygon) {
				if (ring == null) {
					throw new NullPointerException ("ring cannot be null");
				}
				if (ring.size () < 3) {
					throw new IllegalArgumentException ("ring should have at least 3 positions");
				}
			}
		}

		this.polygons = coordinates.stream ().map (GeoJsonPolygon::new).collect (Collectors.toList ());
	}
	
	public List<List<List<GeoJsonPosition>>> getCoordinates () {
		return polygons.stream ().map (GeoJsonPolygon::getCoordinates).collect (Collectors.toList ());
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
		return GeometryType.MULTI_POLYGON;
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
		return polygons.size ();
	}

	@Override
	@JsonIgnore
	public Polygon getGeometryN (final int n) {
		return polygons.get (n);
	}
}

package nl.idgis.geoide.commons.domain.geometry.geojson;

import java.util.List;
import java.util.stream.Collectors;

import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.GeometryType;
import nl.idgis.geoide.commons.domain.geometry.LineString;
import nl.idgis.geoide.commons.domain.geometry.MultiLineString;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoJsonMultiLineString extends AbstractGeoJsonGeometry implements MultiLineString {
	private static final long serialVersionUID = -8095421570647043172L;
	
	private final List<GeoJsonLineString> lineStrings;
	
	@JsonCreator
	public GeoJsonMultiLineString (final @JsonProperty("coordinates") List<List<GeoJsonPosition>> coordinates) {
		if (coordinates == null) {
			throw new NullPointerException ("coordinates cannot be null");
		}
		
		this.lineStrings = coordinates
			.stream ()
			.map ((lineString) -> new GeoJsonLineString (lineString))
			.collect (Collectors.toList ());
	}
	
	public List<List<GeoJsonPosition>> getCoordinates () {
		return lineStrings.stream ().map ((lineString) -> lineString.getCoordinates ()).collect (Collectors.toList ());
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
		return GeometryType.MULTI_LINE_STRING;
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
		return lineStrings.size ();
	}

	@Override
	@JsonIgnore
	public LineString getGeometryN (final int n) {
		return lineStrings.get (n);
	}
}

package nl.idgis.geoide.commons.domain.geometry.geojson;

import java.util.Optional;

import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.domain.geometry.Geometry;
import nl.idgis.geoide.commons.domain.geometry.GeometryType;
import nl.idgis.geoide.commons.domain.geometry.Point;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoJsonPoint extends AbstractGeoJsonGeometry implements Point {
	private static final long serialVersionUID = -1934934469857679504L;
	
	private final GeoJsonPosition position;
	
	@JsonCreator
	public GeoJsonPoint (final @JsonProperty("coordinates") GeoJsonPosition coordinates) {
		if (coordinates == null) {
			throw new NullPointerException ("coordinates cannot be null");
		}
		
		this.position = coordinates;
	}
	
	public GeoJsonPosition getCoordinates () {
		return position;
	}
	
	@JsonIgnore
	public GeoJsonPosition getPosition () {
		return position;
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
		return GeometryType.POINT;
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
	public double getX () {
		return position.get (0);
	}

	@Override
	@JsonIgnore
	public double getY () {
		return position.get (1);
	}

	@Override
	@JsonIgnore
	public Optional<Double> getZ () {
		return position.size () > 2 ? Optional.of (position.get (2)) : Optional.empty ();
	}

	@Override
	@JsonIgnore
	public Optional<Double> getM() {
		return position.size () > 3 ? Optional.of (position.get (3)) : Optional.empty ();
	}
}

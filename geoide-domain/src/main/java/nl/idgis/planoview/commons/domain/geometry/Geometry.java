package nl.idgis.planoview.commons.domain.geometry;

import java.io.Serializable;

public interface Geometry extends Serializable {

	Srs getSrs ();
	Geometry getEnvelope ();
	GeometryType getType ();
	byte[] asBytes ();
	String asText ();
	
	Envelope getRawEnvelope ();
	
	boolean is (GeometryType type);
	<T extends Geometry> T as (GeometryType.Type<T> type);
	Geometry as (GeometryType type);
}
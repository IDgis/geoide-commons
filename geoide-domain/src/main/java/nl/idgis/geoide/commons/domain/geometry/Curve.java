package nl.idgis.geoide.commons.domain.geometry;

public interface Curve extends Geometry {
	Point getStartPoint ();
	Point getEndPoint ();
}

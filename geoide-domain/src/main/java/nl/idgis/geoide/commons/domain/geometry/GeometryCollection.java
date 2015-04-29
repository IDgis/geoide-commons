package nl.idgis.geoide.commons.domain.geometry;

public interface GeometryCollection<T extends Geometry> extends Geometry {

	int getNumGeometries ();
	T getGeometryN (int n);
}

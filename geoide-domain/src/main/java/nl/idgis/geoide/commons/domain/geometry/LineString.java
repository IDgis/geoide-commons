package nl.idgis.geoide.commons.domain.geometry;

public interface LineString extends Curve {
	
	int getNumPoints ();
	Point getPointN (int n);
}

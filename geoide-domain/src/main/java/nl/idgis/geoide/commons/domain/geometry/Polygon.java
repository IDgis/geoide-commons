package nl.idgis.geoide.commons.domain.geometry;

public interface Polygon extends Surface {
	
	LineString getExteriorRing ();
	int getNumInteriorRing ();
	LineString getInteriorRingN (int n);
}

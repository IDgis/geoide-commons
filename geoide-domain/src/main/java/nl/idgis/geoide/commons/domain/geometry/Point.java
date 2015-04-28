package nl.idgis.geoide.commons.domain.geometry;

import java.util.Optional;

public interface Point extends Geometry {
	double getX ();
	double getY ();
	Optional<Double> getZ ();
	Optional<Double> getM ();
}

package nl.idgis.geoide.commons.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import nl.idgis.geoide.commons.domain.geometry.Envelope;

public final class FeatureQuery implements Serializable {
	private static final long serialVersionUID = -272100533712128453L;
	
	private final Envelope bbox;
	
	public FeatureQuery (final Optional<Envelope> bbox) {
		Objects.requireNonNull (bbox, "bbox cannot be null");
		
		this.bbox = bbox.isPresent () ? bbox.get () : null;
	}

	public Optional<Envelope> getBbox() {
		return Optional.ofNullable (bbox);
	}
}

package nl.idgis.geoide.commons.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import nl.idgis.geoide.commons.domain.geometry.Envelope;

public final class FeatureQuery implements Serializable {
	private static final long serialVersionUID = -272100533712128453L;
	
	private final Envelope bbox;
	private final String  maxFeatures;
	
	public FeatureQuery (final Optional<Envelope> bbox, final Optional<String> maxFeatures) {
		Objects.requireNonNull (bbox, "bbox cannot be null");
		
		this.bbox = bbox.isPresent () ? bbox.get () : null;
		
		Objects.requireNonNull (maxFeatures, "maxFeatures cannot be null");
		
		this.maxFeatures = maxFeatures.isPresent () ? maxFeatures.get() : null;
	}

	public Optional<Envelope> getBbox() {
		return Optional.ofNullable (bbox);
	}
	
	public Optional<String> getMaxFeatures() {
		return Optional.ofNullable (maxFeatures);
	}
}

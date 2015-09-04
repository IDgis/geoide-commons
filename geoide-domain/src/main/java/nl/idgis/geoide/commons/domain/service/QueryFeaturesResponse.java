package nl.idgis.geoide.commons.domain.service;

import java.io.Serializable;
import java.util.Objects;

import nl.idgis.geoide.commons.domain.feature.Feature;
import nl.idgis.geoide.util.streams.PublisherReference;

public class QueryFeaturesResponse implements Serializable {
	private static final long serialVersionUID = 8678089305714329913L;
	
	private final PublisherReference<Feature> features;
	
	public QueryFeaturesResponse (final PublisherReference<Feature> features) {
		this.features = Objects.requireNonNull (features, "features cannot be null");
	}
	
	public PublisherReference<Feature> getFeatures () {
		return features;
	}
}

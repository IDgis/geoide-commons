package nl.idgis.geoide.commons.domain.service.messages;

import java.util.Optional;

import nl.idgis.geoide.commons.domain.FeatureQuery;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.util.Assert;

public final class QueryFeatures extends ServiceMessage {
	private static final long serialVersionUID = -457496926885275865L;
	
	private final ParameterizedFeatureType<?> featureType;
	private final FeatureQuery query;

	public QueryFeatures (final ParameterizedFeatureType<?> featureType, final Optional<FeatureQuery> query) {
		super (featureType.getFeatureType ().getService ().getIdentification ());
		
		Assert.notNull (featureType, "featureType");
		Assert.notNull (query, "query");
		
		this.featureType = featureType;
		this.query = query.isPresent () ? query.get () : null;
	}

	public ParameterizedFeatureType<?> getFeatureType() {
		return featureType;
	}

	public Optional<FeatureQuery> getQuery() {
		return query == null ? Optional.empty () : Optional.of (query);
	}
}

package nl.idgis.geoide.service.messages;

import nl.idgis.geoide.commons.domain.FeatureQuery;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.planoview.util.Assert;

public final class QueryFeatures extends ServiceMessage {
	private static final long serialVersionUID = -457496926885275865L;
	
	private final ParameterizedFeatureType<?> featureType;
	private final FeatureQuery query;

	public QueryFeatures (final ParameterizedFeatureType<?> featureType, final FeatureQuery query) {
		super (featureType.getFeatureType ().getService ().getIdentification ());
		
		Assert.notNull (featureType, "featureType");
		
		this.featureType = featureType;
		this.query = query;
	}

	public ParameterizedFeatureType<?> getFeatureType() {
		return featureType;
	}

	public FeatureQuery getQuery() {
		return query;
	}
}

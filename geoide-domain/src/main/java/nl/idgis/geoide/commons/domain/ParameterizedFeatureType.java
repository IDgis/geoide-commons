package nl.idgis.geoide.commons.domain;

import java.io.Serializable;
import java.util.Optional;

import nl.idgis.geoide.util.Assert;

public final class ParameterizedFeatureType<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = -6406963210115981288L;
	
	private final ServiceLayer serviceLayer;
	private final FeatureType featureType;
	private final FeatureQuery query;
	private final T parameters;
	
	public ParameterizedFeatureType (final ServiceLayer serviceLayer, final FeatureType featureType, final Optional<FeatureQuery> query, final T parameters) {
		Assert.notNull (serviceLayer, "serviceLayer");
		Assert.notNull (featureType, "featureType");
		Assert.notNull (query, "query");
		
		this.serviceLayer = serviceLayer;
		this.featureType = featureType;
		this.query = query.isPresent () ? query.get () : null;
		this.parameters = parameters;
	}

	public ServiceLayer getServiceLayer () {
		return serviceLayer;
	}

	public FeatureType getFeatureType () {
		return featureType;
	}

	public Optional<FeatureQuery> getQuery () {
		return query == null ? Optional.empty () : Optional.of (query);
	}

	public T getParameters () {
		return parameters;
	}
}
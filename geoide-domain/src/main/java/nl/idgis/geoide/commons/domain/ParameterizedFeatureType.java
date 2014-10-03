package nl.idgis.geoide.commons.domain;

import java.io.Serializable;

import nl.idgis.geoide.util.Assert;

public final class ParameterizedFeatureType<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = -6406963210115981288L;
	
	private final ServiceLayer serviceLayer;
	private final FeatureType featureType;
	private final FeatureQuery query;
	private final T parameters;
	
	public ParameterizedFeatureType (final ServiceLayer serviceLayer, final FeatureType featureType, final FeatureQuery query, final T parameters) {
		Assert.notNull (serviceLayer, "serviceLayer");
		Assert.notNull (featureType, "featureType");
		
		this.serviceLayer = serviceLayer;
		this.featureType = featureType;
		this.query = query;
		this.parameters = parameters;
	}

	public ServiceLayer getServiceLayer () {
		return serviceLayer;
	}

	public FeatureType getFeatureType () {
		return featureType;
	}

	public FeatureQuery getQuery () {
		return query;
	}

	public T getParameters () {
		return parameters;
	}
}
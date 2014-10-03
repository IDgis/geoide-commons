package nl.idgis.geoide.commons.domain;

import nl.idgis.geoide.util.Assert;

public final class ParameterizedServiceLayer<T> {

	private final ServiceLayer serviceLayer;
	private final T parameters;
	
	public ParameterizedServiceLayer (final ServiceLayer serviceLayer, final T parameters) {
		Assert.notNull (serviceLayer, "serviceLayer");
		
		this.serviceLayer = serviceLayer;
		this.parameters = parameters;
	}

	public ServiceLayer getServiceLayer () {
		return serviceLayer;
	}

	public T getParameters () {
		return parameters;
	}
}

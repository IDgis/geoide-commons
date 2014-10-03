package nl.idgis.geoide.commons.domain;

import java.io.Serializable;

import nl.idgis.geoide.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ServiceIdentification implements Serializable {

	private static final long serialVersionUID = 3762931069634305229L;

	private final String serviceType;
	private final String serviceEndpoint;
	private final String serviceVersion;
	
	@JsonCreator
	public ServiceIdentification (final @JsonProperty("serviceType") String serviceType, final @JsonProperty("serviceEndpoint") String serviceEndpoint, final @JsonProperty("serviceVersion") String serviceVersion) {
		Assert.notNull (serviceType, "serviceType");
		Assert.notNull (serviceEndpoint, "serviceEndpoint");
		Assert.notNull (serviceVersion, "serviceVersion");
		
		this.serviceType = serviceType;
		this.serviceEndpoint = serviceEndpoint;
		this.serviceVersion = serviceVersion;
	}

	public String getServiceType () {
		return serviceType;
	}

	public String getServiceEndpoint () {
		return serviceEndpoint;
	}

	public String getServiceVersion () {
		return serviceVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((serviceEndpoint == null) ? 0 : serviceEndpoint.hashCode());
		result = prime * result
				+ ((serviceType == null) ? 0 : serviceType.hashCode());
		result = prime * result
				+ ((serviceVersion == null) ? 0 : serviceVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceIdentification other = (ServiceIdentification) obj;
		if (serviceEndpoint == null) {
			if (other.serviceEndpoint != null)
				return false;
		} else if (!serviceEndpoint.equals(other.serviceEndpoint))
			return false;
		if (serviceType == null) {
			if (other.serviceType != null)
				return false;
		} else if (!serviceType.equals(other.serviceType))
			return false;
		if (serviceVersion == null) {
			if (other.serviceVersion != null)
				return false;
		} else if (!serviceVersion.equals(other.serviceVersion))
			return false;
		return true;
	}
}

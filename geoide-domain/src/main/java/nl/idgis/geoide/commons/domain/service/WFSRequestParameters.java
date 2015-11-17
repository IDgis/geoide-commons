package nl.idgis.geoide.commons.domain.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import nl.idgis.geoide.commons.domain.QName;
import nl.idgis.geoide.util.Assert;

public final class WFSRequestParameters implements Serializable {
	private static final long serialVersionUID = 4264383019028954228L;
	
	private final QName featureType;
	private final Map<String, String> vendorParameters;
	
	public WFSRequestParameters (final QName featureType) {
		this (featureType, null);
	}
	
	public WFSRequestParameters (final QName featureType, final Map<String, String> vendorParameters) {
		this.featureType = Objects.requireNonNull (featureType,  "featureType cannot be null");
		this.vendorParameters = vendorParameters == null ? Collections.<String, String>emptyMap () : new HashMap<String, String> (vendorParameters);
	}

	public QName getFeatureType () {
		return featureType;
	}
	
	public Map<String, String> getVendorParameters() {
		return vendorParameters;
	}
	
	public static Builder build () {
		return new Builder ();
	}
	
	public final static class Builder {
		private QName featureType;
		private final Map<String, String> vendorParameters = new HashMap<> ();
		
		public Builder addVendorParameter (final String name, final String value) {
			Assert.notNull (name, "name");
			Assert.notNull (value, "value");
			
			vendorParameters.put (name, value);
			
			return this;
		}
		
		public Builder setFeatureType (final QName featureType) {
			this.featureType = featureType;
			return this;
		}
		
		public WFSRequestParameters get () {
			return new WFSRequestParameters (featureType, vendorParameters);
		}
	}
}

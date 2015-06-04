package nl.idgis.geoide.commons.domain.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.util.Assert;

public final class WFSRequestParameters implements Serializable {
	private static final long serialVersionUID = 4264383019028954228L;
	
	private final Map<String, String> vendorParameters;
	
	public WFSRequestParameters () {
		this (null);
	}
	
	public WFSRequestParameters (final Map<String, String> vendorParameters) {
		this.vendorParameters = vendorParameters == null ? Collections.<String, String>emptyMap () : new HashMap<String, String> (vendorParameters);
	}

	public Map<String, String> getVendorParameters() {
		return vendorParameters;
	}
	
	public final static class Builder {
		private final Map<String, String> vendorParameters = new HashMap<> ();
		
		public Builder addVendorParameter (final String name, final String value) {
			Assert.notNull (name, "name");
			Assert.notNull (value, "value");
			
			vendorParameters.put (name, value);
			
			return this;
		}
		
		public WFSRequestParameters get () {
			return new WFSRequestParameters (vendorParameters);
		}
	}
}

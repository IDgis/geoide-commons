package nl.idgis.geoide.service.wms;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.util.Assert;

public final class WMSLayerParameters implements Serializable {

	private static final long serialVersionUID = 3741982066529549789L;
	
	private final boolean singleRequest;
	private final Map<String, String> vendorParameters;
	
	public WMSLayerParameters () {
		this (false, null);
	}
	
	public WMSLayerParameters (final boolean singleRequest, final Map<String, String> vendorParameters) {
		this.singleRequest = singleRequest;
		this.vendorParameters = vendorParameters != null ? new HashMap<String, String> (vendorParameters) : Collections.<String, String>emptyMap ();
	}

	public static Builder create () {
		return new Builder ();
	}
	
	public boolean isSingleRequest () {
		return singleRequest;
	}
	
	public Map<String, String> getVendorParameters () {
		return Collections.unmodifiableMap (vendorParameters);
	}
	
	public static class Builder {
		private final Map<String, String> vendorParameters = new HashMap<> ();
		private boolean singleRequest = false;
		
		public Builder addVendorParameter (final String name, final String value) {
			Assert.notNull (name, "name");
			Assert.notNull (value, "value");
			
			vendorParameters.put (name, value);
			
			return this;
		}
		
		public Builder singleRequest () {
			this.singleRequest = true;
			return this;
		}
		
		public WMSLayerParameters get () {
			return new WMSLayerParameters (singleRequest, vendorParameters);
		}
	}
}

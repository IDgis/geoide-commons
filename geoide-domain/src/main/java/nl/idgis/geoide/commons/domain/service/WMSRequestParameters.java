package nl.idgis.geoide.commons.domain.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude (Include.NON_NULL)
public class WMSRequestParameters implements Serializable{
	private static final long serialVersionUID = 1290295196776190101L;
	
	private final String layers;
	private final Boolean transparent;
	private final Map<String, String> vendorParameters;
	
	public WMSRequestParameters (final String layers, final Boolean transparent) {
		this (layers, transparent, null);
	}
	
	public WMSRequestParameters (final String layers, final Boolean transparent, final Map<String, String> vendorParameters) {
		this.layers = layers;
		this.transparent = transparent;
		this.vendorParameters = vendorParameters != null ? new HashMap<String, String> (vendorParameters) : Collections.<String, String>emptyMap ();
	}
	
	public String getLayers () {
		return layers;
	}
	
	public Boolean getTransparent () {
		return transparent;
	}
	
	@JsonAnyGetter
	public Map<String, String> getVendorParameters () {
		return Collections.unmodifiableMap (vendorParameters);
	}
}
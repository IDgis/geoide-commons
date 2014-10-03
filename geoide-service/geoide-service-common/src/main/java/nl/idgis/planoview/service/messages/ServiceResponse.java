package nl.idgis.planoview.service.messages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.planoview.util.Assert;
import nl.idgis.services.Capabilities;
import akka.util.ByteString;

public class ServiceResponse extends ServiceMessage {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4036324402429593254L;
	private final Capabilities capabilities;
	private final String url;
	private final String contentType;
	private final Map<String, String> cacheHeaders;
	private final ByteString data;
	
	public ServiceResponse (
		final ServiceIdentification serviceIdentification,
		final Capabilities capabilities,
		final String url,
		final String contentType,
		final Map<String, String> cacheHeaders,
		final ByteString data) {

		super (serviceIdentification);
		
		Assert.notNull (capabilities, "capabilities");
		Assert.notNull (url, "url");
		Assert.notNull (contentType, "contentType");
		Assert.notNull (data, "data");

		this.capabilities = capabilities;
		this.url = url;
		this.contentType = contentType;
		this.cacheHeaders = cacheHeaders == null ? Collections.<String, String>emptyMap () : new HashMap<String, String> (cacheHeaders);
		this.data = data;
	}
	
	public Capabilities capabilities () {
		return this.capabilities;
	}
	
	public String url () {
		return this.url;
	}
	
	public String contentType () {
		return this.contentType;
	}
	
	public Map<String, String> cacheHeaders () {
		return Collections.unmodifiableMap (cacheHeaders);
	}
	
	public ByteString data () {
		return data;
	}
}

package nl.idgis.geoide.commons.domain.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import akka.util.ByteString;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.util.streams.PublisherReference;

public class ServiceResponse implements Serializable {
	private static final long serialVersionUID = -8772401378324402139L;
	
	private final MimeContentType contentType;
	private final Map<String, String> cacheHeaders;
	private final PublisherReference<ByteString> data;

	public ServiceResponse (
			final MimeContentType contentType, 
			final Map<String, String> cacheHeaders,
			final PublisherReference<ByteString> data) {
		
		this.contentType = Objects.requireNonNull (contentType, "contentType cannot be null");
		this.cacheHeaders = cacheHeaders == null || cacheHeaders.isEmpty () 
				? Collections.emptyMap ()
				: new HashMap<> (cacheHeaders);
		this.data = Objects.requireNonNull (data, "data cannot be null");
	}

	public MimeContentType getContentType () {
		return contentType;
	}

	public Map<String, String> getCacheHeaders () {
		return cacheHeaders;
	}

	public PublisherReference<ByteString> getData () {
		return data;
	}
}

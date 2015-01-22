package nl.idgis.geoide.commons.http.client.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.idgis.geoide.commons.http.client.HttpResponse;

import org.reactivestreams.Publisher;

import akka.util.ByteString;

public class DefaultHttpResponse implements HttpResponse {
	private final DefaultHttpClient client;
	private final int status;
	private final String statusText;
	private final Map<String, List<String>> headers;
	private final Publisher<ByteString> body;
	
	public DefaultHttpResponse (final DefaultHttpClient client, final int status, final String statusText, final Map<String, List<String>> headers, final Publisher<ByteString> body) {
		if (client == null) {
			throw new NullPointerException ("client cannot be null");
		}
		
		this.client = client;
		this.status = status;
		this.statusText = statusText;
		this.headers = new LinkedHashMap<> (headers == null ? Collections.<String, List<String>>emptyMap () : headers);
		this.body = body;
	}

	public DefaultHttpClient getClient () {
		return client;
	}

	public int getStatus () {
		return status;
	}

	public String getStatusText () {
		return statusText;
	}

	public Map<String, List<String>> getHeaders () {
		return headers;
	}

	public Publisher<ByteString> getBody () {
		return body;
	}
}

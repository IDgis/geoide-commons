package nl.idgis.geoide.commons.http.client.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.idgis.geoide.commons.http.client.HttpResponse;

import org.reactivestreams.Publisher;

import akka.util.ByteString;

/**
 * Implementation of {@link HttpResponse} for use with {@link DefaultHttpClient}.
 * 
 * {@inheritDoc}
 */
public class DefaultHttpResponse implements HttpResponse {
	private final DefaultHttpClient client;
	private final int status;
	private final String statusText;
	private final Map<String, List<String>> headers;
	private final Publisher<ByteString> body;

	/**
	 * Creates a new response for the given client by providing all attributes that make up a response.
	 * 
	 * @param client The client for which the response is created.
	 * @param status The numerical status code of the response.
	 * @param statusText The status text of the response.
	 * @param headers The response headers.
	 * @param body The publisher that produces the response body.
	 */
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

	/**
	 * Returns the client for which this response was created.
	 * 
	 * @return The client linked to this response.
	 */
	public DefaultHttpClient getClient () {
		return client;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStatus () {
		return status;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStatusText () {
		return statusText;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, List<String>> getHeaders () {
		return headers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Publisher<ByteString> getBody () {
		return body;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHeader (final String name) {
		if (name == null) {
			return null;
		}
		
		final String lcName = name.toLowerCase ();
		
		for (final Map.Entry<String, List<String>> entry: getHeaders ().entrySet ()) {
			if (!lcName.equals (entry.getKey ().toLowerCase ())) {
				continue;
			}
			
			final List<String> value = entry.getValue ();
			if (value == null || value.isEmpty ()) {
				continue;
			}
			
			return value.get (0);
		}
		
		return null;
	}
}

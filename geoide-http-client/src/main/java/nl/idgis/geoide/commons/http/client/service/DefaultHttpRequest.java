package nl.idgis.geoide.commons.http.client.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.idgis.geoide.commons.http.client.HttpRequest;

import org.reactivestreams.Publisher;

import akka.util.ByteString;

/**
 * Implementation of {@link HttpRequest} for use by {@link DefaultHttpClient}
 */
public class DefaultHttpRequest implements HttpRequest {
	
	private final DefaultHttpClient client;
	private final Method method;
	private final String url;
	private final long timeoutInMillis;
	private final boolean followRedirects;
	private final Map<String, List<String>> parameters;
	private final Map<String, List<String>> headers;
	private final Publisher<ByteString> body;
	
	DefaultHttpRequest (final DefaultHttpClient client, final Method method, final String url, final long timeoutInMillis, final boolean followRedirects, final Map<String, List<String>> parameters, final Map<String, List<String>> headers, final Publisher<ByteString> body) {
		if (client == null) {
			throw new NullPointerException ("client cannot be null");
		}
		if (method == null) {
			throw new NullPointerException ("method cannot be null");
		}
		if (url == null) {
			throw new NullPointerException ("url cannot be null");
		}

		this.client = client;
		this.method = method;
		this.url = url;
		this.timeoutInMillis = timeoutInMillis;
		this.followRedirects = followRedirects;
		this.parameters = new LinkedHashMap<> (parameters == null ? Collections.<String, List<String>>emptyMap () : parameters);
		this.headers = new LinkedHashMap<> (headers == null ? Collections.<String, List<String>>emptyMap () : headers);
		this.body = body;
	}

	/**
	 * Returns the client for which this request is created.
	 * 
	 * @return the client for which this request is created.
	 */
	public DefaultHttpClient getClient () {
		return client;
	}

	/**
	 * @see HttpRequest#getMethod()
	 */
	@Override
	public Method getMethod () {
		return method;
	}

	/**
	 * @see HttpRequest#getUrl()
	 */
	@Override
	public String getUrl () {
		return url;
	}

	/**
	 * @see HttpRequest#getTimeoutInMillis()
	 */
	@Override
	public long getTimeoutInMillis () {
		return timeoutInMillis;
	}

	/**
	 * @see HttpRequest#isFollowRedirects()
	 */
	@Override
	public boolean isFollowRedirects () {
		return followRedirects;
	}

	/**
	 * @see HttpRequest#getParameters()
	 */
	@Override
	public Map<String, List<String>> getParameters () {
		return parameters;
	}

	/**
	 * @see HttpRequest#getHeaders()
	 */
	@Override
	public Map<String, List<String>> getHeaders () {
		return headers;
	}

	/**
	 * @see HttpRequest#getBody()
	 */
	@Override
	public Publisher<ByteString> getBody () {
		return body;
	}
}
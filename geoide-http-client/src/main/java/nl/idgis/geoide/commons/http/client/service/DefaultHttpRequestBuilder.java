package nl.idgis.geoide.commons.http.client.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.idgis.geoide.commons.http.client.HttpRequest.Method;
import nl.idgis.geoide.commons.http.client.HttpResponse;
import nl.idgis.geoide.commons.http.client.HttpRequestBuilder;

import org.reactivestreams.Publisher;

import play.libs.F.Promise;
import akka.util.ByteString;

public class DefaultHttpRequestBuilder implements HttpRequestBuilder {

	private final DefaultHttpClient client;
	
	private String url = null;
	private Method method = Method.GET;
	private long timeoutInMillis = 60000;
	private boolean followRedirects = false;
	private final Map<String, List<String>> parameters = new LinkedHashMap<> ();
	private final Map<String, List<String>> headers = new LinkedHashMap<> ();
	private Publisher<ByteString> body = null;
	
	public DefaultHttpRequestBuilder (final DefaultHttpClient client) {
		if (client == null) {
			throw new NullPointerException ("client cannot be null");
		}
		
		this.client = client;
	}
	
	@Override
	public HttpRequestBuilder setUrl (final String url) {
		if (url == null) {
			throw new NullPointerException ("url cannot be null");
		}
		this.url = url;
		return this;
	}
	
	@Override
	public HttpRequestBuilder setMethod (final Method method) {
		if (method == null) {
			throw new NullPointerException ("method cannot be null");
		}
		
		this.method = method;
		return this;
	}

	@Override
	public HttpRequestBuilder setTimeoutInMillis (final long timeoutInMillis) {
		this.timeoutInMillis = timeoutInMillis;
		return this;
	}

	@Override
	public HttpRequestBuilder setFollowRedirects (final boolean followRedirects) {
		this.followRedirects = followRedirects;
		return this;
	}

	@Override
	public HttpRequestBuilder setParameter (final String name, final String value) {
		if (name == null) {
			throw new NullPointerException ("name cannot be null");
		}
		if (value == null) {
			throw new NullPointerException ("value cannot be null");
		}

		if (!parameters.containsKey (name)) {
			parameters.put (name, new ArrayList<String> ());
		}
		
		parameters.get (name).add (value);
		
		return this;
	}

	@Override
	public HttpRequestBuilder setHeader(String name, String value) {
		if (name == null) {
			throw new NullPointerException ("name cannot be null");
		}
		if (value == null) {
			throw new NullPointerException ("value cannot be null");
		}

		if (!headers.containsKey (name)) {
			headers.put (name, new ArrayList<String> ());
		}
		
		headers.get (name).add (value);
		
		return this;
	}

	@Override
	public HttpRequestBuilder setBody (final Publisher<ByteString> body) {
		this.body = body;
		return null;
	}

	@Override
	public Promise<HttpResponse> execute (final Method method) {
		return setMethod (method).execute ();
	}

	@Override
	public Promise<HttpResponse> execute () {
		final DefaultHttpRequest request = new DefaultHttpRequest (
				client, 
				method, 
				url, 
				timeoutInMillis, 
				followRedirects, 
				parameters, 
				headers, 
				body
			);
		
		return client.request (request);
	}

	@Override
	public Promise<HttpResponse> get () {
		return execute (Method.GET);
	}

	@Override
	public Promise<HttpResponse> post () {
		return execute (Method.POST);
	}

	@Override
	public Promise<HttpResponse> post (final Publisher<ByteString> body) {
		return setBody (body).execute (Method.POST);
	}
}

package nl.idgis.geoide.commons.http.client;

import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Publisher;

import akka.util.ByteString;

/**
 * Provides a fluent interface for configuring HTTP request. A request builder
 * is typically created by the {@link HttpClient#request()} method.
 */
public interface HttpRequestBuilder {

	/**
	 * @param url The URL for this request. Cannot be null.
	 * @return This builder.
	 */
	HttpRequestBuilder setUrl (String url);
	
	/**
	 * @param method The HTTP method for this request, cannot be null.
	 * @return This builder.
	 */
	HttpRequestBuilder setMethod (HttpRequest.Method method);
	
	/**
	 * @param timeoutInMillis The request timeout in milliseconds. Should be a positive value.
	 * @return This builder.
	 */
	HttpRequestBuilder setTimeoutInMillis (long timeoutInMillis);
	
	/**
	 * @param followRedirects Whether or not to follow redirects for this request.
	 * @return This builder.
	 */
	HttpRequestBuilder setFollowRedirects (boolean followRedirects);
	
	/**
	 * Adds a single parameter to this request. The order of parameters may be significant at the receiving end,
	 * therefore it is recommended that RequestBuilder implementations use a linked map or similar.
	 * 
	 * @param name The name of the parameter, cannot be null. Empty strings are not allowed.
	 * @param value The value of the parameter, cannot be null. An empty string is allowed.
	 * @return This builder.
	 */
	HttpRequestBuilder setParameter (String name, String value);
	
	/**
	 * Adds a single header to this request. The order of headers may be significant at the receiving end,
	 * therefore it is recommended that RequestBuilder implementations use a linked map or similar.
	 * 
	 * @param name The name of the header, cannot be null. Empty strings are not allowed.
	 * @param value The value of the header, cannot be null. An empty string is allowed.
	 * @return This builder.
	 */
	HttpRequestBuilder setHeader (String name, String value);
	
	/**
	 * Sets the body for this request. A null value indicates that the request has no body, this is also the default
	 * if no body is provided.
	 * 
	 * @param body The body for the request in the form of a reactive streams publisher.
	 * @return This builder.
	 */
	HttpRequestBuilder setBody (Publisher<ByteString> body);
	
	/**
	 * Executes the request described in this builder using the given method. This
	 * is similar to invoking {@link HttpRequestBuilder#setMethod(nl.idgis.geoide.commons.http.client.HttpRequest.Method)}
	 * followed by {@link HttpRequestBuilder#execute()}
	 * 
	 * @param method The HTTP method to perform.
	 * @return A promise that returns the HTTP response when it becomes available, or raises an exception in case of an error.
	 */
	CompletableFuture<HttpResponse> execute (HttpRequest.Method method);
	
	/**
	 * Executes the request described in this builder.
	 * 
	 * @return A promise that returns the HTTP response when it becomes available, or raises an exception in case of an error.
	 */
	CompletableFuture<HttpResponse> execute ();
	
	/**
	 * Executes the request described in this builder using the GET method. This
	 * is similar to invoking {@link HttpRequestBuilder#setMethod(nl.idgis.geoide.commons.http.client.HttpRequest.Method)} with the GET method
	 * followed by {@link HttpRequestBuilder#execute()}
	 * 
	 * @return A promise that returns the HTTP response when it becomes available, or raises an exception in case of an error.
	 */
	CompletableFuture<HttpResponse> get ();
	
	/**
	 * Executes the request described in this builder using the POST method. This
	 * is similar to invoking {@link HttpRequestBuilder#setMethod(nl.idgis.geoide.commons.http.client.HttpRequest.Method)} with the POST method
	 * followed by {@link HttpRequestBuilder#execute()}
	 * 
	 * @return A promise that returns the HTTP response when it becomes available, or raises an exception in case of an error.
	 */
	CompletableFuture<HttpResponse> post ();
	
	/**
	 * Executes the request described in this builder using the POST method with given body. This
	 * is similar to invoking {@link HttpRequestBuilder#setMethod(nl.idgis.geoide.commons.http.client.HttpRequest.Method)} with the POST method,
	 * followed by {@link HttpRequestBuilder#setBody(Publisher)}, followed by {@link HttpRequestBuilder#execute()}.
	 * 
	 * @return A promise that returns the HTTP response when it becomes available, or raises an exception in case of an error.
	 */
	CompletableFuture<HttpResponse> post (Publisher<ByteString> body);
}

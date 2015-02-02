package nl.idgis.geoide.commons.http.client;

import play.libs.F.Promise;

/**
 * Generic HTTP client interface. An implementation of this interface is
 * used for all outbound HTTP traffic from Geoide commons.
 */
public interface HttpClient {

	/**
	 * Constructs a {@link HttpRequestBuilder} on this client that can be used to
	 * build and execute a request using fluent syntax.
	 * 
	 * @return A HttpRequestBuilder that is configured to execute requests on this HttpClient.
	 */
	HttpRequestBuilder request ();
	
	/**
	 * Constructs a {@link HttpRequestBuilder} on this client that can be used to
	 * build and execute a request using fluent syntax for the given url.
	 * 
	 * @param url The URL to include in the request.
	 * @return A HttpRequestBuilder that is configured to execute requests on this HttpClient.
	 */
	HttpRequestBuilder url (String url);

	/**
	 * Performs a request on this HttpClient.
	 * 
	 * @param request The request to execute.
	 * @return A promise that returns the response when it becomes available, or raises an exception in case of error.
	 */
	Promise<HttpResponse> request (HttpRequest request);
}

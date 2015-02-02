package nl.idgis.geoide.commons.http.client;

import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;

import akka.util.ByteString;

/**
 * The HttpRequest interface describes the parameters for a request
 * that is to be peformed on {@link HttpClient}. Each HttpClient interface
 * can use a specific implementation of HttpRequest, created indirectly
 * by using {@link HttpClient#request()}. 
 */
public interface HttpRequest {
	
	/**
	 * Returns the HTTP method to be used for this request. A method must always be set
	 * and should default to GET.
	 * 
	 * @return The HTTP method for this request.
	 */
	Method getMethod ();
	
	/**
	 * Returns the URL for this request. An URL must always be set.
	 * 
	 * @return The URL for this request.
	 */
	String getUrl ();
	
	/**
	 * Returns the request timeout in milliseconds. This should default to a
	 * HttpClient implementation specific value and can be overriden using
	 * a {@link HttpRequestBuilder}.
	 * 
	 * @return The request timeout in milliseconds.
	 */
	long getTimeoutInMillis ();
	
	/**
	 * Returns true if redirects should be followed when executing the request. Since
	 * this is a potentially dangerous operation, the default set by {@link HttpClient} implementations
	 * should be false.
	 * 
	 * @return True if redirects should be followed, false otherwise.
	 */
	boolean isFollowRedirects ();
	
	/**
	 * Returns the query parameters for the request, or an empty map if the request has no parameters.
	 * Since the order of parameters can be significant at the receiving end it is recommended that {@link HttpClient} implementations
	 * use a linked map (or similar) for parameters. 
	 * 
	 * @return A map containing query parameters, or an empty map if the request has no parameters. Cannot return null.
	 */
	Map<String, List<String>> getParameters ();
	
	/**
	 * Returns the headers for the request, or an empty map if the request has no headers.
	 * Since the order of headers can be significant at the receiving end it is recommended that {@link HttpClient} implementations
	 * use a linked map (or similar) for headers.
	 * 
	 * @return A map containing headers, or an empty map if the request has no headers. Cannot return null.
	 */
	Map<String, List<String>> getHeaders ();

	/**
	 * Returns the body of the request in the form of a reactive streams publisher of ByteStrings. Can return null
	 * if the request has no body (this is the case with most GET requests for example).
	 * 
	 * @return A publisher for the request body, or null if the request has no body.
	 */
	Publisher<ByteString> getBody ();

	/**
	 * Enum containing all supported HTTP request methods. Names of elements in this
	 * enum match the names of the corresponding methods.
	 */
	public static enum Method {
		GET,
		PATCH,
		POST,
		PUT,
		DELETE,
		HEAD,
		OPTIONS
	}
}

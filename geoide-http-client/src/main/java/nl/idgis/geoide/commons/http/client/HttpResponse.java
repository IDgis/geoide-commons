package nl.idgis.geoide.commons.http.client;

import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;

import akka.util.ByteString;

/**
 * Describes the response of executing a {@link HttpRequest} using an {@link HttpClient}.
 */
public interface HttpResponse {
	
	/**
	 * Returns the numerical status code of the response.
	 * 
	 * @return The response status code.
	 */
	int getStatus ();
	
	/**
	 * Returns the status text, complementing the status code of the response.
	 * 
	 * @return The response status text.
	 */
	String getStatusText ();
	
	/**
	 * Returns the headers that were set during the response. The order of headers may be significant,
	 * therefore implementations of {@link HttpClient} should use a linked map or similar to preserve
	 * iteration order.
	 * 
	 * @return A map containing the headers of the response.
	 */
	Map<String, List<String>> getHeaders ();
	
	/**
	 * Returns the body of the response in the form of a reactive streams publisher that produces
	 * ByteStrings.
	 * 
	 * @return The response body in the form of a reactive streams publisher of byte strings.
	 */
	Publisher<ByteString> getBody ();

	/**
	 * Returns a single header from the request. If the given header is set multiple times, the first occurrence is returned.
	 * Header names are treated as case insensitive.
	 * 
	 * @param name The name of the header to request.
	 * @return The header value, or null if the response didn't specify the requested header.
	 */
	String getHeader (String name);
}

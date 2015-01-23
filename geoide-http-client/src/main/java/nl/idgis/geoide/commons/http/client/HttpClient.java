package nl.idgis.geoide.commons.http.client;

import play.libs.F.Promise;

public interface HttpClient {

	HttpRequestBuilder request ();
	HttpRequestBuilder url (String url);
	Promise<HttpResponse> request (HttpRequest request);
}

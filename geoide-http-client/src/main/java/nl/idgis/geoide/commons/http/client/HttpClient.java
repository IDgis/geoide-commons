package nl.idgis.geoide.commons.http.client;

import play.libs.F.Promise;

public interface HttpClient {

	Promise<HttpResponse> request (HttpRequest request);
}

package nl.idgis.geoide.documentcache;

import java.net.URI;

import play.libs.F.Promise;

public interface DocumentStore {
	Promise<Document> fetch (final URI uri);
}

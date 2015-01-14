package nl.idgis.geoide.documentcache;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;

import nl.idgis.ogc.util.MimeContentType;

public interface CachedDocument extends Serializable {
	URI getUri ();
	MimeContentType getContentType ();
	InputStream asInputStream ();
}

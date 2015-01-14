package nl.idgis.geoide.documentcache;

import java.io.InputStream;
import java.net.URI;

import nl.idgis.ogc.util.MimeContentType;
import akka.util.ByteString;

public final class ByteStringCachedDocument implements CachedDocument {
	private static final long serialVersionUID = -7356469008686508656L;
	
	private final URI uri;
	private final MimeContentType contentType;
	private final ByteString data;
	
	public ByteStringCachedDocument (final URI uri, final MimeContentType contentType, final ByteString data) {
		if (uri == null) {
			throw new NullPointerException ("uri cannot be null");
		}
		if (contentType == null) {
			throw new NullPointerException ("contentType cannot be null");
		}
		if (data == null) {
			throw new NullPointerException ("data cannot be null");
		}
		
		this.uri = uri;
		this.contentType = contentType;
		this.data = data;
	}

	@Override
	public URI getUri () {
		return uri;
	}

	@Override
	public MimeContentType getContentType () {
		return contentType;
	}
	
	@Override
	public InputStream asInputStream () {
		return data.iterator ().asInputStream ();
	}
}

package nl.idgis.geoide.service.messages;

import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.service.messages.ServiceMessage;
import nl.idgis.geoide.commons.domain.service.messages.ServiceMessageContext;
import nl.idgis.services.OGCCapabilities;
import akka.util.ByteString;

public final class OGCServiceResponse extends ServiceMessage {
	private static final long serialVersionUID = 6873695070106625338L;

	private final OGCCapabilities capabilities;
	private final String contentType;
	private final ByteString data;
	private final String url;
	
	public OGCServiceResponse (final ServiceIdentification identification, final OGCCapabilities capabilities, final String contentType, final ByteString data, final ServiceMessageContext context, final String url) {
		super (identification, context);

		if (capabilities == null) {
			throw new NullPointerException ("capabilities cannot be null");
		}
		if (contentType == null) {
			throw new NullPointerException ("contentType cannot be null");
		}
		if (data == null) {
			throw new NullPointerException ("data cannot be null");
		}
		if (url == null) {
			throw new NullPointerException ("url cannot be null");
		}

		this.capabilities = capabilities;
		this.contentType = contentType;
		this.data = data;
		this.url = url;
	}

	public OGCCapabilities capabilities () {
		return capabilities;
	}
	
	public String contentType () {
		return contentType;
	}
	
	public ByteString data () {
		return data;
	}
	
	public String url () {
		return url;
	}
}
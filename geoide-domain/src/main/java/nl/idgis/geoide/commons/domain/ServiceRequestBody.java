package nl.idgis.geoide.commons.domain;

import java.io.Serializable;

import akka.util.ByteString;

public class ServiceRequestBody implements Serializable {
	private static final long serialVersionUID = 196410827167441350L;
	
	private final String contentType;
	private final ByteString content;
	
	public ServiceRequestBody (final String contentType, final ByteString content) {
		this.contentType = contentType;
		this.content = content;
	}

	public String getContentType () {
		return contentType;
	}

	public ByteString getContent () {
		return content;
	}
}

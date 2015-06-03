package nl.idgis.geoide.commons.domain.service.messages;

public enum ServiceErrorType {
	EXCEPTION,				// An exception occurred while handling the request.
	UNSUPPORTED_OPERATION,	// The given message type is not supported by the service.
	INVALID_URL,			// The service endpoint/URL is invalid.
	TIMEOUT,				// The service request produced a timeout.
	HTTP_ERROR,				// The service returned a HTTP error status (e.g. 404).
	FORMAT_ERROR,			// Unable to parse the response from the service.
	
	SERVICE_ERROR			// The service returned an error message.
}
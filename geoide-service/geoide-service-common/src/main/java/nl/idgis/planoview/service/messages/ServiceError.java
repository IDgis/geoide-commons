package nl.idgis.planoview.service.messages;

import nl.idgis.planoview.commons.domain.ServiceIdentification;

public final class ServiceError extends ServiceMessage {
	private static final long serialVersionUID = 4544525612462354468L;
	
	private final ServiceErrorType errorType;
	private final String url;
	private final String message;
	private final Throwable cause;
	
	public ServiceError (final ServiceIdentification serviceIdentification, final ServiceErrorType errorType, final String url, final String message, final Throwable cause, final ServiceMessageContext context) {
		super(serviceIdentification, context);
		
		if (errorType == null) {
			throw new NullPointerException ("errorType cannot be null");
		}
		if (url == null) {
			throw new NullPointerException ("url cannot be null");
		}
		
		this.errorType = errorType;
		this.url = url;
		this.message = message;
		this.cause = cause;
	}
	
	public ServiceErrorType errorType () {
		return errorType;
	}
	
	public String url () {
		return url;
	}
	
	public String message () {
		return message;
	}
	
	public Throwable cause () {
		return cause;
	}
	
	@Override
	public String toString () {
		return String.format ("Service %s raised an error %s with request: %s Message: %s", serviceIdentification ().toString (), errorType.toString (), url, message == null ? "-" : message);			
	}
}
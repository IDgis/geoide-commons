package nl.idgis.geoide.commons.remote;

public class RemoteServiceException extends Exception {
	
	private static final long serialVersionUID = 3155066636560126450L;

	public RemoteServiceException (final String message) {
		super (message);
	}
	
	public RemoteServiceException (final String message, final Throwable cause) {
		super (message, cause);
	}
	
	public RemoteServiceException (final Throwable cause) {
		super (cause);
	}
}

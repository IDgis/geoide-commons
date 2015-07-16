package nl.idgis.geoide.util.streams.messages;

import java.io.Serializable;

public class SubscriberError implements Serializable {
	private static final long serialVersionUID = 7218612154741471172L;
	
	private final Throwable throwable;
	
	public SubscriberError (final Throwable throwable) {
		if (throwable == null) {
			throw new NullPointerException ("throwable cannot be null");
		}
		
		this.throwable = throwable;
	}

	public Throwable getThrowable () {
		return throwable;
	}
}

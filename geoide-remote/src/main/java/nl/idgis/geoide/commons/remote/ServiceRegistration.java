package nl.idgis.geoide.commons.remote;

/**
 * Registration of a service implementation for use with the {@link RemoteMethodServer}.
 *
 * @param <T>	The interface that is provided by this service registration.
 */
public class ServiceRegistration<T> {

	private final Class<T> cls;
	private final T object;
	private final String classifier;

	/**
	 * @param cls			The interface that is provided by this registration. Must be an interface and cannot be null.
	 * @param object		The concrete implementation of the interface the remote method server should delegate to. Cannot be null and must be an implementation of cls.
	 * @param classifier	An optional classifier used to identify the implementation. Use a classifier if multiple implementations of the same interface are registered. Can be null.
	 */
	public ServiceRegistration (final Class<T> cls, final T object, final String classifier) {
		if (cls == null) {
			throw new NullPointerException ("cls cannot be null");
		}
		if (object == null) {
			throw new NullPointerException ("object cannot be null");
		}
		if (!(cls.isAssignableFrom (object.getClass ()))) {
			throw new NullPointerException ("object should be an instance of " + cls.getCanonicalName () + " (is " + object.getClass ().getCanonicalName () + ")");
		}
		
		this.cls = cls;
		this.object = object;
		this.classifier = classifier;
	}

	/**
	 * @return	The interface this service registration provides an implementation for. 
	 */
	public Class<T> getCls () {
		return cls;
	}

	/**
	 * @return The service implementation.
	 */
	public T getObject () {
		return object;
	}

	/**
	 * @return The optional classifier of this registration, or null.
	 */
	public String getClassifier () {
		return classifier;
	}
}

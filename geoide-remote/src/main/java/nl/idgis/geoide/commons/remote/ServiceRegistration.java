package nl.idgis.geoide.commons.remote;

public class ServiceRegistration<T> {

	private final Class<T> cls;
	private final T object;
	private final String classifier;
	
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

	public Class<T> getCls () {
		return cls;
	}

	public T getObject () {
		return object;
	}

	public String getClassifier () {
		return classifier;
	}
}

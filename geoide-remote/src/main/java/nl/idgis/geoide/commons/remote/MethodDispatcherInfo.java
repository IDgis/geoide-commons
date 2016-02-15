package nl.idgis.geoide.commons.remote;

import java.lang.invoke.MethodHandle;

/**
 * Utility class that stores information used to delegate to an interface method.
 * Links a serializable {@link MethodReference} to a standard java {@link MethodHandle}. 
 */
public class MethodDispatcherInfo {
	
	private final MethodReference methodReference;
	private final MethodHandle methodHandle;

	/**
	 * Constructs a new {@link MethodDispatcherInfo}.
	 * 
	 * @param methodReference	The serializable method reference. Cannot be null.
	 * @param methodHandle		The method handle (java reflections). Cannot be null.
	 */
	public MethodDispatcherInfo (final MethodReference methodReference, final MethodHandle methodHandle) {
		if (methodReference == null) {
			throw new NullPointerException ("methodReference cannot be null");
		}
		if (methodHandle == null) {
			throw new NullPointerException ("methodHandle cannot be null");
		}
		
		this.methodReference = methodReference;
		this.methodHandle = methodHandle;
	}

	/**
	 * @return	The method reference.
	 */
	public MethodReference getMethodReference () {
		return methodReference;
	}

	/**
	 * @return	The method handle.
	 */
	public MethodHandle getMethodHandle () {
		return methodHandle;
	}
}

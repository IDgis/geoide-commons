package nl.idgis.geoide.commons.remote;

import java.lang.invoke.MethodHandle;

public class MethodDispatcher {
	
	private final MethodReference methodReference;
	private final MethodHandle methodHandle;
	
	public MethodDispatcher (final MethodReference methodReference, final MethodHandle methodHandle) {
		if (methodReference == null) {
			throw new NullPointerException ("methodReference cannot be null");
		}
		if (methodHandle == null) {
			throw new NullPointerException ("methodHandle cannot be null");
		}
		
		this.methodReference = methodReference;
		this.methodHandle = methodHandle;
	}

	public MethodReference getMethodReference () {
		return methodReference;
	}

	public MethodHandle getMethodHandle () {
		return methodHandle;
	}
}

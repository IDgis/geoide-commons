package nl.idgis.geoide.commons.remote;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RemoteServiceFactory {

	public <T> T createServiceReference (final Class<T> cls) {
		if (!cls.isInterface ()) {
			throw new IllegalArgumentException ("Class " + cls.getCanonicalName () + " should be an interface.");
		}
		
		final Map<String, MethodDispatcher> methodDispatchers = new HashMap<> ();
		
		for (final Method method: cls.getMethods ()) {
			methodDispatchers.put (method.getName (), createMethodDispatcher (cls, method));
		}
		
		return null;
	}
	
	public <T> MethodDispatcher createMethodDispatcher (final Class<T> cls, final Method method) {
		// The method should return CompletableFuture:
		if (!CompletableFuture.class.isAssignableFrom (method.getReturnType ())) {
			throw new IllegalArgumentException ("Method " + cls.getCanonicalName () + "#" + method.getName () + " should return " + CompletableFuture.class.getCanonicalName ());
		}
		
		return new MethodDispatcher ();
	}
	
	private boolean isTypeSerializable (final Type type) {
		if (type instanceof Class) {
			return Serializable.class.isAssignableFrom ((Class<?>) type);
		} else if (type instanceof GenericArrayType) {
			return isTypeSerializable (((GenericArrayType) type).getGenericComponentType ());
		} else if (type instanceof ParameterizedType) {
			return isTypeSerializable (((ParameterizedType) type).getRawType ());
		}

		return false;
	}
}

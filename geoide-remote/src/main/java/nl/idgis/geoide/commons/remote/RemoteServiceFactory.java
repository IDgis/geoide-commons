package nl.idgis.geoide.commons.remote;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RemoteServiceFactory {

	private final RemoteMethodClient client;
	
	public RemoteServiceFactory (final RemoteMethodClient client) {
		if (client == null) {
			throw new NullPointerException ("client cannot be null");
		}
		
		this.client = client;
	}
	
	public <T> T createServiceReference (final Class<T> cls) {
		return createServiceReference (cls, null);
	}
	
	public <T> T createServiceReference (final Class<T> cls, final String qualifier) {
		if (!cls.isInterface ()) {
			throw new IllegalArgumentException ("Class " + cls.getCanonicalName () + " should be an interface.");
		}
		
		final Map<Method, MethodDispatcher> methodDispatchers = new HashMap<> ();
		
		for (final Method method: cls.getMethods ()) {
			methodDispatchers.put (method, createMethodDispatcher (cls, method));
		}
		
		final ProxyDispatcher<T> proxyDispatcher = new ProxyDispatcher<> (cls, methodDispatchers);
		
		return proxyDispatcher.createProxy (client, qualifier);
	}
	
	public <T> MethodDispatcher createMethodDispatcher (final Class<T> cls, final Method method) {
		// The method should return CompletableFuture:
		if (!CompletableFuture.class.isAssignableFrom (method.getReturnType ())) {
			throw new IllegalArgumentException ("Method " + cls.getCanonicalName () + "#" + method.getName () + " should return " + CompletableFuture.class.getCanonicalName ());
		}
		
		try {
			final MethodHandle methodHandle = MethodHandles.lookup ().unreflect (method);
			
			return new MethodDispatcher (new MethodReference (cls, method.getName (), Arrays.asList (method.getParameterTypes ())), methodHandle);
		} catch (IllegalAccessException e) {
			throw new RuntimeException (e);
		}
	}
}

package nl.idgis.geoide.commons.remote;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProxyDispatcher<T> {

	private final Class<T> cls;
	private final Map<Method, MethodDispatcher> methodDispatchers;
	
	public ProxyDispatcher (final Class<T> cls, final Map<Method, MethodDispatcher> methodDispatchers) {
		if (cls == null) {
			throw new NullPointerException ("cls cannot be null");
		}
		if (methodDispatchers == null) {
			throw new NullPointerException ("methodDispatchers cannot be null");
		}
		
		this.cls = cls;
		this.methodDispatchers = new HashMap<> (methodDispatchers);
	}
	
	public T createProxy (final RemoteMethodClient client, final String qualifier) {
		@SuppressWarnings("unchecked")
		final T proxy = (T) Proxy.newProxyInstance (cls.getClassLoader (), new Class<?>[] { cls }, (proxyObject, method, args) -> {
			final MethodDispatcher dispatcher = methodDispatchers.get (method);
			if (dispatcher == null) {
				throw new RuntimeException ("Unknown method: " + method.toString ());
			}
			
			final RemoteMethodCall call = new RemoteMethodCall (cls, qualifier, dispatcher.getMethodReference (), Arrays.asList (args));
			
			return (Object) client.invokeMethod (call);
		});
		
		return proxy;
	}
}

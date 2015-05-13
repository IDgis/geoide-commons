package nl.idgis.geoide.commons.remote;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DispatcherInfo<T> {

	private final Class<T> cls;
	private final Map<Method, MethodDispatcherInfo> methodDispatchers;
	
	public DispatcherInfo (final Class<T> cls, final Map<Method, MethodDispatcherInfo> methodDispatchers) {
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
			final MethodDispatcherInfo dispatcher = methodDispatchers.get (method);
			if (dispatcher == null) {
				throw new RuntimeException ("Unknown method: " + method.toString ());
			}
			
			final RemoteMethodCall call = new RemoteMethodCall (cls, qualifier, dispatcher.getMethodReference (), Arrays.asList (args));
			
			return (Object) client.invokeMethod (call);
		});
		
		return proxy;
	}
	
	public Collection<MethodDispatcherInfo> getMethodDispatcherInfos () {
		return Collections.unmodifiableCollection (methodDispatchers.values ());
	}
}

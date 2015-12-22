package nl.idgis.geoide.commons.remote;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class that contains method dispatchers for a given interface. A dispatcher contains information
 * used by the proxies to delegate method calls to and from interface methods.
 *
 * @param <T>	The interface for which the method dispatchers are created.
 */
public class DispatcherInfo<T> {

	private final Class<T> cls;
	private final Map<Method, MethodDispatcherInfo> methodDispatchers;

	/**
	 * Creates a DispatcherInfo for the given interface.
	 * 
	 * @param cls				The interface. Must be an interface and cannot be null.
	 * @param methodDispatchers	Map containing the {@link MethodDispatcherInfo} objects for the methods in the interface.
	 */
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
	
	/**
	 * Creates a proxy that delegates all calls to interface methods to the given {@link RemoteMethodClient},
	 * optionally passing along an additional qualifier with the requests.
	 * 
	 * @param client		The {@link RemoteMethodClient} to delegate calls to.
	 * @param qualifier		An optional qualifier to identify the interface implementation at the remote end.
	 * @return				A proxy implementation of the interface.
	 */
	public T createProxy (final RemoteMethodClient client, final String qualifier) {
		@SuppressWarnings("unchecked")
		final T proxy = (T) Proxy.newProxyInstance (cls.getClassLoader (), new Class<?>[] { cls }, (proxyObject, method, args) -> {
			final MethodDispatcherInfo dispatcher = methodDispatchers.get (method);
			if (dispatcher == null) {
				throw new RuntimeException ("Unknown method: " + method.toString ());
			}
			
			final RemoteMethodCall call = new RemoteMethodCall (cls, qualifier, dispatcher.getMethodReference (), args == null ? Collections.emptyList() : Arrays.asList (args));
			
			return (Object) client.invokeMethod (call);
		});
		
		return proxy;
	}

	/**
	 * @return	The {@link MethodDispatcherInfo}'s stored in this DispatcherInfo.
	 */
	public Collection<MethodDispatcherInfo> getMethodDispatcherInfos () {
		return Collections.unmodifiableCollection (methodDispatchers.values ());
	}
}

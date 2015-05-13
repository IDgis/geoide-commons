package nl.idgis.geoide.commons.remote;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RemoteServiceFactory {

	public RemoteServiceFactory () {
	}
	
	public <T> T createServiceReference (final RemoteMethodClient client, final Class<T> cls) {
		return createServiceReference (client, cls, null);
	}
	
	public <T> T createServiceReference (final RemoteMethodClient client, final Class<T> cls, final String qualifier) {
		return createDispatcherInfo (cls).createProxy (client, qualifier);
	}
	
	private <T> DispatcherInfo<T> createDispatcherInfo (final Class<T> cls) {
		if (!cls.isInterface ()) {
			throw new IllegalArgumentException ("Class " + cls.getCanonicalName () + " should be an interface.");
		}
		
		final Map<Method, MethodDispatcherInfo> methodDispatchers = new HashMap<> ();
		
		for (final Method method: cls.getMethods ()) {
			methodDispatchers.put (method, createMethodDispatcherInfo (cls, method));
		}
		
		return new DispatcherInfo<> (cls, methodDispatchers);
	}
	
	private <T> MethodDispatcherInfo createMethodDispatcherInfo (final Class<T> cls, final Method method) {
		// The method should return CompletableFuture:
		if (!CompletableFuture.class.isAssignableFrom (method.getReturnType ())) {
			throw new IllegalArgumentException ("Method " + cls.getCanonicalName () + "#" + method.getName () + " should return " + CompletableFuture.class.getCanonicalName ());
		}
		
		try {
			final MethodHandle methodHandle = MethodHandles.lookup ().unreflect (method);
			
			return new MethodDispatcherInfo (new MethodReference (cls, method.getName (), Arrays.asList (method.getParameterTypes ())), methodHandle);
		} catch (IllegalAccessException e) {
			throw new RuntimeException (e);
		}
	}
	
	public RemoteMethodServer createRemoteMethodServer (final ServiceRegistration<?> ... serviceRegistrations) {
		final Map<MethodWithClassifier, MethodHandle> registrations = new HashMap<> ();
		
		for (final ServiceRegistration<?> registration: serviceRegistrations) {
			final Class<?> cls = registration.getCls ();
			final DispatcherInfo<?> dispatcherInfo = createDispatcherInfo (cls);
			
			for (final MethodDispatcherInfo methodInfo: dispatcherInfo.getMethodDispatcherInfos ()) {
				registrations.put (
					new MethodWithClassifier (
						methodInfo.getMethodReference (), 
						registration.getClassifier ()
					), 
					methodInfo.getMethodHandle ().bindTo (registration.getObject ())
				);
			}
		}
		
		return (remoteMethodCall) -> {
			final MethodHandle invoker = registrations.get (
					new MethodWithClassifier (
						remoteMethodCall.getMethodReference (), 
						remoteMethodCall.getQualifier ()
					)
				);
			
			if (invoker == null) {
				final CompletableFuture<Object> future = new CompletableFuture<> ();
				future.completeExceptionally (new RemoteServiceException ("No mapping for method " + remoteMethodCall.getMethodReference ()));
				return future;
			}
			
			try {
				final CompletableFuture<?> result = (CompletableFuture<?>) invoker.invokeWithArguments (remoteMethodCall.getArguments ());
				return result;
			} catch (Throwable e) {
				final CompletableFuture<Object> future = new CompletableFuture<> ();
				future.completeExceptionally (e);
				return future;
			}
		};
	}
	
	private final static class MethodWithClassifier {
		public final MethodReference methodReference;
		public final String classifier;
		
		public MethodWithClassifier (final MethodReference methodReference, final String classifier) {
			this.methodReference = methodReference;
			this.classifier = classifier;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((classifier == null) ? 0 : classifier.hashCode());
			result = prime
					* result
					+ ((methodReference == null) ? 0 : methodReference
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MethodWithClassifier other = (MethodWithClassifier) obj;
			if (classifier == null) {
				if (other.classifier != null)
					return false;
			} else if (!classifier.equals(other.classifier))
				return false;
			if (methodReference == null) {
				if (other.methodReference != null)
					return false;
			} else if (!methodReference.equals(other.methodReference))
				return false;
			return true;
		}
	}
}

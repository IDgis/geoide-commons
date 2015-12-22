package nl.idgis.geoide.commons.remote;

import java.util.concurrent.CompletableFuture;

/**
 * Interface that must be implemented by a remote server implementation. Accepts {@link RemoteMethodCall} 
 * instances that must be dispatched to a concrete implementation.
 */
@FunctionalInterface
public interface RemoteMethodServer {
	/**
	 * Dispatches the given {@link RemoteMethodCall} to a concrete implementation and
	 * provides the result as a {@link CompletableFuture}. Exceptions should be reported through the
	 * returned future.
	 * 
	 * @param call	The remote method call to execute.
	 * @return		A {@link CompletableFuture} that provides the result of the invocation.
	 */
	CompletableFuture<?> invokeMethod (RemoteMethodCall call);
}

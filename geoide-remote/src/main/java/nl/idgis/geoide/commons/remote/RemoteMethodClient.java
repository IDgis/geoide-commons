package nl.idgis.geoide.commons.remote;

import java.util.concurrent.CompletableFuture;

/**
 * Client for remote method invocations. Implementors should be able to dispatch
 * a {@link RemoteMethodCall} message containing information about the method call
 * to a remote destination and return a {@link CompletableFuture} for the result.
 */
@FunctionalInterface
public interface RemoteMethodClient {

	/**
	 * This method is invoked when the client is requested to perform a {@link RemoteMethodCall}.
	 * The client is responsible for transporting the message to the remote server and return
	 * a CompletableFuture for the result. Exceptions should be reported through the
	 * {@link CompletableFuture}.
	 *  
	 * @param call	The remote method call to perform.
	 * @return		A {@link CompletableFuture} containing the result of the method call. May not return null.
	 */
	CompletableFuture<?> invokeMethod (RemoteMethodCall call);
}

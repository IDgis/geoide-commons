package nl.idgis.geoide.commons.remote;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface RemoteMethodClient {

	CompletableFuture<Object> invokeMethod (RemoteMethodCall call);
}

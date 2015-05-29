package nl.idgis.geoide.commons.remote;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface RemoteMethodServer {
	CompletableFuture<?> invokeMethod (RemoteMethodCall call);
}

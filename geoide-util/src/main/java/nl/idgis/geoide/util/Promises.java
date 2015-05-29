package nl.idgis.geoide.util;

import java.util.concurrent.CompletableFuture;

import play.libs.F.Promise;
import play.libs.F.RedeemablePromise;

public final class Promises {
	
	/**
	 * Converts a CompletableFuture into a Play promise.
	 * 
	 * @param future	The future to wrap.
	 * @return			A Play promise that wraps the given CompletableFuture.
	 */
	public static <T> Promise<T> asPromise (final CompletableFuture<T> future) {
		final RedeemablePromise<T> promise = RedeemablePromise.empty ();
		
		future.handle ((T object, Throwable t) -> {
			if (t != null) {
				promise.failure (t);
			} else {
				promise.success (object);
			}
			
			return null;
		});
		
		return promise;
	}
}

package nl.idgis.geoide.util;

import java.util.concurrent.CompletableFuture;

import akka.dispatch.OnComplete;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

public class AkkaFutures {

	/**
	 * Transforms an Akka / Scala future into a Java CompletableFuture.
	 * 
	 * @param future	The scala future to wrap.
	 * @param context	The execution context to use.
	 * @return			A Java CompletableFuture that wraps the given scala future.
	 */
	public static <T> CompletableFuture<T> asCompletableFuture (final Future<T> future, final ExecutionContext context) {
		final CompletableFuture<T> result = new CompletableFuture<> ();
		
		future.onComplete (new OnComplete<T> () {
			@Override
			public void onComplete (final Throwable t, final T object) throws Throwable {
				if (t != null) {
					result.completeExceptionally (t);
				} else {
					result.complete (object);
				}
			}
		}, context);
		
		return result;
	}
}

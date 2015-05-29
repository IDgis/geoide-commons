package nl.idgis.geoide.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public final class Futures {
	
	/**
	 * Creates a completable future that is completed and raises the given exception.
	 * 
	 * @param t	The exception to raise from the future.
	 * @return A completed future.
	 */
	public static <T> CompletableFuture<T> throwing (final Throwable t) {
		final CompletableFuture<T> future = new CompletableFuture<> ();
		
		future.completeExceptionally (t);
		
		return future;
	}

	/**
	 * Returns a future that returns the result of a list of futures. If one of the futures completes
	 * exceptionally, so does the future that is returned by this method. The first exception that is received
	 * is raised by the future.
	 * 
	 * @param futures The futures to wait for.
	 * @return A new future that completes when all given futures have completed.
	 */
	public static <T> CompletableFuture<List<T>> all (final List<CompletableFuture<T>> futures) {
		if (futures == null) {
			throw new NullPointerException ("futures cannot be null");
		}
		
		@SuppressWarnings("unchecked")
		final CompletableFuture<T>[] futuresArray = futures.stream ().toArray (CompletableFuture[]::new);
		
		return CompletableFuture
			.allOf (futuresArray)
			.thenApply ((o) -> futures
				.stream ()
				.map ((f) -> f.getNow (null))
				.collect (Collectors.toList ()));
	}
	
	public static <T> T get (final CompletableFuture<T> future, final long timeoutInMillis) throws Throwable {
		final T value;
		
		try {
			value = future.get (timeoutInMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw e;
		} catch (ExecutionException e) {
			throw e.getCause ();
		} catch (TimeoutException e) {
			throw e;
		}
		
		return value;
	}
}

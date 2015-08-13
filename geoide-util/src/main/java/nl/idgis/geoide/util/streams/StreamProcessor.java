package nl.idgis.geoide.util.streams;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import akka.util.ByteString;

/**
 * The StreamProcessor interface provides generic utilities to work with reactive streams. Most operations
 * provided by a stream processor require a scheduler to perform the operations in an asynchronous manner.
 * By implementing the StreamProcessor interface a specific scheduler can be used to process streams.
 * 
 * Operations provided by StreamProcessor also include conversions to and from "legacy" streams (the
 * InputStream and OutputStream family).
 */
public interface StreamProcessor {

	/**
	 * Performs a "reduce" operation on the elements in the stream. Starting with a given initial value, 
	 * the reducer function is invoked with the initial value and the first element (if any). Then all subsequent
	 * elements in the stream are processed by invoking the reducer with the result of the previous operation and
	 * the next element in the stream. Returns the result of the last reduce call, or the initial value if
	 * the publisher didn't produce any elements.
	 * 
	 * @param publisher The reactive publisher that produces the elements to reduce. Cannot be null.
	 * @param initialValue The initial value to use. Can be null, if the reducer can deal with null values.
	 * @param reducer The reducer function to invoke for each pair of values. Cannot be null.
	 * @return The result of reducing all elements in the stream, or initialValue if the producer didn't produce any elements.
	 */
	<T> CompletableFuture<T> reduce (Publisher<T> publisher, T initialValue, BiFunction<T, T, T> reducer);
	
	/**
	 * Creates a publisher that when subscribed to always produces a stream containing a single value.
	 * The publisher never produces errors.
	 *  
	 * @param value The value to return from the created publisher.
	 * @return A publisher that publishes only value.
	 */
	<T> Publisher<T> publishSinglevalue (T value);

	/**
	 * Utility to convert a legacy input stream into a Publisher that returns a stream of ByteStrings. The maximum
	 * size of the ByteStrings can be controlled usint maxBlockSize. A read timeout should be set with timeoutInMillis.
	 * After the timeout expires the input stream is closed and the publisher produces an IOException.
	 * 
	 * @param inputStream The InputStream to turn into a publisher.
	 * @param maxBlockSize The maximum number of bytes to read from the input stream and to send in each ByteString from the publisher.
	 * @param timeoutInMillis The timeout before the input stream is closed if there is no activity on a subscription to the publisher.
	 * @return A publisher that publishes the given InputStream as a sequence of ByteStrings.
	 */
	Publisher<ByteString> publishInputStream (InputStream inputStream, int maxBlockSize, long timeoutInMillis);
	
	/**
	 * Utility to turn a publisher of ByteStrings into a "legacy" InputStream. Reading from the returned InputStream is
	 * a blocking operation: the thread that invokes the "read" method is blocked until more bytes become available in
	 * the publisher. Use this method with care since this results in blocking operations.
	 * 
	 * The given timeout is used to close the subscription on the publisher after a period of inactivity on
	 * the input stream. When this happens the input stream throws an IOException upon the next read operation.
	 * 
	 * @param publisher The publisher to wrap in an InputStream.
	 * @param timeoutInMillis The timeout before the subscription on the publisher is closed after inactivity.
	 * @return An input stream that reads bytes from the given publisher.
	 */
	InputStream asInputStream (Publisher<ByteString> publisher, long timeoutInMillis);

	/**
	 * Turns a publisher into a "serializable" publisher: a publisher that is serializable and can be transferred to remote hosts.
	 * 
	 * @param publisher	The source publisher.
	 * @return			A remote publisher that wraps the given publisher.
	 */
	<T> Publisher<T> asSerializable (Publisher<T> publisher);
	
	/**
	 * Publishes a static ByteString by splitting it into block of at most blockSize bytes.
	 * 
	 * @param input		The input byteString, cannot be null
	 * @param blockSize	The maximum block size in bytes (1 <= blockSize <= Long.MAX_VALUE).
	 * @return 			A publisher that returns the bytes from the given byte string.
	 */
	Publisher<ByteString> publishByteString (ByteString input, int blockSize);
	
	/**
	 * Reads all values from the given publisher and returns them as a list.
	 * 
	 * @param publisher	The publisher to read values from.
	 * @return			A completablefuture that is completed when all elements from the publisher have been consumed.
	 * 					Completes exceptionally if the publisher returned an error.
	 */
	public static <T> CompletableFuture<List<T>> asList (final Publisher<T> publisher) {
		Objects.requireNonNull (publisher, "publisher cannot be null");
		
		final CompletableFuture<List<T>> future = new CompletableFuture<> ();
		final List<T> result = new ArrayList<> ();
		
		publisher.subscribe (new Subscriber<T> () {
			@Override
			public synchronized void onComplete () {
				future.complete (result);
			}

			@Override
			public synchronized void onError (final Throwable exception) {
				future.completeExceptionally (exception);
			}

			@Override
			public synchronized void onNext (final T value) {
				result.add (value);
			}

			@Override
			public synchronized void onSubscribe (final Subscription subscription) {
				subscription.request (Long.MAX_VALUE);
			}
		});
		
		return future;
	}
	
	/**
	 * Returns a new publisher that is created by mapping the values in the given publisher using
	 * the provided mapper function.
	 * 
	 * @param input		The input publisher to transform.
	 * @param mapper	The mapping function to use when transforming values.
	 * @return			A new publisher that produces transformed values.
	 */
	public static <T, R> Publisher<R> map (final Publisher<T> input, final Function<? super T, ? extends R> mapper) {
		return new MappedPublisher<> (input, mapper);
	}

	/**
	 * Creates a serializable publisher reference for the given publisher. The reference can be transformed
	 * into a publisher at the receiving end.
	 * 
	 * @param publisher			The publisher to create a reference for.
	 * @param timeoutInMillis	The timeout in milliseconds to wait for consumers of the stream.
	 * @return					A {@link PublisherReference} for the given publisher.
	 */
	<T> PublisherReference<T> createPublisherReference (Publisher<T> publisher, long timeoutInMillis);
	
	/**
	 * Resolves a publisher reference back to a publisher.
	 * 
	 * @param publisherReference	The publisher reference.
	 * @param timeoutInMillis		The timeout in milliseconds to wait for elements in the stream.
	 * @return						The publisher that is referenced by the given input.
	 */
	<T> Publisher<T> resolvePublisherReference (PublisherReference<T> publisherReference, long timeoutInMillis);
}

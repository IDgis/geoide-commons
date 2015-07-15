package nl.idgis.geoide.util.streams;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.reactivestreams.Publisher;

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
}

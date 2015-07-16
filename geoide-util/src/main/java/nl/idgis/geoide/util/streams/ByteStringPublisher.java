package nl.idgis.geoide.util.streams;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import akka.util.ByteString;

public class ByteStringPublisher implements Publisher<ByteString> {

	private final ByteString input;
	private final int blockSize;
	
	public ByteStringPublisher (final ByteString input, final int blockSize) {
		this.input = input;
		this.blockSize = blockSize;
	}
	
	@Override
	public void subscribe (final Subscriber<? super ByteString> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}

		final AtomicLong requested = new AtomicLong (0);
		final AtomicBoolean sending = new AtomicBoolean (false);
		final AtomicBoolean stopped = new AtomicBoolean (false);
		final AtomicReference<ByteString> tail = new AtomicReference<ByteString> (input);
		
		subscriber.onSubscribe (new Subscription () {
			@Override
			public void request (final long count) {
				if (count <= 0) {
					subscriber.onError (new IllegalArgumentException ("count should be > 0 (3.9)"));
					return;
				}
				
				// Atomically increment the requested count:
				synchronized (requested) {
					final long currentValue = requested.get ();
					final long newValue = (currentValue + count < currentValue) ? Long.MAX_VALUE : currentValue + count; 
					requested.set (newValue);
				}
				
				// Send parts only if another thread is not currently sending:
				if (!sending.compareAndSet (false, true)) {
					return;
				}
				
				while (true) {
					if (stopped.get ()) {
						break;
					}
					
					final ByteString part = tail.get ().take (blockSize);
					tail.set (tail.get ().drop (blockSize));
					
					if (part.isEmpty()) {
						subscriber.onComplete ();
						stopped.set (true);
						break;
					}
					
					subscriber.onNext (part);
					
					// Atomically decrease count:
					synchronized (requested) {
						final long currentValue = requested.get ();
						requested.set (Math.max (0, currentValue - 1));
						if (currentValue <= 1) {
							sending.set (false);
							return;
						}
					}
				}
			}
			
			@Override
			public void cancel () {
				stopped.set (true);
			}
		});
	}
}

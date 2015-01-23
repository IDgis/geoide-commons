package nl.idgis.geoide.util.streams;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class SingleValuePublisher<T> implements Publisher<T> {
	private final T value;
	
	public SingleValuePublisher (final T value) {
		this.value = value;
	}
	
	@Override
	public void subscribe (final Subscriber<? super T> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}
		
		subscriber.onSubscribe (new SingleValueSubscription (subscriber));
	}
	
	private class SingleValueSubscription implements Subscription {

		private final Subscriber<? super T> subscriber;
		
		private boolean cancelled = false;
		private boolean dataSent = false;
		
		public SingleValueSubscription (final Subscriber<? super T> subscriber) {
			this.subscriber = subscriber;
			
		}
		
		@Override
		public void cancel () {
			cancelled = true;
		}

		@Override
		public void request (long n) {
			// Requests after cancel or onComplete are NOP's:
			if (cancelled) {
				return;
			}
			
			if (!dataSent && n > 0) {
				dataSent = true;
				-- n;
				subscriber.onNext (value);
			}

			if (n > 0) {
				cancelled = true;
				subscriber.onComplete ();
			}
		}
	}
}

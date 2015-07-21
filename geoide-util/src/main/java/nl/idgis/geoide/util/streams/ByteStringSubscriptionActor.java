package nl.idgis.geoide.util.streams;

import java.util.concurrent.TimeUnit;

import org.reactivestreams.Subscriber;

import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import akka.util.ByteString;
import nl.idgis.geoide.util.streams.messages.PublisherCancel;
import nl.idgis.geoide.util.streams.messages.PublisherRequest;
import scala.concurrent.duration.Duration;

public class ByteStringSubscriptionActor extends UntypedActor {
	
	private ByteString byteString;
	private final Subscriber<? super ByteString> subscriber;
	private final int blockSize;
	private final long timeoutInMillis;

	public ByteStringSubscriptionActor (final ByteString byteString, final Subscriber<? super ByteString> subscriber, final int blockSize, final long timeoutInMillis) {
		this.byteString = byteString;
		this.subscriber = subscriber;
		this.blockSize = blockSize;
		this.timeoutInMillis = timeoutInMillis;
	}
	
	public static Props props (final ByteString byteString, final Subscriber<? super ByteString> subscriber, final int blockSize, final long timeoutInMillis) {
		if (byteString == null) {
			throw new NullPointerException ("byteString cannot be null");
		}
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}
		if (blockSize <= 0) {
			throw new IllegalArgumentException ("blockSize must be > 0");
		}
		if (timeoutInMillis <= 0) {
			throw new IllegalArgumentException ("timeoutInMillis must be > 0");
		}
		
		return Props.create (ByteStringSubscriptionActor.class, byteString, subscriber, blockSize, timeoutInMillis);
	}
	
	@Override
	public void preStart () throws Exception {
		super.preStart();
		
		getContext ().setReceiveTimeout (Duration.create (timeoutInMillis, TimeUnit.MILLISECONDS));
	}
	
	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof PublisherRequest) {
			final PublisherRequest request = (PublisherRequest) message;
			
			if (request.getCount () <= 0) {
				subscriber.onError (new IllegalArgumentException ("count should be > 0 (3.9)"));
				getContext ().stop (self ());
				return;
			}

			long count = request.getCount ();
			while (count > 0) {
				if (byteString.isEmpty ()) {
					subscriber.onComplete ();
					getContext ().stop (self ());
					return;
				}
				
				final ByteString block = byteString.take (blockSize);
				byteString = byteString.drop (blockSize);

				subscriber.onNext (block);
				--count;
			}
		} else if (message instanceof PublisherCancel) {
			getContext ().stop (self ());
		} else if (message instanceof ReceiveTimeout) {
			getContext ().stop (self ());
		} else {
			unhandled (message);
		}
	}
}

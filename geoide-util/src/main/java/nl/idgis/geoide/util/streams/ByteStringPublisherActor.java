package nl.idgis.geoide.util.streams;

import java.util.concurrent.TimeUnit;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import akka.util.ByteString;
import nl.idgis.geoide.util.streams.messages.PublisherCancel;
import nl.idgis.geoide.util.streams.messages.PublisherRequest;
import scala.concurrent.duration.Duration;

public class ByteStringPublisherActor extends UntypedActor {

	private final ByteString byteString;
	private final int blockSize;
	private final long timeoutInMillis;
	
	public ByteStringPublisherActor (final ByteString byteString, final int blockSize, final long timeoutInMillis) {
		this.byteString = byteString;
		this.blockSize = blockSize;
		this.timeoutInMillis = timeoutInMillis;
	}
	
	public static Props props (final ByteString byteString, final int blockSize, final long timeoutInMillis) {
		if (byteString == null) {
			throw new NullPointerException ("byteString cannot be null");
		}
		if (blockSize <= 0) {
			throw new IllegalArgumentException ("blockSize should be >= 1");
		}
		if (timeoutInMillis <= 0) {
			throw new IllegalArgumentException ("timeoutInMillis should be >= 1");
		}
		
		return Props.create (ByteStringPublisherActor.class, byteString, blockSize, timeoutInMillis);
	}
	
	@Override
	public void preStart () throws Exception {
		super.preStart();
		
		getContext ().setReceiveTimeout (Duration.create (timeoutInMillis, TimeUnit.MILLISECONDS));
	}
	
	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof Subscriber) {
			@SuppressWarnings("unchecked")
			final Subscriber<? super ByteString> subscriber = (Subscriber<? super ByteString>) message;
			final ActorRef subscriptionActor = getContext ().actorOf (ByteStringSubscriptionActor.props (byteString, subscriber, blockSize, timeoutInMillis));
			
			subscriber.onSubscribe (new Subscription () {
				@Override
				public void request (final long count) {
					subscriptionActor.tell (new PublisherRequest (count), self ());
				}
				
				@Override
				public void cancel () {
					subscriptionActor.tell (new PublisherCancel (), self ());
				}
			});
		} else if (message instanceof ReceiveTimeout) {
			getContext ().stop (self ());
		} else {
			unhandled (message);
		}
	}
}

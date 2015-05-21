package nl.idgis.geoide.util.streams;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import scala.concurrent.duration.Duration;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;

public class SerializablePublisherActor extends UntypedActor {
	
	private final Publisher<? extends Serializable> publisher;
	private final long timeoutInMillis;
	
	public SerializablePublisherActor (final Publisher<? extends Serializable> publisher, final long timeoutInMillis) {
		this.publisher = publisher;
		this.timeoutInMillis = timeoutInMillis;
	}
	
	public static <T extends Serializable> Props props (final Publisher<T> publisher, final long timeoutInMillis) {
		if (publisher == null) {
			throw new NullPointerException ("publisher cannot be null");
		}
		
		return Props.create (SerializablePublisherActor.class, publisher, timeoutInMillis);
	}
	
	@Override
	public void preStart () throws Exception {
		super.preStart();
		
		context ().setReceiveTimeout (Duration.create (timeoutInMillis, TimeUnit.MILLISECONDS));
	}

	@Override
	public void onReceive (final Object message) throws Exception {
		if (message instanceof ReceiveTimeout) {
			context ().stop (self ());
		} else if (message instanceof Subscriber<?>) {
			sender ().tell (context ().actorOf (SerializablePublisherSubscriptionActor.props (self (), publisher, (Subscriber<?>) message, timeoutInMillis)), self ());
		} else {
			unhandled (message);
		}
	}
}

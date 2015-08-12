package nl.idgis.geoide.util.streams;

import java.util.Objects;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class MappedPublisher<T, R> implements Publisher<R> {

	private final Publisher<T> source;
	private final Function<? super T, ? extends R> mapper;
	
	public MappedPublisher (final Publisher<T> source, final Function<? super T, ? extends R> mapper) {
		this.source = Objects.requireNonNull (source, "source cannot be null");
		this.mapper = Objects.requireNonNull (mapper, "mapper cannot be null");
	}
	@Override
	public void subscribe (final Subscriber<? super R> subscriber) {
		Objects.requireNonNull (subscriber, "subscriber cannot be null");
		
		source.subscribe (new Subscriber<T> () {
			private Subscription s = null;
			private boolean terminated = false;
			
			@Override
			public void onSubscribe (final Subscription s) {
				this.s = s;
				
				subscriber.onSubscribe (new Subscription () {
					@Override
					public void cancel () {
						s.cancel ();
					}

					@Override
					public void request (final long n) {
						s.request (n);
					}
				});
			}
			
			@Override
			public void onNext (final T t) {
				if (terminated) {
					return;
				}
				
				try {
					subscriber.onNext (mapper.apply (t));
				} catch (Throwable e) {
					terminated = true;
					subscriber.onError (e);
					s.cancel ();
				}
			}
			
			@Override
			public void onError (final Throwable t) {
				terminated = true;
				subscriber.onError (t);
			}
			
			@Override
			public void onComplete () {
				terminated = true;
				subscriber.onComplete ();
			}
			
		});
	}

}

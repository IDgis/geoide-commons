package nl.idgis.geoide.util.streams;

import org.reactivestreams.Publisher;

public interface EventStreamPublisher<T> extends Publisher<T> {
	void publish (T event);
	void complete ();
}

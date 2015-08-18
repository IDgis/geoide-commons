package nl.idgis.geoide.util.streams;

import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Publisher;

public interface IntervalPublisher extends Publisher<Long> {
	CompletableFuture<Void> stop ();
}

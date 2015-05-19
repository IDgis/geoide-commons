package nl.idgis.geoide.util.streams;

import java.io.Serializable;

import org.reactivestreams.Publisher;

public interface SerializablePublisher<T> extends Publisher<T>, Serializable {
}

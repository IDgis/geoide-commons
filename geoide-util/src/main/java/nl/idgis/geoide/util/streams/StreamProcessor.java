package nl.idgis.geoide.util.streams;

import java.io.InputStream;

import org.reactivestreams.Publisher;

import play.libs.F.Function2;
import play.libs.F.Promise;
import akka.util.ByteString;

public interface StreamProcessor {

	<T> Promise<T> reduce (Publisher<T> publisher, T initialValue, Function2<T, T, T> reducer);
	<T> Publisher<T> publishSinglevalue (T value);
	
	Publisher<ByteString> publishInputStream (InputStream inputStream, int maxBlockSize, long timeoutInMillis);
	InputStream asInputStream (Publisher<ByteString> publisher, long timeoutInMillis);
}

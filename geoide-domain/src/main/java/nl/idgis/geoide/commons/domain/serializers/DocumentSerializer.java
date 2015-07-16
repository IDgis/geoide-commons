package nl.idgis.geoide.commons.domain.serializers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.reactivestreams.Publisher;

import akka.actor.ExtendedActorSystem;
import akka.serialization.JSerializer;
import akka.util.ByteString;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.util.akka.serializers.StreamSerializer;
import nl.idgis.geoide.util.streams.AkkaSerializablePublisher;

public class DocumentSerializer extends JSerializer {

	private final StreamSerializer streamSerializer;
	
	public DocumentSerializer (final ExtendedActorSystem actorSystem) {
		this.streamSerializer = new StreamSerializer (actorSystem);
	}
	
	@Override
	public int identifier () {
		return 42;
	}

	@Override
	public boolean includeManifest () {
		return true;
	}

	@Override
	public byte[] toBinary (final Object value) {
		if (!(value instanceof Document)) {
			throw new UnsupportedOperationException (DocumentSerializer.class.getCanonicalName () + " can only be used to serialize instances of " + Document.class.getCanonicalName ());
		}
		
		final Document document = (Document) value;
		
		try (final ByteArrayOutputStream bos = new ByteArrayOutputStream ()) {
			try (final ObjectOutputStream os = new ObjectOutputStream (bos)) {
				os.writeObject (document.getUri ());
				os.writeObject (document.getContentType ());
				os.writeObject (streamSerializer.toBinary (document.getBody ()));
			}
			
			bos.close ();
			return bos.toByteArray ();
		} catch (URISyntaxException | IOException e) {
			throw new RuntimeException (e);
		}
	}

	@Override
	public Object fromBinaryJava (final byte[] message, final Class<?> cls) {
		if (!Document.class.isAssignableFrom (cls)) {
			throw new UnsupportedOperationException (DocumentSerializer.class.getCanonicalName () + " can only be used to deserialize instances of " + Document.class.getCanonicalName ());
		}

		try (final ByteArrayInputStream bis = new ByteArrayInputStream (message)) {
			try (final ObjectInputStream is = new ObjectInputStream (bis)) {
				final URI uri = (URI) is.readObject ();
				final MimeContentType contentType = (MimeContentType) is.readObject ();
				final byte[] bytes = (byte[]) is.readObject ();
				final Publisher<?> publisher = (Publisher<?>) streamSerializer.fromBinary (bytes, AkkaSerializablePublisher.class);
				
				return new Document () {

					@Override
					public URI getUri () throws URISyntaxException {
						return uri;
					}

					@Override
					public MimeContentType getContentType () {
						return contentType;
					}

					@SuppressWarnings("unchecked")
					@Override
					public Publisher<ByteString> getBody() {
						return (Publisher<ByteString>) publisher;
					}
				};
			}
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException (e);
		}
	}
}

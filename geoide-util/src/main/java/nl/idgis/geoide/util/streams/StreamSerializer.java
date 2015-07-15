package nl.idgis.geoide.util.streams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.CompletableFuture;

import akka.actor.ActorRef;
import akka.actor.ExtendedActorSystem;
import akka.serialization.JSerializer;
import akka.serialization.Serialization;

public class StreamSerializer extends JSerializer {

	private final ExtendedActorSystem actorSystem;
	
	public StreamSerializer (final ExtendedActorSystem actorSystem) {
		this.actorSystem = actorSystem;
	}
	
	@Override
	public int identifier () {
		return 43447;
	}

	@Override
	public boolean includeManifest () {
		return true;
	}

	@Override
	public byte[] toBinary (final Object object) {
		try (final ByteArrayOutputStream bos = new ByteArrayOutputStream ()) {
			try (final ObjectOutputStream os = new ObjectOutputStream (bos)) {
				if (object instanceof AkkaSerializablePublisher) {
					final AkkaSerializablePublisher<?> publisher = (AkkaSerializablePublisher<?>) object;
					os.writeUTF (Serialization.serializedActorPath (publisher.getActorRef ()));
				} else if (object instanceof AkkaSerializableSubscriber) {
					final AkkaSerializableSubscriber<?> subscriber = (AkkaSerializableSubscriber<?>) object;
					os.writeUTF (Serialization.serializedActorPath (subscriber.getActorRef ()));
				} else {
					throw new UnsupportedOperationException (StreamSerializer.class.getCanonicalName () + " cannot be used to serialize instances of " + object.getClass ().getCanonicalName ());
				}
			}
			
			bos.close ();
			return bos.toByteArray ();
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object fromBinaryJava (final byte[] message, final Class<?> cls) {
		try (final ByteArrayInputStream bis = new ByteArrayInputStream (message)) {
			try (final ObjectInputStream is = new ObjectInputStream (bis)) {
				final String identifier = is.readUTF ();
				final ActorRef actorRef = actorSystem.provider ().resolveActorRef (identifier);
				if (AkkaSerializablePublisher.class.equals (cls)) {
					return new AkkaSerializablePublisher (actorSystem, CompletableFuture.completedFuture (actorRef));
				} else if (AkkaSerializableSubscriber.class.equals (cls)) {
					return new AkkaSerializableSubscriber<> (actorSystem, actorRef);
				} else {
					throw new UnsupportedOperationException (StreamSerializer.class.getCanonicalName () + " cannot be used to deserialize instances of " + cls.getCanonicalName ());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
	}
	
	

}

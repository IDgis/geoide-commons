package nl.idgis.geoide.util.akka.serializers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import akka.serialization.JSerializer;
import akka.util.ByteString;

public class ByteStringSerializer extends JSerializer {

	@Override
	public int identifier () {
		return 43448;
	}

	@Override
	public boolean includeManifest() {
		return true;
	}

	@Override
	public byte[] toBinary (final Object object) {
		try (final ByteArrayOutputStream bos = new ByteArrayOutputStream ()) {
			try (final ObjectOutputStream os = new ObjectOutputStream (bos)) {
				if (object instanceof ByteString) {
					os.writeObject (((ByteString) object).compact ());
				} else {
					throw new IllegalArgumentException (ByteStringSerializer.class.getCanonicalName () + " can only be used to serialize instances of " + ByteString.class.getCanonicalName ());
				}
			}
			
			bos.close ();
			return bos.toByteArray ();
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
	}

	@Override
	public Object fromBinaryJava (final byte[] message, final Class<?> cls) {
		if (!ByteString.class.isAssignableFrom (cls)) {
			throw new IllegalArgumentException (ByteStringSerializer.class.getCanonicalName () + " can only be used to deserialize instances of " + ByteString.class.getCanonicalName ());
		}
		
		try (final ByteArrayInputStream bis = new ByteArrayInputStream (message)) {
			try (final ObjectInputStream is = new ObjectInputStream (bis)) {
				return is.readObject ();
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException (e);
		}
	}
}

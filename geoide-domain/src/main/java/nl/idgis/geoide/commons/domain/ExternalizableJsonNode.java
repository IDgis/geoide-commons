package nl.idgis.geoide.commons.domain;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;

public class ExternalizableJsonNode implements Externalizable, Serializable {
	
	private JsonNode node;
	
	public ExternalizableJsonNode () {
		node = null;
	}
	
	public ExternalizableJsonNode (final JsonNode node) {
		if (node == null) {
			throw new NullPointerException ("node cannot be null");
		}
		
		this.node = node;
	}

	@Override
	public void writeExternal (final ObjectOutput out) throws IOException {
		final byte[] bytes;
		
		if (node == null) {
			bytes = new byte[] { };
		} else {
			bytes = JsonFactory.mapper ().writeValueAsBytes (node);
		}
		
		out.writeInt (bytes.length);
		out.write (bytes);
	}

	@Override
	public void readExternal (final ObjectInput in) throws IOException, ClassNotFoundException {
		final int length = in.readInt ();
		final byte[] bytes = new byte[length];

		in.readFully (bytes);
		
		node = JsonFactory.mapper ().readTree (bytes);
	}
	
	public JsonNode getJsonNode () {
		return node;
	}
}

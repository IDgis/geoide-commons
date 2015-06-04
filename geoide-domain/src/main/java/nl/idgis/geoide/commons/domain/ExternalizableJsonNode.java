package nl.idgis.geoide.commons.domain;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.MissingNode;

public class ExternalizableJsonNode extends BaseJsonNode implements Externalizable, Serializable {
	
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
		
		in.read (bytes);
		
		node = JsonFactory.mapper ().readTree (bytes);
	}

	@Override
	public JsonToken asToken() {
		return node.asToken ();
	}

	@Override
	public NumberType numberType() {
		return node.numberType ();
	}

	@Override
	public JsonParser traverse() {
		return node.traverse ();
	}

	@Override
	public JsonParser traverse(ObjectCodec codec) {
		return node.traverse (codec);
	}

	@Override
	protected JsonNode _at(JsonPointer ptr) {
		if (node.isValueNode ()) {
	        return JsonFactory.externalize (MissingNode.getInstance());
		} else {
			return JsonFactory.externalize (get (ptr.getMatchingProperty()));
		}
	}

	@Override
	public String asText() {
		return node.asText ();
	}

	@Override
	public <T extends JsonNode> T deepCopy() {
		return node.deepCopy ();
	}

	@Override
	public boolean equals(Object other) {
		return node.equals (other);
	}

	@Override
	public JsonNode findParent (String v) {
		return JsonFactory.externalize (node.findParent (v));
	}

	@Override
	public List<JsonNode> findParents (String a, List<JsonNode> b) {
		final List<JsonNode> result = new ArrayList<JsonNode> ();
		
		for (final JsonNode n: node.findParents (a, b)) {
			result.add (JsonFactory.externalize (n));
		}
		
		return Collections.unmodifiableList (result);
	}

	@Override
	public JsonNode findValue(String a) {
		return JsonFactory.externalize (node.findValue (a));
	}

	@Override
	public List<JsonNode> findValues(String a, List<JsonNode> b) {
		final List<JsonNode> result = new ArrayList<JsonNode> ();
		
		for (final JsonNode n: node.findValues (a, b)) {
			result.add (JsonFactory.externalize (n));
		}
		
		return Collections.unmodifiableList (result);
	}

	@Override
	public List<String> findValuesAsText(String a, List<String> b) {
		return node.findValuesAsText (a, b);
	}

	@Override
	public JsonNode get(int a) {
		return JsonFactory.externalize (node.get(a));
	}

	@Override
	public JsonNodeType getNodeType() {
		return node.getNodeType ();
	}

	@Override
	public JsonNode path(String a) {
		return JsonFactory.externalize (node.path (a));
	}

	@Override
	public JsonNode path(int a) {
		return JsonFactory.externalize (node.path (a));
	}

	@Override
	public String toString() {
		return node.toString ();
	}
	
    @Override
    public int size() { 
    	return node.size ();
    }
	
    @Override
    public JsonNode get(String fieldName) { 
    	return node.get(fieldName); 
    }

    @Override
    public Iterator<String> fieldNames() {
        return node.fieldNames ();
    }

    @Override
    public boolean isIntegralNumber() { 
    	return node.isIntegralNumber (); 
    }
    
    @Override
    public boolean isFloatingPointNumber() { 
    	return node.isFloatingPointNumber (); 
    }

    @Override
    public boolean isShort() { 
    	return node.isShort (); 
    }

	@Override
	public boolean isInt() {
		return node.isInt();
	}

	@Override
	public boolean isLong() {
		return node.isLong();
	}

	@Override
	public boolean isFloat() {
		return node.isFloat();
	}

	@Override
	public boolean isDouble() {
		return node.isDouble();
	}

	@Override
	public boolean isBigDecimal() {
		return node.isBigDecimal();
	}

	@Override
	public boolean isBigInteger() {
		return node.isBigInteger();
	}

	@Override
	public boolean canConvertToInt() {
		return node.canConvertToInt();
	}

	@Override
	public boolean canConvertToLong() {
		return node.canConvertToLong();
	}

	@Override
	public String textValue() {
		return node.textValue();
	}

	@Override
	public byte[] binaryValue() throws IOException {
		return node.binaryValue();
	}

	@Override
	public boolean booleanValue() {
		return node.booleanValue();
	}

	@Override
	public Number numberValue() {
		return node.numberValue();
	}

	@Override
	public short shortValue() {
		return node.shortValue();
	}

	@Override
	public int intValue() {
		return node.intValue();
	}

	@Override
	public long longValue() {
		return node.longValue();
	}

	@Override
	public float floatValue() {
		return node.floatValue();
	}

	@Override
	public double doubleValue() {
		return node.doubleValue();
	}

	@Override
	public BigDecimal decimalValue() {
		return node.decimalValue();
	}

	@Override
	public BigInteger bigIntegerValue() {
		return node.bigIntegerValue();
	}

	@Override
	public int asInt() {
		return node.asInt();
	}

	@Override
	public int asInt(int defaultValue) {
		return node.asInt(defaultValue);
	}

	@Override
	public long asLong() {
		return node.asLong();
	}

	@Override
	public long asLong(long defaultValue) {
		return node.asLong(defaultValue);
	}

	@Override
	public double asDouble() {
		return node.asDouble();
	}

	@Override
	public double asDouble(double defaultValue) {
		return node.asDouble(defaultValue);
	}

	@Override
	public boolean asBoolean() {
		return node.asBoolean();
	}

	@Override
	public boolean asBoolean(boolean defaultValue) {
		return node.asBoolean(defaultValue);
	}

	@Override
	public boolean has(String fieldName) {
		return node.has(fieldName);
	}

	@Override
	public boolean has(int index) {
		return node.has(index);
	}

	@Override
	public boolean hasNonNull(String fieldName) {
		return node.hasNonNull(fieldName);
	}

	@Override
	public boolean hasNonNull(int index) {
		return node.hasNonNull(index);
	}

	@Override
	public Iterator<JsonNode> elements() {
		final Iterator<JsonNode> rootIterator = node.elements ();
		
		return new Iterator<JsonNode> () {

			@Override
			public boolean hasNext() {
				return rootIterator.hasNext ();
			}

			@Override
			public JsonNode next() {
				final JsonNode n = rootIterator.next ();
				
				return n == null ? n : JsonFactory.externalize (n);
			}
		};
	}

	@Override
	public Iterator<Entry<String, JsonNode>> fields() {
		final Iterator<Entry<String, JsonNode>> rootIterator = node.fields ();
		
		return new Iterator<Entry<String,JsonNode>> () {
			@Override
			public boolean hasNext() {
				return rootIterator.hasNext ();
			}

			@Override
			public Entry<String, JsonNode> next() {
				final Entry<String, JsonNode> entry = rootIterator.next ();
				if (entry == null) {
					return null;
				}

				return new Entry<String, JsonNode> () {
					@Override
					public JsonNode setValue (JsonNode value) {
						throw new UnsupportedOperationException ();
					}
					
					@Override
					public JsonNode getValue() {
						return JsonFactory.externalize (entry.getValue ());
					}
					
					@Override
					public String getKey() {
						return entry.getKey ();
					}
				};
			}
		};
	}

	@Override
	public JsonNode with(String propertyName) {
		return JsonFactory.externalize (node.with(propertyName));
	}

	@Override
	public JsonNode withArray(String propertyName) {
		return JsonFactory.externalize (node.withArray(propertyName));
	}

	@Override
	public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		((BaseJsonNode) node).serialize (jgen, provider);
	}

	@Override
	public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonProcessingException {
		((BaseJsonNode) node).serializeWithType (jgen, provider, typeSer);
	}
}

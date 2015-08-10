package nl.idgis.geoide.commons.domain;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class TestExternalizableJsonNode {

	@Test
	public void testIterate () {
		final ArrayNode node = JsonFactory.mapper ().createArrayNode ();
		
		node.add (JsonFactory.mapper ().createObjectNode ());
		node.add (JsonFactory.mapper ().createObjectNode ());
		node.add (JsonFactory.mapper ().createObjectNode ());
		
		final ExternalizableJsonNode wrapped = new ExternalizableJsonNode (node);
		
		assertEquals (3, wrapped.getJsonNode ().size ());
		int n = 0;
		for (final JsonNode child: wrapped.getJsonNode ()) {
			assertNotNull (child);
			++ n;
		}
		assertEquals (3, n);
	}
}

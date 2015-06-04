package nl.idgis.services.client.arcgis;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.idgis.services.arcgis.ArcGISRestCapabilities;
import nl.idgis.services.arcgis.ArcGISRestCapabilities.Layer;
import nl.idgis.services.arcgis.ArcGISRestCapabilities.ServiceIdentification;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ArcGISRestCapabilitiesParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

	public static ArcGISRestCapabilities parseCapabilities (final InputStream inputStream) throws ParseException {
		try {
			final JsonNode document = objectMapper.readValue (inputStream, JsonNode.class);
			
			return parseCapabilities (document);
		} catch (JsonParseException e) {
			throw new ParseException ("Unable to parse JSON document", e);
		} catch (JsonMappingException e) {
			throw new ParseException ("Unable to parse JSON document", e);
		} catch (IOException e) {
			throw new ParseException ("Unable to read from stream", e);
		}
	}
	
	private static ArcGISRestCapabilities parseCapabilities (final JsonNode document) throws ParseException {
		final String version = document.path ("currentVersion").asText ();
		final String title = coalesceNonEmpty (document.path ("serviceDescription").asText (), document.path ("description").asText (), document.path ("documentInfo").path ("Title").asText (), document.path("mapName").asText());
		final String crs = document.path ("spatialReference").path ("wkid").asText ();
		
		if (version == null || version.isEmpty ()) {
			throw new ParseException ("The capabilities document does not specify a version");
		}
		if (title == null || title.isEmpty ()) {
			throw new ParseException ("The capabilities document does not specify a title");
		}
		if (crs == null || crs.isEmpty ()) {
			throw new ParseException ("The capabilities document should specify a spatial reference system");
		}
		
		final List<Layer> layers = parseLayers (document, "EPSG:" + crs);
		
		if (layers == null || layers.isEmpty ()) {
			throw new ParseException ("The capabilities document should provide at least one layer");
		}

		return new ArcGISRestCapabilities (version, new ServiceIdentification (title, null), layers);
	}
	
	private static List<Layer> parseLayers (final JsonNode document, final String crs) throws ParseException {
		final Map<String, JsonNode> layerMap = new HashMap<> ();
		
		// Index the list of layers:
		for (final JsonNode layerNode: document.path ("layers")) {
			final String id = layerNode.path ("id").asText ();
			final String name = layerNode.path ("name").asText ();
			
			if (id == null || id.isEmpty ()) {
				throw new ParseException ("Each layer should specify an id property");
			}
			if (name == null || name.isEmpty ()) {
				throw new ParseException (String.format ("Layer %s has no name", id));
			}
			
			layerMap.put (id, layerNode);
		}
		
		// Build the final list of layers:
		final List<Layer> layers = new ArrayList<> ();
		for (final JsonNode layerNode: document.path ("layers")) {
			if (layerNode.path ("parentLayerId").asInt () >= 0) {
				continue;
			}
			
			layers.add (buildLayer (layerMap, layerNode, crs));
		}
		
		return layers;
	}
	
	private static Layer buildLayer (final Map<String, JsonNode> layers, final JsonNode layerNode, final String crs) throws ParseException {
		final String id = layerNode.path ("id").asText ();
		final String name = layerNode.path ("name").asText ();
		final List<Layer> subLayers = new ArrayList<> ();
		
		for (final JsonNode subLayerId: layerNode.path ("subLayerIds")) {
			final JsonNode subLayerNode = layers.get (subLayerId.asText ());
			if (subLayerNode == null) {
				throw new ParseException (String.format ("Layer not found: %s", subLayerId.asText ()));
			}
			subLayers.add (buildLayer (layers, subLayerNode, crs));
		}
		
		return new Layer (id, name, crs, subLayers);
	}
	
	private static String coalesceNonEmpty (final String ... values) {
		for (final String v: values) {
			if (v != null && !v.trim ().isEmpty ()) {
				return v;
			}
		}
		
		return "";
	}
	
	public static class ParseException extends Exception {
		private static final long serialVersionUID = -605117083683271588L;

		public ParseException (final String message) {
			super (message);
		}
		
		public ParseException (final String message, final Throwable cause) {
			super (message, cause);
		}
	}
}

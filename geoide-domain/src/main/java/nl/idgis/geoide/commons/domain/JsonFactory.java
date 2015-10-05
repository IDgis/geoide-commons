package nl.idgis.geoide.commons.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonFactory {
	
	private final static ObjectMapper mapper = new ObjectMapper ();
	
	public static ObjectMapper mapper () {
		return mapper;
	}
	
	public static JsonNode asJson (final Object object) {
		if (object instanceof JsonNode) {
			return (JsonNode) object;
		} else {
			return mapper ().valueToTree (object);
		}
	}
	
	public static ExternalizableJsonNode externalize (final JsonNode node) {
		return new ExternalizableJsonNode (node);
	}
	
	public static ServiceIdentification serviceIdentification (final String json) {
		return serviceIdentification (parse (json));
	}
	
	public static ServiceIdentification serviceIdentification (final JsonNode node) {
		return parseObject (node, ServiceIdentification.class);
	}

	public static Service service (final String json) {
		return service (parse (json));
	}
	
	public static Service service (final JsonNode node) {
		return parseObject (node, Service.class);
	}
	
	public static QName qName (final String json) {
		return qName (parse (json));
	}
	
	public static QName qName (final JsonNode node) {
		if (node.isObject ()) {
			return parseObject (node, QName.class);
		} else {
			return new QName (node.asText ());
		}
	}
	
	public static MapDefinition mapDefinition (final JsonNode mapNode, final Map<String, Layer> layerMap, final Map<String, ServiceLayer> serviceLayerMap) {
		final JsonNode id = mapNode.path ("id"); 
		final JsonNode label = mapNode.path ("label");
		final JsonNode prefix = mapNode.path ("prefix");
		final String initialExtent;
		final JsonNode initialExtentNode = mapNode.path("initial-extent");
		if (!initialExtentNode.isMissingNode ()) {
			JsonNode minx = initialExtentNode.path("minx");
			JsonNode miny = initialExtentNode.path("miny");
			JsonNode maxx = initialExtentNode.path("maxx");
			JsonNode maxy = initialExtentNode.path("maxy");
			if(minx.isMissingNode() || miny.isMissingNode() || maxx.isMissingNode() || maxy.isMissingNode()){
				//throw new IllegalArgumentException ("Missing property: initial-extent");
				initialExtent = "";
			} else {
				initialExtent = minx.asText() + "," + miny.asText() + ","+ maxx.asText() + "," + maxy.asText(); 
			}
		} else {
			initialExtent = "";
		}

		if (mapNode.has("maplayers")) {
			final List<LayerRef> layerRefList = new ArrayList<> ();
			//Parse layers:
			for ( final JsonNode layerRefNode: mapNode.path("maplayers")) {
				
				// merge maplayer and layer
				//mergeLayers(mapLayer, layerNodes);
				
				final LayerRef layerRef = JsonFactory.layerRef (layerRefNode, layerMap);
				layerRefList.add (layerRef);	
			}
			return new MapDefinition (id.asText (), label.asText (), prefix.asText(), initialExtent, layerRefList);
		} else {
			throw new IllegalArgumentException ("Missing property: maplayers");
		}
	}
	
	
	private static LayerRef layerRef (JsonNode node, Map <String, Layer> layerMap) {
		final JsonNode layerRefNodes = node.path ("maplayers");
		final JsonNode layerNode = node.path ("layer");
		//state is initial state
		final JsonNode stateNode = node.path("state");		
		
		final List<LayerRef> layerRefList = new ArrayList<> ();
		for (final JsonNode layerRefNode: layerRefNodes) {
			layerRefList.add (layerRef (layerRefNode, layerMap));
		}
		
		final Map<String, JsonNode> stateMap = new HashMap<> ();  
		
		
		if (!layerMap.containsKey (layerNode.asText ())) {
			throw new IllegalArgumentException ("Undefined layer: " + layerNode.asText ());
		}
		Layer layer = layerMap.get (layerNode.asText());
		
		if (!stateNode.isMissingNode()){	
			for (Iterator<Entry<String,JsonNode>> iterator = stateNode.fields(); iterator.hasNext();) {
				 Entry<String, JsonNode> item = iterator.next();   
				 stateMap.put(item.getKey(), item.getValue());
			}
		}

	
		return new LayerRef (
				layer,
				layerRefList,
				stateMap);
	}
		
	// method to merge maplayer and layer
	//TODO: remove after changes in StaticMapProvider
	
	private static JsonNode mergeLayers (final JsonNode mapLayer, final Map<String, JsonNode> layers) {
		final JsonNode layer = mapLayer.path ("layer");
		if (layer.isMissingNode () || layer.asText ().isEmpty ()) {
		
			((ObjectNode) mapLayer).put("layerType", "default");
			final JsonNode mapLayers = mapLayer.path ("maplayers");
			if (mapLayers.isMissingNode ()) {
				throw new IllegalArgumentException ("Missing property: layer or maplayers");
			}
			
			for ( JsonNode childLayer: mapLayers ) {
				childLayer = mergeLayers(childLayer, layers);					
			}
			((ObjectNode) mapLayer).put("layers", mapLayers);
			((ObjectNode) mapLayer).remove("maplayers");
			return mapLayer;
			
		}
		
		for (final JsonNode lyr: layers.values ()) {
			if(lyr.path("id").asText().equals(layer.asText())) { 
				final JsonNode layerType = lyr.path ("layerType");
				final JsonNode serviceLyrs = lyr.path ("serviceLayers");
				((ObjectNode) mapLayer).put("layerType", layerType);
				((ObjectNode) mapLayer).put("serviceLayers", serviceLyrs);
				//state is initial state
				if (lyr.hasNonNull("state")) {
					final JsonNode state = lyr.path("state");
					if (mapLayer.hasNonNull("state")) {
						((ObjectNode) state).setAll((ObjectNode) mapLayer.path("state"));
					}
					((ObjectNode)mapLayer).put("state", state );
				} 	
				
			}
		}
		return mapLayer;
	}
		
	public static ServiceLayer serviceLayer (final String json, final Map<String, Service> services, final Map<String, FeatureType> featureTypes) {
		return serviceLayer (parse (json), services, featureTypes);
	}
	
	public static FeatureType featureType (final String json, final Map<String, Service> services) {
		return featureType (parse (json), services);
	}
	
	public static ServiceLayer serviceLayer (final JsonNode node, final Map<String, Service> services, final Map<String, FeatureType> featureTypes) {
		final ParseNamedServiceEntity content = namedServiceEntity (node, services, featureTypes);
		return new ServiceLayer (
				content.getId (), 
				content.getService (), 
				content.getName (), 
				content.getLabel (),
				content.getFeatureType ()
			);
	}
	
	public static FeatureType featureType (final JsonNode node, final Map<String, Service> services) {
		final ParseNamedServiceEntity content = namedServiceEntity (node, services, null);
		return new FeatureType (content.getId (), content.getService (), content.getName (), content.getLabel ());
	}
	
	private static ParseNamedServiceEntity namedServiceEntity (final JsonNode node, final Map<String, Service> services, final Map<String, FeatureType> featureTypes) {
		final JsonNode id = node.path ("id");
		final JsonNode service = node.path ("service");
		final JsonNode name = node.path ("name");
		final JsonNode label = node.path ("label");
		final JsonNode featureType = node.path ("featureType");

		if (id.isMissingNode () || id.asText ().isEmpty ()) {
			throw new IllegalArgumentException ("Missing property: id");
		}
		if (label.isMissingNode ()) {
			throw new IllegalArgumentException ("Missing property: label");
		}
		if (service.isMissingNode ()) {
			throw new IllegalArgumentException ("Missing property: service");
		}
		if (name.isMissingNode ()) {
			throw new IllegalArgumentException ("Missing property: name");
		}
		
		final String serviceId = service.asText ();

		if (!services.containsKey (serviceId)) {
			throw new IllegalArgumentException ("Service not defined: " + serviceId);
		}

		final FeatureType ft;
		if (!featureType.isMissingNode () && !featureType.isNull () && featureTypes != null) {
			final String featureTypeId = featureType.asText ();
			
			if (!featureTypes.containsKey (featureTypeId)) {
				throw new IllegalArgumentException ("Feature type not defined: " + featureTypeId);
			}
			
			ft = featureTypes.get (featureTypeId);
		} else {
			ft = null;
		}
			
		return new ParseNamedServiceEntity (
				id.asText (),
				services.get (serviceId),
				qName (name),
				label.asText (),
				ft
			);
	}
	
	public static Layer layer (final String json, final Map<String, ServiceLayer> serviceLayers) {
		return layer (parse (json), serviceLayers);
	}
	

	
	public static Layer layer (final JsonNode node, final Map<String, ServiceLayer> serviceLayerMap) {
		final JsonNode id = node.path ("id"); 
		final JsonNode layerType = node.path ("layerType");
		final JsonNode label = node.path ("label"); 
		final JsonNode layers = node.path ("layers");
		final JsonNode serviceLayers = node.path ("serviceLayers");
		//state is initial state
		final JsonNode state = node.path("state");
		final JsonNode properties = node.path("properties");
		
		
		if (id.isMissingNode () || id.asText ().isEmpty ()) {
			throw new IllegalArgumentException ("Missing property: id");
		}
		if (layerType.isMissingNode ()) {
			throw new IllegalArgumentException ("Missing property: layerType");
		}
		if (label.isMissingNode ()) {
			throw new IllegalArgumentException ("Missing property: label");
		}
		
		final List<Layer> layerList = new ArrayList<> ();
		for (final JsonNode layerNode: layers) {
			layerList.add (layer (layerNode, serviceLayerMap));
		}
		
		final List<ServiceLayer> serviceLayerList = new ArrayList<> ();
		final Map<String, JsonNode> initialStateMap = new HashMap<> ();  
		final Map<String, JsonNode> propertiesMap = new HashMap<> ();  
		
		for (final JsonNode serviceLayerNode: serviceLayers) {
			if (!serviceLayerMap.containsKey (serviceLayerNode.asText ())) {
				throw new IllegalArgumentException ("Undefined service layer: " + serviceLayerNode.asText ());
			}
			serviceLayerList.add (serviceLayerMap.get (serviceLayerNode.asText ()));
		}
		
		if (!state.isMissingNode()){	
			for (Iterator<Entry<String,JsonNode>> iterator = state.fields(); iterator.hasNext();) {
				 Entry<String, JsonNode> item = iterator.next();   
				 initialStateMap.put(item.getKey(), item.getValue());
			}
		}
		
		if (!properties.isMissingNode()){	
			for (Iterator<Entry<String,JsonNode>> iterator = properties.fields(); iterator.hasNext();) {
				 Entry<String, JsonNode> item = iterator.next();   
				 propertiesMap.put(item.getKey(), item.getValue());
			}
		}
	
		return new Layer (
				id.asText (), 
				layerType.asText (), 
				label.asText (),  
				serviceLayerList, 
				initialStateMap, 
				propertiesMap);
	}
	
	public static JsonNode serialize (final MapDefinition mapDefinition) {
		return mapper.valueToTree (mapDefinition);
	}
	
	private static JsonNode parse (final String json) {
		try {
			return mapper.readTree (json);
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
	}
	
	private static <T> T parseObject (final JsonNode node, final Class<T> cls) {
		try {
			return mapper.treeToValue (node, cls);
		} catch (JsonProcessingException e) {
			throw new RuntimeException (e);
		}
	}
	
	
	
	private final static class ParseNamedServiceEntity {
		private final String id;
		private final Service service;
		private final QName name;
		private final String label;
		private final FeatureType featureType;
		
		public ParseNamedServiceEntity (final String id, final Service service, final QName name, final String label, final FeatureType featureType) {
			this.id = id;
			this.service = service;
			this.name = name;
			this.label = label;
			this.featureType = featureType;
		}

		public String getId() {
			return id;
		}

		public Service getService() {
			return service;
		}

		public QName getName() {
			return name;
		}

		public String getLabel() {
			return label;
		}

		public FeatureType getFeatureType() {
			return featureType;
		}
	}
	
	
	
}

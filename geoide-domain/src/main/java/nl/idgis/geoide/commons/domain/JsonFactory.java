package nl.idgis.geoide.commons.domain;

import java.io.IOException;
import java.io.InputStream;
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
			return externalize ((JsonNode) object);
		} else {
			return externalize (mapper ().valueToTree (object));
		}
	}
	
	public static ExternalizableJsonNode externalize (final JsonNode node) {
		if (node instanceof ExternalizableJsonNode) {
			return (ExternalizableJsonNode) node;
		} else {
			return new ExternalizableJsonNode (node);
		}
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
	
	public static MapDefinition mapDefinition (final String json) {
		List<JsonNode> mapEntityNodes = new ArrayList<> ();
		mapEntityNodes.add(parse (json));
		return mapDefinitions (mapEntityNodes).get(0);
	}
	
	public static MapDefinition mapDefinition (final JsonNode json) {
		List<JsonNode> mapEntityNodes = new ArrayList<> ();
		mapEntityNodes.add(json);
		return mapDefinitions (mapEntityNodes).get(0);
	}
	
	
	public static MapDefinition mapDefinition (final InputStream inputStream) {
		return mapDefinitions (inputStream).get(0);
	}
	
	public static List<MapDefinition> mapDefinitions (final InputStream... inputStreams) {
		
		List<JsonNode> mapEntityNodes = new ArrayList<> ();
		
		for (final InputStream is: inputStreams) {
			JsonNode node = parse(is);
			mapEntityNodes.add(node);
		}	
		
		return mapDefinitions (mapEntityNodes);
	}
	
	public static List<MapDefinition> mapDefinitions (List<JsonNode> mapEntityNodes) {
		JsonNode services = null; 
		JsonNode featureTypes = null;
		JsonNode serviceLayers = null;
		JsonNode layers = null;
		JsonNode maps = null;
		for (final JsonNode mapEntityNode: mapEntityNodes) {
			if (mapEntityNode.has("services")){
				services = mapEntityNode.path("services");
			}	
			if (mapEntityNode.has("featureTypes")){
				featureTypes =  mapEntityNode.path("featureTypes");	
			}
			if (mapEntityNode.has("serviceLayers")){
				serviceLayers =  mapEntityNode.path("serviceLayers");			
			}
			if (mapEntityNode.has("layers")){ 
				 layers =  mapEntityNode.path("layers");
			}
			if (mapEntityNode.has("maps")){ 
				 maps =  mapEntityNode.path("maps");
			}
		}	
		
		final Map<String, Service> serviceMap = new HashMap<> ();
		final Map<String, FeatureType> featureTypeMap = new HashMap<> ();
		final Map<String, ServiceLayer> serviceLayerMap = new HashMap<> ();
		// Parse services:
		if (services != null) {
			for (final JsonNode serviceNode: services) {
				final Service service = JsonFactory.service (serviceNode);
				serviceMap.put (service.getId (), service);
			}
		}
		// Parse feature types:
		if (featureTypes != null) {
			for (final JsonNode featureTypeNode: featureTypes) {
				final FeatureType featureType = JsonFactory.featureType (featureTypeNode, serviceMap);
				featureTypeMap.put (featureType.getId (), featureType);
			}
		}
		// Parse service layers:
		if (serviceLayers != null) {
			for (final JsonNode serviceLayerNode: serviceLayers) {
				final ServiceLayer serviceLayer = JsonFactory.serviceLayer (serviceLayerNode, serviceMap, featureTypeMap);
				serviceLayerMap.put (serviceLayer.getId (), serviceLayer);
			}	
		}
		final List<MapDefinition> mapDefinitions = new ArrayList<MapDefinition>();
		for (final JsonNode mapNode: maps) {
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
				final List<Layer> layerList = new ArrayList<> ();
				//Parse layers:
				for ( final JsonNode mapLayer: mapNode.path("maplayers")) {
					// merge maplayer and layer
					mergeLayers(mapLayer, layers);
					final Layer layer = JsonFactory.layer (mapLayer, serviceLayerMap);
					layerList.add (layer);	
				}
				mapDefinitions.add (new MapDefinition (id.asText (), label.asText (), prefix.asText(), initialExtent, layerList));
			} else {
				throw new IllegalArgumentException ("Missing property: maplayers");
			}
			
		}
		return mapDefinitions;
		
	}
	
	// method to merge maplayer and layer
	private static JsonNode mergeLayers (final JsonNode mapLayer, final JsonNode layers) {
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
		
		for (final JsonNode lyr: layers) {
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
		
		
		return new Layer (id.asText (), layerType.asText (), label.asText (), layerList, serviceLayerList, initialStateMap, propertiesMap);
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
	
	private static JsonNode parse (final InputStream inputStream) {
		try {
			return mapper.readTree (inputStream);
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

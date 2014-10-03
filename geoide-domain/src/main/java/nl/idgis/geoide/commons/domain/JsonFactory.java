package nl.idgis.geoide.commons.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFactory {
	
	private final static ObjectMapper mapper = new ObjectMapper ();
	
	public static ObjectMapper mapper () {
		return mapper;
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
		return mapDefinition (parse (json));
	}
	
	public static MapDefinition mapDefinition (final InputStream inputStream) {
		return mapDefinition (parse (inputStream));
	}
	
	public static MapDefinition mapDefinition (final JsonNode node) {
		return parseObject (node, MapDefinition.class);
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
		for (final JsonNode serviceLayerNode: serviceLayers) {
			if (!serviceLayerMap.containsKey (serviceLayerNode.asText ())) {
				throw new IllegalArgumentException ("Undefined service layer: " + serviceLayerNode.asText ());
			}
			serviceLayerList.add (serviceLayerMap.get (serviceLayerNode.asText ()));
		}
		
		return new Layer (id.asText (), layerType.asText (), label.asText (), layerList, serviceLayerList);
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

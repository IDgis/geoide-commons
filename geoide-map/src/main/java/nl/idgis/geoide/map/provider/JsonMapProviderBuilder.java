package nl.idgis.geoide.map.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

import nl.idgis.geoide.commons.domain.FeatureType;
import nl.idgis.geoide.commons.domain.JsonFactory;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.commons.domain.provider.StaticMapProvider;

public class JsonMapProviderBuilder {
	
	private final List<JsonNode> nodes;
	
	private JsonMapProviderBuilder (final JsonNode ... nodes) {
		this.nodes = new ArrayList<> (Arrays.asList (nodes));
	}
	
	public static JsonMapProviderBuilder create (final JsonNode ... nodes) {
		return new JsonMapProviderBuilder (nodes);
	}
	
	public JsonMapProviderBuilder addJson (final JsonNode ... nodes) {
		this.nodes.addAll (Arrays.asList (nodes));
		return this;
	}
	
	public StaticMapProvider build () {
		final Map<String, Service> services = parseServices ();
		final Map<String, FeatureType> featureTypes = parseFeatureTypes (services);
		final Map<String, ServiceLayer> serviceLayers = parseServiceLayers (services, featureTypes);
		final Map<String, JsonNode> layers = parseLayers ();
		
		return new StaticMapProvider (parseMaps (layers, serviceLayers));
	}
	
	private Map<String, Service> parseServices () {
		final Map<String, Service> services = new HashMap<> ();
		
		nodes
			.stream ()
			.filter (node -> node.has ("services"))
			.flatMap (node -> StreamSupport
				.stream (node.path ("services").spliterator (), false)
				.map (JsonFactory::service))
			.forEach (service -> {
				services.put (service.getId (), service);
			});
		
		return Collections.unmodifiableMap (services);
	}
	
	private Map<String, FeatureType> parseFeatureTypes (final Map<String, Service> services) {
		final Map<String, FeatureType> featureTypes = new HashMap<> ();
		
		nodes
			.stream ()
			.filter (node -> node.has ("featureTypes"))
			.flatMap (node -> StreamSupport
					.stream (node.path ("featureTypes").spliterator (), false)
					.map (n -> JsonFactory.featureType (n, services)))
			.forEach (featureType -> featureTypes.put (featureType.getId (), featureType));
		
		return Collections.unmodifiableMap (featureTypes);
	}
	
	private Map<String, ServiceLayer> parseServiceLayers (final Map<String, Service> services, final Map<String, FeatureType> featureTypes) {
		final Map<String, ServiceLayer> serviceLayers = new HashMap<> ();
		
		nodes
			.stream ()
			.filter (node -> node.has ("serviceLayers"))
			.flatMap (node -> StreamSupport
					.stream (node.path ("serviceLayers").spliterator (), false)
					.map (n -> JsonFactory.serviceLayer (n, services, featureTypes)))
			.forEach (serviceLayer -> serviceLayers.put (serviceLayer.getId (), serviceLayer));
		
		return Collections.unmodifiableMap (serviceLayers);
	}
	
	private Map<String, JsonNode> parseLayers () {
		final Map<String, JsonNode> layers = new HashMap<> ();
		
		nodes
			.stream ()
			.filter (node -> node.has ("layers"))
			.flatMap (node -> StreamSupport.stream (node.path ("layers").spliterator (), false))
			.forEach (node -> layers.put (node.path ("id").asText (), node));
		
		return Collections.unmodifiableMap (layers);
	}
	
	private Collection<MapDefinition> parseMaps (final Map<String, JsonNode> layers, final Map<String, ServiceLayer> serviceLayers) {
		final Map<String, MapDefinition> mapDefinitions = new HashMap<> ();
		
		nodes
			.stream ()
			.filter (node -> node.has ("maps"))
			.flatMap (node -> StreamSupport.stream (node.path ("maps").spliterator (), false)
				.map (mapNode -> JsonFactory.mapDefinition (mapNode, layers, serviceLayers)))
			.forEach (mapDef -> mapDefinitions.put (mapDef.getId (), mapDef));
		
		return Collections.unmodifiableCollection (mapDefinitions.values ());
	}

}

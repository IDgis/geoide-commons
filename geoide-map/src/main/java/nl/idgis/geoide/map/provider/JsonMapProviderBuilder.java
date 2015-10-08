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
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.QueryDescription;
import nl.idgis.geoide.commons.domain.QueryTerm;
import nl.idgis.geoide.commons.domain.Service;
import nl.idgis.geoide.commons.domain.ServiceLayer;
import nl.idgis.geoide.commons.domain.provider.StaticMapProvider;

/**
 * Builder utility to help with the construction of {@link StaticMapProvider} instances. Combines
 * a list of {@link JsonNode}'s and extracts the following entities from those documents:
 * 
 * - {@link Service}
 * - {@link FeatureType}
 * - {@link ServiceLayer}
 * - {@link Layer}
 * - {@link MapDefinition}
 * 
 * MapDefinitions are created by resolving references to related entities. 
 * 
 * Contents of Json nodes that are added to the builder override entities that have been previously
 * added when their id's match. 
 */
public class JsonMapProviderBuilder {
	
	private final List<JsonNode> nodes;
	
	private JsonMapProviderBuilder (final JsonNode ... nodes) {
		this.nodes = new ArrayList<> (Arrays.asList (nodes));
	}

	/**
	 * Creates a new {@link JsonMapProviderBuilder} by providing a (potentially empty) initial
	 * list of {@link JsonNode}'s containing configuration.
	 * 
	 * @param nodes	The initial list of {@link JsonNode}'s to use for configuring the {@link StaticMapProvider} instance.
	 * @return		A {@link JsonMapProviderBuilder} instance containging the given {@link JsonNode}'s.
	 */
	public static JsonMapProviderBuilder create (final JsonNode ... nodes) {
		return new JsonMapProviderBuilder (nodes);
	}

	/**
	 * Adds one or more Json nodes to this builder. Entities found in the given nodes override
	 * previous entities if they have been added with the same ID. Nodes are added in the order
	 * they are passed to this method.
	 * 
	 * @param nodes		The JSON nodes to add.
	 * @return			This {@link JsonMapProviderBuilder} instance.
	 */
	public JsonMapProviderBuilder addJson (final JsonNode ... nodes) {
		this.nodes.addAll (Arrays.asList (nodes));
		return this;
	}

	/**
	 * Parses the configuration in the {@link JsonNode}'s that have been previously added to this builder
	 * and returns a configured {@link StaticMapProvider}.
	 * 
	 * @return	A {@link StaticMapProvider} instance configured with the entities found in the Json nodes
	 * 			in this builder.
	 */
	public StaticMapProvider build () {
		final Map<String, Service> services = parseServices ();
		final Map<String, FeatureType> featureTypes = parseFeatureTypes (services);
		final Map<String, ServiceLayer> serviceLayers = parseServiceLayers (services, featureTypes);
		final Map<String, QueryDescription> queryDescriptions = parseQueryDescriptions (featureTypes, serviceLayers);  
		final Map<String, Layer> layers = parseLayers (serviceLayers);
		
		return new StaticMapProvider (parseMaps (layers, serviceLayers, queryDescriptions));
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
	
	private Map<String, QueryDescription> parseQueryDescriptions (final Map<String, FeatureType> featureTypes, final Map <String, ServiceLayer> serviceLayers) {
		final Map<String, QueryDescription> queryDescriptions = new HashMap<> ();
		nodes
		.stream ()
		.filter (node -> node.has ("queryDescriptions"))
		.flatMap (node -> StreamSupport
				.stream (node.path ("queryDescriptions").spliterator (), false)
				.map (n -> JsonFactory.queryDescription (n, featureTypes, serviceLayers)))
		.forEach (queryDescription -> queryDescriptions.put (queryDescription.getId(), queryDescription));
		
		return Collections.unmodifiableMap (queryDescriptions);
	}
	
	
	
	private Map<String, Layer> parseLayers (final Map<String, ServiceLayer> serviceLayers) {
		final Map<String, Layer> layers = new HashMap<> ();
		
		nodes
			.stream ()
			.filter (node -> node.has ("layers"))
			.flatMap (node -> StreamSupport
					.stream (node.path ("layers").spliterator (), false)
					.map ( n -> JsonFactory.layer (n, serviceLayers)))
			.forEach (layer -> layers.put (layer.getId (), layer));
		
		return Collections.unmodifiableMap (layers);
	}
	
	private Collection<MapDefinition> parseMaps (
			final Map<String, Layer> layers, 
			final Map<String, ServiceLayer> serviceLayers, 
			final Map<String, QueryDescription> queryDescriptions) {
		final Map<String, MapDefinition> mapDefinitions = new HashMap<> ();
		
		nodes
			.stream ()
			.filter (node -> node.has ("maps"))
			.flatMap (node -> StreamSupport.stream (node.path ("maps").spliterator (), false)
				.map (mapNode -> JsonFactory.mapDefinition (mapNode, layers, serviceLayers, queryDescriptions)))
			.forEach (mapDef -> mapDefinitions.put (mapDef.getId (), mapDef));
		
		return Collections.unmodifiableCollection (mapDefinitions.values ());
	}

}

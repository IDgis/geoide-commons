package nl.idgis.geoide.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.FeatureQuery;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.commons.domain.api.MapQuery;
import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.commons.domain.provider.LayerProvider;
import nl.idgis.geoide.commons.domain.query.Query;
import nl.idgis.geoide.commons.domain.query.QueryLayerInfo;
import nl.idgis.geoide.commons.domain.traits.Traits;
import nl.idgis.geoide.commons.layer.LayerType;
import nl.idgis.geoide.commons.layer.LayerTypeRegistry;

import com.fasterxml.jackson.databind.JsonNode;

public class DefaultMapQuery implements MapQuery {

	private final LayerTypeRegistry layerTypeRegistry;
	private final LayerProvider layerProvider;

	public DefaultMapQuery (final LayerTypeRegistry layerTypeRegistry, final LayerProvider layerProvider) {
		if (layerTypeRegistry == null) {
			throw new NullPointerException ("layerTypeRegistry cannot be null");
		}
		if (layerProvider == null) {
			throw new NullPointerException ("layerProvider cannot be null");
		}
		
		this.layerTypeRegistry = layerTypeRegistry;
		this.layerProvider = layerProvider;
	}
	
	@Override
	public CompletableFuture<Query> prepareQuery (final ExternalizableJsonNode input) {
		return CompletableFuture.completedFuture (parseQueryInfo (input.getJsonNode ()));
	}
	
	@Override
	public CompletableFuture<List<ParameterizedFeatureType<?>>> prepareFeatureTypes (final Query query) {
		return CompletableFuture.completedFuture (createFeatureTypes (query.getLayerInfos ()));
	}
	
	private Query parseQueryInfo (final JsonNode queryNode) {
		return new Query (parseLayerInfos (queryNode.path ("layers")), parseQuery (queryNode.path ("query")));
	}
	
	private List<QueryLayerInfo> parseLayerInfos (final JsonNode layersNode) {
		if (layersNode.isMissingNode () || !layersNode.isArray ()) {
			return Collections.emptyList ();
		}
		
		final List<QueryLayerInfo> layerInfos = new ArrayList<QueryLayerInfo> ();

		for (final JsonNode layerNode: layersNode) {
			layerInfos.add (parseLayerInfo (layerNode));
		}
		
		return layerInfos;
	}
	
	private QueryLayerInfo parseLayerInfo (final JsonNode layerNode) {
		return new QueryLayerInfo (getLayer (layerNode.path ("id")), Optional.of (layerNode.path ("state")), parseQuery (layerNode.path ("query")));
	}
	
	private Optional<FeatureQuery> parseQuery (final JsonNode queryNode) {
		if (queryNode.isMissingNode ()) {
			return Optional.empty ();
		}
		if (queryNode.path ("bbox").isMissingNode ()) {
			return Optional.empty ();
		}
		
		Envelope envelope = getEnvelope (queryNode.path ("bbox"));
		return Optional.of (new FeatureQuery (Optional.of(envelope)));
	}
	
	private Envelope getEnvelope (final JsonNode bboxNode) {
		if (bboxNode.path("minx").isMissingNode() || bboxNode.path("miny").isMissingNode() || bboxNode.path("maxx").isMissingNode() || bboxNode.path("maxy").isMissingNode()) {
			throw new IllegalArgumentException ("Bbox is missing a coördinate");
		}
		Envelope envelope = null;
		try {
			envelope  = new Envelope(bboxNode.path("minx").asDouble(),bboxNode.path("miny").asDouble(),bboxNode.path("maxx").asDouble(),bboxNode.path("maxy").asDouble()); 
		} catch (Exception e) {
			throw new IllegalArgumentException ("Error in Bbox coördinates " + e);
		}
		
		return envelope;
		
	}

	private Layer getLayer (final JsonNode id) {
		if (id == null) {
			throw new IllegalArgumentException ("Missing layer ID");
		}
		
		final Layer layer = layerProvider.getLayer (id.asText ());
		if (layer == null) {
			throw new IllegalArgumentException ("No layer found with ID " + id.asText ());
		}
		
		return layer;
	}

	private List<ParameterizedFeatureType<?>> createFeatureTypes (final List<QueryLayerInfo> layerInfos) {
		final List<ParameterizedFeatureType<?>> featureTypes = new ArrayList<> ();
		
		for (final QueryLayerInfo layerInfo: layerInfos) {
			final Traits<LayerType> layerType = layerTypeRegistry.getLayerType (layerInfo.getLayer ());
			
			featureTypes.addAll (layerType.get ().getFeatureTypes (layerInfo.getLayer (), layerInfo.getQuery (), layerInfo.getState ()));
		}
		
		return featureTypes;
	}
}

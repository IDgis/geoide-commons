package nl.idgis.geoide.commons.domain.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.commons.domain.query.Query;

public interface MapQuery {
	
	CompletableFuture<Query> prepareQuery (ExternalizableJsonNode input, String token);
	CompletableFuture<List<ParameterizedFeatureType<?>>> prepareFeatureTypes (Query query);
}

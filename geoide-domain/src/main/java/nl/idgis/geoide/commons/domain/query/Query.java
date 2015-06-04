package nl.idgis.geoide.commons.domain.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import nl.idgis.geoide.commons.domain.FeatureQuery;

public final class Query implements Serializable {
	private static final long serialVersionUID = -2734282415904161672L;
	
	private final List<QueryLayerInfo> layerInfos;
	private final FeatureQuery featureQuery;
	
	public Query (final List<QueryLayerInfo> layerInfos) {
		this (layerInfos, Optional.empty ());
	}
	
	public Query (final List<QueryLayerInfo> layerInfos, final FeatureQuery featureQuery) {
		this (layerInfos, Optional.of (featureQuery));
	}
	
	public Query (final List<QueryLayerInfo> layerInfos, final Optional<FeatureQuery> featureQuery) {
		if (featureQuery == null) {
			throw new NullPointerException ("featureQuery cannot be null");
		}
		
		this.layerInfos = layerInfos == null || layerInfos.isEmpty () ? Collections.emptyList () : new ArrayList<> (layerInfos);
		this.featureQuery = featureQuery.isPresent () ? featureQuery.get () : null;
	}

	public List<QueryLayerInfo> getLayerInfos () {
		return Collections.unmodifiableList (layerInfos);
	}
	
	public Optional<FeatureQuery> getFeatureQuery () {
		return featureQuery == null ? Optional.empty () : Optional.of (featureQuery);
	}
}

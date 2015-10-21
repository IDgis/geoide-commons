package nl.idgis.geoide.commons.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;

import nl.idgis.geoide.util.Assert;

public class QueryDescription extends Entity {

	private static final long serialVersionUID = -6404778430711796916L;
	
	private final List<QueryTerm> queryTerms;
	private final ServiceLayer layer;

	public QueryDescription(
		final @JsonProperty("id") String id,
		final @JsonProperty("label") String label,
		final @JsonProperty("queryTerms") List<QueryTerm> queryTerms,
		final @JsonProperty("serviceLayer") ServiceLayer serviceLayer) {
		
		super(id, label);
		
		Assert.notNull (queryTerms, "queryTerms");
		Assert.notNull (serviceLayer, "serviceLayer");
		
		this.queryTerms = new ArrayList<> (queryTerms);
		this.layer = serviceLayer;
		
	}

	
	@JsonValue
	public JsonNode serialize () {
		//TODO
		return null;
		
	}
	
	public List<QueryTerm> getQueryTerms () {
		return Collections.unmodifiableList (queryTerms);
	}
	
	public ServiceLayer getLayer () {
		return layer;
	}
	
	


}

package nl.idgis.ogc.client.wfs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Feature {

	private final String featureTypeName;
	private final String featureTypeNamespace;
	
	private final String id;
	private final Map<String, Object> properties;
	
	public Feature (final String featureTypeName, final String featureTypeNamespace, final String id, final Map<String, Object> properties) {
		if (featureTypeName == null) {
			throw new NullPointerException ("featureTypeName cannot be null");
		}
		if (id == null) {
			throw new NullPointerException ("id cannot be null");
		}
		
		this.featureTypeName = featureTypeName;
		this.featureTypeNamespace = featureTypeNamespace;
		this.id = id;
		this.properties = properties == null ? Collections.<String, Object>emptyMap () : new HashMap<> (properties);
	}

	public String featureTypeName () {
		return this.featureTypeName;
	}
	
	public String featureTypeNamespace () {
		return this.featureTypeNamespace;
	}
	
	public String id () {
		return this.id;
	}
	
	public Map<String, Object> properties () {
		return Collections.unmodifiableMap (properties);
	}
}

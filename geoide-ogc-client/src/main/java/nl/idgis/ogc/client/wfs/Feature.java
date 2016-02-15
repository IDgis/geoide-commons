package nl.idgis.ogc.client.wfs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for a feature. Consists of a QName, an ID and a map of properties.
 */
public final class Feature {

	private final String featureTypeName;
	private final String featureTypeNamespace;
	
	private final String id;
	private final Map<String, Object> properties;

	/**
	 * Constructs a new feature.
	 * 
	 * @param featureTypeName		The name of the feature type. Cannot be null.
	 * @param featureTypeNamespace	The namespace of the feature type. Optional: can be null.
	 * @param id					The feature ID.
	 * @param properties			A map of feature properties. A null value is interpreted as an empty map.
	 */
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

	/**
	 * @return The feature type name.
	 */
	public String featureTypeName () {
		return this.featureTypeName;
	}
	
	/**
	 * @return The feature type namespace. Or null if it has no namespace.
	 */
	public String featureTypeNamespace () {
		return this.featureTypeNamespace;
	}

	/**
	 * @return The ID of the feature.
	 */
	public String id () {
		return this.id;
	}
	
	/**
	 * @return The feature properties.
	 */
	public Map<String, Object> properties () {
		return Collections.unmodifiableMap (properties);
	}
}

package nl.idgis.planoview.commons.domain.feature;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.planoview.commons.domain.QName;

public class Feature implements Serializable {
	private static final long serialVersionUID = 4861641890552307368L;
	
	private final QName featureTypeName;
	private final String id;
	private final Map<String, Object> properties;
	
	public Feature (final QName featureTypeName, final String id, final Map<String, Object> properties) {
		if (featureTypeName == null) {
			throw new NullPointerException ("featureTypeName cannot be null");
		}
		if (id == null) {
			throw new NullPointerException ("id cannot be null");
		}
		
		this.featureTypeName = featureTypeName;
		this.id = id;
		this.properties = properties == null ? Collections.<String, Object>emptyMap () : new HashMap<String, Object> (properties);
	}

	public QName getFeatureTypeName () {
		return featureTypeName;
	}

	public String getId () {
		return id;
	}

	public Map<String, Object> getProperties () {
		return Collections.unmodifiableMap (properties);
	}
}

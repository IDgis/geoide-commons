package nl.idgis.geoide.commons.domain.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 */
public class FeatureOverlay implements Serializable {
	private static final long serialVersionUID = 304192288808773183L;
	
	private final String id;
	private final List<OverlayFeature> features;
	
	public FeatureOverlay (final String id, final List<OverlayFeature> features) {
		if (id == null) {
			throw new NullPointerException ("id cannot be null");
		}
		
		this.id = id;
		this.features = features == null || features.isEmpty () ? Collections.emptyList () : new ArrayList<> (features);
	}

	public String getId () {
		return id;
	}

	public List<OverlayFeature> getFeatures () {
		return Collections.unmodifiableList (features);
	}
}

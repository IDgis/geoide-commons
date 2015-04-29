package nl.idgis.geoide.commons.domain.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OverlayFeature implements Serializable {
	private static final long serialVersionUID = -8855880096720936051L;
	
	private final List<StyledGeometry> styledGeometry;
	private final Overlay overlay;
	
	@JsonCreator
	public OverlayFeature (
			final @JsonProperty ("styledGeometry") List<StyledGeometry> styledGeometry,
			final @JsonProperty ("overlay") Overlay overlay) {
		
		if (styledGeometry == null) {
			throw new NullPointerException ("styledGeometry cannot be null");
		}
		
		this.styledGeometry = new ArrayList<> (styledGeometry);
		this.overlay = overlay;
	}

	public List<StyledGeometry> getStyledGeometry () {
		return Collections.unmodifiableList (styledGeometry);
	}

	public Overlay getOverlay () {
		return overlay;
	}
}

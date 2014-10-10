package nl.idgis.geoide.commons.domain.toc;

import nl.idgis.geoide.commons.domain.geometry.Envelope;
import nl.idgis.geoide.util.Assert;


public class TOCItemExtentTrait implements TOCItemTrait{
	private final Envelope extent;
	
	public TOCItemExtentTrait (final Envelope extent) {
		Assert.notNull (extent, "extent");
		
		this.extent = extent;
	}
	
	public Envelope getExtent() {
		return extent;
	}
}

package nl.idgis.geoide.commons.domain.toc;

import nl.idgis.geoide.commons.domain.geometry.Envelope;


public interface TOCItemExtentTrait extends TOCItemTrait{
	//private Envelope extent;
	
	public void setExtent(Envelope extent);
	
	public Envelope getExtent();
	

}

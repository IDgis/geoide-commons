package nl.idgis.geoide.commons.domain.toc;

import java.io.Serializable;

public class Symbol implements Serializable {
	private static final long serialVersionUID = 4813371793654849019L;
	
	private final String id;
	private final String legendGraphicUrl;
	
		
	public Symbol(String id, String legendGraphicUrl) {
		this.id = id;
		this.legendGraphicUrl = legendGraphicUrl;
	}
	
	public String getId(){
		return id;
	}
	
	public String getLegendGraphicUrl(){
		return legendGraphicUrl;
	}
	
}

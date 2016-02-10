package nl.idgis.geoide.commons.domain.toc;

import java.io.Serializable;

public class Symbol implements Serializable {
	private static final long serialVersionUID = 4813371793654849019L;
	
	private final String id;
	private final String legendUrl;
	
		
	public Symbol(String id, String legendUrl) {
		this.id = id;
		this.legendUrl = legendUrl;
	}
	
	public String getId(){
		return id;
	}
	
	public String getLegendUrl(){
		return legendUrl;
	}
	
}

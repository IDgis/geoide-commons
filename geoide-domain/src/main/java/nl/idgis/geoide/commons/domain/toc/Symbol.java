package nl.idgis.geoide.commons.domain.toc;

import java.io.Serializable;


public class Symbol implements Serializable {
	private static final long serialVersionUID = 4813371793654849019L;
	
	private final String id;
	
	public Symbol(String id) {
		this.id = id;
	}
	
	public String getId(){
		return id;
	}
	

}

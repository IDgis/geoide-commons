package nl.idgis.geoide.commons.report.blocks;

import com.fasterxml.jackson.databind.JsonNode;

public interface BlockInfo {

	public void prepare();
	
	public String getBlockAttribute(String attrName);
	
	public JsonNode getClientInfo();			

}

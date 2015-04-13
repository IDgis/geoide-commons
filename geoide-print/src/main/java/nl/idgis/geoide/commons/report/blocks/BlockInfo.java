package nl.idgis.geoide.commons.report.blocks;

import java.util.Map;

import nl.idgis.geoide.commons.report.ReportData;

import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class BlockInfo {
	protected JsonNode clientInfo;
	protected Map<String, String> blockDataSet;
	protected Element block;
	protected ReportData reportData;
	
	public void prepare() {
		
	};
	
	public String getBlockAttribute(String attrName){
		return blockDataSet.get(attrName);
	}
	

	public JsonNode getClientInfo() {
		return clientInfo;
	}
			

}

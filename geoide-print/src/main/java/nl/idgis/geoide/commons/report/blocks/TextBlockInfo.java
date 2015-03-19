package nl.idgis.geoide.commons.report.blocks;

import java.util.Map;

import nl.idgis.geoide.commons.report.ReportData;

import org.jsoup.nodes.Attributes;

import com.fasterxml.jackson.databind.JsonNode;

public class TextBlockInfo implements BlockInfo {
		protected JsonNode clientInfo;
		private final Map<String, String> blockDataSet;
		private final ReportData reportData;
	
	public TextBlockInfo (JsonNode clientInfo, Attributes blockAttributes, ReportData reportData) {
			this.clientInfo = clientInfo;
			this.blockDataSet = blockAttributes.dataset();
			this.reportData = reportData;	
		}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getBlockAttribute(String attrName) {
		return blockDataSet.get(attrName);
	}

	@Override
	public JsonNode getClientInfo() {
		return clientInfo;
	}
	
	public String getText() {
		return clientInfo.path("text").asText(); 
	}
	
}

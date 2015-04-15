package nl.idgis.geoide.commons.report.blocks;

import nl.idgis.geoide.commons.report.ReportData;

import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.JsonNode;

public class TextBlockInfo extends BlockInfo {

	
	public TextBlockInfo (JsonNode clientInfo, Element block, ReportData reportData) {
		this.clientInfo = clientInfo;
		this.block = block;
		this.blockDataSet = block.attributes().dataset();
		this.reportData = reportData;	
	}
	
	public String getText() {
		return clientInfo.path("text").asText(); 
	}
	
}

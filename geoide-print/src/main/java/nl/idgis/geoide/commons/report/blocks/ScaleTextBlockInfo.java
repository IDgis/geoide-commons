package nl.idgis.geoide.commons.report.blocks;


import nl.idgis.geoide.commons.report.ReportData;

import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ScaleTextBlockInfo extends TextBlockInfo  {



	public ScaleTextBlockInfo (JsonNode clientInfo, Element block, ReportData reportData) {
		super( clientInfo, block, reportData);
	}
	
	
	public void setScale(int scale) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode info = mapper.createObjectNode();
		info.put ("tag", "p");
		info.put ("text", "1 : " + scale);
		this.clientInfo = info;
	}


}

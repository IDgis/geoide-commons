package nl.idgis.geoide.commons.report.blocks;

import java.util.Map;

import org.jsoup.nodes.Attributes;

import nl.idgis.geoide.commons.report.ReportData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ScaleTextBlockInfo extends TextBlockInfo implements BlockInfo {



	public ScaleTextBlockInfo (JsonNode clientInfo, Attributes blockAttributes, ReportData reportData) {
		super( clientInfo, blockAttributes, reportData);
		prepare();
	}
	
	@Override
	public void prepare() {
		
	}

	
	public void setScale(int scale) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode info = mapper.createObjectNode();
		info.put ("tag", "p");
		info.put ("text", "1 : " + scale);
		this.clientInfo = info;
	}


}

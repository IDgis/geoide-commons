package nl.idgis.geoide.commons.report.blocks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import nl.idgis.geoide.commons.report.ReportData;

import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DateBlockInfo extends TextBlockInfo {
	
	public DateBlockInfo (JsonNode clientInfo, Element block, ReportData reportData) {
		super( clientInfo, block, reportData);
		prepare();
	}
	
	public void prepare() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode info = mapper.createObjectNode();
		info.put ("tag", "p");
		info.put ("text", dateFormat.format(date));
		clientInfo = info;
	}


}

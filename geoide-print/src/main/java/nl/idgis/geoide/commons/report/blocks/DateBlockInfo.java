package nl.idgis.geoide.commons.report.blocks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.nodes.Attributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.idgis.geoide.commons.report.ReportData;

public class DateBlockInfo extends TextBlockInfo implements BlockInfo {
	
	public DateBlockInfo (JsonNode clientInfo, Attributes blockAttributes, ReportData reportData) {
		super( clientInfo, blockAttributes, reportData);
		prepare();
	}
	
	@Override
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

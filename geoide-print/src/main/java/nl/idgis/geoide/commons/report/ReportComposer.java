package nl.idgis.geoide.commons.report;

import com.fasterxml.jackson.databind.JsonNode;

public class ReportComposer {
	private final ReportPostProcessor writer;
	
	public ReportComposer (ReportPostProcessor writer) {
		this.writer = writer;
		
	}
	
	public void compose (JsonNode printInfo) {
		
	}
	

}

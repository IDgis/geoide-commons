package nl.idgis.geoide.commons.report.blocks;

import nl.idgis.geoide.commons.report.ReportData;

import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.JsonNode;

public class ScaleBarBlockInfo extends BlockInfo {

	public ScaleBarBlockInfo (JsonNode clientInfo, Element block, ReportData reportData) {
		this.clientInfo = clientInfo;
		this.block = block;
		this.blockDataSet = block.attributes().dataset();
		this.reportData = reportData;	
		prepare();
	}
	
	
	public void prepare() {
		 final int scale = clientInfo.path("scale").asInt();
		 int nrOfRects = 2;
		 if(this.getBlockAttribute("nr_of_rects") != null){
			 nrOfRects = Integer.parseInt(this.getBlockAttribute("nrofrects"));
		 }
		 
				 
		 
		
	}

	
}

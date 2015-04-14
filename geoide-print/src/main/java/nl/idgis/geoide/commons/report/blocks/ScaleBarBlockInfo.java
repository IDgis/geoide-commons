package nl.idgis.geoide.commons.report.blocks;

import nl.idgis.geoide.commons.report.ReportData;

import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.JsonNode;

public class ScaleBarBlockInfo extends BlockInfo {
	private final int scale;
	private double rectWidth; 
	private int nrOfRects = 2; 
	private final double totalWidthmm;
	
	
	
	public ScaleBarBlockInfo (JsonNode clientInfo, Element block, ReportData reportData) {
		this.clientInfo = clientInfo;
		this.block = block;
		this.blockDataSet = block.attributes().dataset();
		this.reportData = reportData;
		scale = clientInfo.path("scale").asInt();
			
		if(this.getBlockAttribute("nrofrects") != null){
			nrOfRects = Integer.parseInt(this.getBlockAttribute("nrofrects"));
		} 
			
		
		int gridWidth = BlockUtil.getGridWidth(block);

		totalWidthmm = (reportData.getReportWidth() -  (reportData.getColCount() - 1) * reportData.getGutterH()) / reportData.getColCount() * gridWidth  + ((gridWidth - 1) * reportData.getGutterH());
					
		double maxRectWidthmm = (totalWidthmm - 15) / nrOfRects;
		
		double maxRectWidthm = (scale/1000) * maxRectWidthmm;
		
		
		
		
		int multiplier = 1;
		int n = 1;
		while (n < String.valueOf((int) maxRectWidthm).length()) {
			multiplier = multiplier * 10; 
			n++;
		}
		
		
		
		double [] niceScales = {10,7.5,5,4,3,2.5,2,1.5,1};
		
		rectWidth = 0; 
		
		for (int s=0; s < niceScales.length; s++) {
			int test = (int) Math.floor(maxRectWidthm/(multiplier*niceScales[s]));
			if (test == 1) {
				rectWidth =  multiplier*niceScales[s];
				break;
			}
		}		
		
	}

	public int getNumberOfRects () {
		return nrOfRects;
		
	}
	public double getRectWidthmm () {
		return rectWidth/(scale/1000);
	}
	
	
	public double getTotalWidthmm () {
		return totalWidthmm;
	}
	
	
	public String getScaleBarText (int n) {
		if(String.valueOf(rectWidth).length() > 3) {
			return String.valueOf((n *  rectWidth)/1000) + ((n == nrOfRects ? " km" : ""));
		} else {
			return String.valueOf(n *  rectWidth) + ((n == nrOfRects ? " m" : ""));
		}

	}
	
	

	
}

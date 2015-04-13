package nl.idgis.geoide.commons.report.blocks;

import java.util.Hashtable;

import nl.idgis.geoide.commons.report.ReportData;

import org.jsoup.nodes.Element;

import com.fasterxml.jackson.databind.JsonNode;

public class ScaleBarBlockInfo extends BlockInfo {
	private double rectWidth; 
	private int nrOfRects = 2; 
	double totalWidthmm;
	
	
	public ScaleBarBlockInfo (JsonNode clientInfo, Element block, ReportData reportData) {
		this.clientInfo = clientInfo;
		this.block = block;
		this.blockDataSet = block.attributes().dataset();
		this.reportData = reportData;
		
		
		
		
		prepare();
	}
	
	
	public void prepare() {
		
		final int scale = clientInfo.path("scale").asInt();
		
		if(this.getBlockAttribute("nr-of-rects") != null){
			nrOfRects = Integer.parseInt(this.getBlockAttribute("nrofrects"));
		}
		
		int gridWidth = BlockUtil.getGridWidth(block);

		totalWidthmm = (reportData.getReportWidth() -  (reportData.getColCount() - 1) * reportData.getGutterH()) / reportData.getColCount();
					
		double maxRectWidthmm = ((totalWidthmm * gridWidth) + ((gridWidth - 1) * reportData.getGutterH()) / nrOfRects);
		
		double maxRectWidthm = scale/(maxRectWidthmm * 1000);
		
		
		
		
		int multiplier = 1;
		int n = 1;
		while (n < String.valueOf(scale).length()) {
			multiplier = multiplier * 10; 
			n++;
		}
		
		
		
		double [] niceScales = {10,7.5,5,4,3,2.5,1};
		
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
		return rectWidth;
	}
	
	
	public double getTotalWidthmm () {
		return totalWidthmm;
	}
	
	
	public String getScaleBarText (int n) {
		String text = String.valueOf(n *  rectWidth);
		
		if(String.valueOf(rectWidth).length() > 3) {
			return text += " km";
		} else {
			return text += " m";
		}

	}
	
	

	
}

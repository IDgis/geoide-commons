package nl.idgis.geoide.commons.report.blocks;

import java.util.Map;

import nl.idgis.geoide.commons.report.ReportData;

import org.jsoup.nodes.Attributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MapBlockInfo implements BlockInfo {
	private final JsonNode clientInfo;
	private final Map<String, String> blockDataSet;
	private final ReportData reportData;
	private double scale;
	private double centerX;
	private double centerY;
	private double blockWidthmm;
	private double blockHeightmm;
	private double resolution;
	
	
	public MapBlockInfo (JsonNode clientInfo, Attributes blockAttributes, ReportData reportData) {
		this.clientInfo = clientInfo;
		this.blockDataSet = blockAttributes.dataset();
		this.reportData = reportData;	
		prepare();
	}
	
	public void prepare () {
		resolution = clientInfo.path("resolution").asDouble();
		final JsonNode extent = clientInfo.path("extent");
		scale = clientInfo.path("scale").asInt();	
		
		int gridWidth;
		try {
			gridWidth = Integer.parseInt(blockDataSet.get("grid-width"));
		} catch (NumberFormatException e) {
			gridWidth = 12;
		}
		int gridHeight;
		try {
			gridHeight = Integer.parseInt(blockDataSet.get("grid-height"));
		} catch (NumberFormatException e) {
			gridHeight = 12;
		}
		final boolean scaleFixed = Boolean.getBoolean(blockDataSet.get("scale-fixed"));
		
		blockWidthmm = reportData.getReportWidth() * gridWidth/12; 
		blockHeightmm = reportData.getReportHeight() * gridHeight/12; 
		
		//TODO: CenterScale with resolution	
		centerX = extent.path("minx").asDouble() + ((extent.path("maxx").asDouble() - extent.path("minx").asDouble())/2);
		centerY = extent.path("miny").asDouble() + ((extent.path("maxy").asDouble() - extent.path("miny").asDouble())/2);
				
		if (!scaleFixed || scale == 0) {
			double width = extent.path("maxx").asDouble() - extent.path("minx").asDouble();
			double height = extent.path("maxy").asDouble() - extent.path("miny").asDouble();
			if(width/blockWidthmm > height/blockHeightmm){
				scale = width * 1000 / blockWidthmm;
				//width bepaalt schaal
			} else {
				//height bepaalt schaal
				scale = height * 1000 / blockHeightmm;
			}	
		}
	}
	
	public JsonNode getClientInfo() {
		return clientInfo;
	}
	
	public String getBlockAttribute(String attrName){
		return blockDataSet.get(attrName);
	}
		
	public double getScale(){
		return scale;
	}
	
	public double getCenterX() {
		return centerX;
	}
	
	public double getCenterY() {
		return centerY;
	}
	
	public double getResolution() {
		return this.resolution;
	}
	
	public double getBlockWidth () {
		return blockWidthmm;
	}
	
	public double getBlockHeight () {
		return blockHeightmm;
	}
	
	public int getWidthpx () {
		return (int) (blockWidthmm / 0.28);
	}
	
	public int getHeightpx () {
		return (int) (blockHeightmm / 0.28);
	}
	
	public JsonNode getMapExtent() {
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode mapExtent = mapper.createObjectNode();
		
		mapExtent.put ("minx", centerX - ((scale/1000) * blockWidthmm)/2);
		mapExtent.put ("maxx", centerX + ((scale/1000) * blockWidthmm)/2);
		mapExtent.put ("miny", centerY - ((scale/1000) * blockHeightmm)/2);
		mapExtent.put ("maxy", centerY + ((scale/1000) * blockHeightmm)/2);
		
		return mapExtent;
	}
	
	public double getResizeFactor() {
		double mapWidthm = (centerX + ((scale/1000) * blockWidthmm)/2) - (centerX - ((scale/1000) * blockWidthmm)/2);
		return resolution/(mapWidthm/(blockWidthmm / 0.28));
	}
	
}
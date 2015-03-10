package nl.idgis.geoide.commons.report.blocks;

import com.fasterxml.jackson.databind.JsonNode;

public class CenterScale {
	private double scale;
	private final double centerX;
	private final double centerY;
	
	public CenterScale(boolean scaleFixed, int viewerScale, JsonNode viewerExtent, double blockWidthmm, double blockHeightmm) {
		
		this.centerX = viewerExtent.path("minx").asDouble() + ((viewerExtent.path("maxx").asDouble() - viewerExtent.path("minx").asDouble())/2);
		this.centerY = viewerExtent.path("miny").asDouble() + ((viewerExtent.path("maxy").asDouble() - viewerExtent.path("miny").asDouble())/2);
		this.scale = 0;
		
		if (scaleFixed && viewerScale != 0) {
			scale = viewerScale;
		} else {
			double width = viewerExtent.path("maxx").asDouble() - viewerExtent.path("minx").asDouble();
			double height = viewerExtent.path("maxy").asDouble() - viewerExtent.path("miny").asDouble();
			if(width/blockWidthmm > height/blockHeightmm){
				scale = width * 1000 / blockWidthmm;
				//width bepaalt schaal
			} else {
				//height bepaalt schaal
				scale = height * 1000 / blockHeightmm;
			}
			
		}
	}
	
	public CenterScale (double scale, double centerX, double centerY) {
		this.scale = scale;
		this.centerX = centerX;
		this.centerY = centerY;
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
}

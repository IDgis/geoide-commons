package nl.idgis.geoide.commons.report;

public enum PaperFormat {
	
	A3landscape(420,297), A3portrait(297,420), A4landscape(210,297), A4portrait(297,210);
	private final double width;
	private final double height; 
	
	PaperFormat(double width, double height){
		this.width = width;
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}
	
	
}

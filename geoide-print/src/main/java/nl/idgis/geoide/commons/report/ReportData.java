package nl.idgis.geoide.commons.report;

/**
 * An object to store reportData wich are not reportBlock specific
 */

	
	public class ReportData {
		PaperFormat format;
	    private final double leftMargin;
	    private final double rightMargin;
	    private final double topMargin;
	    private final double bottomMargin;
		
	    public ReportData (PaperFormat format, double leftMargin, double rightMargin, double topMargin, double bottomMargin) {
	        this.format = format;
	        this.leftMargin = leftMargin;
	        this.rightMargin = rightMargin;
	        this.topMargin = topMargin;
	        this.bottomMargin = bottomMargin;
	    }
	    
	    /**
	     * Gives the width in mm of a report based on the (paper) format and the margins
	     * 
	     * @return report width in mm 
	     */
	    
	    public double getReportWidth() {
	        return format.getWidth() - leftMargin - rightMargin;
	    }
	    
	    public double getReportHeight() {
	        return format.getHeight() - topMargin - bottomMargin;
	    }
	    

}

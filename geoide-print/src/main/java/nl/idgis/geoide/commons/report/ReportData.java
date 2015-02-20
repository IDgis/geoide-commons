package nl.idgis.geoide.commons.report;

/**
 * 
 */


	
	public class ReportData {
		PaperFormat format;
	    int leftMargin;
	    int rightMargin;
		
	    public ReportData (PaperFormat format, int leftMargin, int rightMargin) {
	        this.format = format;
	        this.leftMargin = leftMargin;
	        this.rightMargin = rightMargin;
	    }
	    
	    /**
	     * Gives the width in mm of a report based on the (paper) format and the margins
	     * 
	     * @return report width in mm 
	     */
	    
	    public int getReportWidth() {
	        switch (format) {
	            case A4landscape:
	                return 297 - leftMargin - rightMargin;
	            case A4portrait:
	                return 210 - leftMargin - rightMargin;
	            case A3landscape:
	                return 420 - leftMargin - rightMargin;
	            case A3portrait:
	            	return 297 - leftMargin - rightMargin;                
	            default:
	            	return 210 - leftMargin - rightMargin;
	            }
	    }

}

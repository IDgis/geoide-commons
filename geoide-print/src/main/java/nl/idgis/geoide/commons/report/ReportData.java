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
	    private final double gutterH;
	    private final double gutterV;
	    private final int rowCount;
	    private final int colCount;
		
	    public ReportData (PaperFormat format, double leftMargin, double rightMargin, double topMargin, double bottomMargin, double gutterH, double gutterV, int rowCount, int colCount) {
	        this.format = format;
	        this.leftMargin = leftMargin;
	        this.rightMargin = rightMargin;
	        this.topMargin = topMargin;
	        this.bottomMargin = bottomMargin;
	        this.gutterH= gutterH;
	        this.gutterV= gutterV;
	        this.rowCount = rowCount;
	        this.colCount = colCount;
	        
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
	    
	    public double getGutterH() {
	    	return gutterH;
	    }
	    
	    public double getGutterV() {
	    	return gutterV;
	    }
	    
	    public PaperFormat getFormat() {
			return format;
		}

		public double getLeftMargin() {
			return leftMargin;
		}

		public double getRightMargin() {
			return rightMargin;
		}

		public double getTopMargin() {
			return topMargin;
		}

		public double getBottomMargin() {
			return bottomMargin;
		}

		public int getRowCount() {
			return rowCount;
		}

		public int getColCount() {
			return colCount;
		}

	    

}

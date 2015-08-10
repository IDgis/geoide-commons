package nl.idgis.geoide.commons.report;

import java.util.Objects;

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
		
		public static Builder build () {
			return new Builder ();
		}

	    public final static class Builder {
			private PaperFormat format = PaperFormat.A4portrait;
		    private double leftMargin = 15;
		    private double rightMargin = 15;
		    private double topMargin = 15;
		    private double bottomMargin = 15;
		    private double gutterH = 2;
		    private double gutterV = 2;
		    private int rowCount = 12;
		    private int colCount = 12;
		    
			public PaperFormat getFormat () {
				return format;
			}
			
			public Builder setFormat (final PaperFormat format) {
				this.format = Objects.requireNonNull (format, "format cannot be null");
				return this;
			}
			
			public double getLeftMargin () {
				return leftMargin;
			}
			
			public Builder setLeftMargin (final double leftMargin) {
				this.leftMargin = leftMargin;
				return this;
			}
			
			public double getRightMargin () {
				return rightMargin;
			}
			
			public Builder setRightMargin (final double rightMargin) {
				this.rightMargin = rightMargin;
				return this;
			}
			
			public double getTopMargin () {
				return topMargin;
			}
			
			public Builder setTopMargin (final double topMargin) {
				this.topMargin = topMargin;
				return this;
			}
			
			public double getBottomMargin() {
				return bottomMargin;
			}
			
			public Builder setBottomMargin (final double bottomMargin) {
				this.bottomMargin = bottomMargin;
				return this;
			}
			
			public double getGutterH() {
				return gutterH;
			}
			
			public Builder setGutterH (final double gutterH) {
				this.gutterH = gutterH;
				return this;
			}
			
			public double getGutterV () {
				return gutterV;
			}
			
			public Builder setGutterV (final double gutterV) {
				this.gutterV = gutterV;
				return this;
			}
			
			public int getRowCount () {
				return rowCount;
			}
			
			public Builder setRowCount (final int rowCount) {
				this.rowCount = rowCount;
				return this;
			}
			
			public int getColCount () {
				return colCount;
			}
			
			public Builder setColCount (final int colCount) {
				this.colCount = colCount;
				return this;
			}
			
			public ReportData create () {
				return new ReportData (
						format, 
						leftMargin, 
						rightMargin, 
						topMargin, 
						bottomMargin, 
						gutterH, 
						gutterV, 
						rowCount, 
						colCount
					);
			}
	    }

}

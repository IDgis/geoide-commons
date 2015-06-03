package nl.idgis.geoide.commons.domain.report;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TemplateDocument implements Serializable {
	private static final long serialVersionUID = -346872816444959310L;
	
	private final URI uri;
	private final double rightMargin;
	private final double leftMargin;
	private final double topMargin;
	private final double bottomMargin;
	private final String pageFormat;
	private final String pageOrientation;
	private final URI documentUri;
	private final int colCount;
	private final int rowCount;
	private final double gutterH;
	private final double gutterV;
	private final String template;
	private final String description;
	private final List<TemplateVariable> variables;
	private final String content;

	public TemplateDocument (
		final URI uri,
		final double rightMargin,
		final double leftMargin,
		final double topMargin,
		final double bottomMargin,
		final String pageFormat,
		final String pageOrientation,
		final URI documentUri,
		final int colCount,
		final int rowCount,
		final double gutterH,
		final double gutterV,
		final String template,
		final String description,
		final List<TemplateVariable> variables,
		final String content) {
		
		this.uri = uri;
		this.rightMargin = rightMargin;
		this.leftMargin = leftMargin;
		this.topMargin = topMargin;
		this.bottomMargin = bottomMargin;
		this.pageFormat = pageFormat;
		this.pageOrientation = pageOrientation;
		this.documentUri = documentUri;
		this.colCount = colCount;
		this.rowCount = rowCount;
		this.gutterH = gutterH;
		this.gutterV = gutterV;
		this.template = template;
		this.description = description;
		this.variables = variables == null || variables.isEmpty () ? Collections.emptyList () : new ArrayList<> (variables);
		this.content = content;
	}
	
	public static Builder build () {
		return new Builder ();
	}
	
	public URI getUri () {
		return uri;
	}
	
	public double getRightMargin () {
		return rightMargin;
	}
	
	public double getLeftMargin () {
		return leftMargin;
	}
	
	public double getTopMargin () {
		return topMargin;
	}
	
	public double getBottomMargin () {
		return bottomMargin;
	}
	
	public String getPageFormat () {
		return pageFormat;
	}
	
	public String getPageOrientation () {
		return pageOrientation;
	}
	
	public URI getDocumentUri () {
		return documentUri;
	}
	
	public int getColCount () {
		return colCount;
	}
	
	public int getRowCount () {
		return rowCount;
	}
	
	public double getGutterH () {
		return gutterH;
	}
	
	public double getGutterV () {
		return gutterV;
	}
	
	public String getTemplate () {
		return template;
	}

	public String getDescription () {
		return description;
	}
	
	public List<TemplateVariable> getVariables () {
		return Collections.unmodifiableList (variables);
	}
	
	public String getContent () {
		return content;
	}

	public final static class Builder {
		private URI uri;
		private double rightMargin;
		private double leftMargin;
		private double topMargin;
		private double bottomMargin;
		private String pageFormat;
		private String pageOrientation;
		private URI documentUri;
		private int colCount;
		private int rowCount;
		private double gutterH;
		private double gutterV;
		private String template;
		private String description;
		private List<TemplateVariable> variables;
		private String content;
		
		public TemplateDocument create () {
			return new TemplateDocument (uri, rightMargin, leftMargin, topMargin, bottomMargin, pageFormat, pageOrientation, documentUri, colCount, rowCount, gutterH, gutterV, template, description, variables, content); 
		}
		
		public Builder setUri(URI uri) {
			this.uri = uri;
			return this;
		}
		public Builder setRightMargin(double rightMargin) {
			this.rightMargin = rightMargin;
			return this;
		}
		public Builder setLeftMargin(double leftMargin) {
			this.leftMargin = leftMargin;
			return this;
		}
		public Builder setTopMargin(double topMargin) {
			this.topMargin = topMargin;
			return this;
		}
		public Builder setBottomMargin(double bottomMargin) {
			this.bottomMargin = bottomMargin;
			return this;
		}
		public Builder setPageFormat(String pageFormat) {
			this.pageFormat = pageFormat;
			return this;
		}
		public Builder setPageOrientation(String pageOrientation) {
			this.pageOrientation = pageOrientation;
			return this;
		}
		public Builder setDocumentUri(URI documentUri) {
			this.documentUri = documentUri;
			return this;
		}
		public Builder setColCount(int colCount) {
			this.colCount = colCount;
			return this;
		}
		public Builder setRowCount(int rowCount) {
			this.rowCount = rowCount;
			return this;
		}
		public Builder setGutterH(double gutterH) {
			this.gutterH = gutterH;
			return this;
		}
		public Builder setGutterV(double gutterV) {
			this.gutterV = gutterV;
			return this;
		}
		public Builder setTemplate(String template) {
			this.template = template;
			return this;
		}
		public Builder setDescription(String description) {
			this.description = description;
			return this;
		}
		public Builder setContent (final String content) {
			this.content = content;
			return this;
		}
		
		public Builder addVariable (final String name, final String defaultValue, final int maxwidth) {
			variables.add (new TemplateVariable (name, defaultValue, maxwidth));
			return this;
		}

		public URI getUri() {
			return uri;
		}
		public double getRightMargin() {
			return rightMargin;
		}
		public double getLeftMargin() {
			return leftMargin;
		}
		public double getTopMargin() {
			return topMargin;
		}
		public double getBottomMargin() {
			return bottomMargin;
		}
		public String getPageFormat() {
			return pageFormat;
		}
		public String getPageOrientation() {
			return pageOrientation;
		}
		public URI getDocumentUri() {
			return documentUri;
		}
		public int getColCount() {
			return colCount;
		}
		public int getRowCount() {
			return rowCount;
		}
		public double getGutterH() {
			return gutterH;
		}
		public double getGutterV() {
			return gutterV;
		}
		public String getTemplate () {
			return template;
		}
		public String getDescription () {
			return description;
		}
		public List<TemplateVariable> getVariables () {
			return variables;
		}
		public String getContent () {
			return content;
		}
	}
}

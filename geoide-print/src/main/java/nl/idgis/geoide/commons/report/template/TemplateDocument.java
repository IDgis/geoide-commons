package nl.idgis.geoide.commons.report.template;

import java.net.URI;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public interface TemplateDocument  {
		public double getRightMargin();
		public double getLeftMargin();
		public double getTopMargin();
		public double getBottomMargin();
		public String getPageFormat();
		public String getPageOrientation();
		public URI getDocumentUri();
		public int getColCount();
		public int getRowCount();
		double getGutterH();
		double getGutterV();
		Elements getBlocks();
		String asString();
		Document getDocument();
}

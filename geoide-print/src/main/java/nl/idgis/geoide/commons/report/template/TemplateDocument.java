package nl.idgis.geoide.commons.report.template;

import java.net.URI;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public interface TemplateDocument  {
		public Elements getBlocks(); 
		public String asString();
		public Elements select(String cssQuery);
		public Element append(String html); 
		public Element body();
		public Element head();
		public Element child(int childNumber);
		public String attr (String attributeKey);
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
}

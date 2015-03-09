package nl.idgis.geoide.commons.report.template;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public interface TemplateDocument  {
		public Elements getBlocks(); 
		public String getStoreUri(); 
		public String asString();
		public Elements select(String cssQuery);
		public Element append(String html); 
		public Element body();
		public Element head();
		public Element child(int childNumber);
		public String attr (String attributeKey);
		double getRightMargin();
		double getLeftMargin();
		double getTopMargin();
		double getBottomMargin();
		String getPageFormat();
		String getPageOrientation();
}

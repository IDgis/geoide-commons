package nl.idgis.geoide.commons.report.template;

import org.jsoup.select.Elements;

public interface TemplateDocument  {
		public Elements getBlocks(); 
		public String getStoreUri(); 
		public String asString();
}

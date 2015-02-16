package nl.idgis.geoide.commons.report.template;


import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HtmlTemplateDocument extends Document implements TemplateDocument {
	private final String storeUri;

	public HtmlTemplateDocument(String baseUri) {		
		super(baseUri);
		this.storeUri = baseUri;
	}

	@Override
	public Elements getBlocks() {
		return this.getElementsByClass("block"); 
	}

	@Override	
	public String getStoreUri() {
		return storeUri;
	}

	@Override
	public String asString() {
		return this.child(0).html();
	}; 


}

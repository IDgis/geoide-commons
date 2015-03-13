package nl.idgis.geoide.commons.report.template;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

	@Override
	public double getRightMargin() {
		if(this.select("html").attr("data-right-margin") != "") {
			return  Double.parseDouble(this.select("html").attr("data-right-margin"));
		} else { 
			return 20;	
		}
	}

	@Override
	public double getLeftMargin() {
		if(this.select("html").attr("data-left-margin") != "") {
			return  Double.parseDouble(this.select("html").attr("data-left-margin"));
		} else {
			return 20;
		}
	}
	
	@Override
	public double getTopMargin() {
		if(this.select("html").attr("data-top-margin") != "")	{
			return  Double.parseDouble(this.select("html").attr("data-top-margin"));
		} else {
			return 20;
		}
	}
	
	@Override
	public double getBottomMargin() {
		if(this.select("html").attr("data-bottom-margin") != "") {
			return  Double.parseDouble(this.select("html").attr("data-bottom-margin"));
		} else {
			return 20;
		}
	}
	
	@Override
	public String getPageFormat() {
		 return this.select("html").attr("data-pageformat"); 
		 
	}
	
	@Override
	public String getPageOrientation() {
		 return this.select("html").attr("data-page-orientation");
	}
	
	
	public Elements select(String query) {
		return super.select(query);
	}
	
	public Element append(String html) {
		return super.append(html);
	}
	
	public Element body() {
		return super.body();
	}
	
	public Element head() {
		return super.head();
	}
	
	public Element child (int childnumber) {
		return super.child(childnumber);
	}

	public String attr (String attributeKey) {
		return super.attr(attributeKey);
	}
	
	

}

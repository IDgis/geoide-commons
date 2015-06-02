package nl.idgis.geoide.commons.report.template;


import java.net.URI;

import nl.idgis.geoide.commons.domain.report.TemplateDocument;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HtmlTemplateDocument implements TemplateDocument {
	private final URI baseUri;
	private final URI templateUri;
	private final Document html; 

	public HtmlTemplateDocument(URI templateUri, String html, final URI baseUri) {		
		this.templateUri = templateUri;	
		this.baseUri = baseUri;
		
		this.html = Jsoup.parse(html, templateUri.toString());

	}

	@Override
	public URI getUri () {
		return baseUri;
	}
	
	@Override 
	public Document getDocument() {
		return html;
	}
	
	@Override
	public Elements getBlocks() {
		return html.getElementsByClass("block"); 
	}
	
	@Override
	public URI getDocumentUri() {
		return templateUri;
	}

	@Override
	public String asString() {
		return html.child(0).html();
	}; 

	@Override
	public double getRightMargin() {
		if(html.select("html").attr("data-right-margin") != "") {
			return  Double.parseDouble(html.select("html").attr("data-right-margin"));
		} else { 
			return 20;	
		}
	}

	@Override
	public double getLeftMargin() {
		if(html.select("html").attr("data-left-margin") != "") {
			return  Double.parseDouble(html.select("html").attr("data-left-margin"));
		} else {
			return 20;
		}
	}
	
	@Override
	public double getTopMargin() {
		if(html.select("html").attr("data-top-margin") != "")	{
			return  Double.parseDouble(html.select("html").attr("data-top-margin"));
		} else {
			return 20;
		}
	}
	
	@Override
	public double getBottomMargin() {
		if(html.select("html").attr("data-bottom-margin") != "") {
			return  Double.parseDouble(html.select("html").attr("data-bottom-margin"));
		} else {
			return 20;
		}
	}
	
	@Override
	public String getPageFormat() {
		if(html.select("html").attr("data-pageformat") != "") {
			return html.select("html").attr("data-pageformat"); 
		} else {
			return "A4";
		}
		 
	}
	
	@Override
	public String getPageOrientation() {
		if(html.select("html").attr("data-page-orientation") != "") {
			return html.select("html").attr("data-page-orientation");
		} else {
			return "portrait";
		}
	}
	
	@Override
	public double getGutterH() {
		if(html.select("html").attr("data-gutter-h") != "") {
			return Double.parseDouble(html.select("html").attr("data-gutter-h"));
		} else {
			return 2;
		}
	}
	
	@Override
	public double getGutterV() {
		if(html.select("html").attr("data-gutter-v") != "") {
			return Double.parseDouble(html.select("html").attr("data-gutter-v"));
		} else {
			return 2;
		}
	}
	
	@Override
	public int getColCount() {
		if(html.select("html").attr("data-col-count") != "") {
			return Integer.parseInt(html.select("html").attr("data-col-count"));
		} else {
			return 12;
		}
	}

	@Override
	public int getRowCount() {
		if(html.select("html").attr("data-row-count") != "") {
			return Integer.parseInt(html.select("html").attr("data-row-count"));
		} else {
			return 12;
		}
	}
	

	

}

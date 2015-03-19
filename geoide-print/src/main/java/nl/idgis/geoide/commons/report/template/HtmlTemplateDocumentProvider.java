package nl.idgis.geoide.commons.report.template;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import play.Play;

public class HtmlTemplateDocumentProvider implements TemplateDocumentProvider {

	@Override
	public HtmlTemplateDocument getTemplateDocument(String templateUrl) {

		HtmlTemplateDocument templateDoc = null;
		try {
			templateDoc = new HtmlTemplateDocument("stored://" + UUID.randomUUID ().toString ());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document template = null;
		try {
			template = Jsoup.parse(Play.application().resourceAsStream (templateUrl),"UTF-8",templateUrl + ".html");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		templateDoc.appendChild(template);
		
		return templateDoc;
	}

}

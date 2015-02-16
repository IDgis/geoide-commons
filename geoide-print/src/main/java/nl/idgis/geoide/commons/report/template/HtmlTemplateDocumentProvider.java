package nl.idgis.geoide.commons.report.template;

import java.io.IOException;
import java.util.UUID;





import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import play.Play;

public class HtmlTemplateDocumentProvider implements TemplateDocumentProvider {

	@Override
	public HtmlTemplateDocument getTemplateDocument(String templateUrl) throws IOException {

		HtmlTemplateDocument storedTemplate = new HtmlTemplateDocument("stored://" + UUID.randomUUID ().toString ());
		Document template = Jsoup.parse(Play.application().resourceAsStream (templateUrl),"UTF-8",templateUrl);
		storedTemplate.appendChild(template);
		
		return storedTemplate;
	}

}

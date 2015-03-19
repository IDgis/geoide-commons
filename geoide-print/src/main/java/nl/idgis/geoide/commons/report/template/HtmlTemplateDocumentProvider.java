package nl.idgis.geoide.commons.report.template;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
		
		//InputStream is;

	    try {
	        //is = new FileInputStream(templateUrl + "/report.html");
	        template = Jsoup.parse(Play.application().resourceAsStream (templateUrl + "/report.html"),"UTF-8",templateDoc.getDocumentUri().toString());
	       // is.close(); 
	    } catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
				
		templateDoc.appendChild(template);
		
		return templateDoc;
	}

}

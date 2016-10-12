package nl.idgis.geoide.documentcache.service;

import java.net.URI;
import java.net.URLEncoder;

import org.junit.Test;

public class URITest {

	@Test
	public void doSomeTests() throws Exception {
		String str = "http://odru.gispubliek.nl/deegree-webservices/services/wms_archeologie?request=GetLegendGraphic&version=1.1.1&service=WMS&layer=Archeologie Bunnik&style=archeologie_bunnik&format=image/png";
		
		String separator = "";
		StringBuilder builder = new StringBuilder(str.substring(0, str.lastIndexOf("?")));
		for(String part : str.substring(str.lastIndexOf("?")).split("&")) {
			int pos = part.indexOf("=");
			String key = part.substring(0, pos);
			String value = part.substring(pos + 1);
			builder
				.append(separator)
				.append(key)
				.append("=")
				.append(URLEncoder.encode(value, "utf-8"));
			
			separator = "&";
		}
		
		URI uri = new URI(builder.toString());
		System.out.println(uri);
	}
}

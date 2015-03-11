package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;

import nl.idgis.geoide.commons.report.ReportData;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import play.libs.F.Promise;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface for a specialized report composer component. 
 * Composes a report block with information from the client and a (part of) a report template
 **/ 

public interface BlockComposer {
	
	 
	/**
	 * Composes a report block with information from the client and a (part of) a report template
	 * 
	 * @param blockInfo		a Json node with information from the client.
	 * @param block			(part of) a template to be filled with client info
	 * @return A promise that will resolve to the resulting element.
	 * @throws Throwable 
	**/
	Promise<Block> compose(JsonNode blockInfo, Element block, ReportData reportData) throws Throwable;

}

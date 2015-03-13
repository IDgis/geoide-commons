package nl.idgis.geoide.commons.report.blocks;


import nl.idgis.geoide.documentcache.DocumentCache;

import org.jsoup.nodes.Element;

import play.libs.F.Promise;

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

	
	Promise<Block> compose(Element blockElement, BlockInfo info,
			DocumentCache documentCache) throws Throwable;


	
	
}

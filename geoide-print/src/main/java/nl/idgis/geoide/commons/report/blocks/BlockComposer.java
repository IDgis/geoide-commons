package nl.idgis.geoide.commons.report.blocks;


import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.documentcache.DocumentCache;

import org.jsoup.nodes.Element;

/**
 * Interface for a specialized report composer component. 
 * Composes a report block with information from the client and a (part of) a report template
 **/ 

public interface BlockComposer<T extends BlockInfo> {
	
	 

	/**
	 * Composes a report block with information from the client and a (part of) a report template
	 * 
	 * @param blockInfo		a Json node with information from the client.
	 * @param block			(part of) a template to be filled with client info
	 * @return A promise that will resolve to the resulting element.
	 * @throws Throwable 
	**/

	
	CompletableFuture<Block> compose(Element blockElement, T info,
			DocumentCache documentCache) throws Throwable;


	
	
}

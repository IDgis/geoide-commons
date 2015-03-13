package nl.idgis.geoide.commons.report;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.idgis.geoide.commons.report.blocks.Block;
import nl.idgis.geoide.commons.report.blocks.BlockInfo;
import nl.idgis.geoide.commons.report.blocks.DateBlockInfo;
import nl.idgis.geoide.commons.report.blocks.MapBlockComposer;
import nl.idgis.geoide.commons.report.blocks.MapBlockInfo;
import nl.idgis.geoide.commons.report.blocks.ScaleTextBlockInfo;
import nl.idgis.geoide.commons.report.blocks.TextBlockComposer;
import nl.idgis.geoide.commons.report.blocks.TextBlockInfo;
import nl.idgis.geoide.commons.report.template.TemplateDocument;
import nl.idgis.geoide.commons.report.template.TemplateDocumentProvider;
import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.map.MapView;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.F.Tuple;


/**
 * A component that combines information from the client (p.e. viewerstate) with a template to compose 
 * a report for printing.
 * 
 * This component holds several specialized composers that are responsible for their own task (p.e. mapcomposer)
 */

public class ReportComposer {
	private final ReportPostProcessor processor;
	private final TemplateDocumentProvider templateProvider;
	private final TextBlockComposer textBlockComposer;
	private final MapBlockComposer mapBlockComposer;
	private final DocumentCache documentCache;
	
	
	/**
	 * Constructs a report composer with a series of specialized composers
	 * 
	 * @param 	processor 		the postprocessor to send the report to, before printing
	 * @param 	templateprovider 	the template provider to retrieve the report template from. 
	 * @param 	mapView 	
	 * @param	documentCache	a documentCache object to store the report document
	 * 
	 */
	
	public ReportComposer (ReportPostProcessor processor, TemplateDocumentProvider templateProvider, MapView mapView, DocumentCache documentCache) {
		this.processor =  processor;
		this.templateProvider = templateProvider;
		this.textBlockComposer = new TextBlockComposer();
		this.mapBlockComposer = new MapBlockComposer(mapView);
		this.documentCache = documentCache;
	}
	
	/**
	 * Composes a report for printing and sends the composed report to the postprocessor.
	 * 
	 * @param clientInfo 	client information in the form of a Json Node. 
	 * @return a promise of a report document (html)
	 */
	public Promise<Document> compose (JsonNode clientInfo) throws Throwable {
		
		//parse templateInfo
		final JsonNode viewerStates = clientInfo.findPath("viewerstates");
		final JsonNode templateInfo = clientInfo.findPath("template");
		final JsonNode clientInfoBlocks = templateInfo.path("blocks");
		//TODO get template path from configuration 
		final String templatePath = "templates/";
		final String templateUrl = templatePath + templateInfo.path ("id").asText() + ".html";
		
		
		final Map<String, JsonNode> viewerStateNodes = new HashMap<> ();
		for (final JsonNode stateNode: viewerStates) {
			final JsonNode stateId =  stateNode.path ("id");
			viewerStateNodes.put(stateId.textValue(), stateNode);
		}

		
		final TemplateDocument template = templateProvider.getTemplateDocument(templateUrl);
		PaperFormat format = PaperFormat.valueOf(template.getPageFormat() + template.getPageOrientation());
		ReportData reportData = new ReportData(format, template.getLeftMargin() ,template.getRightMargin(), template.getTopMargin(), template.getBottomMargin() );
				
		Elements blockElements = template.body().select(".block");
		
		final List<Promise<Tuple<Element, Block>>> promises = new ArrayList<> (blockElements.size ());
		final List< Tuple<Tuple<Element,Element>, BlockInfo>> preparedBlocks = new ArrayList<> (blockElements.size ());
		final Map <String, MapBlockInfo> mapBlockInfoMap = new HashMap<String, MapBlockInfo>();
		
		ObjectMapper mapper = new ObjectMapper();
		
		
		for (Element templateBlockElement : blockElements) {
			Element blockElement = templateBlockElement.clone();
			BlockInfo blockInfo = null;
			
			//special block date
			if(blockElement.hasClass("date")) {
				blockInfo = new DateBlockInfo (mapper.createObjectNode(), blockElement.attributes(), reportData);
			}
			//specialblock scale
			if(blockElement.hasClass("scale")) {
				blockInfo = new ScaleTextBlockInfo (null, blockElement.attributes(), reportData);
			}
			
			if(blockElement.hasClass("map")) {
				String viewerStateId = blockElement.attributes().get("data-viewerstate-id");
				if(viewerStateId!=null){
					blockInfo = new MapBlockInfo (viewerStateNodes.get(viewerStateId),blockElement.attributes(), reportData);
					mapBlockInfoMap.put(viewerStateId, (MapBlockInfo) blockInfo);
				} else {
					//foutmelding 
				}
			}
			//TextElements
			for (final JsonNode clientInfoBlock: clientInfoBlocks) {
				
				if(blockElement.id().equals(clientInfoBlock.path("id").asText().toLowerCase())){
					blockInfo = new TextBlockInfo(clientInfoBlock, blockElement.attributes(), reportData);
				}
			}	
			
			Tuple <Element,Element> elements = new Tuple<Element, Element>(templateBlockElement, blockElement);
			preparedBlocks.add(new Tuple<Tuple<Element,Element>, BlockInfo> (elements,blockInfo));
		}
		
		for (Tuple <Tuple <Element, Element>, BlockInfo> preparedBlock : preparedBlocks) {
			Element sourceElement = preparedBlock._1._1;
			Element blockElement = preparedBlock._1._2;
			BlockInfo blockInfo = preparedBlock._2;
			
			if (blockElement.hasClass("text") && !blockElement.hasClass("scale")) {
				promises.add (processBlock (sourceElement, textBlockComposer.compose (blockElement, blockInfo, documentCache)));
			}
			if (blockElement.hasClass("scale")){
				int scale = (int) mapBlockInfoMap.get(blockInfo.getBlockAttribute("viewerstate-id")).getScale();
				((ScaleTextBlockInfo) blockInfo).setScale(scale);
				promises.add (processBlock (sourceElement, textBlockComposer.compose (blockElement, blockInfo, documentCache)));
			}
			if (blockElement.hasClass("map")) {
				promises.add (processBlock (sourceElement, mapBlockComposer.compose (blockElement, blockInfo, documentCache)));
			}
			
		}
		
		return Promise
			.sequence(promises)
			.flatMap (new Function<List<Tuple<Element, Block>>, Promise<Document>> () {
				@Override
				public Promise<Document> apply (final List<Tuple<Element, Block>> blocks) throws Throwable {
					
					for (final Tuple<Element, Block> tuple: blocks) {
						final Element sourceElement = tuple._1;
						final Block block = tuple._2;
						final URI cssUri = block.getCss ();
						
						sourceElement.replaceWith (block.getBlock ());
						
						if (cssUri == null) {
							continue;
						}
						
						template
							.head ()
							.appendElement ("link")
							.attr ("rel", "stylesheet")
							.attr ("href", cssUri.toString ())
							.attr ("type", "text/css");
					}
					
					return processor.process (template);
				}
			});
	}
	
	private Promise<Tuple<Element, Block>> processBlock (final Element sourceElement, final Promise<Block> promise) {
		return promise
			.map (new Function<Block, Tuple<Element, Block>> () {
				@Override
				public Tuple<Element, Block> apply (final Block block)
						throws Throwable {
					return new Tuple<> (sourceElement, block);
				}
			});
	}
}

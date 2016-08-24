package nl.idgis.geoide.commons.report;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.api.DocumentCache;
import nl.idgis.geoide.commons.domain.api.ReportComposer;
import nl.idgis.geoide.commons.domain.print.PrintEvent;
import nl.idgis.geoide.commons.domain.report.TemplateDocument;
import nl.idgis.geoide.commons.print.service.HtmlPrintService;
import nl.idgis.geoide.commons.report.blocks.Block;
import nl.idgis.geoide.commons.report.blocks.BlockInfo;
import nl.idgis.geoide.commons.report.blocks.DateBlockInfo;
import nl.idgis.geoide.commons.report.blocks.MapBlockComposer;
import nl.idgis.geoide.commons.report.blocks.MapBlockInfo;
import nl.idgis.geoide.commons.report.blocks.ScaleBarBlockComposer;
import nl.idgis.geoide.commons.report.blocks.ScaleBarBlockInfo;
import nl.idgis.geoide.commons.report.blocks.ScaleTextBlockInfo;
import nl.idgis.geoide.commons.report.blocks.TextBlockComposer;
import nl.idgis.geoide.commons.report.blocks.TextBlockInfo;
import nl.idgis.geoide.commons.report.template.HtmlTemplateDocumentProvider;
import nl.idgis.geoide.map.DefaultMapView;
import nl.idgis.geoide.util.Futures;
import play.libs.F.Tuple;


/**
 * A component that combines information from the client (p.e. viewerstate) with a template to compose 
 * a report for printing.
 * 
 * This component holds several specialized composers that are responsible for their own task (p.e. mapcomposer)
 */

public class DefaultReportComposer implements ReportComposer {
	private final ReportPostProcessor processor;
	private final HtmlTemplateDocumentProvider templateProvider;
	private final TextBlockComposer textBlockComposer;
	private final MapBlockComposer mapBlockComposer;
	private final ScaleBarBlockComposer scaleBarBlockComposer;
	private final DocumentCache documentCache;
	private static Logger log = LoggerFactory.getLogger (HtmlPrintService.class);
	
	/**
	 * Constructs a report composer with a series of specialized composers
	 * 
	 * @param 	processor 		the postprocessor to send the report to, before printing
	 * @param 	templateprovider 	the template provider to retrieve the report template from. 
	 * @param 	mapView 	
	 * @param	documentCache	a documentCache object to store the report document
	 * 
	 */
	
	public DefaultReportComposer (ReportPostProcessor processor, HtmlTemplateDocumentProvider templateProvider, DefaultMapView mapView, DocumentCache documentCache) {
		this.processor =  processor;
		this.templateProvider = templateProvider;
		this.textBlockComposer = new TextBlockComposer();
		this.mapBlockComposer = new MapBlockComposer(mapView);
		this.scaleBarBlockComposer = new ScaleBarBlockComposer();
		this.documentCache = documentCache;
	}
	
	/* (non-Javadoc)
	 * @see nl.idgis.geoide.commons.report.ReportComposer#compose(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public CompletableFuture<Publisher<PrintEvent>> compose (final ExternalizableJsonNode clientInfo, final String token) throws Throwable {
		Objects.requireNonNull (clientInfo, "clientInfo cannot be null");
		
		final JsonNode templateInfo = clientInfo.getJsonNode ().findPath("template");
	
		final String templateUrl = templateInfo.path ("id").asText();
		
		CompletableFuture<TemplateDocument> doc = templateProvider.getTemplateDocument(templateUrl);
		
		return doc.thenCompose((d) -> {
			try { 
				return composeTemplate (d, clientInfo.getJsonNode (), token);
			} catch (Throwable e) { 
				throw new RuntimeException (e);
			}
		});
	}
		
		
	private CompletableFuture<Publisher<PrintEvent>> composeTemplate (TemplateDocument template, JsonNode clientInfo, String token) throws Throwable {	
		//parse templateInfo
		final JsonNode templateInfo = clientInfo.findPath("template");
		final JsonNode viewerStates = clientInfo.findPath("viewerstates");	
		final JsonNode clientInfoBlocks = templateInfo.path("blocks");
		final Map<String, JsonNode> viewerStateNodes = new HashMap<> ();
		for (final JsonNode stateNode: viewerStates) {
			final JsonNode stateId =  stateNode.path ("id");
			viewerStateNodes.put(stateId.textValue(), stateNode);
		}
		
		PaperFormat format = PaperFormat.valueOf(template.getPageFormat() + template.getPageOrientation());
		ReportData reportData = new ReportData(format, template.getLeftMargin() ,template.getRightMargin(), template.getTopMargin(), template.getBottomMargin(), template.getGutterH(), template.getGutterV(), template.getRowCount(), template.getColCount());
				
		final org.jsoup.nodes.Document html = Jsoup.parse (template.getContent (), template.getDocumentUri ().toString ());
		
		Elements blockElements = html.getElementsByClass ("block");
		
		final List<CompletableFuture<Tuple<Element, Block>>> promises = new ArrayList<> (blockElements.size ());
		final List<Tuple<Tuple<Element,Element>, BlockInfo>> preparedBlocks = new ArrayList<> (blockElements.size ());
		final Map <String, MapBlockInfo> mapBlockInfoMap = new HashMap<String, MapBlockInfo>();
		
		ObjectMapper mapper = new ObjectMapper();
		
		
		for (Element templateBlockElement : blockElements) {

			BlockInfo blockInfo = null;
			
			//special block date
			if(templateBlockElement.hasClass("date")) {
				blockInfo = new DateBlockInfo (mapper.createObjectNode(), templateBlockElement, reportData);
			}
			//specialblock scale
			if(templateBlockElement.hasClass("scale")) {
				blockInfo = new ScaleTextBlockInfo (null, templateBlockElement, reportData);
			}
			
			if(templateBlockElement.hasClass("scalebar")) {
				String viewerStateId = templateBlockElement.attributes().get("data-viewerstate-id");
				if(viewerStateId!=null){
					blockInfo = new ScaleBarBlockInfo (viewerStateNodes.get(viewerStateId), templateBlockElement, reportData);
				} else {
					//foutmelding
				}
			}
			if(templateBlockElement.hasClass("map")) {
				String viewerStateId = templateBlockElement.attributes().get("data-viewerstate-id");
				if(viewerStateId!=null){
					
					blockInfo = new MapBlockInfo (viewerStateNodes.get(viewerStateId),templateBlockElement, reportData);
					mapBlockInfoMap.put(viewerStateId, (MapBlockInfo) blockInfo);
				} else {
					//foutmelding 
				}
			}
			//TextElements
			for (final JsonNode clientInfoBlock: clientInfoBlocks) {
				
				if(templateBlockElement.id().equals(clientInfoBlock.path("id").asText().toLowerCase())){
					blockInfo = new TextBlockInfo(clientInfoBlock, templateBlockElement, reportData);
				}
			}		
			Element blockElement = templateBlockElement.clone();
			Tuple <Element,Element> elements = new Tuple<Element, Element>(templateBlockElement, blockElement);
			preparedBlocks.add(new Tuple<Tuple<Element,Element>, BlockInfo> (elements,blockInfo));
		}
		
		for (Tuple <Tuple <Element, Element>, BlockInfo> preparedBlock : preparedBlocks) {
			Element sourceElement = preparedBlock._1._1;
			Element blockElement = preparedBlock._1._2;
			BlockInfo blockInfo = preparedBlock._2;
			if (blockInfo != null) {
				if (blockElement.hasClass("text") && !blockElement.hasClass("scale")) {
					promises.add (processBlock (sourceElement, textBlockComposer.compose (blockElement, blockInfo, documentCache, token)));
				}
				if (blockElement.hasClass("scale")){
					int scale = (int) mapBlockInfoMap.get(blockInfo.getBlockAttribute("viewerstate-id")).getScale();
					((ScaleTextBlockInfo) blockInfo).setScale(scale);
					promises.add (processBlock (sourceElement, textBlockComposer.compose (blockElement, blockInfo, documentCache, token)));
				}
				if (blockElement.hasClass("map")) {
					promises.add (processBlock (sourceElement, mapBlockComposer.compose (blockElement, (MapBlockInfo) blockInfo, documentCache, token)));
				}
				if (blockElement.hasClass("scalebar")) {
					promises.add (processBlock (sourceElement, scaleBarBlockComposer.compose (blockElement, (ScaleBarBlockInfo) blockInfo, documentCache, token)));
				}
			}	
			
		}
		
		return Futures
			.all (promises)
			.thenCompose ((final List<Tuple<Element, Block>> blocks) -> {
				for (final Tuple<Element, Block> tuple: blocks) {
					final Element sourceElement = tuple._1;
					final Block block = tuple._2;
					final URI cssUri = block.getCssUri ();
					
					sourceElement.replaceWith (block.getBlock ());
					
					if (cssUri == null) {
						continue;
					}
					
					html
						.head ()
						.appendElement ("link")
						.attr ("rel", "stylesheet")
						.attr ("href", cssUri.toString ())
						.attr ("type", "text/css");
				}
				
				log.debug("html template :"  + template);
				
				try {
					return processor.process (template, html, reportData);
				} catch (Throwable t) {
					throw new RuntimeException (t);
				}
			});
	}
	
	private CompletableFuture<Tuple<Element, Block>> processBlock (final Element sourceElement, final CompletableFuture<Block> promise) {
		return promise
			.thenApply ((final Block block) -> new Tuple<> (sourceElement, block));
	}
}

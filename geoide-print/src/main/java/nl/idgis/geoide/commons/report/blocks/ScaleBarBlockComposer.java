package nl.idgis.geoide.commons.report.blocks;

import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentCache;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ScaleBarBlockComposer implements BlockComposer<ScaleBarBlockInfo> {

	@Override
	public CompletableFuture<Block> compose(Element blockElement, ScaleBarBlockInfo barinfo,	DocumentCache documentCache, String token) throws Throwable {
		//create svg file
		URI scaleBarUri = new URI ("stored://" + UUID.randomUUID ().toString ());
		Document scaleBarDoc = new Document(scaleBarUri.toString());
		Element svgNode = scaleBarDoc.appendElement("svg"); 
		double f = 1/0.028;
		svgNode	.attr ("width", String.format (Locale.US, "%f", barinfo.getTotalWidthmm() * f))
				.attr ("height", String.format (Locale.US, "%f", 10 * f))
				.attr ("viewBox", String.format (Locale.US, "%f %f %f %f", 0.0, 0.0, barinfo.getTotalWidthmm() * f,  10 * f))
				.attr ("xmlns","http://www.w3.org/2000/svg");
			
		double x = 3 * f;
		double y = 4 * f;
		String style = "stroke:#000000;stroke-width:" + 0.3 * f + ";stroke-linecap:butt; fill:" ;
		Element svgText;
		int n = 0;
		for(n = 0; n < barinfo.getNumberOfRects(); n++ ){
			Element svgRect = svgNode.appendElement("rect");
			svgRect.attr("x", "" + x)
					.attr("y","" + y)
					.attr("width",  "" + barinfo.getRectWidthmm() * f)
					.attr("height","" +  2 * f)
					.attr("style", style + ((n & 1) == 0 ? "#000000" : "#ffffff"));
			
			svgText = svgNode.appendElement("text");
			svgText.attr("x", "" + x)
					.attr("y","" + (y - 1 * f))
					.attr("style","text-anchor: middle;font-size:" + 3 * f + ";font-family:Arial");
			
			svgText.appendText(barinfo.getScaleBarText(n));
			x += barinfo.getRectWidthmm() * f;
					
		}	
		svgText = svgNode.appendElement("text");
		svgText.attr("x", "" + x)
				.attr("y","" + (y - 1 * f))
				.attr("style","text-anchor: middle;font-size:" + 3 * f + ";font-family:Arial");
		svgText.appendText(barinfo.getScaleBarText(n));	
		
		//create html image object
		Element scaleBar = blockElement.appendElement("div");
		scaleBar.attr("id", "scaleBar_" + barinfo.getBlockAttribute("viewerstate-id"));		
		Element scaleBarObject = scaleBar.appendElement("object");
		scaleBarObject	.attr("type", "image/svg+xml")
						.attr("style", "left:0;top:0;width:" + barinfo.getTotalWidthmm()  + "mm;height:10mm;")
						.attr("data", scaleBarUri.toString());
		
		CompletableFuture<nl.idgis.geoide.commons.domain.document.Document> scaleBarPromise = documentCache.store(scaleBarUri, new MimeContentType ("image/svg+xml"), scaleBarDoc.toString().getBytes());
	
		final Block scaleBarBlock = new Block(blockElement, null);
		
		return scaleBarPromise 
				.thenApply ((d) -> scaleBarBlock);
					
		
	}

}

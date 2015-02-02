package controllers.viewer;

import it.innove.play.pdf.PdfGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.commons.domain.toc.TOCItem;
import nl.idgis.geoide.commons.domain.traits.Traits;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.viewer.viewer;
import views.html.viewer.pdf;
import controllers.toc.TOC;

public class Viewer extends Controller {
	private final TOC toc;
	private final MapProvider mapProvider;
	
	
	public Viewer(MapProvider mapProvider, TOC toc){
		this.mapProvider = mapProvider;
		this.toc = toc;
	}
	
	public Result viewerForMap (final String mapId) {
		return viewerForMapOpenLayers2 (mapId);
	}
	
	public Result viewerForMapOpenLayers2 (final String mapId) {
		final MapDefinition mapDef = mapProvider.getMapDefinition(mapId);
		List<Traits<TOCItem>> tocItems = toc.getItems (mapDef);
		
		Logger.debug ("TOC items: " + tocItems.size ());
		/*for(Traits<TOCItem> tocItem : tocItems){
			if(tocItem.has(TOCItemLayerTrait.class)) {
				Logger.debug("TOC items tocItems " + tocItem.trait(TOCItemLayerTrait.class).getLayer().getId());
			}
		}*/
		return ok (viewer.render (mapId, "2", tocItems));
	}
	
	
	
	public Result viewerForMapOpenLayers3 (final String mapId) {
		final MapDefinition mapDef = mapProvider.getMapDefinition(mapId);
		List<Traits<TOCItem>> tocItems = toc.getItems (mapDef);
		return ok (viewer.render (mapId, "3", tocItems));
	}
	
	public Result testPDF () {
		return PdfGenerator.ok (pdf.render (), "http://localhost", Collections.<String>emptyList ());
	}
}

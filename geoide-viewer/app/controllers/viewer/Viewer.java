package controllers.viewer;

import java.util.List;

import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.provider.MapProvider;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.viewer.viewer;
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
		List<Object> tocItems = toc.getItems (mapDef);
		
		return ok (viewer.render (mapId, "2"));//, tocItems));
	}
	
	public Result viewerForMapOpenLayers3 (final String mapId) {
		final MapDefinition mapDef = mapProvider.getMapDefinition(mapId);
		return ok (viewer.render (mapId, "3"));
	}
}

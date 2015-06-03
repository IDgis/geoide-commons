package controllers.viewer;

import it.innove.play.pdf.PdfGenerator;

import java.util.Collections;

import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.api.TableOfContents;
import nl.idgis.geoide.commons.domain.provider.MapProvider;
import nl.idgis.geoide.util.Promises;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.viewer.pdf;
import views.html.viewer.viewer;

public class Viewer extends Controller {
	private final TableOfContents toc;
	private final MapProvider mapProvider;
	
	
	public Viewer(MapProvider mapProvider, final TableOfContents toc){
		this.mapProvider = mapProvider;
		this.toc = toc;
	}
	
	public Promise<Result> viewerForMap (final String mapId) {
		final MapDefinition mapDef = mapProvider.getMapDefinition(mapId);
		
		return Promises.asPromise (toc.getItems (mapDef)).map ((tocItems) -> {
			return ok (viewer.render (mapId, tocItems));
		});
	}
	
	public Result testPDF () {
		return PdfGenerator.ok (pdf.render (), "http://localhost", Collections.<String>emptyList ());
	}
	
	
	
}

package controllers.viewer;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.viewer.viewer;

public class Viewer extends Controller {

	public Result viewerForMap (final String mapId) {
		return viewerForMapOpenLayers2 (mapId);
	}
	
	public Result viewerForMapOpenLayers2 (final String mapId) {
		return ok (viewer.render (mapId, "2"));
	}
	
	public Result viewerForMapOpenLayers3 (final String mapId) {
		return ok (viewer.render (mapId, "3"));
	}
}

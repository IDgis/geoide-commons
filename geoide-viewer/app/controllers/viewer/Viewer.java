package controllers.viewer;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import nl.idgis.geoide.commons.domain.api.MapProviderApi;
import nl.idgis.geoide.commons.domain.api.TableOfContents;
import nl.idgis.geoide.util.Promises;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.viewer.viewer;


public class Viewer extends Controller {
	private final TableOfContents toc;
	private final MapProviderApi mapProvider;

	
	
	@Inject
	public Viewer(MapProviderApi mapProvider, final TableOfContents toc){
		this.mapProvider = mapProvider;
		this.toc = toc;
	}
	
	public Promise<Result>  startup (final String mapId) {
		return Promises.asPromise (mapProvider.getToken()).flatMap ((token) -> {
			
			response().setCookie("configToken", token);
			String tokenstr = request().cookies().get("configToken").value();
			
			System.out.println("heb een token " + tokenstr);
			
			return Promise.pure(redirect("/map/viewer/" + mapId)); 
		});
		
	}
	
	public Promise<Result> viewerForMap (final String mapId) {

		String token = request().cookies().get("configToken").value();
		
		System.out.println("heb echt een token " + token);
		//return ok();
		return Promises.asPromise (mapProvider.getMapDefinition(mapId, token)).flatMap ((mapDef) -> {
			return Promises.asPromise (toc.getItems (mapDef)).map ((tocItems) -> {
				return ok (viewer.render (mapId, tocItems));
			});
		});
	}
	
	public Promise<Result> viewerReload () {
		return Promises.asPromise (mapProvider.refresh())
			.map((b) -> {
				System.out.println("result back " + b);
				return ok();
		});
	}
	
	
}

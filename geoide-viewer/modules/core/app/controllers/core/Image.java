package controllers.core;

import javax.inject.Inject;
import nl.idgis.geoide.commons.domain.api.ImageProvider;
import nl.idgis.geoide.commons.domain.document.StoredImage;
import nl.idgis.geoide.util.Promises;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;


public class Image extends Controller {
	private final ImageProvider imageProvider;
	
	@Inject
	public Image(ImageProvider imageProvider) {
		this.imageProvider = imageProvider;
		
		if (imageProvider  == null) {
			throw new NullPointerException ("imageProvider cannot be null");
		}
		
	}
		
	public	 Promise<Result> getImage (String imageUrl) throws Throwable {

		final Promise<StoredImage> imagePromise = Promises.asPromise (this.imageProvider.getImage(imageUrl));
		
		return imagePromise.map((image) -> {		
			return ok(image.getImage());
		});
			
	}
	
}

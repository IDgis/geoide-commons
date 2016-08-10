package nl.idgis.geoide.commons.domain.api;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


import nl.idgis.geoide.commons.domain.document.StoredImage;


public interface ImageProvider {
	CompletableFuture<StoredImage> getImage(String imageUrl) throws IOException;
}


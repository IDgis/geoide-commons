package nl.idgis.geoide.toc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.swing.ImageIcon;

import org.reactivestreams.Publisher;

import akka.util.ByteString;
import akka.util.ByteString.ByteStrings;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.document.StoredImage;
import nl.idgis.geoide.documentcache.service.DelegatingStore;
import nl.idgis.geoide.documentcache.service.FileStore;
import nl.idgis.geoide.util.Futures;
import nl.idgis.geoide.util.streams.StreamProcessor;
import nl.idgis.geoide.commons.domain.api.ImageProvider;


public class StoredImageProvider implements ImageProvider {
	
	private final FileStore fileStore;
	private final StreamProcessor streamProcessor;
	
	
	public StoredImageProvider(FileStore fileStore, StreamProcessor streamProcessor) {
		this.streamProcessor = streamProcessor;
		this.fileStore = fileStore;
	}
	

	public CompletableFuture<StoredImage> getImage(String imageName) {
		
		URI uri;
		
		try {
	    	uri = new URI("image:///" + imageName );
		 }  catch(URISyntaxException e) {	
		    return Futures.throwing(e);
		}
		
    	CompletableFuture<Document> doc = fileStore.fetch(uri);
		return doc.thenApply ((d) -> {
			try {
				URI imageURI = new URI("stored://" + UUID.randomUUID ().toString ());
				
				final Publisher<ByteString> body = streamProcessor.resolvePublisherReference (d.getBody (), 5000);
				final InputStream inputStream = streamProcessor.asInputStream (body, 5000);
				
				final byte[] buffer = new byte[4096];
				ByteString data = ByteStrings.empty ();
				int nRead;
				while ((nRead = inputStream.read (buffer, 0, buffer.length)) >= 0) {
					data = data.concat (ByteStrings.fromArray (buffer, 0, nRead));
				}
				inputStream.close ();
						
				final StoredImage image = new StoredImage (new ImageIcon(data.toArray ()), uri);
				
				return image;
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException (e);
			}
		});
	}
	
}

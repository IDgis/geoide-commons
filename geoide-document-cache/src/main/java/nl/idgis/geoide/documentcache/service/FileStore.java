package nl.idgis.geoide.documentcache.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.documentcache.DocumentStore;
import nl.idgis.geoide.util.Futures;
import nl.idgis.geoide.util.streams.StreamProcessor;
import nl.idgis.ogc.util.MimeContentType;

import org.apache.commons.io.FilenameUtils;
import org.reactivestreams.Publisher;

import play.Logger;
import akka.util.ByteString;


/**
 * An implementation of {@link DocumentStore} that uses a {@link BaseUrl} to retrieve documents
 * by composing a file url.
 * 
 */


public class FileStore implements DocumentStore { 
	
	private final File basePath;
	private final String protocol;
	private final StreamProcessor streamProcessor;
	
	/**
	 * Creates a new (file) document store for the given basePath and protocol
	 * 
	 * @param basePath The basePath on the filesystem for this store
	 * @param protocol The protocol for this FileStore (f.i. "template"
	 * @streamProcessor A Utility class  {@link StreamProcessor} to read and write files
	 */
	public FileStore(File basePath, String protocol, StreamProcessor streamProcessor) {
		this.basePath = basePath;
		this.protocol = protocol;
		this.streamProcessor = streamProcessor;
	}
	
	/**
	 * Returns the protocol of the filestore.
	 *  
	 * @return The protocol of the filestore.
	 */
	public String getProtocol() {
		return protocol;
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public CompletableFuture<Document> fetch (URI fileUri) {
				
		if (!protocol.equals (fileUri.getScheme ())) {
			Logger.debug ("Bad scheme: " + fileUri.toString ());
			return Futures.throwing (new DocumentCacheException.DocumentNotFoundException (fileUri)); 
		}
		
		final File file = new File (basePath, fileUri.getPath ());
		
		if (!file.getAbsolutePath().startsWith(basePath.getAbsolutePath())) {
			Logger.debug ("file: " + fileUri.getPath() + " is not on the basepath: " + basePath);
			return Futures.throwing (new DocumentCacheException.IOError(null));
		}
		
		String contentType;
		try {
			contentType = Files.probeContentType(file.toPath());
			if(contentType == null) {
				String ext = FilenameUtils.getExtension(file.getAbsolutePath());
				if (ext.equals("less")){
					contentType = "text/less";
				} else {
					contentType = "application/octet-stream";
				}
				
			}
			
		} catch (IOException e) {
			Logger.debug ("Cannot determine contentType from file: " + file.getAbsolutePath());
			return Futures.throwing (e); 
		}
		
		String contType = contentType;
		
		Publisher<ByteString> body; 

		try {
			body = streamProcessor.publishInputStream(new FileInputStream(file), 1024, 30000);
		} catch (FileNotFoundException e) {
			Logger.debug ("Cannot find file on filePath: " + file.getAbsolutePath());
	    	return Futures.throwing (e);
		}
		
		
		Document document = new Document () {
				@Override
				public URI getUri () throws URISyntaxException  {
					return fileUri;
				}
				
				@Override
				public MimeContentType getContentType () {
					return new MimeContentType(contType);
				}
				
				@Override
				public Publisher<ByteString> getBody () {
					return body;
				}
			};
		
		return CompletableFuture.completedFuture (document);	

	}
	
	/**
	 * Returns an Array of files located on the basePath of this filestore.
	 *  
	 * @return Array of files
	 */
	public File[] getFiles() {
		return basePath.listFiles();
	}
	/**
	 * Returns an Array of directories located on the basePath of this filestore.
	 *  
	 * @return Array of directories
	 */
	public File[] getDirectories() {
		File[] directories = basePath.listFiles(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		return directories;
	}
}

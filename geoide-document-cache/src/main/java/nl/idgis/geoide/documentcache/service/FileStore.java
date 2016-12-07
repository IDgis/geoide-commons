package nl.idgis.geoide.documentcache.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FilenameUtils;

import akka.util.ByteString;
import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.api.DocumentStore;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.commons.domain.document.DocumentCacheException;
import nl.idgis.geoide.util.Futures;
import nl.idgis.geoide.util.streams.PublisherReference;
import nl.idgis.geoide.util.streams.StreamProcessor;
import play.Logger;


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

	private File getFile (URI fileUri) throws DocumentCacheException {
		if (!protocol.equals (fileUri.getScheme ())) {
			Logger.debug ("Bad scheme: " + fileUri.toString ());
			throw new DocumentCacheException.DocumentNotFoundException (fileUri); 
		}
		
		final File file = new File (basePath, fileUri.getPath ());
		
		if (!file.getAbsolutePath().startsWith(basePath.getAbsolutePath())) {
			Logger.debug ("file: " + fileUri.getPath() + " is not on the basepath: " + basePath);
			throw new DocumentCacheException.IOError (null);
		}
		
		if (!file.exists () || !file.isFile ()) {
			throw new DocumentCacheException.DocumentNotFoundException (fileUri);
		}

		return file;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletableFuture<Document> fetch (URI fileUri) {
		final File file;
		Logger.debug("try fetching file from FileStore " + fileUri.getPath());
		try {
			file = getFile (fileUri);
		} catch (DocumentCacheException e) {
			return Futures.throwing (e);
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
		
		final String contType = contentType;
		final PublisherReference<ByteString> bodyReference;
		
		try {
			 bodyReference = streamProcessor.createPublisherReference (
					streamProcessor.publishInputStream(new FileInputStream(file), 1024, 30000),
					5000
				);			
		} catch (FileNotFoundException e) {
			Logger.debug ("Cannot find file on filePath: " + file.getAbsolutePath());
	    	return Futures.throwing (new DocumentCacheException.DocumentNotFoundException (fileUri, e));
		}
		
		
		Document document = new Document (fileUri, new MimeContentType (contType), bodyReference);
		
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
		if (!basePath.exists () || !basePath.isDirectory ()) {
			return new File[0];
		}
		
		File[] directories = basePath.listFiles(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		return directories;
	}
}

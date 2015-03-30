package nl.idgis.geoide.documentcache.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

import nl.idgis.geoide.documentcache.Document;
import nl.idgis.geoide.documentcache.DocumentCache;
import nl.idgis.geoide.documentcache.DocumentCacheException;
import nl.idgis.geoide.documentcache.DocumentStore;
import nl.idgis.geoide.util.streams.StreamProcessor;
import nl.idgis.ogc.util.MimeContentType;

import org.reactivestreams.Publisher;

import play.Logger;
import play.libs.F.Promise;
import akka.util.ByteString;


/**
 * An implementation of {@link DocumentStore} that uses a {@link BaseUrl} to retrieve documents
 * by composing a file url.
 * 
 * The store doesn't perform caching, but it can be used as a readthrough
 * store for a {@link DocumentCache}.
 */


public class FileStore implements DocumentStore { 
	
	private final File basePath;
	private final String protocol;
	private final StreamProcessor streamProcessor;
	

	public FileStore(File basePath, String protocol, StreamProcessor streamProcessor) {
		this.basePath = basePath;
		this.protocol = protocol;
		this.streamProcessor = streamProcessor;
	}
	
	@Override
	public Promise<Document> fetch (URI localUri) {
		File file = null;
		String filePath = basePath + localUri.getPath();
		final URI uri;
		try {
			uri = new URI(filePath).normalize();
		} catch (URISyntaxException e) {
	    	Logger.debug ("Not a valid URI: " + filePath);
	    	return Promise.throwing (e); 
		}	
			
		try {
			file = new File (uri);
		} catch (Exception e) {
			Logger.debug ("Cannot create file from filePath: " + filePath);
			return Promise.throwing (e); 
		}
	
		
		if (file.getAbsolutePath().indexOf(basePath.getAbsolutePath()) == -1) {
			Logger.debug ("file: " + filePath + " is not on the basepath: " + basePath);
			return Promise.throwing (new DocumentCacheException.DocumentNotFoundException (uri));
		}

		//combine url, normalize path
		//check if file within basepath
		//check if exists
		//make FileInputStream from File
		//publishInputStream
		//Publisher 
		// bepaal contenttype op basis file extensie mimemagic oid
		// anders standaard mimetype binary content
		// return met promise.pure
		
		String contentType;
		try {
			contentType = Files.probeContentType(file.toPath());
		} catch (IOException e) {
			Logger.debug ("Cannot determine contentType from file: " + file.getAbsolutePath());
			return Promise.throwing (e); 
		}
		
		
		
		Publisher<ByteString> body; 

		try {
			body = streamProcessor.publishInputStream(new FileInputStream(file), 512, 1000);
		} catch (FileNotFoundException e) {
			Logger.debug ("Cannot find file on filePath: " + file.getAbsolutePath());
	    	return Promise.throwing (e);
		}
		
		 Document document = new Document () {
				@Override
				public URI getUri () {
					
					return uri;
				}
				
				@Override
				public MimeContentType getContentType () {
					return new MimeContentType(contentType);
				}
				
				@Override
				public Publisher<ByteString> getBody () {
					return body;
				}
			};
		
		return Promise.pure(document);	

	}
	
	
	public String[] getFileList() {
		return basePath.list();
	}

}

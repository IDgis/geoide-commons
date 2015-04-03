package nl.idgis.geoide.documentcache.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
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
	
	
	public String getProtocol() {
		return protocol;
	}
	
	@Override
	public Promise<Document> fetch (URI fileUri) {
		final File file = new File (basePath, fileUri.getPath ());
	
		
		if (!file.getAbsolutePath().startsWith(basePath.getAbsolutePath())) {
			Logger.debug ("file: " + fileUri.getPath() + " is not on the basepath: " + basePath);
			return Promise.throwing (new DocumentCacheException.IOError(null));
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
			body = streamProcessor.publishInputStream(new FileInputStream(file), 1024, 30000);
		} catch (FileNotFoundException e) {
			Logger.debug ("Cannot find file on filePath: " + file.getAbsolutePath());
	    	return Promise.throwing (e);
		}
		
		
		Document document = new Document () {
				@Override
				public URI getUri () throws URISyntaxException  {
					return new URI(file.getPath());
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
	
	
	public File[] getFiles() {
		return basePath.listFiles();
	}
	
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

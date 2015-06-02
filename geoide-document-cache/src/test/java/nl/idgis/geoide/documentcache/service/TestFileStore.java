package nl.idgis.geoide.documentcache.service;


import static org.junit.Assert.assertEquals;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.idgis.geoide.commons.domain.MimeContentType;
import nl.idgis.geoide.commons.domain.document.Document;
import nl.idgis.geoide.util.streams.AkkaStreamProcessor;
import nl.idgis.geoide.util.streams.StreamProcessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Logger;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;

public class TestFileStore {
	private ActorSystem actorSystem;
	private StreamProcessor streamProcessor;
	private File basePath;
	private FileStore fileStore;
	
	/**
	 * Creates a FileStore and streamProcessor.
	 * @throws IOException 
	 */
	@Before
	public void createFileStore ()   {
		actorSystem = ActorSystem.create ();
		streamProcessor = new AkkaStreamProcessor (actorSystem);	
		try {
			basePath = createTempDirectory();
			fillBasePath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileStore= new FileStore(basePath, "template", streamProcessor);
		
	}
	
	

	/**
	 * Destroys the FileStore and stream processor and remove the temp files from the filesystem.
	 */
	@After
	public void destroyFileStore () {
		streamProcessor = null;
		JavaTestKit.shutdownActorSystem (actorSystem);
		fileStore = null;
		deleteDirectory(basePath);
	}
	
	
	/**
	 * Performs a fetch operation on the document store and validates the result.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testFetch () throws Throwable {
		running (fakeApplication (), new Runnable () {
			@Override
			public void run () {
			
				final Document document;
				try {
					document = fileStore.fetch (new URI ("template:///test.txt")).get (1000, TimeUnit.MILLISECONDS);
				} catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
					throw new RuntimeException (e);
				}
				assertEquals (new MimeContentType ("text/plain"), document.getContentType ());
				try {
					TestDefaultDocumentCache.assertContent ("Hello, World!", document, streamProcessor);
				} catch (IOException e) {
					throw new RuntimeException (e);
				} 
			}
		});
	}
	
	
	/**
	 * Peforms a fetch operation with an URI that contains a relative path
	 * 
	 * @throws Throwable
	 */
	
	@Test
	public void testFetch2 () throws Throwable {
  
		running (fakeApplication (), new Runnable () {
			@Override
			public void run () {
			
				final Document document;
				try {
					document = fileStore.fetch (new URI ("template:///test/../test.txt")).get (1000, TimeUnit.MILLISECONDS);
				} catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
					throw new RuntimeException (e);
				}
				assertEquals (new MimeContentType ("text/plain"), document.getContentType ());
				try {
					TestDefaultDocumentCache.assertContent ("Hello, World!", document, streamProcessor);
				} catch (IOException e) {
					throw new RuntimeException (e);
				}
				
			}
		});
	}
	
	
	/**
	 * Peforms a getFiles operation 
	 * 
	 * @throws Throwable
	 */
	
	@Test
	public void testGetFiles () throws Throwable {
		
		running (fakeApplication (), new Runnable () {
			@Override
			public void run () {
				final File[] files;
				files = fileStore.getFiles();
				assertEquals (3, files.length);	
			}
		});
		
	
	}
	
	@Test
	public void testGetDirectories () throws Throwable {
		running (fakeApplication (), new Runnable () {
			@Override
			public void run () {
				final File[] dirs;
				dirs = fileStore.getDirectories();
				assertEquals (1, dirs.length);	
				
			}
		});
		
		
		
	
	}
	
	
	
	private static File createTempDirectory() throws IOException {
		    final File temp;
		    temp = File.createTempFile("temp","");
		    Logger.debug("temp directory aangemaakt " + temp.getAbsolutePath());
		    if(!(temp.delete()))
		    {
		        throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		    }

		    if(!(temp.mkdir()))
		    {
		        throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		    }

		    return (temp);
	}
	
	private void fillBasePath() throws IOException {
		File testFile = new File(basePath, "test.txt");
		Boolean bool = testFile.createNewFile();
        BufferedWriter output = new BufferedWriter(new FileWriter(testFile));
        output.write("Hello, World!");
        output.close();
		File testFile2 = new File(basePath, "test2.txt");
		bool = testFile2.createNewFile();
		File testDir = new File(basePath, "test");
		bool = testDir.mkdirs();
		File testFile3 = new File(testDir, "test3.txt");
		bool = testFile3.createNewFile();
		
	}
	
	
	public static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
	
}

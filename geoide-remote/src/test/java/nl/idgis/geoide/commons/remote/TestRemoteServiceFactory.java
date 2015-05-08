package nl.idgis.geoide.commons.remote;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;

public class TestRemoteServiceFactory {

	private RemoteServiceFactory factory;
	
	@Before
	public void createFactory () {
		factory = new RemoteServiceFactory ();
	}
	
	@Test
	public void testCreateProxy () {
		final TestInterface proxy = factory.createServiceReference (TestInterface.class);
	}

	public static interface TestInterface {
		CompletableFuture<List<String>> testMethod (String a, double b);
	}
}

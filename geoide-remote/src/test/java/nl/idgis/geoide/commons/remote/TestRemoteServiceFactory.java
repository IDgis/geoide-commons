package nl.idgis.geoide.commons.remote;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class TestRemoteServiceFactory {

	private RemoteMethodClient client; 
	private RemoteServiceFactory factory;
	
	@Before
	public void createFactory () {
		client = mock (RemoteMethodClient.class);
		
		factory = new RemoteServiceFactory (client);
	}
	
	@Test
	public void testInvokeProxyMethod () throws Throwable {
		final TestInterface proxy = factory.createServiceReference (TestInterface.class);
		
		when (client.invokeMethod (any (RemoteMethodCall.class))).thenReturn (CompletableFuture.completedFuture (new ArrayList<> (Arrays.asList (new String[] { "Hello", "World!" }))));
		
		final CompletableFuture<List<String>> result = proxy.testMethod ("Hello, World!", 123.45);

		assertNotNull (result);
		
		final List<String> resultList = result.get ();
		
		assertNotNull (resultList);
		assertEquals (2, resultList.size ());
		assertEquals ("Hello", resultList.get (0));
		assertEquals ("World!", resultList.get (1));
		
		final ArgumentCaptor<RemoteMethodCall> captor = ArgumentCaptor.forClass (RemoteMethodCall.class);

		verify (client, times (1)).invokeMethod (captor.capture ());
		
		assertNotNull (captor.getValue ());
		assertEquals (2, captor.getValue ().getArguments ().size ());
		assertEquals ("Hello, World!", captor.getValue ().getArguments ().get (0));
		assertEquals (123.45, (Double)captor.getValue ().getArguments ().get (1), .0001);
		assertEquals ("testMethod", captor.getValue ().getMethodReference().getName ());
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testCreateProxyWithoutFuture () {
		factory.createServiceReference (TestInterfaceWithoutFuture.class);
	}

	public static interface TestInterface {
		CompletableFuture<List<String>> testMethod (String a, double b);
	}
	
	public static interface TestInterfaceWithoutFuture {
		List<String> testMethod (String a, double b);
	}
}

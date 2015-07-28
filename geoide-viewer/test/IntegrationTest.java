import static play.inject.Bindings.bind;
import static play.test.Helpers.HTMLUNIT;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import nl.idgis.geoide.commons.domain.ExternalizableJsonNode;
import nl.idgis.geoide.commons.domain.Layer;
import nl.idgis.geoide.commons.domain.MapDefinition;
import nl.idgis.geoide.commons.domain.ParameterizedFeatureType;
import nl.idgis.geoide.commons.domain.ServiceIdentification;
import nl.idgis.geoide.commons.domain.ServiceRequest;
import nl.idgis.geoide.commons.domain.api.MapProviderApi;
import nl.idgis.geoide.commons.domain.api.MapQuery;
import nl.idgis.geoide.commons.domain.api.MapView;
import nl.idgis.geoide.commons.domain.api.ServiceProviderApi;
import nl.idgis.geoide.commons.domain.api.TableOfContents;
import nl.idgis.geoide.commons.domain.layer.LayerState;
import nl.idgis.geoide.commons.domain.query.Query;
import nl.idgis.geoide.commons.domain.toc.TOCItem;
import nl.idgis.geoide.commons.domain.traits.Traits;
import play.Application;
import play.Mode;
import play.inject.guice.GuiceApplicationBuilder;

public class IntegrationTest {
	
	private static Application application;
	
	@BeforeClass
	public static void createApplication () {
    	application = new GuiceApplicationBuilder ()
    			.in (Mode.TEST)
    			.in (IntegrationTest.class.getClassLoader ())
    			.overrides (bind (MapView.class).to (MockMapView.class))
    			.overrides (bind (MapQuery.class).to (MockMapQuery.class))
    			.overrides (bind (TableOfContents.class).to (MockTableOfContents.class))
    			.overrides (bind (ServiceProviderApi.class).to (MockServiceProviderApi.class))
    			.overrides (bind (MapProviderApi.class).to (MockMapProviderApi.class))
    			.build ();
	}

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
        running (testServer (application), HTMLUNIT, browser -> {
            browser.goTo("/map/afdelingen");
            // assertThat(browser.pageSource()).contains("id=\"viewer-container\"");
        });
    }
    
    public static class MockMapView implements MapView {

		@Override
		public CompletableFuture<List<Traits<LayerState>>> flattenLayerList(JsonNode arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CompletableFuture<List<ServiceRequest>> getServiceRequests(List<Traits<LayerState>> arg0) {
			// TODO Auto-generated method stub
			return null;
		}
    	
    }
    
    public static class MockMapQuery implements MapQuery {

		@Override
		public CompletableFuture<List<ParameterizedFeatureType<?>>> prepareFeatureTypes(Query arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CompletableFuture<Query> prepareQuery(ExternalizableJsonNode arg0) {
			// TODO Auto-generated method stub
			return null;
		}
    	
    }
    
    public static class MockTableOfContents implements TableOfContents {

		@Override
		public CompletableFuture<List<Traits<TOCItem>>> getItems(MapDefinition arg0) {
			// TODO Auto-generated method stub
			return null;
		}
    	
    }
    
    public static class MockServiceProviderApi implements ServiceProviderApi {

		@Override
		public CompletableFuture<ServiceIdentification> findService(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}
    	
    }
    
    public static class MockMapProviderApi implements MapProviderApi {

		@Override
		public CompletableFuture<List<Layer>> getLayers(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CompletableFuture<MapDefinition> getMapDefinition(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CompletableFuture<List<Layer>> getRootLayers(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}
    }
}

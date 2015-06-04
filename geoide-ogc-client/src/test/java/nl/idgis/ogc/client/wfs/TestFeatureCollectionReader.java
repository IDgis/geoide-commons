package nl.idgis.ogc.client.wfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.idgis.geoide.commons.domain.MimeContentType;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Point;
import org.junit.Test;

public class TestFeatureCollectionReader {

	@Test
	public void testReadGML212 () throws Throwable {
		testReadGML (
			getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/ogc/client/wfs/wfs-featurecollection-gml212.xml"),
			new MimeContentType ("text/xml; subtype=gml/2.1.2"),
			new AssertFeature () {
				@Override
				public void invoke (final Feature feature) {
					assertFeature (feature);
				}
			}
		);
	}
	
	@Test
	public void testReadGML311 () throws Throwable {
		testReadGML (
			getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/ogc/client/wfs/wfs-featurecollection-gml311.xml"),
			new MimeContentType ("text/xml; subtype=gml/3.1.1"),
			new AssertFeature () {
				@Override
				public void invoke (final Feature feature) {
					assertFeature (feature);
				}
			}
		);
	}
	
	@Test
	public void testReadGML321 () throws Throwable {
		testReadGML (
			getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/ogc/client/wfs/wfs-featurecollection-gml321.xml"),
			new MimeContentType ("text/xml; subtype=gml/3.2.1"),
			new AssertFeature () {
				@Override
				public void invoke (final Feature feature) {
					assertFeature (feature);
				}
			}
		);
	}
	
	@Test
	public void testReadWFS2GML321 () throws Throwable {
		testReadGML (
			getClass ().getClassLoader ().getResourceAsStream ("nl/idgis/ogc/client/wfs/wfs-featurecollection-geoserver-gml321.xml"),
			new MimeContentType ("text/xml; subtype=gml/3.2"),
			new AssertFeature () {
				@Override
				public void invoke (final Feature feature) {
					assertEquals ("beschermdenatuurmonumenten", feature.featureTypeName ());
					assertTrue (feature.properties ().containsKey ("naam"));
					assertTrue (feature.properties ().containsKey ("datuminstellingsbesluit"));
					assertTrue (feature.properties ().containsKey ("gebiedsnummer"));
					assertTrue (feature.properties ().containsKey ("shape_area"));
					assertTrue (feature.properties ().get ("geom") instanceof Geometry);
					
					System.out.println (feature.properties ().get ("geom").getClass ().getCanonicalName ());
					// assertTrue (((Geometry)feature.properties ().get ("geom")).get)
				}
			}
		);
	}
	
	private void testReadGML (final InputStream inputStream, final MimeContentType contentType, final AssertFeature assertFeatureCallback) throws Throwable {
		final FeatureCollectionReader reader = new FeatureCollectionReader (contentType);
		final FeatureCollection featureCollection = reader.parseCollection (inputStream);
		
		final List<Feature> features = new ArrayList<> ();
		for (final Feature feature: featureCollection) {
			assertFeatureCallback.invoke (feature);
			features.add (feature);
		}
		
		assertEquals (2, features.size ());
	}
	
	public static interface AssertFeature {
		void invoke (final Feature feature);
	}
	
	private void assertFeature (final Feature feature) {
		assertNotNull (feature);
		assertNotNull (feature.id ());
		
		assertTrue (feature.properties ().containsKey ("geometrie"));
		assertTrue (feature.properties ().get ("geometrie") instanceof Point);
		
		assertTrue (feature.properties ().containsKey ("BEVOEGD_GEZAG"));
		assertTrue (feature.properties ().containsKey ("WM_VERGUNNING"));
		assertTrue (feature.properties ().containsKey ("DATE_CHANGED"));
		assertTrue (feature.properties ().containsKey ("PEILDATUM"));
		assertTrue (feature.properties ().containsKey ("POSTCODE"));
		assertTrue (feature.properties ().containsKey ("GEMEENTE"));
		assertTrue (feature.properties ().containsKey ("PLAATS"));
		assertTrue (feature.properties ().containsKey ("HUISNUMMER"));
		assertTrue (feature.properties ().containsKey ("STRAAT"));
		assertTrue (feature.properties ().containsKey ("AUTORISATIE_DT"));
		assertTrue (feature.properties ().containsKey ("AOBJECTID"));
		assertTrue (feature.properties ().containsKey ("URL"));
		assertTrue (feature.properties ().containsKey ("REDEN_OPNAME"));
		assertTrue (feature.properties ().containsKey ("INDDFE"));
		assertTrue (feature.properties ().containsKey ("IND_BEVI_IRG"));
		assertTrue (feature.properties ().containsKey ("RRGS_ID"));
		assertTrue (feature.properties ().containsKey ("NAAM_INRICHTING"));
	}
}

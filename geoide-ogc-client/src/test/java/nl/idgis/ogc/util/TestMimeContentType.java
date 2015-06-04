package nl.idgis.ogc.util;

import static org.junit.Assert.*;
import nl.idgis.geoide.commons.domain.MimeContentType;

import org.junit.Test;

public class TestMimeContentType {

	public void testValid () {
		new MimeContentType ("text/plain; charset=utf-8");
		new MimeContentType ("text/*; charset=\"utf-8\"");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testInvalid () {
		new MimeContentType ("text/plain/bla");
	}

	@Test
	public void testGMLTypes () {
		// GML types:
		new MimeContentType ("application/gml+xml; version=3.2");
		new MimeContentType ("text/xml; subtype=gml/3.2.1");
		new MimeContentType ("text/xml; subtype=gml/3.1.1");
		new MimeContentType ("application/gml+xml; version=3.2");
		new MimeContentType ("application/gml+xml; version=2.1");
		new MimeContentType ("text/xml; subType=gml/3.1.1/profiles/gmlsf/1.0.0/0");
	}
	
	@Test
	public void testCompare () {
		// Type / subtype matching:
		assertTrue (matches ("text/plain", "text/plain"));
		assertTrue (matches ("text/plain", "text/PLAIN"));
		assertTrue (matches ("text/*", "text/plain"));
		assertTrue (matches ("text/plain", "text/*"));
		assertTrue (matches ("text/*", "text/*"));
		assertFalse (matches ("text/plain", "text/not-plain"));
		assertFalse (matches ("application/plain", "text/plain"));
		
		// Parameter matching:
		assertFalse (matches ("text/plain", "text/plain; charset=utf-8"));
		assertTrue (matches ("text/plain; charset=utf-8", "text/plain; charset=utf-8"));
		assertTrue (matches ("text/plain; charset=UTF-8", "text/plain; charset=utf-8"));
		assertTrue (matches ("text/plain; CHARSET=UTF-8", "text/plain; charset=utf-8"));
		assertTrue (matches ("text/plain;charset=utf-8", "text/plain; charset=utf-8"));
		assertFalse (matches ("text/plain; charset=utf-16", "text/plain; charset=utf-8"));
		
		// Multi parameter matching:
		assertTrue (matches ("text/plain; a=b; c=d", "text/plain; c=d; a=b"));
		assertTrue (matches ("text/plain; a=b; c=d", "text/plain;c=d;a=b"));
	}
	
	private boolean matches (final String a, final String b) {
		final MimeContentType typeA = new MimeContentType (a);
		final MimeContentType typeB = new MimeContentType (b);
		
		if (typeA.equals (typeB)) {
			assertEquals (typeA.hashCode (), typeB.hashCode ());
		}
		
		return typeA.matches (typeB);
	}
}

package nl.idgis.geoide.commons.print;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

public class FopTranscode {

	public static void main (final String[] args) throws Throwable {
		if (args.length < 1) {
			System.err.println ("Usage: fop [output.pdf]");
			System.exit (1);
			return;
		}

		try (final OutputStream out = new BufferedOutputStream (new FileOutputStream (new File (args[0])))) {
			// Construct FOP with the PDF output format:
			final FopFactory fopFactory = FopFactory.newInstance ();	// Factory must be re-used during JVM lifetime.
			fopFactory.ignoreNamespace ("http://www.w3.org/2001/XMLSchema-instance");
			final Fop fop = fopFactory.newFop (MimeConstants.MIME_PDF, out);
			
			// Create a JAXP transformer:
			final TransformerFactory transformerFactory = TransformerFactory.newInstance ();
			final Transformer transformer = transformerFactory.newTransformer ();
			
			// Setup input and output of the XSLT transformation:
			final InputStream stream = FopTranscode.class.getClassLoader ().getResourceAsStream ("nl/idgis/geoide/commons/print/a4-portrait.fo");
			final Source source = new StreamSource (stream);
			final Result result = new SAXResult (fop.getDefaultHandler ());
			
			transformer.transform (source, result);
		}
	}
}

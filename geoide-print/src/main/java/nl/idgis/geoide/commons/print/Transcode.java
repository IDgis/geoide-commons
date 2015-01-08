package nl.idgis.geoide.commons.print;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.util.ParsedURL;
import org.apache.fop.svg.PDFTranscoder;

public class Transcode {

	public static void main (final String[] args) throws Throwable {
		if (args.length < 2) {
			System.err.println ("Usage: transcode [input.svg] [output.pdf]");
			System.exit (1);
			return;
		}
		
		// ParsedURL.registerHandler (handler);
		
		final Transcoder transcoder = new PDFTranscoder ();
		final TranscoderInput transcoderInput = new TranscoderInput (new FileInputStream (new File (args[0])));
		final TranscoderOutput transcoderOutput = new TranscoderOutput (new FileOutputStream (new File (args[1])));
		
		transcoder.transcode (transcoderInput, transcoderOutput);
	}
}

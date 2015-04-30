package nl.idgis.geoide.commons.report.render;

import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SvgRenderer {
	
	private final static String NS = "http://www.w3.org/2000/svg";
	
	@FunctionalInterface
	public static interface RenderCallback {
		public void render (final XMLStreamWriter writer) throws XMLStreamException;
	}
	
	@FunctionalInterface
	public static interface RenderFunction<T> {
		public T render (final XMLStreamWriter writer) throws XMLStreamException;
	}
	
	public void svg (final XMLStreamWriter writer, final double minX, final double minY, final double width, final double height, final RenderCallback content) throws XMLStreamException {
		svg (writer, minX, minY, width, height, (w) -> {
			content.render (w);
			return true;
		});
	}
	
	public <T> T svg (final XMLStreamWriter writer, final double minX, final double minY, final double width, final double height, final RenderFunction<T> content) throws XMLStreamException {
		writer.writeStartDocument ();

		writer.writeStartElement ("svg");
		writer.writeDefaultNamespace (NS);
		writer.writeAttribute ("width", String.format (Locale.US, "%f", width));
		writer.writeAttribute ("height", String.format (Locale.US, "%f", height));
		writer.writeAttribute ("viewBox", String.format (Locale.US, "%f %f %f %f", minX, minY, width, height));

		final T result = content.render (writer);
		
		writer.writeEndElement ();	// svg
		
		writer.writeEndDocument ();
		
		return result;
	}
	
	public void circle (final XMLStreamWriter writer, final double cx, final double cy, final double radius, final Stroke stroke, final Fill fill) throws XMLStreamException {
		writer.writeStartElement (NS, "circle");

		writer.writeAttribute ("cx", String.format (Locale.US, "%f", cx));
		writer.writeAttribute ("cy", String.format (Locale.US, "%f", cy));
		writer.writeAttribute ("r", String.format (Locale.US, "%f", radius));

		strokeAttributes (writer, stroke);
		fillAttributes (writer, fill);
		
		writer.writeEndElement ();	// circle;
	}
	
	public void polyline (final XMLStreamWriter writer, final List<SvgPoint> points, final Stroke stroke) throws XMLStreamException {
		final StringBuffer pointsBuffer = new StringBuffer ();
		
		for (final SvgPoint point: points) {
			if (pointsBuffer.length () > 0) {
				pointsBuffer.append (" ");
			}
			
			pointsBuffer.append (String.format (Locale.US, "%f,%f", point.getX (), point.getY ()));
		}
		
		writer.writeStartElement (NS, "polyline");
		
		strokeAttributes (writer, stroke);
		fillAttributes (writer, null);
		
		writer.writeAttribute ("points", pointsBuffer.toString ());
		
		writer.writeEndElement (); // polyline
	}
	
	@SafeVarargs
	public final void path (final XMLStreamWriter writer, final Stroke stroke, final Fill fill, final List<SvgPoint> ... pointLists) throws XMLStreamException {
		final StringBuilder builder = new StringBuilder ();
		
		for (final List<SvgPoint> pointList: pointLists) {
			if (pointList == null || pointList.size () < 2) {
				continue;
			}
			
			// Move to the first point:
			builder.append (String.format (Locale.US, "M %f %f ", pointList.get (0).getX (), pointList.get (0).getY ()));
			
			// Draw lines to the next points:
			for (int i = 1; i < pointList.size (); ++ i) {
				builder.append (String.format (Locale.US, "L %f %f ", pointList.get (i).getX (), pointList.get (i).getY ()));
			}
		}
		
		writer.writeStartElement (NS, "path");
		
		writer.writeAttribute ("d", builder.toString ());
		
		strokeAttributes (writer, stroke);
		fillAttributes (writer, fill);
		
		writer.writeEndElement (); // path
	}
	
	public void polygon (final XMLStreamWriter writer, final List<SvgPoint> points, final Stroke stroke, final Fill fill) throws XMLStreamException {
		final StringBuffer pointsBuffer = new StringBuffer ();
		
		for (final SvgPoint point: points) {
			if (pointsBuffer.length () > 0) {
				pointsBuffer.append (" ");
			}
			
			pointsBuffer.append (String.format (Locale.US, "%f,%f", point.getX (), point.getY ()));
		}
		
		writer.writeStartElement (NS, "polygon");
		
		strokeAttributes (writer, stroke);
		fillAttributes (writer, fill);
		
		writer.writeAttribute ("points", pointsBuffer.toString ());
		
		writer.writeEndElement (); // polygon
	}
	
	public void strokeAttributes (final XMLStreamWriter writer, final Stroke stroke) throws XMLStreamException {
		if (stroke == null || stroke.getWidth () <= 0) {
			writer.writeAttribute ("stroke", "none");
			return;
		}
		
		writer.writeAttribute ("stroke", stroke.getColor ());
		writer.writeAttribute ("stroke-width", String.format (Locale.US, "%f", stroke.getWidth ()));
		if (Math.abs (stroke.getOpacity () - 1.0) >= .0001) {
			writer.writeAttribute ("stroke-opacity", String.format (Locale.US, "%f", stroke.getOpacity ()));
		}
	}
	
	public void fillAttributes (final XMLStreamWriter writer, final Fill fill) throws XMLStreamException {
		if (fill == null) {
			writer.writeAttribute ("fill", "none");
			return;
		}
		
		writer.writeAttribute ("fill", fill.getColor ());
		if (Math.abs (fill.getOpacity () - 1.0) >= .0001) {
			writer.writeAttribute ("fill-opacity", String.format (Locale.US, "%f", fill.getOpacity ()));
		}
	}
	
	public static class SvgPoint {
		private final double x;
		private final double y;
		
		public SvgPoint (final double x, final double y) {
			this.x = x;
			this.y = y;
		}

		public double getX () {
			return x;
		}

		public double getY () {
			return y;
		}
	}
	
	public static class Stroke {
		private final String color;
		private final double opacity;
		private final double width;
		
		public Stroke (final String color, final double opacity, final double width) {
			this.color = color;
			this.opacity = opacity;
			this.width = width;
		}

		public String getColor() {
			return color;
		}

		public double getOpacity() {
			return opacity;
		}

		public double getWidth() {
			return width;
		}
	}
	
	public static class Fill {
		private final String color;
		private final double opacity;
		
		public Fill (final String color, final double opacity) {
			this.color = color;
			this.opacity = opacity;
		}

		public String getColor () {
			return color;
		}

		public double getOpacity () {
			return opacity;
		}
	}
}

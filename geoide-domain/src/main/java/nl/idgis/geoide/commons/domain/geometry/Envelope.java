package nl.idgis.geoide.commons.domain.geometry;

import java.io.Serializable;

public final class Envelope implements Serializable {
	private static final long serialVersionUID = -4932613830993434967L;
	
	private final double minX;
	private final double minY;
	private final double maxX;
	private final double maxY;
	
	public Envelope (final double minX, final double minY, final double maxX, final double maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	public double getMinX () {
		return minX;
	}

	public double getMinY () {
		return minY;
	}

	public double getMaxX () {
		return maxX;
	}

	public double getMaxY () {
		return maxY;
	}
	
	public Envelope combine (final Envelope other) {
		return new Envelope (
				Math.min (minX, other.minX),
				Math.min (minY, other.minY),
				Math.max (maxX, other.maxX),
				Math.max (maxY, other.maxY)
			);
	}
}

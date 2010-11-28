/*
 * Copyright 2010 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.android.maps;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

/**
 * CircleOverlay is a special Overlay to display a circle on top of the map. Center point and
 * radius of the circle are adjustable.
 * <p>
 * All rendering parameters like color, stroke width, pattern and transparency can be configured
 * via the two {@link android.graphics.Paint Paint} objects in the
 * {@link #CircleOverlay(Paint,Paint) constructor}. Anti-aliasing is always used to improve the
 * visual quality of the image.
 * <p>
 * <b>The implementation of this class is not complete. Its functionality and visible methods
 * are likely to change in a future release.</b>
 */
public class CircleOverlay extends Overlay {
	private static final String THREAD_NAME = "CircleOverlay";

	private Point cachedCenterPosition;
	private byte cachedZoomLevel;
	private GeoPoint center;
	private Paint fillPaint;
	private Paint outlinePaint;
	private final Path path;
	private float radius;

	/**
	 * Constructs a new CircleOverlay.
	 * 
	 * @param fillPaint
	 *            the paint object which will be used to fill the circle.
	 * @param outlinePaint
	 *            the paint object which will be used to draw the outline of the circle.
	 */
	public CircleOverlay(Paint fillPaint, Paint outlinePaint) {
		setPaint(fillPaint, outlinePaint);
		this.path = new Path();
		this.cachedZoomLevel = Byte.MIN_VALUE;
	}

	/**
	 * Sets the parameters of the circle.
	 * 
	 * @param center
	 *            the geographical coordinates of the center point.
	 * @param radius
	 *            the radius of the circle.
	 */
	public synchronized void setCircleData(GeoPoint center, float radius) {
		this.center = center;
		if (this.center != null) {
			// create the array for the cached center point position
			this.cachedCenterPosition = new Point();
		}
		this.radius = radius;
	}

	/**
	 * Sets the paint parameters which will be used to draw the circle.
	 * 
	 * @param fillPaint
	 *            the paint object which will be used to fill the circle.
	 * @param outlinePaint
	 *            the paint object which will be used to draw the outline of the circle.
	 */
	public synchronized void setPaint(Paint fillPaint, Paint outlinePaint) {
		this.fillPaint = fillPaint;
		if (this.fillPaint != null) {
			this.fillPaint.setAntiAlias(true);
		}
		this.outlinePaint = outlinePaint;
		if (this.outlinePaint != null) {
			this.outlinePaint.setAntiAlias(true);
		}
	}

	/**
	 * This method should be called after a center point has been added to the Overlay.
	 */
	protected final void populate() {
		super.requestRedraw();
	}

	@Override
	final synchronized void drawOverlayBitmap(Point drawPosition, byte drawZoomLevel) {
		if (this.center == null || this.radius < 0) {
			// no valid parameters to draw the circle
			return;
		} else if (this.fillPaint == null && this.outlinePaint == null) {
			// no paint to draw
			return;
		}

		// make sure that the cached center position is valid
		if (drawZoomLevel != this.cachedZoomLevel) {
			this.cachedCenterPosition = this.projection.toPoint(this.center,
					this.cachedCenterPosition, drawZoomLevel);
			this.cachedZoomLevel = drawZoomLevel;
		}

		// assemble the path
		this.path.reset();
		this.path.addCircle(this.cachedCenterPosition.x - drawPosition.x,
				this.cachedCenterPosition.y - drawPosition.y, this.radius, Path.Direction.CCW);

		// draw the path on the canvas
		if (this.fillPaint != null) {
			this.internalCanvas.drawPath(this.path, this.fillPaint);
		}
		if (this.outlinePaint != null) {
			this.internalCanvas.drawPath(this.path, this.outlinePaint);
		}
	}

	@Override
	String getThreadName() {
		return THREAD_NAME;
	}
}
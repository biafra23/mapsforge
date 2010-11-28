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
 * RouteOverlay is a special Overlay to display a sequence of way nodes. To draw an arbitrary
 * polygon, add a way node sequence where the first and the last way node are equal.
 * <p>
 * All rendering parameters like color, stroke width, pattern and transparency can be configured
 * via the {@link android.graphics.Paint Paint} object in the {@link #RouteOverlay(Paint)
 * constructor}. Anti-aliasing is always used to improve the visual quality of the image.
 */
public class RouteOverlay extends Overlay {
	private static final String THREAD_NAME = "RouteOverlay";

	private Point[] cachedWayPositions;
	private byte cachedZoomLevel;
	private Paint paint;
	private final Path path;
	private GeoPoint[] wayNodes;

	/**
	 * Constructs a new RouteOverlay.
	 * 
	 * @param paint
	 *            the paint object which will be used to draw the route.
	 */
	public RouteOverlay(Paint paint) {
		setPaint(paint);
		this.path = new Path();
		this.cachedZoomLevel = Byte.MIN_VALUE;
	}

	/**
	 * Sets the paint object which will be used to draw the route.
	 * 
	 * @param paint
	 *            the paint object which will be used to draw the route.
	 */
	public synchronized void setPaint(Paint paint) {
		this.paint = paint;
		if (this.paint != null) {
			this.paint.setAntiAlias(true);
		}
	}

	/**
	 * Sets the way nodes of the route.
	 * 
	 * @param wayNodes
	 *            the geographical coordinates of the way nodes.
	 */
	public synchronized void setRouteData(GeoPoint[] wayNodes) {
		this.wayNodes = wayNodes;
		if (this.wayNodes != null) {
			// create the array for the cached way node positions
			this.cachedWayPositions = new Point[this.wayNodes.length];
		}
	}

	/**
	 * This method should be called after way nodes have been added to the Overlay.
	 */
	protected final void populate() {
		super.requestRedraw();
	}

	@Override
	final synchronized void drawOverlayBitmap(Point drawPosition, byte drawZoomLevel) {
		if (this.cachedWayPositions == null || this.cachedWayPositions.length < 1) {
			// no way nodes to draw
			return;
		} else if (this.paint == null) {
			// no paint to draw
			return;
		}

		// make sure that the cached way node positions are valid
		if (drawZoomLevel != this.cachedZoomLevel) {
			for (int i = 0; i < this.cachedWayPositions.length; ++i) {
				this.cachedWayPositions[i] = this.projection.toPoint(this.wayNodes[i],
						this.cachedWayPositions[i], drawZoomLevel);
			}
			this.cachedZoomLevel = drawZoomLevel;
		}

		// assemble the path
		this.path.reset();
		this.path.moveTo(this.cachedWayPositions[0].x - drawPosition.x,
				this.cachedWayPositions[0].y - drawPosition.y);
		for (int i = 1; i < this.cachedWayPositions.length; ++i) {
			this.path.lineTo(this.cachedWayPositions[i].x - drawPosition.x,
					this.cachedWayPositions[i].y - drawPosition.y);
		}

		// draw the path on the canvas
		this.internalCanvas.drawPath(this.path, this.paint);
	}

	@Override
	String getThreadName() {
		return THREAD_NAME;
	}
}
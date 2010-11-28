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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Overlay is the abstract base class to display geographical data such as points and ways on
 * top of the map. To add an Overlay to a <code>MapView</code>, create a subclass of this class
 * and add an instance to the list returned by {@link MapView#getOverlays()}.
 * <p>
 * This implementation runs in a separate thread to avoid blocking the UI thread.
 */
public abstract class Overlay extends Thread {
	/**
	 * Reference to the MapView instance.
	 */
	private MapView internalMapView;

	/**
	 * Flag to indicate if this Overlay is new to the MapView.
	 */
	private boolean isNew;

	/**
	 * Transformation matrix for the Overlay.
	 */
	private final Matrix matrix;

	/**
	 * Used to calculate the scale of the transformation matrix.
	 */
	private float matrixScaleFactor;

	/**
	 * First internal bitmap for the Overlay to draw on.
	 */
	private Bitmap overlayBitmap1;

	/**
	 * Second internal bitmap for the Overlay to draw on.
	 */
	private Bitmap overlayBitmap2;

	/**
	 * A temporary reference to swap the two Overlay bitmaps.
	 */
	private Bitmap overlayBitmapSwap;

	/**
	 * Stores the top-left map position at which the redraw should happen.
	 */
	private final Point point;

	/**
	 * Stores the map position before drawing starts.
	 */
	private Point positionAfterDraw;

	/**
	 * Stores the map position after drawing is finished.
	 */
	private Point positionBeforeDraw;

	/**
	 * Flag to indicate if the Overlay should redraw itself.
	 */
	private boolean redraw;

	/**
	 * Stores the zoom level after drawing is finished.
	 */
	private byte zoomLevelAfterDraw;

	/**
	 * Stores the zoom level before drawing starts.
	 */
	private byte zoomLevelBeforeDraw;

	/**
	 * Used to calculate the zoom level difference.
	 */
	private byte zoomLevelDiff;

	/**
	 * Canvas that is used in the Overlay for drawing.
	 */
	Canvas internalCanvas;

	/**
	 * A cached reference to the MapView projection.
	 */
	Projection projection;

	/**
	 * Default constructor which must be called by all subclasses.
	 */
	public Overlay() {
		this.isNew = true;
		this.matrix = new Matrix();
		this.point = new Point();
		start();
	}

	/**
	 * Draws the Overlay on top of the map. This will be called by the MapView.
	 * 
	 * @param canvas
	 *            the canvas the Overlay will be drawn onto.
	 * @param mapView
	 *            the calling MapView.
	 * @param shadow
	 *            true if the shadow layer should be drawn, false otherwise.
	 */
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		synchronized (this.matrix) {
			canvas.drawBitmap(this.overlayBitmap1, this.matrix, null);
		}
	}

	/**
	 * Handles a key down event.
	 * <p>
	 * The default implementation of this method does nothing and returns false.
	 * 
	 * @param keyCode
	 *            the keyCode of the event.
	 * @param event
	 *            the key down event.
	 * @param mapView
	 *            the MapView that triggered the event.
	 * @return true if the event was handled, false otherwise.
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event, MapView mapView) {
		return false;
	}

	/**
	 * Handles a key up event.
	 * <p>
	 * The default implementation of this method does nothing and returns false.
	 * 
	 * @param keyCode
	 *            the keyCode of the event.
	 * @param event
	 *            the key up event.
	 * @param mapView
	 *            the MapView that triggered the event.
	 * @return true if the event was handled, false otherwise.
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event, MapView mapView) {
		return false;
	}

	/**
	 * Handles a touch event.
	 * <p>
	 * The default implementation of this method does nothing and returns false.
	 * 
	 * @param event
	 *            the touch event.
	 * @param mapView
	 *            the MapView that triggered the event.
	 * @return true if the event was handled, false otherwise.
	 */
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		return false;
	}

	/**
	 * Handles a trackball event.
	 * <p>
	 * The default implementation of this method does nothing and returns false.
	 * 
	 * @param event
	 *            the trackball event.
	 * @param mapView
	 *            the MapView that triggered the event.
	 * @return true if the event was handled, false otherwise.
	 */
	public boolean onTrackballEvent(MotionEvent event, MapView mapView) {
		return false;
	}

	@Override
	public final void run() {
		setName(getThreadName());

		while (!isInterrupted()) {
			synchronized (this) {
				while (!isInterrupted() && (!this.redraw)) {
					try {
						wait();
					} catch (InterruptedException e) {
						// restore the interrupted status
						interrupt();
					}
				}
			}

			if (isInterrupted()) {
				break;
			}

			this.redraw = false;
			if (!isNew()) {
				redraw();
			}
		}

		// free the Overlay bitmaps memory
		if (this.overlayBitmap1 != null) {
			this.overlayBitmap1.recycle();
			this.overlayBitmap1 = null;
		}

		if (this.overlayBitmap2 != null) {
			this.overlayBitmap2.recycle();
			this.overlayBitmap2 = null;
		}

		// set some fields to null to avoid memory leaks
		this.internalMapView = null;
		this.projection = null;
		this.internalCanvas = null;
	}

	/**
	 * Redraws the Overlay.
	 */
	private void redraw() {
		// clear the second bitmap and make the canvas use it
		this.overlayBitmap2.eraseColor(Color.TRANSPARENT);
		this.internalCanvas.setBitmap(this.overlayBitmap2);

		// save the zoom level and map position before drawing
		synchronized (this.internalMapView) {
			this.zoomLevelBeforeDraw = this.internalMapView.getZoomLevel();
			this.positionBeforeDraw = this.projection.toPoint(this.internalMapView
					.getMapCenter(), this.positionBeforeDraw, this.zoomLevelBeforeDraw);
		}

		this.point.x = this.positionBeforeDraw.x - (this.internalCanvas.getWidth() >> 1);
		this.point.y = this.positionBeforeDraw.y - (this.internalCanvas.getHeight() >> 1);
		this.drawOverlayBitmap(this.point, this.zoomLevelBeforeDraw);

		// save the zoom level and map position after drawing
		synchronized (this.internalMapView) {
			this.zoomLevelAfterDraw = this.internalMapView.getZoomLevel();
			this.positionAfterDraw = this.projection.toPoint(this.internalMapView
					.getMapCenter(), this.positionAfterDraw, this.zoomLevelBeforeDraw);
		}

		// adjust the transformation matrix of the Overlay
		synchronized (this.matrix) {
			this.matrix.reset();
			this.matrix.postTranslate(this.positionBeforeDraw.x - this.positionAfterDraw.x,
					this.positionBeforeDraw.y - this.positionAfterDraw.y);

			this.zoomLevelDiff = (byte) (this.zoomLevelAfterDraw - this.zoomLevelBeforeDraw);
			if (this.zoomLevelDiff > 0) {
				// zoom level has increased
				this.matrixScaleFactor = 1 << this.zoomLevelDiff;
			} else if (this.zoomLevelDiff < 0) {
				// zoom level has decreased
				this.matrixScaleFactor = 1.0f / (1 << -this.zoomLevelDiff);
			} else {
				// zoom level is unchanged
				this.matrixScaleFactor = 1;
			}
			this.matrix.postScale(this.matrixScaleFactor, this.matrixScaleFactor,
					this.internalCanvas.getWidth() >> 1, this.internalCanvas.getHeight() >> 1);
		}

		// swap the two Overlay bitmaps
		this.overlayBitmapSwap = this.overlayBitmap1;
		this.overlayBitmap1 = this.overlayBitmap2;
		this.overlayBitmap2 = this.overlayBitmapSwap;

		// request the MapView to redraw
		this.internalMapView.postInvalidate();
	}

	/**
	 * Draws the Overlay on the bitmap.
	 * 
	 * @param drawPosition
	 *            the top-left position of the map relative to the world map.
	 * @param drawZoomLevel
	 *            the zoom level of the map.
	 */
	abstract void drawOverlayBitmap(Point drawPosition, byte drawZoomLevel);

	/**
	 * Returns the name of the Overlay implementation. It will be used as the name for the
	 * Overlay thread.
	 * 
	 * @return the name of the Overlay implementation.
	 */
	abstract String getThreadName();

	/**
	 * Checks if this Overlay is new and {@link #setupOverlay(MapView)} should be called.
	 * 
	 * @return true if this Overlay is new and needs to be set up, false otherwise.
	 */
	final boolean isNew() {
		return this.isNew;
	}

	/**
	 * @param sx
	 *            the horizontal scale.
	 * @param sy
	 *            the vertical scale.
	 * @param px
	 *            the horizontal pivot point.
	 * @param py
	 *            the vertical pivot point.
	 */
	final void matrixPostScale(float sx, float sy, float px, float py) {
		synchronized (this.matrix) {
			this.matrix.postScale(sx, sy, px, py);
		}
	}

	/**
	 * @param dx
	 *            the horizontal translation.
	 * @param dy
	 *            the vertical translation.
	 */
	final void matrixPostTranslate(float dx, float dy) {
		synchronized (this.matrix) {
			this.matrix.postTranslate(dx, dy);
		}
	}

	/**
	 * Requests a redraw of the Overlay. This method gets called by the {@link MapView}.
	 */
	final void requestRedraw() {
		this.redraw = true;
		synchronized (this) {
			notify();
		}
	}

	/**
	 * Initializes the Overlay. This method must be called by the MapView once on each new
	 * Overlay and every time the size or the projection of the MapView has changed.
	 * 
	 * @param mapView
	 *            the calling MapView.
	 */
	final void setupOverlay(MapView mapView) {
		this.internalMapView = mapView;

		// save a reference to the MapView projection
		this.projection = this.internalMapView.getProjection();

		// check if the previous Overlay bitmaps must be recycled
		if (this.overlayBitmap1 != null) {
			this.overlayBitmap1.recycle();
		}
		if (this.overlayBitmap2 != null) {
			this.overlayBitmap2.recycle();
		}

		// create the two Overlay bitmaps with the correct dimensions
		this.overlayBitmap1 = Bitmap.createBitmap(this.internalMapView.getWidth(),
				this.internalMapView.getHeight(), Bitmap.Config.ARGB_8888);
		this.overlayBitmap2 = Bitmap.createBitmap(this.internalMapView.getWidth(),
				this.internalMapView.getHeight(), Bitmap.Config.ARGB_8888);
		this.internalCanvas = new Canvas();

		this.isNew = false;
		requestRedraw();
	}
}
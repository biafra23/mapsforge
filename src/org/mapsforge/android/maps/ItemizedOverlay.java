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

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

/**
 * ItemizedOverlay is an abstract base class to display a list of OverlayItems.
 * 
 * @param <Item>
 *            the type of items handled by this Overlay.
 */
public abstract class ItemizedOverlay<Item extends OverlayItem> extends Overlay {
	private static final String THREAD_NAME = "ItemizedOverlay";

	private int bottom;
	private final Drawable defaultMarker;
	private Point itemPoint;
	private Drawable itemMarker;
	private final Point itemPosition;
	private int left;
	private int numberOfItems;
	private Item overlayItem;
	private int right;
	private Point tapPosition;
	private int top;

	/**
	 * Constructs a new ItemizedOverlay.
	 * 
	 * @param defaultMarker
	 *            the default marker for each item.
	 */
	public ItemizedOverlay(Drawable defaultMarker) {
		this.defaultMarker = defaultMarker;
		this.itemPosition = new Point();
	}

	@Override
	public final synchronized void drawOverlayBitmap(Canvas canvas, Point drawPosition,
			Projection projection, byte drawZoomLevel) {
		this.numberOfItems = size();
		if (this.numberOfItems < 1) {
			// no items to draw
			return;
		}

		// draw the Overlay items
		for (int i = 0; i < this.numberOfItems; ++i) {
			// get the current item
			this.overlayItem = createItem(i);

			// check if the item has a position
			if (this.overlayItem.getPoint() == null) {
				continue;
			}

			// make sure that the cached item position is valid
			if (drawZoomLevel != this.overlayItem.cachedZoomLevel) {
				this.overlayItem.cachedMapPosition = projection.toPoint(this.overlayItem
						.getPoint(), this.overlayItem.cachedMapPosition, drawZoomLevel);
				this.overlayItem.cachedZoomLevel = drawZoomLevel;
			}

			// calculate the relative item position on the display
			this.itemPosition.x = this.overlayItem.cachedMapPosition.x - drawPosition.x;
			this.itemPosition.y = this.overlayItem.cachedMapPosition.y - drawPosition.y;

			// get the correct marker for the item
			if (this.overlayItem.getMarker() == null) {
				this.itemMarker = this.defaultMarker;
			} else {
				this.itemMarker = this.overlayItem.getMarker();
			}

			// calculate the bounding box of the centered marker
			this.left = this.itemPosition.x - (this.itemMarker.getIntrinsicWidth() / 2);
			this.right = this.itemPosition.x + (this.itemMarker.getIntrinsicWidth() / 2);
			this.top = this.itemPosition.y - (this.itemMarker.getIntrinsicHeight() / 2);
			this.bottom = this.itemPosition.y + (this.itemMarker.getIntrinsicHeight() / 2);

			// check if the bounding box of the marker intersects with the canvas
			if (this.right >= 0 && this.left <= canvas.getWidth() && this.bottom >= 0
					&& this.top <= canvas.getHeight()) {
				// set the relative center position of the marker
				this.itemMarker.setBounds(this.itemPosition.x
						- this.itemMarker.getIntrinsicWidth() / 2, this.itemPosition.y
						- this.itemMarker.getIntrinsicHeight() / 2, this.itemPosition.x
						+ this.itemMarker.getIntrinsicWidth() / 2, this.itemPosition.y
						+ this.itemMarker.getIntrinsicHeight() / 2);

				// draw the item marker on the canvas
				this.itemMarker.draw(canvas);
			}
		}
	}

	@Override
	public final String getThreadName() {
		return THREAD_NAME;
	}

	@Override
	public synchronized boolean onTap(GeoPoint geoPoint, MapView mapView) {
		Projection projection = mapView.getProjection();
		this.tapPosition = projection.toPixels(geoPoint, this.tapPosition);

		// iterate over all items
		for (int i = size() - 1; i >= 0; --i) {
			// get the current item
			this.overlayItem = createItem(i);

			// check if the item has a position
			if (this.overlayItem.getPoint() == null) {
				continue;
			}

			this.itemPoint = projection.toPixels(this.overlayItem.getPoint(), this.itemPoint);

			// select the correct marker for the item
			if (this.overlayItem.getMarker() == null) {
				this.itemMarker = this.defaultMarker;
			} else {
				this.itemMarker = this.overlayItem.getMarker();
			}

			// check if the hit position is within the bounds of the marker
			if (Math.abs(this.itemPoint.x - this.tapPosition.x) <= this.itemMarker
					.getIntrinsicWidth() / 2
					&& Math.abs(this.itemPoint.y - this.tapPosition.y) <= this.itemMarker
							.getIntrinsicHeight() / 2) {
				return onTap(i);
			}
		}

		// no hit
		return false;
	}

	/**
	 * Returns the numbers of items in this Overlay.
	 * 
	 * @return the numbers of items in this Overlay.
	 */
	public abstract int size();

	/**
	 * Creates an item in the Overlay.
	 * 
	 * @param i
	 *            the index of the item.
	 * @return the item.
	 */
	protected abstract Item createItem(int i);

	/**
	 * Handles a tap event.
	 * <p>
	 * The default implementation of this method does nothing and returns false.
	 * 
	 * @param index
	 *            the position of the item.
	 * 
	 * @return true if the event was handled, false otherwise.
	 */
	protected boolean onTap(int index) {
		return false;
	}

	/**
	 * This method should be called after items have been added to the Overlay.
	 */
	protected final void populate() {
		super.requestRedraw();
	}
}
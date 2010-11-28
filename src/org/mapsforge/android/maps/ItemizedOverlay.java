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

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

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
	private Item hitTestItem;
	private Drawable hitTestMarker;
	private Point hitTestPosition;
	private Drawable itemMarker;
	private final Point itemPosition;
	private int left;
	private int numberOfItems;
	private Item overlayItem;
	private int right;
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
	public synchronized boolean onTouchEvent(MotionEvent event, MapView mapView) {
		// iterate over all items
		for (int i = size() - 1; i >= 0; --i) {
			// get the current item
			this.hitTestItem = createItem(i);

			if (hitTest(this.hitTestItem, this.hitTestItem.getMarker(), (int) event.getX(),
					(int) event.getY())) {
				// abort the testing at the first hit
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
	 * Calculates if the given point is within the bounds of an item.
	 * 
	 * @param item
	 *            the item to test.
	 * @param marker
	 *            the marker of the item.
	 * @param hitX
	 *            the x coordinate of the point.
	 * @param hitY
	 *            the y coordinate of the point.
	 * @return true if the point is within the bounds of the item.
	 */
	protected boolean hitTest(Item item, Drawable marker, int hitX, int hitY) {
		// check if the item has a position
		if (item.getPoint() == null) {
			return false;
		}
		this.hitTestPosition = this.projection.toPixels(item.getPoint(), this.hitTestPosition);

		// select the correct marker for the item
		if (marker == null) {
			this.hitTestMarker = this.defaultMarker;
		} else {
			this.hitTestMarker = marker;
		}

		// check if the hit position is within the bounds of the marker
		if (Math.abs(this.hitTestPosition.x - hitX) <= this.hitTestMarker.getIntrinsicWidth() / 2
				&& Math.abs(this.hitTestPosition.y - hitY) <= this.hitTestMarker
						.getIntrinsicHeight() / 2) {
			return true;
		}
		return false;
	}

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

	@Override
	final synchronized void drawOverlayBitmap(Point drawPosition, byte drawZoomLevel) {
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
				this.overlayItem.cachedMapPosition = this.projection.toPoint(this.overlayItem
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
			if (this.right >= 0 && this.left <= this.internalCanvas.getWidth()
					&& this.bottom >= 0 && this.top <= this.internalCanvas.getHeight()) {
				// set the relative center position of the marker
				this.itemMarker.setBounds(this.itemPosition.x
						- this.itemMarker.getIntrinsicWidth() / 2, this.itemPosition.y
						- this.itemMarker.getIntrinsicHeight() / 2, this.itemPosition.x
						+ this.itemMarker.getIntrinsicWidth() / 2, this.itemPosition.y
						+ this.itemMarker.getIntrinsicHeight() / 2);

				// draw the item marker on the canvas
				this.itemMarker.draw(this.internalCanvas);
			}
		}
	}

	@Override
	final String getThreadName() {
		return THREAD_NAME;
	}
}
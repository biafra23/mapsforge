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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Thread-safe implementation of the {@link ItemizedOverlay} class using an {@link ArrayList} as
 * internal data structure.
 */
public class ArrayItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	private static final int ARRAY_LIST_INITIAL_CAPACITY = 8;

	private final Context context;
	private AlertDialog.Builder dialog;
	private OverlayItem item;
	private final ArrayList<OverlayItem> overlayItems;

	/**
	 * Constructs a new ArrayItemizedOverlay.
	 * 
	 * @param defaultMarker
	 *            the default marker.
	 * @param context
	 *            the reference to the application context.
	 */
	public ArrayItemizedOverlay(Drawable defaultMarker, Context context) {
		super(defaultMarker);
		this.context = context;
		this.overlayItems = new ArrayList<OverlayItem>(ARRAY_LIST_INITIAL_CAPACITY);
	}

	/**
	 * Adds the given item to the Overlay.
	 * 
	 * @param overlayItem
	 *            the item that should be added to the Overlay.
	 */
	public synchronized void addOverlay(OverlayItem overlayItem) {
		this.overlayItems.add(overlayItem);
	}

	@Override
	public synchronized int size() {
		return this.overlayItems.size();
	}

	@Override
	protected synchronized OverlayItem createItem(int i) {
		return this.overlayItems.get(i);
	}

	@Override
	protected synchronized boolean onTap(int index) {
		this.item = this.overlayItems.get(index);
		this.dialog = new AlertDialog.Builder(this.context);
		this.dialog.setTitle(this.item.getTitle());
		this.dialog.setMessage(this.item.getSnippet());
		this.dialog.show();
		return true;
	}
}
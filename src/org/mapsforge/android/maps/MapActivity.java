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

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * MapActivity is the abstract base class which must be extended in order to use a
 * {@link MapView}. There are no abstract methods in this implementation that subclasses need to
 * override. In addition, no API key or registration is required.
 * <p>
 * A subclass may create a MapView either via one of the MapView constructors or by inflating an
 * XML layout file. It is possible to use more than one MapView at the same time as each of them
 * works independently from the others.
 * <p>
 * When the MapActivity is shut down, the current center position, zoom level and map file of
 * the MapView are saved in a preferences file and restored automatically during the setup
 * process of a MapView.
 */
public abstract class MapActivity extends Activity {
	/**
	 * Name of the file where the map position and other settings are stored.
	 */
	private static final String PREFERENCES_FILE = "MapActivity";

	/**
	 * Counter to store the last ID given to a MapView.
	 */
	private int lastMapViewId;

	/**
	 * Internal list which contains references to all running MapView objects.
	 */
	private ArrayList<MapView> mapViews = new ArrayList<MapView>(2);

	private void destroyMapViews() {
		if (this.mapViews != null) {
			MapView currentMapView;
			while (!this.mapViews.isEmpty()) {
				currentMapView = this.mapViews.get(0);
				currentMapView.destroy();
			}
			currentMapView = null;
			this.mapViews.clear();
			this.mapViews = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyMapViews();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Editor editor = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit();
		for (MapView currentMapView : this.mapViews) {
			currentMapView.onPause();

			editor.clear();
			if (currentMapView.hasValidCenter()) {
				if (!currentMapView.getMapViewMode().requiresInternetConnection()
						&& currentMapView.hasValidMapFile()) {
					// save the map file
					editor.putString("mapFile", currentMapView.getMapFile());
				}
				// save the map position and zoom level
				GeoPoint mapCenter = currentMapView.getMapCenter();
				editor.putInt("latitude", mapCenter.getLatitudeE6());
				editor.putInt("longitude", mapCenter.getLongitudeE6());
				editor.putInt("zoomLevel", currentMapView.getZoomLevel());
			}
			editor.commit();
		}

		if (isFinishing()) {
			destroyMapViews();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		for (MapView currentMapView : this.mapViews) {
			currentMapView.onResume();
		}
	}

	/**
	 * Returns a unique MapView ID on each call.
	 * 
	 * @return the new MapView ID.
	 */
	final int getMapViewId() {
		return ++this.lastMapViewId;
	}

	/**
	 * This method is called once by each MapView during its setup process.
	 * 
	 * @param mapView
	 *            the calling MapView.
	 */
	final void registerMapView(MapView mapView) {
		if (this.mapViews != null) {
			this.mapViews.add(mapView);

			SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
			// restore the position
			if (preferences.contains("latitude") && preferences.contains("longitude")
					&& preferences.contains("zoomLevel")) {
				if (!mapView.getMapViewMode().requiresInternetConnection()
						&& preferences.contains("mapFile")) {
					// get and set the map file
					mapView.setMapFileFromPreferences(preferences.getString("mapFile", null));
				}

				// get and set the map position and zoom level
				GeoPoint defaultStartPoint = mapView.getDefaultStartPoint();
				mapView.setCenterAndZoom(new GeoPoint(preferences.getInt("latitude",
						defaultStartPoint.getLatitudeE6()), preferences.getInt("longitude",
						defaultStartPoint.getLongitudeE6())), (byte) preferences.getInt(
						"zoomLevel", mapView.getDefaultZoomLevel()));
			}
		}
	}

	/**
	 * This method is called once by each MapView when it gets destroyed.
	 * 
	 * @param mapView
	 *            the calling MapView.
	 */
	final void unregisterMapView(MapView mapView) {
		if (this.mapViews != null) {
			this.mapViews.remove(mapView);
		}
	}
}
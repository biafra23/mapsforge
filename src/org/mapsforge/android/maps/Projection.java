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

/**
 * A Projection translates between the pixel coordinate system on the screen and geographical
 * points on the earth. To retrieve the currently used Projection for a given MapView, call the
 * {@link MapView#getProjection()} method.
 */
public interface Projection {
	/**
	 * Translates the given screen coordinates to a GeoPoint.
	 * 
	 * @param x
	 *            the pixel x coordinate on the screen.
	 * @param y
	 *            the pixel y coordinate on the screen.
	 * @return a new GeoPoint which is relative to the top-left of the MapView.
	 */
	GeoPoint fromPixels(int x, int y);

	/**
	 * Translates the given GeoPoint to relative pixel coordinates on the screen.
	 * 
	 * @param in
	 *            the geographical point to convert.
	 * @param out
	 *            an already existing object to use for the output. If this parameter is null, a
	 *            new Point object will be created and returned.
	 * @return a Point which is relative to the top-left of the MapView.
	 */
	Point toPixels(GeoPoint in, Point out);

	/**
	 * Translates the given GeoPoint to absolute pixel coordinates on the world map.
	 * 
	 * @param in
	 *            the geographical point to convert.
	 * @param out
	 *            an already existing object to use for the output. If this parameter is null, a
	 *            new Point object will be created and returned.
	 * @param zoom
	 *            the zoom level at which the point should be converted.
	 * @return a Point which is relative to the top-left of the world map.
	 */
	Point toPoint(GeoPoint in, Point out, byte zoom);
}
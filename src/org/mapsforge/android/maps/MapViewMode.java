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

/**
 * The MapViewMode enumeration lists all possible {@link MapView} operating modes. To check if a
 * MapViewMode requires an Internet connection, use the {@link #requiresInternetConnection()}
 * method.
 */
public enum MapViewMode {
	/**
	 * Map tiles are rendered using the <code>android.graphics</code> package (Skia library).
	 */
	CANVAS_RENDERER,

	/**
	 * Map tiles are downloaded from the Mapnik server. Requires an Internet connection.
	 */
	MAPNIK_TILE_DOWNLOAD,

	/**
	 * Map tiles are downloaded from the OpenCycleMap server. Requires an Internet connection.
	 */
	OPENCYCLEMAP_TILE_DOWNLOAD,

	/**
	 * Map tiles are rendered with OpenGL ES. <b>This mode is unstable and for testing only.</b>
	 */
	OPENGL_RENDERER,

	/**
	 * Map tiles are downloaded from the Osmarender server. Requires an Internet connection.
	 */
	OSMARENDER_TILE_DOWNLOAD;

	/**
	 * This method checks, if an Internet connection is required for the given MapViewMode.
	 * 
	 * @param mapViewMode
	 *            the MapViewMode to check.
	 * @return true if the MapViewMode requires an Internet connection, false otherwise.
	 */
	public static boolean requiresInternetConnection(MapViewMode mapViewMode) {
		switch (mapViewMode) {
			case CANVAS_RENDERER:
				return false;
			case MAPNIK_TILE_DOWNLOAD:
				return true;
			case OPENCYCLEMAP_TILE_DOWNLOAD:
				return true;
			case OPENGL_RENDERER:
				return false;
			case OSMARENDER_TILE_DOWNLOAD:
				return true;
		}
		return false;
	}

	/**
	 * Convenience method to check if this MapViewMode requires an Internet connection.
	 * 
	 * @return true if this MapViewMode requires an Internet connection, false otherwise.
	 */
	public boolean requiresInternetConnection() {
		return requiresInternetConnection(this);
	}
}
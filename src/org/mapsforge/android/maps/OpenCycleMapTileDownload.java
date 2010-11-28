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
 * A MapGenerator that downloads tiles from the OpenCycleMap server.
 */
class OpenCycleMapTileDownload extends TileDownloadMapGenerator {
	private static final String SERVER_HOST_NAME = "tile.opencyclemap.org";
	private static final String THREAD_NAME = "OpenCycleMapTileDownload";
	private static final String URL_FIRST_PART = "http://" + SERVER_HOST_NAME + "/cycle/";
	private static final byte ZOOM_MAX = 18;

	@Override
	byte getMaxZoomLevel() {
		return ZOOM_MAX;
	}

	@Override
	String getServerHostName() {
		return SERVER_HOST_NAME;
	}

	@Override
	String getThreadName() {
		return THREAD_NAME;
	}

	@Override
	void getTilePath(Tile tile, StringBuilder imagePath) {
		imagePath.append(URL_FIRST_PART);
		imagePath.append(tile.zoomLevel);
		imagePath.append("/");
		imagePath.append(tile.x);
		imagePath.append("/");
		imagePath.append(tile.y);
		imagePath.append(".png");
	}
}
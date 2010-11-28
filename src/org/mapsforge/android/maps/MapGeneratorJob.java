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
 * A container class that holds all immutable rendering parameters for a single map image
 * together with a mutable priority field, which indicates the importance of this task.
 */
class MapGeneratorJob implements Comparable<MapGeneratorJob> {
	private final int hashCode;
	private MapGeneratorJob other;

	/**
	 * Indicates if a frame should be drawn around the image for debug purposes.
	 */
	final boolean drawTileFrames;

	/**
	 * Map file used to render the map image (may be null).
	 */
	final String mapFile;

	/**
	 * MapViewMode used to render the map image.
	 */
	final MapViewMode mapViewMode;

	/**
	 * Rendering priority of this job.
	 */
	int priority;

	/**
	 * Tile that is rendered to a map image.
	 */
	final Tile tile;

	/**
	 * Creates a new job for the MapGenerator with the given parameters.
	 * 
	 * @param tile
	 *            the tile to be rendered as a map image.
	 * @param mapViewMode
	 *            the operation mode in which the map image should be generated.
	 * @param mapFile
	 *            the map file or null, if no map file is needed.
	 * @param drawTileFrames
	 *            flag to enable tile frames.
	 */
	MapGeneratorJob(Tile tile, MapViewMode mapViewMode, String mapFile, boolean drawTileFrames) {
		this.tile = tile;
		this.mapViewMode = mapViewMode;
		this.mapFile = mapFile;
		this.drawTileFrames = drawTileFrames;
		this.hashCode = calculateHashCode();
	}

	@Override
	public int compareTo(MapGeneratorJob another) {
		return this.priority - another.priority;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof MapGeneratorJob)) {
			return false;
		} else {
			this.other = (MapGeneratorJob) obj;
			if (!this.tile.equals(this.other.tile)) {
				return false;
			} else if (this.mapViewMode != this.other.mapViewMode) {
				return false;
			} else if (this.mapFile == null && this.other.mapFile != null) {
				return false;
			} else if (this.mapFile != null && !this.mapFile.equals(this.other.mapFile)) {
				return false;
			} else if (this.drawTileFrames != this.other.drawTileFrames) {
				return false;
			}
			return true;
		}
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	/**
	 * Calculates the hash value of this object.
	 * 
	 * @return the hash value of this object.
	 */
	private int calculateHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.tile == null) ? 0 : this.tile.hashCode());
		result = prime * result
				+ ((this.mapViewMode == null) ? 0 : this.mapViewMode.hashCode());
		result = prime * result + ((this.mapFile == null) ? 0 : this.mapFile.hashCode());
		result = prime * result + (this.drawTileFrames ? 1231 : 1237);
		return result;
	}
}
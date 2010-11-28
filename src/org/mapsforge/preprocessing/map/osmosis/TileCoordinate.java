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
package org.mapsforge.preprocessing.map.osmosis;

import java.util.ArrayList;
import java.util.List;

class TileCoordinate {

	private static final int MAX_ZOOMLEVEL_DISTANCE = 6;
	private final int x;
	private final int y;
	private final byte zoomlevel;

	public TileCoordinate(int x, int y, byte zoomlevel) {
		super();
		this.x = x;
		this.y = y;
		this.zoomlevel = zoomlevel;
	}

	int getX() {
		return x;
	}

	int getY() {
		return y;
	}

	byte getZoomlevel() {
		return zoomlevel;
	}

	List<TileCoordinate> translateToZoomLevel(byte zoomlevelNew) {
		List<TileCoordinate> tiles = null;
		int zoomlevelDistance = zoomlevelNew - this.zoomlevel;

		if (zoomlevelDistance > 0 && zoomlevelDistance > MAX_ZOOMLEVEL_DISTANCE)
			throw new IllegalArgumentException(
					"zoom level distance too large, allowed is a distance <= "
							+ MAX_ZOOMLEVEL_DISTANCE);

		int factor = (int) Math.pow(2, Math.abs(zoomlevelDistance));
		if (zoomlevelDistance > 0) {
			tiles = new ArrayList<TileCoordinate>(
					(int) Math.pow(4, Math.abs(zoomlevelDistance)));
			int tileUpperLeftX = this.x * factor;
			int tileUpperLeftY = this.y * factor;
			for (int i = 0; i < factor; i++) {
				for (int j = 0; j < factor; j++) {
					tiles.add(new TileCoordinate(tileUpperLeftX + j, tileUpperLeftY + i,
							zoomlevelNew));
				}
			}

		} else {
			tiles = new ArrayList<TileCoordinate>(1);
			tiles.add(new TileCoordinate(this.x / factor, this.y / factor, zoomlevelNew));
		}
		return tiles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + zoomlevel;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TileCoordinate other = (TileCoordinate) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (zoomlevel != other.zoomlevel)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TileCoordinate [x=" + x + ", y=" + y + ", zoomlevel=" + zoomlevel + "]";
	}

}

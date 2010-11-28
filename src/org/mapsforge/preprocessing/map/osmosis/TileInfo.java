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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Logger;

class TileInfo {

	private static final Logger logger =
			Logger.getLogger(TileInfo.class.getName());

	private static final byte SEA = 0x2;

	private static final byte ZOOMLEVEL12 = 0xC;

	private static final byte BITMASK = 0x3;

	// 4096 * 4096 / 4 (2 bits for each tile)
	private static final int N_BYTES = 0x400000;
	// 4096 * 4096 = number of tiles on zoom level 12
	private static final int N_BITS = 0x1000000;

	private final BitSet seaTileInfo = new BitSet(N_BITS);

	TileInfo(String strInputFile) throws IOException {
		DataInputStream dis = new DataInputStream(
				ClassLoader.getSystemResourceAsStream(strInputFile));
		byte currentByte;

		long start = System.currentTimeMillis();
		for (int i = 0; i < N_BYTES; i++) {
			currentByte = dis.readByte();
			if (((currentByte >> 6) & BITMASK) == SEA) {
				seaTileInfo.set(i * 4);
			}
			if (((currentByte >> 4) & BITMASK) == SEA) {
				seaTileInfo.set(i * 4 + 1);
			}
			if (((currentByte >> 2) & BITMASK) == SEA) {
				seaTileInfo.set(i * 4 + 2);
			}
			if ((currentByte & BITMASK) == SEA) {
				seaTileInfo.set(i * 4 + 3);
			}
		}
		logger.info("loading of tile info data took " + (System.currentTimeMillis() - start)
				+ " ms");
	}

	/**
	 * Checks if a tile is completely covered by water. <b>Important notice:</b> The method may
	 * produce false negatives on higher zoom levels than 12.
	 * 
	 * @param tc
	 *            tile given as TileCoordinate
	 * @return true if the tile is completely covered by water, false if the associated tile(s)
	 *         on zoom level 12 is(are) not completely covered by water.
	 */
	boolean isWaterTile(TileCoordinate tc) {
		List<TileCoordinate> tiles = tc.translateToZoomLevel(ZOOMLEVEL12);
		for (TileCoordinate tile : tiles) {
			if (!seaTileInfo.get(tile.getY() * 4096 + tile.getX()))
				return false;
		}
		return true;
	}

	// public static void main(String[] args) throws Exception {
	// try {
	//
	// TileInfo ti = new TileInfo(
	// "org/mapsforge/preprocessing/map/osmosis/oceantiles_12.dat");
	//
	// // TileCoordinate tc1 = new TileCoordinate(1660, 167, (byte) 12);
	// TileCoordinate tc2 = new TileCoordinate(2141, 1325, (byte) 12);
	// // TileCoordinate tc3 = new TileCoordinate(1065, 659, (byte) 11);
	// // TileCoordinate tc4 = new TileCoordinate(1065, 660, (byte) 11);
	// // TileCoordinate tc5 = new TileCoordinate(65, 40, (byte) 7);
	// TileCoordinate tc6 = new TileCoordinate(4283, 2651, (byte) 13);
	// // System.out.println(ti.isWaterTile(tc1));
	// System.out.println(ti.isWaterTile(tc2));
	// // System.out.println(ti.isWaterTile(tc3));
	// // System.out.println(ti.isWaterTile(tc4));
	// // System.out.println(ti.isWaterTile(tc5));
	// System.out.println(ti.isWaterTile(tc6));
	// System.gc();
	// System.gc();
	// System.gc();
	// System.gc();
	//
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

}

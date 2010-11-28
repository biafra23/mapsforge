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
package org.mapsforge.preprocessing.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapsforge.core.MercatorProjection;

class POIToTiles {
	/**
	 * The default maximum character size for all tags not in the map.
	 */
	private static final byte DEFAULT_MAXIMUM_CHARACTER_SIZE = 10;

	/**
	 * A map for all tags that are not rendered with the default font size.
	 */
	private static final Map<String, Byte> MAXIMUM_CHARACTER_SIZE_MAP = getCharacterSizeMap();

	/**
	 * The maximum height of the POI symbol.
	 */
	private static final byte POI_MAXIMUM_SYMBOL_HEIGHT = 42;

	/**
	 * The vertical distance between the POI position on the map and the name.
	 */
	private static final byte POI_NAME_OFFSET_VERTICAL = 20;

	/**
	 * Return a map with tags and their maximum size per character.
	 * 
	 * @return the map.
	 */
	private static Map<String, Byte> getCharacterSizeMap() {
		Map<String, Byte> fontSizeMap = new HashMap<String, Byte>();

		// all tags with font size 25
		fontSizeMap.put("place=city", Byte.valueOf((byte) 25));

		// all tags with font size 20
		fontSizeMap.put("place=island", Byte.valueOf((byte) 20));

		// all tags with font size 15
		fontSizeMap.put("place=suburb", Byte.valueOf((byte) 15));
		fontSizeMap.put("place=town", Byte.valueOf((byte) 15));
		fontSizeMap.put("place=village", Byte.valueOf((byte) 15));

		// all tags with font size 13
		fontSizeMap.put("railway=station", Byte.valueOf((byte) 13));

		// all tags with font size 12
		fontSizeMap.put("natural=peak", Byte.valueOf((byte) 12));

		// all tags with font size 11
		fontSizeMap.put("railway=halt", Byte.valueOf((byte) 11));
		fontSizeMap.put("railway=tram_stop", Byte.valueOf((byte) 11));
		fontSizeMap.put("station=light_rail", Byte.valueOf((byte) 11));
		fontSizeMap.put("station=subway", Byte.valueOf((byte) 11));

		return fontSizeMap;
	}

	/**
	 * Calculate all tiles that the given POI may intersect when rendered.
	 * 
	 * The method estimates the font size of the POI name by looking at it's tags. It then
	 * calculates the maximum bounding box of the rendered POI in consideration of the estimated
	 * font size, name length and POI symbol. A list of all tiles within this bounding box is
	 * returned.
	 * 
	 * @param pointOfInterest
	 *            the POI that should be analyzed.
	 * @param zoom
	 *            the zoom level on which all calculations should be done.
	 * @return a list containing all tiles that this POI must be assigned to.
	 */
	static List<Tile> getPOITiles(MapElementNode pointOfInterest, byte zoom) {
		// convert the POI coordinates to pixel XY coordinates
		double poiPixelX = MercatorProjection.longitudeToPixelX(
				pointOfInterest.longitude / 1000000d, zoom);
		double poiPixelY = MercatorProjection.latitudeToPixelY(
				pointOfInterest.latitude / 1000000d, zoom);

		// get the length of the POI name
		int poiNameLength;
		if (pointOfInterest.name == null) {
			poiNameLength = 0;
		} else {
			poiNameLength = pointOfInterest.name.length();
		}

		// determine the maximum character size of this POI
		int poiMaximumCharacterSize = DEFAULT_MAXIMUM_CHARACTER_SIZE;
		for (String currentTag : pointOfInterest.tags) {
			if (MAXIMUM_CHARACTER_SIZE_MAP.containsKey(currentTag)) {
				poiMaximumCharacterSize = MAXIMUM_CHARACTER_SIZE_MAP.get(currentTag);
				break;
			}
		}

		// calculate the maximum width of the rendered POI name
		int poiMaximumNameWidth = poiNameLength * poiMaximumCharacterSize;

		// calculate the maximum bounding box coordinates of the rendered POI
		double poiPixelXMinimum = poiPixelX - (poiMaximumNameWidth >> 1);
		double poiPixelXMaximum = poiPixelX + (poiMaximumNameWidth >> 1);
		double poiPixelYMinimum = poiPixelY - POI_NAME_OFFSET_VERTICAL
				- poiMaximumCharacterSize;
		double poiPixelYMaximum = poiPixelY + (POI_MAXIMUM_SYMBOL_HEIGHT >> 1);

		// calculate the tile XY numbers of the POI bounding box coordinates
		long poiTileXMinimum = MercatorProjection.pixelXToTileX(poiPixelXMinimum, zoom);
		long poiTileXMaximum = MercatorProjection.pixelXToTileX(poiPixelXMaximum, zoom);
		long poiTileYMinimum = MercatorProjection.pixelYToTileY(poiPixelYMinimum, zoom);
		long poiTileYMaximum = MercatorProjection.pixelYToTileY(poiPixelYMaximum, zoom);

		// create a list and add all tiles within the bounding box to it
		List<Tile> tileList = new ArrayList<Tile>();
		for (long tileX = poiTileXMinimum; tileX <= poiTileXMaximum; ++tileX) {
			for (long tileY = poiTileYMinimum; tileY <= poiTileYMaximum; ++tileY) {
				tileList.add(new Tile(tileX, tileY, zoom));
			}
		}

		// return the tile list
		return tileList;
	}
}
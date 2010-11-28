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

import java.util.logging.Logger;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.MercatorProjection;
import org.mapsforge.core.Rect;

abstract class BaseTileBasedDataStore implements TileBasedDataStore {

	private static final Logger logger =
			Logger.getLogger(BaseTileBasedDataStore.class.getName());

	protected final Rect boundingbox;
	protected final ZoomIntervalConfiguration zoomIntervalConfiguration;

	protected final TileCoordinate[] upperLeftTiles;

	// protected final int[] tileOffsetsHorizontal;
	// protected final int[] tileOffsetsVertical;

	public BaseTileBasedDataStore(
			double minLat, double maxLat,
			double minLon, double maxLon,
			ZoomIntervalConfiguration zoomIntervalConfiguration) {
		this(new Rect(minLon, maxLon, minLat, maxLat), zoomIntervalConfiguration);

	}

	public BaseTileBasedDataStore(Rect bbox, ZoomIntervalConfiguration zoomIntervalConfiguration) {
		super();
		this.boundingbox = bbox;
		this.zoomIntervalConfiguration = zoomIntervalConfiguration;
		this.upperLeftTiles = new TileCoordinate[zoomIntervalConfiguration
												.getNumberOfZoomIntervals()];
		long cumulatedNumberOfTiles = cumulatedNumberOfTiles();
		if (cumulatedNumberOfTiles > MAX_TILES_SUPPORTED) {
			throw new IllegalArgumentException("Bounding box of this area is too large. Need "
					+ cumulatedNumberOfTiles + " number of tiles, but only "
					+ MAX_TILES_SUPPORTED + " are supported.");
		}

		// compute horizontal and vertical tile coordinate offsets for all
		// base zoom levels
		for (int i = 0; i < zoomIntervalConfiguration.getNumberOfZoomIntervals(); i++) {
			this.upperLeftTiles[i] = new TileCoordinate(
													(int) MercatorProjection.longitudeToTileX(
															GeoCoordinate
																	.intToDouble(boundingbox.minLongitudeE6),
															zoomIntervalConfiguration
																	.getBaseZoom(i)),
													(int) MercatorProjection.latitudeToTileY(
																	GeoCoordinate
																			.intToDouble(boundingbox.maxLatitudeE6),
																	zoomIntervalConfiguration
																			.getBaseZoom(i)),
																	zoomIntervalConfiguration
																			.getBaseZoom(i)
													);
		}
	}

	@Override
	public Rect getBoundingBox() {
		return boundingbox;
	}

	@Override
	public ZoomIntervalConfiguration getZoomIntervalConfiguration() {
		return zoomIntervalConfiguration;
	}

	@Override
	public TileCoordinate getUpperLeft(int zoomIntervalIndex) {
		return upperLeftTiles[zoomIntervalIndex];
	}

	@Override
	public long cumulatedNumberOfTiles() {
		long cumulated = 0;
		for (int i = 0; i < zoomIntervalConfiguration.getNumberOfZoomIntervals(); i++) {
			cumulated += (computeNumberOfHorizontalTiles(i) * computeNumberOfVerticalTiles(i));
		}
		return cumulated;
	}

	protected int computeNumberOfHorizontalTiles(int zoomIntervalIndex) {
		long tileCoordinateLeft = MercatorProjection.longitudeToTileX(
				GeoCoordinate.intToDouble(boundingbox.getMinLongitudeE6()),
				zoomIntervalConfiguration.getBaseZoom(zoomIntervalIndex));

		long tileCoordinateRight = MercatorProjection.longitudeToTileX(
				GeoCoordinate.intToDouble(boundingbox.getMaxLongitudeE6()),
				zoomIntervalConfiguration.getBaseZoom(zoomIntervalIndex));

		assert tileCoordinateLeft <= tileCoordinateRight;
		assert tileCoordinateLeft - tileCoordinateRight + 1 < Integer.MAX_VALUE;

		logger.finest("basezoom: " + zoomIntervalConfiguration.getBaseZoom(zoomIntervalIndex)
				+ "\t+n_horizontal: " + (tileCoordinateRight - tileCoordinateLeft + 1));

		return (int) (tileCoordinateRight - tileCoordinateLeft + 1);

	}

	protected int computeNumberOfVerticalTiles(int zoomIntervalIndex) {
		long tileCoordinateBottom = MercatorProjection.latitudeToTileY(
				GeoCoordinate.intToDouble(boundingbox.getMinLatitudeE6()),
				zoomIntervalConfiguration.getBaseZoom(zoomIntervalIndex));

		long tileCoordinateTop = MercatorProjection.latitudeToTileY(
				GeoCoordinate.intToDouble(boundingbox.getMaxLatitudeE6()),
				zoomIntervalConfiguration.getBaseZoom(zoomIntervalIndex));

		assert tileCoordinateBottom >= tileCoordinateTop;
		assert tileCoordinateBottom - tileCoordinateTop + 1 <= Integer.MAX_VALUE;

		logger.finest("basezoom: " + zoomIntervalConfiguration.getBaseZoom(zoomIntervalIndex)
				+ "\t+n_vertical: " + (tileCoordinateBottom - tileCoordinateTop + 1));

		return (int) (tileCoordinateBottom - tileCoordinateTop + 1);
	}
}

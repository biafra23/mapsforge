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
 * A performance optimized implementation of the spherical Mercator projection.
 */
class MercatorProjection implements Projection {
	/**
	 * Calculates the distance on the ground that is represented by a single pixel on the map.
	 * 
	 * @param latitude
	 *            the latitude coordinate at which the resolution should be calculated.
	 * @param zoom
	 *            the zoom level at which the resolution should be calculated.
	 * @return the ground resolution at the given latitude and zoom level.
	 */
	static double calculateGroundResolution(double latitude, byte zoom) {
		return Math.cos(latitude * Math.PI / 180) * 40075016.686
				/ ((long) Tile.TILE_SIZE << zoom);
	}

	/**
	 * Converts a latitude coordinate (in degrees) to a pixel Y coordinate at a certain zoom
	 * level.
	 * 
	 * @param latitude
	 *            the latitude coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the pixel Y coordinate of the latitude value.
	 */
	static double latitudeToPixelY(double latitude, byte zoom) {
		double sinLatitude = Math.sin(latitude * Math.PI / 180);
		return ((0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI)) * ((long) Tile.TILE_SIZE << zoom));
	}

	/**
	 * Converts a latitude coordinate (in degrees) to a tile Y number at a certain zoom level.
	 * 
	 * @param latitude
	 *            the latitude coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the tile Y number of the latitude value.
	 */
	static long latitudeToTileY(double latitude, byte zoom) {
		return pixelYToTileY(latitudeToPixelY(latitude, zoom), zoom);
	}

	/**
	 * Converts a longitude coordinate (in degrees) to a pixel X coordinate at a certain zoom
	 * level.
	 * 
	 * @param longitude
	 *            the longitude coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the pixel X coordinate of the longitude value.
	 */
	static double longitudeToPixelX(double longitude, byte zoom) {
		return ((longitude + 180) / 360 * ((long) Tile.TILE_SIZE << zoom));
	}

	/**
	 * Converts a longitude coordinate (in degrees) to the tile X number at a certain zoom
	 * level.
	 * 
	 * @param longitude
	 *            the longitude coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the tile X number of the longitude value.
	 */
	static long longitudeToTileX(double longitude, byte zoom) {
		return pixelXToTileX(longitudeToPixelX(longitude, zoom), zoom);
	}

	/**
	 * Converts a pixel X coordinate at a certain zoom level to a longitude coordinate.
	 * 
	 * @param pixelX
	 *            the pixel X coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the longitude value of the pixel X coordinate.
	 */
	static double pixelXToLongitude(double pixelX, byte zoom) {
		return 360 * ((pixelX / ((long) Tile.TILE_SIZE << zoom)) - 0.5);
	}

	/**
	 * Converts a pixel X coordinate to the tile X number.
	 * 
	 * @param pixelX
	 *            the pixel X coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the tile X number.
	 */
	static long pixelXToTileX(double pixelX, byte zoom) {
		return (long) Math.min(Math.max((pixelX / Tile.TILE_SIZE), 0), Math.pow(2, zoom) - 1);
	}

	/**
	 * Converts a pixel Y coordinate at a certain zoom level to a latitude coordinate.
	 * 
	 * @param pixelY
	 *            the pixel Y coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the latitude value of the pixel Y coordinate.
	 */
	static double pixelYToLatitude(double pixelY, byte zoom) {
		double y = 0.5 - (pixelY / ((long) Tile.TILE_SIZE << zoom));
		return 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
	}

	/**
	 * Converts a pixel Y coordinate to the tile Y number.
	 * 
	 * @param pixelY
	 *            the pixel Y coordinate that should be converted.
	 * @param zoom
	 *            the zoom level at which the coordinate should be converted.
	 * @return the tile Y number.
	 */
	static long pixelYToTileY(double pixelY, byte zoom) {
		return (long) Math.min(Math.max((pixelY / Tile.TILE_SIZE), 0), Math.pow(2, zoom) - 1);
	}

	/**
	 * Converts a tile X number at a certain zoom level to a longitude coordinate.
	 * 
	 * @param tileX
	 *            the tile X number that should be converted.
	 * @param zoom
	 *            the zoom level at which the number should be converted.
	 * @return the longitude value of the tile X number.
	 */
	static double tileXToLongitude(long tileX, byte zoom) {
		return pixelXToLongitude(tileX * Tile.TILE_SIZE, zoom);
	}

	/**
	 * Converts a tile Y number at a certain zoom level to a latitude coordinate.
	 * 
	 * @param tileY
	 *            the tile Y number that should be converted.
	 * @param zoom
	 *            the zoom level at which the number should be converted.
	 * @return the latitude value of the tile Y number.
	 */
	static double tileYToLatitude(long tileY, byte zoom) {
		return pixelYToLatitude(tileY * Tile.TILE_SIZE, zoom);
	}

	private GeoPoint mapCenter1;
	private GeoPoint mapCenter2;
	private final MapView mapView;
	private byte mapZoomLevel1;
	private byte mapZoomLevel2;
	private double pixelX1;
	private double pixelX2;
	private double pixelY1;
	private double pixelY2;
	private Point returnPoint2;
	private Point returnPoint3;

	/**
	 * Constructs a new MercatorProjection that uses the parameters of the given MapView.
	 * 
	 * @param mapView
	 *            the MapView on which this instance should operate.
	 */
	MercatorProjection(MapView mapView) {
		this.mapView = mapView;
	}

	@Override
	public GeoPoint fromPixels(int x, int y) {
		// save the current position and zoom level of the map
		this.mapCenter1 = this.mapView.getMapCenter();
		this.mapZoomLevel1 = this.mapView.getZoomLevel();

		// calculate the pixel coordinates of the top left corner
		this.pixelX1 = longitudeToPixelX(this.mapCenter1.getLongitude(), this.mapZoomLevel1)
				- (this.mapView.getWidth() >> 1);
		this.pixelY1 = latitudeToPixelY(this.mapCenter1.getLatitude(), this.mapZoomLevel1)
				- (this.mapView.getHeight() >> 1);

		// convert the pixel coordinates to a GeoPoint
		return new GeoPoint(pixelYToLatitude(this.pixelY1 + y, this.mapZoomLevel1),
				pixelXToLongitude(this.pixelX1 + x, this.mapZoomLevel1));
	}

	@Override
	public Point toPixels(GeoPoint in, Point out) {
		// save the current position and zoom level of the map
		this.mapCenter2 = this.mapView.getMapCenter();
		this.mapZoomLevel2 = this.mapView.getZoomLevel();

		// calculate the pixel coordinates of the top left corner
		this.pixelX2 = longitudeToPixelX(this.mapCenter2.getLongitude(), this.mapZoomLevel2)
				- (this.mapView.getWidth() >> 1);
		this.pixelY2 = latitudeToPixelY(this.mapCenter2.getLatitude(), this.mapZoomLevel2)
				- (this.mapView.getHeight() >> 1);

		if (out == null) {
			// create a new point object
			this.returnPoint2 = new Point();
		} else {
			// reuse the existing point object
			this.returnPoint2 = out;
		}

		this.returnPoint2.x = (int) (longitudeToPixelX(in.getLongitude(), this.mapZoomLevel2) - this.pixelX2);
		this.returnPoint2.y = (int) (latitudeToPixelY(in.getLatitude(), this.mapZoomLevel2) - this.pixelY2);
		return this.returnPoint2;
	}

	@Override
	public Point toPoint(GeoPoint in, Point out, byte zoom) {
		if (out == null) {
			// create a new point object
			this.returnPoint3 = new Point();
		} else {
			// reuse the existing point object
			this.returnPoint3 = out;
		}
		this.returnPoint3.x = (int) longitudeToPixelX(in.getLongitude(), zoom);
		this.returnPoint3.y = (int) latitudeToPixelY(in.getLatitude(), zoom);
		return this.returnPoint3;
	}
}
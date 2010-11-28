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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.mapsforge.core.MercatorProjection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

class Utils {
	private static GeometryFactory geoFac = new GeometryFactory();

	// values for the tile bit mask which indicate on which sub tile a way has to be rendered
	private static final int[] tileBitMaskValues = new int[] { 32768, 16384, 2048, 1024, 8192,
			4096, 512, 256, 128, 64, 8, 4, 32, 16, 2, 1 };

	// list of values for extending a tile on the base zoom level
	private static final double[] tileEpsilon = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0.0006, 0.00025, 0.00013, 0.00006 };

	// list of values for extending a sub tile
	private static final double[] subTileEpsilon = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0.0015, 0.0001, 0.00085, 0.00065 };

	/**
	 * Get the bounding box of a tile.
	 * 
	 * @param tileX
	 *            the X number of the tile.
	 * @param tileY
	 *            the Y number of the tile.
	 * @param zoom
	 *            the zoom level at which the bounding box should be calculated.
	 * @return a geometry object which represents the bounding box of this tile
	 */
	static LinearRing getBoundingBox(long tileX, long tileY, byte zoom) {
		// project the tile coordinates to latitude and longitude
		double minLat = MercatorProjection.tileYToLatitude(tileY + 1, zoom);
		double maxLat = MercatorProjection.tileYToLatitude(tileY, zoom);
		double minLon = MercatorProjection.tileXToLongitude(tileX, zoom);
		double maxLon = MercatorProjection.tileXToLongitude(tileX + 1, zoom);

		// create a geometry object which represents the bounding box
		return geoFac.createLinearRing(new Coordinate[] { new Coordinate(maxLat, minLon),
				new Coordinate(minLat, minLon), new Coordinate(minLat, maxLon),
				new Coordinate(maxLat, maxLon), new Coordinate(maxLat, minLon) });
	}

	/**
	 * Get the bounding box of a tile.
	 * 
	 * @param tileX
	 *            the X number of the tile.
	 * @param tileY
	 *            the Y number of the tile.
	 * @param zoom
	 *            the zoom level at which the bounding box should be calculated.
	 * @param enlarged
	 *            indicates if a enlarged bounding box should be created
	 * @return a geometry object which represents the bounding box of this tile
	 */
	static LinearRing getBoundingBox(long tileX, long tileY, byte zoom, boolean enlarged) {
		double maxLat = MercatorProjection.tileYToLatitude(tileY, zoom);
		double minLat = MercatorProjection.tileYToLatitude(tileY + 1, zoom);
		double minLon = MercatorProjection.tileXToLongitude(tileX, zoom);
		double maxLon = MercatorProjection.tileXToLongitude(tileX + 1, zoom);

		if (enlarged) {
			// if the bounding box should be bigger, add a certain epsilon to the coordinates
			Coordinate[] c = new Coordinate[] {
					new Coordinate(maxLat + tileEpsilon[zoom - 1], minLon
							- tileEpsilon[zoom - 1]),
					new Coordinate(minLat - tileEpsilon[zoom - 1], minLon
							- tileEpsilon[zoom - 1]),
					new Coordinate(minLat - tileEpsilon[zoom - 1], maxLon
							+ tileEpsilon[zoom - 1]),
					new Coordinate(maxLat + tileEpsilon[zoom - 1], maxLon
							+ tileEpsilon[zoom - 1]),
					new Coordinate(maxLat + tileEpsilon[zoom - 1], minLon
							- tileEpsilon[zoom - 1]) };
			// create a geometry object which represents the bounding box
			return geoFac.createLinearRing(c);
		}
		// create a geometry object which represents the bounding box
		return geoFac.createLinearRing(new Coordinate[] { new Coordinate(maxLat, minLon),
				new Coordinate(minLat, minLon), new Coordinate(minLat, maxLon),
				new Coordinate(maxLat, maxLon), new Coordinate(maxLat, minLon) });
	}

	/**
	 * Get the bounding box of a sub tile.
	 * 
	 * @param tileX
	 *            the X number of the tile.
	 * @param tileY
	 *            the Y number of the tile.
	 * @param zoom
	 *            the zoom level at which the bounding box should be calculated.
	 * @param enlarged
	 *            indicates if a enlarged bounding box should be created
	 * @return a geometry object which represents the bounding box of this tile
	 */
	static LinearRing getSubTileBoundingBox(long tileX, long tileY, byte zoom, boolean enlarged) {
		double maxLat = MercatorProjection.tileYToLatitude(tileY, zoom);
		double minLat = MercatorProjection.tileYToLatitude(tileY + 1, zoom);
		double minLon = MercatorProjection.tileXToLongitude(tileX, zoom);
		double maxLon = MercatorProjection.tileXToLongitude(tileX + 1, zoom);

		if (enlarged) {
			Coordinate[] c = new Coordinate[] {
					new Coordinate(maxLat + subTileEpsilon[zoom - 1], minLon
							- subTileEpsilon[zoom - 1]),
					new Coordinate(minLat - subTileEpsilon[zoom - 1], minLon
							- subTileEpsilon[zoom - 1]),
					new Coordinate(minLat - subTileEpsilon[zoom - 1], maxLon
							+ subTileEpsilon[zoom - 1]),
					new Coordinate(maxLat + subTileEpsilon[zoom - 1], maxLon
							+ subTileEpsilon[zoom - 1]),
					new Coordinate(maxLat + subTileEpsilon[zoom - 1], minLon
							- subTileEpsilon[zoom - 1]) };
			return geoFac.createLinearRing(c);
		}
		return geoFac.createLinearRing(new Coordinate[] { new Coordinate(maxLat, minLon),
				new Coordinate(minLat, minLon), new Coordinate(minLat, maxLon),
				new Coordinate(maxLat, maxLon), new Coordinate(maxLat, minLon) });
	}

	/**
	 * Get the coordinates of the bounding box of a tile.
	 * 
	 * @param tileX
	 *            the X number of the tile.
	 * @param tileY
	 *            the Y number of the tile.
	 * @param zoom
	 *            the zoom level at which the bounding box should be calculated.
	 * @return an array containing the coordinates of the bounding box of this tile
	 */
	static double[] getBoundingBoxArray(long tileX, long tileY, byte zoom) {
		double[] result = new double[4];

		double minLat = MercatorProjection.tileYToLatitude(tileY, zoom);
		double maxLat = MercatorProjection.tileYToLatitude(tileY + 1, zoom);
		double minLon = MercatorProjection.tileXToLongitude(tileX, zoom);
		double maxLon = MercatorProjection.tileXToLongitude(tileX + 1, zoom);

		result[0] = minLat;
		result[1] = maxLat;
		result[2] = minLon;
		result[3] = maxLon;

		return result;
	}

	/**
	 * Returns all sub tiles of a given tile on a configurable higher zoom level. The resulting
	 * list is computed by recursively calling getSubtiles() on all four sub tiles of the given
	 * tile. On each recursive call the second parameter is decremented by one until zero is
	 * reached.
	 * 
	 * @param currentTile
	 *            the starting tile for the computation of the sub tiles.
	 * @param zoomLevelDistance
	 *            how many zoom levels higher the sub tiles should be.
	 * @return a list containing all sub tiles on the higher zoom level.
	 * @throws InvalidParameterException
	 *             if {@code zoomLevelDistance} is less than zero.
	 */
	static List<Tile> getSubtiles(Tile currentTile, byte zoomLevelDistance) {
		// check for valid zoomLevelDistance parameter
		if (zoomLevelDistance < 0) {
			throw new InvalidParameterException();
		}

		// create the list of tiles that will be returned
		List<Tile> childrenTiles = new ArrayList<Tile>();

		if (zoomLevelDistance == 0) {
			// add only the current tile to the list
			childrenTiles.add(currentTile);
		} else {
			// add all subtiles of the upper left subtile recursively
			childrenTiles.addAll(getSubtiles(new Tile(currentTile.x * 2, currentTile.y * 2,
					(byte) (currentTile.zoomLevel + 1)), (byte) (zoomLevelDistance - 1)));

			// add all subtiles of the upper right subtile recursively
			childrenTiles.addAll(getSubtiles(new Tile(currentTile.x * 2 + 1, currentTile.y * 2,
					(byte) (currentTile.zoomLevel + 1)), (byte) (zoomLevelDistance - 1)));

			// add all subtiles of the lower left subtile recursively
			childrenTiles.addAll(getSubtiles(new Tile(currentTile.x * 2, currentTile.y * 2 + 1,
					(byte) (currentTile.zoomLevel + 1)), (byte) (zoomLevelDistance - 1)));

			// add all subtiles of the lower right subtile recursively
			childrenTiles.addAll(getSubtiles(new Tile(currentTile.x * 2 + 1,
					currentTile.y * 2 + 1, (byte) (currentTile.zoomLevel + 1)),
					(byte) (zoomLevelDistance - 1)));
		}

		// return the tile list
		return childrenTiles;
	}

	/**
	 * Calculate all tiles on a certain zoom level which contain a given way.
	 * 
	 * @param geoWay
	 *            the way
	 * @param wayType
	 *            the type of the way, 1 = simple way, 2 = closed way/area, 3 = part of a
	 *            multipolygon
	 * @param zoom
	 *            the zoom level at which the tiles should be calculated
	 * @return a set of tiles which contain the whole way or a part of the way
	 * 
	 */
	static Set<Tile> wayToTilesWay(Geometry geoWay, int wayType, byte zoom) {
		Set<Tile> wayTiles = new HashSet<Tile>();

		Tile parentTile = null;

		int minTileX;
		int minTileY;
		int maxTileX;
		int maxTileY;
		Coordinate min;
		Coordinate max;

		if (geoWay == null) {
			return wayTiles;
		}

		try {
			// get the bounding box of the current way
			Geometry boundingBox = geoWay.getEnvelope();
			Coordinate[] bBoxCoords = boundingBox.getCoordinates();

			if (bBoxCoords.length == 1) {
				return wayTiles;
			}

			if (bBoxCoords.length == 2) {
				int compare = bBoxCoords[0].compareTo(bBoxCoords[1]);

				if (compare == 1) {
					max = bBoxCoords[0];
					min = bBoxCoords[1];
				} else if (compare == 0) {
					max = bBoxCoords[0];
					min = bBoxCoords[0];
				} else {
					max = bBoxCoords[1];
					min = bBoxCoords[0];
				}

			} else {
				min = bBoxCoords[3];
				max = bBoxCoords[1];
			}

			// get the minimal and maximal tile coordinates for the tiles that cover the corners
			// of the bounding box
			minTileX = (int) MercatorProjection.longitudeToTileX(min.y, zoom);
			minTileY = (int) MercatorProjection.latitudeToTileY(min.x, zoom);

			maxTileX = (int) MercatorProjection.longitudeToTileX(max.y, zoom);
			maxTileY = (int) MercatorProjection.latitudeToTileY(max.x, zoom);

			// calculate the tile coordinates and the corresponding bounding boxes for the tiles
			// that cover the bounding box
			Map<Coordinate, Geometry> tiles = new HashMap<Coordinate, Geometry>();
			for (long i = minTileX; i <= maxTileX; i++) {
				for (long j = minTileY; j <= maxTileY; j++) {
					tiles.put(new Coordinate(i, j), geoFac.createPolygon(Utils.getBoundingBox(
							i, j, zoom), null));
				}
			}

			// check for every tile in the set if the tile is related to the given way
			Set<Entry<Coordinate, Geometry>> set = tiles.entrySet();
			for (Entry<Coordinate, Geometry> e : set) {
				Coordinate c = e.getKey();
				Geometry currentTile = e.getValue();
				// the current way is open
				if (wayType == 1) {
					if (geoWay.crosses(currentTile) || geoWay.within(currentTile)
							|| geoWay.intersects(currentTile) || currentTile.contains(geoWay)
							|| currentTile.contains(geoWay) || currentTile.intersects(geoWay)) {
						// if the tile is related to the way add the tile to the result list
						parentTile = new Tile((long) c.x, (long) c.y, zoom);
						wayTiles.add(parentTile);
					}
				}
				// the current way is closed or part of a multipolygon
				if (wayType != 1) {
					if (currentTile.within(geoWay) || currentTile.intersects(geoWay)
							|| currentTile.contains(geoWay) || geoWay.contains(currentTile)
							|| geoWay.crosses(currentTile)) {
						// if the tile is related to the way add the tile to the result list
						parentTile = new Tile((long) c.x, (long) c.y, zoom);
						wayTiles.add(parentTile);
					}
				}
			}
			return wayTiles;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return wayTiles;
		}
	}

	/**
	 * Creates a geometry which represents a way.
	 * 
	 * @param way
	 *            element that holds all information of a way
	 * @param wayNodes
	 *            the coordinates of the way nodes
	 * @return a geometry which represents a certain way
	 */
	static Geometry createWay(MapElementWay way, Coordinate[] wayNodes) {
		Geometry geoWay;

		if (wayNodes.length < 2) {
			return null;
		}

		if (way.wayType == 1) {
			// open way
			geoWay = geoFac.createLineString(wayNodes);
		} else {
			if (wayNodes.length < 4) {
				// a closed way has to have at least four way nodes
				return null;
			}
			geoWay = geoFac.createPolygon(geoFac.createLinearRing(wayNodes), null);
			way.convexness = (int) ((geoWay.getArea() / geoWay.convexHull().getArea()) * 100);
		}
		return geoWay;
	}

	/**
	 * Calculates for each way that is related to a given tile a tile bit mask. The bit mask
	 * determines for which tiles on zoom level initialTile.zoom+2 the way is needed for
	 * rendering.
	 * 
	 * @param geoWay
	 *            a certain way
	 * @param wayTiles
	 *            all tiles to which the way is related
	 * @param wayType
	 *            the type of the way, 1 = simple way, 2 = closed way/area, 3 = part of a
	 *            multipolygon
	 * @return a map containing tiles and the calculated tile bit mask
	 */
	static Map<Tile, Short> getTileBitMask(Geometry geoWay, Set<Tile> wayTiles, short wayType) {
		Map<Tile, Short> result = new HashMap<Tile, Short>();
		short tileCounter;
		short bitMask;
		short tmp = 0;

		Geometry subTile;

		if (geoWay == null) {
			return result;
		}

		// for every tile to which the current way is related
		for (Tile p : wayTiles) {
			// get all sixteen sub tiles
			List<Tile> currentSubTiles = getSubtiles(new Tile(p.x, p.y, p.zoomLevel), (byte) 2);
			tileCounter = 0;
			bitMask = 0;
			result.put(p, (short) 0);

			// for every sub tile
			for (Tile csb : currentSubTiles) {
				// get the bounding box of the sub tile
				subTile = geoFac.createPolygon(Utils.getSubTileBoundingBox(csb.x, csb.y,
						(byte) (p.zoomLevel + 2), true), null);

				// check the relation between the current way and the sub tile
				// the way is open
				if (wayType == 1) {
					if (geoWay.crosses(subTile) || geoWay.within(subTile)
							|| geoWay.intersects(subTile)) {
						// if the way is related to the sub tile add the corresponding value to
						// the bit mask
						tmp = result.get(p);
						tmp |= tileBitMaskValues[tileCounter];
						result.put(p, tmp);
					}
				}
				// the way is closed or part of a multipolygon
				if (wayType != 1) {
					if (subTile.within(geoWay) || subTile.intersects(geoWay)
							|| subTile.contains(geoWay)) {
						// if the way is related to the sub tile add the corresponding value to
						// the bit mask
						bitMask = bitMask |= tileBitMaskValues[tileCounter];
						result.put(p, bitMask);
					}
				}
				tileCounter++;
			}
		}
		return result;
	}

	/**
	 * Clips a given polygon to a clipping edge (here: edge of a tile).
	 * 
	 * (edges go from A to B)
	 * 
	 * @param tileA
	 *            first point of the clipping edge
	 * @param tileB
	 *            second point of the clipping edge
	 * @param polygonNodes
	 *            list of polygon nodes
	 * @return list of nodes of the clipped polygon
	 */
	static ArrayList<Coordinate> clipPolygonToTile(Coordinate tileA, Coordinate tileB,
			ArrayList<Coordinate> polygonNodes) {

		ArrayList<Coordinate> output = new ArrayList<Coordinate>();

		Coordinate lastNode;
		Coordinate currentNode;

		Coordinate intersectionPoint;

		if (polygonNodes.size() == 0) {
			return output;
		} else if (!polygonNodes.get(0).equals(polygonNodes.get(polygonNodes.size() - 1))) {
			return polygonNodes;
		}

		// get the last polygon node
		lastNode = polygonNodes.get(polygonNodes.size() - 1);

		// for every node of the polygon
		for (int j = 0; j < polygonNodes.size(); j++) {
			// get the current node
			currentNode = polygonNodes.get(j);

			if (nodeInsideTile(currentNode, tileA, tileB)) {
				// the current node lies inside the clipping region
				if (nodeInsideTile(lastNode, tileA, tileB)) {
					// the last node lies inside the clipping region
					// add the current node to the output list
					output.add(currentNode);

				} else {
					// the last node lies outside the clipping region

					// calculate the intersection point of the edges (lastNode, currentNode)
					// and (tileA,tileB)
					intersectionPoint = intersection(lastNode, currentNode, tileA, tileB);

					// add the intersection point to the output list
					output.add(intersectionPoint);
					// add also the current node to the output list
					output.add(currentNode);
				}
			} else {
				// the current node lies outside the clipping region
				if (nodeInsideTile(lastNode, tileA, tileB)) {
					// the last node lies inside the clipping region

					// calculate the intersection point of the edges (lastNode,currentNode)
					// and (tileA,tileB)
					intersectionPoint = intersection(lastNode, currentNode, tileA, tileB);
					// add the intersection point to the output list
					output.add(intersectionPoint);
				}
				// if lastNode and currentNode are both outside the clipping region, no node
				// is added to the output list
			}
			// set lastNode to currentNode
			lastNode = currentNode;
		}

		return output;
	}

	/**
	 * calculates whether a given point lies inside the clipping region (i.e. on the left side
	 * of the clipping edge)
	 * 
	 * @param node
	 *            point for which the position should be tested
	 * @param tileA
	 *            first point of the clipping edge
	 * @param tileB
	 *            second point of the clipping edge
	 * @return true if the given point lies in the clipping region, else false
	 */
	static boolean nodeInsideTile(Coordinate node, Coordinate tileA, Coordinate tileB) {
		if (tileB.x > tileA.x) {
			if (node.y <= tileA.y) {
				return true;
			}
		}
		if (tileB.x < tileA.x) {
			if (node.y >= tileA.y) {
				return true;
			}
		}
		if (tileB.y > tileA.y) {
			if (node.x >= tileB.x) {
				return true;
			}
		}
		if (tileB.y < tileA.y) {
			if (node.x <= tileB.x) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Calculates the intersection point of two edges (here: an edge of the polygon and the
	 * current clipping edge
	 * 
	 * (the edges go from A to B)
	 * 
	 * @param polygonA
	 *            first point of the polygon edge
	 * @param polygonB
	 *            second point of the polygon edge
	 * @param tileA
	 *            first point of the clipping edge
	 * @param tileB
	 *            second point of the clipping edge
	 * @return the coordinates of the intersection point
	 * 
	 */
	static Coordinate intersection(Coordinate polygonA, Coordinate polygonB, Coordinate tileA,
			Coordinate tileB) {
		Coordinate intersectionPoint = new Coordinate();

		if (tileA.x == tileB.x) {
			// tile edge is vertical
			// this means that the x coordinate of the intersection point is equal to the values
			// of the x coordinate of the tile edge vertices

			intersectionPoint.x = tileA.x;
			intersectionPoint.y = polygonA.y + (tileA.x - polygonA.x)
					* (polygonB.y - polygonA.y) / (polygonB.x - polygonA.x);
		} else {
			// tile edge is horizontal
			// this means that the y coordinate of the intersection point is equal to the values
			// of the y coordinates of the tile edge vertices

			intersectionPoint.x = polygonA.x + (tileA.y - polygonA.y)
					* (polygonB.x - polygonA.x) / (polygonB.y - polygonA.y);
			intersectionPoint.y = tileA.y;
		}
		return intersectionPoint;
	}

	/**
	 * This method filters the way nodes of a way and returns only those way nodes which are not
	 * projected to the same pixel at the given zoom level. The first and the last way node of a
	 * way are always added to the result list.
	 * 
	 * @param wayNodes
	 *            list of way nodes
	 * @param zoom
	 *            zoom level for which the calculations are made
	 * @return a list of way nodes
	 */
	static ArrayList<Coordinate> filterWaynodesOnSamePixel(ArrayList<Coordinate> wayNodes,
			byte zoom) {
		// zoom: maximal zoom level concerning a certain base zoom level
		ArrayList<Coordinate> result = new ArrayList<Coordinate>();
		int wayLength;
		float delta = 2;
		double pixelXPrev;
		double pixelYPrev;
		double pixelX;
		double pixelY;

		wayLength = wayNodes.size();

		pixelXPrev = MercatorProjection.longitudeToPixelX(wayNodes.get(0).y, zoom);
		pixelYPrev = MercatorProjection.latitudeToPixelY(wayNodes.get(0).x, zoom);

		// add the first way node to the result list
		result.add(wayNodes.get(0));

		for (int i = 1; i < wayLength - 1; i++) {
			// calculate the pixel coordinates for the current way node
			pixelX = MercatorProjection.longitudeToPixelX(wayNodes.get(i).y, zoom);
			pixelY = MercatorProjection.latitudeToPixelY(wayNodes.get(i).x, zoom);

			// if one of the pixel coordinates is more than delta pixels away from the way
			// node which was most recently added to the result list, add the current way node
			// to the result list
			if (Math.abs(pixelX - pixelXPrev) > delta || Math.abs(pixelY - pixelYPrev) > delta) {
				pixelXPrev = pixelX;
				pixelYPrev = pixelY;
				result.add(wayNodes.get(i));
			}
		}

		// add the last way node to the result list
		result.add(wayNodes.get(wayLength - 1));

		return result;
	}

	/**
	 * Calculates the distance between two way nodes as an integer offset to the previous way
	 * node. The first way node is always stored with its complete resolution of latitude and
	 * longitude coordinates. All following coordinates are offsets. At the end the maximum
	 * values for latitude and longitude offsets are added to the list.
	 * 
	 * @param wayNodes
	 *            list of all way nodes of a way
	 * @param maxValues
	 *            specifies if the maximum values of the latitude and longitude offsets should
	 *            be stored in the result list
	 * 
	 * @return a list that holds the complete coordinates of the first way node, offsets for
	 *         calculating the coordinates of the following way nodes and the maximum values for
	 *         latitude and longitude offsets for this way.
	 */
	static ArrayList<Integer> compressWayNodeDistances(ArrayList<Integer> wayNodes,
			boolean maxValues) {
		int distanceLat;
		int distanceLon;

		int maxDiffLat = 0;
		int maxDiffLon = 0;

		ArrayList<Integer> result = new ArrayList<Integer>();

		if (wayNodes.isEmpty())
			return result;

		// add the first way node to the result list
		result.add(wayNodes.get(0));
		result.add(wayNodes.get(1));

		for (int i = 2; i < wayNodes.size(); i += 2) {

			// calculate the distances and add them to the result list
			distanceLat = (-1) * (wayNodes.get(i - 2) - wayNodes.get(i));
			result.add(distanceLat);

			distanceLon = (-1) * (wayNodes.get(i - 1) - wayNodes.get(i + 1));
			result.add(distanceLon);

			distanceLat = Math.abs(distanceLat);
			distanceLon = Math.abs(distanceLon);

			// compare the current distances with the present maximum distances
			if (distanceLat > maxDiffLat) {
				maxDiffLat = distanceLat;
			}
			if (distanceLon > maxDiffLon) {
				maxDiffLon = distanceLon;
			}
		}

		// if maxValues is true add the maximum values for the distances to the result list
		if (maxValues) {
			result.add(maxDiffLat);
			result.add(maxDiffLon);
		}

		return result;
	}
}
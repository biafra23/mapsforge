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

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.MercatorProjection;
import org.mapsforge.core.Rect;
import org.mapsforge.preprocessing.map.osmosis.TileData.TDNode;
import org.mapsforge.preprocessing.map.osmosis.TileData.TDWay;

class RAMTileBasedDataStore extends BaseTileBasedDataStore {
	private static final Logger logger =
			Logger.getLogger(TileBasedDataStore.class.getName());

	private TLongObjectHashMap<TDNode> nodes;
	protected TLongObjectHashMap<TDWay> ways;
	protected TLongObjectHashMap<TLongArrayList> multipolygons;
	protected TileData[][][] tileData;

	// private TileData[][][] wayTileData;

	private RAMTileBasedDataStore(
			double minLat, double maxLat,
			double minLon, double maxLon,
			ZoomIntervalConfiguration zoomIntervalConfiguration) {
		this(new Rect(minLon, maxLon, minLat, maxLat), zoomIntervalConfiguration);
	}

	private RAMTileBasedDataStore(Rect bbox, ZoomIntervalConfiguration zoomIntervalConfiguration) {
		super(bbox, zoomIntervalConfiguration);
		this.nodes = new TLongObjectHashMap<TDNode>();
		this.ways = new TLongObjectHashMap<TDWay>();
		this.multipolygons = new TLongObjectHashMap<TLongArrayList>();
		this.tileData = new TileData[zoomIntervalConfiguration.getNumberOfZoomIntervals()][][];
		// compute number of tiles needed on each base zoom level
		for (int i = 0; i < zoomIntervalConfiguration.getNumberOfZoomIntervals(); i++) {
			this.tileData[i] = new TileData[computeNumberOfHorizontalTiles(i)][computeNumberOfVerticalTiles(i)];
		}
	}

	static RAMTileBasedDataStore newInstance(Rect bbox,
			ZoomIntervalConfiguration zoomIntervalConfiguration) {
		return new RAMTileBasedDataStore(bbox, zoomIntervalConfiguration);
	}

	static RAMTileBasedDataStore newInstance(double minLat, double maxLat,
			double minLon, double maxLon, ZoomIntervalConfiguration zoomIntervalConfiguration) {
		return new RAMTileBasedDataStore(minLat, maxLat, minLon, maxLon,
				zoomIntervalConfiguration);
	}

	static RAMTileBasedDataStore getStandardInstance(
			double minLat, double maxLat,
			double minLon, double maxLon) {

		return new RAMTileBasedDataStore(
				minLat, maxLat, minLon, maxLon,
				ZoomIntervalConfiguration.getStandardConfiguration());
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
	public TDNode getNode(long id) {
		return nodes.get(id);
	}

	@Override
	public TDWay getWay(long id) {
		return ways.get(id);
	}

	@Override
	public List<TDWay> getInnerWaysOfMultipolygon(long outerWayID) {
		TLongArrayList innerwayIDs = multipolygons.get(outerWayID);
		return getInnerWaysOfMultipolygon(innerwayIDs.toArray());
	}

	private List<TDWay> getInnerWaysOfMultipolygon(long[] innerWayIDs) {
		if (innerWayIDs == null)
			return null;
		List<TDWay> res = new ArrayList<TileData.TDWay>();
		for (long id : innerWayIDs) {
			TDWay current = getWay(id);
			if (current == null)
				continue;
			res.add(current);
		}

		return res;
	}

	@Override
	public int numberOfNodes() {
		return nodes.size();
	}

	@Override
	public boolean addNode(TDNode node) {
		nodes.put(node.getId(), node);
		return true;
	}

	@Override
	public boolean containsNode(long id) {
		return nodes.contains(id);
	}

	@Override
	public boolean addPOI(TDNode node) {
		byte minZoomLevel = node.getMinimumZoomLevel();
		if (minZoomLevel > zoomIntervalConfiguration.getMaxMaxZoom())
			minZoomLevel = zoomIntervalConfiguration.getMaxMaxZoom();
		for (int i = 0; i < zoomIntervalConfiguration.getNumberOfZoomIntervals(); i++) {

			// is poi seen in a zoom interval?
			if (minZoomLevel <= zoomIntervalConfiguration.getMaxZoom(i)) {
				long tileCoordinateX = MercatorProjection.longitudeToTileX(
						GeoCoordinate.intToDouble(node.getLongitude()),
						zoomIntervalConfiguration.getBaseZoom(i));
				long tileCoordinateY = MercatorProjection.latitudeToTileY(
						GeoCoordinate.intToDouble(node.getLatitude()),
						zoomIntervalConfiguration.getBaseZoom(i));
				//
				// System.out.println("adding poi: " + tileCoordinateX + "\t" + tileCoordinateY
				// + "\t" + zoomIntervalConfiguration.getBaseZoom(i));
				// System.out.println(node);
				TileData td = getTile(i, tileCoordinateX, tileCoordinateY);
				if (td != null) {
					td.addPOI(node);
				}
			}
		}
		return true;
	}

	@Override
	public boolean addWay(TDWay way) {
		this.ways.put(way.getId(), way);
		byte minZoomLevel = way.getMinimumZoomLevel();
		if (minZoomLevel > zoomIntervalConfiguration.getMaxMaxZoom())
			minZoomLevel = zoomIntervalConfiguration.getMaxMaxZoom();
		for (int i = 0; i < zoomIntervalConfiguration.getNumberOfZoomIntervals(); i++) {
			// is way seen in a zoom interval?
			if (minZoomLevel <= zoomIntervalConfiguration.getMaxZoom(i)) {
				Set<TileCoordinate> matchedTiles = GeoUtils.mapWayToTiles(way,
						zoomIntervalConfiguration.getBaseZoom(i));
				for (TileCoordinate matchedTile : matchedTiles) {
					TileData td = getTile(i, matchedTile.getX(), matchedTile.getY());
					if (td != null)
						td.addWay(way);
				}
			}
		}

		return true;
	}

	@Override
	public boolean addMultipolygon(long outerWayID, long[] innerWayIDs) {
		TDWay outerWay = getWay(outerWayID);
		// check if outer way exists
		if (outerWay == null) {
			logger.finer("outer way with id " + outerWayID + " not existent in relation");
			return false;
		}
		// check if outer way is polygon
		if (!GeoUtils.isClosedPolygon(outerWay)) {
			logger.finer("outer way is not a polygon, id: " + outerWayID);
			return false;
		}

		// check if all inner ways exist
		List<TDWay> innerWays = getInnerWaysOfMultipolygon(innerWayIDs);
		if (innerWays == null || innerWays.size() < innerWayIDs.length) {
			logger.finer("some inner ways are missing for outer way with id " + outerWayID);
			return false;
		}

		for (Iterator<TDWay> innerWaysIterator = innerWays.iterator(); innerWaysIterator
				.hasNext();) {
			TDWay innerWay = innerWaysIterator.next();
			// remove all tags from the inner way that are already present in the outer way
			if (outerWay.getTags() != null && innerWay.getTags() != null) {
				innerWay.getTags().removeAll(outerWay.getTags());
			}
			// only remove from normal ways, if the inner way has no tags other than the
			// outer way
			if (innerWay.getTags() == null || innerWay.getTags().size() == 0) {
				for (int i = 0; i < zoomIntervalConfiguration.getNumberOfZoomIntervals(); i++) {
					Set<TileCoordinate> associatedTiles = GeoUtils.mapWayToTiles(innerWay,
							zoomIntervalConfiguration.getBaseZoom(i));
					if (associatedTiles == null)
						continue;
					for (TileCoordinate associatedTile : associatedTiles) {
						TileData td = getTile(i, associatedTile.getX(), associatedTile.getY());
						if (td != null)
							td.removeWay(innerWay);
					}
				}
			}
			// the inner way has tags other than the outer way --> must be rendered as normal
			// way, remove it from list of inner ways
			else {
				innerWaysIterator.remove();
			}
		}

		TLongArrayList innerWayIDList = multipolygons.get(outerWayID);
		if (innerWayIDList == null) {
			innerWayIDList = new TLongArrayList();
		}
		innerWayIDList.add(innerWayIDs);
		multipolygons.put(outerWayID, innerWayIDList);

		// TODO document this side effect

		outerWay.setWaytype((short) 3);

		return true;
	}

	@Override
	public TileData getTile(int baseZoomIndex, long tileCoordinateX, long tileCoordinateY) {
		int tileCoordinateXIndex = (int) (tileCoordinateX - upperLeftTiles[baseZoomIndex]
				.getX());
		int tileCoordinateYIndex = (int) (tileCoordinateY - upperLeftTiles[baseZoomIndex]
				.getY());
		// check for valid range
		if (tileCoordinateXIndex < 0 || tileCoordinateYIndex < 0 ||
				tileData[baseZoomIndex].length <= tileCoordinateXIndex ||
				tileData[baseZoomIndex][0].length <= tileCoordinateYIndex)
			return null;

		TileData td = tileData[baseZoomIndex][tileCoordinateXIndex][tileCoordinateYIndex];
		if (td == null) {
			td = tileData[baseZoomIndex][tileCoordinateXIndex][tileCoordinateYIndex] = new TileData();
		}

		return td;
	}

	@Override
	public int numberOfHorizontalTiles(int zoomIntervalIndex) {
		return tileData[zoomIntervalIndex].length;
	}

	@Override
	public int numberOfVerticalTiles(int zoomIntervalIndex) {
		return tileData[zoomIntervalIndex][0].length;
	}
}

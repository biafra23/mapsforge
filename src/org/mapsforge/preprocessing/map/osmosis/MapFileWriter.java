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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.preprocessing.map.osmosis.TileData.TDNode;
import org.mapsforge.preprocessing.map.osmosis.TileData.TDWay;

class MapFileWriter {

	private static final String DEBUG_INDEX_START_STRING = "+++IndexStart+++";

	private static final int SIZE_ZOOMINTERVAL_CONFIGURATION = 13;

	private static final int PIXEL_COMPRESSION_MAX_DELTA = 3;

	private static final int BYTE_AMOUNT_SUBFILE_INDEX_PER_TILE = 5;

	private static final String MAGIC_BYTE = "mapsforge binary OSM";

	// DEBUG STRINGS
	private static final String DEBUG_STRING_POI_HEAD = "***POIStart";
	private static final String DEBUG_STRING_POI_TAIL = "***";
	private static final String DEBUG_STRING_TILE_HEAD = "###TileStart";
	private static final String DEBUG_STRING_TILE_TAIL = "###";
	private static final String DEBUG_STRING_WAY_HEAD = "---WayStart";
	private static final String DEBUG_STRING_WAY_TAIL = "---";

	// bitmap flags for pois and ways
	private static final short BITMAP_NAME = 128;

	// bitmap flags for pois
	private static final short BITMAP_ELEVATION = 64;
	private static final short BITMAP_HOUSENUMBER = 32;

	// bitmap flags for ways
	private static final short BITMAP_REF = 64;
	// private static final short BITMAP_LABEL = 32;
	private static final short BITMAP_MULTIPOLYGON = 16;
	private static final short BITMAP_WAYNODECOMPRESSION_4_BYTE = 0;
	private static final short BITMAP_WAYNODECOMPRESSION_3_BYTE = 1;
	private static final short BITMAP_WAYNODECOMPRESSION_2_BYTE = 2;
	private static final short BITMAP_WAYNODECOMPRESSION_1_BYTE = 3;
	private static final short BITMAP_HIGHWAY = 128;
	private static final short BITMAP_RAILWAY = 64;
	private static final short BITMAP_BUILDING = 32;
	private static final short BITMAP_LANDUSE = 16;
	private static final short BITMAP_LEISURE = 8;
	private static final short BITMAP_AMENITY = 4;
	private static final short BITMAP_NATURAL = 2;
	private static final short BITMAP_WATERWAY = 1;

	// bitmap flags for file features
	private static final short BITMAP_DEBUG = 128;
	private static final short BITMAP_MAP_START_POSITION = 64;
	private static final short BITMAP_WAYNODE_FILTERING = 32;
	private static final short BITMAP_POLYGON_CLIPPING = 16;
	private static final short BITMAP_WAYNODE_COMPRESSION = 8;

	private static final Logger logger = Logger.getLogger(MapFileWriter.class.getName());

	private static final String PROJECTION = "Mercator";

	private static final byte MAX_ZOOMLEVEL_PIXEL_FILTER = 11;

	private static final byte MIN_ZOOMLEVEL_POLYGON_CLIPPING = 12;

	// data
	private TileBasedDataStore dataStore;

	// IO
	private static final int HEADER_BUFFER_SIZE = 0x100000; // 1MB
	private static final int MIN_TILE_BUFFER_SIZE = 0xA00000; // 10MB
	private static final int TILE_BUFFER_SIZE = 0x3200000;
	private final RandomAccessFile randomAccessFile;
	private MappedByteBuffer bufferZoomIntervalConfig;

	// concurrent computation of subtile bitmask
	private final ExecutorService executorService;

	// accounting
	private long tilesProcessed = 0;
	private long fivePercentOfTilesToProcess;

	MapFileWriter(TileBasedDataStore dataStore, RandomAccessFile file, int threadpoolSize) {
		super();
		this.dataStore = dataStore;
		this.randomAccessFile = file;
		fivePercentOfTilesToProcess = dataStore.cumulatedNumberOfTiles() / 20;
		if (fivePercentOfTilesToProcess == 0)
			fivePercentOfTilesToProcess = 1;
		executorService = Executors.newFixedThreadPool(threadpoolSize);
	}

	final void writeFileWithDebugInfos(long date, int version, short tilePixel)
			throws IOException {
		writeFile(date, version, tilePixel, null, true, true, true, true, null);
	}

	final void writeFile(long date, int version, short tilePixel) throws IOException {
		writeFile(date, version, tilePixel, null, false, true, true, true, null);
	}

	final void writeFile() throws IOException {
		writeFile(System.currentTimeMillis(), 1, (short) 256, null, false, true, true, true,
				null);
	}

	final void writeFile(GeoCoordinate mapStartPosition) throws IOException {
		writeFile(System.currentTimeMillis(), 1, (short) 256, null, false, true, true, true,
				mapStartPosition);
	}

	final void writeFile(long date, int version, short tilePixel, String comment,
			boolean debugStrings, boolean waynodeCompression, boolean polygonClipping,
			boolean pixelCompression, GeoCoordinate mapStartPosition)
			throws IOException {

		// CONTAINER HEADER
		long totalHeaderSize = writeContainerHeader(date, version, tilePixel,
				comment, debugStrings, waynodeCompression, polygonClipping, pixelCompression,
				mapStartPosition);

		int n_zoom_intervals = dataStore.getZoomIntervalConfiguration()
				.getNumberOfZoomIntervals();

		// SUB FILES
		// for each zoom interval write a sub file
		long currentFileSize = totalHeaderSize;
		for (int i = 0; i < n_zoom_intervals; i++) {
			// SUB FILE INDEX AND DATA
			long subfileSize = writeSubfile(currentFileSize, i, debugStrings,
					waynodeCompression, polygonClipping, pixelCompression);
			// SUB FILE META DATA IN CONTAINER HEADER
			writeSubfileMetaDataToContainerHeader(i, currentFileSize, subfileSize);
			currentFileSize += subfileSize;
		}

		randomAccessFile.setLength(currentFileSize);
		randomAccessFile.close();
	}

	private void writeUTF8(String string, MappedByteBuffer buffer) {
		buffer.putShort((short) string.getBytes().length);
		buffer.put(string.getBytes());
	}

	private long writeContainerHeader(long date, int version, short tilePixel, String comment,
			boolean debugStrings, boolean waynodeCompression, boolean polygonClipping,
			boolean pixelCompression, GeoCoordinate mapStartPosition)
			throws IOException {

		// get metadata for the map file
		int numberOfZoomIntervals = dataStore.getZoomIntervalConfiguration()
				.getNumberOfZoomIntervals();

		logger.fine("writing header");

		MappedByteBuffer containerHeaderBuffer = randomAccessFile.getChannel().map(
				MapMode.READ_WRITE, 0,
				HEADER_BUFFER_SIZE);

		// write file header
		// magic byte
		byte[] magicBytes = MAGIC_BYTE.getBytes();
		containerHeaderBuffer.put(magicBytes);

		// write container header size
		int headerSizePosition = containerHeaderBuffer.position();
		containerHeaderBuffer.position(headerSizePosition + 4);

		// version number of the binary file format
		containerHeaderBuffer.putInt(version);

		// meta info byte
		containerHeaderBuffer.put(buildMetaInfoByte(debugStrings, mapStartPosition != null,
				pixelCompression,
				polygonClipping, waynodeCompression));

		// amount of map files inside this file
		containerHeaderBuffer.put((byte) numberOfZoomIntervals);

		// projection type
		writeUTF8(PROJECTION, containerHeaderBuffer);

		// width and height of a tile in pixel
		containerHeaderBuffer.putShort(tilePixel);

		logger.fine("Bounding box for file: " +
				dataStore.getBoundingBox().maxLatitudeE6 + ", " +
				dataStore.getBoundingBox().minLongitudeE6 + ", " +
				dataStore.getBoundingBox().minLatitudeE6 + ", " +
				dataStore.getBoundingBox().maxLongitudeE6);
		// upper left corner of the bounding box
		containerHeaderBuffer.putInt(dataStore.getBoundingBox().maxLatitudeE6);
		containerHeaderBuffer.putInt(dataStore.getBoundingBox().minLongitudeE6);
		containerHeaderBuffer.putInt(dataStore.getBoundingBox().minLatitudeE6);
		containerHeaderBuffer.putInt(dataStore.getBoundingBox().maxLongitudeE6);

		if (mapStartPosition != null) {
			containerHeaderBuffer.putInt(mapStartPosition.getLatitudeE6());
			containerHeaderBuffer.putInt(mapStartPosition.getLongitudeE6());
		}

		// date of the map data
		containerHeaderBuffer.putLong(date);

		// store the mapping of tags to tag ids
		containerHeaderBuffer.putShort((short) PoiEnum.values().length);
		for (PoiEnum poiEnum : PoiEnum.values()) {
			writeUTF8(poiEnum.toString(), containerHeaderBuffer);
			containerHeaderBuffer.putShort((short) poiEnum.ordinal());
		}
		containerHeaderBuffer.putShort((short) WayEnum.values().length);
		for (WayEnum wayEnum : WayEnum.values()) {
			writeUTF8(wayEnum.toString(), containerHeaderBuffer);
			containerHeaderBuffer.putShort((short) wayEnum.ordinal());
		}

		// comment
		if (comment != null && !comment.equals("")) {
			writeUTF8(comment, containerHeaderBuffer);
		} else {
			writeUTF8("", containerHeaderBuffer);
		}

		// initialize buffer for writing zoom interval configurations
		bufferZoomIntervalConfig = randomAccessFile.getChannel().map(MapMode.READ_WRITE,
				containerHeaderBuffer.position(), SIZE_ZOOMINTERVAL_CONFIGURATION
						* numberOfZoomIntervals);

		containerHeaderBuffer.position(containerHeaderBuffer.position()
				+ SIZE_ZOOMINTERVAL_CONFIGURATION
				* numberOfZoomIntervals);

		// -4 bytes of header size variable itself
		int headerSize = containerHeaderBuffer.position() - headerSizePosition - 4;
		containerHeaderBuffer.putInt(headerSizePosition, headerSize);

		return containerHeaderBuffer.position();
	}

	private void writeSubfileMetaDataToContainerHeader(int i, long startIndexOfSubfile,
			long subfileSize) {

		// HEADER META DATA FOR SUB FILE
		// write zoom interval configuration to header
		byte minZoomCurrentInterval = dataStore.getZoomIntervalConfiguration().getMinZoom(i);
		byte maxZoomCurrentInterval = dataStore.getZoomIntervalConfiguration().getMaxZoom(i);
		byte baseZoomCurrentInterval = dataStore.getZoomIntervalConfiguration().getBaseZoom(i);

		bufferZoomIntervalConfig.put(baseZoomCurrentInterval);
		bufferZoomIntervalConfig.put(minZoomCurrentInterval);
		bufferZoomIntervalConfig.put(maxZoomCurrentInterval);
		bufferZoomIntervalConfig.put(Serializer.getFiveBytes(startIndexOfSubfile));
		bufferZoomIntervalConfig.put(Serializer.getFiveBytes(subfileSize));
	}

	private long writeSubfile(long startPositionSubfile, int zoomIntervalIndex,
			boolean debugStrings, boolean waynodeCompression, boolean polygonClipping,
			boolean pixelCompression)
			throws IOException {

		logger.fine("writing data for zoom interval " + zoomIntervalIndex
				+ ", number of tiles: " +
				dataStore.numberOfHorizontalTiles(zoomIntervalIndex)
				* dataStore.numberOfVerticalTiles(zoomIntervalIndex));

		TileCoordinate upperLeft = dataStore.getUpperLeft(zoomIntervalIndex);
		int lengthX = dataStore.numberOfHorizontalTiles(zoomIntervalIndex);
		int lengthY = dataStore.numberOfVerticalTiles(zoomIntervalIndex);

		byte minZoomCurrentInterval = dataStore.getZoomIntervalConfiguration().getMinZoom(
				zoomIntervalIndex);
		byte maxZoomCurrentInterval = dataStore.getZoomIntervalConfiguration().getMaxZoom(
				zoomIntervalIndex);
		byte baseZoomCurrentInterval = dataStore.getZoomIntervalConfiguration().getBaseZoom(
				zoomIntervalIndex);
		byte maxMaxZoomlevel = dataStore.getZoomIntervalConfiguration().getMaxMaxZoom();

		int tileAmountInBytes = dataStore.numberOfHorizontalTiles(zoomIntervalIndex)
				* dataStore.numberOfVerticalTiles(zoomIntervalIndex)
				* BYTE_AMOUNT_SUBFILE_INDEX_PER_TILE;
		int indexBufferSize = tileAmountInBytes
				+ (debugStrings ? DEBUG_INDEX_START_STRING.getBytes().length : 0);
		MappedByteBuffer indexBuffer = randomAccessFile.getChannel().map(MapMode.READ_WRITE,
				startPositionSubfile, indexBufferSize);
		MappedByteBuffer tileBuffer = randomAccessFile.getChannel().map(MapMode.READ_WRITE,
				startPositionSubfile + indexBufferSize, TILE_BUFFER_SIZE);

		long currentSubfileOffset = indexBufferSize;

		for (int tileY = upperLeft.getY(); tileY < upperLeft.getY() + lengthY; tileY++) {
			for (int tileX = upperLeft.getX(); tileX < upperLeft.getX() + lengthX; tileX++) {
				// logger.info("writing data for tile (" + tileX + ", " + tileY + ")");

				long currentTileOffsetInBuffer = tileBuffer.position();
				TileCoordinate currentTileCoordinate = new TileCoordinate(tileX, tileY,
						baseZoomCurrentInterval);

				// seek to index frame of this tile and write relative offset of this
				// tile as five bytes to the index
				indexBuffer.put(Serializer.getFiveBytes(currentSubfileOffset));

				// get statistics for tile
				TileData currentTile = dataStore.getTile(zoomIntervalIndex, tileX, tileY);

				// ************* POI ************
				// write amount of POIs and ways for each zoom level
				// TODO is this computation correct? Ways that have an associated zoom level of
				// e.g. 9
				// are lifted to zoom level 12 for an interval 12,14,17
				Map<Byte, List<TDNode>> poisByZoomlevel = currentTile
						.poisByZoomlevel(minZoomCurrentInterval, maxMaxZoomlevel);
				Map<Byte, List<TDWay>> waysByZoomlevel = currentTile
						.waysByZoomlevel(minZoomCurrentInterval, maxMaxZoomlevel);

				if (poisByZoomlevel.size() > 0 || waysByZoomlevel.size() > 0) {
					int tileContainerStart = tileBuffer.position();
					if (debugStrings) {
						// write tile header
						StringBuilder sb = new StringBuilder();
						sb.append(DEBUG_STRING_TILE_HEAD).append(tileX).append(",")
								.append(tileY)
								.append(DEBUG_STRING_TILE_TAIL);
						tileBuffer.put(sb.toString().getBytes());
						// append withespaces so that block has 32 bytes
						appendWhitespace(32 - sb.toString().getBytes().length, tileBuffer);
					}

					short cumulatedPOIs = 0;
					short cumulatedWays = 0;
					for (byte zoomlevel = minZoomCurrentInterval; zoomlevel <= maxZoomCurrentInterval; zoomlevel++) {
						if (poisByZoomlevel.get(zoomlevel) != null)
							cumulatedPOIs += poisByZoomlevel.get(zoomlevel).size();
						if (waysByZoomlevel.get(zoomlevel) != null)
							cumulatedWays += waysByZoomlevel.get(zoomlevel).size();
						tileBuffer.putShort(cumulatedPOIs);
						tileBuffer.putShort(cumulatedWays);
					}

					// skip 4 bytes, later these 4 bytes will contain the start
					// position of the ways in this tile
					int fileIndexStartWayContainer = tileBuffer.position();
					tileBuffer.position(fileIndexStartWayContainer + 4);

					// write POIs for each zoom level beginning with lowest zoom level
					for (byte zoomlevel = minZoomCurrentInterval; zoomlevel <= maxZoomCurrentInterval; zoomlevel++) {
						List<TDNode> pois = poisByZoomlevel.get(zoomlevel);
						if (pois == null)
							continue;
						for (TDNode poi : pois) {
							if (debugStrings) {
								StringBuilder sb = new StringBuilder();
								sb.append(DEBUG_STRING_POI_HEAD).append(poi.getId())
										.append(DEBUG_STRING_POI_TAIL);
								tileBuffer.put(sb.toString().getBytes());
								// append withespaces so that block has 32 bytes
								appendWhitespace(32 - sb.toString().getBytes().length,
										tileBuffer);
							}

							// write poi features to the file
							tileBuffer.putInt(poi.getLatitude());
							tileBuffer.putInt(poi.getLongitude());

							// write byte with layer and tag amount info
							tileBuffer.put(buildLayerTagAmountByte(poi.getLayer(),
									poi.getTags() == null ? 0 : (short) poi.getTags().size()));

							// write tag ids to the file
							if (poi.getTags() != null) {
								for (PoiEnum poiEnum : poi.getTags()) {
									tileBuffer.putShort((short) poiEnum.ordinal());
								}
							}

							// write byte with bits set to 1 if the poi has a name, an elevation
							// or a housenumber
							tileBuffer.put(buildInfoByteForPOI(poi.getName(),
									poi.getElevation(),
									poi.getHouseNumber()));

							if (poi.getName() != null && poi.getName().length() > 0) {
								writeUTF8(poi.getName(), tileBuffer);

							}
							if (poi.getElevation() != 0) {
								tileBuffer.putShort(poi.getElevation());
							}
							if (poi.getHouseNumber() != null
									&& poi.getHouseNumber().length() > 0) {
								writeUTF8(poi.getHouseNumber(), tileBuffer);
							}
						}
					}// end for loop over POIs

					// write offset to first way in the tile header
					tileBuffer.putInt(fileIndexStartWayContainer, tileBuffer.position()
							- tileContainerStart);

					// ************* WAYS ************
					// write ways
					for (byte zoomlevel = minZoomCurrentInterval; zoomlevel <= maxZoomCurrentInterval; zoomlevel++) {
						List<TDWay> ways = waysByZoomlevel.get(zoomlevel);
						if (ways == null)
							continue;

						// use executor service to parallelize computation of subtile bitmasks
						// for all
						// ways in the current tile
						short[] bitmaskComputationResults = computeSubtileBitmasks(ways,
								currentTileCoordinate);
						assert bitmaskComputationResults.length == ways.size();
						// needed to access bitmask computation results in the foreach loop
						int i = 0;
						for (TDWay way : ways) {
							// // INNER WAY
							// // inner ways will be written as part of the outer way
							// if (way.isInnerWay())
							// continue;
							int startIndexWay = tileBuffer.position();

							WayNodePreprocessingResult wayNodePreprocessingResult = preprocessWayNodes(
									way, waynodeCompression, pixelCompression, polygonClipping,
									maxZoomCurrentInterval, minZoomCurrentInterval,
									currentTileCoordinate);

							if (wayNodePreprocessingResult == null) {
								continue;
							}
							if (debugStrings) {
								StringBuilder sb = new StringBuilder();
								sb.append(DEBUG_STRING_WAY_HEAD).append(way.getId())
										.append(DEBUG_STRING_WAY_TAIL);
								tileBuffer.put(sb.toString().getBytes());
								// append withespaces so that block has 32 bytes
								appendWhitespace(32 - sb.toString().getBytes().length,
										tileBuffer);
							}

							// skip 4 bytes to reserve space for way size
							int startIndexWaySize = tileBuffer.position();
							tileBuffer.position(startIndexWaySize + 4);

							// write way features
							// short bitmask = GeoUtils.computeBitmask(way,
							// currentTileCoordinate);
							// short bitmask = (short) 0xffff;
							tileBuffer.putShort(bitmaskComputationResults[i++]);

							// write byte with layer and tag amount
							tileBuffer.put(buildLayerTagAmountByte(way.getLayer(),
									way.getTags() == null ? 0 : (short) way.getTags().size()));

							// set type of the way node compression
							int compressionType = wayNodePreprocessingResult
									.getCompressionType();

							// write byte with amount of tags which are rendered
							tileBuffer.put(buildRenderTagWayNodeCompressionByte(
									way.getTags(),
									compressionType));

							// write tag bitmap
							tileBuffer.put(buildTagBitmapByte(way.getTags()));
							// file.writeByte((byte) 0xff);

							// write tag ids
							if (way.getTags() != null) {
								for (WayEnum wayEnum : way.getTags()) {
									tileBuffer.putShort((short) wayEnum.ordinal());
								}
							}
							// write the amount of way nodes to the file
							tileBuffer.putShort((short) (wayNodePreprocessingResult
									.getWaynodesAsList()
									.size() / 2));

							// write the way nodes:
							// the first node is always stored with four bytes
							// the remaining way node differences are stored according to the
							// compression type
							writeWayNodes(wayNodePreprocessingResult.getWaynodesAsList(),
									wayNodePreprocessingResult.getCompressionType(), tileBuffer);

							// write a byte with name, label and way type information
							tileBuffer.put(buildInfoByteForWay(way.getName(),
									way.getWaytype(),
									way.getRef()));

							// // if the way has a name, write it to the file
							if (way.getName() != null && way.getName().length() > 0) {
								writeUTF8(way.getName(), tileBuffer);
							}

							// if the way has a ref, write it to the file
							if (way.getRef() != null && way.getRef().length() > 0) {
								writeUTF8(way.getRef(), tileBuffer);
							}
							//
							// // // if the way has a label position write it to the file
							// // if (labelPositionLatitude != 0 && labelPositionLongitude != 0)
							// {
							// // raf.writeInt(labelPositionLatitude);
							// // raf.writeInt(labelPositionLongitude);
							// // }
							//
							// *********MULTIPOLYGON PROCESSING***********
							if (way.getWaytype() == 3 && dataStore
									.getInnerWaysOfMultipolygon(way.getId()) != null) {
								List<TDWay> innerways = dataStore
										.getInnerWaysOfMultipolygon(way.getId());

								if (innerways == null) {
									tileBuffer.put((byte) 0);
								} else {
									tileBuffer.put((byte) innerways.size());
									for (TDWay innerway : innerways) {
										WayNodePreprocessingResult innerWayNodePreprocessingResult =
												preprocessWayNodes(innerway,
														waynodeCompression, pixelCompression,
														false,
														maxZoomCurrentInterval,
														minZoomCurrentInterval,
														currentTileCoordinate);
										// write the amount of way nodes to the file
										tileBuffer
												.putShort((short) (innerWayNodePreprocessingResult
														.getWaynodesAsList()
														.size() / 2));
										writeWayNodes(
												innerWayNodePreprocessingResult
														.getWaynodesAsList(),
												wayNodePreprocessingResult.getCompressionType(),
												tileBuffer);
									}
								}
							}
							// write the size of the way to the file
							tileBuffer.putInt(startIndexWaySize, tileBuffer.position()
									- startIndexWay);
						}
					}// end for loop over ways
				}// end if clause checking if tile is empty or not
				long tileSize = tileBuffer.position() - currentTileOffsetInBuffer;
				currentSubfileOffset += tileSize;

				// if necessary, allocate new buffer
				if (tileBuffer.remaining() < MIN_TILE_BUFFER_SIZE)
					tileBuffer = randomAccessFile.getChannel().map(MapMode.READ_WRITE,
							startPositionSubfile + currentSubfileOffset,
							TILE_BUFFER_SIZE);

				tilesProcessed++;
				if (tilesProcessed % fivePercentOfTilesToProcess == 0) {
					logger.info("written " + (tilesProcessed / fivePercentOfTilesToProcess)
							* 5
							+ "% of file");
				}
			}// end for loop over tile columns
		}// /end for loop over tile rows

		// return size of sub file in bytes
		return currentSubfileOffset;
	}

	private short[] computeSubtileBitmasks(List<TDWay> ways,
			TileCoordinate currentTileCoordinate) {
		short[] bitmaskComputationResults = new short[ways.size()];
		Collection<TileBitmaskComputationTask> tasks = new ArrayList<MapFileWriter.TileBitmaskComputationTask>();
		for (TDWay tdWay : ways) {
			tasks.add(new TileBitmaskComputationTask(currentTileCoordinate,
					tdWay));
		}

		try {
			List<Future<Short>> results = executorService.invokeAll(tasks);
			int i = 0;
			for (Future<Short> future : results) {
				bitmaskComputationResults[i++] = future.get().shortValue();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

		return bitmaskComputationResults;
	}

	private List<Integer> waynodesAsList(List<GeoCoordinate> waynodeCoordinates) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (GeoCoordinate geoCoordinate : waynodeCoordinates) {
			result.add(geoCoordinate.getLatitudeE6());
			result.add(geoCoordinate.getLongitudeE6());
		}

		return result;
	}

	private void appendWhitespace(int amount, MappedByteBuffer buffer) {
		for (int i = 0; i < amount; i++) {
			buffer.put((byte) ' ');
		}
	}

	private WayNodePreprocessingResult preprocessWayNodes(TDWay way,
			boolean waynodeCompression,
			boolean pixelCompression, boolean polygonClipping, byte maxZoomCurrentInterval,
			byte minZoomCurrentInterval,
			TileCoordinate tile) {
		List<GeoCoordinate> waynodeCoordinates = way.wayNodesAsCoordinateList();

		// if the sub file for lower zoom levels is written, remove all way
		// nodes from the list which are projected on the same pixel
		if (pixelCompression && maxZoomCurrentInterval <= MAX_ZOOMLEVEL_PIXEL_FILTER) {
			waynodeCoordinates = GeoUtils.filterWaynodesOnSamePixel(
					waynodeCoordinates,
					maxZoomCurrentInterval, PIXEL_COMPRESSION_MAX_DELTA);
		}

		// if the way is a multipolygon without a name, clip the way to the
		// tile
		if (polygonClipping && way.getWaytype() >= 2 && waynodeCoordinates.size() >= 4 &&
				(way.getName() == null || way.getName().length() == 0)
					&& minZoomCurrentInterval >= MIN_ZOOMLEVEL_POLYGON_CLIPPING) {
			List<GeoCoordinate> clipped = GeoUtils.clipPolygonToTile(
					waynodeCoordinates, tile);
			if (clipped != null)
				waynodeCoordinates = clipped;
		}

		// if the wayNodeCompression flag is set, compress the way nodes
		// with a minimal amount of bytes
		List<Integer> waynodesAsList = null;
		int maxDiff = Integer.MAX_VALUE;
		if (waynodeCompression) {
			waynodesAsList = GeoUtils.waynodeAbsoluteCoordinatesToOffsets(waynodeCoordinates);
			maxDiff = GeoUtils.maxDiffBetweenCompressedWayNodes(waynodesAsList);
		} else {
			waynodesAsList = waynodesAsList(waynodeCoordinates);
		}

		WayNodePreprocessingResult result = new WayNodePreprocessingResult(waynodesAsList,
				computeCompressionType(maxDiff));

		return result;
	}

	/**
	 * Returns a byte that specifies the layer on which the map element should be rendered and
	 * the amount of tags that a map element has.
	 * 
	 * @param layer
	 *            the layer on which a map element should be rendered
	 * @param tagAmount
	 *            the amount of all tags a map element has
	 * @return a byte that holds the specified information
	 */
	private byte buildLayerTagAmountByte(byte layer, short tagAmount) {
		return (byte) (layer << 4 | tagAmount);
	}

	private byte buildMetaInfoByte(boolean debug, boolean mapStartPosition, boolean filtering,
			boolean clipping,
			boolean compression) {
		byte infoByte = 0;

		if (debug)
			infoByte |= BITMAP_DEBUG;
		if (mapStartPosition)
			infoByte |= BITMAP_MAP_START_POSITION;
		if (filtering)
			infoByte |= BITMAP_WAYNODE_FILTERING;
		if (clipping)
			infoByte |= BITMAP_POLYGON_CLIPPING;
		if (compression)
			infoByte |= BITMAP_WAYNODE_COMPRESSION;

		return infoByte;
	}

	private byte buildInfoByteForPOI(String name, int elevation, String housenumber) {
		byte infoByte = 0;

		if (name != null && name.length() > 0) {
			infoByte |= BITMAP_NAME;
		}
		if (elevation != 0) {
			infoByte |= BITMAP_ELEVATION;
		}
		if (housenumber != null && housenumber.length() > 0) {
			infoByte |= BITMAP_HOUSENUMBER;
		}
		return infoByte;
	}

	private int computeCompressionType(int maxDiff) {
		int compressionType = 0;

		if (maxDiff <= Byte.MAX_VALUE)
			compressionType = 3;
		else if (maxDiff <= Short.MAX_VALUE)
			compressionType = 2;
		else if (maxDiff <= Serializer.MAX_VALUE_THREE_BYTES)
			compressionType = 1;

		return compressionType;
	}

	private byte buildRenderTagWayNodeCompressionByte(EnumSet<WayEnum> tags,
			int wayNodeCompressionType) {

		byte infoByte = 0;
		short counter = 0;
		if (tags != null) {
			for (WayEnum wayEnum : tags) {
				if (wayEnum.associatedWithValidZoomlevel())
					counter++;
			}
			infoByte = (byte) (counter << 5);
		}

		if (wayNodeCompressionType == 0) {
			infoByte |= BITMAP_WAYNODECOMPRESSION_4_BYTE;
		}
		if (wayNodeCompressionType == 1) {
			infoByte |= BITMAP_WAYNODECOMPRESSION_3_BYTE;
		}
		if (wayNodeCompressionType == 2) {
			infoByte |= BITMAP_WAYNODECOMPRESSION_2_BYTE;
		}
		if (wayNodeCompressionType == 3) {
			infoByte |= BITMAP_WAYNODECOMPRESSION_1_BYTE;
		}

		return infoByte;
	}

	private byte buildTagBitmapByte(EnumSet<WayEnum> tags) {
		if (tags == null)
			return 0;
		byte infoByte = 0;

		for (WayEnum wayEnum : tags) {
			switch (wayEnum.waytype()) {
				case HIGHWAY:
					infoByte |= BITMAP_HIGHWAY;
					break;
				case RAILWAY:
					infoByte |= BITMAP_RAILWAY;
					break;
				case BUILDING:
					infoByte |= BITMAP_BUILDING;
					break;
				case LANDUSE:
					infoByte |= BITMAP_LANDUSE;
					break;
				case LEISURE:
					infoByte |= BITMAP_LEISURE;
					break;
				case AMENITY:
					infoByte |= BITMAP_AMENITY;
					break;
				case NATURAL:
					infoByte |= BITMAP_NATURAL;
					break;
				case WATERWAY:
					infoByte |= BITMAP_WATERWAY;
					break;
				case UNCLASSIFIED:
					break;
			}
		}
		return infoByte;
	}

	private byte buildInfoByteForWay(String name, int wayType, String ref) {
		byte infoByte = 0;

		if (name != null && name.length() > 0) {
			infoByte |= BITMAP_NAME;
		}
		if (ref != null && ref.length() > 0) {
			infoByte |= BITMAP_REF;
		}
		// TODO we do not yet support label positions for ways
		// if (labelPosLat != 0 && labelPosLon != 0) {
		// infoByte |= BITMAP_LABEL;
		// }
		if (wayType == 3) {
			infoByte |= BITMAP_MULTIPOLYGON;
		}

		return infoByte;
	}

	private void writeWayNodes(List<Integer> waynodes, int compressionType,
			MappedByteBuffer buffer) {
		if (!waynodes.isEmpty()
				&& waynodes.size() % 2 == 0) {
			Iterator<Integer> waynodeIterator = waynodes.iterator();
			buffer.putInt(waynodeIterator.next());
			buffer.putInt(waynodeIterator.next());

			while (waynodeIterator.hasNext()) {
				switch (compressionType) {
					case 0:
						buffer.putInt(waynodeIterator.next().intValue());
						buffer.putInt(waynodeIterator.next().intValue());
						break;
					case 1:
						buffer.put(Serializer
								.getSignedThreeBytes(waynodeIterator.next().intValue()));
						buffer.put(Serializer
								.getSignedThreeBytes(waynodeIterator.next().intValue()));
						break;
					case 2:
						buffer.putShort(waynodeIterator.next().shortValue());
						buffer.putShort(waynodeIterator.next().shortValue());
						break;
					case 3:
						buffer.put(waynodeIterator.next().byteValue());
						buffer.put(waynodeIterator.next().byteValue());
						break;
				}
			}
		}
	}

	private class WayNodePreprocessingResult {
		private final List<Integer> waynodesAsList;
		private final int compressionType;

		WayNodePreprocessingResult(List<Integer> waynodesAsList, int compressionType) {
			super();
			this.waynodesAsList = waynodesAsList;
			this.compressionType = compressionType;
		}

		List<Integer> getWaynodesAsList() {
			return waynodesAsList;
		}

		int getCompressionType() {
			return compressionType;
		}
	}

	private class TileBitmaskComputationTask implements Callable<Short> {

		private final TileCoordinate baseTile;
		private final TDWay way;

		TileBitmaskComputationTask(TileCoordinate baseTile, TDWay way) {
			super();
			this.baseTile = baseTile;
			this.way = way;
		}

		@Override
		public Short call() {
			return GeoUtils.computeBitmask(way, baseTile);
		}

	}
}
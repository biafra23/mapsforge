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

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.mapsforge.core.DBConnection;
import org.mapsforge.core.MercatorProjection;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Extracts information relevant for binary map file generation from the OSM-data and stores it
 * in a database. The schema for the database is defined in map_import.sql contained in the sql
 * folder. Currently the class expects the database to be PostgreSQL.
 * 
 * <b>Attention:</b> Truncates tables of target database before inserting new data.
 * 
 * @author bross
 * 
 *         adjusted by sschroet
 * 
 */

public class XML2PostgreSQLMap extends DefaultHandler {

	private static final Logger logger = Logger
			.getLogger(XML2PostgreSQLMap.class.getName());

	private static final String DEFAULT_BATCH_SIZE_POI = "55000";

	private static final String DEFAULT_BATCH_SIZE_WAY = "5000";

	private static final int batchSizeMP = 600;

	private static final int VERSION = 1;

	// sql queries
	private final String SQL_INSERT_POIS_TMP = "INSERT INTO pois_tmp (id, latitude, longitude, name_length, name, tags_amount, tags, layer, elevation, housenumber) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private final String SQL_INSERT_POIS = "INSERT INTO pois (id, latitude, longitude, name_length, name, tags_amount, tags, layer, elevation, housenumber) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private final String SQL_INSERT_POI_TAG = "INSERT INTO pois_tags(poi_id, tag) VALUES (?,?)";
	private final String SQL_INSERT_WAYS = "INSERT INTO ways (id, name_length, name, tags_amount, tags, layer, waynodes_amount, way_type, convexness, label_pos_lat, label_pos_lon, inner_way_amount, ref) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private final String SQL_INSERT_WAY_TAG = "INSERT INTO ways_tags(way_id, tag) VALUES (?,?)";
	private final String SQL_INSERT_MULTIPOLYGONS = "INSERT INTO multipolygons (outer_way_id, inner_way_sequence, waynode_sequence, latitude, longitude) VALUES (?,?,?,?,?)";
	private final String SQL_SELECT_WAYNODES = "SELECT id, latitude, longitude FROM pois_tmp WHERE id in ";
	private final String SQL_INSERT_WAYNODES = "INSERT INTO waynodes (way_id, waynode_sequence, latitude, longitude) VALUES (?,?,?,?)";
	private final String SQL_INSERT_WAYNODES_TMP = "INSERT INTO waynodes_tmp (way_id, waynode_sequence, latitude, longitude) VALUES (?,?,?,?)";
	private final String SQL_SELECT_INNERWAYNODES = "SELECT latitude,longitude FROM waynodes_tmp WHERE way_id = ?";
	private final String SQL_INSERT_METADATA = "INSERT INTO metadata (maxlat,minlon,minlat,maxlon,date,import_version,base_zoom_level,base_zoom_level_low,tile_size,min_zoom_level,max_zoom_level,min_zoom_level_low,max_zoom_level_low) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private final String SQL_UPDATE_WAYTYPE_FLAG = "UPDATE ways SET way_type = 3 WHERE id = ?";
	private final String SQL_UPDATE_OPEN_WAYTYPE_FLAG = "UPDATE ways SET way_type = 0 WHERE id = ?";
	private final String SQL_INSERT_POI_TILE = "INSERT INTO pois_to_tiles (poi_id,tile_x,tile_y,zoom_level) VALUES (?,?,?,?)";
	private final String SQL_INSERT_WAY_TILE = "INSERT INTO ways_to_tiles (way_id,tile_x,tile_y,tile_bitmask,zoom_level) VALUES (?,?,?,?,?)";

	private final String SQL_INSERT_POI_TILE_LESS = "INSERT INTO pois_to_tiles_low (poi_id,tile_x,tile_y,zoom_level) VALUES (?,?,?,?)";
	private final String SQL_INSERT_WAY_TILE_LESS = "INSERT INTO ways_to_tiles_low (way_id,tile_x,tile_y,tile_bitmask,zoom_level) VALUES (?,?,?,?,?)";

	private final String SQL_SELECT_WAY_WITH_TAGS = "SELECT * FROM ways WHERE id = ?";
	private final String SQL_WAY_NODE_DIFF = "INSERT INTO waynodes_diff (way_id,waynode_sequence,diff_lat,diff_lon) VALUES (?,?,?,?)";
	private final String SQL_UPDATE_INNER_WAY_AMOUNT = "UPDATE ways SET inner_way_amount = ? WHERE id = ?";

	private final String SQL_GET_INNER_WAY_TAGS = "SELECT tags, tags_amount FROM ways WHERE id =?";
	private final String SQL_UPDATE_INNER_WAY_TAGS = "UPDATE ways SET tags = ?, tags_amount = ? WHERE id = ?";

	private static byte zoom_high;
	private static byte zoom_low;

	private static byte minZoomHigh;
	private static byte maxZoomHigh;

	private static byte minZoomLow;
	private static byte maxZoomLow;

	private static long DATE;

	// prepared statements
	private PreparedStatement pstmtPoisTmp;
	private PreparedStatement pstmtInsertWayNodes;
	private PreparedStatement pstmtInsertWayNodesTmp;
	private PreparedStatement pstmtWays;
	private PreparedStatement pstmtWayTag;
	private PreparedStatement pstmtMultipolygons;
	private PreparedStatement pstmtInnerWays;
	private PreparedStatement pstmtMetadata;
	private PreparedStatement pstmtUpdateWayType;
	private PreparedStatement pstmtUpdateOpenWayType;
	private PreparedStatement pstmtPoisTiles;
	private PreparedStatement pstmtWaysTiles;
	private PreparedStatement pstmtPoisTags;
	private PreparedStatement pstmtPoisTag;
	private PreparedStatement pstmtWaysWithTags;
	private PreparedStatement pstmtWayNodeDiff;
	private PreparedStatement pstmtUpdateWayInnerWayAmount;
	private PreparedStatement pstmtPoisTilesLessData;
	private PreparedStatement pstmtWaysTilesLessData;
	private PreparedStatement pstmtInnerWayTags;
	private PreparedStatement pstmtUpdateInnerWayTags;

	// result sets
	private ResultSet rsWayNodes;
	private ResultSet rsInnerWays;
	private ResultSet rsWaysWithTags;
	private ResultSet rsInnerWayTags;

	private boolean poisBatched = false;
	private boolean waysBatched = false;

	private double latitudeDouble;
	private double longitudeDouble;

	private Connection conn;
	private Coordinate wayNodeCoord;

	private Geometry geoWay;

	private int innerWayAmount;
	private int latitude;
	private int longitude;
	private int maxlat;
	private int maxlon;
	private int minlat;
	private int minlon;
	private int multipolygons;
	private int nodes;
	private int outerWayAmount;
	private int poiBatchSize;
	private int tagAmount;
	private int wayBatchSize;
	private int wayNodeSequence;
	private int ways;
	private int wayType;
	private int wayNodeAmount;

	private int[] innerWayNodes;

	private Iterator<String> tagIterator;

	private long outerWayId;
	private long startTime;

	private Map<String, Byte> poiTagWhiteList;
	private Map<String, Byte> wayTagWhiteList;
	private Map<Tile, Short> wayTilesMap;

	private MapElementNode currentNode;
	private MapElementWay currentWay;

	private short nameLength;
	private short renderedTagsAmountNode;
	private short renderedTagsAmountWay;

	private Set<Entry<Tile, Short>> wayTilesEntries;
	private Set<Tile> wayTiles;

	private String innerWayTags;
	private String newInnerWayTags;
	private String outerWayTags;
	private String storedTags;
	private String tag;
	private String wayNodeSql;
	private String[] singleTag;
	private String[] splittedTags;

	private StringBuffer sb;

	Tile mainTileForPOI;

	private TLongArrayList currentInnerWays;
	private TLongArrayList currentOuterWays;
	private TLongArrayList currentWayNodes;
	private TLongObjectHashMap<Coordinate> wayNodesMap;

	private Vector<String> currentTags;
	private Vector<String> tagList;

	XML2PostgreSQLMap(String propertiesFile, short baseZoom) throws Exception {

		Properties props = new Properties();
		props.load(new FileInputStream(propertiesFile));

		DBConnection dbConnection = new DBConnection(propertiesFile);

		zoom_high = (byte) baseZoom;
		minZoomHigh = (byte) (zoom_high - 2);
		maxZoomHigh = (byte) (zoom_high + 3);

		zoom_low = (byte) (zoom_high - 6);
		minZoomLow = (byte) (zoom_low - 2);
		maxZoomLow = (byte) (zoom_low + 3);

		currentTags = new Vector<String>();
		currentWayNodes = new TLongArrayList();
		currentOuterWays = new TLongArrayList();
		currentInnerWays = new TLongArrayList();

		wayTiles = new HashSet<Tile>();

		wayNodesMap = new TLongObjectHashMap<Coordinate>();

		poiBatchSize = Integer.parseInt(props.getProperty("import.poiBatchSize",
				DEFAULT_BATCH_SIZE_POI));

		wayBatchSize = Integer.parseInt(props.getProperty("import.wayBatchSize",
				DEFAULT_BATCH_SIZE_WAY));

		sb = new StringBuffer();

		conn = dbConnection.getConnection();

		conn.setAutoCommit(false);

		logger.info("truncating tables");

		// truncate all tables
		conn.createStatement().execute("TRUNCATE TABLE multipolygons CASCADE");
		conn.createStatement().execute("TRUNCATE TABLE waynodes CASCADE");
		conn.createStatement().execute("TRUNCATE TABLE ways CASCADE");
		conn.createStatement().execute("TRUNCATE TABLE pois CASCADE");
		conn.createStatement().execute("TRUNCATE TABLE metadata CASCADE");
		conn.createStatement().execute("TRUNCATE TABLE pois_to_tiles CASCADE");
		conn.createStatement().execute("TRUNCATE TABLE ways_to_tiles CASCADE");
		conn.createStatement().execute("TRUNCATE TABLE pois_tags CASCADE");
		conn.createStatement().execute("TRUNCATE TABLE ways_tags CASCADE");

		// drop existing indices
		conn.createStatement().execute("DROP INDEX IF EXISTS pois_tags_idx");
		conn.createStatement().execute("DROP INDEX IF EXISTS ways_tags_idx");
		conn.createStatement().execute("DROP INDEX IF EXISTS pois_to_tiles_idx");
		conn.createStatement().execute("DROP INDEX IF EXISTS ways_to_tiles_idx");
		conn.createStatement().execute("DROP INDEX IF EXISTS pois_to_tiles_id_idx");
		conn.createStatement().execute("DROP INDEX IF EXISTS ways_to_tiles_id_idx");
		conn.createStatement().execute("DROP INDEX IF EXISTS ways_to_tiles_id_tile_idx");
		conn.createStatement().execute("DROP INDEX IF EXISTS waynodes_id_idx");
		conn.createStatement().execute("DROP INDEX IF EXISTS waynodes_id_sequence_idx");
		conn.createStatement().execute("DROP INDEX IF EXISTS multipolygons_idx");

		conn.commit();

		conn.createStatement().execute("SET CONSTRAINTS ALL DEFERRED");

		// create temporary tables
		conn.createStatement().execute(
				"CREATE TEMPORARY TABLE pois_tmp (id bigint NOT NULL,"
						+ "latitude bigint,longitude bigint,name_length smallint,"
						+ "name text,tags_amount smallint,tags text,layer smallint,"
						+ "elevation integer,housenumber text)");

		conn.createStatement().execute("ALTER TABLE pois_tmp OWNER TO osm");

		conn.createStatement().execute(
				"ALTER TABLE ONLY pois_tmp ADD CONSTRAINT pois_tmp_pkey PRIMARY KEY (id)");

		conn.createStatement().execute(
				"CREATE TEMPORARY TABLE waynodes_tmp (way_id bigint,"
						+ "waynode_sequence smallint,latitude bigint,longitude bigint)");

		conn.createStatement().execute("ALTER TABLE waynodes_tmp OWNER TO osm");

		conn
				.createStatement()
				.execute(
						"ALTER TABLE ONLY waynodes_tmp ADD CONSTRAINT waynodes_tmp_pkey PRIMARY KEY (way_id,waynode_sequence)");

		// prepare all needed sql statements
		pstmtPoisTmp = conn.prepareStatement(SQL_INSERT_POIS_TMP);
		pstmtInsertWayNodes = conn.prepareStatement(SQL_INSERT_WAYNODES);
		pstmtInsertWayNodesTmp = conn.prepareStatement(SQL_INSERT_WAYNODES_TMP);
		pstmtWays = conn.prepareStatement(SQL_INSERT_WAYS);
		pstmtWayTag = conn.prepareStatement(SQL_INSERT_WAY_TAG);
		pstmtMultipolygons = conn.prepareStatement(SQL_INSERT_MULTIPOLYGONS);
		pstmtInnerWays = conn.prepareStatement(SQL_SELECT_INNERWAYNODES);
		pstmtMetadata = conn.prepareStatement(SQL_INSERT_METADATA);
		pstmtUpdateWayType = conn.prepareStatement(SQL_UPDATE_WAYTYPE_FLAG);
		pstmtUpdateOpenWayType = conn.prepareStatement(SQL_UPDATE_OPEN_WAYTYPE_FLAG);
		pstmtPoisTiles = conn.prepareStatement(SQL_INSERT_POI_TILE);
		pstmtWaysTiles = conn.prepareStatement(SQL_INSERT_WAY_TILE);
		pstmtPoisTags = conn.prepareStatement(SQL_INSERT_POIS);
		pstmtPoisTag = conn.prepareStatement(SQL_INSERT_POI_TAG);
		pstmtWaysWithTags = conn.prepareStatement(SQL_SELECT_WAY_WITH_TAGS);
		pstmtWayNodeDiff = conn.prepareStatement(SQL_WAY_NODE_DIFF);
		pstmtUpdateWayInnerWayAmount = conn.prepareStatement(SQL_UPDATE_INNER_WAY_AMOUNT);
		pstmtPoisTilesLessData = conn.prepareStatement(SQL_INSERT_POI_TILE_LESS);
		pstmtWaysTilesLessData = conn.prepareStatement(SQL_INSERT_WAY_TILE_LESS);
		pstmtInnerWayTags = conn.prepareStatement(SQL_GET_INNER_WAY_TAGS);
		pstmtUpdateInnerWayTags = conn.prepareStatement(SQL_UPDATE_INNER_WAY_TAGS);

		logger.info("database connection setup done...");

	}

	@Override
	protected void finalize() throws Throwable {
		// close the database connection
		if (!conn.isClosed())
			conn.close();
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();

		poiTagWhiteList = WhiteList.getNodeTagWhitelist();
		wayTagWhiteList = WhiteList.getWayTagWhitelist();

		logger.info("start reading file");
		startTime = System.currentTimeMillis();
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();

		logger.info("executing last batches...");

		try {
			// write metadata to the database
			pstmtMetadata.setInt(1, maxlat);
			pstmtMetadata.setInt(2, minlon);
			pstmtMetadata.setInt(3, minlat);
			pstmtMetadata.setInt(4, maxlon);
			pstmtMetadata.setLong(5, DATE);
			pstmtMetadata.setInt(6, VERSION);
			pstmtMetadata.setShort(7, zoom_high);
			pstmtMetadata.setShort(8, zoom_low);
			pstmtMetadata.setInt(9, Tile.TILE_SIZE);
			pstmtMetadata.setShort(10, minZoomHigh);
			pstmtMetadata.setShort(11, maxZoomHigh);
			pstmtMetadata.setShort(12, minZoomLow);
			pstmtMetadata.setShort(13, maxZoomLow);
			pstmtMetadata.execute();

			logger.info("metadata inserted");

			logger.info("execute last multipolygon batches");

			// execute very last batches
			pstmtMultipolygons.executeBatch();
			pstmtUpdateWayType.executeBatch();
			pstmtUpdateWayInnerWayAmount.executeBatch();
			pstmtUpdateInnerWayTags.executeBatch();

			logger.info("create indices on tag tables");

			// create indices for:
			// pois/ways_tags
			conn.createStatement().execute("CREATE INDEX pois_tags_idx ON pois_tags (tag)");
			conn.createStatement().execute("CREATE INDEX ways_tags_idx ON ways_tags(tag)");

			// pois/ways_to_tiles
			conn.createStatement().execute(
					"CREATE INDEX pois_to_tiles_idx ON pois_to_tiles(tile_x,tile_y)");
			conn.createStatement().execute(
					"CREATE INDEX ways_to_tiles_idx ON ways_to_tiles(tile_x,tile_y)");

			conn.createStatement().execute(
					"CREATE INDEX pois_to_tiles_id_idx ON pois_to_tiles(poi_id)");
			conn.createStatement().execute(
					"CREATE INDEX ways_to_tiles_id_idx ON ways_to_tiles(way_id)");

			conn
					.createStatement()
					.execute(
							"CREATE INDEX ways_to_tiles_id_tile_idx ON ways_to_tiles(way_id, tile_x, tile_y)");

			// way nodes
			conn.createStatement().execute("CREATE INDEX waynodes_id_idx ON waynodes(way_id)");
			conn
					.createStatement()
					.execute(
							"CREATE INDEX waynodes_id_sequence_idx ON waynodes(way_id,waynode_sequence)");

			// multipolygons
			conn.createStatement().execute(
					"CREATE INDEX multipolygons_idx ON multipolygons(outer_way_id)");

			logger.info("created indices on tag tables");

			// truncate temporary tables
			conn.createStatement().execute("TRUNCATE TABLE waynodes_tmp CASCADE");
			conn.createStatement().execute("TRUNCATE TABLE pois_tmp CASCADE");

			logger.info("committing transaction...");

			conn.commit();

		} catch (SQLException e) {
			logger.info("SQLException: " + e.getMessage());
			while ((e = e.getNextException()) != null) {
				logger.info(e.getMessage());
			}
		}

		logger
				.info("processing took " + (System.currentTimeMillis() - startTime) / 1000
						+ "s.");

		logger.info("inserted " + nodes + " nodes");
		logger.info("inserted " + ways + " ways");
		logger.info("inserted " + multipolygons + " multipolygons");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {

		// get bounding box coordinates
		if (qName.equals("bound")) {
			String bbox = attributes.getValue("box");
			String[] coordinates = bbox.split(",");
			minlat = (int) (Double.parseDouble(coordinates[0]) * 1000000);
			minlon = (int) (Double.parseDouble(coordinates[1]) * 1000000);
			maxlat = (int) (Double.parseDouble(coordinates[2]) * 1000000);
			maxlon = (int) (Double.parseDouble(coordinates[3]) * 1000000);
		}

		if (qName.equals("bounds")) {
			minlat = (int) (Double.parseDouble(attributes.getValue("minlat")) * 1000000);
			minlon = (int) (Double.parseDouble(attributes.getValue("minlon")) * 1000000);
			maxlat = (int) (Double.parseDouble(attributes.getValue("maxlat")) * 1000000);
			maxlon = (int) (Double.parseDouble(attributes.getValue("maxlon")) * 1000000);
		}

		if (qName.equals("node")) {
			// start processing node
			currentTags.clear();
			// get id and coordinates
			long id = Long.parseLong(attributes.getValue("id"));
			latitude = (int) (Double.parseDouble(attributes.getValue("lat")) * 1000000);
			longitude = (int) (Double.parseDouble(attributes.getValue("lon")) * 1000000);
			currentNode = new MapElementNode(id, latitude, longitude);

		} else if (qName.equals("way")) {
			// before processing the first way execute last poi batches
			if (!poisBatched) {
				try {
					pstmtPoisTmp.executeBatch();
					pstmtPoisTags.executeBatch();
					pstmtPoisTiles.executeBatch();
					pstmtPoisTilesLessData.executeBatch();
					pstmtPoisTag.executeBatch();
					logger.info("last pois batched");
				} catch (SQLException e) {
					System.err.println(pstmtPoisTmp);
					e.printStackTrace();
				}
				poisBatched = true;
			}

			// start processing way
			currentTags.clear();
			currentWayNodes.clear();

			// get id
			currentWay = new MapElementWay(Integer.parseInt(attributes.getValue("id")));

		} else if (qName.equals("relation")) {
			// before processing the first relation execute last way batches
			if (!waysBatched) {
				try {
					pstmtWays.executeBatch();
					pstmtInsertWayNodes.executeBatch();
					pstmtInsertWayNodesTmp.executeBatch();
					pstmtWaysTiles.executeBatch();
					pstmtWaysTilesLessData.executeBatch();
					pstmtWayNodeDiff.executeBatch();
					pstmtWayTag.executeBatch();
					logger.info("last ways batched");
				} catch (SQLException e) {
					System.err.println(pstmtWays);
					System.err.println(pstmtInsertWayNodes);
					e.printStackTrace();
					if (e.getNextException() != null) {
						System.out.println(e.getNextException().getMessage());
					}
				}
				waysBatched = true;
			}
			currentOuterWays.clear();
			currentInnerWays.clear();
		} else if (qName.equals("tag")) {
			// get the tags for the current element
			if (!"created_by".equals(attributes.getValue("k"))) {
				tag = attributes.getValue("k");
				tag += "=";
				tag += attributes.getValue("v");
				currentTags.add(tag);
			}
		} else if (qName.equals("nd")) {
			// get the ids of the way nodes
			currentWayNodes.add(Long.parseLong(attributes.getValue("ref")));
		} else if (qName.equals("member")) {
			// only multipolygons with outer and inner ways are considered
			if (attributes.getValue("type").equals("way")
					&& attributes.getValue("role").equals("outer")) {
				currentOuterWays.add(Integer.parseInt(attributes.getValue("ref")));
			} else if (attributes.getValue("type").equals("way")
					&& attributes.getValue("role").equals("inner")) {
				currentInnerWays.add(Long.parseLong(attributes.getValue("ref")));
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		storedTags = "";

		if (qName.equals("node")) {
			processNode();
		} else if (qName.equals("way")) {
			processWay();
		} else if (qName.equals("relation")) {
			processRelation();
		}
	}

	private void processNode() {
		try {
			nodes++;
			pstmtPoisTmp.setLong(1, currentNode.id);
			pstmtPoisTmp.setInt(2, currentNode.latitude);
			pstmtPoisTmp.setInt(3, currentNode.longitude);

			tagIterator = currentTags.iterator();
			tagAmount = currentTags.size();
			splittedTags = new String[2];

			tagList = new Vector<String>();
			renderedTagsAmountNode = 0;
			while (tagIterator.hasNext()) {
				tag = tagIterator.next();
				splittedTags = tag.split("=");

				if (splittedTags.length == 2) {
					// special tags like elevation, house number, name and layer are stored
					// in separate fields in the database
					if (splittedTags[0].equals("ele")) {
						try {
							currentNode.elevation = (int) Double
									.parseDouble(splittedTags[1]);
							if (currentNode.elevation > 32000) {
								currentNode.elevation = 32000;
							}
						} catch (NumberFormatException e) {
							currentNode.elevation = 0;
						}
					} else if (splittedTags[0].equals("addr:housenumber")
							&& splittedTags[1].getBytes().length <= 128) {
						currentNode.housenumber = splittedTags[1];
					} else if (splittedTags[0].equals("name")
							&& splittedTags[1].getBytes().length <= 128) {
						currentNode.name = splittedTags[1];
					} else if (splittedTags[0].equals("layer")) {
						try {
							currentNode.layer = Integer.parseInt(splittedTags[1]);
							if (currentNode.layer >= -5 && currentNode.layer <= 5)
								currentNode.layer += 5;
							else
								currentNode.layer = 5;
						} catch (NumberFormatException e) {
							currentNode.layer = 5;
						}
					} else if (poiTagWhiteList.containsKey(tag)) {
						// if current tag is in the white list, add it to the temporary tag
						// list
						currentNode.zoomLevel = poiTagWhiteList.get(tag);
						if (currentNode.zoomLevel == Byte.MAX_VALUE) {
							currentNode.zoomLevel = maxZoomHigh;
						}
						tagList.add(tag);

						renderedTagsAmountNode++;
					}
				}
				tagAmount--;
			}

			// create one string of tags. tags are separated with a line break
			for (int k = 0; k < renderedTagsAmountNode; k++) {
				sb.append(tagList.get(k));
				sb.append("\n");
			}

			storedTags = sb.toString();
			sb.delete(0, sb.length());

			if (currentNode.name != null) {
				nameLength = (short) currentNode.name.getBytes().length;
			} else {
				nameLength = 0;
			}

			// all nodes are stored in the temporary poi table
			pstmtPoisTmp.setInt(4, nameLength);
			pstmtPoisTmp.setString(5, currentNode.name);
			pstmtPoisTmp.setShort(6, renderedTagsAmountNode);
			pstmtPoisTmp.setString(7, storedTags);
			pstmtPoisTmp.setInt(8, currentNode.layer);
			pstmtPoisTmp.setInt(9, currentNode.elevation);
			pstmtPoisTmp.setString(10, currentNode.housenumber);
			pstmtPoisTmp.addBatch();

			// if the node has tags it is a poi and it is stored in the persistent poi table
			if (!storedTags.equals("") || !currentNode.housenumber.equals("")) {
				pstmtPoisTags.setLong(1, currentNode.id);
				pstmtPoisTags.setInt(2, currentNode.latitude);
				pstmtPoisTags.setInt(3, currentNode.longitude);
				pstmtPoisTags.setShort(4, nameLength);
				pstmtPoisTags.setString(5, currentNode.name);
				pstmtPoisTags.setShort(6, renderedTagsAmountNode);
				pstmtPoisTags.setString(7, storedTags);
				pstmtPoisTags.setInt(8, currentNode.layer);
				pstmtPoisTags.setInt(9, currentNode.elevation);
				pstmtPoisTags.setString(10, currentNode.housenumber);
				pstmtPoisTags.addBatch();

				// to execute fast filtering of elements every tag is stored together with
				// the element id in a tag table
				singleTag = storedTags.split("\n");
				for (String s : singleTag) {
					pstmtPoisTag.setLong(1, currentNode.id);
					pstmtPoisTag.setString(2, s);
					pstmtPoisTag.addBatch();
				}

				// calculate the tile in which the poi is located
				mainTileForPOI = new Tile(MercatorProjection.longitudeToTileX(
						(double) currentNode.longitude / 1000000, zoom_high),
						MercatorProjection.latitudeToTileY(
								(double) currentNode.latitude / 1000000, zoom_high),
						zoom_high);

				pstmtPoisTiles.setLong(1, currentNode.id);
				pstmtPoisTiles.setLong(2, mainTileForPOI.x);
				pstmtPoisTiles.setLong(3, mainTileForPOI.y);
				pstmtPoisTiles.setShort(4, currentNode.zoomLevel);
				pstmtPoisTiles.addBatch();

				if (currentNode.zoomLevel < minZoomHigh) { // minimal zoom of higher base zoom
					mainTileForPOI = new Tile(MercatorProjection.longitudeToTileX(
							(double) currentNode.longitude / 1000000, zoom_low),
							MercatorProjection.latitudeToTileY(
									(double) currentNode.latitude / 1000000, zoom_low),
							zoom_low); // lower base zoom

					pstmtPoisTilesLessData.setLong(1, currentNode.id);
					pstmtPoisTilesLessData.setLong(2, mainTileForPOI.x);
					pstmtPoisTilesLessData.setLong(3, mainTileForPOI.y);
					pstmtPoisTilesLessData.setShort(4, currentNode.zoomLevel);
					pstmtPoisTilesLessData.addBatch();
				}
			}

			poisBatched = false;
			if (nodes % poiBatchSize == 0) {
				pstmtPoisTmp.executeBatch();
				pstmtPoisTags.executeBatch();
				pstmtPoisTiles.executeBatch();
				pstmtPoisTilesLessData.executeBatch();
				pstmtPoisTag.executeBatch();
				logger.info("executed batch for nodes " + (nodes - poiBatchSize) + "-"
						+ nodes);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			if (e.getNextException() != null) {
				System.out.println(e.getNextException().getMessage());
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

	}

	private void processWay() {
		try {
			wayNodeSequence = 1;
			latitude = 0;
			longitude = 0;

			ways++;

			tagIterator = currentTags.iterator();
			tagAmount = currentTags.size();
			tagList = new Vector<String>();
			renderedTagsAmountWay = 0;
			splittedTags = new String[2];

			wayNodeSql = "";
			wayNodesMap.clear();

			while (tagIterator.hasNext()) {
				tag = tagIterator.next();
				splittedTags = tag.split("=");

				if (splittedTags.length == 2) {
					// special tags like name and layer are stored in separate fields in the
					// database
					if (splittedTags[0].equals("name")
							&& splittedTags[1].getBytes().length <= 128) {
						currentWay.name = splittedTags[1];
					} else if (splittedTags[0].equals("layer")) {
						try {
							currentWay.layer = (byte) (Integer.parseInt(splittedTags[1]));
							if (currentWay.layer >= -5 && currentWay.layer <= 5)
								currentWay.layer += 5;
							else
								currentWay.layer = 5;
						} catch (NumberFormatException e) {
							currentWay.layer = 5;
						}
					} else if (splittedTags[0].equals("ref")) {
						currentWay.ref = splittedTags[1];
					} else if (wayTagWhiteList.containsKey(tag)) {
						// if current tag is in the white list, add it to the temporary tag
						// list
						currentWay.zoomLevel = wayTagWhiteList.get(tag);
						if (currentWay.zoomLevel == Byte.MAX_VALUE) {
							currentWay.zoomLevel = maxZoomHigh;
						}
						tagList.add(tag);

						renderedTagsAmountWay++;
					}
				}
				tagAmount--;
			}

			// create one string of tags
			// tags are separated with a line break
			for (int k = 0; k < renderedTagsAmountWay; k++) {
				sb.append(tagList.get(k));
				sb.append("\n");
			}

			storedTags = sb.toString();
			sb.delete(0, sb.length());

			wayNodeAmount = currentWayNodes.size();

			if (wayNodeAmount > 1) {
				// mark the way as area if the first and the last way node are the same and
				// if the way has more than two way nodes
				if ((currentWayNodes.get(0) == currentWayNodes.get(wayNodeAmount - 1))
						&& !(currentWayNodes.size() == 2)) {
					currentWay.wayType = (short) 2;
				}

				Coordinate[] wayNodes = new Coordinate[wayNodeAmount];
				int[] tempWayNodes = new int[wayNodeAmount * 2];

				int tmpWnSize = wayNodeAmount;

				int counterForWayNodes = 0;

				for (int i = 0; i < wayNodeAmount; i++) {
					sb.append(currentWayNodes.get(i));
					sb.append(",");
				}

				wayNodeSql = sb.toString();
				sb.delete(0, sb.length());

				// get the way nodes from the temporary poi table
				rsWayNodes = conn.createStatement().executeQuery(
						SQL_SELECT_WAYNODES + "("
								+ wayNodeSql.substring(0, wayNodeSql.length() - 1) + ")");
				while (rsWayNodes.next()) {

					latitude = rsWayNodes.getInt("latitude");
					longitude = rsWayNodes.getInt("longitude");

					latitudeDouble = (double) latitude / 1000000;
					longitudeDouble = (double) longitude / 1000000;

					wayNodesMap.put(rsWayNodes.getLong("id"), new Coordinate(latitudeDouble,
							longitudeDouble));
				}

				for (int i = 0; i < wayNodeAmount; i++) {
					tmpWnSize--;
					wayNodeCoord = wayNodesMap.get(currentWayNodes.get(i));
					wayNodes[i] = wayNodeCoord;

					tempWayNodes[counterForWayNodes] = (int) (wayNodeCoord.x * 1000000);
					tempWayNodes[counterForWayNodes + 1] = (int) (wayNodeCoord.y * 1000000);
					counterForWayNodes += 2;
				}

				if (tmpWnSize == 0) {
					wayNodeSequence = 0;
					for (int i = 0; i < tempWayNodes.length; i += 2) {
						// store all way nodes in a temporary way node table
						pstmtInsertWayNodesTmp.setLong(1, currentWay.id);
						pstmtInsertWayNodesTmp.setInt(2, wayNodeSequence);
						pstmtInsertWayNodesTmp.setInt(3, tempWayNodes[i]);
						pstmtInsertWayNodesTmp.setInt(4, tempWayNodes[i + 1]);
						pstmtInsertWayNodesTmp.addBatch();

						if (storedTags != "") {
							// if the way has tags store the way nodes in a persistent way
							// node table
							pstmtInsertWayNodes.setLong(1, currentWay.id);
							pstmtInsertWayNodes.setInt(2, wayNodeSequence);
							pstmtInsertWayNodes.setInt(3, tempWayNodes[i]);
							pstmtInsertWayNodes.setInt(4, tempWayNodes[i + 1]);
							pstmtInsertWayNodes.addBatch();
						}
						wayNodeSequence++;
					}

					// store only ways with tags in the persistent table
					if (storedTags != "") {
						// to execute fast filtering of elements every tag is stored
						// together with the element id in a tag table
						singleTag = storedTags.split("\n");
						for (String s : singleTag) {
							pstmtWayTag.setLong(1, currentWay.id);
							pstmtWayTag.setString(2, s);
							pstmtWayTag.addBatch();
						}

						// create a geometry object for the way
						geoWay = Utils.createWay(currentWay, wayNodes);

						// calculate all tiles which are related to a way
						wayTiles = Utils.wayToTilesWay(geoWay, currentWay.wayType,
								zoom_high);

						// calculate all sub tiles of the tiles which are related to the way
						// and create the tile bit mask
						wayTilesMap = Utils.getTileBitMask(geoWay, wayTiles,
								(short) currentWay.wayType);

						wayTilesEntries = wayTilesMap.entrySet();
						for (Entry<Tile, Short> entry : wayTilesEntries) {
							pstmtWaysTiles.setLong(1, currentWay.id);
							pstmtWaysTiles.setLong(2, entry.getKey().x);
							pstmtWaysTiles.setLong(3, entry.getKey().y);
							pstmtWaysTiles.setInt(4, entry.getValue());
							pstmtWaysTiles.setShort(5, currentWay.zoomLevel);
							pstmtWaysTiles.addBatch();
						}

						if (currentWay.zoomLevel < minZoomHigh) {
							// create a geometry object for the way
							geoWay = Utils.createWay(currentWay, wayNodes);

							// calculate all tiles which are related to a way
							wayTiles = Utils.wayToTilesWay(geoWay, currentWay.wayType,
									zoom_low); // lower base zoom

							// calculate all sub tiles of the tiles which are related to the
							// way and create the tile bit mask
							wayTilesMap = Utils.getTileBitMask(geoWay, wayTiles,
									(short) currentWay.wayType);

							wayTilesEntries = wayTilesMap.entrySet();
							for (Entry<Tile, Short> entry : wayTilesEntries) {
								pstmtWaysTilesLessData.setLong(1, currentWay.id);
								pstmtWaysTilesLessData.setLong(2, entry.getKey().x);
								pstmtWaysTilesLessData.setLong(3, entry.getKey().y);
								pstmtWaysTilesLessData.setInt(4, entry.getValue());
								pstmtWaysTilesLessData.setShort(5, currentWay.zoomLevel);
								pstmtWaysTilesLessData.addBatch();
							}
						}

						pstmtWays.setLong(1, currentWay.id);
						if (currentWay.name != null) {
							pstmtWays.setInt(2, currentWay.name.getBytes().length);
						} else {
							pstmtWays.setInt(2, 0);
						}
						pstmtWays.setString(3, currentWay.name);
						pstmtWays.setInt(4, renderedTagsAmountWay);
						pstmtWays.setString(5, storedTags);
						pstmtWays.setInt(6, currentWay.layer);
						pstmtWays.setInt(7, wayNodeAmount);
						pstmtWays.setInt(8, currentWay.wayType);
						pstmtWays.setInt(9, currentWay.convexness);
						pstmtWays.setInt(10, 0);
						pstmtWays.setInt(11, 0);
						// inner way amount if way is outer way of a multipolygon
						pstmtWays.setInt(12, 0);
						pstmtWays.setString(13, currentWay.ref);
						pstmtWays.addBatch();
						waysBatched = false;
					}
				}
			}
			if (ways % wayBatchSize == 0) {
				pstmtWays.executeBatch();
				pstmtInsertWayNodes.executeBatch();
				pstmtInsertWayNodesTmp.executeBatch();
				pstmtWayNodeDiff.executeBatch();
				pstmtWaysTiles.executeBatch();
				pstmtWaysTilesLessData.executeBatch();
				pstmtWayTag.executeBatch();
				logger
						.info("executed batch for ways " + (ways - wayBatchSize) + "-"
								+ ways);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			if (e.getNextException() != null) {
				System.out.println(e.getNextException().getMessage());
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void processRelation() {
		try {
			innerWayTags = "";

			outerWayAmount = currentOuterWays.size();

			if (outerWayAmount != 0) {
				for (int i = 0; i < outerWayAmount; i++) {
					// a multipolygon could have multiple outer ways
					outerWayId = currentOuterWays.get(i);

					// get tags of outer way
					pstmtWaysWithTags.setLong(1, outerWayId);
					rsWaysWithTags = pstmtWaysWithTags.executeQuery();
					if (rsWaysWithTags.next()) {
						wayType = rsWaysWithTags.getInt("way_type");
						outerWayTags = rsWaysWithTags.getString("tags");

						// get for every inner way the way nodes
						innerWayAmount = currentInnerWays.size();
						innerWayNodes = new int[innerWayAmount];
						for (int j = 0; j < innerWayAmount; j++) {
							pstmtInnerWays.setLong(1, currentInnerWays.get(j));
							rsInnerWays = pstmtInnerWays.executeQuery();
							wayNodeSequence = 1;
							while (rsInnerWays.next()) {
								try {
									pstmtMultipolygons.setLong(1, outerWayId);
									pstmtMultipolygons.setInt(2, j + 1);
									pstmtMultipolygons.setInt(3, wayNodeSequence);
									pstmtMultipolygons.setLong(4, rsInnerWays
											.getInt("latitude"));
									pstmtMultipolygons.setLong(5, rsInnerWays
											.getInt("longitude"));
									pstmtMultipolygons.addBatch();

									wayNodeSequence++;
								} catch (SQLException e) {
									System.err.println(pstmtMultipolygons);
									throw e;
								}
							}
							innerWayNodes[j] = wayNodeSequence - 1;

							// get the tags of the current inner way
							pstmtInnerWayTags.setLong(1, currentInnerWays.get(j));
							rsInnerWayTags = pstmtInnerWayTags.executeQuery();
							int newTagAmount = 0;
							while (rsInnerWayTags.next()) {
								innerWayTags = rsInnerWayTags.getString("tags");
								newTagAmount = rsInnerWayTags.getShort("tags_amount");
							}

							// if the inner way has a tag with the outer way in common remove it
							// from the list
							splittedTags = innerWayTags.split("\n");
							// newInnerWayTags = "";
							for (String s : splittedTags) {
								if (outerWayTags.indexOf(s) == -1) {
									sb.append(s + "\n");
								} else {
									newTagAmount--;
								}
							}

							newInnerWayTags = sb.toString();
							sb.delete(0, sb.length());
							// if the inner way has no tags anymore remove all attached records
							// in database
							if (newInnerWayTags.equals("")) {
								newTagAmount = 0;
								conn.createStatement().execute(
										"delete from ways_to_tiles where way_id = "
												+ currentInnerWays.get(j));
								conn
										.createStatement()
										.execute(
												"delete from ways_to_tiles_low where way_id = "
														+ currentInnerWays.get(j));
							}
							// if the inner way has no tags anymore remove all attached records
							// in database
							if (newInnerWayTags.equals("\n")) {
								newInnerWayTags = "";
								newTagAmount = 0;
								conn.createStatement().execute(
										"delete from ways_to_tiles where way_id = "
												+ currentInnerWays.get(j));
								conn
										.createStatement()
										.execute(
												"delete from ways_to_tiles_low where way_id = "
														+ currentInnerWays.get(j));
							}
							if (newInnerWayTags.indexOf("=") != -1) {
								newTagAmount = newInnerWayTags.split("\n").length;
							}
							pstmtUpdateInnerWayTags.setString(1, newInnerWayTags);
							pstmtUpdateInnerWayTags.setInt(2, newTagAmount);
							pstmtUpdateInnerWayTags.setLong(3, currentInnerWays.get(j));
							pstmtUpdateInnerWayTags.addBatch();
						}

						// if the inner way has no inner way nodes don't mark the outer way
						// as multipolygon
						boolean noInnerNodes = true;
						for (int k = 0; k < innerWayNodes.length; k++) {
							if (innerWayNodes[k] == 0) {
								noInnerNodes = noInnerNodes && true;
							} else {
								noInnerNodes = noInnerNodes && false;
							}
						}

						// set inner way amount for every outer way
						if (!noInnerNodes) {
							pstmtUpdateWayInnerWayAmount.setInt(1, innerWayAmount);
							pstmtUpdateWayInnerWayAmount.setLong(2, outerWayId);
							pstmtUpdateWayInnerWayAmount.addBatch();
							if (wayType == 2) {
								// outer way is a closed way
								pstmtUpdateWayType.setLong(1, outerWayId);
								pstmtUpdateWayType.addBatch();
							} else if (wayType == 1) {
								// outer way is a "normal" way
								pstmtUpdateOpenWayType.setLong(1, outerWayId);
								pstmtUpdateOpenWayType.addBatch();
							}
						}
					}
					multipolygons++;
				}
			}
			if (multipolygons % batchSizeMP == 0) {
				pstmtMultipolygons.executeBatch();
				pstmtUpdateWayType.executeBatch();
				pstmtUpdateWayInnerWayAmount.executeBatch();
				pstmtUpdateInnerWayTags.executeBatch();
				logger.info("executed batch for multipolygons "
						+ (multipolygons - batchSizeMP) + "-" + multipolygons);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			if (e.getNextException() != null) {
				System.out.println(e.getNextException().getMessage());
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	private static void usage() {
		System.out
				.println("Usage: XML2PostgreSQLMap <properties-file> <osm-file> <base-zoom-level>");
	}

	/**
	 * Main method to start the import of osm data to the database.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {

		if (args.length != 3) {
			usage();
			System.exit(0);
		}

		File file = new File(args[0]);
		if (!file.isFile()) {
			System.out.println("Path is no file.");
			usage();
			System.exit(1);
		}
		try {
			DATE = file.lastModified();

			DefaultHandler saxParser = new XML2PostgreSQLMap(args[0], Short
					.parseShort(args[2]));

			// get a factory
			SAXParserFactory spf = SAXParserFactory.newInstance();

			// get a new instance of parser
			SAXParser sp = spf.newSAXParser();

			// parse the file and also register this class for call backs
			sp.parse(args[1], saxParser);

		} catch (SAXException se) {
			se.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
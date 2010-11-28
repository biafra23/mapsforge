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
package org.mapsforge.preprocessing.graph.interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * An adapted implementation of the XML2PostgreSQL class
 * 
 * @author kunis
 */
public class SimpleOSM2DBParser {

	private final File osm;
	private final XMLReader xmlReader;

	/**
	 * The constructor of the parser.
	 * 
	 * @param conn
	 *            a database connection
	 * @param osm
	 *            the osm file that should be parsed
	 * @throws SAXException
	 *             if the parsing failed
	 * @throws SQLException
	 *             if there are any problems with the database
	 */
	public SimpleOSM2DBParser(Connection conn, File osm) throws SAXException, SQLException {

		this.osm = osm;
		this.xmlReader = XMLReaderFactory.createXMLReader();
		this.xmlReader.setContentHandler(new OsmHandler(conn));
	}

	/**
	 * The start method of the parser.
	 * 
	 * @throws IOException
	 *             maybe can not read the file
	 * @throws SAXException
	 *             an error while parsing
	 */
	public void parseFile() throws IOException, SAXException {
		FileInputStream is = new FileInputStream(osm);
		xmlReader.parse(new InputSource(is));
		is.close();
	}

	private class OsmHandler extends DefaultHandler {

		private final Logger logger = Logger.getLogger(SimpleOSM2DBParser.class.getName());

		private static final int DEFAULT_BATCH_SIZE = 500;

		private int batchSize;

		private int nodes;
		private int node_tags;

		private int ways;
		private int way_tags;
		private int way_points;

		private int relations;
		// private int relation_points;

		private Node currentNode;
		private Vector<String> currentTags;

		private long currentWay;
		private Vector<Long> currentWayPoints;

		private long startTime;

		private final String SQL_INSERT_NODES = "INSERT INTO nodes (id, int_latitude, int_longitude) VALUES (?,?,?)";
		private final String SQL_INSERT_NODE_TAGS = "INSERT INTO node_tags (id, k, v) VALUES (?,?,?)";

		private final String SQL_INSERT_WAYS = "INSERT INTO ways (id) VALUES (?)";
		private final String SQL_INSERT_WAY_TAGS = "INSERT INTO way_tags (id, k, v) VALUES (?,?,?)";
		private final String SQL_INSERT_WAY_NODES = "INSERT INTO way_nodes (id, node_id, sequence_id) VALUES (?,?,?)";

		// private final String SQL_INSERT_RELATIONS =
		// "INSERT INTO relations (id) VALUES (?)";
		// private final String SQL_INSERT_RELATION_TAGS =
		// "INSERT INTO relation_tags (id, k, v) VALUES (?,?,?)";
		// private final String SQL_INSERT_RELATION_MEMBERS =
		// "INSERT INTO relation_members (id, member_id, member_role, member_type, sequence_id) VALUES (?,?,?,?,?)";

		private PreparedStatement pstmtNodes;
		private PreparedStatement pstmtNodeTags;

		private PreparedStatement pstmtWays;
		private PreparedStatement pstmtWayTags;
		private PreparedStatement pstmtWayNodes;

		// private PreparedStatement pstmtRelations;
		// private PreparedStatement pstmtRelationTags;
		// private PreparedStatement pstmtRelationMembers;

		private Connection conn;

		OsmHandler(Connection conn) throws SQLException {

			currentTags = new Vector<String>();
			currentWayPoints = new Vector<Long>();

			batchSize = DEFAULT_BATCH_SIZE;

			this.conn = conn;
			this.conn.setAutoCommit(false);

			logger.info("truncating tables");

			// conn.createStatement().execute("TRUNCATE TABLE relation_members");
			// conn.createStatement().execute("TRUNCATE TABLE relation_tags");
			// conn.createStatement().execute("TRUNCATE TABLE relations");

			conn.createStatement().execute("TRUNCATE TABLE way_nodes CASCADE");
			conn.createStatement().execute("TRUNCATE TABLE way_tags CASCADE");
			conn.createStatement().execute("TRUNCATE TABLE ways CASCADE");

			conn.createStatement().execute("TRUNCATE TABLE node_tags CASCADE");
			conn.createStatement().execute("TRUNCATE TABLE nodes CASCADE");

			conn.commit();

			conn.createStatement().execute("SET CONSTRAINTS ALL DEFERRED");

			pstmtNodes = conn.prepareStatement(SQL_INSERT_NODES);
			pstmtNodeTags = conn.prepareStatement(SQL_INSERT_NODE_TAGS);

			pstmtWays = conn.prepareStatement(SQL_INSERT_WAYS);
			pstmtWayTags = conn.prepareStatement(SQL_INSERT_WAY_TAGS);
			pstmtWayNodes = conn.prepareStatement(SQL_INSERT_WAY_NODES);

			// pstmtRelations = conn.prepareStatement(SQL_INSERT_RELATIONS);
			// pstmtRelationTags = conn.prepareStatement(SQL_INSERT_RELATION_TAGS);
			// pstmtRelationMembers =
			// conn.prepareStatement(SQL_INSERT_RELATION_MEMBERS);

			logger.info("database connection setup done...");
		}

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) {
			if (qName.equals("node")) {
				currentNode = new Node(Long.parseLong(attributes.getValue("id")), Double
						.parseDouble(attributes.getValue("lat")), Double.parseDouble(attributes
						.getValue("lon")));
				currentTags.clear();

			} else if (qName.equals("way")) {
				currentTags.clear();
				currentWayPoints.clear();

				currentWay = Long.parseLong(attributes.getValue("id"));
			} else if (qName.equals("tag")) {
				if (!"created_by".equals(attributes.getValue("k"))) {
					currentTags.add(attributes.getValue("k"));
					currentTags.add(attributes.getValue("v"));
				}
			} else if (qName.equals("nd")) {
				currentWayPoints.add(Long.parseLong(attributes.getValue("ref")));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			try {
				if (qName.equals("node")) {
					nodes++;
					pstmtNodes.setLong(1, currentNode.getId());
					// pstmtNodes.setDouble(2, currentNode.latitude);
					// pstmtNodes.setDouble(3, currentNode.longitude);
					pstmtNodes.setInt(2, GeoCoordinate.doubleToInt(currentNode.getLatitude()));

					pstmtNodes.setInt(3, GeoCoordinate.doubleToInt(currentNode.getLongitude()));

					pstmtNodes.addBatch();

					String k, v;
					Iterator<String> it = currentTags.iterator();
					while (it.hasNext()) {
						node_tags++;

						k = it.next();
						v = it.next();
						pstmtNodeTags.setLong(1, currentNode.getId());
						pstmtNodeTags.setString(2, k);
						pstmtNodeTags.setString(3, v);
						pstmtNodeTags.addBatch();
					}

					if (nodes % batchSize == 0) {
						pstmtNodes.executeBatch();
						logger.info("executed batch for nodes " + (nodes - batchSize) + "-"
								+ nodes);
					}
					if (node_tags % batchSize == 0) {
						pstmtNodeTags.executeBatch();
						// logger.info("executed batch for node tags " + (node_tags
						// - DEFAULT_BATCH_SIZE) + "-" + node_tags);
					}
				} else if (qName.equals("way")) {
					ways++;

					// System.out.println(ways);
					pstmtWays.setLong(1, currentWay);
					pstmtWays.addBatch();

					String k, v;
					Iterator<String> itWayTags = currentTags.iterator();
					while (itWayTags.hasNext()) {
						way_tags++;

						k = itWayTags.next();
						v = itWayTags.next();
						pstmtWayTags.setLong(1, currentWay);
						pstmtWayTags.setString(2, k);
						pstmtWayTags.setString(3, v);
						pstmtWayTags.addBatch();
					}

					int sequenceID = 1;
					for (Long nodeID : currentWayPoints) {
						way_points++;

						pstmtWayNodes.setLong(1, currentWay);
						pstmtWayNodes.setLong(2, nodeID);
						pstmtWayNodes.setInt(3, sequenceID++);
						pstmtWayNodes.addBatch();
					}

					if (ways % batchSize == 0) {
						pstmtWays.executeBatch();
						logger.info("executed batch for ways " + (ways - batchSize) + "-"
								+ ways);
					}
					if (way_tags % batchSize == 0) {
						pstmtWayTags.executeBatch();
						// logger.info("executed batch for way tags " + (way_tags -
						// DEFAULT_BATCH_SIZE) + "-" + way_tags);
					}
					if (way_points % batchSize == 0) {
						pstmtWayNodes.executeBatch();
						// logger.info("executed batch for way points " +
						// (way_points - DEFAULT_BATCH_SIZE) + "-" + way_points);
					}
				} else if (qName.equals("relation")) {
					relations++;
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

		@Override
		public void endDocument() throws SAXException {
			super.endDocument();
			try {
				logger.info("executing last batches...");
				pstmtNodes.executeBatch();
				pstmtNodeTags.executeBatch();

				pstmtWays.executeBatch();
				pstmtWayTags.executeBatch();
				pstmtWayNodes.executeBatch();

				logger.info("committing transaction...");
				conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			logger.info("processing took " + (System.currentTimeMillis() - startTime) / 1000
					+ "s.");

			logger.info("inserted " + nodes + " nodes");
			logger.info("inserted " + ways + " ways");
			logger.info("inserted " + way_points + " way points");
		}
	}
}

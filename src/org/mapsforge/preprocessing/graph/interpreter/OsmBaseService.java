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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.mapsforge.preprocessing.graph.model.osmxml.OsmWay_withNodes;
import org.mapsforge.preprocessing.graph.osm2rg.osmxml.OsmNode;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgEdge;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgVertex;
import org.mapsforge.preprocessing.model.EHighwayLevel;

/**
 * This is the database wrapper implementation to communicate with the postgres database.
 * 
 * @author kunis
 */
public class OsmBaseService implements IOsmBaseService {

	private static final int BATCH_SIZE = 1000;

	private static final String SQL_INSERT_VERTEX = "INSERT INTO rg_vertex (" + "id, "
			+ "osm_node_id, " + "lon, " + "lat " + ") VALUES (" + "?, " + "?, " + "?, " + "? "
			+ ");";

	private final static String SQL_INSERT_EDGE = "INSERT INTO rg_edge ( " + "id, "
			+ "source_id, " + "target_id, " + "osm_way_id, " + "name, " + "length_meters, "
			+ "undirected, " + "urban, " + "hwy_lvl, " + "longitudes, " + "latitudes "
			+ ") VALUES ( " + "?, " + "?, " + "?, " + "?, " + "?, " + "?, " + "?, " + "?, "
			+ "?, " + "? :: DOUBLE PRECISION[], " + "? :: DOUBLE PRECISION[] " + ");";

	private final static String SQL_INSERT_HIGHWAY_LEVELS = "INSERT INTO rg_hwy_lvl (" + "id, "
			+ "name " + ") VALUES (" + "?, " + "? " + ");";

	private final static String SQL_SELECT_ALL_WAYS = "SELECT way_nodes.id as way_id, way_tags.k as way_tag_key, "
			+ "way_tags.v as way_tag_value, nodes.id as node_id, nodes.int_longitude as node_longitude, "
			+ "nodes.int_latitude as node_latitude, node_tags.k as node_tag_key ,node_tags.v as node_tag_value, "
			+ "way_nodes.sequence_id as way_node_sequence_id "
			+ "FROM  nodes LEFT OUTER JOIN node_tags ON nodes.id = node_tags.id "
			+ "JOIN way_nodes ON nodes.id = way_nodes.node_id "
			+ "LEFT JOIN way_tags ON way_nodes.id = way_tags.id "
			+ "ORDER BY way_nodes.id, way_nodes.sequence_id;";

	private final Connection conn;

	/**
	 * Constructor to create a OsmBaseService object to communicate with osm_base postgres
	 * database.
	 * 
	 * @param conn
	 *            the database connection.
	 */
	public OsmBaseService(Connection conn) {

		this.conn = conn;
		try {
			clearTables();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (!conn.isClosed())
			conn.close();
	}

	private void clearTables() throws SQLException {
		String sql = "TRUNCATE TABLE rg_vertex CASCADE;" + "TRUNCATE TABLE rg_edge CASCADE;"
				+ "TRUNCATE TABLE rg_hwy_lvl CASCADE;";
		conn.createStatement().executeUpdate(sql);
		conn.commit();
	}

	private String doubleArrayToSqlString(double[] array) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			if (i < array.length - 1) {
				sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();

	}

	@Override
	public LinkedList<OsmWay_withNodes> getAllWaysFromDB() throws SQLException {

		PreparedStatement pstmtAllWays = conn.prepareStatement(SQL_SELECT_ALL_WAYS);
		ResultSet rsWays = pstmtAllWays.executeQuery();

		LinkedList<OsmWay_withNodes> ways = new LinkedList<OsmWay_withNodes>();
		LinkedList<OsmNode> way_nodes = new LinkedList<OsmNode>();

		long current_way_id, current_node_id, current_node_lat, current_node_long;
		String current_way_key, current_way_value, current_node_key, current_node_value;

		OsmWay_withNodes currentWay = null;
		OsmNode currentNode = null;

		System.out.println("fetching all ways:");

		while (rsWays.next()) {

			current_way_id = rsWays.getLong("way_id");
			current_node_id = rsWays.getLong("node_id");
			current_node_lat = rsWays.getLong("node_latitude");
			current_node_long = rsWays.getLong("node_longitude");
			current_way_key = rsWays.getString("way_tag_key");
			current_way_value = rsWays.getString("way_tag_value");
			current_node_key = rsWays.getString("node_tag_key");
			current_node_value = rsWays.getString("node_tag_value");

			if (currentWay == null) {
				currentWay = new OsmWay_withNodes(current_way_id);
			}

			if (currentNode == null) {
				currentNode = new OsmNode(current_node_id, current_node_long, current_node_lat);
			}

			if (currentWay.getId() != current_way_id) {
				/*
				 * we get a new way. therefore we add the previous way node to the previous way
				 * and add this one to the list of ways. afterwards we create a new way and a
				 * new way node. it is possible that the the previous way node and the new way
				 * node are the same real nodes, but at the moment it doesn't matter.
				 */
				way_nodes.add(currentNode);
				currentWay.setNodes(way_nodes);
				ways.add(currentWay);
				currentNode = new OsmNode(current_node_id, current_node_long, current_node_lat);
				currentWay = new OsmWay_withNodes(current_way_id);
				way_nodes = new LinkedList<OsmNode>();

			} else if (currentNode.getId() != current_node_id) {
				/*
				 * there is a new node on the old way, we add the previous way node to this way
				 * an create a new way node.
				 */
				way_nodes.add(currentNode);
				currentNode = new OsmNode(current_node_id, current_node_long, current_node_lat);
			}

			if (current_node_key != "" && current_node_value != "") {
				currentNode.addTag(current_node_key, current_node_value);
			}
			if (current_way_key != "" && current_way_value != "") {
				currentWay.addTag(current_way_key, current_way_value);
			}
		}

		// the last way_node must also added
		if (currentNode != null) {
			way_nodes.add(currentNode);
		}

		// the last way must also added
		if (currentWay != null) {
			currentWay.setNodes(way_nodes);
			ways.add(currentWay);
		}

		return ways;
	}

	/**
	 * This method inserts the set of highway levels into the database.
	 * 
	 * @param hwyLvls
	 *            the set of highway levels that should be inserted.
	 * @throws SQLException
	 *             Exception.
	 */
	public void insertHighwayLevels(HashSet<EHighwayLevel> hwyLvls) throws SQLException {
		PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_HIGHWAY_LEVELS);
		for (EHighwayLevel hwyLvl : hwyLvls) {
			pstmt.setInt(1, hwyLvl.ordinal());
			pstmt.setString(2, hwyLvl.toString());
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		conn.commit();
	}

	@Override
	public void insertEdges(LinkedList<RgEdge> edges) throws SQLException {

		PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_EDGE);

		RgEdge currentEdge;
		long insertEdgeCount = 0;

		Iterator<RgEdge> it = edges.iterator();

		while (it.hasNext()) {
			currentEdge = it.next();
			pstmt.setInt(1, currentEdge.getId());
			pstmt.setInt(2, currentEdge.getSourceId());
			pstmt.setInt(3, currentEdge.getTargetId());
			pstmt.setLong(4, currentEdge.getOsmWayId());
			pstmt.setString(5, currentEdge.getName());
			pstmt.setDouble(6, currentEdge.getLengthMeters());
			pstmt.setBoolean(7, currentEdge.isUndirected());
			pstmt.setBoolean(8, currentEdge.isUrban());
			pstmt.setInt(9, EHighwayLevel.valueOf(currentEdge.getHighwayLevel()).ordinal());
			pstmt.setString(10, doubleArrayToSqlString(currentEdge.getLongitudes()));
			pstmt.setString(11, doubleArrayToSqlString(currentEdge.getLatitudes()));
			pstmt.addBatch();
			if ((++insertEdgeCount) % BATCH_SIZE == 0) {
				pstmt.executeBatch();
				conn.commit();
			}

		}
		pstmt.executeBatch();
		conn.commit();
		pstmt.close();

	}

	@Override
	public void insertVertices(LinkedList<RgVertex> vertices) throws SQLException {

		PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_VERTEX);

		RgVertex currentVertex;
		long insertVertexCount = 0;

		Iterator<RgVertex> it = vertices.iterator();

		while (it.hasNext()) {
			currentVertex = it.next();
			System.out.println(currentVertex.getId() + " " + currentVertex.getOsmNodeId()
					+ " "
					+ currentVertex.getLongitude() + " " + currentVertex.getLatitude());

			pstmt.setInt(1, currentVertex.getId());
			pstmt.setLong(2, currentVertex.getOsmNodeId());
			pstmt.setDouble(3, currentVertex.getLongitude());
			pstmt.setDouble(4, currentVertex.getLatitude());
			pstmt.execute();
			conn.commit();

			insertVertexCount++;
			if ((insertVertexCount) % BATCH_SIZE == 0) {
				pstmt.executeBatch();
				conn.commit();
			}

		}
		pstmt.executeBatch();
		conn.commit();
		pstmt.close();

	}
}

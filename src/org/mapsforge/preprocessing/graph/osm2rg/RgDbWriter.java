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
package org.mapsforge.preprocessing.graph.osm2rg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgEdge;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgVertex;

/**
 * 
 * Important to write nodes before edges since referential constraints will throw an exception
 * else. Call Flush after writing to write the buffer.
 */
class RgDbWriter {

	private static final int BATCH_SIZE = 1000;

	private static final String SQL_INSERT_VERTEX =
			"INSERT INTO rg_vertex "
					+ "(id, osm_node_id, lon, lat) VALUES "
					+ "(?, ?, ?, ?);";

	private final static String SQL_INSERT_EDGE =
			"INSERT INTO rg_edge "
					+ "(id, source_id, target_id, osm_way_id, name, ref, destination,"
					+ "length_meters, undirected, urban, roundabout, hwy_lvl, "
					+ "longitudes, latitudes ) VALUES "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "? :: DOUBLE PRECISION[], ? :: DOUBLE PRECISION[]);";

	private final static String SQL_INSERT_EDGE_CLASSES =
			"INSERT INTO rg_hwy_lvl (id, name) VALUES (?, ?);";

	private final Connection conn;
	private final PreparedStatement pstmtInsertVertex, pstmtInsertEdge;
	private long insertVertexCount, insertEdgeCount;
	private final HashMap<String, Integer> highwayLevelToId;

	public RgDbWriter(Connection conn, HashSet<String> highwayLevels) throws SQLException {
		this.conn = conn;
		conn.setAutoCommit(false);
		pstmtInsertVertex = conn.prepareStatement(SQL_INSERT_VERTEX);
		pstmtInsertEdge = conn.prepareStatement(SQL_INSERT_EDGE);
		insertVertexCount = insertEdgeCount = 0;
		highwayLevelToId = new HashMap<String, Integer>();
		{
			int id = 0;
			for (String key : highwayLevels) {
				highwayLevelToId.put(key, id++);
			}
		}
	}

	public void clearTables() throws SQLException {
		String sql = "TRUNCATE TABLE rg_vertex CASCADE;" + "TRUNCATE TABLE rg_edge CASCADE;"
				+ "TRUNCATE TABLE rg_hwy_lvl CASCADE;";
		conn.createStatement().executeUpdate(sql);
		conn.commit();
	}

	public long numInsertedVertices() {
		return insertVertexCount;
	}

	public long numInsertedEdges() {
		return insertEdgeCount;
	}

	public void insertVertex(RgVertex v) throws SQLException {
		pstmtInsertVertex.setLong(1, v.getId());
		pstmtInsertVertex.setLong(2, v.getOsmNodeId());
		pstmtInsertVertex.setDouble(3, v.getLongitude());
		pstmtInsertVertex.setDouble(4, v.getLatitude());
		pstmtInsertVertex.addBatch();
		if ((++insertVertexCount) % BATCH_SIZE == 0) {
			pstmtInsertVertex.executeBatch();
			conn.commit();
		}
	}

	public void insertEdge(RgEdge e) throws SQLException {
		pstmtInsertEdge.setInt(1, e.getId());
		pstmtInsertEdge.setInt(2, e.getSourceId());
		pstmtInsertEdge.setInt(3, e.getTargetId());
		pstmtInsertEdge.setLong(4, e.getOsmWayId());
		pstmtInsertEdge.setString(5, e.getName());
		pstmtInsertEdge.setString(6, e.getRef());
		pstmtInsertEdge.setString(7, e.getDestination());
		pstmtInsertEdge.setDouble(8, e.getLengthMeters());
		pstmtInsertEdge.setBoolean(9, e.isUndirected());
		pstmtInsertEdge.setBoolean(10, e.isUrban());
		pstmtInsertEdge.setBoolean(11, e.isRoundabout());
		pstmtInsertEdge.setInt(12, highwayLevelToId.get(e.getHighwayLevel()));
		pstmtInsertEdge.setString(13, doubleArrayToSqlString(e.getLongitudes()));
		pstmtInsertEdge.setString(14, doubleArrayToSqlString(e.getLatitudes()));
		pstmtInsertEdge.addBatch();
		if ((++insertEdgeCount) % BATCH_SIZE == 0) {
			pstmtInsertEdge.executeBatch();
			conn.commit();
		}
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

	public void insertHighwayLevels() throws SQLException {
		PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_EDGE_CLASSES);
		for (String highwayLevel : highwayLevelToId.keySet()) {
			pstmt.setInt(1, highwayLevelToId.get(highwayLevel));
			pstmt.setString(2, highwayLevel);
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		conn.commit();
	}

	public void flush() throws SQLException {
		pstmtInsertVertex.executeBatch();
		pstmtInsertEdge.executeBatch();
		conn.commit();
	}
}

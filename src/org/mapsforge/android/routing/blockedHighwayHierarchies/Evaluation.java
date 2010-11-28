package org.mapsforge.android.routing.blockedHighwayHierarchies;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import org.mapsforge.core.DBConnection;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.preprocessing.routing.highwayHierarchies.HHDbReader;
import org.mapsforge.preprocessing.routing.highwayHierarchies.HHGraphProperties;

/**
 * ATTENTION : dirty coded CONSTRAINT use only one instance!!!
 * 
 */
class Evaluation {

	private static class TestRoute {
		final HHVertex source;
		final HHVertex target;

		public TestRoute(HHVertex source, HHVertex target) {
			this.source = source;
			this.target = target;
		}
	}

	private static class Result {
		int numGraphBlockReads;
		int numFileSystemBlockReads;
		int numSettledVertices;
		int numCacheHits;
		int maxHeapSize;

		Result() {
			this.numGraphBlockReads = 0;
			this.numFileSystemBlockReads = 0;
			this.numSettledVertices = 0;
			this.numCacheHits = 0;
			this.maxHeapSize = 0;
		}
	}

	public static final int PHASE_A = 0;
	public static final int PHASE_B = 1;

	private static int currentPhase = PHASE_A;
	private static Result[] currentResult = getEmptyResult();

	private static final String SQL_INSERT_BINARY_FILE = "INSERT INTO hh_binary (file_name, c, h, hop_limit, hop_indices, clustering, clustering_threshold) VALUES(?, ?, ?, ?, ?, ?, ?);";
	private static final String SQL_INSERT_TEST_ROUTE = "INSERT INTO test_route (file_name, rank, test_route_id, p1_num_settled, p1_max_heap_size, p1_num_fs_block_reads, p1_num_cluster_reads, p1_num_cache_hits, p2_num_settled, p2_max_heap_size, p2_num_fs_block_reads, p2_num_cluster_reads, p2_num_cache_hits) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String SQL_TRUNCATE_BINARY_FILE_TABLE = "TRUNCATE TABLE hh_binary CASCADE;";
	private static final String SQL_TRUNCATE_TEST_ROUTE_TABLE = "TRUNCATE TABLE test_route CASCADE;";

	// database
	private static final String DB_HOST = "localhost";
	private static final int DB_PORT = 5432;
	private static final String DB_USER = "osm";
	private static final String DB_PASS = "osm";

	// general configuration
	private static final int FILE_SYSTEM_BLOCK_SIZE = 4096;
	private static final int CACHE_SIZE = 400 * 1000 * 1024; // 400MB big enough to cache it all

	private static Result[] getEmptyResult() {
		return new Result[] { new Result(), new Result() };
	}

	static void setPhase(int phase) {
		if (phase == PHASE_A || phase == PHASE_B) {
			currentPhase = phase;
		}
	}

	static void notifyBlockRead(long startAddr, long endAddr) {
		// one graph block was read
		currentResult[currentPhase].numGraphBlockReads++;

		// number of file system blocks read
		while (startAddr < endAddr) {
			startAddr = startAddr + FILE_SYSTEM_BLOCK_SIZE
					- (startAddr % FILE_SYSTEM_BLOCK_SIZE);
			currentResult[currentPhase].numFileSystemBlockReads++;
		}
	}

	static void notifyCacheHit() {
		currentResult[currentPhase].numCacheHits++;
	}

	static void notifyVertexSettled() {
		currentResult[currentPhase].numSettledVertices++;
	}

	static void notifyHeapSizeChanged(int currentHeapSize) {
		currentResult[currentPhase].maxHeapSize = Math.max(
				currentResult[currentPhase].maxHeapSize, currentHeapSize);
	}

	private final Connection evalDbConnection;
	private final PreparedStatement pstmtInsertBinaryFile;
	private final PreparedStatement pstmtInsertTestRoute;

	public Evaluation(Connection evalDbConnection) throws SQLException {
		this.evalDbConnection = evalDbConnection;
		this.pstmtInsertBinaryFile = this.evalDbConnection
				.prepareStatement(SQL_INSERT_BINARY_FILE);
		this.pstmtInsertTestRoute = this.evalDbConnection
				.prepareStatement(SQL_INSERT_TEST_ROUTE);
	}

	void executeTestRoutesWriteToDb(File[] hhBinaryFiles, File[] testRouteFiles)
			throws IOException, SQLException {
		System.out.println("truncate tables");

		Statement stmt = evalDbConnection.createStatement();
		stmt.executeUpdate(SQL_TRUNCATE_BINARY_FILE_TABLE);
		stmt.executeUpdate(SQL_TRUNCATE_TEST_ROUTE_TABLE);
		stmt.close();

		for (File hhBinaryFile : hhBinaryFiles) {
			if (getHasHopindicesFromFileName(hhBinaryFile)) {
				continue;
			}

			System.out.println("executing testroutes on file : '" + hhBinaryFile.getName()
					+ "'");
			Connection conn = getConnection(getDbNameFromFileName(hhBinaryFile));
			HHGraphProperties props = getHHPropertiesFromDb(conn);
			conn.close();

			// (file_name, c, h, hop_limit, hop_indices)
			pstmtInsertBinaryFile.setString(1, hhBinaryFile.getName());
			pstmtInsertBinaryFile.setDouble(2, props.c);
			pstmtInsertBinaryFile.setInt(3, props.h);
			pstmtInsertBinaryFile.setInt(4, props.hopLimit);
			pstmtInsertBinaryFile.setBoolean(5, getHasHopindicesFromFileName(hhBinaryFile));
			pstmtInsertBinaryFile.setString(6,
					getClusteringAlgorithmNameFromFileName(hhBinaryFile));
			pstmtInsertBinaryFile.setInt(7, getClusteringThresholdFromFileName(hhBinaryFile));
			pstmtInsertBinaryFile.executeUpdate();

			for (File testRoutesFile : testRouteFiles) {
				System.out.println(Integer.parseInt(testRoutesFile.getName()
						.substring(0, testRoutesFile.getName().length() - 4)));
				LinkedList<Result[]> result = executeTestRouteBinaryFile(hhBinaryFile,
						testRoutesFile);
				int testRouteId = 0;
				for (Result[] r : result) {
					pstmtInsertTestRoute.setString(1, hhBinaryFile.getName());
					pstmtInsertTestRoute.setInt(2, Integer.parseInt(testRoutesFile.getName()
							.substring(0, testRoutesFile.getName().length() - 4)));
					pstmtInsertTestRoute.setInt(3, testRouteId++);

					pstmtInsertTestRoute.setInt(4, r[0].numSettledVertices);
					pstmtInsertTestRoute.setInt(5, r[0].maxHeapSize);
					pstmtInsertTestRoute.setInt(6, r[0].numFileSystemBlockReads);
					pstmtInsertTestRoute.setInt(7, r[0].numGraphBlockReads);
					pstmtInsertTestRoute.setInt(8, r[0].numCacheHits);

					pstmtInsertTestRoute.setInt(9, r[1].numSettledVertices);
					pstmtInsertTestRoute.setInt(10, r[1].maxHeapSize);
					pstmtInsertTestRoute.setInt(11, r[1].numFileSystemBlockReads);
					pstmtInsertTestRoute.setInt(12, r[1].numGraphBlockReads);
					pstmtInsertTestRoute.setInt(13, r[1].numCacheHits);

					pstmtInsertTestRoute.addBatch();
				}
				pstmtInsertTestRoute.executeBatch();
			}
		}
	}

	private LinkedList<Result[]> executeTestRouteBinaryFile(File hhBinaryFile,
			File testRoutesFile)
			throws IOException {
		HHRoutingGraph routingGraph = new HHRoutingGraph(hhBinaryFile, CACHE_SIZE);
		System.out.println(routingGraph.hasShortcutHopIndices);
		HHAlgorithm algo = new HHAlgorithm(routingGraph);
		LinkedList<TestRoute> testRoutes = getTestRoutesFromFile(testRoutesFile, routingGraph);
		LinkedList<Result[]> results = new LinkedList<Result[]>();

		int count = 0;
		for (TestRoute testRoute : testRoutes) {
			currentResult = getEmptyResult();
			algo.getShortestPath(testRoute.source.vertexIds[0], testRoute.target.vertexIds[0],
					new LinkedList<HHEdge>(), true);
			results.add(currentResult);

			if (count % 10 == 0) {
				System.out.println(count + "/" + testRoutes.size());
			}
			count++;
		}
		return results;
	}

	private LinkedList<TestRoute> getTestRoutesFromFile(File testRoutesFile,
			HHRoutingGraph routingGraph)
			throws IOException {
		final int maxDistance = 300;
		LinkedList<TestRoute> testRoutes = new LinkedList<TestRoute>();
		LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(
				testRoutesFile)));
		String line;
		while ((line = lnr.readLine()) != null) {
			String[] coords = line.split(";");
			String[] s = coords[0].split(",");
			String[] t = coords[1].split(",");
			HHVertex source = routingGraph.getNearestVertex(new GeoCoordinate(Double
					.parseDouble(s[0]), Double.parseDouble(s[1])), maxDistance);
			HHVertex target = routingGraph.getNearestVertex(new GeoCoordinate(Double
					.parseDouble(t[0]), Double.parseDouble(t[1])), maxDistance);
			testRoutes.add(new TestRoute(source, target));
		}
		return testRoutes;
	}

	private Connection getConnection(String dbName) throws SQLException {
		return DBConnection.getJdbcConnectionPg(DB_HOST, DB_PORT, dbName, DB_USER, DB_PASS);
	}

	private HHGraphProperties getHHPropertiesFromDb(Connection conn) throws SQLException {
		HHDbReader reader = new HHDbReader(conn);
		return reader.getGraphProperties();
	}

	private String getDbNameFromFileName(File hhBinaryFile) {
		return hhBinaryFile.getName().substring(0, 6);
	}

	private String getClusteringAlgorithmNameFromFileName(File hhBinaryFile) {
		String s = hhBinaryFile.getName().substring(7);
		if (s.startsWith("quad_tree")) {
			return "quad_tree";
		}
		return "k_center";
	}

	private int getClusteringThresholdFromFileName(File hhBinaryFile) {
		return Integer.parseInt(hhBinaryFile.getName().split("_")[4]);
	}

	private boolean getHasHopindicesFromFileName(File hhBinaryFile) {
		return hhBinaryFile.getName().split("_")[5].startsWith("true");
	}

	public static void main(String[] args) throws IOException {
		try {
			File[] hhBinaryFiles = new File[] { new File(
					"evaluation/binaries/ger_12_quad_tree_400_false.blockedHH") };
			for (int i = 0; i < hhBinaryFiles.length; i++) {
				System.out.println("enqueue " + hhBinaryFiles[i].getName());
			}
			File[] testRoutesFiles = new File("evaluation/routes/").listFiles();
			Connection conn = DBConnection.getJdbcConnectionPg("localhost", 5432,
					"eval_single",
					"osm", "osm");
			Evaluation eval = new Evaluation(conn);
			eval.executeTestRoutesWriteToDb(hhBinaryFiles,
					testRoutesFiles);
			conn.commit();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			while (e.getNextException() != null) {
				e = e.getNextException();
				e.printStackTrace();
			}
		}
	}
}

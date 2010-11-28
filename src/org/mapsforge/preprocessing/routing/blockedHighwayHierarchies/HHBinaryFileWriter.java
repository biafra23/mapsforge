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
package org.mapsforge.preprocessing.routing.blockedHighwayHierarchies;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Rect;
import org.mapsforge.preprocessing.routing.blockedHighwayHierarchies.LevelGraph.Level;
import org.mapsforge.preprocessing.routing.blockedHighwayHierarchies.LevelGraph.Level.LevelEdge;
import org.mapsforge.preprocessing.routing.blockedHighwayHierarchies.LevelGraph.Level.LevelVertex;
import org.mapsforge.preprocessing.routing.highwayHierarchies.util.Serializer;
import org.mapsforge.preprocessing.util.DBConnection;

class HHBinaryFileWriter {

	private final static byte[] HEADER_MAGIC = HHGlobals.BINARY_FILE_HEADER_MAGIC;
	private final static int HEADER_LENGTH = HHGlobals.BINARY_FILE_HEADER_LENGTH;

	private final static int BUFFER_SIZE = 16384 * 1000;

	public static void writeBinaryFile(Properties props) {
		try {
			/*
			 * read the config from properties
			 */

			String dbHost = props.getProperty("blockedHH.input.db.host");
			int dbPort = Integer.parseInt(props.getProperty("blockedHH.input.db.port"));
			String dbName = props.getProperty("blockedHH.input.db.name");
			String dbUser = props.getProperty("blockedHH.input.db.user");
			String dbPassword = props.getProperty("blockedHH.input.db.password");

			File inputFile;
			try {
				inputFile = new File(props.getProperty("blockedHH.input.file"));
			} catch (NullPointerException e) {
				inputFile = null;
			}
			File outputFile = new File(props.getProperty("blockedHH.output.file"));

			String clusteringAlgorithm = props.getProperty("blockedHH.clustering.algorithm");
			int quadtreeVertexThreshold = Integer.parseInt(props
					.getProperty("blockedHH.clustering.vertexThreshold"));
			int kcenterAvgVerticesPerCluster = Integer.parseInt(props
					.getProperty("blockedHH.clustering.avgVerticesPerCluster"));
			int kcenterOversamplingFac = Integer
					.parseInt(props
					.getProperty("blockedHH.clustering.oversamplingFac"));

			int addressLookupTableMaxGroupSize = Integer.parseInt(props
					.getProperty("blockedHH.addressLookupTable.maxGroupSize"));
			int hopIndices = Integer.parseInt(props
					.getProperty("blockedHH.hopIndices"));
			int rtreeBlockSize = Integer.parseInt(props
					.getProperty("blockedHH.rtree.blockSize"));

			System.out.println("create blocked highway hierarchies binary :'"
					+ outputFile.getAbsolutePath() + "'");
			LevelGraph levelGraph = null;
			if (inputFile != null && inputFile.exists() && inputFile.isFile()) {
				// load graph from file
				try {
					System.out.print("loading the graph : '" + inputFile.getAbsolutePath()
							+ "'" + " .. ");
					levelGraph = Serializer.deserialize(inputFile);
					System.out.println("successfull!");
				} catch (ClassNotFoundException e) {
					System.out.println("not successfull!");
				}
			}
			if (levelGraph == null) {
				// load graph from database
				System.out.println("loading the graph : 'jdbc://" + dbHost + ":" + dbPort + "/"
						+ dbName + "'");
				Connection conn = DBConnection.getJdbcConnectionPg(dbHost, dbPort, dbName,
						dbUser,
						dbPassword);
				levelGraph = new LevelGraph(conn);
				conn.close();
			}

			// compute clustering
			System.out.println("compute clustering");
			Clustering[] clustering;
			if (clusteringAlgorithm.equals(QuadTreeClusteringAlgorithm.ALGORITHM_NAME)) {
				System.out.println("algorithm = quad_tree");
				System.out.println("threshold = " + quadtreeVertexThreshold);
				clustering = QuadTreeClusteringAlgorithm.computeClustering(levelGraph
						.getLevels(),
						levelGraph.getVertexLongitudesE6(), levelGraph.getVertexLatitudesE6(),
						QuadTreeClusteringAlgorithm.HEURISTIC_CENTER,
						quadtreeVertexThreshold);

			} else if (clusteringAlgorithm.equals(KCenterClusteringAlgorithm.ALGORITHM_NAME)) {
				System.out.println("algorithm = k_center");
				System.out.println("verticesPerCluster = " + kcenterAvgVerticesPerCluster);
				clustering = KCenterClusteringAlgorithm.computeClustering(levelGraph
						.getLevels(), kcenterAvgVerticesPerCluster, kcenterOversamplingFac,
						KCenterClusteringAlgorithm.HEURISTIC_MIN_SIZE);
			} else {
				System.out.println("invalid clustering algorithm specified in properties.");
				return;
			}
			// create the binary file
			System.out.println("serialize data to '" + outputFile.getAbsolutePath() + "'");
			writeBinaryFile(levelGraph, clustering, outputFile, addressLookupTableMaxGroupSize,
					rtreeBlockSize, hopIndices);
			System.out.println("ready!");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void writeBinaryFile(LevelGraph levelGraph, Clustering[] clustering,
			File targetFile, int indexGroupSizeThreshold, int rtreeBlockSize,
			int includeHopIndices) throws IOException {

		// ---------------- WRITE TEMPORARY FILES --------------------------

		File fBlocks = new File(targetFile.getAbsolutePath() + ".blocks");
		File fileAddressLookupTable = new File(targetFile.getAbsolutePath()
				+ ".addressLookupTable");
		File fRTree = new File(targetFile.getAbsolutePath() + ".rtree");

		// write the graphs cluster blocks
		ClusterBlockMapping mapping = new ClusterBlockMapping(clustering);
		int[] blockSize = BlockWriter.writeClusterBlocks(fBlocks, levelGraph, clustering,
				mapping, includeHopIndices);

		// write block index
		AddressLookupTableWriter.writeTable(blockSize, indexGroupSizeThreshold,
				fileAddressLookupTable);

		// construct and write r-tree (insert only level 0 clusters)
		int[] minLat = new int[clustering[0].size()];
		int[] maxLat = new int[clustering[0].size()];
		int[] minLon = new int[clustering[0].size()];
		int[] maxLon = new int[clustering[0].size()];
		int[] blockId = new int[clustering[0].size()];
		{
			int i = 0;
			for (Cluster c : clustering[0].getClusters()) {
				Rect r = getBoundingBox(c, levelGraph.getLevel(0));
				minLat[i] = r.minLatitudeE6;
				maxLat[i] = r.maxLatitudeE6;
				minLon[i] = r.minLongitudeE6;
				maxLon[i] = r.maxLongitudeE6;
				blockId[i] = mapping.getBlockId(c);
				i++;
			}
		}
		StaticRTreeWriter.packSortTileRecursive(minLon, maxLon, minLat, maxLat, blockId,
				rtreeBlockSize, fRTree);

		// ---------------- WRITE THE BINARY FILE --------------------------

		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(targetFile)));

		// write header of the binary
		long startAddrGraph = HEADER_LENGTH;
		long endAddrGraph = startAddrGraph + fBlocks.length();

		long startAddrBlockIdx = endAddrGraph;
		long endAddrBlockIdx = startAddrBlockIdx + fileAddressLookupTable.length();

		long startAddrRTree = endAddrBlockIdx;
		long endAddrRTree = startAddrRTree + fRTree.length();

		out.write(HEADER_MAGIC);
		out.writeLong(startAddrGraph);
		out.writeLong(endAddrGraph);
		out.writeLong(startAddrBlockIdx);
		out.writeLong(endAddrBlockIdx);
		out.writeLong(startAddrRTree);
		out.writeLong(endAddrRTree);
		if (out.size() <= HEADER_LENGTH) {
			out.write(new byte[HEADER_LENGTH - out.size()]);
		} else {
			throw new RuntimeException("need to increase header length.");
		}

		// write components
		writeFile(fBlocks, out);
		writeFile(fileAddressLookupTable, out);
		writeFile(fRTree, out);

		out.flush();
		out.close();

		// ---------------- CLEAN UP TEMPORARY FILES --------------------------
		fBlocks.delete();
		fileAddressLookupTable.delete();
		fRTree.delete();
	}

	private static Rect getBoundingBox(Cluster c, Level graph) {
		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;

		int[] vertexIds = c.getVertices();
		for (int vId : vertexIds) {
			LevelVertex v = graph.getVertex(vId);
			GeoCoordinate coord = v.getCoordinate();
			minLat = Math.min(coord.getLatitudeE6(), minLat);
			maxLat = Math.max(coord.getLatitudeE6(), maxLat);
			minLon = Math.min(coord.getLongitudeE6(), minLon);
			maxLon = Math.max(coord.getLongitudeE6(), maxLon);
			for (LevelEdge e : v.getOutboundEdges()) {
				GeoCoordinate[] waypoints = e.getWaypoints();
				if (waypoints != null) {
					for (GeoCoordinate wp : waypoints) {
						minLat = Math.min(wp.getLatitudeE6(), minLat);
						maxLat = Math.max(wp.getLatitudeE6(), maxLat);
						minLon = Math.min(wp.getLongitudeE6(), minLon);
						maxLon = Math.max(wp.getLongitudeE6(), maxLon);
					}
				}
			}
		}
		return new Rect(minLon, maxLon, minLat, maxLat);
	}

	/**
	 * Writes the content of the given file to the output stream.
	 * 
	 * @param f
	 *            the file to be written.
	 * @param oStream
	 *            the stream to write to.
	 * @return number of bytes written.
	 * @throws IOException
	 *             on write errors.
	 */
	private static long writeFile(File f, OutputStream oStream) throws IOException {
		byte[] buff = new byte[BUFFER_SIZE];
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));

		int len;
		long offset = 0;
		while ((len = in.read(buff)) > 0) {
			oStream.write(buff, 0, len);
			offset += len;
		}
		in.close();
		return offset;
	}

	public static void main(String[] args) throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream("res/conf/blockedHH.properties"));
		writeBinaryFile(props);
	}
}

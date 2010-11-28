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
package org.mapsforge.preprocessing.routing.highwayHierarchies;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.array.TIntArrayStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import org.mapsforge.core.DBConnection;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgDAO;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgEdge;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgWeightFunctionDistance;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgWeightFunctionTime;
import org.mapsforge.preprocessing.graph.routingGraphInterface.IRgDAO;
import org.mapsforge.preprocessing.graph.routingGraphInterface.IRgEdge;
import org.mapsforge.preprocessing.graph.routingGraphInterface.IRgVertex;
import org.mapsforge.preprocessing.graph.routingGraphInterface.IRgWeightFunction;
import org.mapsforge.preprocessing.routing.highwayHierarchies.HHDynamicGraph.HHDynamicEdge;
import org.mapsforge.preprocessing.routing.highwayHierarchies.HHDynamicGraph.HHDynamicVertex;
import org.mapsforge.preprocessing.routing.highwayHierarchies.HHGraphProperties.HHLevelStats;
import org.mapsforge.preprocessing.routing.highwayHierarchies.util.arrays.BitArraySynchronized;
import org.mapsforge.server.routing.highwayHierarchies.DistanceTable;
import org.mapsforge.server.routing.highwayHierarchies.HHRouterServerside;

//select x.c as degree, count(*) * 100.0 / (select count(*) from hh_vertex where lvl = 0) as count from (select source_id, count(*) as c from hh_edge group by source_id order by c) as x group by x.c order by degree;

/**
 * 
 * This class is responsible for the complete process of preprocessing. As input, it reads a
 * properties file res/hhPreprocessing.properties. tuning params for preprocessing: (names
 * similar to highway hierarchies paper)
 * 
 * 1. h = neighborhood size
 * 
 * 2. c = contraction facor, see method isBypassable for details.
 * 
 * 3. vertexThreshold = on number of core node, should be >>> 0 to use distance table
 * 
 * 4. hop limit = limit on number of edges of level l - 1 a shortcut of level l can represent
 * 
 * 5. downgrade edge = downgrade edges leaving core (check of restriction 2 is no more needed
 * during query)
 */
public final class HHComputation {

	/**
	 * neighborhood of vertices not in core
	 */
	public static final int INFINITY_1 = Integer.MAX_VALUE;
	/**
	 * neighborhood of vertices belonging to the top level core.
	 */
	public static final int INFINITY_2 = Integer.MAX_VALUE - 1;

	/* hierarchy construction parameters */
	private static int H;
	static int HOP_LIMIT;
	private static int NUM_THREADS, VERTEX_THRESHOLD;
	private static boolean DOWNGRADE_EDGES_LEAVING_CORE;
	static double C;

	private static <V extends IRgVertex, E extends IRgEdge> void doPreprocessing(
			IRgDAO<V, E> rgDao, IRgWeightFunction<E> wFunc, int h, int hopLimit, double c,
			int vertexThreshold, boolean downgradeEdges, int numThreads, Connection outputDb)
			throws SQLException {
		DecimalFormat df = new DecimalFormat("#.#");

		System.out.println("import routing graph : ");
		HHDynamicGraph graph = HHDynamicGraph.importRoutingGraph(rgDao, wFunc);

		System.out.println("\nstart hierarchy coputation : ");
		System.out.println("input-graph : |V| = " + graph.numVertices() + " |E| = "
				+ graph.numEdges());
		System.out.println("h = " + h);
		System.out.println("hopLimit = " + hopLimit);
		System.out.println("c = " + df.format(c));
		System.out.println("vertexThreshold = " + vertexThreshold);
		System.out.println("downgradeEdges = " + downgradeEdges);
		System.out.println("numThreads = " + numThreads);

		// compute hierarchy
		long hierarchyComputationStart = System.currentTimeMillis();
		HierarchyComputationResult result = HHComputation.computeHierarchy(graph, h, hopLimit,
				c, vertexThreshold, downgradeEdges, numThreads);
		if (result == null) {
			System.out.println("aborting.");
			return;
		}
		System.out.println(result);
		double compTimeMinutes = ((double) (System.currentTimeMillis() - hierarchyComputationStart)) / 60000;

		// compute core distance table
		DistanceTable distanceTable = ThreadedDistanceTableComputation
				.computeCoreDistanceTable(graph, graph.numLevels() - 1, numThreads);

		// write result to output database
		HHDbWriter writer = new HHDbWriter(outputDb);
		writer.clearTables();

		// write vertices
		for (Iterator<V> iter = rgDao.getVertices().iterator(); iter.hasNext();) {
			IRgVertex v = iter.next();
			writer.writeVertex(result.originalVertexIdsToAssignedVertexId[v.getId()], v
					.getLongitude(), v.getLatitude());
		}
		writer.flush();

		// write vertex-levels
		for (int i = 0; i < graph.numVertices(0); i++) {
			HHDynamicVertex v = graph.getVertex(i);
			for (int lvl = 0; lvl <= v.getMaxLevel(); lvl++) {
				writer.writeVertexLevel(v.getId(), lvl, v.getNeighborhood(lvl));
			}
		}
		writer.flush();

		// write edges
		for (int i = 0; i < graph.numEdgeEntries(); i++) {
			HHDynamicEdge e = graph.getEdge(i);
			writer.writeEdge(e.getId(), e.getSource().getId(), e.getTarget().getId(), e
					.getWeight(), e.getMinLevel(), e.getMaxLevel(), e.isForward(), e
					.isBackward(), e.isShortcut());
		}
		writer.flush();

		// write hierarchy meta data
		writer.writeGraphProperties(new HHGraphProperties(new Date(System.currentTimeMillis()),
				"car", h, vertexThreshold, hopLimit, numThreads, c, compTimeMinutes,
				downgradeEdges, result.levelStats));
		// write distance table
		writer.writeDistanceTable(distanceTable);

		writer.flush();
		System.out.println("\n" + result);
		double minutes = (System.currentTimeMillis() - hierarchyComputationStart) / 60000d;
		System.out.println("finished in " + df.format(minutes) + " minutes.");
	}

	private static HierarchyComputationResult computeHierarchy(HHDynamicGraph graph, int h,
			int hopLimit, double c, int vertexThreshold, boolean downgradeEdgesLeavingCore,
			int numThreads) {
		H = h;
		HOP_LIMIT = hopLimit;
		NUM_THREADS = numThreads;
		C = c;
		VERTEX_THRESHOLD = vertexThreshold;
		DOWNGRADE_EDGES_LEAVING_CORE = downgradeEdgesLeavingCore;

		return computeHH(graph);
	}

	private static HierarchyComputationResult computeHH(HHDynamicGraph graph) {

		if (!verifyInputGraph(graph)) {
			return null;
		}
		LinkedList<HHLevelStats> levelInfo = new LinkedList<HHLevelStats>();
		levelInfo.add(new HHLevelStats(0, graph.numEdges(), graph.numVertices(), graph
				.numEdges(), graph.numVertices()));

		if (graph.numVertices(0) > VERTEX_THRESHOLD) {
			/*
			 * COMPUTE LEVEL ONE
			 */
			ThreadedNeighborhoodComputation.computeNeighborhoods(graph, 0, H, NUM_THREADS);
			BitArraySynchronized highwayEdges = new BitArraySynchronized(graph
					.getEdgeIdUpperBound());
			ThreadedHighwayNetworkComputation.computeHighwayNetwork(graph, 0, highwayEdges,
					NUM_THREADS, true);
			ThreadedHighwayNetworkComputation.computeHighwayNetwork(graph, 0, highwayEdges,
					NUM_THREADS, false);
			graph.addLevel();
			graph.addLevel();

			for (Iterator<HHDynamicVertex> iter = graph.getVertices(0); iter.hasNext();) {
				HHDynamicVertex v = iter.next();
				for (HHDynamicEdge e : v.getOutboundEdges(0)) {
					if (highwayEdges.get(e.getId())) {
						graph.addEdge(e, 1);
						graph.addEdge(e, 2);
					}
				}
			}
			Contractor.contractGraph(graph, 2);
			removeParallelEdges(graph, 2);
			addLevelInfo(graph, levelInfo);
			ThreadedNeighborhoodComputation.computeNeighborhoods(graph, 2, H, NUM_THREADS);

			for (Iterator<HHDynamicVertex> iter = graph.getVertices(1); iter.hasNext();) {
				HHDynamicVertex v = iter.next();
				if (v.getMaxLevel() == 2) {
					v.setNeighborhood(v.getNeighborhood(2), 1);
				} else {
					v.setNeighborhood(INFINITY_1, 1);
				}
				for (HHDynamicEdge e : v.getOutboundEdges(2)) {
					if (e.getMinLevel() == 2 && e.getHopCount() > 1) {
						graph.addEdge(e, 1);
					}
				}
			}
			// graph-level 1 now contains Highway network of level 0 + core edges of level 1
			// graph-level 2 now contains only the core of level 1
			// graph-level 2 is input for higher levels
			// remove non highway edges
			// copy highway network upward
			// contract it
			// remove parallel core edges
			// propagate new shortcuts downward -> next iteration
			/*
			 * COMPUTE FURTHER LEVELS
			 */
			while (graph.numVertices(graph.numLevels() - 1) > VERTEX_THRESHOLD) {

				// remove non highway edges
				highwayEdges = new BitArraySynchronized(graph.getEdgeIdUpperBound());
				ThreadedHighwayNetworkComputation.computeHighwayNetwork(graph, graph
						.numLevels() - 1, highwayEdges, NUM_THREADS, true);
				ThreadedHighwayNetworkComputation.computeHighwayNetwork(graph, graph
						.numLevels() - 1, highwayEdges, NUM_THREADS, false);
				for (Iterator<HHDynamicVertex> iter = graph.getVertices(graph.numLevels() - 1); iter
						.hasNext();) {
					HHDynamicVertex v = iter.next();
					for (HHDynamicEdge e : v.getOutboundEdges(graph.numLevels() - 1)) {
						if (!highwayEdges.get(e.getId())) {
							graph.removeEdge(e, graph.numLevels() - 1);
						}
					}
				}

				// add all edges to next level
				graph.addLevel();
				for (Iterator<HHDynamicVertex> iter = graph.getVertices(graph.numLevels() - 2); iter
						.hasNext();) {
					HHDynamicVertex v = iter.next();
					for (HHDynamicEdge e : v.getOutboundEdges(graph.numLevels() - 2)) {
						graph.addEdge(e, graph.numLevels() - 1);
					}
				}

				// get core of topmost level
				Contractor.contractGraph(graph, graph.numLevels() - 1);
				removeParallelEdges(graph, graph.numLevels() - 1);
				addLevelInfo(graph, levelInfo);
				ThreadedNeighborhoodComputation.computeNeighborhoods(graph,
						graph.numLevels() - 1, H, NUM_THREADS);

				// set neighborhoods of vertices in 2nd highest level & propagate new shortcuts
				// to 2nd highest level
				for (Iterator<HHDynamicVertex> iter = graph.getVertices(graph.numLevels() - 2); iter
						.hasNext();) {
					HHDynamicVertex v = iter.next();
					if (v.getMaxLevel() == graph.numLevels() - 1) {
						v.setNeighborhood(v.getNeighborhood(graph.numLevels() - 1), graph
								.numLevels() - 2);
					} else {
						v.setNeighborhood(INFINITY_1, graph.numLevels() - 2);
					}
					for (HHDynamicEdge e : v.getOutboundEdges(graph.numLevels() - 1)) {
						if (e.getMinLevel() == graph.numLevels() - 1 && e.getHopCount() > 1) {
							graph.addEdge(e, graph.numLevels() - 2);
						}
					}
				}
			}

			// set neighborhoods of top level
			for (Iterator<HHDynamicVertex> iter = graph.getVertices(graph.numLevels() - 2); iter
					.hasNext();) {
				HHDynamicVertex v = iter.next();
				if (v.getMaxLevel() == graph.numLevels() - 1) {
					v.setNeighborhood(INFINITY_2, graph.numLevels() - 2);
				} else {
					v.setNeighborhood(INFINITY_1, graph.numLevels() - 2);
				}
			}

			// remove top level containing only the core
			for (Iterator<HHDynamicVertex> iter = graph.getVertices(graph.numLevels() - 1); iter
					.hasNext();) {
				HHDynamicVertex v = iter.next();
				for (HHDynamicEdge e : v.getOutboundEdges(graph.numLevels() - 1)) {
					graph.removeEdge(e, graph.numLevels() - 1);
				}
			}
			while (graph.numEdges(graph.numLevels() - 1) == 0
					&& graph.numVertices(graph.numLevels() - 1) == 0) {
				graph.removeTopLevel();
			}

			while (levelInfo.size() > graph.numLevels()) {
				levelInfo.removeLast();
			}

			if (DOWNGRADE_EDGES_LEAVING_CORE) {
				for (int lvl = 1; lvl < graph.numLevels(); lvl++) {
					for (Iterator<HHDynamicVertex> iter = graph.getVertices(lvl); iter
							.hasNext();) {
						HHDynamicVertex v = iter.next();
						for (HHDynamicEdge e : v.getOutboundEdges(lvl)) {// TODO: check this,
							// may down grade
							// also top level
							// edges?
							if (e.getSource().getNeighborhood(lvl) != INFINITY_1
									&& e.getTarget().getNeighborhood(lvl) == INFINITY_1) {
								graph.addEdge(e, lvl - 1);
								graph.removeEdge(e, lvl);
								// remember : down grade from level 1 to level 0 never down
								// grades
								// shortcuts, since shortcuts of level 1 cannot leave the
								// core, they are always completely within the core!!!
							}
						}
					}
				}
			}

		}
		// group vertices by core level
		int[] orgIdToAssignedId = regroupVerticesByCoreLevel(graph);
		graph.reassignEdgeIds();

		return new HierarchyComputationResult(levelInfo, graph, orgIdToAssignedId);
	}

	private static void addLevelInfo(HHDynamicGraph graph, LinkedList<HHLevelStats> levelInfo) {
		levelInfo
				.add(new HHLevelStats(graph.numLevels() - 2, graph
				.numEdges(graph.numLevels() - 2), graph
				.numVertices(graph.numLevels() - 2), graph
				.numEdges(graph.numLevels() - 1), graph
				.numVertices(graph.numLevels() - 1)));
	}

	private static boolean verifyInputGraph(HHDynamicGraph graph) {
		// graph has exactly one level?
		if (graph.numLevels() != 1) {
			System.out.println("input graph has too many levels");
			return false;
		}
		for (int i = 0; i < graph.numVertices(); i++) {
			HHDynamicVertex v = graph.getVertex(i);
			// has unconnected vertices?
			if (v.getOutboundEdges(0).length == 0 && v.getInboundEdges(0).length == 0) {
				System.out.println("input graph has unconnected vertices");
				return false;
			}
			// negative edge weights?
			for (HHDynamicEdge e : v.getOutboundEdges(0)) {
				if (e.getWeight() < 0) {
					System.out.println("input graph has negative edge weights");
					return false;
				}
			}
		}
		return true;
	}

	private static int[] regroupVerticesByCoreLevel(HHDynamicGraph graph) {
		int[] originalIds = new int[graph.numVertices(0)];
		for (int i = 0; i < originalIds.length; i++) {
			originalIds[i] = i;
		}
		return originalIds;
		/*
		 * int lvlOffset = 0; for (int lvl = -1; lvl <= graph.numLevels(); lvl++) { int i =
		 * lvlOffset; while (i < graph.numVertices(0) && getCoreLevel(graph.getVertex(i)) ==
		 * lvl) i++;
		 * 
		 * for (int j = graph.numVertices(0) - 1; j > i; j--) { if
		 * (getCoreLevel(graph.getVertex(j)) == lvl) { graph.swapVertexIds(graph.getVertex(i),
		 * graph.getVertex(j)); int tmp = originalIds[i]; originalIds[i] = originalIds[j];
		 * originalIds[j] = tmp; while (i < graph.numVertices(0) &&
		 * getCoreLevel(graph.getVertex(i)) == lvl) i++; } } lvlOffset = i; } int[] orgIdToId =
		 * new int[graph.numVertices(0)]; for (int i = 0; i < orgIdToId.length; i++) {
		 * orgIdToId[originalIds[i]] = i; }
		 * 
		 * return orgIdToId;
		 */
	}

	// private static int getCoreLevel(HHDynamicVertex v) {
	// if (v.getNeighborhood(v.getMaxLevel()) != INFINITY_1) {
	// return v.getMaxLevel();
	// }
	// return v.getMaxLevel() - 1;
	// }

	private static void removeParallelEdges(HHDynamicGraph graph, int lvl) {
		for (Iterator<HHDynamicVertex> iter = graph.getVertices(lvl); iter.hasNext();) {
			HHDynamicVertex v = iter.next();
			HashMap<Integer, Integer> dFwd = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> dBwd = new HashMap<Integer, Integer>();
			for (HHDynamicEdge e : v.getOutboundEdges(lvl)) {
				if (e.isForward()) {
					if (dFwd.containsKey(e.getTarget().getId())) {
						dFwd.put(e.getTarget().getId(), Math.min(e.getWeight(), dFwd.get(e
								.getTarget().getId())));
					} else {
						dFwd.put(e.getTarget().getId(), e.getWeight());
					}
				}
				if (e.isBackward()) {
					if (dBwd.containsKey(e.getTarget().getId())) {
						dBwd.put(e.getTarget().getId(), Math.min(e.getWeight(), dBwd.get(e
								.getTarget().getId())));
					} else {
						dBwd.put(e.getTarget().getId(), e.getWeight());
					}
				}
			}
			for (HHDynamicEdge e : v.getOutboundEdges(lvl)) {
				if ((!e.isBackward() || (e.isBackward() && e.getWeight() > dBwd.get(e
						.getTarget().getId())))
						&& (!e.isForward() || (e.isForward() && e.getWeight() > dFwd.get(e
						.getTarget().getId())))) {
					graph.removeEdge(e, lvl);

				}
			}
		}
	}

	/*
	 * CONTRACTION (VERTEX REDUCTION BY INTRODUCING SHORTCUT EDGES)
	 */

	private static class Contractor {

		public static void contractGraph(HHDynamicGraph graph, int lvl) {
			TIntArrayStack queue = new TIntArrayStack();
			for (int i = 0; i < graph.numVertices(0); i++) {
				HHDynamicVertex v = graph.getVertex(i);
				bypassVertex(graph, v, queue, lvl);

			}
			while (queue.size() > 0) {
				bypassVertex(graph, graph.getVertex(queue.pop()), queue, lvl);
			}
		}

		private static void bypassVertex(HHDynamicGraph graph, HHDynamicVertex v,
				TIntArrayStack queue, int lvl) {
			if (!isBypassable(v, lvl)) {
				return;
			}
			// create shortcuts
			for (HHDynamicEdge in : v.getInboundEdges(lvl)) {
				for (HHDynamicEdge out : v.getOutboundEdges(lvl)) {
					if (in.getSource().getId() == out.getTarget().getId()) {
						// don't insert loops into the graph
						continue;
					}
					if ((in.isForward() != out.isForward())
							&& (in.isBackward() != out.isBackward())) {
						// no shortcut can be created
						continue;
					}
					HHDynamicEdge e = graph.addEdge(in.getSource().getId(), out.getTarget()
							.getId(), in.getWeight() + out.getWeight(), in.isForward()
							&& out.isForward(), in.isBackward() && out.isBackward(), lvl);
					if (!e.isForward() && !e.isBackward()) {
						System.out.println("error");
					}
					e.setHopCount(getHops(in, lvl) + getHops(out, lvl));
					e.setShortcut(true);
				}
			}

			// remove all adjacent edges of v and collect incident vertices
			TIntHashSet set = new TIntHashSet();
			for (HHDynamicEdge e : v.getInboundEdges(lvl)) {
				set.add(e.getSource().getId());
				graph.removeEdge(e, lvl);
			}
			for (HHDynamicEdge e : v.getOutboundEdges(lvl)) {
				set.add(e.getTarget().getId());
				graph.removeEdge(e, lvl);
			}

			// enqueue all incident vertices
			for (TIntIterator iter = set.iterator(); iter.hasNext();) {
				queue.push(iter.next());
			}
		}

		private static boolean isBypassable(HHDynamicVertex v, int lvl) {
			if (v == null || ((v.getInDegree(lvl) == 0) && (v.getOutDegree(lvl) == 0))) {
				return false;
			}

			// check if at least one shortcut would exceed the hopLimit
			int numShortcuts = 0;
			for (HHDynamicEdge in : v.getInboundEdges(lvl)) {
				for (HHDynamicEdge out : v.getOutboundEdges(lvl)) {
					if (in.getSource().getId() == out.getTarget().getId()) {
						continue;
					}
					if ((in.isForward() != out.isForward())
							&& (in.isBackward() != out.isBackward())) {
						continue;
					}
					if (getHops(in, lvl) + getHops(out, lvl) > HOP_LIMIT) {
						return false;
					}
					numShortcuts++;
				}
			}

			// check standard bypassability criterion
			float dIn = v.getInDegree(lvl);
			float dOut = v.getOutDegree(lvl);
			return numShortcuts <= C * (dIn + dOut);
		}

		private static int getHops(HHDynamicEdge e, int lvl) {
			if (e.getMinLevel() == lvl) {
				return e.getHopCount();
			}
			return 1;

		}

		// private static int getUndirectedDegree(DynamicLevelVertex v, int lvl){
		// TIntHashSet incidentVertices = new TIntHashSet();
		// for(DynamicLevelEdge e : v.getInboundEdges(lvl)){
		// incidentVertices.add(e.getSource().getId());
		// }
		// for(DynamicLevelEdge e : v.getOutboundEdges(lvl)){
		// incidentVertices.add(e.getTarget().getId());
		// }
		// return incidentVertices.size();
		// }
	}

	static class HierarchyComputationResult {
		public final HHLevelStats[] levelStats;
		public final int[] originalVertexIdsToAssignedVertexId;
		public final HHDynamicGraph highwayHierarchy;

		HierarchyComputationResult(LinkedList<HHLevelStats> levelInfo,
				HHDynamicGraph highwayHierarchy, int[] originalVertexIdsToAssignedVertexId) {
			this.levelStats = new HHLevelStats[levelInfo.size()];
			levelInfo.toArray(this.levelStats);
			this.originalVertexIdsToAssignedVertexId = originalVertexIdsToAssignedVertexId;
			this.highwayHierarchy = highwayHierarchy;
		}

		@Override
		public String toString() {
			String str = "";
			for (int i = 0; i < levelStats.length; i++) {
				str += levelStats[i] + "\n";
			}
			return str;
		}
	}

	/**
	 * @param args
	 *            1 argument, filename of the properties file.
	 * @throws SQLException
	 *             shit happens
	 * @throws FileNotFoundException
	 *             shit happens
	 * @throws IOException
	 *             shit happens
	 */
	public static void main(String[] args) throws SQLException, FileNotFoundException,
			IOException {

		// parameters
		int h;
		double c;
		int hopLimit;
		int vertexThreshold;
		boolean downgradeEdges;
		String weightFunction;
		int numThreads;
		Connection inputDb, outputDb;
		File outputFile;
		File highwayLevelToAverageSpeed;

		// initialize parameters
		if (args.length == 1) {
			// get parameters from properties file
			Properties props = new Properties();
			props.load(new FileInputStream(args[0]));

			inputDb = getInputDbConnection(props);
			outputDb = getOutputDbConnection(props);

			h = Integer.parseInt(props.getProperty("preprocessing.param.h"));
			hopLimit = Integer.parseInt(props.getProperty("preprocessing.param.hopLimit"));
			c = Double.parseDouble(props.getProperty("preprocessing.param.c"));
			vertexThreshold = Integer.parseInt(props
					.getProperty("preprocessing.param.vertexThreshold"));
			downgradeEdges = Boolean.parseBoolean(props
					.getProperty("preprocessing.param.downgradeEdges"));
			numThreads = Integer.parseInt(props.getProperty("preprocessing.param.numThreads"));
			outputFile = new File(props.getProperty("output.file"));

			weightFunction = props.getProperty("preprocessing.param.weightFunction");
			highwayLevelToAverageSpeed = new File(props
					.getProperty("preprocessing.param.weightFunction.time.input.file"));
		} else if (args.length == 14) {
			// get parameters from command line
			// this can be removed in near future : currently in use by scripts which will be
			// changed later on.
			h = Integer.parseInt(args[0]);
			c = Double.parseDouble(args[1]);
			hopLimit = Integer.parseInt(args[2]);
			vertexThreshold = 0;
			downgradeEdges = false;
			weightFunction = "TIME";
			numThreads = Integer.parseInt(args[3]);
			inputDb = DBConnection.getJdbcConnectionPg(args[4], Integer.parseInt(args[5]),
					args[6], args[7], args[8]);
			outputDb = DBConnection.getJdbcConnectionPg(args[9], Integer.parseInt(args[10]),
					args[11], args[12], args[13]);
			outputFile = null;
			highwayLevelToAverageSpeed = new File("res/conf/highwayLevel2AverageSpeed.txt");
		} else if (args.length == 18) {
			// get parameters from command line
			h = Integer.parseInt(args[0]);
			c = Double.parseDouble(args[1]);
			hopLimit = Integer.parseInt(args[2]);
			vertexThreshold = Integer.parseInt(args[3]);
			downgradeEdges = Boolean.parseBoolean(args[4]);
			weightFunction = args[5];
			numThreads = Integer.parseInt(args[6]);
			inputDb = DBConnection.getJdbcConnectionPg(args[7], Integer.parseInt(args[8]),
					args[9], args[10], args[11]);
			outputDb = DBConnection.getJdbcConnectionPg(args[12], Integer.parseInt(args[13]),
					args[14], args[15], args[16]);
			outputFile = new File(args[17]);
			highwayLevelToAverageSpeed = new File("res/conf/highwayLevel2AverageSpeed.txt");
		} else {
			usage();
			return;
		}

		// initialize weight function
		IRgWeightFunction<RgEdge> wFunc;
		if (weightFunction != null && weightFunction.equals("DISTANCE")) {
			wFunc = new RgWeightFunctionDistance();
		} else {
			wFunc = new RgWeightFunctionTime(highwayLevelToAverageSpeed);
		}

		// read input database and write to output database
		RgDAO rgDao = new RgDAO(inputDb);
		doPreprocessing(rgDao, wFunc, h, hopLimit, c, vertexThreshold, downgradeEdges,
				numThreads, outputDb);

		// read highway hierarchies from database and create java binary file
		if (outputFile != null) {
			System.out.println("create binary file...");
			File dir = new File(outputFile.getAbsolutePath().substring(0,
					outputFile.getAbsolutePath().lastIndexOf(File.separatorChar))
					+ File.separatorChar);
			dir.mkdirs();
			HHRouterServerside router = HHRouterServerside.getFromDb(outputDb);
			router.serialize(new FileOutputStream(outputFile));
			System.out.println("finished.");
		}
	}

	private static void usage() {
		System.out.println("HHComputation <properties-file>\n");
		System.out
				.println("HHComputation <h> <c> <hopLimit> <numThreads> <in.db.host> <in.db.port> <in.db.name> <in.db.user> <in.db.pass> <out.db.host> <out.db.port> <out.db.name> <out.db.user> <out.db.pass>\n");
		System.out
				.println("HHComputation <h> <c> <hopLimit> <vertexThreshold> <downgradeEdges> <weightFunction> <numThreads> <in.db.host> <in.db.port> <in.db.name> <in.db.user> <in.db.pass> <out.db.host> <out.db.port> <out.db.name> <out.db.user> <out.db.pass> <output.file>\n");

		System.out.println(" <h> neighborhood - [60, 150]");
		System.out.println(" <c> contraction factor - [1.0, 2.0]");
		System.out.println(" <hopLimit> eges per shortcut - [5, 15]");
		System.out
				.println(" <vertexThreshold> create distance table if level has less vertices - [0 .. 10000]");
		System.out
				.println(" <downgrade edges> edges leaving core are downgraded(faster) [true|false]");
		System.out.println(" <weight function> [DISTANCE|TIME]");
		System.out.println(" <numThreads> [1, ?]");
		System.out.println(" <output.file> server side binary file");

	}

	private static Connection getInputDbConnection(Properties props) throws SQLException,
			NumberFormatException {
		String host = props.getProperty("input.db.host");
		int port = Integer.parseInt(props.getProperty("input.db.port"));
		String dbName = props.getProperty("input.db.name");
		String user = props.getProperty("input.db.user");
		String pass = props.getProperty("input.db.pass");

		return new DBConnection(host, dbName, user, pass, port).getConnection();
	}

	private static Connection getOutputDbConnection(Properties props) throws SQLException,
			NumberFormatException {
		String outHostName = props.getProperty("output.db.host");
		int outPort = Integer.parseInt(props.getProperty("output.db.port"));
		String outDbName = props.getProperty("output.db.name");
		String outUser = props.getProperty("output.db.user");
		String outPass = props.getProperty("output.db.pass");
		return new DBConnection(outHostName, outDbName, outUser, outPass, outPort)
				.getConnection();
	}
}

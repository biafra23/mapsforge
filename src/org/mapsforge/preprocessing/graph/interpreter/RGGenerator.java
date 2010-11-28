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

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.hash.TLongHashSet;

import java.sql.Connection;
import java.util.Iterator;
import java.util.LinkedList;

import org.mapsforge.preprocessing.graph.model.gui.Transport;
import org.mapsforge.preprocessing.graph.model.osmxml.OsmWay_withNodes;
import org.mapsforge.preprocessing.graph.osm2rg.osmxml.OsmNode;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgEdge;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgVertex;

/**
 * This class is the routing graph generator. here we get a list of ways from the graph
 * generator service object and build a new routing graph of all these ways.
 * 
 * @author kunis
 */
public class RGGenerator {

	private RGService service;
	private static int doubleSeenCount;

	/**
	 * Constructor to create a new RGGenerator.
	 * 
	 * @param conn
	 *            the connection to the database.
	 */
	public RGGenerator(Connection conn) {

		this.service = new RGService(conn);
		doubleSeenCount = 0;
	}

	/**
	 * This method get a filtered list of ways from the service an create the graph nodes an
	 * edges
	 * 
	 * @param transport
	 *            the transport configuration with the list of ways which the graph should
	 *            contains
	 */
	public void generate(Transport transport) {

		LinkedList<OsmWay_withNodes> ways = service.getWaysForTransport(transport);

		/*
		 * here we check every way node of all ways. the first and last way nodes are nodes in
		 * the graph. also this nodes that were hit by more then one way. all nodes that would
		 * be graph nodes get a new graph node id
		 */
		Iterator<OsmWay_withNodes> it = ways.iterator();
		OsmWay_withNodes way;
		LinkedList<OsmNode> way_nodes;
		int vertex_id = 0;

		TLongIntHashMap graph_nodes = new TLongIntHashMap(); // nodes that are already in the
		// graph
		TLongHashSet seen_nodes = new TLongHashSet(); // nodes that were seen
		LinkedList<RgVertex> vertices = new LinkedList<RgVertex>(); // list of all graph nodes
		long way_node_id;

		System.out.println("suche und erstelle graphknoten");
		System.out.println("ways" + ways.size());
		while (it.hasNext()) {

			way = it.next();
			way_nodes = way.getNodes();

			System.out.println("way " + way.getId() + " has " + way_nodes.size()
					+ " way nods. The first one is " + way_nodes.getFirst().getId()
					+ " and the last one is " + way_nodes.getLast().getId());
			for (OsmNode way_node : way_nodes) {

				way_node_id = way_node.getId();
				// System.out.print(way_node_id + ";");

				// would be seen before
				if (seen_nodes.contains(way_node_id)) {
					/*
					 * yes it would, but it isn't an graph node until yet
					 */
					if (!(graph_nodes.contains(way_node_id))) {
						graph_nodes.put(way_node_id, vertex_id);
						vertices.add(new RgVertex(vertex_id, way_node.getLongitude(), way_node
								.getLatitude(), way_node_id));
						vertex_id++;
					} else {
						doubleSeenCount++;
					}
				} else if (way_node_id == way_nodes.getFirst().getId()
						|| way_node_id == way_nodes.getLast().getId()) {
					/*
					 * is a node that wouldn't seen before. but it is the first or the last node
					 * of a way. so it must be a graph node
					 */
					graph_nodes.put(way_node_id, vertex_id);
					vertices.add(new RgVertex(vertex_id, way_node.getLongitude(), way_node
							.getLatitude(), way_node_id));
					vertex_id++;
					seen_nodes.add(way_node_id);
				} else {
					seen_nodes.add(way_node_id);
				}
				// the node was checked, go to the next one
			}
		}

		System.out.println("insert vertices");
		service.insertVerticesIntoDB(vertices);

		vertices = null;
		way_nodes = null;
		seen_nodes = null;

		/*
		 * create edges now. therefore handle every way again. for every way node calculate the
		 * length to the previous node. is the current node a graph node create an edge to the
		 * last graph node that would seen
		 */

		TDoubleArrayList longitudes = new TDoubleArrayList();
		TDoubleArrayList latitudes = new TDoubleArrayList();
		it = ways.iterator();
		float edge_length;
		int edge_id = 0;
		OsmNode previous_junction_node, previous_node = null;
		long current_node_id;
		// long previous_junction_node_id;
		OsmWay_withNodes current_way = null;
		LinkedList<RgEdge> edges = new LinkedList<RgEdge>();
		// TLongHashSet end_nodes_set;
		// TLongObjectHashMap<TLongHashSet> existing_edges = new
		// TLongObjectHashMap<TLongHashSet>();

		System.out.println("create edges");
		while (it.hasNext()) {

			current_way = it.next();
			previous_node = previous_junction_node = null;
			edge_length = 0;
			for (OsmNode current_node : current_way.getNodes()) {

				// read first or last node of this way, this is always a graph node
				if (previous_node == null || previous_junction_node == null) {
					// just a check
					latitudes.clear();
					longitudes.clear();
					previous_junction_node = current_node;

				} else {
					// current_node has predecessor
					// previous_junction_node_id = previous_junction_node.getId();
					current_node_id = current_node.getId();
					// calculate the length to the predecessor
					edge_length += (float) previous_node.distance(current_node);
					// current_node is no graph node
					if (graph_nodes.contains(current_node_id)) {

						edges.add(new RgEdge(edge_id++, graph_nodes.get(previous_junction_node
								.getId()), graph_nodes.get(current_node.getId()), longitudes
								.toArray(), latitudes.toArray(), false, false, current_way
								.getId(), "testName",
								edge_length, "HighwayLevel", "Ref", false, "destination"));
						// current node is now previous junction node
						previous_junction_node = current_node;
						latitudes.clear();
						longitudes.clear();
					}

					// just a way node, add longitude and latitude the lists, so the edges has
					// all way nodes
					latitudes.add(current_node.getLatitude());
					longitudes.add(current_node.getLongitude());

				}
				// next node
				previous_node = current_node;
			}
		}

		/*
		 * insert nodes an edges
		 */

		service.insertHighwayLevels(transport.getUseableWays());
		System.out.println("insert edges");
		service.insertEdgesIntoDB(edges);
	}

	/**
	 * Sets the double seen counter.
	 * 
	 * @param doubleSeenCount
	 *            the double seen counter.
	 */
	public void setDoubleSeenCount(int doubleSeenCount) {
		RGGenerator.doubleSeenCount = doubleSeenCount;
	}

	/**
	 * Returns the double seen counter.
	 * 
	 * @return the double seen counter.
	 */
	public static int getDoubleSeenCount() {
		return doubleSeenCount;
	}
}

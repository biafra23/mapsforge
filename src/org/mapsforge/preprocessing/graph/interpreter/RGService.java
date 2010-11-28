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
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.mapsforge.preprocessing.graph.model.gui.Transport;
import org.mapsforge.preprocessing.graph.model.osmxml.OsmWay_withNodes;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgEdge;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgVertex;
import org.mapsforge.preprocessing.model.EHighwayLevel;
import org.mapsforge.preprocessing.util.DBConnection;
import org.mapsforge.preprocessing.util.HighwayLevelExtractor;

/**
 * The routing graph service class. Here is the connection to the database wrapper implemented.
 * Furthermore the list of ways would be filtered by the transport configuration. so this part
 * would be abstracted to the graph generation. So it not necessary if the ways get of the
 * database or of an osm extractor class.
 * 
 * @author kunis
 */
public class RGService {

	private OsmBaseService dbService;

	/**
	 * Constructor to create a new RGService with the given database connection.
	 * 
	 * @param conn
	 *            the database connection.
	 */
	public RGService(Connection conn) {
		dbService = new OsmBaseService(conn);
	}

	/**
	 * This method get all ways of the database and filter them.
	 * 
	 * @param transport
	 *            the transport configuration where the graph should be filtered
	 * 
	 * @return a list of ways
	 */
	public LinkedList<OsmWay_withNodes> getWaysForTransport(Transport transport) {

		LinkedList<OsmWay_withNodes> allWays = null;

		try {
			allWays = dbService.getAllWaysFromDB();
		} catch (SQLException e) {
			System.err
					.println("Error: Can't get all ways from the database. Extracting would be canceled.");
			e.printStackTrace();
			System.exit(-1);
		}

		LinkedList<OsmWay_withNodes> useableWays = new LinkedList<OsmWay_withNodes>();

		if (allWays == null) {
			System.err.println("Error: Get no ways from the database.");
			System.exit(-1);
		} else {

			// check for each way if the highway level can used by the transport
			OsmWay_withNodes currentWay;
			EHighwayLevel hwyLvl;
			Iterator<OsmWay_withNodes> it = allWays.iterator();

			while (it.hasNext()) {
				currentWay = it.next();
				hwyLvl = currentWay.getHighwayLevel();
				if (hwyLvl != null) {
					if (transport.getUseableWays().contains(hwyLvl)) {
						useableWays.add(currentWay);
					}
				}
			}
		}

		allWays = null;
		return useableWays;
	}

	/**
	 * This method send a list of vertices to the database wrapper to insert them.
	 * 
	 * @param vertices
	 *            the list of vertices that should added to the database
	 */
	public void insertVerticesIntoDB(LinkedList<RgVertex> vertices) {

		try {
			dbService.insertVertices(vertices);
		} catch (SQLException e) {
			System.err
					.println("Error: An error occurred while inserting the vertices. Extracting would be canceled.");
			e.printStackTrace();
			System.out.println("wie offt besuche ich knoten doppelt?"
					+ RGGenerator.getDoubleSeenCount());
			System.exit(-1);

		}
	}

	/**
	 * This method send a list of edges to the database wrapper to insert them.
	 * 
	 * @param edges
	 *            the list of edges that should added to the database
	 */
	public void insertEdgesIntoDB(LinkedList<RgEdge> edges) {

		try {
			dbService.insertEdges(edges);
		} catch (SQLException e) {
			System.err
					.println("Error: An error occurred while inserting the edges. Extracting would be canceled.");
			e.printStackTrace();
			e.getNextException().printStackTrace();
			System.exit(-1);

		}

	}

	/**
	 * This method send all highway levels that would be needed for the graph to the database to
	 * insert them.
	 * 
	 * @param useableWays
	 *            the set of highway levels that should added to the db
	 */
	public void insertHighwayLevels(HashSet<EHighwayLevel> useableWays) {
		try {
			dbService.insertHighwayLevels(useableWays);
		} catch (SQLException e) {
			System.err
					.println("Error: An error occurred while inserting the edges. Extracting would be canceled.");
			e.printStackTrace();
			e.getNextException().printStackTrace();
			System.exit(-1);

		}

	}

	/**
	 * A main method, just for testing.
	 * 
	 * @param args
	 *            no arguments are needed.
	 */
	public static void main(String[] args) {
		System.out.println("Teste getAllWay methode:");

		RGService s = null;
		DBConnection connection;
		try {
			connection = new DBConnection("U:\\berlin.osm\\preprocessing.properties");
			s = new RGService(connection.getConnection());
		} catch (Exception e) {
			System.err.println("Can't create connection to the datbase.");
			System.exit(-1);
		}
		Transport trans = new Transport("Auto", 100);
		EHighwayLevel hwyLvl = HighwayLevelExtractor.getLevel("secondary");
		if (!trans.addHighwayLevelToUsableWays(hwyLvl)) {
			System.out.println("Fehler");
			System.exit(-1);
		}

		if (s != null) {
			LinkedList<OsmWay_withNodes> test = s.getWaysForTransport(trans);
			System.out.println(test.size());
		}

	}
}

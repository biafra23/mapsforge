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
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.mapsforge.preprocessing.graph.model.gui.Profile;
import org.mapsforge.preprocessing.util.DBConnection;
import org.xml.sax.SAXException;

/**
 * The SimpleRoutingInterpreter is an implementation of an interpreter for the preproccesing of
 * the routing. Here we parse the configuration file to get a profile of a preproccessing turn.
 * This profile consist the information of the database, the used osm file and the information
 * of the transport object.
 * 
 * @author kunis
 * 
 */
public class SimpleRoutingInterpreter implements IInterpreter {

	/**
	 * This method start the preprocessing for a given configuration file
	 */
	public void startPreprocessing(InputStream xmlConfigFile) {

		// first we must parse the xml profile file
		SimpleRoutingConfigurationParser parser = new SimpleRoutingConfigurationParser(
				xmlConfigFile);
		Profile profile = parser.getProfile();

		// debugging
		// System.out.println("name: " + profile.getDbProberties().getDbName() + "; host: "
		// + profile.getDbProberties().getHost());

		// now we create a database connection because that would needed later
		Connection conn = null;
		try {
			conn = new DBConnection(profile.getDbProberties()).getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out
					.println("Can't connect to the Database. Maybe the Proberties are wrong.");
			System.exit(-1);
		}
		// TODO check if file exists
		File osm = getOsmFile(profile.getUrl());
		// System.out.println(osm.getAbsolutePath());

		try {
			// parse osm file an insert into db
			new SimpleOSM2DBParser(conn, osm).parseFile();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// create new graph generator which generate the routing graph
		RGGenerator generator = new RGGenerator(conn);
		generator.generate(profile.getTransport());
	}

	private File getOsmFile(String url) {
		File osmFile = null;
		try {
			osmFile = new FileLoader().getOsmFile(url);
		} catch (Exception e) {

			// System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		if (osmFile == null) {
			System.out.println("NULL");
		}
		return osmFile;
	}

	private static void usage() {
		System.out.println("Usage: SimpleRoutingInterpreter <profile file>");
	}

	/**
	 * Main method, just for testing the execution.
	 * 
	 * @param args
	 *            the configuration file.
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			usage();
		}
		File file = new File(args[0]);

		if (!file.isFile() || !file.getName().endsWith(".xml")) {
			System.out.println("Path is no xml file.");
			usage();
			System.exit(1);
		}

		try {
			InputStream xmlConfigFile = InterpreterMain.class.getResourceAsStream("/res/"
					+ args[0]);
			SimpleRoutingInterpreter sri = new SimpleRoutingInterpreter();
			sri.startPreprocessing(xmlConfigFile);
		} catch (IllegalArgumentException e) {
			System.out.println();
			System.out.println("Can not finde or open configuration file.");
			System.out.println("The file must be stored in the resource folder.");
			System.out.println("<application folder>/res/" + args[0]);

		}
	}

}

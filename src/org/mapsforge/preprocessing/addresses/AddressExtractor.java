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
package org.mapsforge.preprocessing.addresses;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Rect;
import org.mapsforge.preprocessing.util.DBConnection;

/**
 * A tool that extracts street names and their geo location from an OSM database with apiv6
 * schema. If available the tool also extracts the postal code of a street. The tool requires
 * the special view 'streetnames' which is defined in "conf/sql/streetnames_view.sql".
 * 
 * As the OSM format may partition a street into various segments, the extractor performs an
 * algorithm that regroups the segments to a single street. The algorithm computes the bounding
 * box of any segment and compares any pair of streets with the same name and if their bounding
 * boxes overlap or their distance is less than a constant (currently 100m) groups them
 * together.
 * 
 * The geo location of a street with many way points is computed as follows: Build the bounding
 * box of the whole street and compute its center. Then compare every way point with the center
 * and choose the way point which is the closest to the center.
 * 
 * @author bross
 * 
 */
public class AddressExtractor {

	private static final double MAX_DISTANCE = 100d;
	private Connection conn;
	// private SQLiteConnection sqliteConn;
	// private SQLiteStatement pstmtInsertStreet;
	private BufferedWriter bw;
	private int n_joined = 0;

	private static final String GET_STREETS = "SELECT * FROM STREETNAMES";

	/**
	 * Constructor
	 * 
	 * @param propertiesFile
	 *            the properties file containing the database connection data
	 * @param outFile
	 *            the name of the output file
	 */
	public AddressExtractor(String propertiesFile, String outFile) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(propertiesFile));
			DBConnection dbConnection = new DBConnection(propertiesFile);
			conn = dbConnection.getConnection();

			bw = new BufferedWriter(new FileWriter(outFile));

			// sqliteConn = new SQLiteConnection(new File(sqliteFile));
			// sqliteConn.open();
			// sqliteConn.exec("CREATE TABLE IF NOT EXISTS " + "streets ("
			// + SearchManager.SUGGEST_COLUMN_TEXT_1 + " TEXT,"
			// + SearchManager.SUGGEST_COLUMN_TEXT_2
			// + " TEXT, lat INTEGER , lon INTEGER , priority INTEGER)");
			// sqliteConn.exec("CREATE INDEX IF NOT EXISTS " + "name_zip_idx ON streets ("
			// + SearchManager.SUGGEST_COLUMN_TEXT_1 + ","
			// + SearchManager.SUGGEST_COLUMN_TEXT_2 + ")");
			//
			// pstmtInsertStreet = sqliteConn.prepare("INSERT INTO streets "
			// + "VALUES (?,?,?,?,?)");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	public void process() {
		try {

			Statement stmt = conn.createStatement();
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(GET_STREETS);

			// HashMap<String, List<Street>> currentSameName =
			// new HashMap<String, List<AddressExtractor.Street>>();

			ArrayList<Street> currentStreets = new ArrayList<AddressExtractor.Street>();
			int currentID;
			String currentStreetName, previousStreetName = null;
			String currentZip;
			String currentCoordinates = null;
			Street currentStreet = null;
			while (rs.next()) {
				// get current data from DB
				currentID = rs.getInt(1);
				currentStreetName = rs.getString(2);
				currentZip = rs.getString(3);
				currentCoordinates = rs.getString(4);

				// streetnames of current and previous street are NOT equal
				// --> find unique streets from the last group of streets with
				// the same name
				if (!currentStreetName.equals(previousStreetName) && previousStreetName != null) {

					Street street;
					GeoCoordinate center;
					// find unique streets
					ArrayList<ArrayList<Street>> groups = findUniqueStreets(currentStreets);

					// for each unique street, look at the way points and find the center
					// of the street
					for (ArrayList<Street> group : groups) {
						// we just need the name, so any street of the group is fine,
						// we take the first, as this MUST exist
						street = group.get(0);

						// compute the center
						center = getCenterOfStreet(group);

						// write tab-separated data to file
						bw.write(street.name + "\t" + street.zip + "\t"
								+ center.getLatitudeE6() + "\t" + center.getLongitudeE6()
								+ "\t" + 0 + "\n");
						// System.out.println(group.get(0) + "\t" + getCenterOfStreet(group));
						// try {
						// pstmtInsertStreet.bind(1, street.name);
						// pstmtInsertStreet.bind(2, street.zip);
						// pstmtInsertStreet.bind(3, center.getLatitudeE6());
						// pstmtInsertStreet.bind(4, center.getLongitudeE6());
						// pstmtInsertStreet.bind(5, 0);
						// pstmtInsertStreet.step();
						// pstmtInsertStreet.reset();
						// } catch (SQLiteException e) {
						// // TODO Auto-generated catch block
						// e.printStackTrace();
						// }
					}

					currentStreets.clear();
				}

				currentStreet = new Street(currentID, currentStreetName, currentZip,
						currentCoordinates);
				currentStreets.add(currentStreet);
				previousStreetName = currentStreetName;
				// if(currentZip == null){
				// currentZip = "unknown";
				// }
				// List<Street> streets = currentSameName.get(currentZip);
				// if(streets == null){
				// streets = new ArrayList<AddressExtractor.Street>();
				// currentSameName.put(currentZip, streets);
				// }
				// streets.add(currentStreet);

			}
			// System.out.println(n_joined);
			bw.close();
			// pstmtInsertStreet.dispose();
			// sqliteConn.dispose();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Performs the algorithm described in the class javadoc for the given list of streets with
	 * the same name.
	 * 
	 * @param currentStreets
	 *            a list of street segments with the same name
	 * @return groups of street segments where segments in each group have the same name and are
	 *         located close to each other
	 */
	private ArrayList<ArrayList<Street>> findUniqueStreets(ArrayList<Street> currentStreets) {
		ArrayList<Street> streetsToAnalyze = new ArrayList<AddressExtractor.Street>();
		streetsToAnalyze.addAll(currentStreets);
		Street currentStreet;
		Rect currentBBox;
		Street candidate;
		ArrayList<Street> currentGroup;
		ArrayList<ArrayList<Street>> groups = new ArrayList<ArrayList<Street>>();

		// as long as we need to analyze streets
		while (!streetsToAnalyze.isEmpty()) {
			// remove the current street from "queue"
			currentStreet = streetsToAnalyze.remove(0);
			currentBBox = currentStreet.bbox;

			// as this street was not added to some group in
			// the previous iteration it forms its own new group
			currentGroup = new ArrayList<AddressExtractor.Street>();
			currentGroup.add(currentStreet);
			groups.add(currentGroup);
			boolean expandedGroup;
			do {
				expandedGroup = false;
				// look at all remaining streets
				Iterator<Street> it = streetsToAnalyze.iterator();
				while (it.hasNext()) {
					candidate = it.next();
					// if the candidate overlaps the current street,
					// expand the bounding box, to include the candidate and
					// remove the candidate from the streets we need to analyze
					if (currentBBox.overlaps(candidate.bbox)
							|| currentBBox.distance(candidate.bbox) < MAX_DISTANCE) {
						currentBBox.expandToInclude(candidate.bbox);
						currentGroup.add(candidate);
						it.remove();
						expandedGroup = true;
						n_joined++;
					}
				}
				// as we expanded the bounding box of this group,
				// we may have missed a street in a previous iteration
			} while (expandedGroup);
		}

		return groups;
	}

	/**
	 * Computes the center of a group of street segments using the algorithm described in the
	 * class javadoc.
	 * 
	 * @param streetSegments
	 *            the group of street segments
	 * @return the way point on one of the segments which is closest to the center of the
	 *         bounding box framing the whole group
	 */
	private GeoCoordinate getCenterOfStreet(List<Street> streetSegments) {
		Rect bbox = null;
		// create the bounding box that covers all segments
		for (Street street : streetSegments) {
			if (bbox == null)
				bbox = new Rect(street.bbox);
			else
				bbox.expandToInclude(street.bbox);
		}

		if (bbox == null)
			throw new RuntimeException("error while computing the center of bounding box");

		// compute the center
		GeoCoordinate center = bbox.center();
		double minDistance = Double.MAX_VALUE;
		double dist;
		GeoCoordinate nearestToCenter = null;

		// find way point that is closest to the center
		for (Street street : streetSegments) {
			for (GeoCoordinate c : street.coordinates) {
				dist = c.sphericalDistance(center);
				if (dist < minDistance) {
					minDistance = dist;
					nearestToCenter = c;
				}
			}
		}

		return nearestToCenter;
	}

	private class Street {
		int id;
		String name;
		String zip;
		GeoCoordinate[] coordinates;
		Rect bbox;

		public Street(int id, String name, String zip, String coordinates) {
			this.id = id;
			this.name = name;
			this.zip = zip;

			String[] splitCoordinates = coordinates.split(",");
			assert splitCoordinates.length >= 2;
			assert splitCoordinates.length % 2 == 0;

			this.coordinates = new GeoCoordinate[splitCoordinates.length / 2];
			for (int i = 0; i < splitCoordinates.length; i += 2) {
				this.coordinates[i / 2] = new GeoCoordinate(
						Integer.parseInt(splitCoordinates[i]),
						Integer.parseInt(splitCoordinates[i + 1]));
			}

			bbox = new Rect(this.coordinates[0]);
			for (int i = 1; i < this.coordinates.length; i++) {
				bbox.expandToInclude(this.coordinates[i]);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Street [id=" + id + ", name=" + name + ", zip=" + zip + "]";
		}

	}

	/**
	 * Usage: AddressExtractor <properties-file> <out-file>
	 * 
	 * @param args
	 *            args of the main method
	 */
	public static void main(String[] args) {
		AddressExtractor ae = new AddressExtractor(args[0], args[1]);

		long start = System.currentTimeMillis();
		ae.process();
		System.out.println("processing took " + (System.currentTimeMillis() - start) + "ms");
	}

}

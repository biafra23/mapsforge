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
package org.mapsforge.preprocessing.graph.gui.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.regex.PatternSyntaxException;

import org.mapsforge.preprocessing.graph.model.gui.DatabaseProperties;
import org.mapsforge.preprocessing.graph.model.gui.Profile;
import org.mapsforge.preprocessing.graph.model.gui.Transport;
import org.mapsforge.preprocessing.model.EHighwayLevel;
import org.mapsforge.preprocessing.util.HighwayLevelExtractor;

/**
 * This class implements the IDatabaseService class and therefore it abstracts the sqlite
 * database connection.
 * 
 * @author kunis
 */
public class DatabaseService implements IDatabaseService {

	private static Connection con;

	/**
	 * Constructor to create a new database service.
	 * 
	 * @param con
	 *            the database connection.
	 */
	public DatabaseService(Connection con) {
		DatabaseService.setCon(con);
	}

	// Getter

	/**
	 * Returns the database connection.
	 * 
	 * @return the database connection.
	 */
	public Connection getCon() {
		return con;
	}

	// Setter
	/**
	 * Sets the database connection.
	 * 
	 * @param con
	 *            the database connection.
	 */
	public static void setCon(Connection con) {
		DatabaseService.con = con;
	}

	/**
	 * This method initialize the database. This would be needed for the first start at a new
	 * system.
	 */
	public void init() {

		createTables();
		checkDefaultDbConfig();
	}

	@Override
	public void addTransport(Transport transport) {

		String sql = "INSERT INTO transports (transportname, maxspeed, useableways) VALUES (?, ?, ?);";
		try {
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, transport.getName());
			pstmt.setInt(2, transport.getMaxSpeed());
			pstmt.setString(3, transport.getUseableWaysSerialized());
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			throw new IllegalArgumentException(
					"Can't create transport confiuration. There already exits one with this name.");
		}

	}

	@Override
	public void updateTransport(Transport transport) {

		String sql = "UPDATE Transports SET transportname = ?, maxspeed = ?, useableways = ? WHERE transportname = ? ;";
		int update = 0;
		try {
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, transport.getName());
			pstmt.setInt(2, transport.getMaxSpeed());
			pstmt.setString(3, transport.getUseableWaysSerialized());
			pstmt.setString(4, transport.getName());
			update = pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// no update, because this transport, doesn't exists
		if (update == 0)
			throw new NoSuchElementException(
					"There isn't a transport configuration with the name "
							+ transport.getName());

	}

	@Override
	public void deleteTransport(String name) {
		String sql = "DELETE FROM transports WHERE transportname = '" + name + "';";
		Statement stmt;
		int delete = 0;
		try {
			stmt = con.createStatement();
			delete = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// no delete because no transport with this name exists
		if (delete == 0) {
			throw new NoSuchElementException(
					"There isn't a transport configuration with the name " + name + ".");
		}
	}

	@Override
	public Transport getTransport(String transportName) {
		String sql = "SELECT * FROM transports WHERE transportname = ?;";

		ResultSet rs = null;
		String transportname = null;
		int speed = 0;
		String ways = null;
		try {

			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, transportName);
			rs = pstmt.executeQuery();

			if (!rs.next()) {
				throw new NoSuchElementException("There isn't a transport "
						+ "object in the database with the name " + transportName + ".");
			}

			transportname = rs.getString("transportname");
			speed = rs.getInt("maxspeed");
			ways = rs.getString("useableways");

			// an error that should not occurs
			if (rs.next()) {
				throw new NoSuchElementException(
						"There exists a few transport objects in the database with the name "
								+ transportName + ".");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return new Transport(transportname, speed, deserialized(ways));
	}

	@Override
	public ArrayList<Transport> getAllTransports() {

		String sql = "SELECT * FROM transports;";
		Statement stmt;
		ResultSet rs = null;
		ArrayList<Transport> transports = new ArrayList<Transport>();
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql);

			String name;
			int speed;
			String ways;
			while (rs.next()) {
				name = rs.getString("transportname");
				speed = rs.getInt("maxspeed");
				ways = rs.getString("useableways");

				transports.add(new Transport(name, speed, deserialized(ways)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return transports;
	}

	@Override
	public void addProfile(Profile profile) {
		String sql = "INSERT INTO profile (profileName, osm, transport, heuristic) VALUES ( ?, ?, ?, ?);";
		PreparedStatement pstmt;
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, profile.getName());
			pstmt.setString(2, profile.getUrl());
			pstmt.setString(3, profile.getTransport().getName());
			pstmt.setString(4, profile.getHeuristic());
			pstmt.execute();
			pstmt.close();
		} catch (SQLException e) {
			throw new IllegalArgumentException(
					"Can't create profile. There already exists one wiht this name.");
		}
	}

	@Override
	public void updateProfile(Profile p) {
		String sql = "UPDATE Profile SET profilename = ?, osm = ?, transport = ?, heuristic = ? WHERE profilename = ? ;";
		int update = 0;
		try {
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, p.getName());
			pstmt.setString(2, p.getUrl());
			pstmt.setString(3, p.getTransport().getName());
			pstmt.setString(4, p.getHeuristic());
			pstmt.setString(5, p.getName());
			update = pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// no update, because this transport, doesn't exists
		if (update == 0)
			throw new NoSuchElementException("There isn't such a profile  " + p.getName() + ".");
	}

	@Override
	public void deleteProfile(String name) {
		String sql = "DELETE FROM Profile WHERE profilename = '" + name + "';";
		Statement stmt;
		int delete = 0;
		try {
			stmt = con.createStatement();
			delete = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (delete == 0) {
			throw new NoSuchElementException("There isn't such a profile  " + name + ".");
		}

	}

	@Override
	public ArrayList<Profile> getProfilesOfTransport(Transport transport) {
		String sql = "SELECT * FROM profile WHERE transport = ? ;";
		PreparedStatement pstmt;
		ResultSet rs = null;
		ArrayList<Profile> profiles = new ArrayList<Profile>();
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, transport.getName());
			rs = pstmt.executeQuery();
			String transportname, profilname, osm, heuristic;
			Transport trans;
			while (rs.next()) {
				profilname = rs.getString("profilename");
				osm = rs.getString("osm");
				transportname = rs.getString("transport");
				heuristic = rs.getString("heuristic");

				trans = getTransport(transportname);

				profiles.add(new Profile(profilname, osm, trans, heuristic));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return profiles;
	}

	@Override
	public ArrayList<Profile> getAllProfiles() {
		String sql = "SELECT * FROM profile;";
		Statement stmt;
		ResultSet rs = null;
		ArrayList<Profile> profiles = new ArrayList<Profile>();
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql);
			String transportname, profilname, osm, heuristic;
			Transport trans;
			while (rs.next()) {
				profilname = rs.getString("profilename");
				osm = rs.getString("osm");
				transportname = rs.getString("transport");
				heuristic = rs.getString("heuristic");

				trans = getTransport(transportname);

				profiles.add(new Profile(profilname, osm, trans, heuristic));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return profiles;
	}

	/**
	 * This method get the default database configuration.
	 * 
	 * @return the default database configuration in a DatabaseProperties object
	 */
	public DatabaseProperties getDefaultDbConfig() {

		DatabaseProperties dbProps = null;
		String sql = "SELECT * FROM DbConfigurations WHERE id=" + 0 + ";";
		Statement stmt;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql);

			String host, dbname, username, password;
			int port;
			while (rs.next()) {
				host = rs.getString("host");
				dbname = rs.getString("dbname");
				username = rs.getString("username");
				password = rs.getString("password");
				port = rs.getInt("port");

				dbProps = new DatabaseProperties(host, port, dbname, username, password);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dbProps;
	}

	// public DatabaseProperties getDbConfig() {
	//
	// DatabaseProperties dbProps = null;
	// String sql = "SELECT * FROM DbConfigurations WHERE id=" + 0 + ";";
	// Statement stmt;
	// ResultSet rs = null;
	// try {
	// stmt = con.createStatement();
	// rs = stmt.executeQuery(sql);
	//
	// String host, dbname, username, password;
	// int port;
	// while (rs.next()) {
	// host = rs.getString("host");
	// dbname = rs.getString("dbname");
	// username = rs.getString("username");
	// password = rs.getString("password");
	// port = rs.getInt("port");
	//
	// dbProps = new DatabaseProperties(host, port, dbname, username, password);
	// }
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	//
	// return dbProps;
	// }

	/**
	 * This method set the default database configurations.
	 * 
	 * @param dbProps
	 *            the properties of the database
	 */
	public void setDefaultDatabaseConfig(DatabaseProperties dbProps) {

		String sql = "INSERT INTO DbConfigurations (id,host,dbname,username,password,port) VALUES (?,?,?,?,?,?);";
		try {
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, 0);
			pstmt.setString(2, dbProps.getHost());
			pstmt.setString(3, dbProps.getDbName());
			pstmt.setString(4, dbProps.getUsername());
			pstmt.setString(5, dbProps.getPassword());
			pstmt.setInt(6, dbProps.getPort());
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			// e.printStackTrace();
		}
	}

	/**
	 * This method adds a given database property to the database.
	 * 
	 * @param dbProps
	 *            the database properties that should be saved.
	 * @throws Exception
	 *             an Exception if something faild.
	 */
	public void addDatabaseConfig(DatabaseProperties dbProps) throws Exception {

		String sql = "UPDATE DbConfigurations SET id = ?, host = ?, dbname = ?, username = ?, password = ?, port = ? WHERE id ="
				+ 0 + " ;";
		int update = 0;
		try {
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, 0);
			pstmt.setString(2, dbProps.getHost());
			pstmt.setString(3, dbProps.getDbName());
			pstmt.setString(4, dbProps.getUsername());
			pstmt.setString(5, dbProps.getPassword());
			pstmt.setInt(6, dbProps.getPort());
			update = pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (update == 0) {
			throw new Exception(
					"An unexpected error occurs while inserting the configuration into the database.");
		}
	}

	private HashSet<EHighwayLevel> deserialized(String ways) {
		HashSet<EHighwayLevel> hwyLvls = new HashSet<EHighwayLevel>();

		if (ways.length() != 0) {
			String[] pairs = null;
			try {
				pairs = ways.split(";");
			} catch (PatternSyntaxException e) {
				e.printStackTrace();
			}
			if (pairs != null) {
				EHighwayLevel hwyLvl = null;
				for (String pair : pairs) {

					hwyLvl = HighwayLevelExtractor.getLevel(pair);
					if (hwyLvl != null && !hwyLvls.contains(hwyLvl))
						hwyLvls.add(hwyLvl);
				}
			}
		}

		return hwyLvls;
	}

	/*
	 * This is a private method to create all tables
	 */
	private void createTables() {
		String trans, profile, dbConfig;
		trans = "CREATE TABLE IF NOT EXISTS Transports (Transportname VARCHAR(30) PRIMARY KEY, Maxspeed INTEGER, Useableways STRING);";
		profile = "CREATE TABLE IF NOT EXISTS Profile (Profilename VARCHAR(30) PRIMARY KEY, Osm VARCHAR(55), Transport VARCHAR(30), Heuristic VARCHAR(30));";
		dbConfig = "CREATE TABLE IF NOT EXISTS DbConfigurations(id INTEGER PRIMARY KEY, host VARCHAR(30), dbname VARCHAR(30), username VARCHAR(30), password VARCHAR(30), port INTEGER)";
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate(trans);
			stmt.executeUpdate(profile);
			stmt.executeUpdate(dbConfig);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// TODO just a workaround, bug must be fixed
	private void checkDefaultDbConfig() {

		// System.out.println("check");
		// DatabaseProperties dbprops = getDefaultDbConfig();
		// System.out.println(dbprops);

		setDefaultDatabaseConfig(new DatabaseProperties("localhost", 5432, "osm_base",
				"postgres", "bachelor"));

	}

	/*
	 * this method is to drop all tables
	 */
	private void dropTables() {
		String trans, profile, dbConfig;
		trans = "DROP TABLE Transports;";
		profile = "DROP TABLE Profile;";
		dbConfig = "DROP TABLE DbConfigurations;";
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate(trans);
			stmt.executeUpdate(profile);
			stmt.executeUpdate(dbConfig);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * The main method, just for testing and initializing with test data.
	 * 
	 * @param args
	 *            not needed.
	 */
	public static void main(String[] args) {

		DatabaseService dbs = new DatabaseService(new JDBCConnection().getConnection());
		try {
			dbs.dropTables();
			dbs.createTables();
			// dbs.listTables();
			// dbs.getSchema();
			EHighwayLevel hwyLvl1 = HighwayLevelExtractor.getLevel("motorway");
			EHighwayLevel hwyLvl2 = HighwayLevelExtractor.getLevel("track");
			EHighwayLevel hwyLvl3 = HighwayLevelExtractor.getLevel("primary");
			HashSet<EHighwayLevel> set1 = new HashSet<EHighwayLevel>();
			set1.add(hwyLvl1);
			set1.add(hwyLvl3);

			HashSet<EHighwayLevel> set2 = new HashSet<EHighwayLevel>();
			set2.add(hwyLvl2);
			set2.add(hwyLvl3);

			HashSet<EHighwayLevel> set3 = new HashSet<EHighwayLevel>();
			set3.add(hwyLvl2);

			Transport auto = new Transport("Auto", 20, set1);
			dbs.addTransport(auto);
			Transport fahrrad = new Transport("Fahrrad1", 10, set3);
			dbs.addTransport(new Transport("Fahrrad", 10, set2));
			dbs.addTransport(fahrrad);

			dbs.addProfile(new Profile("Testprofil", "keineUrl", auto, "keineHeuristic"));
			dbs.addProfile(new Profile("Testprofil2", "keineUrl", auto, "keineHeuristic"));
			dbs.addProfile(new Profile("Testprofil3", "keineUrl", fahrrad, "keineHeuristic"));

			ArrayList<Profile> profiles = dbs.getAllProfiles();
			for (Profile p : profiles) {
				System.out.println(p.getName());
			}
			DatabaseProperties dbProps = new DatabaseProperties("localhost", 5432, "osm_base",
					"postgres", "bachelor");
			dbs.setDefaultDatabaseConfig(dbProps);
			// dbs.deleteTransport("Motorrad");
			// ArrayList<Transport> testlist = dbs.getAllTransports();
			// for (Transport t : testlist) {
			// System.out.println(t.getId()+": "+t.getName());
			// System.out.println(t.getUseableWaysSerialized());
			// }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbs.getCon().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}

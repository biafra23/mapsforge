package org.mapsforge.preprocessing.osmosis.poi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.mapsforge.poi.persistence.IPersistenceManager;
import org.mapsforge.poi.persistence.PersistenceManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;

abstract class PoiWriterFactory extends TaskManagerFactory {

	private static final String DRIVER = "org.postgresql.Driver";
	private static final String DEFAULT_DB_SERVER = "localhost";
	private static final String DEFAULT_PORT = "5432";
	private static final String DEFAULT_DATABASE = "mapsforge_pois";
	private static final String DEFAULT_USER = "postgres";
	private static final String DEFAULT_PASSWORD = "postgres";
	private static final String DEFAULT_PERST_FILE = "perstPoi.dbs";

	protected List<OsmPoiCategory> categories;
	protected IPersistenceManager persistenceManager;

	private String modus;
	private String database;
	private String username;
	private String password;
	private String protocol;
	private String perstFile;

	protected void handleArguments(TaskConfiguration taskConfig) {
		// Get the task arguments.
		String categoryFileName = getStringArgument(taskConfig, "poiWriter-categoryFile");
		perstFile = getStringArgument(taskConfig, "poiWriter-perstFile", DEFAULT_PERST_FILE);
		database = getStringArgument(taskConfig, "poiWriter-db", DEFAULT_DATABASE);
		username = getStringArgument(taskConfig, "poiWriter-username", DEFAULT_USER);
		password = getStringArgument(taskConfig, "poiWriter-password", DEFAULT_PASSWORD);
		String port = getStringArgument(taskConfig, "poiWriter-port", DEFAULT_PORT);
		String dbServer = getStringArgument(taskConfig, "poiWriter-server", DEFAULT_DB_SERVER);
		modus = getStringArgument(taskConfig, "poiWriter-modus", "perst");
		protocol = "jdbc:postgresql://" + dbServer + ":" + port + "/";

		if (categoryFileName == null) {
			throw new IllegalArgumentException("NO FILE ARGUMENT");
		}

		categories = new PoiCategoryParser().parseFile(categoryFileName);

		if (modus.equalsIgnoreCase("postGis")) {
			persistenceManager = PersistenceManagerFactory
					.getPostGisPersistenceManager(establishDBConnection());
		} else if (modus.equals("perst")) {
			persistenceManager = PersistenceManagerFactory
					.getPerstMultiRtreePersistenceManager(perstFile);
		} else {
			persistenceManager = PersistenceManagerFactory.getDualPersistenceManager(
					establishDBConnection(), perstFile);
		}
	}

	private Connection establishDBConnection() {

		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Unble to connect to Database");
		}

		Connection connection = null;

		try {
			connection = DriverManager.getConnection(protocol + database, username, password);
			System.out.println("DB connection established");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException();
		}

		return connection;
	}

}

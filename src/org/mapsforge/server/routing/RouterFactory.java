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
package org.mapsforge.server.routing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import org.mapsforge.core.DBConnection;
import org.mapsforge.server.routing.highwayHierarchies.HHRouterServerside;

public class RouterFactory {

	private final static String PROPERTIES_FILE = "res/conf/routerFactory.properties";
	private final static Logger logger = Logger.getLogger(RouterFactory.class.getName());

	public static IRouter getRouter() {
		return getRouter(PROPERTIES_FILE);
	}

	public static IRouter getRouter(String fileURI) {
		Properties props = loadProperties(fileURI);
		if (props != null) {
			String algorithm = props.getProperty("algorithm");
			if (algorithm == null) {
				logger.info("No algorithm specified in properties file.");
			} else if (algorithm.equals("hh")) {
				return getHHRouter(props);
			} else {
				logger.info("Algorithm not found : '" + algorithm + "'");
			}
		} else {
			logger.info("Could not Load properties file");
		}
		return null;
	}

	private static IRouter getHHRouter(Properties props) {
		String filename = props.getProperty("hh.file");
		if (filename == null) {
			logger.info("No file name specified for HHRouter.");
			return null;
		}

		HHRouterServerside hhRouter = null;
		// try read from file :
		try {
			FileInputStream iStream = new FileInputStream(filename);
			hhRouter = HHRouterServerside.deserialize(iStream);
			iStream.close();

		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}

		// try get from db :

		if (hhRouter == null) {
			logger.info("Could not load HHRouter from file.");
			Connection conn;
			try {
				String hostName = props.getProperty("hh.input.db.host");
				String dbName = props.getProperty("hh.input.db.name");
				String username = props.getProperty("hh.input.db.user");
				String password = props.getProperty("hh.input.db.pass");
				int port = Integer.parseInt(props.getProperty("hh.input.db.port"));

				conn = DBConnection.getJdbcConnectionPg(hostName, port, dbName, username,
						password);
				hhRouter = HHRouterServerside.getFromDb(conn);
			} catch (SQLException e) {
				logger.info("Could not load HHRouter from db.");
			} catch (Exception e) {
				logger.info("Invalid properties for HHRouter.");
			}

			// try write to file :

			if (hhRouter != null) {
				try {
					File f = new File(filename);
					File dir = new File(f.getAbsolutePath().substring(0,
							f.getAbsolutePath().lastIndexOf(File.separatorChar))
							+ File.separatorChar);
					dir.mkdirs();
					hhRouter.serialize(new FileOutputStream(f));
					logger.info("Written HHRouter to '" + filename + "'.");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					logger.info("Cannot write HHRouter to '" + filename + "'.");
				} catch (IOException e) {
					e.printStackTrace();
					logger.info("Cannot write HHRouter to '" + filename + "'.");
				}
			}
		}
		return hhRouter;
	}

	private static Properties loadProperties(String fileURI) {
		Properties props = null;
		try {
			FileInputStream fis = new FileInputStream(fileURI);
			props = new Properties();
			props.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return props;
	}
}

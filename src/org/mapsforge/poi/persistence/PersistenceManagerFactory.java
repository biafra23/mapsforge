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
package org.mapsforge.poi.persistence;

import java.sql.Connection;

/**
 * Factory providing methods for instantiating {@link IPersistenceManager} implementations.
 * 
 * @author weise
 * 
 */
public class PersistenceManagerFactory {

	/**
	 * @param connection
	 *            {@link Connection} to a PostGis capable PostgreSql database.
	 * @return {@link IPersistenceManager} using an underlying PostGis database.
	 */
	public static IPersistenceManager getPostGisPersistenceManager(Connection connection) {
		return new PostGisPersistenceManager(connection);
	}

	/**
	 * @param filename
	 *            path to perst storage file.
	 * @return {@link IPersistenceManager} using an underlying Perst database.
	 */
	public static IPersistenceManager getPerstMultiRtreePersistenceManager(String filename) {
		return new MultiRtreePersistenceManager(filename);
	}

	/**
	 * @param filename
	 *            path to perst storage file.
	 * @return {@link IPersistenceManager} using an underlying Perst database with multiple
	 *         Hilbert R-Trees as spatial index.
	 */
	public static IPersistenceManager getPerst3DRtreePersistenceManager(String filename) {
		return new Perst3DRtreePersistenceManager(filename);
	}

	/**
	 * @param connection
	 *            {@link Connection} to a PostGis capable PostgreSql database.
	 * @param filename
	 *            path to perst storage file.
	 * @return {@link IPersistenceManager} using both a PostGis and a Perst database.
	 */
	public static IPersistenceManager getDualPersistenceManager(Connection connection,
			String filename) {
		return new DualPersistenceManager(new PostGisPersistenceManager(connection),
				new MultiRtreePersistenceManager(filename));
	}

	/**
	 * @param filename
	 *            path to perst storage file.
	 * @return {@link IPersistenceManager} using an underlying Perst database with multiple
	 *         Guttman R-Tree as spatial index.
	 */
	public static IPersistenceManager getPerstMRtreePersistenceManager(String filename) {
		return new PerstMRtreePersistenceManager(filename);
	}
}

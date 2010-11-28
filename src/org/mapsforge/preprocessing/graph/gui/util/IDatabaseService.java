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

import java.util.ArrayList;

import org.mapsforge.preprocessing.graph.model.gui.Profile;
import org.mapsforge.preprocessing.graph.model.gui.Transport;

/**
 * An interface for the embedded database wrapper class. Here are all functions declared that
 * will be needed for the gui.
 * 
 * @author kunis
 */
public interface IDatabaseService {

	/**
	 * Insert a new transport object into the database.
	 * 
	 * @param transport
	 *            the transport object that should added to the database
	 */
	public void addTransport(Transport transport);

	/**
	 * Update an existing transport object in the database.
	 * 
	 * @param transport
	 *            the object that should be updated
	 */
	public void updateTransport(Transport transport);

	/**
	 * Delete the transport object with the given name of the database.
	 * 
	 * @param name
	 *            the transport name that should be deleted
	 */
	public void deleteTransport(String name);

	/**
	 * Get one transport object with the given name.
	 * 
	 * @param transportName
	 *            the name of the transport object
	 * @return transport the desired transport object
	 */
	public Transport getTransport(String transportName);

	/**
	 * Get all transport objects of the database.
	 * 
	 * @return a list of all transport objects
	 */
	public ArrayList<Transport> getAllTransports();

	/**
	 * Insert a new profile object into the database.
	 * 
	 * @param profile
	 *            the object that should be added
	 */
	public void addProfile(Profile profile);

	/**
	 * Update an existing profile in the database.
	 * 
	 * @param p
	 *            the object that should be updated
	 */
	public void updateProfile(Profile p);

	/**
	 * Delete a profile of the database
	 * 
	 * @param name
	 *            the name of the object that should be deleted
	 */
	public void deleteProfile(String name);

	/**
	 * Get all profiles of a given transport.
	 * 
	 * @param transport
	 *            the transport where the profiles should be
	 * @return a list of profiles of this transport
	 */
	public ArrayList<Profile> getProfilesOfTransport(Transport transport);

	/**
	 * Get all available profiles.
	 * 
	 * @return all profiles
	 */
	public ArrayList<Profile> getAllProfiles();

}

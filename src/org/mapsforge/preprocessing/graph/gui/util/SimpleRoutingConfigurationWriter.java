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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.mapsforge.preprocessing.graph.model.gui.Profile;
import org.mapsforge.preprocessing.model.EHighwayLevel;

import com.thoughtworks.xstream.XStream;

/**
 * This class create XML code of a profile and write this into a file.
 * 
 * @author kunis
 */
public class SimpleRoutingConfigurationWriter {

	private Profile profile;

	/**
	 * The constructor for the XML file creator.
	 * 
	 * @param p
	 *            the profile which should be written into a file
	 */
	public SimpleRoutingConfigurationWriter(Profile p) {

		this.profile = p;
	}

	/**
	 * This method create the XML schema of the profile object and write it into a file.
	 * 
	 * @param configFile
	 *            the file where the XML should be written.
	 * @throws Exception
	 *             raised if the file could not be written.
	 */
	public void writeProfile2File(File configFile) throws Exception {

		// the XStream is a library to create XML of an object
		final XStream xs = new XStream();

		// we must register the classes to create the attribute fields
		xs.alias("profile", Profile.class);
		xs.alias("highway", EHighwayLevel.class);

		// create XML string
		String xml = xs.toXML(profile);

		// write XML into the file
		try {
			FileWriter fw = new FileWriter(configFile);
			fw.write(xml);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			throw new Exception("Can not write configuration file.");
		}
	}
}

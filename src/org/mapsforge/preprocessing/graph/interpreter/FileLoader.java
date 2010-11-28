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
import java.io.FileNotFoundException;

/**
 * The FileLoader to load the osm file of the file system.
 * 
 * @author kunis
 */
public class FileLoader {

	/**
	 * Constructor to create a file loader.
	 */
	public FileLoader() {
		// Actually nothing
	}

	/**
	 * The method to get file of the given URL.
	 * 
	 * @param url
	 *            the path to the file.
	 * @return the OSM file that would be ask for by the given URL.
	 * @throws FileNotFoundException
	 *             file could not be founded, maybe URL is wrong.
	 */
	public File getOsmFile(String url) throws FileNotFoundException {

		// TODO implement a well functioning version. actually the file is just hard coded.
		if (url == "") {
			throw new FileNotFoundException("No value for osm file.");
		}
		String url2 = url;
		url2 = "U:\\berlin.osm\\berlin.osm";
		// String uri = System.getProperty("user.dir");
		// File file = new File(uri + "\\res\\" + url);

		// TODO have to check the file and do a good exception handling

		File file = new File(url2);
		// System.out.println(file.getAbsolutePath());
		if (!file.exists()) {
			throw new FileNotFoundException("OSM file does not exits.");
		}
		// System.out.println("Test");
		return file;
	}
}

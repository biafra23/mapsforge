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
package org.mapsforge.preprocessing.map.tagIdTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Tool to generate the static final tag IDs for the map render.
 * 
 * The input file may contain empty lines and comment lines starting with "//".
 */
class TagIdCreator {
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("missing parameter: inputFile");
			System.exit(1);
		} else if (args.length > 1) {
			System.err.println("bad usage: inputFile");
			System.exit(1);
		}

		// check if the input file exists and is a readable file
		File inputFile = new File(args[0]);
		if (!inputFile.exists()) {
			System.err.println("error: " + inputFile.getAbsolutePath() + " does not exist");
			System.exit(1);
		} else if (!inputFile.isFile()) {
			System.err.println("error: " + inputFile.getAbsolutePath() + " is not a file");
			System.exit(1);
		} else if (!inputFile.canRead()) {
			System.err.println("error: " + inputFile.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}

		// open the file and create a buffered reader for reading line-by-line
		FileInputStream fileInputStream = new FileInputStream(inputFile);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		// read and process all lines until EOF
		String currentLine = bufferedReader.readLine();
		short currentId = 0;
		Map<String, Short> map = new TreeMap<String, Short>();
		while (currentLine != null) {
			if (currentLine.length() == 0) {
				// skip empty line
			} else if (currentLine.startsWith("//")) {
				// skip line with comment
			} else {
				// write the line of java code with the corresponding id
				System.out.println("static final short "
						+ currentLine.replace('=', '$').toUpperCase(Locale.ENGLISH) + " = "
						+ currentId + ";");
				map.put(currentLine, currentId);
				++currentId;
			}
			currentLine = bufferedReader.readLine();
		}

		// write the java code for the map with all entries
		System.out.println();
		System.out.println("static final Map<String, Short> getMap() {");
		System.out.println("	Map<String, Short> map = new HashMap<String, Short>();");

		for (Entry<String, Short> entry : map.entrySet()) {
			System.out.println("	map.put(\"" + entry.getKey() + "\", Short.valueOf((short) "
					+ entry.getValue() + "));");
		}

		System.out.println("	return map;");
		System.out.println("}");

		// close all readers and streams
		bufferedReader.close();
		inputStreamReader.close();
		fileInputStream.close();
	}
}
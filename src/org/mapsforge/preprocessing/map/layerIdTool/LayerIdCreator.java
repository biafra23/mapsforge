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
package org.mapsforge.preprocessing.map.layerIdTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tool to generate the static final layer IDs for the map render.
 * 
 * The input file may contain empty lines and comment lines starting with "//".
 */
class LayerIdCreator {
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
		String[] words;
		byte currentId = 0;
		Map<String, Byte> map = new TreeMap<String, Byte>();
		while (currentLine != null) {
			if (currentLine.length() == 0) {
				// skip empty line
			} else if (currentLine.startsWith("//")) {
				// skip line with comment
			} else {
				words = currentLine.split(" ");
				for (String word : words) {
					// write the current word and ID
					System.out.println("static final byte "
							+ word.replace('=', '$').toUpperCase(Locale.ENGLISH) + " = "
							+ currentId + ";");
					map.put(word, currentId);
				}
				++currentId;
			}
			currentLine = bufferedReader.readLine();
		}

		// close all readers and streams
		bufferedReader.close();
		inputStreamReader.close();
		fileInputStream.close();
	}
}
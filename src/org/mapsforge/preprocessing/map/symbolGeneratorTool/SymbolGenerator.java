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
package org.mapsforge.preprocessing.map.symbolGeneratorTool;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Tool to create PNG files from SVG files, currently used for creating map symbols. It takes
 * two folders as input parameters and runs inkscape on every SVG file from the first folder to
 * create a PNG file in the folder given as second parameter. Already existing files will be
 * overwritten without a warning. During a normal run no textual output is written.
 */
class SymbolGenerator {
	public static void main(String[] args) throws IOException, InterruptedException {
		// check for correct usage
		if (args.length != 2) {
			System.err.println("usage: <sourceDirectory> <targetDirectory>");
			System.exit(1);
		}

		// check for valid source directory parameter
		File sourceDirectory = new File(args[0]);
		if (!sourceDirectory.exists()) {
			System.err.println("error: " + sourceDirectory.getAbsolutePath()
					+ " does not exist");
			System.exit(1);
		} else if (!sourceDirectory.isDirectory()) {
			System.err.println("error: " + sourceDirectory.getAbsolutePath()
					+ " is not a directory");
			System.exit(1);
		} else if (!sourceDirectory.canRead()) {
			System.err.println("error: " + sourceDirectory.getAbsolutePath()
					+ " cannot be read");
			System.exit(1);
		}

		// check for valid target directory parameter
		File targetDirectory = new File(args[1]);
		if (!targetDirectory.exists()) {
			System.err.println("error: " + targetDirectory.getAbsolutePath()
					+ " does not exist");
			System.exit(1);
		} else if (!targetDirectory.isDirectory()) {
			System.err.println("error: " + targetDirectory.getAbsolutePath()
					+ " is not a directory");
			System.exit(1);
		} else if (!targetDirectory.canWrite()) {
			System.err.println("error: " + targetDirectory.getAbsolutePath()
					+ " cannot be written");
			System.exit(1);
		}

		Runtime runtime = Runtime.getRuntime();
		Process process;
		File[] svgFiles = sourceDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".svg");
			}
		});

		// check if the directory has SVG files to process
		if (svgFiles.length == 0) {
			System.err.println("error: " + sourceDirectory.getAbsolutePath()
					+ " contains no SVG files");
			System.exit(1);
		}

		// for each SVG file in the source folder
		for (File file : svgFiles) {
			// convert the SVG file into a PNG file
			process = runtime.exec("inkscape -z --file=" + file.getAbsolutePath()
					+ " --export-area-drawing --export-png="
					+ targetDirectory.getAbsolutePath() + "/"
					+ file.getName().substring(0, file.getName().lastIndexOf('.')) + ".png");
			if (process.waitFor() != 0) {
				System.err.println("error: inkscape exit code = " + process.waitFor());
				System.exit(1);
			}
		}
	}
}
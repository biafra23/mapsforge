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

import java.io.InputStream;

/**
 * This class main class for the interpreter.
 * 
 * @author kunis;
 */
public class InterpreterMain {

	// this method print the usage to the prompt.
	private static void usage() {
		System.out.println("Usage: InterpreterMain <configuration-file>");
		System.exit(-1);
	}

	/**
	 * This method starts the interpreter bye creating a new SimpleRoutingInterpreter
	 * 
	 * @param args
	 *            the configuration file
	 */
	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("Error! No arguments delivered.");
			usage();
		}
		if (args.length > 1) {
			System.out.println("Error! Illegal number of arguments after InterpreterMain.");
			usage();
		}

		try {
			// use a class loader to get the resource file
			InputStream xmlConfigFile = InterpreterMain.class.getResourceAsStream("/res/"
					+ args[0]);
			SimpleRoutingInterpreter sri = new SimpleRoutingInterpreter();
			sri.startPreprocessing(xmlConfigFile);
		} catch (IllegalArgumentException e) {
			System.out.println();
			System.out.println("Can not finde or open configuration file.");
			System.out.println("The file must be stored in the resource folder.");
			System.out.println("<application folder>/res/" + args[0]);

		}

	}
}

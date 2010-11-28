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
package org.mapsforge.preprocessing.graph;

import org.mapsforge.preprocessing.graph.gui.GuiMain;
import org.mapsforge.preprocessing.graph.interpreter.InterpreterMain;

/**
 * This is the main class to start the automation tool.
 * 
 * @author kunis
 */
public class Main {

	private static void usage() {
		System.out
				.println("Usage: Main to start the GUI or Main <configuration-file> to start the interpreter.");
		System.exit(-1);
	}

	/**
	 * This method starts either the GUI or the interpreter.
	 * 
	 * @param args
	 *            the configuration file if the the interpreter should be started
	 */
	public static void main(String[] args) {

		// if there is no argument, we start the gui. otherwise we start the interpreter. more
		// than one parameter is an illegal input.
		if (args.length == 0) {
			GuiMain.main(args);
		} else if (args.length == 1) {
			InterpreterMain.main(args);
		} else {
			System.out.println("Error. Illegal number of arguments after Main.");
			usage();
		}

	}

}

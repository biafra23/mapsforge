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
package org.mapsforge.preprocessing.graph.gui;

/**
 * This class is the main class for the GUI.
 * 
 * @author kunis
 */
public class GuiMain {

	// this method print the usage to the prompt.
	private static void usage() {
		System.out.println("Usage: GuiMain");
		System.exit(-1);
	}

	/**
	 * This method starts the GUI by creating a new MainFrame.
	 * 
	 * @param args
	 *            no arguments expected
	 */
	public static void main(String[] args) {
		if (args.length != 0) {
			System.out.println("Error! Illegal number of arguments after GuiMain.");
			usage();
		}

		MainFrame mf = new MainFrame();
		mf.setVisible(true);
	}

}

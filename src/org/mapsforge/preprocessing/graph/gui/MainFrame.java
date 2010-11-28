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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mapsforge.preprocessing.graph.gui.panels.ConfigurationFilePanel;
import org.mapsforge.preprocessing.graph.gui.panels.DbPreferences;
import org.mapsforge.preprocessing.graph.gui.panels.ProfilePanel;
import org.mapsforge.preprocessing.graph.gui.panels.TransportPanel;
import org.mapsforge.preprocessing.graph.gui.util.DatabaseService;
import org.mapsforge.preprocessing.graph.gui.util.JDBCConnection;

/**
 * This class create the main window and starts all other functions that are needed for the GUI.
 * 
 * @author kunis
 */
public class MainFrame extends JFrame {

	private static final long serialVersionUID = 3109971929230077879L;
	static DatabaseService dbs;

	/**
	 * This is the constructor of the main window. It creates the window and starts all
	 * necessary methods.
	 */
	public MainFrame() {

		super("Preprocessing Configuration");
		// get a jdbc connection for the embedded database
		dbs = new DatabaseService(new JDBCConnection().getConnection());
		init();
	}

	/**
	 * This method is the getter for a DatabaseService object which comprised the connection to
	 * the embedded database.
	 * 
	 * @return a DatabaseService object
	 */
	public DatabaseService getDbService() {
		return dbs;
	}

	/*
	 * This method initialize the main window. That contains the composing of all components and
	 * the adjustment of the visual appearance.
	 */
	private void init() {

		// check if the database is initialized
		// System.out.println("init");
		dbs.init();

		// set the local because this is another workaround for the bugged jfilechooser
		Locale.setDefault(Locale.ENGLISH);
		JComponent.setDefaultLocale(Locale.ENGLISH);

		// set the window to the center of the screen
		this
				.setLocation(
						(Toolkit.getDefaultToolkit().getScreenSize().width - this.getSize().width) / 4,
						(Toolkit.getDefaultToolkit().getScreenSize().height - this.getSize().height) / 4);

		// set the visual appearance
		lookAndFeel();

		// set close operation
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// add an additional menu bar and menus
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new exitAction());
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);

		JMenu preferencesMenu = new JMenu("Preferences");
		menuBar.add(preferencesMenu);

		JMenuItem dbPrefMenuItem = new JMenuItem("Database Preferences");
		dbPrefMenuItem.addActionListener(new openDbPreferences());
		preferencesMenu.add(dbPrefMenuItem);

		// add a tabbed pane to the window, here the several work steps are added
		JTabbedPane mainPanel = new JTabbedPane();

		// create and add the several panels

		final JComponent transportPanel = new TransportPanel(dbs);
		final JComponent profilePanel = new ProfilePanel(dbs);
		final JComponent configurationFilePanel = new ConfigurationFilePanel(dbs);

		// this mouse listener check the database for new transport configurations and profiles,
		// that would be created at this season
		mainPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				((ProfilePanel) profilePanel).initialize();
				((ConfigurationFilePanel) configurationFilePanel).initialize();
			}
		});

		mainPanel.addTab("transport configuration", transportPanel);
		mainPanel.addTab("profile configuration", profilePanel);
		mainPanel.add("create configuration file", configurationFilePanel);

		this.add(mainPanel);
		this.pack();
	}

	/*
	 * This method configure the visual appearance for the window.
	 */
	private void lookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * This is the main method of the main window. Herewith could it be started.
	 * 
	 * @param args
	 *            no arguments would needed
	 */
	public static void main(String[] args) {
		MainFrame mf = new MainFrame();
		mf.setVisible(true);
	}

	/*
	 * This is an action listener class to close the window by using the menu item exit.
	 */
	class exitAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}

	}

	/*
	 * This is an action listener class to open the database preference window set up their
	 * preferences.
	 */
	class openDbPreferences implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			DbPreferences dbPrefs = new DbPreferences(dbs);
			dbPrefs.setVisible(true);
		}
	}

}

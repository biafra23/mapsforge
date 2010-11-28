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
package org.mapsforge.preprocessing.graph.gui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.mapsforge.preprocessing.graph.gui.util.DatabaseService;
import org.mapsforge.preprocessing.graph.gui.util.SimpleRoutingConfigurationWriter;
import org.mapsforge.preprocessing.graph.model.gui.DatabaseProperties;
import org.mapsforge.preprocessing.graph.model.gui.Profile;

/**
 * This is the class for the configuration panel
 * 
 * @author kunis
 */
public class ConfigurationFilePanel extends JPanel {

	private static final long serialVersionUID = -489116927561664972L;
	private DatabaseService dbs;
	JFileChooser fc;

	private JComboBox cbChooseProfile;
	JTextField tfFilePath;

	/**
	 * The constructor to create a configuration tab.
	 * 
	 * @param dbs
	 *            a database service object to connect to the embedded database
	 */
	public ConfigurationFilePanel(DatabaseService dbs) {
		this.dbs = dbs;
		this.setLayout(new BorderLayout());

		JPanel panel = new JPanel(new BorderLayout());
		this.add(panel, BorderLayout.WEST);

		panel.add(getCreatePanel(), BorderLayout.CENTER);

		init();
	}

	/**
	 * This method checks reload the profiles, maybe there would be created a new one during
	 * this season.
	 */
	public void initialize() {
		getProfiles();
	}

	/*
	 * this method initialize the tab
	 */
	private void init() {

		this.fc = new JFileChooser(System.getProperty("user.dir")) {

			/*
			 * this is a workaround to use the jfilechooser class without losing system
			 * resources
			 */
			private static final long serialVersionUID = -832241031328432102L;

			@Override
			public void updateUI() {
				putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
				super.updateUI();
			}
		};

		// fill the cbChooseProfile with profiles
		getProfiles();
	}

	// get all profiles
	private void getProfiles() {
		ArrayList<Profile> alProfiles = dbs.getAllProfiles();
		cbChooseProfile.removeAllItems();
		for (Profile p : alProfiles) {
			cbChooseProfile.addItem(p);
		}

	}

	/*
	 * this method create components and add them to the panel
	 */
	private Component getCreatePanel() {

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		// add border
		panel.setBorder(BorderFactory.createTitledBorder(null, "config file",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
						"Dialog", Font.PLAIN, 11), Color.BLACK));

		// set constraints
		constraints.insets = new Insets(5, 5, 0, 5);
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("choose profile:"), constraints);

		constraints.gridy = 1;
		cbChooseProfile = new JComboBox();
		panel.add(cbChooseProfile, constraints);

		constraints.gridy = 2;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(new JLabel("save as:"), constraints);

		constraints.gridy = 3;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		tfFilePath = new JTextField();
		panel.add(tfFilePath, constraints);

		// constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridx = 4;
		JButton bSaveConfigFile = new JButton("save as");
		panel.add(bSaveConfigFile, constraints);
		bSaveConfigFile.addActionListener(new saveConfigurationFile());

		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weighty = 1.0;

		constraints.gridy = 5;
		constraints.gridx = 2;
		// constraints.gridwidth = GridBagConstraints.RELATIVE;

		JButton bCreateConfigFile = new JButton("create config file");
		panel.add(bCreateConfigFile, constraints);
		bCreateConfigFile.addActionListener(new createConfigurationFile());

		return panel;
	}

	/*
	 * This method creates checked all parameters and create a writer object to create a
	 * configuration file
	 */
	void createNewConfigurationFile() {

		// check if profile is selected
		Profile profile = (Profile) cbChooseProfile.getSelectedItem();
		if (profile == null) {
			String message = "No profile is selected. Can't create a config file without get any profile.";
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// check if file name is inserted
		String uri = tfFilePath.getText();
		if (uri == "") {
			String message = "Can't create config file. Please insert a file name.";
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!uri.endsWith(".config")) {
			if (uri.contains(".")) {
				String message = "Can't create config file. The file must end on .config.";
				JOptionPane
						.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			uri.concat(".config");
		}

		// check if file exists
		File file = new File(uri);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			int answer = JOptionPane.showConfirmDialog(null,
					"Do you really want to overrite the old config file?",
					"overwrite config file", JOptionPane.YES_NO_OPTION);
			if (answer != 0) {
				return;
			}
		}

		DatabaseProperties probs = dbs.getDefaultDbConfig();
		profile.setDbProberties(probs);

		// the class which create the file
		SimpleRoutingConfigurationWriter writer = new SimpleRoutingConfigurationWriter(profile);
		try {
			writer.writeProfile2File(file);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	class saveConfigurationFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			fc.setLocale(Locale.ENGLISH);
			int returnVal = fc.showSaveDialog(ConfigurationFilePanel.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				tfFilePath.setText(fc.getSelectedFile().toString());
			}
		}
	}

	class createConfigurationFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			createNewConfigurationFile();
		}
	}
}

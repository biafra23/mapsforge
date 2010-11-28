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
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.mapsforge.preprocessing.graph.gui.util.DatabaseService;
import org.mapsforge.preprocessing.graph.model.gui.Profile;
import org.mapsforge.preprocessing.graph.model.gui.Transport;

/**
 * This is the class for the profile configuration panel.
 * 
 * @author kunis
 */
public class ProfilePanel extends JPanel {

	private static final long serialVersionUID = 7197827414427804065L;

	/**
	 * This is a jdbc connection to connect with the embedded database. This would be needed to
	 * load and also store new or changed profile configurations.
	 */
	private DatabaseService dbs;

	JComboBox cbChooseProfile;
	JComboBox cbChooseTransportOnManagePanel;
	private JComboBox cbChooseTransportOnConfigurationPanel;
	private JComboBox cbChooseHeuristic;
	private JTextField tfProfileName;
	private JTextField tfUrl;

	/**
	 * The constructor creates a main panel for this tab which is left-aligned. Here we add the
	 * attribute and the manage panels where all the elements are contained.
	 * 
	 * @param dbs
	 *            connection to the embedded database to load/store transport configurations
	 */
	public ProfilePanel(DatabaseService dbs) {
		this.dbs = dbs;

		JPanel panel = new JPanel(new BorderLayout());
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.WEST);

		// add the panels with the elements
		panel.add(getConfigurationPanel(), BorderLayout.WEST);
		panel.add(getManagePanel(), BorderLayout.EAST);

		// initialize the elements
		setComboBoxChooseTransport();
		setComboBoxChooseHeuristic();
		// the profiles are null, because no transport is selected first
		setComboBoxChooseProfile(null);
	}

	/**
	 * This method checks reload the transports, maybe there would be created a new one during
	 * this season.
	 */
	public void initialize() {
		setComboBoxChooseTransport();
	}

	/*
	 * This method create the manage panel and their elements. the event handling for the
	 * buttons is forwarded to the corresponding methods.
	 */
	private Component getManagePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		// add border
		panel.setBorder(BorderFactory.createTitledBorder(null, "manage profiles",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
						"Dialog", Font.PLAIN, 11), Color.BLACK));

		// set constraints
		constraints.insets = new Insets(5, 5, 0, 5);
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		// add labels and comboboxes to the panel

		panel.add(new JLabel("select an existing transport"), constraints);
		constraints.gridy = 1;
		cbChooseTransportOnManagePanel = new JComboBox();
		panel.add(cbChooseTransportOnManagePanel, constraints);

		constraints.gridy = 2;
		panel.add(new JLabel("select an existing profile"), constraints);

		constraints.gridy = 3;
		cbChooseProfile = new JComboBox();
		panel.add(cbChooseProfile, constraints);

		// add buttons to the panel
		constraints.gridy = 4;
		JButton bSaveProfile = new JButton("save profile");
		panel.add(bSaveProfile, constraints);
		constraints.gridy = 5;
		JButton bCreateProfile = new JButton("create new profile");
		panel.add(bCreateProfile, constraints);
		constraints.gridy = 6;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weighty = 1.0;
		JButton bDeleteProfile = new JButton("delete profile");
		panel.add(bDeleteProfile, constraints);

		// add action listener to the combobox that fills also the profile combobox if a
		// transport would selected
		cbChooseTransportOnManagePanel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setComboBoxChooseProfile((Transport) cbChooseTransportOnManagePanel
						.getSelectedItem());
			}
		});

		// add action listener to the combobox. so the attributes of a profile would be shown
		// when one would be selected
		cbChooseProfile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				showChoosenProfile((Profile) cbChooseProfile.getSelectedItem());
			}
		});

		// add action listeners to the buttons
		// starts the save event
		bSaveProfile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateProfileToDB();
			}
		});

		// starts the create event
		bCreateProfile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				createProfilInDB();
			}
		});

		// starts the delete event
		bDeleteProfile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteProfileFromDB();
			}
		});

		return panel;
	}

	/*
	 * This method creates the configuration panel with the all the attribute elements for a
	 * profile configuration. Here is also the event handling implemented, so that the the
	 * requests would be forwarded to the corresponding methods.
	 */
	private Component getConfigurationPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		// add border
		panel.setBorder(BorderFactory.createTitledBorder(null, "profile attributes",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
						"Dialog", Font.PLAIN, 11), Color.BLACK));

		// set constraints
		constraints.insets = new Insets(5, 5, 0, 5);
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		// add labels to first row
		panel.add(new JLabel("name of the profile: "), constraints);

		// create textfields
		tfProfileName = new JTextField();
		tfUrl = new JTextField();

		// add textfield to second row

		constraints.gridy = 1;
		constraints.gridx = 0;
		panel.add(tfProfileName, constraints);

		// add labels
		constraints.gridy = 3;
		constraints.gridx = 0;
		panel.add(new JLabel("used transport configuration:"), constraints);
		constraints.gridx = 1;
		panel.add(new JLabel("used heuristic configuration:"), constraints);

		// create comboBoxen
		cbChooseTransportOnConfigurationPanel = new JComboBox();
		cbChooseHeuristic = new JComboBox();

		// add comboBoxen

		constraints.gridy = 4;
		constraints.gridx = 0;
		panel.add(cbChooseTransportOnConfigurationPanel, constraints);
		constraints.gridx = 1;
		panel.add(cbChooseHeuristic, constraints);

		// add url label

		constraints.gridy = 5;
		constraints.gridx = 0;
		panel.add(new JLabel("used osm file: "), constraints);

		// change constraints
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weighty = 1.0;
		constraints.gridheight = 2;

		// add url components

		constraints.gridy = 6;
		constraints.gridx = 0;
		panel.add(tfUrl = new JTextField(".osm"), constraints);

		return panel;
	}

	/*
	 * This method parse the attributes of the profile from the input fields
	 */
	private Profile getProfileFromInput() {

		String profileName = tfProfileName.getText();
		String url = tfUrl.getText();
		String heuristic = null;
		Transport transport = null;

		if (profileName.equals("")) {
			throw new IllegalArgumentException("No valid profile name would be inserted.");
		}

		if (url.equals("")) {
			throw new IllegalArgumentException("No valid url would be inserted.");
		}

		transport = (Transport) cbChooseTransportOnConfigurationPanel.getSelectedItem();

		if (transport == null) {
			throw new IllegalArgumentException("No valid transport would be selected.");
		}

		heuristic = (String) cbChooseHeuristic.getSelectedItem();

		if (heuristic == null) {
			throw new IllegalArgumentException("No valid heurstic would be inserted.");
		}
		return new Profile(profileName, url, transport, heuristic);
	}

	/*
	 * This method update an existing profile in the database.
	 */
	void updateProfileToDB() {
		Profile p = null;
		try {
			p = getProfileFromInput();
			// before overwrite we ask the user for the last time
			int answer = JOptionPane
					.showConfirmDialog(
							null,
							"You will overwrite an existing profile. The old data would be lost. Are you sure to do this?",
							"overwrite profile", JOptionPane.YES_NO_OPTION);
			if (answer == 0) {
				// if he wanted to update we do it
				dbs.updateProfile(p);
			}
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		} catch (NoSuchElementException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// update was successful so the choose fields must be updated
		setComboBoxChooseProfile(p.getTransport());
	}

	/*
	 * This method creates a new profile in the database
	 */
	void createProfilInDB() {
		Profile p = null;
		try {
			p = getProfileFromInput();
			dbs.addProfile(p);
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		setComboBoxChooseTransport();
		setComboBoxChooseProfile(null);
	}

	/*
	 * This method delete an existing profile of the database
	 */
	void deleteProfileFromDB() {
		// get name of profile that should be deleted
		String name = tfProfileName.getText();
		if (name.equals(null) || name.equals("")) {
			JOptionPane.showMessageDialog(this,
					"There is no name inserted for the profile that should be deleted.",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			int answer = JOptionPane.showConfirmDialog(null,
					"Are you sure to delete this profile?", "delete profile",
					JOptionPane.YES_NO_OPTION);

			if (answer == 0) {
				try {
					dbs.deleteProfile(name);
				} catch (NoSuchElementException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				// after deleting we update the choose fields
				setComboBoxChooseTransport();
				setComboBoxChooseProfile(null);
			}
		}

	}

	/*
	 * This method show the attributes of a selected profile
	 */
	void showChoosenProfile(Profile p) {

		// no profile so no attributes must shown
		if (p == null) {
			// no profile is selected
			tfProfileName.setText("");
			tfUrl.setText("");
			cbChooseHeuristic.setSelectedItem(null);
			// we choose a transport at the right site so the left one should also be selected
			// this transport
			cbChooseTransportOnConfigurationPanel.getModel().setSelectedItem(
					cbChooseTransportOnManagePanel.getModel().getSelectedItem());

		} else {

			// we want to to show the selected item in all choose fields
			cbChooseProfile.getModel().setSelectedItem(p);
			cbChooseTransportOnManagePanel.getModel().setSelectedItem(p.getTransport());

			// a profile would selected, so the attributes must be shown
			tfProfileName.setText(p.getName());
			cbChooseTransportOnConfigurationPanel.getModel().setSelectedItem(p.getTransport());
			cbChooseHeuristic.getModel().setSelectedItem(p.getHeuristic());
			tfUrl.setText(p.getUrl());
		}

	}

	/*
	 * after choosing a transport, we must get all profiles of this one an add them to the
	 * profile choosing field
	 */
	void setComboBoxChooseProfile(Transport transport) {
		cbChooseProfile.removeAllItems();
		ArrayList<Profile> alProfiles = null;
		if (transport == null) {
			// no transport specified, so all profiles would be shown
			alProfiles = dbs.getAllProfiles();
		} else {
			// show all profiles of transport
			alProfiles = dbs.getProfilesOfTransport(transport);
		}
		if (alProfiles.size() > 0) {
			// if there are any profiles for this transport, we add them to the combobox
			cbChooseProfile.addItem(null);
			for (Profile p : alProfiles) {
				cbChooseProfile.addItem(p);
			}
		} else {
			// there are no profiles for this transport or no profiles at all. so the user would
			// be informed
			// the info message is annoying!!!
			/*
			 * String message; if (transport == null) { message =
			 * "There exists no profiles in the Database. Please create one at first."; } else {
			 * message = "There exists no profiles for the selected transport " +
			 * transport.getName() + ". Please create on at first."; }
			 * 
			 * JOptionPane.showMessageDialog(this, message, "Information",
			 * JOptionPane.INFORMATION_MESSAGE);
			 */
		}
	}

	/*
	 * insert all transports to the comboboxes
	 */
	private void setComboBoxChooseTransport() {
		cbChooseTransportOnManagePanel.removeAllItems();
		cbChooseTransportOnConfigurationPanel.removeAllItems();
		ArrayList<Transport> alTransports = dbs.getAllTransports();

		if (alTransports != null) {
			// add a null element to the right combobox so the user can delete all attribute
			// fields
			cbChooseTransportOnManagePanel.addItem(null);
			for (Transport t : alTransports) {
				cbChooseTransportOnManagePanel.addItem(t);
				cbChooseTransportOnConfigurationPanel.addItem(t);
			}
		}

	}

	private void setComboBoxChooseHeuristic() {
		cbChooseHeuristic.removeAllItems();

		cbChooseHeuristic.addItem("DEFAULT");
	}

}

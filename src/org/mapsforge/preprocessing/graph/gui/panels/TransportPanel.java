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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import org.mapsforge.preprocessing.graph.gui.util.DatabaseService;
import org.mapsforge.preprocessing.graph.model.gui.Transport;
import org.mapsforge.preprocessing.model.EHighwayLevel;
import org.mapsforge.preprocessing.util.HighwayLevelExtractor;

/**
 * This is the class for the transport configuration panel.
 * 
 * @author kunis
 */
public class TransportPanel extends JPanel {

	private static final long serialVersionUID = 7494776847583748626L;

	/**
	 * This is a jdbc connection to connect with the embedded database. This would be needed to
	 * load and also store new or changed transport configurations.
	 */
	private DatabaseService dbs;

	JComboBox cbChooseTransport;
	private JTextField tfTransportName;
	private JFormattedTextField ftfTransportMaxSpeed;
	JList jlTransportUsableWays;
	JList jlAllUsableWays;
	private DefaultListModel dlmListModel;

	/**
	 * The constructor creates a main panel for this tab which is left-aligned. Here we add the
	 * attribute and the manage panels where all the elements are contained.
	 * 
	 * @param dbs
	 *            connection to the embedded database to load/store transport configurations
	 */
	public TransportPanel(DatabaseService dbs) {

		this.dbs = dbs;
		this.setLayout(new BorderLayout());

		JPanel panel = new JPanel(new BorderLayout());
		this.add(panel, BorderLayout.WEST);

		// add the panels with the elements
		panel.add(getAttributePanel(), BorderLayout.WEST);
		panel.add(getManagePanel(), BorderLayout.EAST);

		// initialize the elements
		setComboBoxChooseTransport();
		setJListAllUseabaleWayTags();
	}

	/*
	 * This method creates the attribute panel with the all the attribute elements for a
	 * transport configuration. Here is also the event handling implemented, so that the the
	 * requests would be forwarded to the corresponding methods.
	 */
	private JPanel getAttributePanel() {

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		// add border
		panel.setBorder(BorderFactory.createTitledBorder(null, "transport attributes",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
						"Dialog", Font.PLAIN, 11), Color.BLACK));

		// set constraints
		constraints.insets = new Insets(5, 5, 0, 5);
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		// add labels to the first row
		panel.add(new JLabel("name: "), constraints);
		constraints.gridx = 2;
		panel.add(new JLabel("maximum speed: "), constraints);

		// create textfields
		tfTransportName = new JTextField();
		ftfTransportMaxSpeed = new JFormattedTextField(NumberFormat.getNumberInstance());

		// add textfields to the second row
		constraints.gridy = 1;
		constraints.gridx = 0;
		panel.add(tfTransportName, constraints);
		constraints.gridx = 2;
		panel.add(ftfTransportMaxSpeed, constraints);

		// add labels to the third row
		constraints.gridy = 3;
		constraints.gridx = 0;
		panel.add(new JLabel("useabel ways:"), constraints);
		constraints.gridx = 2;
		panel.add(new JLabel("available ways:"), constraints);

		// create buttons and actions listeners
		JButton b_RemoveTagFromTransport = new JButton(">>");
		// this action listener is to remove the selected items of the usable ways
		b_RemoveTagFromTransport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteTagsFromList(jlTransportUsableWays.getSelectedValues());

			}
		});

		// this action listener is to add the selected items from all ways to the usable ways
		JButton b_AddTagToTransport = new JButton("<<");
		b_AddTagToTransport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addTagsToList(jlAllUsableWays.getSelectedValues());

			}
		});

		// add buttons the the second column
		constraints.gridx = 1;
		constraints.gridy = 4;
		panel.add(b_AddTagToTransport, constraints);
		constraints.gridy = 5;
		panel.add(b_RemoveTagFromTransport, constraints);

		// create lists
		jlAllUsableWays = new JList();
		jlAllUsableWays.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jlAllUsableWays.setLayoutOrientation(JList.VERTICAL);
		jlAllUsableWays.setVisibleRowCount(1);
		jlAllUsableWays.setBackground(Color.WHITE);
		JScrollPane allUseableWaysScrollPane = new JScrollPane(jlAllUsableWays);
		allUseableWaysScrollPane.setPreferredSize(new Dimension(150, 60));

		dlmListModel = new DefaultListModel();
		jlTransportUsableWays = new JList(dlmListModel);
		jlTransportUsableWays.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jlTransportUsableWays.setLayoutOrientation(JList.VERTICAL);
		jlTransportUsableWays.setVisibleRowCount(1);
		jlTransportUsableWays.setBackground(Color.WHITE);
		JScrollPane transportUseableWaysScrollPane = new JScrollPane(jlTransportUsableWays);
		transportUseableWaysScrollPane.setPreferredSize(new Dimension(150, 60));

		// change constraints
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weighty = 1.0;
		constraints.gridy = 4;
		constraints.gridheight = 2;

		// add lists into the last row
		constraints.gridx = 0;
		panel.add(transportUseableWaysScrollPane, constraints);
		constraints.gridx = 2;
		panel.add(allUseableWaysScrollPane, constraints);

		return panel;
	}

	/*
	 * This method create the manage panel and their elements. the event handling for the
	 * buttons is forwarded to the corresponding methods.
	 */
	private JPanel getManagePanel() {

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		// add border
		panel.setBorder(BorderFactory.createTitledBorder(null, "manage transports",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
						"Dialog", Font.PLAIN, 11), Color.BLACK));
		// set constraints
		constraints.insets = new Insets(5, 5, 0, 5);
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		// add combobox to the panel
		panel.add(new JLabel("select an existing transport:"), constraints);
		constraints.gridy = 1;
		cbChooseTransport = new JComboBox();
		panel.add(cbChooseTransport, constraints);

		// add buttons to the panel
		constraints.gridy = 2;
		JButton bSaveTransport = new JButton("save existing configuration");
		panel.add(bSaveTransport, constraints);
		constraints.gridy = 3;
		JButton bCreateTransport = new JButton("create a new configuration");
		panel.add(bCreateTransport, constraints);
		constraints.gridy = 4;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weighty = 1.0;
		JButton bDeleteTransport = new JButton("delete existing configuration");
		panel.add(bDeleteTransport, constraints);

		// add action listener to the combobox
		// this action listener is to show a the selected transport on the attribute panel
		cbChooseTransport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				showChoosenTransport((Transport) cbChooseTransport.getSelectedItem());
			}
		});

		// add action listeners to the buttons

		// this action listener is to save a transport after clicking the save button
		bSaveTransport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateTransportToDB();
			}
		});

		// this action listener is to create a transport after clicking the create button
		bCreateTransport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				createTransportInDB();
			}
		});

		// this action listener is to delete a transport after clicking the delete button
		bDeleteTransport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteTransportFromDB();
			}
		});

		return panel;
	}

	/*
	 * This method adds all objects from the array selectedValues to the dl_listModel, which is
	 * the model of the jl_TransportUseableWays. There are only this objects added, which are
	 * not in the list before.
	 */
	void addTagsToList(Object[] selectedValues) {

		for (Object obj : selectedValues) {
			if (dlmListModel.lastIndexOf(obj) == -1) {
				dlmListModel.addElement(obj.toString());
			} else {
				// if the current element is already in the list, the would be informed
				JOptionPane.showMessageDialog(this, "This tag is already in the list.",
						"Information", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	/*
	 * This method deletes all the selected of the jl_TransportUseableWays.
	 */
	void deleteTagsFromList(Object[] selectedValues) {

		for (Object obj : selectedValues) {
			dlmListModel.removeElement(obj);
		}
	}

	/*
	 * This method adds a new transport to the embedded database. If there are any Problems,
	 * like the database connection get lost or the table did not exists, then an info panel
	 * informed the user.
	 */
	void createTransportInDB() {

		Transport t = null;
		try {
			// call the method which parse the input
			t = getTransportFromInput();
			dbs.addTransport(t);
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// update choice field
		setComboBoxChooseTransport();
	}

	/*
	 * This method update an existing configuration. If any error occurred the user would be
	 * informed. After updating the panel elements would be updated.
	 */
	void updateTransportToDB() {
		Transport t = null;
		try {
			// call the method which parse the input
			t = getTransportFromInput();
			dbs.updateTransport(t);
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Fehler",
					JOptionPane.ERROR_MESSAGE);
			return;
		} catch (NoSuchElementException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Fehler",
					JOptionPane.ERROR_MESSAGE);
		}

		// update choice field and attribute fields
		setComboBoxChooseTransport();
		showChoosenTransport(t);

	}

	/*
	 * This method delete an existing transport configuration. To get the right one, the current
	 * name of attribute field for the transport name would be parsed an a look up into the
	 * database would be started. If the transport configuration for this name exists the user
	 * would be asked if he really want do delete this. Depending on his answer the action would
	 * be done. The user would also be informed if no configuration with the given name exists.
	 */
	void deleteTransportFromDB() {
		// get the name of the configuration which would deleted
		String name = tfTransportName.getText();
		if (name.equals(null) || name.equals("")) {
			JOptionPane.showMessageDialog(this, "You insert no value for the transport name.",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			int answer = JOptionPane.showConfirmDialog(null,
					"Are you sure to delete this transport configuration?",
					"Delete Transport Configuration?", JOptionPane.YES_NO_OPTION);

			// the user wants to delete this configuration
			if (answer == 0) {
				try {
					dbs.deleteTransport(name);
				} catch (NoSuchElementException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				setComboBoxChooseTransport();
			}
		}
	}

	/*
	 * This method parse all attributes of the attributes fields and check their validness.
	 */
	private Transport getTransportFromInput() {
		String name;
		int maxspeed = -1;
		ArrayList<String> ways = new ArrayList<String>();
		name = tfTransportName.getText();
		if (name.equals(null) || name.equals("")) {
			throw new IllegalArgumentException("You insert no value for the transport name.");
		}
		try {
			maxspeed = ((Number) ftfTransportMaxSpeed.getValue()).intValue();
		} catch (Exception e) {
			throw new IllegalArgumentException("You insert no value for the maximum speed.");
		}

		if (maxspeed <= 0) {
			throw new IllegalArgumentException(
					"You insert a invalid value for the maximum speed.");
		}
		Enumeration<?> e = dlmListModel.elements();
		while (e.hasMoreElements()) {
			ways.add(e.nextElement().toString());

		}
		if (ways.size() == 0) {
			throw new IllegalArgumentException(
					"There are no ways added for this transport configuration.");
		}

		// the transport object could be created if all parameters are parsed and valid
		return new Transport(name, maxspeed, StringListToHighwaySet(ways));
	}

	/*
	 * This method gets a list of ways and check if they are valid highways. The ways are also
	 * added to a set for better look up.
	 */
	private HashSet<EHighwayLevel> StringListToHighwaySet(ArrayList<String> ways) {
		HashSet<EHighwayLevel> result = new HashSet<EHighwayLevel>();
		EHighwayLevel hwyLvl = null;
		for (String way : ways) {
			hwyLvl = HighwayLevelExtractor.getLevel(way);
			if (hwyLvl != null && !result.contains(hwyLvl))
				result.add(hwyLvl);
		}
		return result;
	}

	/*
	 * This method initialize the choose list at the manage panel.
	 */
	private void setComboBoxChooseTransport() {

		cbChooseTransport.removeAllItems();
		ArrayList<Transport> al_transports = dbs.getAllTransports();

		if (al_transports != null) {
			cbChooseTransport.addItem(null);
			for (Transport t : al_transports) {
				cbChooseTransport.addItem(t);
			}
		}
	}

	/*
	 * This method parse the config file which be located under mapsforge/res/conf/allWays.conf.
	 * This file get a list of all valid values for the ways a transport can use. REGARD!
	 * actually there are only highway tags in the enum EHighwayLevel inserted, so only ths way
	 * are valid, too.
	 */
	private Vector<String> getListOfAllWays() throws IOException {

		// get the the path of this application
		String uri = System.getProperty("user.dir");
		File file = new File(uri + "\\res\\conf\\gui\\allWays.conf");
		BufferedReader br;
		Vector<String> hwyLvls = new Vector<String>();
		if (file.exists()) {

			// parse the file and add all entries which are also in the enum EHighwayLevel (this
			// temporary static)
			br = new BufferedReader(new FileReader(file));
			EHighwayLevel hwyLvl;
			String input = br.readLine();
			while (input != null) {
				hwyLvl = HighwayLevelExtractor.getLevel(input.split("=")[1]);
				if (hwyLvl != EHighwayLevel.unmapped)
					hwyLvls.add(hwyLvl.toString());
				input = br.readLine();
			}

		} else {
			System.out.println("Can't finde a needed ressource. " + file.getPath());
			System.exit(-1);
		}
		return hwyLvls;
	}

	/*
	 * This method filled the list with the parsed ways.
	 */
	private void setJListAllUseabaleWayTags() {
		try {
			jlAllUsableWays.setListData(getListOfAllWays());
		} catch (IOException e) {
			// this exception should not occurs
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/*
	 * This method show the attributes at the corresponding attribute fields of the chosen
	 * transport configuration
	 */
	void showChoosenTransport(Transport t) {

		if (t == null) {
			tfTransportName.setText("");
			ftfTransportMaxSpeed.setValue(null);
			dlmListModel.clear();

		} else {
			tfTransportName.setText(t.getName());
			ftfTransportMaxSpeed.setValue(t.getMaxSpeed());
			dlmListModel.clear();
			for (EHighwayLevel hwhLvl : t.getUseableWays()) {
				dlmListModel.addElement(hwhLvl.toString());
			}
		}

	}

}

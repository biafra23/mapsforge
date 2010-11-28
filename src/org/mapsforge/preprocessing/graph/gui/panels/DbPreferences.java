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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mapsforge.preprocessing.graph.gui.util.DatabaseService;
import org.mapsforge.preprocessing.graph.model.gui.DatabaseProperties;

/**
 * This is the class for the database preference window of the menu bar.
 * 
 * @author kunis
 */
public class DbPreferences extends JFrame {

	private static final long serialVersionUID = 2481113734700551000L;

	private DatabaseService dbs;
	private JTextField tf_hostName, tf_database, tf_username, tf_password;
	private JFormattedTextField ftf_port;

	/**
	 * The constructor to create a database configuration preference window.
	 * 
	 * @param dbs
	 *            a database service object to connect to the embedded database
	 */
	public DbPreferences(DatabaseService dbs) {

		super("Database Preferences");
		this.dbs = dbs;
		this
				.setLocation(
						(Toolkit.getDefaultToolkit().getScreenSize().width - this.getSize().width) / 4,
						(Toolkit.getDefaultToolkit().getScreenSize().height - this.getSize().height) / 4);

		this.setSize(400, 200);
		init();
	}

	// Initialize the preference window
	private void init() {

		this.getContentPane().setLayout(new BorderLayout());

		this.getContentPane().add(drawInputPanel(), BorderLayout.NORTH);
		this.getContentPane().add(drawButtonPanel(), BorderLayout.SOUTH);
		loadDbConfig();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
	}

	/*
	 * This method draws all buttons on the panel
	 */
	private JPanel drawButtonPanel() {

		JPanel panel = new JPanel(new FlowLayout());
		// GridBagConstraints constraints = new GridBagConstraints();

		JButton b_saveDbConfig = new JButton("Save Database Configuration");
		JButton b_getDefaultDbConfig = new JButton("Restore Configuration");
		panel.add(b_getDefaultDbConfig);
		panel.add(b_saveDbConfig);

		b_getDefaultDbConfig.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				loadDbConfig();
			}
		});
		b_saveDbConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDbConfig();
			}
		});

		return panel;
	}

	/*
	 * This method draws all components on the panel
	 */
	private JPanel drawInputPanel() {

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.insets = new Insets(5, 5, 0, 5);
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		constraints.gridwidth = GridBagConstraints.REMAINDER;
		JLabel l_information = new JLabel(
				"Here you can change the default database configuration.");
		panel.add(l_information, constraints);

		constraints.weightx = 0.2;
		constraints.gridwidth = GridBagConstraints.RELATIVE;

		constraints.gridy = 1;
		panel.add(new JLabel("host:"), constraints);
		constraints.gridy = 2;
		panel.add(new JLabel("port:"), constraints);
		constraints.gridy = 3;
		panel.add(new JLabel("database:"), constraints);
		constraints.gridy = 4;
		panel.add(new JLabel("username:"), constraints);
		constraints.gridy = 5;
		panel.add(new JLabel("password:"), constraints);

		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1.0;

		tf_hostName = new JTextField();
		ftf_port = new JFormattedTextField();
		tf_database = new JTextField();
		tf_username = new JTextField();
		tf_password = new JTextField();

		tf_hostName.setPreferredSize(new Dimension(100, 20));

		constraints.gridx = 1;
		constraints.gridy = 1;
		panel.add(tf_hostName, constraints);

		constraints.gridy = 2;
		panel.add(ftf_port, constraints);

		constraints.gridy = 3;
		panel.add(tf_database, constraints);

		constraints.gridy = 4;
		panel.add(tf_username, constraints);

		constraints.gridy = 5;
		panel.add(tf_password, constraints);

		return panel;
	}

	// get the default database configuration to draw it to the panel
	void loadDbConfig() {
		drawDbConfig(dbs.getDefaultDbConfig());
	}

	/*
	 * This method draws the given props on the panel
	 */
	private void drawDbConfig(DatabaseProperties dbProps) {
		if (dbProps == null) {
			String message = ("There exists no default database configuration.");
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		tf_hostName.setText(dbProps.getHost());
		tf_database.setText(dbProps.getDbName());
		tf_username.setText(dbProps.getUsername());
		tf_password.setText(dbProps.getPassword());
		ftf_port.setValue(dbProps.getPort());
	}

	/*
	 * save the actual input to the database
	 */
	void saveDbConfig() {

		String host, dbname, username, password;
		int port = 0;

		host = tf_hostName.getText();
		dbname = tf_database.getText();
		username = tf_username.getText();
		password = tf_password.getText();
		try {
			port = ((Number) ftf_port.getValue()).intValue();
		} catch (Exception e) {

			e.printStackTrace();
			String message = ("There was insert an invaild value for the port.");
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (host == "" || dbname == "" || username == "" || password == "") {

			String message = ("Any of the input fields contians a invalid value.");
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			DatabaseProperties dbProps = new DatabaseProperties(host, port, dbname, username,
					password);
			try {
				dbs.addDatabaseConfig(dbProps);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}

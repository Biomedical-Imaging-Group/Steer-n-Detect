/*
 * Steer'n'Detect
 * 
 * Zsuzsanna Puspoki and Daniel Sage, Biomedical Imaging Group
 * Ecole Polytechnique Federale de Lausanne (EPFL), Switzerland
 * 
 * Information: http://bigwww.epfl.ch/algorithms/steer_n_detect/
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results 
 * that are based on it.
 * 
 * Reference: Z. Puspoki et al. submitted to Bioinformatics 2021.
 */

/*
 * Copyright 2016-2021 Biomedical Imaging Group at the EPFL.
 * 
 * Steer'n'Detect is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Steer'n'Detect is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Steer'n'Detect. If not, see <http://www.gnu.org/licenses/>.
 */
package steerabledetector.gui.settings;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SettingsFileDialog extends JDialog implements ActionListener {
	private JTextField	txt;
	private JButton		bnCancel	= new JButton("Cancel");
	private JButton		bnReset		= new JButton("Default");
	private JButton		bnSaveAs	= new JButton("Save As");
	private JButton		bnLoad		= new JButton("Load");
	private JButton		bnSave		= new JButton("Save");

	private Settings	settings;

	public SettingsFileDialog(Settings settings) {
		super(new JFrame(), "Settings of " + settings.getProject());
		this.settings = settings;
		txt = new JTextField(settings.getFilename());

		txt.setEditable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		JPanel pn1 = new JPanel(new FlowLayout());
		pn1.add(txt);

		JPanel pn2 = new JPanel(new FlowLayout());
		pn2.add(bnCancel);
		pn2.add(bnReset);
		pn2.add(bnSave);
		pn2.add(bnSaveAs);
		pn2.add(bnLoad);

		contentPane.add(pn1, BorderLayout.NORTH);
		contentPane.add(pn2, BorderLayout.SOUTH);
		bnCancel.addActionListener(this);
		bnReset.addActionListener(this);
		bnSaveAs.addActionListener(this);
		bnSave.addActionListener(this);
		bnLoad.addActionListener(this);
		pack();

		setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
		setResizable(false);
		Dimension dim = getToolkit().getScreenSize();
		Rectangle abounds = getBounds();
		setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bnSaveAs) {
			JFileChooser chooser = new JFileChooser(txt.getText());
			chooser.setSelectedFile(new File("config.txt"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(new FileNameExtensionFilter("Configuration file", "txt"));
			int returnVal = chooser.showSaveDialog(new JFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String name = chooser.getSelectedFile().getAbsolutePath();
				if (!name.endsWith(".txt"))
					name += ".txt";
				txt.setText(name);
				settings.storeRecordedItems(txt.getText());
			}
		}
		else if (e.getSource() == bnLoad) {
			JFileChooser chooser = new JFileChooser(txt.getText());
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(new FileNameExtensionFilter("Configuration file", "txt"));
			int returnVal = chooser.showOpenDialog(new JFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String name = chooser.getSelectedFile().getAbsolutePath();
				txt.setText(name);
			}
			settings.loadRecordedItems(txt.getText());
		}
		else if (e.getSource() == bnSave) {
			settings.storeRecordedItems(txt.getText());
		}
		else if (e.getSource() == bnReset) {
			settings.loadRecordedItems("default-no-file");
		}

		bnCancel.removeActionListener(this);
		bnReset.removeActionListener(this);
		bnLoad.removeActionListener(this);
		bnSave.removeActionListener(this);
		bnSaveAs.removeActionListener(this);
		dispose();
	}
}
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
package steerabledetector.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ij.gui.GUI;
import steerabledetector.gui.components.GridPanel;
import steerabledetector.gui.components.SpinnerDouble;

public class DialogFeatureSelection extends JDialog implements ActionListener, ChangeListener {

	private String			name[]					= new String[] { "X", "Y", "Angle", "Confidence"};
	private SpinnerDouble	spnLower[]				= new SpinnerDouble[4];
	private SpinnerDouble	spnUpper[]				= new SpinnerDouble[4];
	private DialogSelection	parent;
	private Data				data;
	private JButton			bnOK					= new JButton("OK");
	private double[]		initMinimum				= new double[4];
	private double[]		initMaximum				= new double[4];

	public DialogFeatureSelection(DialogSelection parent, final Data data, int nx, int ny) {
		super(parent, "Feature-based Selection");
		
		this.parent = parent;
		this.data = data;

		data.copyRangeValuesTo(initMinimum, initMaximum);

		GridPanel selection = new GridPanel("Range selection", 4);
		selection.place(0, 0, "Feature");
		selection.place(0, 2, "Lower");
		selection.place(0, 4, "Upper");

		for (int i = 0; i < name.length; i++) {
			spnLower[i] = data.getLowerSpinner(i);
			spnUpper[i] = data.getUpperSpinner(i);
			selection.place(i + 1, 0, name[i]);
			selection.place(i + 1, 2, spnLower[i]);
			selection.place(i + 1, 4, spnUpper[i]);
			spnLower[i].addChangeListener(this);
			spnUpper[i].addChangeListener(this);
		}

		GridPanel main = new GridPanel(false, 8);
		main.place(5, 0, 3, 1, selection);
		main.place(6, 2, bnOK);

		bnOK.addActionListener(this);
		add(main);
		pack();
		setResizable(false);
		GUI.center(this);
		setModal(true);
		setVisible(true);
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (data != null)
			select();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == bnOK) {
			select();
			dispose();
		}
	}

	private void select() {
		double lower[] = new double[name.length];
		double upper[] = new double[name.length];

		for (int i = 0; i < name.length; i++) {
			lower[i] = spnLower[i].get();
			upper[i] = spnUpper[i].get();
		}
		data.select(lower, upper);
		parent.updateCanvas(-1);
		parent.update(-1);
	}

}

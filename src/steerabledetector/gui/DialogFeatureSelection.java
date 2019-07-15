//====================================================================================================
// Project: Steer'n'Detect
// 
// Authors: Zsuzsanna Puspoki and Daniel Sage
// Organization: Biomedical Imaging Group (BIG), Ecole Polytechnique Federale de Lausanne
// Address: EPFL-STI-IMT-LIB, 1015 Lausanne, Switzerland
//
// Information: http://bigwww.epfl.ch/algorithms/steer_n_detect/
//
// Reference:
// Zsuzsanna Puspoki et al. submitted to Bioinformatics 2019
//
// Conditions of use:
// You'll be free to use this software for research purposes, but you should not redistribute 
// it without our consent. In addition, we expect you to include a citation whenever you 
// present or publish results that are based on it.
//====================================================================================================

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

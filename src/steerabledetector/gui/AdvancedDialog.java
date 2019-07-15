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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import ij.gui.GUI;
import steerabledetector.detector.Parameters;
import steerabledetector.gui.components.GridPanel;
import steerabledetector.gui.components.SpinnerDouble;
import steerabledetector.gui.components.SpinnerInteger;
import steerabledetector.gui.settings.Settings;

public class AdvancedDialog extends JDialog implements ActionListener {

	private JButton			bnClose			= new JButton("Close");
	private SpinnerDouble	spnDeltaAlpha	= new SpinnerDouble(1, 0.25, 360, 0.5);
	private SpinnerDouble	spnOverlap		= new SpinnerDouble(0, -99999, 99999, 1);
	private SpinnerInteger	spnMargin		= new SpinnerInteger(10, 0, 99999, 1);
	private SpinnerDouble	spnQuantile		= new SpinnerDouble(100, 0, 100, 1);

	private Parameters 		params;
	
	public AdvancedDialog(Parameters params, Settings settings) {
		super(new JFrame(), "Advanced Parameters");
		this.params = params;

		spnOverlap.set(params.overlap);
		spnMargin.set(params.margin);
		spnDeltaAlpha.set(params.deltaAlpha);

		GridPanel pnParams = new GridPanel(true, 8);
		pnParams.place(1, 0, "Overlap");
		pnParams.place(1, 1, spnOverlap);
		pnParams.place(1, 2, "px");
		pnParams.place(2, 0, "Margin");
		pnParams.place(2, 1, spnMargin);
		pnParams.place(2, 2, "px");
		pnParams.place(6, 0, "Delta angle");
		pnParams.place(6, 1, spnDeltaAlpha);
		pnParams.place(6, 2, "degrees");
		//pnParams.place(7, 0, "Refine orientation on quantile");
		//pnParams.place(7, 1, spnQuantile);
		//pnParams.place(7, 2, "%");
		
		settings.record("spnDeltaAlpha", spnDeltaAlpha, "5");
		settings.record("spnMargin", spnMargin, "10");
		settings.record("spnOverlap", spnOverlap, "0");
		settings.record("spnQuantile", spnQuantile, "100");

		bnClose.addActionListener(this);
		getParameters();
		setLayout(new BorderLayout());
		add(pnParams, BorderLayout.CENTER);
		add(bnClose, BorderLayout.SOUTH);
		pack();
		setModal(true);
		setResizable(false);
		GUI.center(this);

	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == bnClose) {
			dispose();
			setParameters();
		}
	}
	
	public void setParameters() {
		params.deltaAlpha = spnDeltaAlpha.get();
		params.overlap = spnOverlap.get();
		params.margin = spnMargin.get();
	}
	
	public void getParameters() {
		spnDeltaAlpha.set(params.deltaAlpha);
		spnOverlap.set(params.overlap);
		spnMargin.set(params.margin);
	}

}

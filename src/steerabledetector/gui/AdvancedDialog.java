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

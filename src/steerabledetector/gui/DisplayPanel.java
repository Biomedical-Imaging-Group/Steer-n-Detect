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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import steerabledetector.gui.components.ColorName;
import steerabledetector.gui.components.GridPanel;
import steerabledetector.gui.components.SpinnerInteger;
import steerabledetector.gui.settings.Settings;

public class DisplayPanel extends GridPanel implements ActionListener, ChangeListener {

	private JCheckBox		showOuter		= new JCheckBox("Show Outer", true);
	private JCheckBox		showInner		= new JCheckBox("Show Inner", false);
	private JCheckBox		showLabel		= new JCheckBox("Show Label", false);
	private JCheckBox		showCross		= new JCheckBox("Show Center", false);
	private SpinnerInteger	spnStroke		= new SpinnerInteger(2, 0, 100, 1);
	private SpinnerInteger	spnOpacity		= new SpinnerInteger(50, 0, 100, 1);
	private JComboBox		cmbLabel		= new JComboBox(new String[] { "ID", "Angle", "Confidence"});
	private JComboBox		cmbOuterColor	= new JComboBox(ColorName.names);
	private JComboBox		cmbInnerColor	= new JComboBox(ColorName.names);
	private JComboBox		cmbLabelColor	= new JComboBox(ColorName.names);
	private JComboBox		cmbCrossColor	= new JComboBox(ColorName.names);
	private JButton			bnScreenshot	= new JButton("Screenshot");
	private CanvasSelection	canvas;

	public DisplayPanel(Settings settings, CanvasSelection canvas) {
		super(false, 2);
		this.canvas = canvas;
		settings.record("showOuter", showOuter, true);
		settings.record("showInner", showInner, false);
		settings.record("showLabel", showLabel, false);
		settings.record("showCross", showCross, false);
		settings.record("spnStroke", spnStroke, "2");
		settings.record("spnOpacity", spnOpacity, "50");
		settings.record("cmbLabel", cmbLabel, "No label");
		settings.record("cmbOuterColor", cmbOuterColor, "Red");
		settings.record("cmbInnerColor", cmbInnerColor, "Lime");
		settings.record("cmbLabelColor", cmbLabelColor, "Red");
		settings.record("cmbCrossColor", cmbCrossColor, "Red");

		settings.loadRecordedItems();

		place(1, 0, showOuter);
		place(1, 3, spnStroke);
		place(1, 2, cmbOuterColor);

		place(2, 0, showInner);
		place(2, 3, spnOpacity);
		place(2, 2, cmbInnerColor);

		place(3, 0, showLabel);
		place(3, 3, cmbLabel);
		place(3, 2, cmbLabelColor);

		place(4, 0, showCross);
		place(4, 2, cmbCrossColor);

		place(4, 3, bnScreenshot);

		showOuter.addActionListener(this);
		showInner.addActionListener(this);
		showLabel.addActionListener(this);
		showCross.addActionListener(this);
		spnOpacity.addChangeListener(this);
		spnStroke.addChangeListener(this);
		cmbLabel.addActionListener(this);
		cmbOuterColor.addActionListener(this);
		cmbInnerColor.addActionListener(this);
		cmbLabelColor.addActionListener(this);
		cmbCrossColor.addActionListener(this);
		bnScreenshot.addActionListener(this);
	}

	public void updateCanvas() {
		if (canvas != null) {
			boolean shows[] = new boolean[] { 
					showOuter.isSelected(), 
					showInner.isSelected(), 
					showLabel.isSelected(), 
					showCross.isSelected() };
			String colors[] = new String[] { 
					(String) cmbOuterColor.getSelectedItem(), 
					(String) cmbInnerColor.getSelectedItem(),
					(String) cmbLabelColor.getSelectedItem(), 
					(String) cmbCrossColor.getSelectedItem() };
			canvas.setDisplay(shows, colors, spnStroke.get(), spnOpacity.get(), (String) cmbLabel.getSelectedItem());
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src == showOuter || src == showInner || src == showLabel || src == cmbLabel || src == showCross) {
			updateCanvas();
		}
		if (src == cmbOuterColor || src == cmbInnerColor || src == cmbLabelColor || src == cmbCrossColor) {
			updateCanvas();
		}
		if (src == bnScreenshot) {
			ImagePlus imp = canvas.getImage();
			ImageCanvas canvas1 = imp.getCanvas();
			Rectangle r = canvas1.getBounds();
			Image image = canvas1.createImage(r.width, r.height);
			Graphics g = image.getGraphics();
			canvas.paint(g);
			ImagePlus screenshot = new ImagePlus("Screenshot", image);
			screenshot.show();
		}
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() == spnOpacity || event.getSource() == spnStroke) {
			updateCanvas();
		}
	}

}

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

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import steerabledetector.detector.Parameters;
import steerabledetector.detector.SteerableDetector;
import steerabledetector.gui.Data;
import steerabledetector.gui.DialogSelection;
import steerabledetector.gui.components.HTMLPane;

public class Steer_Detect_Load implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.error("No open image.");
			return;
		}
		JFileChooser saver = new JFileChooser();
		saver.setDialogTitle("Specify the filename to load");
		int ret = saver.showOpenDialog(new JFrame());
		if (ret != JFileChooser.APPROVE_OPTION)
			return;
		String filename = saver.getSelectedFile().getAbsolutePath();

		Parameters params = new Parameters();
		Data data = new Data(imp, params);
		data.loadCVS(filename);
		HTMLPane info = new HTMLPane(300, 200);
		info.append("p", "" + data.getDetected().size() + " model loaded");
		SteerableDetector detector = new SteerableDetector(imp, null, params, null, info);

		new DialogSelection(imp, detector, data, params, info);
	}
}

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

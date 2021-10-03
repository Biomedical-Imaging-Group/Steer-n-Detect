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

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import steerabledetector.detector.OutputMode;
import steerabledetector.detector.Parameters;
import steerabledetector.detector.RunningMode;
import steerabledetector.gui.DialogMain;

public class Steer_Detect implements PlugIn {

	public static void main(String arg[]) {
		ImagePlus imp = IJ.createImage("Test", "32-bits", 600, 300, 1);
		ImageProcessor ip = imp.getProcessor();
		for(int x = 50; x<=550; x+=50)
			draw(x, 150, (x-300)/3, ip);
		ip.noise(0.1);
		ip.resetMinAndMax();
		imp.show();
		new Steer_Detect().run("");
	}
	
	private static void draw(int x, int y, double angle, ImageProcessor ip) {
		double cosa = Math.cos(Math.toRadians(angle));
		double sina = Math.sin(Math.toRadians(angle));
		for(int i=-30; i<=30; i++)
		for(int j=-30; j<=30; j++) {
			double u = cosa*i + sina*j;
			double v = -sina*i + cosa*j;
			v = v > 0 ? v : v*4;
			ip.putPixelValue(x+i, y+j, Math.exp(-u*u/32-v*v/256));
		}
	}

	@Override
	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.error("No open image.");
			return;
		}
		if (imp.getWidth() % 2 != 0 || imp.getHeight() % 2 != 0) {
			IJ.error("The image size should be a multiple of 2.");
			return;
		}

		int type = imp.getType();
		if (type != ImagePlus.GRAY8 && type != ImagePlus.GRAY16 && type != ImagePlus.GRAY32) {
			IJ.error("Only process 8-bit, 16-bit or 32-bit image.");
			return;
		}
		
		if (Macro.getOptions() == null) {
			Parameters params = new Parameters();
			new DialogMain(imp, RunningMode.STANDARD, OutputMode.SELECTION, params);
		}
		else {
			Parameters params = new Parameters();
			params.fromMacro(Macro.getOptions());
			new DialogMain(imp, RunningMode.MACRO, OutputMode.SAVE, params);
		}
	}
}

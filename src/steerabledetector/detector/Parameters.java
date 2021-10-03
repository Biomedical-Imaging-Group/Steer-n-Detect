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
package steerabledetector.detector;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ij.ImagePlus;
import ij.Macro;
import steerabledetector.gui.components.HTMLPane;

public class Parameters {

	public double		overlap			= 0;
	public int			margin			= 0;
	public double		deltaAlpha		= 1;
	public int 			nHarmonics      = 7;
	public double       minAlpha        = 0;
	public double		maxAlpha        = 360;
	public double		gamma        	= 0;
	public int			nDetections		= Integer.MAX_VALUE;
	public int          patternSizeX    = 0;
	public int          patternSizeY    = 0;	
	public boolean		coarseToFine		= true;
	public double		referenceOrientation = 0;
	public String		filename			= "";
	
	public void setAutomaticValues() {
		overlap = 0;
		deltaAlpha = 1;
		nDetections = 100;
		minAlpha = 0;
		maxAlpha = 360;
		margin = 0;
		gamma = 0;
	}
	
	public void fromMacro(String options) {
		overlap = Double.parseDouble(Macro.getValue(options, "overlap", "" + overlap));
		margin = (int)Double.parseDouble(Macro.getValue(options, "margin", "" + margin));
		nDetections = (int)Double.parseDouble(Macro.getValue(options, "ndetections", ""+ nDetections));
		minAlpha = Double.parseDouble(Macro.getValue(options, "minalpha", "" + minAlpha));
		maxAlpha = Double.parseDouble(Macro.getValue(options, "maxalpha", "" + maxAlpha));		
		deltaAlpha = Double.parseDouble(Macro.getValue(options, "deltaalpha", "" + deltaAlpha));			
		nHarmonics = (int)Double.parseDouble(Macro.getValue(options, "nharmonics", ""+ nHarmonics));
		gamma = (int)Double.parseDouble(Macro.getValue(options, "gamma", ""+ gamma));
		filename = Macro.getValue(options, "output", ""+ filename);
	}
	
	public String toMacro() {
		String options = "";
		options += "overlap=" + overlap + " ";
		options += "margin=" + margin + " ";
		options += "nDetections=" + nDetections + " ";
		options += "minAlpha=" + minAlpha + " ";
		options += "maxAlpha=" + maxAlpha + " ";
		options += "deltaAlpha=" + deltaAlpha + " ";
		options += "nharmonics=" + nHarmonics + " ";
		options += "gamma=" + gamma + " ";
		return options;
	}
	
	public void setInformation(ImagePlus imp, HTMLPane info) {
		info.append("b", "Computing");
		info.append("p", "Image title: " + imp.getTitle());
		info.append("p", "Image size: " + imp.getWidth() + "x" + imp.getHeight());
		info.append("p", "Admissible overlap: " + overlap + " pixels");
		info.append("p", "Margin border: " + margin + " pixels");
		info.append("p", "Max number of detection: " + nDetections);
		info.append("p", "Number of harmonics: " + nHarmonics);
		info.append("p", "Min angle: " + minAlpha);
		info.append("p", "Max angle: " + maxAlpha);
		info.append("p", "Accuracy (delta): " + deltaAlpha);
		info.append("p", "Gamma (shaping filter): " + gamma);
		info.append("p", "Coarse to fine: " + coarseToFine);
	}

	@Override
	public String toString() {
		return "Steerable Detector run: " + new SimpleDateFormat("dd MMMM yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) + "\n" + "Image: "
				+"\n" + "Admissible overlap: " + overlap + " pixels" + "\n" + "Margin border: " + margin + " pixels" + "\n" + "Upper bound for the number of detections: " + nDetections + "\n"
				+ "Number of harmonics: " + nHarmonics + "\n" + "\n" + "Accuracy (delta): " + deltaAlpha + "\n" + filename + "\n";

	}
}

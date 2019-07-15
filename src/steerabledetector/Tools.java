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

package steerabledetector;

public class Tools {

	public static String time(double ns) {
		if (ns < 3000.0)
			return String.format("%3.2f ns", ns);
		ns *= 0.001;
		if (ns < 3000.0)
			return String.format("%3.2f us", ns);
		ns *= 0.001;
		if (ns < 3000.0)
			return String.format("%3.2f ms", ns);
		ns *= 0.001;
		if (ns < 3600.0 * 3)
			return String.format("%3.2f s", ns);
		ns /= 3600;
		return String.format("%3.2f h", ns);
	}
}

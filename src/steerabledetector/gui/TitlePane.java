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

import steerabledetector.Constants;
import steerabledetector.gui.components.HTMLPane;

public class TitlePane extends HTMLPane {

	public TitlePane() {
		super(300, 80);
		append("<h1><center>Steer 'n' Detect</center></h1>");
		append("<p><center>" + Constants.urlHelp + "</center></p>");
	}
}

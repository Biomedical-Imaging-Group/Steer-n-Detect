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

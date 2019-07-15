package steerabledetector.gui.components;

import javax.swing.JProgressBar;

public class ProgressionBar extends JProgressBar {

	private double chrono;
	
	public ProgressionBar(String msg) {
		setStringPainted(true);
		reset(msg);
	}

	public void progress(String msg, double value) {
		progress(msg, (int)value);
	}

	public void progress(String msg, int value) {
		double elapsedTime = System.currentTimeMillis() - chrono;
		String t = " [" + (elapsedTime > 3000 ?  Math.round(elapsedTime/10)/100.0 + "s." : elapsedTime + "ms") + "]";
		setValue(value);
		setString(msg + t);
	}
	
	public void reset(String msg) {
		chrono = System.currentTimeMillis();
		setValue(0);
		setString(msg );
	}
}

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

package steerabledetector.detector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Detection {

	public int		id;
	public double	x;
	public double	y;
	public double	angle;
	public double	amplitude;
	public double	size;
	private String	type	= "Auto";

	public Detection(int id, double x, double y, double angle, double amplitude, double size, String type) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.amplitude = amplitude;
		this.size = size;
		this.type = type;
	}

	public Detection(int id, double x, double y, double angle, double amplitude, double size) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.amplitude = amplitude;
		this.size = size;
	}

	public boolean isManual() {
		return !type.equals("Auto");
	}

	public String getType() {
		return type;
	}

	public double[] getFeature() {
		return new double[] { x, y, angle, amplitude };
	}

	public boolean contains(int xm, int ym, int r) {
		return (distance(xm, ym) <= r);
	}

	public void setManual() {
		DateFormat df = new SimpleDateFormat("h:m:s");
		type = "Manual " + df.format(new Date());
	}

	public String toStringComma() {
		String item[] = toArrayString();
		String s = "";
		for (int i = 0; i < item.length; i++)
			s += item[i] + ",";
		return s;
	}

	public String[] toArrayString() {
		String s = String.format("%05d", id);
		return new String[] { s, "" + x, "" + y, "" + angle, "" + amplitude, "" + size, type };
	}

	public static String[] toArrayStringHeader() {
		return new String[] { "No", "X", "Y", "Angle", "Confidence", "Type" };
	}

	public double distance(double xp, double yp) {
		return Math.sqrt((x - xp) * (x - xp) + (y - yp) * (y - yp));
	}

	public double distance(Detection detection) {
		double d = Math.sqrt((x - detection.x) * (x - detection.x) + (y - detection.y) * (y - detection.y));
		return d;
	}
}

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

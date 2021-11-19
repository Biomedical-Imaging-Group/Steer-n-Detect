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

package steerabledetector.filter;

/**
 * Implementation of B2 Spline
 */
public class Spline {

	private int width;

	public Spline() {
		this.width = 3;
	}

	public int getWidth() {
		return width;
	}
	
	public double getValue(double rho, int kVal, double delta) {
		rho = rho / delta - kVal;
		if (rho <= -1.5)
			return 0.0;
		if (rho >= 1.5)
			return 0.0;
		double positionShift = rho + 1.5;
		if (rho < -0.5f)
			return 0.5 * positionShift * positionShift;
		if (rho < 0.5)
			return (-2.0 * positionShift * positionShift + 6.0 * positionShift - 3.0) / 2.0;

		return 0.5 * (3.0 - positionShift) * (3.0 - positionShift);
	}

	public double[] splineBound(int kVal, double delta) {
		double[] output = { 0f, 0f };
		output[0] = -1.5 * delta + kVal * delta;
		output[1] = 1.5 * delta + kVal * delta;
		return output;
	}

	public String getName() {
		return "B2";
	}

	public double innerProduct(int k1Val, double delta1, int k2Val, double delta2, double step) {
		double output = 0;
		double[] xBorn = splineBound(k1Val, delta1);

		double x = xBorn[0];

		while (x < xBorn[1]) {
			output += (getValue(x, k1Val, delta1) * getValue(x, k2Val, delta2)) * Math.abs(x);
			x += step;
		}
		return output * step;
	}

}

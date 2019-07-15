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

package steerabledetector.filter;

public class Spline {
	
	//Implements a B2 spline
	
	public int width;

	public Spline() {
		this.width=3;
	}
	
	public double getValue(double rho, int kVal, double delta) {
		rho=rho/delta-kVal;
		if (rho <= -1.5)
			return 0.0;
		if (rho >= 1.5)
			return 0.0;
		double positionShift = rho + 1.5;
		if (rho < -0.5f)
			return 0.5*positionShift * positionShift;
		if (rho < 0.5)
			return (-2.0 * positionShift * positionShift + 6.0 * positionShift - 3.0) / 2.0;
			
		return 0.5*(3.0 - positionShift) * (3.0 - positionShift);

	}

	public double[] splineBound(int kVal, double delta) {
		double [] output = {0f,0f};
		output[0]=-1.5*delta + kVal*delta;
		output[1]= 1.5*delta + kVal*delta;
		return output;
	}

	public String getName() {
		return "B2";
	}	
	
	public double innerProduct(int k1Val,double delta1, int k2Val, double delta2 , double step)
	{
		double output = 0;
		double []xBorn=splineBound(k1Val, delta1);

		double x = xBorn[0];

		while (x < xBorn[1]) {
			output += (getValue(x , k1Val,delta1) * getValue(x , k2Val,delta2))   * Math.abs(x);
			x += step;
		}
		return output*step;
	}
	
}

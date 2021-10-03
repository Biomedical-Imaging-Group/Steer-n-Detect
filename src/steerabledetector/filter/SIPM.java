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

import java.util.HashMap;
import java.util.Map;

import jama.Matrix;
import steerabledetector.gui.components.HTMLPane;
import steerabledetector.gui.components.ProgressionBar;
import steerabledetector.image2d.ImageCartesian;
import steerabledetector.image2d.ImageCartesian.Domain;

public class SIPM extends Method {

	public final Spline				spline;

	public final int					nSplineShift;
	public final double				deltaRho;

	// Unique allocation for avoiding repeating memory allocation
	private final double				factorD;
	private double[][]				d;
	private ImageCartesian			filterGS;
	public final double[][]			GInvA;
	
	protected double[][]				cos_template = null;
	protected double[][]				sin_template = null;

	private Map<Integer, double[][]>	cN	= new HashMap<Integer, double[][]>();
	private int						computedN;
	ProgressionBar 					progress;
	HTMLPane 						info;
	
	public SIPM(ProgressionBar p, HTMLPane info, ImageCartesian templateSpaceInput, Spline sInput, double deltaRhoInput) {

		super(p, info, templateSpaceInput);
		progress = p;
		this.info = info;
		spline = sInput;
		computedN = -1;
		deltaRho = deltaRhoInput;
		nSplineShift = (int) Math.ceil(2. * Math.PI / deltaRhoInput) + 4;// +2

		double[][] GArr = new double[nSplineShift][nSplineShift];

		for (int k1 = 0; k1 < nSplineShift; k1++) {
			int k1Val = getKVal(k1);// shiftIndex[k1];
			for (int k2 = k1; k2 < nSplineShift; k2++) {
				int k2Val = getKVal(k2);// shiftIndex[k2];
				if (Math.abs(k1Val - k2Val) > 3) {
					GArr[k1][k2] = 0;
					GArr[k2][k1] = 0;
				} 
				else {
					double innerProd = spline.innerProduct(k1Val, deltaRho,  k2Val, deltaRho, 0.001);
					GArr[k1][k2] = innerProd;
					GArr[k2][k1] = innerProd;
				}
			}
		}
		Matrix G = new Matrix(GArr);
		Matrix GInv = G.inverse();
		GInvA = GInv.getArrayCopy();
		factorD = 1 / (Math.PI * 2) * templateCurrent.dx * templateCurrent.dy;
		filterGS = new ImageCartesian(tempNx, tempNx, Domain.FOURIER);
		d = new double[2][nSplineShift];
	}

	public static SIPM getMethod(ProgressionBar progress, HTMLPane info, ImageCartesian templateSpaceInput, Spline s, ImageCartesian optimisationTemplate, int optimisationOrder) {

		progress.progress("start FFT", 10);
		ImageCartesian optitempFFT = optimisationTemplate.FFT();
		progress.progress("end FFT", 20);

		double deltaRhoInitial = Math.PI * 2. / templateSpaceInput.nx;
		double deltaRho = deltaRhoInitial;
		progress.progress("start SPIM", 30);
		SIPM test = new SIPM(progress, info, templateSpaceInput, s, deltaRho);
		double error = optitempFFT.error(test.getDetector(0, 0, optitempFFT));
		progress.progress("end SPIM", 40);

		double step = 0.1;
		double optimalDRho = deltaRho;
		double minError = error;
		int count = 0;
		// System.out.println(" drho choice : "+deltaRho+" "+error);
		double chrono = System.nanoTime();
		do {
			deltaRho = (1 + step) * deltaRho;
			progress.progress("count " + count, 40+count*10);
			test = new SIPM(progress, info, templateSpaceInput, s, deltaRho);
			error = optitempFFT.error(test.getDetector(optimisationOrder, 0, optitempFFT));
			double d = ((System.nanoTime() - chrono) * 10e-6);
			System.out.println(" drho choice : "+deltaRho + " " + error + " " + " count " + count + ": " + d + "ms");
			if (error < minError) {
				minError = error;
				optimalDRho = deltaRho;
			} 
			else {
				if (step > 0 && optimalDRho == deltaRhoInitial) {
					deltaRho = deltaRhoInitial;
					step *= -1;
				} 
				else {
					count = 100;
				}
			}
			count++;
		} 
		while (count < 5);
		return new SIPM(progress, info, templateSpaceInput, s, optimalDRho);
	}

	@Override
	public String getName() {
		return "Spline(" + spline.getName() + ")";
	}

	@Override
	protected double[][] assureCNandGetB(int order) {
		assureCnComputed(order, false);
		return cN.get(order);
	}
	
	@Override
	protected void getRadialValuePix(double rho, double b[][]) {
		pix_real = 0.0;
		pix_imag = 0.0;
		int min = (int) Math.ceil(rho / deltaRho - spline.width / 2.);
		int max = min + spline.width;

		if (getK(max) >= nSplineShift) {
			max = getKVal(nSplineShift - 1);
			if (min > max) {
				min = max;
			}
		}
		if (getK(min) < 0) {
			min = getKVal(0);
			if (min > max) {
				max = min;
			}
		}
		for (int kval = min; kval < max; kval++) {
			double a = spline.getValue(rho, kval, deltaRho) ;
			pix_real += a * b[0][kval + nSplineShift / 2];
			pix_imag += a * b[1][kval + nSplineShift / 2];
		}
	}

	@Override
	protected void assureCnComputed(int order, boolean forDetector) {

		if (cN.containsKey(order) == false) {
			int N = (order < 0 ? -order : order);

			while (computedN < N) {
				// SteerableFilter.updateProgress("computing
				// cN",(int)Math.round((100.*computedN-100.*progInit)/((double)N)));
				++computedN;
				cN.put(computedN, ComputeCN(computedN));
				coreGetFilter(computedN, 0., rho_template, cos_template, sin_template, filterGS);
				templateCurrent.substract(filterGS);
				filterGS.clearData();
				
				
				if (computedN != 0) {
					cN.put(-computedN, ComputeCN(-computedN));
					coreGetFilter(-computedN, 0., rho_template, cos_template, sin_template, filterGS);
					templateCurrent.substract(filterGS);
					filterGS.clearData();
				}
			}
		}
	}

	private int getKVal(int k) {
		return k - nSplineShift / 2;
	}

	private int getK(int kVal) {
		return kVal + nSplineShift / 2;
	}

	private double[][] ComputeCN(int n) {
		ComputeDcoeff(n);
		double[][] cnCoeef = new double[2][nSplineShift];
		for (int l = 0; l < GInvA.length; l++) {
			for (int m = 0; m < GInvA[0].length; m++) {
				cnCoeef[0][l] += GInvA[m][l] * d[0][m];
				cnCoeef[1][l] += GInvA[m][l] * d[1][m];
			}
		}
		return cnCoeef;
	}

	private void ComputeDcoeff(int n) {
		
		cos_template = computeCos(n, theta_template, templateCurrent.nx, templateCurrent.ny);
		sin_template = computeSin(n, theta_template, templateCurrent.nx, templateCurrent.ny);
		
		double tempSpline;
		for (int k = 0; k < nSplineShift; k++) {
			d[0][k] = 0;
			d[1][k] = 0;
		}
		double[] pix = new double[2];
		for (int indX = 0; indX < tempNx; indX++) {
			for (int indY = 0; indY < tempNx; indY++) {

				templateCurrent.getPixelFast(indX, indY, pix);

				for (int k = 0; k < nSplineShift; k++) {

					tempSpline = spline.getValue(rho_template[indX][indY], getKVal(k), deltaRho)
							+ spline.getValue(-rho_template[indX][indY], getKVal(k), deltaRho);

					if (tempSpline != 0) {

						// sign inverse because minus in exponential : exp(- j n theta)
						d[0][k] += tempSpline
								* (pix[0] * cos_template[indX][indY] + pix[1] * sin_template[indX][indY]);
						d[1][k] += tempSpline
								* (pix[1] * cos_template[indX][indY] - pix[0] * sin_template[indX][indY]);
					}
				}
			}
		}

		for (int k = 0; k < nSplineShift; k++) {
			d[0][k] *= factorD;
			d[1][k] *= factorD;
		}
	}

	public double[][] getCN(int order) {
		assureCnComputed(order, false);
		return cN.get(order).clone();
	}
	

}

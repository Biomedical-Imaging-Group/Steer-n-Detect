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

import steerabledetector.detector.Parameters;
import steerabledetector.gui.components.HTMLPane;
import steerabledetector.gui.components.ProgressionBar;
import steerabledetector.image2d.ImageCartesian;
import steerabledetector.image2d.ImageCartesian.Domain;

public abstract class Method {

	protected ImageCartesian	templateCurrent;

	protected int				templateSize;
	protected final double		templateSizeX;
	protected final double		templateSizeY;

	protected double[][]		rho_detector	= null;
	protected double[][]		theta_detector	= null;
	protected double[][]		rho_filter		= null;
	protected double[][]		theta_filter	= null;
	protected double[][]		rho_template	= null;
	protected double[][]		theta_template	= null;

	protected double[][]		cos_detector	= null;
	protected double[][]		sin_detector	= null;
	protected double[][]		cos_filter		= null;
	protected double[][]		sin_filter		= null;

	protected double			pix_real;
	protected double			pix_imag;
	protected double[]			computePix;
	private double				cosNT;
	private double				sinNT;

	private ProgressionBar		progress;
	private boolean				stop			= false;
	private HTMLPane			info;

	protected Method(ProgressionBar progress, HTMLPane info, ImageCartesian template) {
		this.progress	= progress;
		this.info		= info;

		templateCurrent	= new ImageCartesian(template);
		templateCurrent	= templateCurrent.FFT();
		templateCurrent.complexconjugate();
		templateCurrent.removeAnisotropicFreq();
		templateSizeX	= template.sizeXSpace;
		templateSizeY	= template.sizeYSpace;
		templateSize	= template.nx;
		rho_template	= initRho(template);
		theta_template	= initTheta(template);

		computePix		= new double[2];
	}

	public void stop() {
		stop = true;
	}

	public void start() {
		stop = false;
	}

	protected abstract double[][] getCoefficients(int order);

	protected abstract void getRadialValuePix(double rho, double b[][]);

	protected abstract void assureCnComputed(int order, boolean forDetector);

	public abstract String getName();

	public ImageCartesian getFilter(int order, ImageCartesian tofit) {
		return getFilter(order, tofit.nx, tofit.ny, tofit.sizeXSpace, tofit.sizeYSpace);
	}

	public ImageCartesian getFilter(int order) {
		return getFilter(order, templateSize, templateSize, templateSizeX, templateSizeY);
	}

	protected ImageCartesian getFilter(int order, int nx, int ny, double sX, double sY) {
		assureCnComputed(order, false);
		ImageCartesian filter = new ImageCartesian(nx, ny, sX, sY, ImageCartesian.Domain.FOURIER, "filter_n" + order);
		rho_filter		= initRho(filter);
		theta_filter	= initTheta(filter);
		cos_filter		= computeCos(order, theta_filter, nx, ny);
		sin_filter		= computeSin(order, theta_filter, nx, ny);
		return getCoreFilter(order, 0., rho_filter, cos_filter, sin_filter, filter);
	}

	public ImageCartesian getDetector(int N, double angle) {
		return getDetector(N, angle, templateSize, templateSize, templateSizeX, templateSizeY);
	}

	public ImageCartesian getDetector(int N, double angle, ImageCartesian tofit) {
		return getDetector(N, angle, tofit.nx, tofit.ny, tofit.sizeXSpace, tofit.sizeYSpace);
	}

	protected ImageCartesian getDetector(int N, double angle, int nx, int ny, double sX, double sY) {

		if (N < 0) {
			throw new IllegalArgumentException("N cannot be negative");
		}

		assureCnComputed(N, true);

		ImageCartesian detector = new ImageCartesian(nx, ny, sX, sY, Domain.FOURIER, "Detector");

		rho_detector	= initRho(detector);
		theta_detector	= initTheta(detector);
		cos_detector	= computeCos(0, theta_detector, detector.nx, detector.ny);
		sin_detector	= computeSin(0, theta_detector, detector.nx, detector.ny);
		getCoreFilter(0, angle, rho_detector, cos_detector, sin_detector, detector);

		for (int order = 1; order <= N; order++) {
			cos_detector	= computeCos(order, theta_detector, detector.nx, detector.ny);
			sin_detector	= computeSin(order, theta_detector, detector.nx, detector.ny);
			getCoreFilter(order, angle, rho_detector, cos_detector, sin_detector, detector);
			// cos_detector = computeCos(-order, theta_detector, detector.nx, detector.ny);
			sin_detector = computeSin(-order, theta_detector, detector.nx, detector.ny);
			getCoreFilter(-order, angle, rho_detector, cos_detector, sin_detector, detector);
		}

		detector.name = "Detector-" + getName() + "-N" + N;
		detector.complexconjugate();
		return detector;

	}

	public ImageCartesian unsteeredAnalysis(ImageCartesian input, int Nmax) {
		ImageCartesian output = new ImageCartesian(input);
		output = output.FFT();
		output.removeAnisotropicFreq();
		ImageCartesian detector = getDetector(Nmax, 0., output);
		detector.complexconjugate();
		output.pointWiseMult(detector);
		output		= output.inverseFFT();
		output.name	= input.name + "-Analyzed-" + getName();
		return output;
	}

	public ImageCartesian steeredAnalysis(ProgressionBar progress, ImageCartesian imageToAnalyze, int nHarmonic, Parameters params) {

		// double angleMin = Math.PI * params.minAlpha / 180.0;
		double			accuraryRequested	= Math.PI * params.deltaAlpha / 180.0;
		double			angleMin			= Math.PI * params.minAlpha / 180.0;
		double			deltaAngle			= Math.PI * params.deltaAlpha / 180.0;
		ImageCartesian	AB					= new ImageCartesian(imageToAnalyze.nx, imageToAnalyze.ny, Domain.SPACE);
		AB.name = "AB";

		ImageCartesian[] fCI = filter(imageToAnalyze, nHarmonic, params.gamma);
		if (fCI == null)
			return AB;

		double	cos[][]	= getTableCos(params, nHarmonic);
		double	sin[][]	= getTableSin(params, nHarmonic);
		int		nangles	= cos[0].length - 1;
		int		npixels	= AB.nx * AB.ny;
		int		coarse	= 1;
		if (params.coarseToFine)
			coarse = (int) Math.max(1, Math.floor(nangles / (2.0 * nHarmonic)));
		info.append("p", "Requested Range [" + params.minAlpha + ", " + params.maxAlpha + "] step: " + params.deltaAlpha);

		for (int k = 0; k < npixels; k++) {
			int		argmax	= 0;
			double	max		= AB.dataReel[k];
			if (k == 0)
				info.append("p", "Initial loop [0, " + nangles + "] step: " + coarse);
			for (int a = 0; a <= nangles; a += coarse) {
				double sum = 0.0;
				for (int n = 0; n < cos.length; n++)
					sum += fCI[n].dataReel[k] * cos[n][a] - fCI[n].dataImag[k] * sin[n][a];
				if (max < sum) {
					max		= sum;
					argmax	= a;
				}
			}
			if (params.coarseToFine) {
				int fine = coarse;
				while (fine / 180.0 * Math.PI > accuraryRequested) {
					fine = (int) Math.max(1, Math.floor(fine * 0.5));
					int		argmaxFine	= 0;
					double	maxFine		= -Double.MAX_VALUE;
					if (k == 0)
						info.append("p", "Fine loop [" + (argmax - fine) + ", " + (argmax + fine) + "] step: " + fine);
					for (int a = argmax - fine; a <= argmax + fine; a += fine) {
						int		ap	= periodize(a, nangles);
						double	sum	= 0.0;
						for (int n = 0; n < cos.length; n++)
							sum += fCI[n].dataReel[k] * cos[n][ap] - fCI[n].dataImag[k] * sin[n][ap];
						if (maxFine < sum) {
							maxFine		= sum;
							argmaxFine	= a;
						}
					}
					argmax	= argmaxFine;
					max		= maxFine;
				}
			}
			AB.dataReel[k]	= max;
			AB.dataImag[k]	= periodize(angleMin + argmax * deltaAngle + params.referenceOrientation, 2 * Math.PI);
			if (stop)
				return AB;
			if (k % AB.nx == 0) {
				int y = k / AB.nx;
				progress.progress("Row " + y, y * 100.0 / AB.ny);
			}
		}
		return AB;
	}

	private ImageCartesian[] filter(ImageCartesian imageToAnalyze, int nHarmonic, double gamma) {
		ImageCartesian		input		= new ImageCartesian(imageToAnalyze);
		ImageCartesian		inputFFT	= input.FFT();
		ImageCartesian[]	fCI			= new ImageCartesian[2 * nHarmonic + 1];

		ImageCartesian		w2gamma		= null;
		if (gamma > 0) {
			// Whitening filter multiply by |w|^{2*gamma}
			w2gamma = new ImageCartesian(input.nx, input.ny, input.sizeXSpace, input.sizeYSpace, Domain.FOURIER, "test");
			double fact = (templateSizeX / templateSize) / (input.sizeXSpace / input.nx);
			for (int i = 0; i < input.nx; i++) {
				for (int j = 0; j < input.ny; j++) {
					w2gamma.addPixel(i, j, Math.pow(fact * w2gamma.indexToRho(i, j), 2.0 * gamma), 0.0);
				}
			}
		}

		for (int n = -nHarmonic; n <= nHarmonic; n++) {
			progress.progress("Filter " + n, ((n + nHarmonic) * 100.0 / (2 * nHarmonic)));
			if (stop)
				return null;
			ImageCartesian filter = getFilter(n, input);
			if (gamma > 0)
				filter.pointWiseMult2(w2gamma, inputFFT);
			else
				filter.pointWiseMult(inputFFT);
			fCI[n + nHarmonic] = filter.inverseFFT();
		}
		return fCI;
	}

	private double[][] getTableCos(Parameters params, int nHarmonic) {
		double	angleMin	= Math.PI * params.minAlpha / 180.0;
		double	angleMax	= Math.PI * params.maxAlpha / 180.0;
		double	deltaAngle	= Math.PI * params.deltaAlpha / 180.0;
		int		nangles		= (int) Math.ceil((angleMax - angleMin) / deltaAngle);
		double	cos[][]		= new double[2 * nHarmonic + 1][nangles + 1];
		for (int a = 0; a <= nangles; a++) {
			double alpha = angleMin + a * deltaAngle;
			for (int n = -nHarmonic; n <= nHarmonic; n++) {
				cos[n + nHarmonic][a] = Math.cos(-n * alpha);
			}
		}
		return cos;
	}

	private double[][] getTableSin(Parameters params, int nHarmonic) {
		double	angleMin	= Math.PI * params.minAlpha / 180.0;
		double	angleMax	= Math.PI * params.maxAlpha / 180.0;
		double	deltaAngle	= Math.PI * params.deltaAlpha / 180.0;
		int		nangles		= (int) Math.ceil((angleMax - angleMin) / deltaAngle);
		double	sin[][]		= new double[2 * nHarmonic + 1][nangles + 1];
		for (int a = 0; a <= nangles; a++) {
			double alpha = angleMin + a * deltaAngle;
			for (int n = -nHarmonic; n <= nHarmonic; n++) {
				sin[n + nHarmonic][a] = Math.sin(-n * alpha);
			}
		}
		return sin;
	}

	private int periodize(int a, int period) {
		if (a < 0)
			return period + a;
		if (a > period)
			return a - period;
		return a;
	}

	private double periodize(double a, double nangles) {
		if (a < 0)
			return nangles + a;
		if (a > nangles)
			return a - nangles;
		return a;
	}

	protected ImageCartesian getCoreFilter(int order, double alpha, double[][] rho, double[][] cos, double[][] sin, ImageCartesian filter) {
		cosNT	= Math.cos(-order * alpha);
		sinNT	= Math.sin(-order * alpha);
		int		nx		= rho.length;
		int		ny		= rho[0].length;
		double	real, imag;
		double	b[][]	= getCoefficients(order);
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				getRadialValuePix(rho[i][j], b);
				real	= cos[i][j] * pix_real - sin[i][j] * pix_imag;
				imag	= cos[i][j] * pix_imag + sin[i][j] * pix_real;
				filter.addPixel(i, j, cosNT * real - sinNT * imag, cosNT * imag + sinNT * real);
			}
		}
		return filter;
	}

	protected double[][] computeCos(int order, double[][] theta, int nx, int ny) {
		double[][] cos = new double[nx][ny];
		for (int indX = 0; indX < nx; indX++) {
			for (int indY = 0; indY < ny; indY++) {
				cos[indX][indY] = Math.cos(order * theta[indX][indY]);
			}
		}
		if (order == 0) {
			cos[0][0] = 1;
		}
		else {
			cos[0][0] = 0;
		}
		return cos;
	}

	protected double[][] computeSin(int order, double[][] theta, int nx, int ny) {
		double[][] sin = new double[nx][ny];
		for (int indX = 0; indX < nx; indX++) {
			for (int indY = 0; indY < ny; indY++) {
				sin[indX][indY] = Math.sin(order * theta[indX][indY]);
			}
		}
		sin[0][0] = 0;
		return sin;
	}

	protected double[][] initRho(ImageCartesian image) {
		double	fact	= ((double) templateSizeX / (double) templateSize) / ((double) image.sizeXSpace / (double) image.nx);
		double	rho[][]	= new double[image.nx][image.ny];
		for (int indX = 0; indX < image.nx; indX++) {
			for (int indY = 0; indY < image.ny; indY++) {
				rho[indX][indY] = fact * image.indexToRho(indX, indY);
			}
		}
		return rho;
	}

	protected double[][] initTheta(ImageCartesian image) {
		double[][] theta = new double[image.nx][image.ny];
		for (int indX = 0; indX < image.nx; indX++) {
			for (int indY = 0; indY < image.ny; indY++) {
				theta[indX][indY] = image.indexToTheta(indX, indY);
			}
		}
		return theta;
	}

}

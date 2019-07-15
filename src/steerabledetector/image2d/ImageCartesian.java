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

package steerabledetector.image2d;

import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import steerabledetector.fftacademic.AcademicFFT;

public class ImageCartesian {

	public enum Domain {
		SPACE, FOURIER;
	}

	public String		name;
	public Domain	domain;

	public double	sizeXSpace;
	public double	sizeYSpace;

	public double	dx;
	public double	dy;

	public int	nx;
	public int	ny;

	public double[]		dataReel;
	public double[]		dataImag;

	private double[]	splineCoeffReel;
	private double[]	splineCoeffImag;

	public ImageCartesian(int nx, int ny, double sizeX, double sizeY, double[] realValues, double[] imaginaryValues, Domain dom, String nameInput) {
		build(nx, ny, sizeX, sizeY, realValues, imaginaryValues, dom, nameInput);
	}
	

	public ImageCartesian(int nx, int ny, Domain dom) {
		build(nx, ny, 1, 1, null, null, dom, "empty");
	}

	public ImageCartesian(int nx, int ny, double sizeXS, double sizeYS, Domain dom, String name) {
		build(nx, ny, sizeXS, sizeYS, null, null, dom, name);
	}

	public ImageCartesian(int nx, int ny, double[] realValues, Domain dom, String name) {
		build(nx, ny, 1, 1, realValues, null, dom, name);
	}

	public ImageCartesian(ImageCartesian init) {
		build(init.nx, init.ny, init.sizeXSpace, init.sizeYSpace, init.dataReel, init.dataImag, init.domain, init.name);
	}

	
	private void build(int nx, int ny, double sizeX, double sizeY, double[] realValues, double[] imaginaryValues,
			Domain dom, String nameInput) {
		this.name = nameInput;
		this.nx = nx;
		this.ny = ny;

		sizeXSpace = sizeX;
		sizeYSpace = sizeY;

		domain = dom;
		if (dom == Domain.SPACE) {
			dx = sizeXSpace / nx;
			dy = sizeYSpace / ny;
		}
		else {
			dx = 2. * Math.PI / nx;
			dy = 2. * Math.PI / ny;
		}

		if (realValues == null) {
			dataReel = new double[nx * ny];
			Arrays.fill(dataReel, 0f);
		}
		else {
			if (realValues.length != nx * ny) {
				throw new IllegalArgumentException("Dimension Mismatch between array and dimension");
			}
			dataReel = realValues.clone();
		}

		if (imaginaryValues == null) {
			dataImag = new double[nx * ny];
			Arrays.fill(dataImag, 0f);
		}
		else {
			if (imaginaryValues.length != nx * ny) {
				throw new IllegalArgumentException("Dimension Mismatch between array and dimension");
			}
			dataImag = imaginaryValues.clone();

		}
	}

	public double[][] getReal() {
		double[][] output = new double[nx][ny];

		this.swapArray(dataReel, dataReel);
		for (int indX = 0; indX < nx; indX++) {
			for (int indY = 0; indY < ny; indY++) {
				output[indX][indY] = dataReel[indX + indY * nx];
			}
		}
		this.swapArray(dataReel, dataReel);
		return output;

	}

	public double[][] getImag() {
		double[][] output = new double[nx][ny];

		this.swapArray(dataImag, dataImag);
		for (int indX = 0; indX < nx; indX++) {
			for (int indY = 0; indY < ny; indY++) {
				output[indX][indY] = dataImag[indX + indY * nx];
			}
		}
		this.swapArray(dataImag, dataImag);
		return output;

	}

	public static ImageCartesian getImage(ImagePlus imp) {
		if (imp == null) {
			IJ.error("No open image :");
			return null;
		}

		ImageProcessor ip = imp.getProcessor();
		FloatProcessor fp = (FloatProcessor) ip.convertToFloat();

		float[] tempPix = (float[]) fp.getPixels();
		double[] tempDouble = new double[tempPix.length];

		for (int k = 0; k < tempPix.length; k++) {
			tempDouble[k] = tempPix[k];
		}

		ImageCartesian output = new ImageCartesian(fp.getWidth(), fp.getHeight(), fp.getWidth() / 100.,
				fp.getHeight() / 100., tempDouble, null, ImageCartesian.Domain.SPACE,
				imp.getTitle().split("\\.", 2)[0]);
		output.swapArray(output.dataReel, output.dataReel);

		return output;
	}

	public static ImageCartesian getCircularHarmonic(int nx, int ny, int order) {
		return getCircularHarmonic(nx, ny, order, 0.);
	}

	public static ImageCartesian getCircularHarmonic(int nx, int ny, int order, double angle) {

		ImageCartesian output = new ImageCartesian(nx, ny, 1, 1, Domain.FOURIER, "CircularHarmonic" + order);

		output.verifyPairDimension();

		double theta = 0;

		for (int indX = 0; indX < nx; indX++) {
			for (int indY = 0; indY < ny; indY++) {
				theta = output.indexToTheta(indX, indY);

				output.dataReel[indX + output.nx * indY] = Math.cos(order * (theta - angle));
				output.dataImag[indX + output.nx * indY] = Math.sin(order * (theta - angle));
			}

		}

		if (order == 0) {
			output.dataReel[0] = 1f;
			output.dataImag[0] = 0f;
		}
		else {
			output.dataReel[0] = 0f;
			output.dataImag[0] = 0f;
		}

		return output;
	}

	public void verifyPairDimension() {
		if (nx % 2 != 0 || ny % 2 != 0) {
			throw new IllegalArgumentException("Image size is not a multiple of 2");
		}
	}

	public ImageCartesian FFT() {

		if (domain == Domain.FOURIER) {
			throw new UnsupportedOperationException("impossible to perform FFT on a frequency domain image");
		}
		verifyPairDimension();

		AcademicFFT academicFFT = new AcademicFFT(nx, ny, 0, 0);

		double[] realPart = dataReel.clone();
		double[] imaginaryPart = dataImag.clone();

		academicFFT.directTransform(realPart, imaginaryPart, null, null, AcademicFFT.InputDataType.COMPLEXINPUT);

		ImageCartesian output = new ImageCartesian(nx, ny, sizeXSpace, sizeYSpace, realPart, imaginaryPart,
				Domain.FOURIER, name + "-Fourier");
		output.multiply(Math.sqrt(dx * dy));
		return output;
	}

	public ImageCartesian inverseFFT() {

		if (domain == Domain.SPACE) {
			throw new UnsupportedOperationException("impossible to perform the inverse FFT on a spatial domain image");
		}
		verifyPairDimension();

		AcademicFFT academicFFT = new AcademicFFT(nx, ny, 0, 0);

		double[] realPart = dataReel.clone();
		double[] imaginaryPart = dataImag.clone();

		academicFFT.inverseTransform(realPart, imaginaryPart, null, null);
		ImageCartesian output = new ImageCartesian(nx, ny, sizeXSpace, sizeYSpace, realPart, imaginaryPart,
				Domain.SPACE, name + "-Space");
		output.multiply(1 / Math.sqrt(output.dx * output.dy));
		return output;
	}

	public void addOrientedFilter(double factR, double factI, ImageCartesian filter) {
		int n = nx * ny;
		for (int ind = 0; ind < n; ind++) {
			double a = filter.dataReel[ind];
			double b = filter.dataReel[ind];
			dataReel[ind] += a * factR - b * factI;
			dataImag[ind] += a * factI + b * factR;
		}
	}

	public ImageCartesian cropToPairSquareSize() {
		int nxNew = (nx % 2 == 0) ? nx : nx - 1;
		int nyNew = (ny % 2 == 0) ? ny : ny - 1;

		int size = nx < ny ? nx : ny;

		if (size <= 0) {
			throw new IllegalStateException("problem with the image size");
		}
		ImageCartesian output = new ImageCartesian(nxNew, nyNew, nxNew * dx, nyNew * dy, domain, name + "Cropped");

		for (int indX = 0; indX < nxNew; indX++) {
			for (int indY = 0; indY < nyNew; indY++) {
				output.putPixel(indX, indY, getPixel(indX, indY));
			}
		}

		return output;
	}

	public ImageCartesian crop(int nxNew, int nyNew) {

		if (nxNew >= nx || nyNew >= ny) {
			throw new IllegalArgumentException("The crooped image must be smaller than the original image.");
		}

		swapArray(dataReel, dataReel);
		swapArray(dataImag, dataImag);

		ImageCartesian output = new ImageCartesian(nxNew, nyNew, nxNew * dx, nyNew * dy, domain,
				name + "-cropped");

		int cX = (nx - nxNew) / 2;
		int cY = (ny - nyNew) / 2;

		for (int indX = 0; indX < output.nx; indX++) {
			for (int indY = 0; indY < output.ny; indY++) {
				output.putPixel(indX, indY, getPixel(indX + cX, indY + cY));
			}
		}

		swapArray(dataReel, dataReel);
		swapArray(dataImag, dataImag);
		output.swapArray(output.dataReel, output.dataReel);
		output.swapArray(output.dataImag, output.dataImag);
		return output;
	}

	public ImageCartesian upSample() {

		verifyPairDimension();

		ImageCartesian output = new ImageCartesian(2 * nx, 2 * ny, sizeXSpace, sizeYSpace, domain,
				name + "-downSampled");

		double[] pix = new double[2];
		for (int indX = 0; indX < nx; indX++) {
			for (int indY = 0; indY < ny; indY++) {

				getPixelFast(indX, indY, pix);

				output.putPixel(2 * indX, 2 * indY, pix);
				output.putPixel(2 * indX, 2 * indY + 1, pix);
				output.putPixel(2 * indX + 1, 2 * indY, pix);
				output.putPixel(2 * indX + 1, 2 * indY + 1, pix);

			}
		}
		return output;
	}

	public ImageCartesian downSample() {

		verifyPairDimension();

		ImageCartesian output = new ImageCartesian(nx / 2, ny / 2, sizeXSpace, sizeYSpace, domain, name + "-downSampled");

		for (int indX = 0; indX < output.nx; indX++) {
			for (int indY = 0; indY < output.ny; indY++) {
				output.putPixel(indX, indY, getPixel(indX * 2, indY * 2));
			}
		}
		return output;
	}

	public ImageCartesian zeroPads(int nxNew, int nyNew) {
		if (nxNew < nx) {
			nxNew = nx;
		}
		if (nyNew < ny) {
			nyNew = ny;
		}

		int PadX = (nxNew - nx);
		int PadY = (nyNew - ny);

		if (PadX < 0 || PadY < 0) {
			throw new IllegalArgumentException("The padded image must be bigger than the original image.");
		}

		ImageCartesian output = new ImageCartesian(nxNew, nyNew, nxNew * dx, nyNew * dy, domain,
				name + "-ZeroPadded");

		double[] val = { 0., 0. };
		int indXN = 0;
		int indYN = 0;
		for (int indX = 0; indX < output.nx; indX++) {
			for (int indY = 0; indY < output.ny; indY++) {
				indXN = indX - PadX;

				if (indX < nx / 2) {
					indXN = indX;
				}
				if (indX >= nx / 2 && indX < nx / 2 + PadX) {
					indXN = -1;
				}

				indYN = indY - PadY;
				if (indY < ny / 2) {
					indYN = indY;
				}
				if (indY >= ny / 2 && indY < ny / 2 + PadY) {
					indYN = -1;
				}

				// System.out.println(indX + " "+ indY+" / "+indXN + " "+ indYN );
				if (indXN != -1 && indYN != -1) {
					val = getPixel(indXN, indYN);
					output.putPixel(indX, indY, val);
				}
			}
		}
		return output;
	}

	private void verifyIndexes(int indX, int indY) {
		if (indX < 0 || indX >= nx || indY < 0 || indY >= ny)
			throw new IllegalArgumentException("Index out of bound " +
					" indX:" + indX + " indY: " + indY +
					" nx:" + nx + " ny: " + ny);
		return;
	}

	private void verifyIndexes(double indX, double indY) {
		if (indX < 0. || indX > nx || indY < 0. || indY > ny)
			throw new IllegalArgumentException("Index out of bound " +
					" indX:" + indX + " indY: " + indY +
					" nx:" + nx + " ny: " + ny);
		return;
	}

	public double[] indexToPosition(double indX, double indY) {

		verifyIndexes(indX, indY);

		double[] output = { 0., 0. };

		output[0] = indX < nx / 2 ? indX / nx : (indX - nx) / nx;
		output[1] = indY < ny / 2 ? indY / ny : (indY - ny) / ny;

		return output;
	}

	public double[] positionToIndex(double posX, double posY) {

		if (posX < -0.5 || posX >= 0.5 || posY < -0.5 || posY >= 0.5)
			throw new IllegalArgumentException("correct range : [-0.5 , 0.5[, posX = " + posX + " posY" + posY);

		double[] output = { 0, 0 };

		output[0] = posX >= 0 ? posX * nx : posX * nx + nx;
		output[1] = posY >= 0 ? posY * ny : posY * ny + ny;

		return output;
	}

	public double indexToRho(int indX, int indY) {

		double[] position = indexToPosition(indX, indY);
		return Math.PI * 2 * Math.sqrt(Math.pow(position[0], 2) + Math.pow(position[1], 2));
	}

	public double indexToTheta(int indX, int indY) {

		double[] position = indexToPosition(indX, indY);
		return Math.atan2(position[1], position[0]);
	}

	public boolean isFourierImage() {
		return domain == Domain.FOURIER ? true : false;
	}

	public double[] getRealPixels() {
		return dataReel.clone();
	}

	public double[] getImaginaryPixels() {
		return dataImag.clone();
	}

	public void getPixelFast(int indX, int indY, double[] pix) {

		pix[0] = dataReel[indX + nx * indY];
		pix[1] = dataImag[indX + nx * indY];
	}

	public double[] getPixel(int indX, int indY) {

		verifyIndexes(indX, indY);

		double output[] = new double[2];

		output[0] = dataReel[indX + nx * indY];
		output[1] = dataImag[indX + nx * indY];

		return output;
	}

	public void putPixel(int indX, int indY, double[] val) {

		if (val.length != 2) {
			throw new IndexOutOfBoundsException("size of val must be 2, actual size : " + val.length);
		}

		verifyIndexes(indX, indY);

		dataReel[indX + nx * indY] = val[0];
		dataImag[indX + nx * indY] = val[1];
	}

	public void multiplyPixel(int indX, int indY, double valRe, double valIm) {

		verifyIndexes(indX, indY);

		double tempR = dataReel[indX + nx * indY];
		double tempI = dataImag[indX + nx * indY];

		dataReel[indX + nx * indY] = tempR * valRe - tempI * valIm;
		dataImag[indX + nx * indY] = tempR * valIm + tempI * valRe;
	}

	public void clearData() {
		Arrays.fill(dataReel, 0f);
		Arrays.fill(dataImag, 0f);
		if (splineCoeffImag != null) {
			Arrays.fill(splineCoeffImag, 0f);
		}
		if (splineCoeffReel != null) {
			Arrays.fill(splineCoeffReel, 0f);
		}
	}

	public void addPixel(int indX, int indY, double valRe, double valIm) {
		verifyIndexes(indX, indY);
		dataReel[indX + nx * indY] += valRe;
		dataImag[indX + nx * indY] += valIm;
	}

	private double[] getQuadraticSpline(double t) {
		double[] v = new double[3];

		if (t < -0.5 || t > 0.5) {
			throw new ArrayStoreException("wrong B-spline argument");
		}

		v[0] = ((t - 0.5f) * (t - 0.5f)) / 2f;
		v[2] = ((t + 0.5f) * (t + 0.5f)) / 2f;
		v[1] = 1f - v[0] - v[2];

		return v;
	}

	private int circularPeriodicX(int inputCoordinate) {
		inputCoordinate = inputCoordinate % nx;
		return inputCoordinate += (inputCoordinate < 0 ? nx : 0);
	}

	private int circularPeriodicY(int inputCoordinate) {
		inputCoordinate = inputCoordinate % ny;
		return inputCoordinate += (inputCoordinate < 0 ? ny : 0);
	}

	private double[][][] getCoeff(int xt, int yt) {
		double[][][] output = new double[3][3][2];

		int pos;

		for (int k = 0; k < 3; k++) {
			for (int l = 0; l < 3; l++) {
				pos = circularPeriodicX(xt + k - 1) + nx * circularPeriodicY(yt + l - 1);

				output[k][l][0] = splineCoeffReel[pos];
				output[k][l][1] = splineCoeffImag[pos];
			}
		}

		return output;
	}

	public void apodizationHann() {
		for (int i = 0; i < nx; i++) 
		for (int j = 0; j < ny; j++) 
			dataReel[i+j*nx] *= apodize(i, nx) * apodize(j, ny);
	}
	
	private double apodize(double x, double n) {
		return 0.5 *(1.0-Math.cos((x+n/2)*2.0*Math.PI/(n-1)));
	}
	
	public void computeSplineCoeff() {

		splineCoeffImag = new double[nx * ny];
		splineCoeffReel = new double[nx * ny];

		// Swap coefficient to avoid discontinuity in the middle of the image
		swapArray(dataReel, splineCoeffReel);
		swapArray(dataImag, splineCoeffImag);

		// the lines
		for (int indY = 0; indY < ny; indY++) {
			putRow(indY, filterSE(getRow(indY, splineCoeffImag)), splineCoeffImag);
			putRow(indY, filterSE(getRow(indY, splineCoeffReel)), splineCoeffReel);

		}

		// the column
		for (int indX = 0; indX < nx; indX++) {
			putColumn(indX, filterSE(getColumn(indX, splineCoeffReel)), splineCoeffReel);
			putColumn(indX, filterSE(getColumn(indX, splineCoeffImag)), splineCoeffImag);
		}

		// Swap coefficient for matching with the Data format storage
		swapArray(splineCoeffReel, splineCoeffReel);
		swapArray(splineCoeffImag, splineCoeffImag);

	}

	public void swapArray(double[] a, double[] b) {

		double temp = 0;
		for (int indX = 0; indX < nx / 2; indX++) {
			for (int indY = 0; indY < ny / 2; indY++) {

				temp = a[indX + indY * nx];
				b[indX + indY * nx] = a[indX + nx / 2 + (indY + ny / 2) * nx];
				b[indX + nx / 2 + (indY + ny / 2) * nx] = temp;

				temp = a[indX + nx / 2 + indY * nx];
				b[indX + nx / 2 + indY * nx] = a[indX + (indY + ny / 2) * nx];
				b[indX + (indY + ny / 2) * nx] = temp;
			}
		}
	}

	public double[] getRow(int indY, double[] data) {
		double[] row = new double[nx];
		for (int indX = 0; indX < nx; indX++) {
			row[indX] = data[indX + indY * nx];
		}
		return row;
	}

	private void putRow(int indY, double[] row, double[] splineCoeff) {
		for (int indX = 0; indX < nx; indX++) {
			splineCoeff[indX + indY * nx] = row[indX];
		}
	}

	private void putColumn(int indX, double[] col, double[] splineCoeff) {
		for (int indY = 0; indY < ny; indY++) {
			splineCoeff[indX + indY * nx] = col[indY];
		}
	}

	public double[] getColumn(int indX, double[] data) {
		double[] col = new double[ny];
		for (int indY = 0; indY < ny; indY++) {
			col[indY] = data[indY * nx + indX];
		}
		return col;
	}

	private double[] filterSE(double[] s) {
		int n = s.length;
		double c[] = new double[n];
		double c0 = 8;
		double a = 2 * Math.sqrt(2) - 3;
		double cp[] = new double[n];
		cp[0] = computeIVC(s, a);

		for (int i = 1; i < n; i++) {
			cp[i] = a * cp[i - 1] + s[i];
		}
		c[n - 1] = computeIVAC(cp, a);
		for (int i = n - 2; i > -1; i--) {
			c[i] = a * (c[i + 1] - cp[i]);
		}

		for (int i = 0; i < n; i++) {
			c[i] = c0 * c[i];
		}

		return c;
	}

	private double computeIVC(double[] signal, double a) {
		double epsilon = 1e-6;
		int k0 = (int) Math.ceil(Math.log(epsilon) / Math.log(Math.abs(a)));
		double polek = a;
		double v = signal[0];
		for (int k = 1; k < k0; k++) {
			v = v + polek * signal[k];
			polek = polek * a;
		}

		return v;
	}

	private double computeIVAC(double signal[], double a) {
		int n = signal.length;
		return (a / (a * a - 1)) * (signal[n - 1] + a * signal[n - 2]);
	}

	private void verifyCompatibleImage(ImageCartesian second) {
		if (nx != second.nx || ny != second.ny) {

		}
		if (domain != second.domain) {
			throw new IllegalArgumentException("Image domain is different");
		}

		return;
	}

	public double norm1Real() {
		double output = 0.;

		for (int ind = 0; ind < nx * ny; ind++) {

			output += dataReel[ind];

		}
		output /= (nx * ny);

		return output;
	}

	public double[] norm2Separate() {
		double[] output = { 0, 0 };

		for (int ind = 0; ind < nx * ny; ind++) {

			output[0] += dataReel[ind] * dataReel[ind];
			output[1] += dataImag[ind] * dataImag[ind];

		}
		output[0] *= dx * dy;
		output[1] *= dx * dy;

		return output;
	}

	public double[] innerProduct(ImageCartesian g) {
		verifyCompatibleImage(g);
		double[] innerprod = {0, 0};
		for (int ind = 0; ind < nx * ny; ind++) {
			innerprod[0] += dataReel[ind] * g.dataReel[ind] + dataImag[ind] * g.dataImag[ind];
			innerprod[1] += g.dataReel[ind] * dataImag[ind] - dataReel[ind] * g.dataImag[ind];
		}

		innerprod[0] *= dx * dy;
		innerprod[1] *= dx * dy;

		if (domain == Domain.FOURIER) {
			innerprod[0] *= 1 / (Math.PI * Math.PI * 4);
			innerprod[1] *= 1 / (Math.PI * Math.PI * 4);
		}

		return innerprod;
	}

	public double rmse(ImageCartesian second) {

		verifyCompatibleImage(second);

		double result = 0;

		for (int ind = 0; ind < nx * ny; ind++) {

			result += Math.pow(dataReel[ind] - second.dataReel[ind], 2);
			result += Math.pow(dataImag[ind] - second.dataImag[ind], 2);
		}
		result = result / (nx * ny);
		return Math.sqrt(result);
	}

	public double norm() {
		return Math.sqrt(this.innerProduct(this)[0]);
	}

	public double error(final ImageCartesian detector) {
		double[] norm2T = innerProduct(this);
		ImageCartesian diff = new ImageCartesian(this);
		diff.substract(detector);
		double[] norm2Diff = diff.innerProduct(diff);
		if (norm2T[0] < 10e-20)
			return Double.MAX_VALUE;
		return Math.sqrt(norm2Diff[0] / norm2T[0]);
	}

	public double similarity(ImageCartesian detector) {
		double normT = Math.sqrt(this.innerProduct(this)[0]);
		double normD = Math.sqrt(detector.innerProduct(detector)[0]);
		double inProd = this.innerProduct(detector)[0];
		return inProd / (normT * normD);
	}

	public void multiply(double fact) {
		for (int ind = 0; ind < nx * ny; ind++) {
			dataReel[ind] *= fact;
			dataImag[ind] *= fact;
		}
	}

	public void add(ImageCartesian second) {
		verifyCompatibleImage(second);
		for (int ind = 0; ind < nx * ny; ind++) {
			dataReel[ind] += second.dataReel[ind];
			dataImag[ind] += second.dataImag[ind];
		}
	}

	public void substract(ImageCartesian second) {
		verifyCompatibleImage(second);
		for (int ind = 0; ind < nx * ny; ind++) {
			dataReel[ind] -= second.dataReel[ind];
			dataImag[ind] -= second.dataImag[ind];
		}
	}

	public void substractReal(double value) {
		for (int ind = 0; ind < nx * ny; ind++) {
			dataReel[ind] -= value;
		}
	}

	public void abs() {
		for (int ind = 0; ind < nx * ny; ind++) {
			dataReel[ind] = dataReel[ind] < 0 ? -dataReel[ind] : dataReel[ind];
			dataImag[ind] = dataImag[ind] < 0 ? -dataImag[ind] : dataImag[ind];
		}
	}

	public void complexconjugate() {
		for (int ind = 0; ind < nx * ny; ind++) {
			dataImag[ind] = -dataImag[ind];
		}
	}

	public void pointWiseMult(ImageCartesian factor) {
		verifyCompatibleImage(factor);
		double a, b;
		int n = nx*ny;
		for (int k = 0; k < n; k++) {
			a = dataReel[k];
			b = dataImag[k];
			dataReel[k] = a * factor.dataReel[k] - b * factor.dataImag[k];
			dataImag[k] = a * factor.dataImag[k] + b * factor.dataReel[k];
		}
	}

	public void pointWiseMult2(ImageCartesian factor1, ImageCartesian factor2) {
		verifyCompatibleImage(factor1);
		verifyCompatibleImage(factor2);
		double a, b, ta, tb;
		int n = nx*ny;
		for (int k = 0; k < n; k++) {
			a = dataReel[k];
			b = dataImag[k];
			ta = a * factor1.dataReel[k] - b * factor1.dataImag[k];
			tb = a * factor1.dataImag[k] + b * factor1.dataReel[k];
			dataReel[k] = ta * factor2.dataReel[k] - tb * factor2.dataImag[k];
			dataImag[k] = ta * factor2.dataImag[k] + tb * factor2.dataReel[k];
			
		}
	}

	public void pointWiseMultCC(ImageCartesian second) {
		verifyCompatibleImage(second);

		double tempReel;
		double tempImag;

		double tempSecondReel;
		double tempSecondImag;

		for (int ind = 0; ind < nx * ny; ind++) {
			tempReel = dataReel[ind];
			tempImag = dataImag[ind];
			tempSecondReel = second.dataReel[ind];
			tempSecondImag = second.dataImag[ind];

			dataReel[ind] = tempReel * tempSecondReel + tempImag * tempSecondImag;
			dataImag[ind] = -tempReel * tempSecondImag + tempImag * tempSecondReel; // sign ....
		}
	}

	public void updateABImage(ImageCartesian second, double angle) {

		for (int ind = 0; ind < nx * ny; ind++) {
			if (dataReel[ind] < second.dataReel[ind]) {
				dataReel[ind] = second.dataReel[ind];
				dataImag[ind] = angle;
			}
		}
	}

	public boolean dataAreValid() {
		boolean output = true;

		for (int ind = 0; ind < nx * ny; ind++) {
			if (Double.isNaN(dataReel[ind]) || Double.isNaN(dataImag[ind])) {
				output = false;
				System.out.println("wrong index : " + ind);
			}

		}
		return output;
	}

	public static double removeAnisotropicFreq(double rho) {
		double output = 1;
		if (rho > Math.PI) {
			output = 0;
		}

		if (rho > 0.95 * Math.PI && rho <= Math.PI) {
			output = (-rho + Math.PI) / (Math.PI * (1 - 0.95));

			if (output < 0 || output > 1) {
				System.out.println("rho " + rho + "   coeff:" + output);
				throw new IllegalArgumentException("bad coeff");
			}
		}
		return output;
	}

	public void removeAnisotropicFreq() {

		if (domain == Domain.SPACE) {
			throw new IllegalArgumentException("The image should be in Fourier representation");
		}

		for (int indX = 0; indX < nx; indX++) {
			for (int indY = 0; indY < ny; indY++) {

				double rho = indexToRho(indX, indY);
				this.multiplyPixel(indX, indY, removeAnisotropicFreq(rho), 0);
			}
		}
	}

	public ImagePlus getRealPart() {

		double[] real = dataReel.clone();

		swapArray(real, real);

		FloatProcessor fp = new FloatProcessor(nx, ny, real);
		ImagePlus im = new ImagePlus(name, fp);
		return im;
	}

	public ImagePlus getImaginaryPart() {

		double[] real = dataImag.clone();

		swapArray(real, real);

		FloatProcessor fp = new FloatProcessor(nx, ny, real);
		ImagePlus im = new ImagePlus(name + "-Imaginary", fp);
		return im;
	}

	public ImagePlus getArgument() {
		double[] Argument = new double[dataReel.length];

		for (int ind = 0; ind < nx * ny; ind++) {
			Argument[ind] = Math.sqrt(Math.pow(dataReel[ind], 2) + Math.pow(dataImag[ind], 2));
		}

		swapArray(Argument, Argument);

		FloatProcessor fp = new FloatProcessor(nx, ny, Argument);
		ImagePlus im = new ImagePlus(name + "-Arg", fp);
		return im;
	}

	public ImagePlus getArgumentLog() {
		double[] Argument = new double[dataReel.length];

		for (int ind = 0; ind < nx * ny; ind++) {
			Argument[ind] = Math.sqrt(Math.pow(dataReel[ind], 2) + Math.pow(dataImag[ind], 2));
		}

		swapArray(Argument, Argument);

		FloatProcessor fp = new FloatProcessor(nx, ny, Argument);
		fp.log();
		ImagePlus im = new ImagePlus(name + "-ArgLog", fp);
		return im;
	}

	public void saveArgumentOnDisk(String s) {
		double[] Argument = new double[dataReel.length];

		for (int ind = 0; ind < nx * ny; ind++) {
			Argument[ind] = Math.log10(Math.sqrt(Math.pow(dataReel[ind], 2) + Math.pow(dataImag[ind], 2)));
		}

		swapArray(Argument, Argument);

		FloatProcessor fp = new FloatProcessor(nx, ny, Argument);
		ImagePlus im = new ImagePlus(s + " - Argument", fp);
		FileSaver fs = new FileSaver(im);
		fs.saveAsPng();
	}

	public void saveOnDisk(String s) {

		double[] dataCopy = dataReel.clone();

		swapArray(dataCopy, dataCopy);

		FloatProcessor fp = new FloatProcessor(nx, ny, dataCopy);
		ImagePlus im = new ImagePlus(s + "_realCoeff", fp);
		FileSaver fs = new FileSaver(im);
		// fs.saveAsPng();
		fs.saveAsJpeg();

		dataCopy = dataImag.clone();

		swapArray(dataCopy, dataCopy);

		fp = new FloatProcessor(nx, ny, dataCopy);
		im = new ImagePlus(s + " - imaginary Coeff", fp);
		fs = new FileSaver(im);
		// fs.saveAsPng();

	}

}

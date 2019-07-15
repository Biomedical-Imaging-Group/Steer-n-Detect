package steerabledetector.fftacademic;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class ComplexProcessor {

	public FloatProcessor real;
	public FloatProcessor imag;

	public enum Representation {
		CARTESIAN, POLAR
	};

	public ComplexProcessor(int nx, int ny) {
		real = new FloatProcessor(nx, ny);
		imag = new FloatProcessor(nx, ny);
	}
	
	public ComplexProcessor(ImageProcessor im1, ImageProcessor im2, Representation r) {
		FloatProcessor fp1 = im1.convertToFloatProcessor();
		FloatProcessor fp2 = im2.convertToFloatProcessor();
		if (r == Representation.CARTESIAN) {
			this.real = fp1;
			this.imag = fp2;
		} else {
			this.real = computeReal(fp1, fp2);
			this.imag = computeImaginary(fp1, fp2);
		}
	}

	public ComplexProcessor(ImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
		this.real = ip.convertToFloatProcessor();
		this.imag = new FloatProcessor(real.getWidth(), real.getHeight());
	}

	public ComplexProcessor(ImageProcessor real) {
		this.real = real.convertToFloatProcessor();
		this.imag = new FloatProcessor(real.getWidth(), real.getHeight());
	}

	public ComplexProcessor multiply(ComplexProcessor b) {
		int nx = real.getWidth();
		int ny = real.getWidth();
		ComplexProcessor out = new ComplexProcessor(nx, ny);
		float[] ar = (float[])real.getPixels();
		float[] ai = (float[])imag.getPixels();
		float[] br = (float[])b.real.getPixels();
		float[] bi = (float[])b.imag.getPixels();
		float[] or = (float[])out.real.getPixels();
		float[] oi = (float[])out.imag.getPixels();
		for(int k=0; k<nx*ny; k++) {
			or[k] = ar[k]*br[k] - ai[k]*bi[k];
			oi[k] = ar[k]*bi[k] + ai[k]*br[k];
		}
		return out;
	}
	public ComplexProcessor transform() {
		int nx = real.getWidth();
		int ny = real.getHeight();
		float[] re = (float[]) real.getPixels();
		float[] im = (float[]) imag.getPixels();
		AcademicFFT fft = new AcademicFFT(nx, ny, 0, 0);
		fft.directTransform(re, im, null, null, AcademicFFT.InputDataType.COMPLEXINPUT);
		shift(re, nx, ny);
		shift(im, nx, ny);
		FloatProcessor real = new FloatProcessor(nx, ny, re);
		FloatProcessor imag = new FloatProcessor(nx, ny, im);
		
		return new ComplexProcessor(real, imag, Representation.CARTESIAN);
	}

	public ComplexProcessor inverse() {
		int nx = real.getWidth();
		int ny = imag.getHeight();
		AcademicFFT fft = new AcademicFFT(nx, ny, 0, 0);
		float[] re = (float[]) real.getPixels();
		float[] im = (float[]) imag.getPixels();
		shift(re, nx, ny);
		shift(im, nx, ny);
		fft.inverseTransform(re, im, null, null);
		FloatProcessor real = new FloatProcessor(nx, ny, re);
		FloatProcessor imag = new FloatProcessor(nx, ny, im);
		return new ComplexProcessor(real, imag, Representation.CARTESIAN);
	}

	public void hermittian() {
		int nx = real.getWidth();
		int ny = real.getHeight();
		float[] re = (float[]) real.getPixels();
		float[] im = (float[]) imag.getPixels();
		AcademicFFT fft = new AcademicFFT(nx, ny, 0, 0);
		fft.makeHermitian(re, im, null, null);
	}

	public ComplexProcessor translate(double dx, double dy) {
		real.translate(dx, dy);
		imag.translate(dx, dy);
		return this;
	}
	
	public void add(ComplexProcessor c1, ComplexProcessor c2, ComplexProcessor c3) {
		int nx = c1.getWidth();
		int ny = c1.getHeight();
		ComplexProcessor c = new ComplexProcessor(nx, ny);
		float r[] = (float[])real.getPixels();
		float i[] = (float[])imag.getPixels();
		
		float r1[] = (float[])c1.real.getPixels();
		float i1[] = (float[])c1.imag.getPixels();
		
		float r2[] = (float[])c2.real.getPixels();
		float i2[] = (float[])c2.imag.getPixels();
		
		float r3[] = (float[])c3.real.getPixels();
		float i3[] = (float[])c3.imag.getPixels();
		
		for (int k = 0; k < nx*ny; k++) {
			r[k] += r1[k] + r2[k] + r3[k];
			i[k] += i1[k] + i2[k] + i3[k];
		}
	}

	public FloatProcessor computeModule() {
		int nx = real.getWidth();
		int ny = real.getHeight();
		float[] re = (float[]) real.getPixels();
		float[] im = (float[]) imag.getPixels();
		int n = re.length;
		float module[] = new float[n];
		for (int k = 0; k < n; k++)
			module[k] = (float) Math.sqrt(re[k] * re[k] + im[k] * im[k]);
		return new FloatProcessor(nx, ny, module);
	}

	public FloatProcessor computeLogModule() {
		int nx = real.getWidth();
		int ny = real.getHeight();
		float[] re = (float[]) real.getPixels();
		float[] im = (float[]) imag.getPixels();
		int n = re.length;
		float module[] = new float[n];
		for (int k = 0; k < n; k++)
			module[k] = (float) (10.0 * Math.log10(re[k] * re[k] + im[k] * im[k]));
		return new FloatProcessor(nx, ny, module);
	}

	public FloatProcessor computePhase() {
		int nx = real.getWidth();
		int ny = real.getHeight();
		float[] re = (float[]) real.getPixels();
		float[] im = (float[]) imag.getPixels();
		int n = re.length;
		float phase[] = new float[n];
		for (int k = 0; k < n; k++)
			phase[k] = (float) Math.atan2(im[k], re[k]);
		return new FloatProcessor(nx, ny, phase);
	}

	public FloatProcessor computeReal(FloatProcessor module, FloatProcessor phase) {
		int nx = module.getWidth();
		int ny = module.getHeight();
		float[] m = (float[]) module.getPixels();
		float[] p = (float[]) phase.getPixels();
		int n = m.length;
		float re[] = new float[n];
		for (int k = 0; k < n; k++)
			re[k] = (float) (m[k] * Math.cos(p[k]));
		return new FloatProcessor(nx, ny, re);
	}

	public FloatProcessor computeImaginary(FloatProcessor module, FloatProcessor phase) {
		int nx = module.getWidth();
		int ny = module.getHeight();
		float[] m = (float[]) module.getPixels();
		float[] p = (float[]) phase.getPixels();
		int n = m.length;
		float im[] = new float[n];
		for (int k = 0; k < n; k++)
			im[k] = (float) (m[k] * Math.sin(p[k]));
		return new FloatProcessor(nx, ny, im);
	}

	public int getWidth() {
		return real.getWidth();
	}

	public int getHeight() {
		return real.getHeight();
	}

	public float[] shift(float a[], int nx, int ny) {
		int xshift = nx / 2;
		int yshift = ny / 2;
		for (int y = 0; y < ny; y++) {
			float u[] = new float[nx];
			float v[] = new float[nx];
			for (int x = 0; x < nx; x++)
				u[x] = a[x + y * nx];
			System.arraycopy(u, xshift, v, 0, nx - xshift);
			System.arraycopy(u, 0, v, nx - xshift, xshift);
			for (int x = 0; x < nx; x++)
				a[x + y * nx] = v[x];
		}
		for (int x = 0; x < nx; x++) {
			float u[] = new float[ny];
			float v[] = new float[ny];
			for (int y = 0; y < ny; y++)
				u[y] = a[x + y * nx];
			System.arraycopy(u, yshift, v, 0, ny - yshift);
			System.arraycopy(u, 0, v, ny - yshift, yshift);
			for (int y = 0; y < ny; y++)
				a[x + y * nx] = v[y];
		}
		return a;
	}
}

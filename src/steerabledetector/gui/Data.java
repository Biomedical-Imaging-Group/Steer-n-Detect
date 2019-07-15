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

package steerabledetector.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;

import ij.ImagePlus;
import steerabledetector.detector.Detection;
import steerabledetector.detector.Parameters;
import steerabledetector.gui.components.SpinnerDouble;
import steerabledetector.image2d.Image2DDouble;

public class Data {

	private ArrayList<Detection>	detsDetected		= new ArrayList<Detection>();
	private ArrayList<Detection>	detsSelected 		= new ArrayList<Detection>();
	private ArrayList<Detection>	detsManual			= new ArrayList<Detection>();
	private ArrayList<double[]>		localMax			= new ArrayList<double[]>();

	private double				minValueFeature[]		= new double[4];
	private double				maxValueFeature[]		= new double[4];
	private String				mostSignificantFeature	= "";
	private int					mostSignificantNumber	= 999999;

	private ImagePlus			imp;
	private Parameters          params;

	private double				lowerFeature[]			= new double[4];
	private double				upperFeature[]			= new double[4];

	public Data(ImagePlus imp, Parameters params) {
		this.imp = imp;
		this.params = params;
	}

	public void setLocalMax(ArrayList<double[]> localMax) {
		this.localMax = localMax;
	}

	public ArrayList<double[]> getLocalMax() {
		return localMax;
	}

	public ArrayList<Detection> getSelected() {
		return detsSelected;
	}

	public ArrayList<Detection> getDetected() {
		return detsDetected;
	}

	public ArrayList<Detection> getManual() {
		return detsManual;
	}
	
	public ArrayList<Detection> getSelectedAndManual(boolean automatic, boolean manual) {
		ArrayList<Detection> detsAll = new ArrayList<Detection>();
		if (automatic)
			for (Detection detection : detsSelected)
				detsAll.add(detection);
		if (manual)
			for (Detection detection : detsManual)
				detsAll.add(detection);
		return detsAll;
	}

	public void setDetections(ArrayList<Detection> detsAutomatic, ArrayList<Detection> detsManual_) {
		
		detsSelected = new ArrayList<Detection>();
		detsDetected = new ArrayList<Detection>();
		
		for (Detection detection : detsAutomatic) {
			detsDetected.add(detection);
			detsSelected.add(detection);
		}
		detsManual = new ArrayList<Detection>();
		for (Detection detection : detsManual_) {
			detsManual.add(detection);
		}

		ArrayList<Detection> detsAll = getSelectedAndManual(true, true);

		for (int i = 0; i < lowerFeature.length; i++) {
			lowerFeature[i] = Double.MAX_VALUE;
			maxValueFeature[i] = -Double.MAX_VALUE;
		}

		
		for (Detection detection : detsAll) {
			double[] feature = detection.getFeature();
			for (int i = 0; i < upperFeature.length; i++)
				lowerFeature[i] = Math.min(lowerFeature[i], feature[i]);
			for (int i = 0; i < upperFeature.length; i++)
				upperFeature[i] = Math.max(upperFeature[i], feature[i]);
		}
		
		lowerFeature[0] = 0;
		lowerFeature[1] = 0;
		maxValueFeature[0] = imp.getWidth() + 1;
		maxValueFeature[1] = imp.getHeight() + 1;

		resetRangeValue();
	}

	public Detection findDetInDetect(int id) {
		for (Detection detection : detsDetected)
			if (detection.id == id)
				return detection;
		return null;
	}

	public Detection findDetection(int id) {
		for (Detection detection : detsDetected)
			if (detection.id == id)
				return detection;
		for (Detection detection : detsManual)
			if (detection.id == id)
				return detection;
		for (Detection detection : detsSelected)
			if (detection.id == id)
				return detection;
		return null;
	}

	public int findDetectionID(int xm, int ym, boolean automatic, boolean manual) {
		double dAuto = Double.MAX_VALUE;
		int idAuto = -1;
		
		double psX = 0.5*params.patternSizeX;
		double psY = 0.5*params.patternSizeY;
		
		int r = (int) Math.sqrt(psX*psX+psY*psY);
		
		if (automatic)
			for (Detection detection : detsSelected)
				if (detection.contains(xm, ym, r)) {
					double d = detection.distance(xm, ym);
					if (d < dAuto) {
						idAuto = detection.id;
						dAuto = d;
					}
				}

		double dManual = Double.MAX_VALUE;
		int idManual = -1;
		if (manual)
			for (Detection detection : detsManual)
				if (detection.contains(xm, ym,r)) {
					double d = detection.distance(xm, ym);
					if (d < dManual) {
						idManual = detection.id;
						dManual = d;
					}
				}

		if (idAuto == -1) {
			if (idManual == -1)
				return -1;
			else
				return idManual;
		}
		else {
			if (idManual == -1)
				return idAuto;
			else
				return (dAuto < dManual ? idAuto : idManual);
		}
	}

	public void move(int id, double x, double y, double angle, double amplitude, Image2DDouble input) {
		
		for (Detection detection : detsDetected)
			if (detection.id == id) {
				detection.x = x;
				detection.y = y;
				detection.angle = angle;
				detection.amplitude = amplitude;
				detection.setManual();
				detsDetected.remove(detection);
				detsSelected.remove(detection);
				detsManual.add(detection);
				return;
			}
			
		for (Detection detection : detsManual)
			if (detection.id == id) {
				detection.x = x;
				detection.y = y;
				detection.angle = angle;
				detection.amplitude = amplitude;
				return;
			}
	}

	public void remove(int id) {
		remove(id, detsSelected);
		remove(id, detsManual);
	}

	public void remove(int id, ArrayList<Detection> detections) {
		Detection delete = null;
		for (Detection detection : detections)
			if (detection.id == id)
				delete = detection;
		if (delete != null)
			detections.remove(delete);
	}

	public void add(Detection detection) {
		detection.setManual();
		int max = 0;
		for (Detection s : detsManual)
			if (s.id > max)
				max = s.id;
		for (Detection s : detsDetected)
			if (s.id > max)
				max = s.id;
		for (Detection s : detsSelected)
			if (s.id > max)
				max = s.id;
		
		detection.id = max+1;
		detsManual.add(detection);
	}

	public SpinnerDouble getLowerSpinner(int i) {
		double a = lowerFeature[i];
		double b = upperFeature[i];
		double step = 0;
		if (b > a) {
			step = (b - a) / 100.0;
		}
		return new SpinnerDouble(minValueFeature[i], a - step, b + step, step);
	}

	public SpinnerDouble getUpperSpinner(int i) {
		double a = lowerFeature[i];
		double b = upperFeature[i];
		double step = 0;
		if (b > a) {
			step = (b - a) / 100.0;
		}
		return new SpinnerDouble(maxValueFeature[i], a - step, b + step, step);
	}

	public void setLimitValue(int i, double min, double max) {
		maxValueFeature[i] = max;
		minValueFeature[i] = min;
	}

	public boolean loadCVS(String filename) {
		String line = "";
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(filename));
			line = buffer.readLine();
			line = buffer.readLine();
			ArrayList<Detection> detsAutoLoaded = new ArrayList<Detection>();
			ArrayList<Detection> detsManualLoaded = new ArrayList<Detection>();
			while (line != null) {
				StringTokenizer tokens = new StringTokenizer(line, ",");
				int id = Integer.parseInt(tokens.nextToken().trim());
				int x = (int) Double.parseDouble(tokens.nextToken().trim());
				int y = (int) Double.parseDouble(tokens.nextToken().trim());
				double r = Double.parseDouble(tokens.nextToken().trim());
				double a = Double.parseDouble(tokens.nextToken().trim());
				double size = Double.parseDouble(tokens.nextToken().trim());
				String type = tokens.nextToken().trim();
				if (type.startsWith("A"))
					detsAutoLoaded.add(new Detection(id, x, y, r, a, size, type));
				else
					detsManualLoaded.add(new Detection(id, x, y, r, a, size, type));
				line = buffer.readLine();
			}
			buffer.close();
			setDetections(detsAutoLoaded, detsManualLoaded);
			return true;
		}
		catch (Exception ex) {
			System.out.println("Unable to read the file " + line);
		}
		return false;
	}

	public void saveCVS(String filename) {
		File file = new File(filename);
		try {
			String headers[] = Detection.toArrayStringHeader();
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
			String s = "";
			for (int i = 0; i < headers.length; i++)
				s += headers[i] + ",";
			buffer.write(s + "\n");
			for (Detection detection : detsSelected)
				buffer.write(detection.toStringComma() + "\n");
			for (Detection detection : detsManual)
				buffer.write(detection.toStringComma() + "\n");
			buffer.close();
		}
		catch (IOException ex) {
			System.out.println("" + ex);
		}
	}

	public String getSignificantFeature() {
		return mostSignificantFeature;
	}

	public int getSignificantNumber() {
		return mostSignificantNumber;
	}

	public void setSignificantFeature(String feature) {
		mostSignificantFeature = feature;
	}

	public void setSignificantNumber(int number) {
		mostSignificantNumber = number;
	}

	public void copyRangeValuesTo(double[] min, double max[]) {
		for (int i = 0; i < min.length; i++) {
			min[i] = minValueFeature[i];
			max[i] = maxValueFeature[i];
		}
	}

	public void copyRangeValuesFrom(double[] min, double max[]) {
		for (int i = 0; i < min.length; i++) {
			minValueFeature[i] = min[i];
			maxValueFeature[i] = max[i];
		}
	}

	public void resetRangeValue() {
		for (int i = 0; i < maxValueFeature.length; i++) {
			minValueFeature[i] = lowerFeature[i];
			maxValueFeature[i] = upperFeature[i];
		}
	}

	public void changeRatioSelected(boolean increase) {
		int m = detsSelected.size();
		int n = detsDetected.size();
		if (n > 0) {
			double inc = (increase ? Math.max(1, (n-m)*0.1) : Math.min(-1, (0-m)*0.1));
			select("Contrast", (int)Math.round(m + inc));
		}
	}

	public void select(String mostSignificantFeature, int mostSignificantNumber) {
		this.mostSignificantFeature = mostSignificantFeature;
		this.mostSignificantNumber = mostSignificantNumber;
		select();
	}

	public void selectInitContrast(double contrastMinimum) {
		minValueFeature[4] = contrastMinimum;
		select();
		resetRangeValue();
	}

	public void select(double minValueFeature[], double maxValueFeature[]) {
		this.minValueFeature = minValueFeature;
		this.maxValueFeature = maxValueFeature;
		select();

	}

	public void selectAll() {
		resetRangeValue();
		mostSignificantFeature = "";
	}

	private void select() {

		detsSelected.clear();
		String feature = mostSignificantFeature;

		if (feature.equals("X"))
			sortX();
		if (feature.equals("Y"))
			sortY();
		if (feature.equals("Angle"))
			sortAngle();
		if (feature.equals("Confidence"))
			sortConfidence();

		ArrayList<Detection> detsSignificant = new ArrayList<Detection>();
		for (int i = 0; i < Math.min(mostSignificantNumber, detsDetected.size()); i++) {
			detsSignificant.add(detsDetected.get(i));
		}

		for (Detection detection : detsSignificant) {
			if (!detection.isManual()) {
				if (detection.x >= minValueFeature[0] && detection.x <= maxValueFeature[0])
					if (detection.y >= minValueFeature[1] && detection.y <= maxValueFeature[1])
						if (detection.angle >= minValueFeature[2] && detection.angle <= maxValueFeature[2])
							if (detection.amplitude >= minValueFeature[3] && detection.amplitude <= maxValueFeature[3])
										detsSelected.add(detection);
			}
		}

	}

	private void sortX() {
		Collections.sort(detsDetected, new Comparator<Detection>() {
			@Override
			public int compare(Detection detection1, Detection detection2) {
				return (detection1.x < detection2.x ? 1 : -1);
			}
		});
	}

	public void sortY() {
		Collections.sort(detsDetected, new Comparator<Detection>() {
			@Override
			public int compare(Detection detection1, Detection detection2) {
				return (detection1.y < detection2.y ? 1 : -1);
			}
		});
	}

	private void sortAngle() {
		Collections.sort(detsDetected, new Comparator<Detection>() {
			@Override
			public int compare(Detection detection1, Detection detection2) {
				return (detection1.angle < detection2.angle ? 1 : -1);
			}
		});

	}

	private void sortConfidence() {
		Collections.sort(detsDetected, new Comparator<Detection>() {
			@Override
			public int compare(Detection detection1, Detection detection2) {
				return (detection1.amplitude < detection2.amplitude ? 1 : -1);
			}
		});

	}
}

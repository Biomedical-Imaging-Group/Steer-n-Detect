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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ij.IJ;
import ij.ImagePlus;
import steerabledetector.filter.SIPM;
import steerabledetector.gui.Data;
import steerabledetector.gui.components.HTMLPane;
import steerabledetector.gui.components.ProgressionBar;
import steerabledetector.image2d.ImageCartesian;

public class SteerableDetector {

	private ImagePlus		imp;
	private SIPM				model;
	private Parameters		params;
	private ProgressionBar	progress;

	private double[][]		map;
	private double[][]		angles;

	private Data				data;
	
	public SteerableDetector(ImagePlus imp, SIPM model, Parameters params, ProgressionBar progress, HTMLPane info) {
		this.imp = imp;
		this.model = model;
		this.params = params;
		this.data = new Data(imp, params);
		this.progress = progress;
	}

	public String getName() {
		return "Wavelet-based scale estimation";
	}

	public Data getData() {
		return data;
	}

	public Detection getDetection(int id, int x, int y) {
		double size = 0.5*(params.patternSizeX + params.patternSizeY);
		return new Detection(id, x, y, angles[x][y], map[x][y], size, "Auto");
	}

	public Detection getDetection(int x, int y) {
		if (angles == null)
			return null;
		if (map == null)
			return null;
		double size = 0.5*(params.patternSizeX + params.patternSizeY);
		return new Detection(1000, x, y, angles[x][y], map[x][y], size, "Auto");
	}

	public void analysis() {
		ImageCartesian imageToAnalyze = ImageCartesian.getImage(imp);
		progress.progress("Start steering", 10);
		ImageCartesian IC = model.steeredAnalysis(progress, imageToAnalyze, params.nHarmonics, params);
		// amplitudes in the real part
		// angles in the imaginary part
		progress.progress("analysis", 20);

		map = IC.getReal();
		progress.progress("map", 30);
		normalizeMap();
		progress.progress("normalize", 40);

		angles = IC.getImag();
		angles2Deg();

		ArrayList<Detection> spotsSteered = new ArrayList<Detection>();
		ArrayList<double[]> localMaxPositions = findLocalMax3x3(map, params.margin);
		progress.progress("local max", 50);

		data.setLocalMax(localMaxPositions);

		double[] coord = new double[2];
		int x, y;
		double size = 0.5*(params.patternSizeX + params.patternSizeY);
		int n = localMaxPositions.size();
		progress.reset("Detection");
		for (int i = 0; i <n ; ++i) {
			coord = localMaxPositions.get(i);
			x = (int) coord[0];
			y = (int) coord[1];
			Detection detection = new Detection(spotsSteered.size(), x, y, angles[x][y], map[x][y], size, "Auto");
			if (detection != null) {
				spotsSteered.add(detection);
			}
			progress.progress("Detection " + (i+1) + "/" + n, (double)(i*100.0/n));
		}

		progress.progress("Start trim ", 90);
		ArrayList<Detection> detections = trim(spotsSteered, params.nDetections, params.overlap);
		progress.progress("End trim ", 100);

		if (data == null)
			IJ.log("data: Null.");
		data.setDetections(detections, new ArrayList<Detection>());

	}

	private ArrayList<double[]> findLocalMax3x3(double[][] map, double margin) {
		int nx = map.length;
		int ny = map[0].length;
		int m = (int) Math.max(1, margin);
		ArrayList<double[]> locMaxPosition = new ArrayList<double[]>();
		for (int i = m; i < nx - m; ++i) {
			for (int j = m; j < ny - m; ++j) {
				double test = map[i][j];
				if (test > 0)
					if (map[i - 1][j - 1] <= test)
						if (map[i - 1][j] <= test)
							if (map[i - 1][j + 1] <= test)
								if (map[i + 1][j - 1] <= test)
									if (map[i + 1][j] <= test)
										if (map[i + 1][j + 1] <= test)
											if (map[i][j - 1] <= test)
												if (map[i][j + 1] <= test) {
													locMaxPosition.add(new double[] { i, j, test });
												}
			}
		}
		return locMaxPosition;
	}

	private ArrayList<Detection> trim(ArrayList<Detection> detections, int maxNumber, double proximity) {
		Collections.sort(detections, new Comparator<Detection>() {
			@Override
			public int compare(Detection spot1, Detection spot2) {
				return (spot1.amplitude < spot2.amplitude ? 1 : -1);

			}
		});

		ArrayList<Detection> goods = new ArrayList<Detection>();
		for (Detection candidate : detections) {
			boolean flag = false;
			for (Detection good : goods)
				if (good.distance(candidate) < proximity)
					flag = true;
			if ((flag == false)) {
				candidate.id = goods.size();
				goods.add(candidate);
			}
			if (goods.size() >= maxNumber) {	
				return goods;
			}
		}
		return goods;
	}

	private void normalizeMap() {

		double maxval = -Double.MAX_VALUE;
		double minval = Double.MAX_VALUE;
		for (int i = 0; i < map.length; ++i) {
			for (int j = 0; j < map[0].length; ++j) {
				if (map[i][j] < minval)
					minval = map[i][j];
				if (map[i][j] > maxval)
					maxval = map[i][j];
			}
		}
		for (int i = 0; i < map.length; ++i) {
			for (int j = 0; j < map[0].length; ++j) {
				if (maxval - minval != 0)
					map[i][j] = (map[i][j] - minval) / (maxval - minval);
				else
					map[i][j] = Double.MAX_VALUE;
			}
		}
	}

	private void angles2Deg() {
		for (int i = 0; i < angles.length; ++i) {
			for (int j = 0; j < angles[0].length; ++j) {
				angles[i][j] = 180 * angles[i][j] / Math.PI;
			}
		}

	}

}
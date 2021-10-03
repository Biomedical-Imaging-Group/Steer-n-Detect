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
package steerabledetector.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GUI;
import ij.plugin.frame.Recorder;
import steerabledetector.Constants;
import steerabledetector.detector.OutputMode;
import steerabledetector.detector.Parameters;
import steerabledetector.detector.RunningMode;
import steerabledetector.detector.SteerableDetector;
import steerabledetector.filter.SIPM;
import steerabledetector.gui.components.GridPanel;
import steerabledetector.gui.components.HTMLPane;
import steerabledetector.gui.components.ProgressionBar;
import steerabledetector.gui.components.SliderTextHorizontal;
import steerabledetector.gui.components.SpinnerInteger;
import steerabledetector.gui.settings.Settings;

public class DialogMain extends JDialog implements Runnable, ActionListener {

	private Settings						settings			= new Settings(Constants.name, Constants.settings);
	private Data							data;
	private SIPM							model;
	private SteerableDetector			detector;
	private Parameters					params;

	private JButton						bnStop			= new JButton("Stop");
	private JButton						bnHelp1			= new JButton("Help");
	private JButton						bnHelp2			= new JButton("Help");
	private JButton						bnAdvanced		= new JButton("Advanced ...");
	private JButton						bnClose			= new JButton("Close");
	private JButton						bnRun			= new JButton("Run");
	
	private ProgressionBar				progress			= new ProgressionBar(Constants.copyright);
	private HTMLPane						info				= new HTMLPane(300, 300);
	private ImagePlus					imp;

	private SpinnerInteger				spnNumberMax		= new SpinnerInteger(1000, 1, 999999, 1);

	private JCheckBox					chkMultithread	= new JCheckBox("Multithread");
	private JCheckBox					chkAnalyzedImage	= new JCheckBox("Show analysis");

	private OutputMode 					outputMode		= OutputMode.SAVE;
	private RunningMode					runningMode		= RunningMode.STANDARD;
	private boolean						stop				= false;

	private JRadioButton					chkAutomatic		= new JRadioButton("Automatic");
	private JRadioButton					chkCustom		= new JRadioButton("Custom", true);
	private GridPanel					pnParams			= new GridPanel(true, 4);
	private GridPanel					pnSave			= new GridPanel("Output file", 4);

	private JPanel						cards			= new JPanel(new CardLayout());
	private SliderTextHorizontal			slider 			= new SliderTextHorizontal();

	private AdvancedDialog				dlgAdvanced;
	private TemplatePanel				template;

	private CanvasTemplate				canvas;
	
	public DialogMain(ImagePlus imp, RunningMode runningMode, OutputMode outputMode, Parameters params) {
		super(new JFrame(), "Steer'n'Detect " + (runningMode == RunningMode.PLAIN ? "Plain" : ""));

		this.detector = null;
		this.model = null;
		this.imp = imp;
		this.data = null;
		this.params = params;
		this.runningMode = runningMode;
		this.outputMode = outputMode;
		
		spnNumberMax.set(params.nDetections);
		
		dlgAdvanced = new AdvancedDialog(params, settings);
		template	 = new TemplatePanel(this, imp, progress, info, params);

		ButtonGroup group1 = new ButtonGroup();
		group1.add(chkAutomatic);
		group1.add(chkCustom);
		
		// Panel Parameters
		pnParams.place(0, 0, 2, 1, slider);
		pnParams.place(1, 0, "Max. Detections");
		pnParams.place(1, 1, spnNumberMax);

		// Panel Mode
		GridPanel pnMode = new GridPanel(false, 4);
		pnMode.place(0, 0, chkAutomatic);
		pnMode.place(0, 1, chkCustom);
		pnMode.place(0, 3, bnAdvanced);

		// Panel Parameters
		GridPanel pnParameter = new GridPanel("Parameters", 4);

		if (runningMode == RunningMode.PLAIN) {
			pnParameter.place(4, 0, pnSave);
		}

		pnParameter.place(1, 0, 2, 1, pnMode);
		pnParameter.place(2, 0, 2, 1, pnParams);

		GridPanel pnButtons = new GridPanel(false, 4);
		pnButtons.place(0,  0, bnHelp1);
		pnButtons.place(0,  1, bnClose);
		pnButtons.place(0,  2, bnRun);

		// Panel Status
		JToolBar pnStatus = new JToolBar("status");
		pnStatus.setLayout(new BorderLayout(0, 0));
		pnStatus.setFloatable(false);
		pnStatus.add(bnHelp2, BorderLayout.WEST);
		pnStatus.add(progress, BorderLayout.CENTER);
		pnStatus.add(bnStop, BorderLayout.EAST);

		// Central part of the dialog - 2 cards
		JPanel pnJournal = new JPanel(new BorderLayout(0, 0));
		pnJournal.add(info.getPane(), BorderLayout.CENTER);
		pnJournal.add(pnStatus, BorderLayout.SOUTH);

		JPanel pnDetector = new JPanel();
		pnDetector.setLayout(new BoxLayout(pnDetector, BoxLayout.PAGE_AXIS));
		pnDetector.add(template);
		pnDetector.add(pnParameter);
		pnDetector.add(pnButtons);
		
		cards.add(pnDetector, "detector");
		cards.add(pnJournal, "journal");

		// Main panel
		setLayout(new BorderLayout());
		add(new TitlePane(), BorderLayout.NORTH);
		add(cards, BorderLayout.CENTER);

		setCard(runningMode == RunningMode.MACRO ? "journal" : "detector");

		bnAdvanced.addActionListener(this);
		bnClose.addActionListener(this);
		bnRun.addActionListener(this);
		bnStop.addActionListener(this);
		bnHelp1.addActionListener(this);
		bnHelp2.addActionListener(this);
		chkAutomatic.addActionListener(this);
		chkCustom.addActionListener(this);

		settings.record("spnNumberMax", spnNumberMax, "1000");
		settings.record("chkMultithread", chkMultithread, true);
		settings.record("chkAnalyzedImage", chkAnalyzedImage, false);
		params.gamma = settings.loadValue("gamma", 0);
		settings.loadRecordedItems();
		slider.setValue(params.gamma);
		pack();
		updateInterface();

		GUI.center(this);
		setVisible(true);
		setModal(false);
		setResizable(false);
		canvas = new CanvasTemplate(imp);
		
		if (runningMode == RunningMode.MACRO) {
			template.getRoi();
			template.run();
			run();
			data.saveCVS(params.filename);
		}
	}

	private void setCard(String name) {
		CardLayout cl = (CardLayout) (cards.getLayout());
		cl.show(cards, name);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == bnClose) {
			settings.storeRecordedItems();
			settings.storeValue("gamma", params.gamma);
			dispose();
		}
		else if (event.getSource() == bnStop)
			stop();
		else if (event.getSource() == bnHelp1)
			WebBrowser.open(Constants.urlHelp);
		else if (event.getSource() == bnHelp2)
			WebBrowser.open(Constants.urlHelp);
		else if (event.getSource() == chkAutomatic)
			updateInterface();
		else if (event.getSource() == chkCustom)
			updateInterface();
		else if (event.getSource() == bnAdvanced)
			dlgAdvanced.setVisible(true);
		else if (event.getSource() == bnRun) {
			if (imp == null) {
				IJ.error("Invalid source image.");
				return;
			}
			if (imp.getProcessor() == null) {
				IJ.error("Invalid source image.");
				return;
			}
			if (model == null) {
				IJ.error("The template model is not defined");
				return;
			}
			Thread thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
			settings.storeRecordedItems();
		}
	}

	@Override
	public void run() {
		stop = false;
		if (model != null)
			model.start();
		info.clear();
		setCard("journal");
		setParameters();
		double chrono = System.nanoTime();
		progress.reset("Detector ");
		
		this.detector = new SteerableDetector(imp, model, params, progress, info);
		this.detector.analysis();
		data = detector.getData();
		if (stop) {
			stop();
			return;
		}
		
		dispose();
		info.append("p", "Time: " + new SimpleDateFormat("dd MMMM yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
		info.append("p", "" + data.getLocalMax().size() + " local maxima");
		info.append("p", "" + data.getDetected().size() + " detections");
		info.append("p", "" + data.getSelected().size() + " detections");

		int mb = 1024 * 1024;
		Runtime instance = Runtime.getRuntime();
		info.append("p", "Total memory: " + instance.totalMemory() / mb + "Mb");
		info.append("p", "Used memory: " + (instance.totalMemory() - instance.freeMemory()) / mb + "Mb");
		info.append("p", "Elapsed time: " + (System.nanoTime() - chrono) / (1000000.0) + "ms");

		//if (outputMode == OutputMode.SELECTION) 
			new DialogSelection(imp, detector, data, params, info);
			
		if (outputMode == OutputMode.SAVE)
			data.saveCVS(params.filename);
	}

	private void setParameters() {
		params.gamma = slider.getValue();
		if (runningMode != RunningMode.MACRO) {
			params.nDetections = spnNumberMax.get();
			dlgAdvanced.setParameters();
		}
		params.setInformation(imp, info);
		if (Recorder.record)
			Recorder.record("run", "SteerDetect Run", params.toMacro());
	}

	private void updateInterface() {
		Component cs[] = pnParams.getComponents();
		params.gamma = slider.getValue();
		if (chkAutomatic.isSelected()) {
			params.setAutomaticValues();
			spnNumberMax.set(params.nDetections);
			dlgAdvanced.getParameters();
			bnAdvanced.setEnabled(false);
			for (Component c : cs)
				c.setEnabled(false);
		}

		if (chkCustom.isSelected()) {
			bnAdvanced.setEnabled(true);
			settings.loadRecordedItems();
			for (Component c : cs)
				c.setEnabled(true);
		}
	}

	private void stop() {
		stop = true;

		if (model != null)
			model.stop();

		if (runningMode == RunningMode.STANDARD)
			setCard("detector");
		else
			dispose();
	}

	public void setModel(SIPM model) {
		this.model = model;
	}
}

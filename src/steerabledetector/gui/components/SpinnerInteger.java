//====================================================================================================
// Project: SpotCaliper
// 
// Authors: Zsuzsanna Puspoki and Daniel Sage
// Organization: Biomedical Imaging Group (BIG), Ecole Polytechnique Federale de Lausanne
// Address: EPFL-STI-IMT-LIB, 1015 Lausanne, Switzerland
//
// Information: http://bigwww.epfl.ch/algorithms/spotcaliper/
//
// References:
// Zsuzsanna Puspoki et al.
// SpotCaliper: Fast Wavelet-based Spot Detection with Accurate Size Estimation 
// Bioinformatics Oxford, submitted in June 2015.
// Available at: http://bigwww.epfl.ch/publications/
//
// Conditions of use:
// You'll be free to use this software for research purposes, but you should not redistribute 
// it without our consent. In addition, we expect you to include a citation whenever you 
// present or publish results that are based on it.
//====================================================================================================
package steerabledetector.gui.components;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class SpinnerInteger extends JSpinner {

	private SpinnerNumberModel	model;

	private int					defValue;
	private int					minValue;
	private int					maxValue;
	private int					incValue;

	/**
	 * Constructor.
	 */
	public SpinnerInteger(int defValue, int minValue, int maxValue, int incValue) {
		super();
		this.defValue = defValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.incValue = incValue;
		Integer def = new Integer(defValue);
		Integer min = new Integer(minValue);
		Integer max = new Integer(maxValue);
		Integer inc = new Integer(incValue);
		model = new SpinnerNumberModel(def, min, max, inc);
		setModel(model);
		JFormattedTextField tf = ((JSpinner.DefaultEditor) getEditor()).getTextField();
		tf.setColumns(7);
	}

	/**
	 * Constructor.
	 */
	public SpinnerInteger(int defValue, int minValue, int maxValue, int incValue, String format) {
		super();
		this.defValue = defValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.incValue = incValue;

		Integer def = new Integer(defValue);
		Integer min = new Integer(minValue);
		Integer max = new Integer(maxValue);
		Integer inc = new Integer(incValue);
		model = new SpinnerNumberModel(def, min, max, inc);
		setModel(model);
		setEditor(new JSpinner.NumberEditor(this, format));
		JFormattedTextField tf = ((JSpinner.DefaultEditor) getEditor()).getTextField();
		tf.setColumns(7);
	}

	/**
	 * Constructor.
	 */
	public SpinnerInteger(int defValue, int minValue, int maxValue, int incValue, int visibleChars) {
		super();
		this.defValue = defValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.incValue = incValue;

		Integer def = new Integer(defValue);
		Integer min = new Integer(minValue);
		Integer max = new Integer(maxValue);
		Integer inc = new Integer(incValue);
		model = new SpinnerNumberModel(def, min, max, inc);
		setModel(model);
		JFormattedTextField tf = ((JSpinner.DefaultEditor) getEditor()).getTextField();
		tf.setColumns(visibleChars);
	}

	/**
	 * Set the format of the numerical value.
	 */
	public void setFormat(String format) {
		setEditor(new JSpinner.NumberEditor(this, format));
	}

	/**
	 * Set the minimal and the maximal limit.
	 */
	public void setLimit(int minValue, int maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		int value = get();
		Integer min = new Integer(minValue);
		Integer max = new Integer(maxValue);
		Integer inc = new Integer(incValue);
		defValue = (value > maxValue ? maxValue : (value < minValue ? minValue : value));
		Integer def = new Integer(defValue);
		model = new SpinnerNumberModel(def, min, max, inc);
		setModel(model);
	}

	/**
	 * Set the incremental step.
	 */
	public void setIncrement(int incValue) {
		this.incValue = incValue;
		Integer def = (Integer) getModel().getValue();
		Integer min = new Integer(minValue);
		Integer max = new Integer(maxValue);
		Integer inc = new Integer(incValue);
		model = new SpinnerNumberModel(def, min, max, inc);
		setModel(model);
	}

	/**
	 * Returns the incremental step.
	 */
	public int getIncrement() {
		return incValue;
	}

	/**
	 * Set the value in the JSpinner with clipping in the range [min..max].
	 */
	public void set(int value) {
		value = (value > maxValue ? maxValue : (value < minValue ? minValue : value));
		model.setValue(value);
	}

	/**
	 * Return the value without clipping the value in the range [min..max].
	 */
	public int get() {
		if (model.getValue() instanceof Integer) {
			Integer i = (Integer) model.getValue();
			int ii = i.intValue();
			return (ii > maxValue ? maxValue : (ii < minValue ? minValue : ii));
		}
		else if (model.getValue() instanceof Double) {
			Double i = (Double) model.getValue();
			int ii = (int) i.doubleValue();
			return (ii > maxValue ? maxValue : (ii < minValue ? minValue : ii));
		}
		else if (model.getValue() instanceof Float) {
			Float i = (Float) model.getValue();
			int ii = (int) i.floatValue();
			return (ii > maxValue ? maxValue : (ii < minValue ? minValue : ii));
		}
		return 0;
	}
}

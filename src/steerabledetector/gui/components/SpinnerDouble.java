package steerabledetector.gui.components;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class SpinnerDouble extends JSpinner {

	private SpinnerNumberModel	model;

	private double				defValue;
	private double				minValue;
	private double				maxValue;
	private double				incValue;

	/**
	 * Constructor.
	 */
	public SpinnerDouble(double defValue, double minValue, double maxValue, double incValue) {
		super();
		this.defValue = defValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.incValue = incValue;
		Double def = new Double(defValue);
		Double min = new Double(minValue);
		Double max = new Double(maxValue);
		Double inc = new Double(incValue);
		model = new SpinnerNumberModel(def, min, max, inc);
		setModel(model);
		JFormattedTextField tf = ((JSpinner.DefaultEditor) getEditor()).getTextField();
		tf.setColumns(7);
	}

	/**
	 * Constructor.
	 */
	public SpinnerDouble(double defValue, double minValue, double maxValue, double incValue, String format) {
		super();
		this.defValue = defValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.incValue = incValue;

		Double def = new Double(defValue);
		Double min = new Double(minValue);
		Double max = new Double(maxValue);
		Double inc = new Double(incValue);
		model = new SpinnerNumberModel(def, min, max, inc);
		setModel(model);
		setEditor(new JSpinner.NumberEditor(this, format));
		JFormattedTextField tf = ((JSpinner.DefaultEditor) getEditor()).getTextField();
		tf.setColumns(7);
	}

	/**
	 * Constructor.
	 */
	public SpinnerDouble(double defValue, double minValue, double maxValue, double incValue, int visibleChars) {
		super();
		this.defValue = defValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.incValue = incValue;

		Double def = new Double(defValue);
		Double min = new Double(minValue);
		Double max = new Double(maxValue);
		Double inc = new Double(incValue);
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
	public void setLimit(double minValue, double maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		double value = get();
		Double min = new Double(minValue);
		Double max = new Double(maxValue);
		Double inc = new Double(incValue);
		defValue = (value > maxValue ? maxValue : (value < minValue ? minValue : value));
		Double def = new Double(defValue);
		model = new SpinnerNumberModel(def, min, max, inc);
		setModel(model);
	}

	/**
	 * Set the incremental step.
	 */
	public void setIncrement(double incValue) {
		this.incValue = incValue;
		Double def = (Double) getModel().getValue();
		Double min = new Double(minValue);
		Double max = new Double(maxValue);
		Double inc = new Double(incValue);
		model = new SpinnerNumberModel(def, min, max, inc);
		setModel(model);
	}

	/**
	 * Returns the incremental step.
	 */
	public double getIncrement() {
		return incValue;
	}

	/**
	 * Set the value in the JSpinner with clipping in the range [min..max].
	 */
	public void set(double value) {
		value = (value > maxValue ? maxValue : (value < minValue ? minValue : value));
		model.setValue(value);
	}

	/**
	 * Return the value with clipping the value in the range [min..max].
	 */
	public double get() {
		if (model.getValue() instanceof Integer) {
			Integer i = (Integer) model.getValue();
			double ii = i.intValue();
			return (ii > maxValue ? maxValue : (ii < minValue ? minValue : ii));
		}
		else if (model.getValue() instanceof Double) {
			Double i = (Double) model.getValue();
			double ii = i.doubleValue();
			return (ii > maxValue ? maxValue : (ii < minValue ? minValue : ii));
		}
		else if (model.getValue() instanceof Float) {
			Float i = (Float) model.getValue();
			double ii = i.floatValue();
			return (ii > maxValue ? maxValue : (ii < minValue ? minValue : ii));
		}
		return 0.0;
	}
}

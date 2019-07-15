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

package steerabledetector.gui.settings;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import steerabledetector.gui.components.SpinnerDouble;
import steerabledetector.gui.components.SpinnerInteger;

/**
 * This class allows to store and load key-associated values in a text file. The
 * class has methods to load and store single value linked to a string key
 * describing the value. Furthermore, this class has methods to record a GUI
 * component to a specified key. By this way this class allows to load and store
 * all recorded items.
 * 
 * @author Daniel Sage, Biomedical Imaging Group, EPFL, Lausanne, Switzerland.
 * 
 */
public class Settings {

	private String			filename;
	private String			project;
	private ArrayList<Item>	items;
	private Properties		props;

	/**
	 * Constructors a Settings abject for a given project name and a given
	 * filename.
	 * 
	 * @param project
	 *            a string describing the project
	 * @param filename
	 *            a string give the full name of the file, including the path
	 */
	public Settings(String project, String filename) {
		this.filename = filename;
		this.project = project;
		items = new ArrayList<Item>();
		props = new Properties();
	}

	public String getProject() {
		return project;
	}

	public String getFilename() {
		return filename;
	}

	/**
	 * Records a JTextField component to store/load automatically.
	 * 
	 * @param key
	 *            a string describing the value
	 * @param component
	 *            the component to record
	 * @param defaultValue
	 *            the default value
	 */
	public void record(String key, JTextField component, String defaultValue) {
		Item item = new Item(key, component, defaultValue);
		items.add(item);
	}

	/**
	 * Records a JComboBox component to store/load automatically.
	 * 
	 * @param key
	 *            a string describing the value
	 * @param component
	 *            the component to record
	 * @param defaultValue
	 *            the default value
	 */
	public void record(String key, JComboBox component, String defaultValue) {
		Item item = new Item(key, component, defaultValue);
		items.add(item);
	}

	/**
	 * Records a JSpinner component to store/load automatically.
	 * 
	 * @param key
	 *            a string describing the value
	 * @param component
	 *            the component to record
	 * @param defaultValue
	 *            the default value
	 */
	public void record(String key, JSpinner component, String defaultValue) {
		Item item = new Item(key, component, defaultValue);
		items.add(item);
	}

	/**
	 * Records a JToggleButton component to store/load automatically.
	 * 
	 * @param key
	 *            a string describing the value
	 * @param component
	 *            the component to record
	 * @param defaultValue
	 *            the default value
	 */
	public void record(String key, JToggleButton component, boolean defaultValue) {
		Item item = new Item(key, component, (defaultValue ? "on" : "off"));
		items.add(item);
	}

	/**
	 * Records a JCheckBox component to store/load automatically.
	 * 
	 * @param key
	 *            a string describing the value
	 * @param component
	 *            the component to record
	 * @param defaultValue
	 *            the default value
	 */
	public void record(String key, JCheckBox component, boolean defaultValue) {
		Item item = new Item(key, component, (defaultValue ? "on" : "off"));
		items.add(item);
	}

	/**
	 * Records a JSlider component to store/load automatically.
	 * 
	 * @param key
	 *            a int value
	 * @param component
	 *            the component to record
	 * @param defaultValue
	 *            the default value
	 */
	public void record(String key, JSlider component, String defaultValue) {
		Item item = new Item(key, component, defaultValue);
		items.add(item);
	}

	/**
	 * Load an individual double value given a specified key
	 * 
	 * @param key
	 *            a string describing the value
	 * @param defaultValue
	 *            the default value
	 * @return the value get from the file
	 */
	public String loadValue(String key, String defaultValue) {
		String s = "";
		try {
			FileInputStream in = new FileInputStream(filename);
			props.load(in);
			s = props.getProperty(key, "" + defaultValue);
		}
		catch (Exception e) {
			s = defaultValue;
		}
		return s;
	}

	/**
	 * Load an individual double value given a specified key
	 * 
	 * @param key
	 *            a string describing the value
	 * @param defaultValue
	 *            the default value
	 * @return the value get from the file
	 */
	public double loadValue(String key, double defaultValue) {
		double d = 0;
		try {
			FileInputStream in = new FileInputStream(filename);
			props.load(in);
			String value = props.getProperty(key, "" + defaultValue);
			d = (new Double(value)).doubleValue();
		}
		catch (Exception e) {
			d = defaultValue;
		}
		return d;
	}

	/**
	 * Load an individual integer value given a specified key
	 * 
	 * @param key
	 *            a string describing the value
	 * @param defaultValue
	 *            the default value
	 * @return the value get from the file
	 */
	public int loadValue(String key, int defaultValue) {
		int i = 0;
		try {
			FileInputStream in = new FileInputStream(filename);
			props.load(in);
			String value = props.getProperty(key, "" + defaultValue);
			i = (new Integer(value)).intValue();
		}
		catch (Exception e) {
			i = defaultValue;
		}
		return i;
	}

	/**
	 * Load an individual boolean value given a specified key
	 * 
	 * @param key
	 *            a string describing the value
	 * @param defaultValue
	 *            the default value
	 * @return the value get from the file
	 */
	public boolean loadValue(String key, boolean defaultValue) {
		boolean b = false;
		try {
			FileInputStream in = new FileInputStream(filename);
			props.load(in);
			String value = props.getProperty(key, "" + defaultValue);
			b = (new Boolean(value)).booleanValue();
		}
		catch (Exception e) {
			b = defaultValue;
		}
		return b;
	}

	/**
	 * Store an individual double value given a specified key
	 * 
	 * @param key
	 *            a string describing the value
	 * @param value
	 *            the value to store
	 */
	public void storeValue(String key, String value) {
		props.setProperty(key, value);
		try {
			FileOutputStream out = new FileOutputStream(filename);
			props.store(out, project);
		}
		catch (Exception e) {
			System.out.println(project + ": Impossible to store settings in (" + filename + ")");
		}
	}

	/**
	 * Store an individual double value given a specified key
	 * 
	 * @param key
	 *            a string describing the value
	 * @param value
	 *            the value to store
	 */
	public void storeValue(String key, double value) {
		props.setProperty(key, "" + value);
		try {
			FileOutputStream out = new FileOutputStream(filename);
			props.store(out, project);
		}
		catch (Exception e) {
			System.out.println(project + ": Impossible to store settings in (" + filename + ")");
		}
	}

	/**
	 * Store an individual integer value given a specified key
	 * 
	 * @param key
	 *            a string describing the value
	 * @param value
	 *            the value to store
	 */
	public void storeValue(String key, int value) {
		props.setProperty(key, "" + value);
		try {
			FileOutputStream out = new FileOutputStream(filename);
			props.store(out, project);
		}
		catch (Exception e) {
			System.out.println(project + ": Impossible to store settings in (" + filename + ")");
		}
	}

	/**
	 * Store an individual boolean value given a specified key
	 * 
	 * @param key
	 *            a string describing the value
	 * @param value
	 *            the value to store
	 */
	public void storeValue(String key, boolean value) {
		props.setProperty(key, "" + value);
		try {
			FileOutputStream out = new FileOutputStream(filename);
			props.store(out, project);
		}
		catch (Exception e) {
			System.out.println(project + ": Impossible to store settings in (" + filename + ")");
		}
	}

	/**
	 * Load all recorded values.
	 */
	public void loadRecordedItems() {
		loadRecordedItems(filename);
	}

	/**
	 * Load all recorded values from a specified filename.
	 */
	public void loadRecordedItems(String fname) {
		try {
			FileInputStream in = new FileInputStream(fname);
			props.load(in);
		}
		catch (Exception e) {
			props = new Properties();
		}

		for (int i = 0; i < items.size(); i++) {
			Item item = (Item) items.get(i);
			String value = props.getProperty(item.key, item.defaultValue);
			if (item.component instanceof JTextField) {
				((JTextField) item.component).setText(value);
			}
			else if (item.component instanceof JComboBox) {
				((JComboBox) item.component).setSelectedItem(value);
			}
			else if (item.component instanceof JCheckBox) {
				((JCheckBox) item.component).setSelected(value.equals("on") ? true : false);
			}
			else if (item.component instanceof JToggleButton) {
				((JToggleButton) item.component).setSelected(value.equals("on") ? true : false);
			}
			else if (item.component instanceof SpinnerInteger) {
				((SpinnerInteger) item.component).set(Math.round((new Double(value)).intValue()));
			}
			else if (item.component instanceof SpinnerDouble) {
				((SpinnerDouble) item.component).set((new Double(value)).doubleValue());
			}
			else if (item.component instanceof JSlider) {
				((JSlider) item.component).setValue((new Integer(value)).intValue());
			}
		}
	}

	/**
	 * Store all recorded values.
	 */
	public void storeRecordedItems() {
		storeRecordedItems(filename);
	}

	/**
	 * Store all recorded values into a specified filename
	 */
	public void storeRecordedItems(String fname) {

		for (int i = 0; i < items.size(); i++) {
			Item item = (Item) items.get(i);
			if (item.component instanceof JTextField) {
				String value = ((JTextField) item.component).getText();
				props.setProperty(item.key, value);
			}
			else if (item.component instanceof JComboBox) {
				String value = (String) ((JComboBox) item.component).getSelectedItem();
				props.setProperty(item.key, value);
			}
			else if (item.component instanceof JCheckBox) {
				String value = (((JCheckBox) item.component).isSelected() ? "on" : "off");
				props.setProperty(item.key, value);
			}
			else if (item.component instanceof JToggleButton) {
				String value = (((JToggleButton) item.component).isSelected() ? "on" : "off");
				props.setProperty(item.key, value);
			}
			else if (item.component instanceof JSpinner) {
				String value = "" + ((JSpinner) item.component).getValue();
				props.setProperty(item.key, value);
			}
			else if (item.component instanceof JSlider) {
				String value = "" + ((JSlider) item.component).getValue();
				props.setProperty(item.key, value);
			}
		}

		try {
			FileOutputStream out = new FileOutputStream(fname);
			props.store(out, project);
		}
		catch (Exception e) {
			System.out.println(project + ": Impossible to store settings in (" + fname + ")");
		}
	}

	/**
	 * Private class to store one component and its key.
	 */
	private class Item {
		public Object	component;
		public String	defaultValue;
		public String	key;

		public Item(String key, Object component, String defaultValue) {
			this.component = component;
			this.defaultValue = defaultValue;
			this.key = key;
		}
	}

}

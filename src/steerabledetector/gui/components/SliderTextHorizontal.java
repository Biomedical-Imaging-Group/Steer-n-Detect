package steerabledetector.gui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SliderTextHorizontal extends JPanel implements ChangeListener {

	private JSlider	slider	= new JSlider();
	private JLabel	lbl		= new JLabel("0.5");
	
	public SliderTextHorizontal() {
		setLayout(new BorderLayout());
		slider.setMinimum(0);
		slider.setMaximum(100);
		slider.setPreferredSize(new Dimension(200, 40));
		lbl.setPreferredSize(new Dimension(40, 40));
		slider.setValue(50);

		Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		JLabel lbl0 = new JLabel("Off");
		JLabel lbl1 = new JLabel("Low");
		JLabel lbl2 = new JLabel("Medium");
		JLabel lbl3 = new JLabel("High");
		JLabel lbl4 = new JLabel("Max.");
		java.awt.Font font = lbl1.getFont();
		java.awt.Font small = new java.awt.Font(font.getFamily(), font.getStyle(), font.getSize() - 3);
		lbl0.setFont(small);
		lbl1.setFont(small);
		lbl2.setFont(small);
		lbl3.setFont(small);
		lbl4.setFont(small);
		labels.put(0, lbl0);
		labels.put(25, lbl1);
		labels.put(50, lbl2);
		labels.put(75, lbl3);
		labels.put(100, lbl4);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setMajorTickSpacing(25);
		slider.setLabelTable(labels);
		slider.addChangeListener(this);
		add(new JLabel("Shaping"), BorderLayout.WEST);
		add(slider, BorderLayout.CENTER);
		add(lbl, BorderLayout.EAST);
	}
	
	public void setValue(double value) {
		slider.setValue((int)(value*100));
	}
	
	public double getValue(double valu) {
		return slider.getValue()*0.01;
	}
	
	public JSlider getSlider() {
		return slider;
	}
	
	public void setEnabled(boolean b) {
		Component cs[] = getComponents();
		for (Component c : cs)
			c.setEnabled(b);		
	}
	
	public void updateFromSlider() {
		int d = slider.getValue();
		lbl.setText(String.format("%1.2f", d*0.01));
	}

	public double getValue() {
		return slider.getValue() * 0.01;	
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updateFromSlider();
	}
}

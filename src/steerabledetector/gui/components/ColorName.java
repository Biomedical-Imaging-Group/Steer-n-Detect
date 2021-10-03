package steerabledetector.gui.components;

import java.awt.Color;

import javax.swing.JComboBox;

import steerabledetector.detector.Detection;

public class ColorName {

	public static String[]	names	= { "Conf-coded", "Angle-coded", "Maroon", "Red", "Purple", "Fuchsia",
			"Green", "Lime", "Olive", "Yellow", "Navy", "Blue", "Teal", "Aqua", "Black", "Silver", "Gray", "White" };

	public static Color[]	colors	= { new Color(0x80, 0x00, 0x00), 
			new Color(0x80, 0x00, 0x00), new Color(0x80, 0x00, 0x00), new Color(0xFF, 0x00, 0x00),
			new Color(0x80, 0x00, 0x80), new Color(0xFF, 0x00, 0xFF), new Color(0x00, 0x80, 0x00),
			new Color(0x00, 0xFF, 0x00), new Color(0x80, 0x80, 0x00), new Color(0xFF, 0xFF, 0x00),
			new Color(0x00, 0x00, 0x80), new Color(0x00, 0x00, 0xFF), new Color(0x00, 0x80, 0x80),
			new Color(0x00, 0xFF, 0xFF), new Color(0x00, 0x00, 0x00), new Color(0xC0, 0xC0, 0xC0),
			new Color(0x80, 0x80, 0x80), new Color(0xFF, 0xFF, 0xFF), new Color(0xFF, 0xF0, 0xFF) };

	public static String getName(Color c) {
		for (int i = 0; i < names.length; i++) {
			if (c.getRed() == colors[i].getRed())
				if (c.getGreen() == colors[i].getGreen())
					if (c.getBlue() == colors[i].getBlue())
						return names[i];
		}
		return "unknown";
	}

	public static Color getColor(String name, Detection detection) {
		if (name.equals(names[0])) {
			float h = (float)(detection.amplitude);
			h = Math.max(0, Math.min(1, h));
			return Color.getHSBColor(h, 1f, 1f);
		}
		if (name.equals(names[1])) {
			float h = (float)(detection.angle/360.0f);
			h = Math.max(0, Math.min(1, h));
			return Color.getHSBColor(h, 1f, 1f);
		}
		 
		for (int i = 0; i < names.length; i++)
			if (names[i].equals(name))
				return colors[i];
		return colors[0];
	}

	public static JComboBox<String> createComboBox() {
		JComboBox<String> cmb = new JComboBox<String>();
		for (String name : names)
			cmb.addItem(name);
		return cmb;
	}

	public static Color inverse(Color c) {
		return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
	}

	public static Color inverse(Color c, int opacity) {
		return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), opacity);
	}

	public static Color opacify(Color c, int opacity) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), opacity);
	}
}

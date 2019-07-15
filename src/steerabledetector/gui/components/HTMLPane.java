package steerabledetector.gui.components;

import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * This class extends the Java JEditorPane to make a easy to use panel to
 * display HTML information.
 * 
 * @author Daniel Sage, Biomedical Imaging Group, EPFL, Lausanne, Switzerland.
 * 
 */
public class HTMLPane extends JEditorPane {

	private String		html		= "";
	private String		header		= "";
	private String		footer		= "";
	private Dimension	dim;
	private String		font		= "verdana";
	private String		color		= "#111111";
	private String		background	= "#fCfCfC";

	public HTMLPane(String font) {
		this.font = font;
		create();
	}

	public HTMLPane(int width, int height) {
		this.dim = new Dimension(width, height);
		create();
	}

	public HTMLPane(String font, int width, int height) {
		this.font = font;
		this.dim = new Dimension(width, height);
		create();
	}

	public HTMLPane(String font, String color, String background, int width, int height) {
		this.font = font;
		this.dim = new Dimension(width, height);
		this.color = color;
		this.background = background;
		create();
	}

	@Override
	public String getText() {
		Document doc = this.getDocument();
		try {
			return doc.getText(0, doc.getLength());
		}
		catch (BadLocationException e) {
			e.printStackTrace();
			return getText();
		}
	}
	
	public void clear() {
		html = "";
		append("");
	}

	private void create() {
		header += "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">\n";
		header += "<html><head>\n";
		header += "<style>body {background-color:" + background + "; color:" + color + "; font-family: " + font + ";margin:4px}</style>\n";
		header += "<style>h1 {color:#333366; font-size:1.1em; font-weight:bold; padding:1px; margin:1px;}</style>\n";
		header += "<style>h2 {color:#333333; font-size:0.9em; font-weight:bold; padding:1px; margin:1px;}</style>\n";
		header += "<style>h3 {color:#000000; font-size:0.9em; font-weight:italic; padding:1px; margin:1px;}</style>\n";
		header += "<style>p  {color:" + color + "; font-size:0.9em; padding:1px; margin:0px;}</style>\n";
		header += "</head>\n";
		header += "<body>\n";
		footer += "</body></html>\n";
		setEditable(false);
		setContentType("text/html; charset=ISO-8859-1");
	}

	public void append(String content) {
		html += content;
		setText(header + html + footer);
		if (dim != null) {
			setPreferredSize(dim);
		}
		setCaretPosition(0);
	}

	public void append(String tag, String content) {
		html += "<" + tag + ">" + content + "</" + tag + ">";
		setText(header + html + footer);
		if (dim != null) {
			setPreferredSize(dim);
		}
		setCaretPosition(0);
	}

	public JScrollPane getPane() {
		JScrollPane scroll = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(dim);
		return scroll;
	}
}

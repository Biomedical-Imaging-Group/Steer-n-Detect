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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import steerabledetector.detector.Detection;
import steerabledetector.detector.SteerableDetector;
import steerabledetector.gui.components.ColorName;
import steerabledetector.gui.components.HTMLPane;
import steerabledetector.image2d.Image2DDouble;

public class CanvasSelection extends ImageCanvas implements Runnable, KeyListener {

	private ImageCanvas			canvasOriginal;
	private SteerableDetector	detector;
	private Data					data;
	private boolean[]			shows			= new boolean[] { true, false, false, false };
	private String[]				colors			= new String[] { "Conf-coded", "Conf-coded", "Red", "Red"};
	private int					stroke			= 2;
	private int					opacity			= 50;
	private String				label			= "None";
	private Image2DDouble		input;
	private int					selectedID		= -1;
	
	// For the double buffering
	private Dimension		dim;
	private Image			offscreen;
	private Graphics2D		bufferGraphics;

	private int				xmouse, ymouse;
	private Thread			thread				= null;
	private DialogSelection	dialog;

	private Cursor			cursor;
	private Cursor			hand;

	private boolean			isAlreadyOneClick	= false;
	private HTMLPane			info;
	private int				optimalCenter;
	private int				optimalRadius;
	private boolean			automatic			= true;
	private boolean			manual				= true;
		
	public CanvasSelection(ImagePlus imp, SteerableDetector detector, Data data, DialogSelection dialog, HTMLPane info) {
		super(imp);
		input = new Image2DDouble(imp);
		this.detector = detector;
		this.data = data;
		this.dialog = dialog;
	
		if (imp.getStackSize() > 1)
			imp.setWindow(new StackWindow(imp, this));
		else
			imp.setWindow(new ImageWindow(imp, this));
		resetBuffer();
		cursor = this.getCursor();
		hand = new Cursor(Cursor.HAND_CURSOR);

		ImageWindow window = imp.getWindow();
		canvasOriginal = imp.getCanvas();
		window.removeKeyListener(IJ.getInstance());
		canvasOriginal.removeKeyListener(IJ.getInstance());
		canvasOriginal.addKeyListener(this);
		window.addKeyListener(this);
		this.info = info;
	}

	public void setOptimize(int optimalCenter, int optimalRadius) {
		this.optimalCenter = optimalCenter;
		this.optimalRadius = optimalRadius;
	}

	public void setFlag(boolean automatic, boolean manual) {
		this.automatic = automatic;
		this.manual = manual;
		repaint();
	}

	public void setSelected(int selectedID) {
		this.selectedID = selectedID;
		repaint();
	}
	
	
	@Override
	public void paint(Graphics g) {

		if (shows[0] == false && shows[1] == false && shows[2] == false && shows[3] == false) {
			super.paint(g);
			return;
		}
		if (dim == null)
			dim = getSize();
		
		if (dim.width != getSize().width || dim.height != getSize().height || bufferGraphics == null || offscreen == null)
			resetBuffer();

		super.paint(bufferGraphics);
	
		double mag = this.getMagnification();
		int o = (int) (2.55 * opacity);
		
		ArrayList<Detection> detections = data.getSelectedAndManual(automatic, manual);

		for (Detection detection : detections) {
			Color outer = ColorName.getColor(colors[0], detection);
			Color inner = ColorName.opacify(ColorName.getColor(colors[1], detection), o);
			if (detection.id != selectedID)
				drawDetection(bufferGraphics, detection, outer, inner, mag);
			else
				drawDetection(bufferGraphics, detection, ColorName.inverse(outer), ColorName.inverse(inner, o), mag);
		}
		
		g.drawImage(offscreen, 0, 0, this);
	}

	private void remove() {
		selectedID = data.findDetectionID(xmouse, ymouse, automatic, manual);
		if (selectedID >= 0) {
			data.remove(selectedID);
			if (dialog != null)
				dialog.update(-1);
			if (info != null)
				info.append("p", "Remove spot " + selectedID);
		}
		repaint();
	}

	private void add(int xmouse, int ymouse) {
		Detection detection = detector.getDetection(xmouse, ymouse);
		if (detection != null) {
			data.add(detection);
			if (dialog != null)
				dialog.update(detection.id);
		}
		repaint();
		if (info != null)
			info.append("p", "Add spot " + selectedID + " at (" + xmouse + ", " + ymouse + ")");
	}

	public void setDisplay(boolean shows[], String colors[], int stroke, int opacity, String label) {
		this.shows = shows;
		this.colors = colors;
		this.stroke = stroke;
		this.opacity = opacity;
		this.label = label;
		repaint();
	}

	public void resetBuffer() {
		if (bufferGraphics != null) {
			bufferGraphics.dispose();
			bufferGraphics = null;
		}
		if (offscreen != null) {
			offscreen.flush();
			offscreen = null;
		}
		dim = getSize();
		offscreen = createImage(dim.width, dim.height);
		bufferGraphics = (Graphics2D) offscreen.getGraphics();
	}

	public void drawDetection(Graphics2D g2, Detection detection, Color outer, Color inner, double mag) {
		
		double radius = detection.size*0.5/Math.sqrt(2);
		double arad = Math.toRadians(detection.angle);
		int xd = screenXD(detection.x);
		int yd = screenYD(detection.y);
		int xe = screenXD(detection.x + radius*Math.cos(arad));
		int ye = screenYD(detection.y + radius*Math.sin(arad));
		double r = Math.sqrt((xd-xe)*(xd-xe)+(yd-ye)*(yd-ye));
		
		if (shows[0]) {
			g2.setColor(outer);
			g2.setStroke(new BasicStroke(stroke));
			//g2.drawPolygon(xPoints, yPoints, 4);
			Shape shape = new Ellipse2D.Double(xd - r, yd - r, 2 * r, 2 * r);
			//g2.fillPolygon(xPoints, yPoints, 4);
			g2.draw(shape);
			g2.drawLine(xd, yd, xe, ye);
		}
		
		if (shows[1]) {
			g2.setColor(inner);
			Shape shape = new Ellipse2D.Double(xd - r, yd - r, 2 * r, 2 * r);
			//g2.fillPolygon(xPoints, yPoints, 4);
			g2.fill(shape);
			g2.drawLine(xd, yd, xe, ye);
		}
		
		if (shows[2]) {
			Color c = ColorName.getColor(colors[2], detection);
			if (label.equals("ID"))
				print(g2, xd, yd, "" + detection.id, c);
			else if (label.equals("Confidence"))
				print(g2, xd, yd, String.format("%2.2f", detection.amplitude), c);
			else if (label.equals("Angle"))
				print(g2, xd, yd, String.format("%2.2f", detection.angle), c);
		}
		
		if (shows[3]) {
			Color c = ColorName.getColor(colors[3], detection);
			g2.setColor(c);
			g2.setStroke(new BasicStroke(1));
			double a1 = arad;
			double a2 = a1 + Math.PI/2;
			double a3 = a2 + Math.PI/2;
			double a4 = a3 + Math.PI/2;
			int x1 = (int)Math.round(screenXD(detection.x) + r*Math.cos(a1));
			int y1 = (int)Math.round(screenYD(detection.y) + r*Math.sin(a1));
			int x2 = (int)Math.round(screenXD(detection.x) + r*Math.cos(a3));
			int y2 = (int)Math.round(screenYD(detection.y) + r*Math.sin(a3));
			g2.drawLine(x1, y1, x2, y2);
			int x3 = (int)Math.round(screenXD(detection.x) + r*Math.cos(a2));
			int y3 = (int)Math.round(screenYD(detection.y) + r*Math.sin(a2));
			int x4 = (int)Math.round(screenXD(detection.x) + r*Math.cos(a4));
			int y4 = (int)Math.round(screenYD(detection.y) + r*Math.sin(a4));
			g2.drawLine(x3, y3, x4, y4);
		}
	}

	public void print(Graphics g2, int x, int y, String message, Color fore) {
		double distWhite = Math.abs(255 - fore.getGreen()) + Math.abs(255 - fore.getRed()) + Math.abs(255 - fore.getBlue());
		double distBlack = fore.getGreen() + fore.getRed() + fore.getBlue();
		g2.setColor(distWhite < distBlack ? Color.WHITE : Color.BLACK);
		g2.drawString(message, x + 1, y + 1);
		g2.setColor(fore);
		g2.drawString(message, x, y);
	}

	@Override
	public void run() {
		//cmbBestCenter	= new JComboBox(new String[] { "Fixed position", "Best center in 3x3", "Best center in 5x5", "7x7", "9x9", "Best center in 11x11", "Best center in 13x13" });
		//cmbBestRadius	= new JComboBox(new String[] { "Optimal radius", "Fixed radius" });
		int n = optimalCenter; 
		double max = -Double.MAX_VALUE;
		int imax = xmouse;
		int jmax = ymouse;
		if (n>0)
		for (int i = xmouse - n; i <= xmouse + n; i++)
			for (int j = ymouse - n; j <= ymouse + n; j++) {
				Detection detectionTest = detector.getDetection(selectedID, i, j);
				if (detectionTest != null)
					if (detectionTest.amplitude > max) {
						max = detectionTest.amplitude;
						imax = i;
						jmax = j;
					}
			}
		Detection detection = data.findDetection(selectedID);
		if (detection != null) {	
			if (optimalRadius == 0) {
				Detection detNew = detector.getDetection(selectedID, imax, jmax);
				if (detNew != null)
					data.move(selectedID, detNew.x, detNew.y, detNew.angle, detNew.amplitude, input);
			}
			else {
				data.move(selectedID, imax, jmax, detection.angle, detection.amplitude, input);
			}
			if (dialog != null)
				dialog.updateTable(selectedID);
			if (dialog != null)
				dialog.updateDetection(detection);
		}	
		if (dialog != null)
			dialog.updateCount();
		repaint();
		thread = null;
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		setCursor(selectedID >= 0 ? hand : cursor);
		super.mouseMoved(event);
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		selectedID = -1;
		super.mouseReleased(event);
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		if (selectedID >= 0) {
			xmouse = offScreenX(event.getX());
			ymouse = offScreenY(event.getY());
			if (thread == null) {
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		else {
			super.mouseDragged(event);
		}
	}

	@Override
	public void mousePressed(final MouseEvent event) {
		if (isAlreadyOneClick) {
			mousePressedDoubleClick(event);
			isAlreadyOneClick = false;
			return;
		}
		// Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval").intValue();
		int time = 200; // ms
		isAlreadyOneClick = true;
		Timer t = new Timer("doubleclickTimer", false);
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				if (isAlreadyOneClick)
					mousePressedSimpleClick(event);
				isAlreadyOneClick = false;
			}
		}, time);
	}

	private void mousePressedSimpleClick(MouseEvent event) {
		xmouse = offScreenX(event.getX());
		ymouse = offScreenY(event.getY());
		selectedID = data.findDetectionID(xmouse, ymouse, automatic, manual);
		if (selectedID >= 0) {
			dialog.updateCanvas(selectedID);
			return;
		}
		super.mousePressed(event);
	}

	private void mousePressedDoubleClick(MouseEvent event) {
		xmouse = offScreenX(event.getX());
		ymouse = offScreenY(event.getY());
		selectedID = data.findDetectionID(xmouse, ymouse, automatic, manual);
		if (selectedID < 0)
			add(xmouse, ymouse);
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE || event.getKeyCode() == KeyEvent.VK_DELETE)
			remove();
	}

	@Override
	public void keyReleased(KeyEvent event) {
	}

	@Override
	public void keyTyped(KeyEvent event) {
	}

}

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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.StackWindow;
public class CanvasTemplate extends ImageCanvas {

	private ImageCanvas		canvasOriginal;
	private int				selectedID			= -1;

	private int				xmouse, ymouse;

	private Cursor			cursor;
	private Cursor			hand;

	private Point			center;
	private Point			control;

	private Color			orange = new Color(250, 100, 75);
	private Color			transp = new Color(250, 100, 75, 150);
	
	public CanvasTemplate(ImagePlus imp) {
		super(imp);

		if (imp.getStackSize() > 1)
			imp.setWindow(new StackWindow(imp, this));
		else
			imp.setWindow(new ImageWindow(imp, this));
		cursor = this.getCursor();
		hand = new Cursor(Cursor.HAND_CURSOR);

		ImageWindow window = imp.getWindow();
		canvasOriginal = imp.getCanvas();
		window.removeKeyListener(IJ.getInstance());
		canvasOriginal.removeKeyListener(IJ.getInstance());
		
		center = new Point(imp.getWidth() / 2, imp.getHeight() / 2);
		int radius = Math.max(20, Math.min(imp.getWidth() / 2, imp.getHeight() / 2) / 10);
		Roi roi = imp.getRoi();
		if(roi != null) {
			if (roi.getType() == Roi.OVAL || roi.getType() == Roi.RECTANGLE) {
				Rectangle rect = roi.getBounds();
				int w = rect.width;
				int h = rect.height;
				radius = (int)(0.25*(w+h));
				center = new Point(rect.x + w/2, rect.y + h/2);
			}
		}
		control = new Point(center.x, center.y + radius);
	}
	
	@Override
	public void paint(Graphics g) {

		super.paint(g);
		double mag = this.getMagnification();
		double size = 6 * mag;
		int xc = screenXD(center.x);
		int yc = screenYD(center.y);
		int xo = screenXD(control.x);
		int yo = screenYD(control.y);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int dx = xo - xc;
		int dy = yo - yc;
		double radius = Math.sqrt(dx * dx + dy * dy);
		double oradius = radius / Math.sqrt(2);
		Shape outer = new Ellipse2D.Double(xc - radius, yc - radius, 2 * radius, 2 * radius);
		Shape inner = new Ellipse2D.Double(xc - oradius, yc - oradius, 2 * oradius, 2 * oradius);
		Area circle = new Area(outer);
		circle.subtract(new Area(inner));

		//g2.setColor(transp);
		RadialGradientPaint rgp = new RadialGradientPaint(xo, yo, (int)radius, 
				new float[] {0.0f, 1.0f}, new Color[] {transp, new Color(255, 255, 255, 100)});
		g2.setPaint(rgp);
		g2.fill(circle);

		print(g2, 15, 15, "Select a ROI or open a image file as template", orange);
		
		GradientPaint gp = new GradientPaint(xo, yo, orange, xc, yc, transp);
		g2.setPaint(gp);
		g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.drawLine(xo, yo, xc, yc);
		
		g.setColor(orange);
		g2.setStroke(new BasicStroke(1f));
		double a = Math.atan2(yc-yo, xc-xo);
		
		for(int i=0; i<360; i+=15) {
			drawTick(g2, xc,yc, radius, oradius, a + Math.toRadians(i), i%90==0);
		}
	
		g.setColor(new Color(255, 255, 255, 100));
		g2.fill(new Ellipse2D.Double(xc - size, yc - size, 2 * size, 2 * size));
		g2.fill(new Ellipse2D.Double(xo - size, yo - size, 2 * size, 2 * size));
		
		g.setColor(Color.BLACK);
		g2.draw(new Ellipse2D.Double(xc - size, yc - size, 2 * size, 2 * size));
		g2.draw(new Ellipse2D.Double(xo - size, yo - size, 2 * size, 2 * size));

		g2.dispose();
	}

	private void drawTick(Graphics2D g2, double xc, double yc, double r1, double r2, double angle, boolean major) {
		double x1 = xc + (r2 + (r1-r2) * (major ? 1 :0.75)) * Math.cos(angle);
		double y1 = yc + (r2 + (r1-r2) * (major ? 1 :0.75)) * Math.sin(angle);
		double x2 = xc + r2 * Math.cos(angle);
		double y2 = yc + r2 * Math.sin(angle);
		GradientPaint gp = new GradientPaint((int)x1, (int)y1, Color.BLACK, (int)x2, (int)y2, Color.white);
		g2.setPaint(gp);
		g2.draw(new Line2D.Double(x1, y1, x2, y2));
	}
	
	public Point[] getHotPoints() {
		return new Point[] {center, control};
	}


	public void print(Graphics g2, int x, int y, String message, Color fore) {
		g2.setColor(Color.BLACK);
		g2.drawString(message, x + 1, y + 1);
		g2.setColor(fore);
		g2.drawString(message, x, y);
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		setCursor(selectedID >= 0 ? hand : cursor);
		if (selectedID == 2) {
			xmouse = offScreenX(event.getX());
			ymouse = offScreenY(event.getY());
			limit();
			repaint();
			return;
		}		
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
			if (selectedID == 1) {
				int dx = control.x - center.x;
				int dy = control.y - center.y;
				center.x = xmouse;
				center.y = ymouse;
				control.x = center.x + dx;
				control.y = center.y + dy;
			}
			if (selectedID == 2) {
				limit();
			}
			repaint();
			return;
		}
		super.mouseDragged(event);
	}

	@Override
	public void mousePressed(final MouseEvent event) {
		xmouse = offScreenX(event.getX());
		ymouse = offScreenY(event.getY());

		if (center.distance(xmouse, ymouse) < 10) {
			center.x = xmouse;
			center.y = ymouse;
			selectedID = 1;
			repaint();
			return;
		}
		if (control.distance(xmouse, ymouse) < 10) {
			control.x = xmouse;
			control.y = ymouse;
			selectedID = 2;
			repaint();
			return;
		}
		super.mousePressed(event);

	}
	
	private void limit() {
		double dx = xmouse - center.x;
		double dy = ymouse - center.y;
		double a = Math.atan2(dy, dx);
		double r = Math.min(200, Math.max(20, Math.sqrt(dx*dx+dy*dy)));
		control.x = (int)Math.round(center.x + r * Math.cos(a));
		control.y = (int)Math.round(center.y + r * Math.sin(a));	
	}

}

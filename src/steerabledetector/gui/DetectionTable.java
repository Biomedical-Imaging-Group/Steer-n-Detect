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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import steerabledetector.detector.Detection;


public class DetectionTable extends JTable {

	private Color			colorOddRow		= new Color(245, 245, 250);
	private Color			colorEvenRow	= new Color(232, 232, 237);

	private String[]		tooltip			= new String[] { "" + "Identifier of the detection", "Horizontal coordinate of the object (in pixels)", "Vertical coordinate of the object (in pixels)",
			"Angle of the object (in degree)", "Confidence level: strength of the wavelet coefficient", "Type of insertion" };
	private String[]		headers			= new String[] { "ID", "X", "Y", "Angle", "Confidence", "Type" };

	public DetectionTable(Data data) {
		super();
		DefaultTableModel tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int column) {
				if (column <= 2)
					return Integer.class;
				else if (column <= 6)
					return Double.class;
				return super.getColumnClass(column);
			}
		};

		setModel(tableModel);
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.setColumnIdentifiers(headers);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowSelectionAllowed(true);
		setAutoCreateRowSorter(true);

		for (int i = 0; i < headers.length; i++) {
			TableColumn tc = getColumnModel().getColumn(i);
			tc.setCellRenderer(new AlternatedRowRenderer());
		}
		update(data.getSelectedAndManual(true, true));

		JTableHeader header = getTableHeader();
		ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
		for (int c = 0; c < getColumnCount(); c++) {
			TableColumn col = getColumnModel().getColumn(c);
			tips.setToolTip(col, tooltip[c]);
		}
		header.addMouseMotionListener(tips);

	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component component = super.prepareRenderer(renderer, row, column);
		int rendererWidth = component.getPreferredSize().width;
		TableColumn tableColumn = getColumnModel().getColumn(column);
		if (column <= 2)
			tableColumn.setPreferredWidth(34);
		else if (column <= 5)
			tableColumn.setPreferredWidth(59);
		else
			tableColumn.setPreferredWidth(Math.max(rendererWidth + 1, tableColumn.getPreferredWidth()));
		return component;
	}

	public JScrollPane getPane(int width, int height) {
		JScrollPane scrollpane = new JScrollPane(this);
		scrollpane.setPreferredSize(new Dimension(width, height));
		return scrollpane;
	}

	public int getID(int row) {
		if (row < 0)
			return -1;
		if (row >= getRowCount())
			return -1;
		return (Integer)getValueAt(row, 0);
	}

	public int getRow(int id) {
		for (int row = 0; row < getRowCount(); row++) {
			if (((Integer)getValueAt(row, 0)) == id)
				return row;
		}
		return -1;
	}

	public void update(ArrayList<Detection> detections) {
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.getDataVector().removeAllElements();
		for (Detection detection : detections)
			addDetection(detection);
		repaint();
	}

	public void update(Detection detection) {
		DefaultTableModel model = (DefaultTableModel) getModel();
		int row = getRow(detection.id);	
		if (row < 0)
			return;

		model.setValueAt(detection.x, row, 1);
		model.setValueAt(detection.y, row, 2);
		model.setValueAt(round(detection.angle, 4), row, 3);
		model.setValueAt(round(detection.amplitude, 4), row, 4);
		model.setValueAt(detection.getType(), row, 5);
		repaint();
	}

	private double round(double num, int n) {
		if (num == 0.0)
			return 0.0;
		double d = Math.ceil(Math.log10(num < 0.0 ? -num : num));
		int power = n - (int) d;
		double a = Math.pow(10, power);
		long shifted = Math.round(num * a);
		return shifted / a;
	}

	private void addDetection(Detection detection) {
		Object[] tmp = new Object[] { detection.id, detection.x, detection.y, round(detection.angle, 4), round(detection.amplitude, 4), detection.getType() };
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.addRow(tmp);
	}

	public class AlternatedRowRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			if (!isSelected)
				c.setBackground(row % 2 == 0 ? colorEvenRow : colorOddRow);
			if (c instanceof JComponent)
				((JComponent) c).setToolTipText(tooltip[col]);
			return c;
		}
	}

	class ColumnHeaderToolTips extends MouseMotionAdapter {
		TableColumn						curCol;
		HashMap<TableColumn, String>	tips	= new HashMap<TableColumn, String>();

		public void setToolTip(TableColumn col, String tooltip) {
			if (tooltip == null)
				tips.remove(col);
			else
				tips.put(col, tooltip);
		}

		@Override
		public void mouseMoved(MouseEvent evt) {
			JTableHeader header = (JTableHeader) evt.getSource();
			JTable table = header.getTable();
			TableColumnModel colModel = table.getColumnModel();
			int vColIndex = colModel.getColumnIndexAtX(evt.getX());
			TableColumn col = null;
			if (vColIndex >= 0) {
				col = colModel.getColumn(vColIndex);
			}
			if (col != curCol) {
				header.setToolTipText((String) tips.get(col));
				curCol = col;
			}
		}
	}

}

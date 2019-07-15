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

package steerabledetector.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ij.ImagePlus;
import ij.gui.GUI;
import steerabledetector.Constants;
import steerabledetector.detector.Detection;
import steerabledetector.detector.Parameters;
import steerabledetector.detector.SteerableDetector;
import steerabledetector.gui.components.GridPanel;
import steerabledetector.gui.components.HTMLPane;
import steerabledetector.gui.settings.Settings;

public class DialogSelection extends JDialog implements ListSelectionListener, WindowListener, MouseListener, KeyListener, ActionListener {

	private Settings			settings		= new Settings(Constants.name, Constants.settings);
	private String			name			= "Untitled";
	private Data				data;
	private ImagePlus		imp;

	private JButton			bnFeature		= new JButton("Set limits ...");

	private JButton			bnClose			= new JButton("Close");
	private JButton			bnSave			= new JButton("Save");
	private JButton			bnHelp			= new JButton("Help");
	private JButton			bnMore			= new JButton("More");
	private JButton			bnLess			= new JButton("Less");

	private JButton			bnJournal		= new JButton("Journal");
	private JCheckBox		chkManual		= new JCheckBox("Manual", true);
	private JCheckBox		chkAutomatic	= new JCheckBox("Automatic", true);
	private JLabel			lblCountAuto	= new JLabel("Automatic");
	private JLabel			lblCountManual	= new JLabel("Manual");

	private JComboBox<String>		cmbBestCenter	= new JComboBox<String>(new String[] { "No centering", "Best center in 3x3", "Best center in 5x5", "Best center in 7x7", "Best center in 9x9", "Best center in 11x11", "Best center in 13x13" });
	private DetectionTable		table;
	private CanvasSelection	canvas;

	private JTabbedPane		tab				= new JTabbedPane();
	private DisplayPanel	pnDisplay;
	private HTMLPane		info;

	public DialogSelection(ImagePlus imp, SteerableDetector detector, Data data, Parameters params, HTMLPane info) {
		super(new JFrame(), Constants.name + " [" + imp.getTitle() + "]");
		this.data = data;
		this.imp = imp;
		this.name = imp.getTitle();
		this.info = info;

		table = new DetectionTable(data);
		canvas = new CanvasSelection(imp, detector, data, this, info);
		settings.record("cmbBestCenter", cmbBestCenter, "Best center in 5x5");
		settings.loadRecordedItems();

		lblCountAuto.setBorder(BorderFactory.createEtchedBorder());
		lblCountManual.setBorder(BorderFactory.createEtchedBorder());
		lblCountAuto.setPreferredSize(new Dimension(160, 20));
		lblCountManual.setPreferredSize(new Dimension(160, 20));
		GridPanel pnAuto = new GridPanel(true, 1);

		pnAuto.place(1, 0, 2, 1, lblCountAuto);
		pnAuto.place(2, 0, 1, 1, bnLess);
		pnAuto.place(2, 1, 1, 1, bnMore);
		pnAuto.place(3, 0, 2, 1, bnFeature);

		GridPanel pnManual = new GridPanel(true, 1);
		pnManual.place(1, 0, 1, 1, lblCountManual);
		pnManual.place(2, 0, 1, 1, cmbBestCenter);

		GridPanel pnSelection = new GridPanel(false, 1);
		pnSelection.place(0, 0, chkAutomatic);
		pnSelection.place(0, 1, chkManual);
		pnSelection.place(1, 0, pnAuto);
		pnSelection.place(1, 1, pnManual);

		// Panel Display
		pnDisplay = new DisplayPanel(settings, canvas);
		GridPanel button = new GridPanel(false, 0);
		button.place(0, 1, bnHelp);
		button.place(0, 2, bnSave);
		button.place(0, 3, bnJournal);
		button.place(0, 4, bnClose);

		tab.add("Selection", pnSelection);
		tab.add("Display", pnDisplay);

		JPanel main = new JPanel(new BorderLayout());
		main.add(tab, BorderLayout.NORTH);
		main.add(table.getPane(200, 300), BorderLayout.CENTER);
		main.add(button, BorderLayout.SOUTH);

		chkManual.addActionListener(this);
		chkAutomatic.addActionListener(this);
		bnMore.addActionListener(this);
		bnLess.addActionListener(this);
		bnClose.addActionListener(this);
		bnSave.addActionListener(this);
		bnHelp.addActionListener(this);
		bnJournal.addActionListener(this);
		bnFeature.addActionListener(this);
		pnDisplay.updateCanvas();
		cmbBestCenter.addActionListener(this);
		table.addKeyListener(this);
		table.getSelectionModel().addListSelectionListener(this);

		addWindowListener(this);
		addMouseListener(this);
		add(main);

		pack();
		Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dlg = this.getSize();
		Dimension img = imp.getWindow().getSize();
		Point loc = imp.getWindow().getLocation();
		if (loc.x + img.width + dlg.width < scr.width)
			setLocation(loc.x + img.width + 5, loc.y);
		else
			GUI.center(this);
		setVisible(true);
		update(-1);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		cmbBestCenter.addActionListener(this);

		if (src == bnClose)
			close(true);
		else if (event.getSource() == bnJournal)
			showJournal();
	//	else if (event.getSource() == cmbBestCenter)
	//		canvas.setOptimize(cmbBestCenter.getSelectedIndex());
		else if (event.getSource() == bnMore) {
			data.changeRatioSelected(true);
			update(-1);
		}
		else if (event.getSource() == bnLess) {
			data.changeRatioSelected(false);
			update(-1);
		}
		else if (event.getSource() == chkManual)
			update(-1);
		else if (event.getSource() == chkAutomatic)
			update(-1);
		else if (event.getSource() == bnFeature)
			new DialogFeatureSelection(this, data, imp.getWidth(), imp.getHeight());

		else if (event.getSource() == bnHelp)
			WebBrowser.open("http://bigwww.epfl.ch");

		else if (src == bnSave) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Specify the filename to save");
			fc.setSelectedFile(new File(name + ".csv"));
			int ret = fc.showSaveDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				data.saveCVS(file.getAbsolutePath());
			}
		}
	}

	public void close(boolean saveParameters) {
		bnClose.removeActionListener(this);
		bnSave.removeActionListener(this);
		bnHelp.removeActionListener(this);
		bnFeature.removeActionListener(this);
		this.removeMouseListener(this);
		this.removeWindowListener(this);
		if (imp != null) {
			imp.killRoi();
			ImagePlus i = imp.duplicate();
			i.show();
			i.setTitle(imp.getTitle());
			imp.close();
		}
		if (saveParameters)
			settings.storeRecordedItems();
		dispose();
	}

	public void disableAll() {
		this.setTitle("Detector [No image]");
		bnFeature.setEnabled(false);
		this.removeMouseListener(this);
		this.removeWindowListener(this);
	}

	private void showJournal() {
		JFrame frame = new JFrame("Journal");
		frame.getContentPane().add(info.getPane());
		frame.pack();
		frame.setVisible(true);
	}

	public void update(int selectedID) {
		boolean automatic = chkAutomatic.isSelected();
		boolean manual = chkManual.isSelected();
		updateTable(selectedID);
		table.update(data.getSelectedAndManual(automatic, manual));
		canvas.setFlag(automatic, manual);
		updateCount();
	}

	public void updateDetection(Detection detection) {
		table.update(detection);
	}

	public void updateTable(int selectedID) {
		for (int row = 0; row < table.getRowCount(); row++) {
			int id = table.getID(row);
			if (selectedID == id) {
				JViewport viewport = (JViewport) table.getParent();
				Rectangle rect = table.getCellRect(row, 0, true);
				Point pt = viewport.getViewPosition();
				rect.setLocation(rect.x - pt.x, rect.y - pt.y);
				viewport.scrollRectToVisible(rect);
				table.setRowSelectionInterval(row, row);
			}
		}
	}

	public void updateCanvas(int selectedID) {
		canvas.setSelected(selectedID);
		updateTable(selectedID);
		updateCount();
	}

	public void updateCount() {
		lblCountManual.setText("" + data.getManual().size() + " detections");
		lblCountAuto.setText("" + data.getSelected().size() + "/" + data.getDetected().size() + " detections");
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		int row = table.getSelectedRow();
		updateCanvas(table.getID(row));
	}

	@Override
	public void windowActivated(WindowEvent event) {
	}

	@Override
	public void windowClosed(WindowEvent event) {
	}

	@Override
	public void windowClosing(WindowEvent event) {
		close(false);
	}

	@Override
	public void windowDeactivated(WindowEvent event) {
	}

	@Override
	public void windowDeiconified(WindowEvent event) {
	}

	@Override
	public void windowIconified(WindowEvent event) {
	}

	@Override
	public void windowOpened(WindowEvent event) {
	}

	@Override
	public void mouseClicked(MouseEvent event) {
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		if (imp == null)
			disableAll();
		if (imp != null && imp.isVisible() == false)
			disableAll();
	}

	@Override
	public void mouseExited(MouseEvent event) {
	}

	@Override
	public void mousePressed(MouseEvent event) {
	}

	@Override
	public void mouseReleased(MouseEvent event) {
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE || event.getKeyCode() == KeyEvent.VK_DELETE) {
			int row = table.getSelectedRow();
			if (row >= 0) {
				data.remove(table.getID(row));
				update(-1);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}
}

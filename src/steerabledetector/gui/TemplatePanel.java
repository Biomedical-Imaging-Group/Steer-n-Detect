package steerabledetector.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.Roi;
import ij.plugin.filter.RankFilters;
import ij.process.ImageConverter;
import steerabledetector.Tools;
import steerabledetector.detector.Parameters;
import steerabledetector.filter.SIPM;
import steerabledetector.filter.Spline;
import steerabledetector.gui.components.GridPanel;
import steerabledetector.gui.components.HTMLPane;
import steerabledetector.gui.components.ProgressionBar;
import steerabledetector.gui.components.SpinnerInteger;
import steerabledetector.image2d.ImageCartesian;

public class TemplatePanel extends JPanel implements ActionListener, Runnable {

	private DialogMain		dlg;

	private JButton			bnRoi			= new JButton("Get ROI");
	private JButton			bnFile			= new JButton("Get File");
	private JButton			bnWindow			= new JButton("Get Window");
	private JButton			bnRebuild		= new JButton("Rebuild");
	private JCheckBox		chkHarmonics		= new JCheckBox("Harmonics");
	private JCheckBox		chkPrefilter		= new JCheckBox("Prefilter ROI", true);
	private SpinnerInteger	spnMedian		= new SpinnerInteger(2, 0, 100, 1);

	private SpinnerInteger	spnHarmonic		= new SpinnerInteger(7, 1, 30, 1);
	private ImagePlus		impSource		= null;
	private ImagePlus		impTemplate		= null;
	private HTMLPane			pane				= new HTMLPane(300, 80);
	private Parameters		params;
	private ProgressionBar	progress;
	private HTMLPane 		info;

	public TemplatePanel(DialogMain dlg, ImagePlus impSource, ProgressionBar progress, HTMLPane info, Parameters params) {
		this.dlg = dlg;
		this.impSource = impSource;
		this.progress = progress;
		this.info = info;
		this.params = params;

		spnHarmonic.set(params.nHarmonics);
		
		GridPanel pnTemplate = new GridPanel("Template", 4);
		pnTemplate.place(0, 0, bnRoi);
		pnTemplate.place(0, 1, bnFile);
		pnTemplate.place(0, 2, bnWindow);
		pnTemplate.place(1, 0, chkHarmonics);
		pnTemplate.place(1, 1, spnHarmonic);
		pnTemplate.place(1, 2, bnRebuild);
		
		pnTemplate.place(2, 0, chkPrefilter);
		pnTemplate.place(2, 1, "Median");
		pnTemplate.place(2, 2, spnMedian);
		
		pnTemplate.place(3, 0, 3, 1, pane);

		bnFile.setDropTarget(new LocalDropTargetTemplate());
		pane.setDropTarget(new LocalDropTargetTemplate());
		reset();
		pane.append("p", "Drag & drop a file, e.g. TIF, PNG");
		pane.append("p", "2D square image (graylevel)");

		add(pnTemplate);

		bnRoi.addActionListener(this);
		bnFile.addActionListener(this);
		bnWindow.addActionListener(this);
		bnRebuild.addActionListener(this);
		chkHarmonics.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bnRoi) {
			getRoi();
			build();
		}
		if (e.getSource() == bnFile) {
			String filename = browse();
			openFile(filename);
			build();
		}
		if (e.getSource() == bnWindow) {
			openWindow();
			build();
		}
		if (e.getSource() == bnRebuild) {
			bnRebuild.setEnabled(false);
			spnHarmonic.setEnabled(false);
			chkHarmonics.setSelected(false);
			build();
		}
		if (e.getSource() == chkHarmonics) {
			bnRebuild.setEnabled(chkHarmonics.isSelected());
			spnHarmonic.setEnabled(chkHarmonics.isSelected());
		}
	}

	public void reset() {
		pane.clear();
		pane.append("b", "Template model");
	}

	public void build() {
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	public void run() {
		if (impTemplate == null)
			return;
		params.nHarmonics = spnHarmonic.get();
		params.patternSizeX = impTemplate.getWidth();
		params.patternSizeY = impTemplate.getHeight();
		new ImageConverter(impTemplate).convertToGray32();
		double mean = impTemplate.getStatistics().mean;
		impTemplate.getProcessor().subtract(mean);
		ImageCartesian template = ImageCartesian.getImage(impTemplate);
		template.apodizationHann();
		if (template.isFourierImage()) {
			template = template.inverseFFT();
		}
		reset();
		pane.append("p", "Building ... ");
		pane.append("p", "Size: " + params.patternSizeX + "x" + params.patternSizeY);
		pane.append("p", params.nHarmonics + " harmonics");
		double chrono = System.nanoTime();		
		SIPM model = createSIPM(progress, info, template);
		reset();
		if (model == null) {
			pane.append("Impossible to build this template model");
			return;
		}

		pane.append("p", "Template: " + params.patternSizeX + "x" + params.patternSizeY);
		pane.append("p", params.nHarmonics + " harmonics");
		pane.append("p", "" + Tools.time(System.nanoTime() - chrono));
		dlg.setModel(model);
		model.getDetector(params.nHarmonics, 0).inverseFFT().getRealPart().show();
	}

	private SIPM createSIPM(ProgressionBar progress, HTMLPane info, ImageCartesian templateSpaceInput) {
		return SIPM.getMethod(progress, info, templateSpaceInput, new Spline(), templateSpaceInput, 0);
	}

	public void getRoi() {
		if (impSource == null)
			return;
		if (!(impSource.getCanvas() instanceof CanvasTemplate))
			return;

		CanvasTemplate canvas = (CanvasTemplate) impSource.getCanvas();
		Point[] centerAndControl = canvas.getHotPoints();
		Point center = centerAndControl[0];
		Point control = centerAndControl[1];
		int dx = center.x - control.x;
		int dy = center.y - control.y;
		int r = (int)(Math.round(Math.sqrt(dx * dx + dy * dy)));			
		Roi roi = new Roi(center.x-r, center.y-r, 2*r, 2*r);
		impSource.setRoi(roi);
		impTemplate = impSource.crop();
		new ImageConverter(impTemplate).convertToGray32();
		//double angle = Math.toDegrees(Math.atan2(dy, -dx));
		//impCrop.getProcessor().rotate(angle);
			
		if (chkPrefilter.isSelected() && spnMedian.get() > 0) {
			RankFilters rf = new RankFilters();
			rf.rank(impTemplate.getProcessor(), spnMedian.get(), RankFilters.MEDIAN);
		}
		params.referenceOrientation = Math.atan2(-dy, -dx);
		impSource.killRoi();
	}

	public String browse() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ret = fc.showOpenDialog(null);
		if (ret == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile().getAbsolutePath();
		}
		return "";
	}

	public void openFile(String filename) {
		File file = new File(filename);
		reset();
		if (!file.exists()) {
			pane.append("p", "Filename not found");
			return;
		}
		if (!file.isFile()) {
			pane.append("p", "Filename is not a file");
			return;
		}
		ImagePlus image = IJ.openImage(filename);
		if (image == null) {
			pane.append("p", "Filename is not a image:");
			return;
		}
		if (image.getType() != ImagePlus.GRAY8 && image.getType() != ImagePlus.GRAY16
				&& image.getType() != ImagePlus.GRAY32) {
			pane.append("p", " It is not agrayscale image");
			return;
		}
		if (image.getStackSize() > 1) {
			pane.append("p", "It is not a 2D image");
			return;
		}
		if (image.getHeight() != image.getWidth()) {
			pane.append("p", "This image is not square [" + image.getHeight() + "x" + image.getWidth() + "]");
			return;
		}
		impTemplate = image.duplicate();
	}

	public void openWindow() {
		Dialog dialog = new Dialog();
		dialog.setVisible(true);
		if (dialog.wasCancel())
			return;
		impTemplate = WindowManager.getImage(dialog.getName());
	}

	public class Dialog extends JDialog implements ActionListener, WindowListener {

		private JList<String>	list;
		private JButton			bnOK		= new JButton("OK");
		private JButton			bnCancel	= new JButton("Cancel");
		private boolean			cancel		= false;
		private String			name		= "";

		public Dialog() {
			super(new JFrame(), "List of square images");

			JPanel bn = new JPanel(new GridLayout(1, 2));
			bn.add(bnCancel);
			bn.add(bnOK);

			JPanel panel = new JPanel(new BorderLayout());
			int[] ids = WindowManager.getIDList();

			if (ids != null) {
				DefaultListModel<String> listModel = new DefaultListModel<String>();
				list = new JList<String>(listModel);
				for (int id : ids) {
					ImagePlus idp = WindowManager.getImage(id);
					if (idp != null)
						if (idp.getStackSize() == 1)
							if (idp.getWidth() == idp.getHeight()) {
								((DefaultListModel<String>) listModel).addElement((String) idp.getTitle());
							}
				}
				list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
				JScrollPane listScroller = new JScrollPane(list);
				listScroller.setPreferredSize(new Dimension(250, 80));
				panel.add(listScroller, BorderLayout.CENTER);
			}
			else {
				panel.add(new JLabel("No open square images."));
			}
			panel.add(bn, BorderLayout.SOUTH);
			panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			bnOK.addActionListener(this);
			bnCancel.addActionListener(this);
			add(panel);
			pack();
			addWindowListener(this);
			GUI.center(this);
			setModal(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			bnOK.removeActionListener(this);
			bnCancel.removeActionListener(this);
			if (e.getSource() == bnCancel) {
				cancel = true;
				name = "";
				dispose();
				return;
			}
			else
				if (e.getSource() == bnOK) {
					cancel = false;
					name = (String) list.getSelectedValue();
					dispose();
				}
		}

		@Override
		public String getName() {
			return name;
		}

		public boolean wasCancel() {
			return cancel;
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			dispose();
			cancel = true;
			name = "";
			return;
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}
	}

	public class LocalDropTargetTemplate extends DropTarget {

		@Override
		public void drop(DropTargetDropEvent e) {
			e.acceptDrop(DnDConstants.ACTION_COPY);
			e.getTransferable().getTransferDataFlavors();
			Transferable transferable = e.getTransferable();
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			for (DataFlavor flavor : flavors) {
				if (flavor.isFlavorJavaFileListType()) {
					try {
						List<File> files = (List<File>) transferable.getTransferData(flavor);
						for (File file : files) {
							if (file.isFile()) {
								String filename = file.getAbsolutePath();
								openFile(filename);
								build();
							}
						}
					}
					catch (UnsupportedFlavorException ex) {
						ex.printStackTrace();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			e.dropComplete(true);
			super.drop(e);
		}
	}
}

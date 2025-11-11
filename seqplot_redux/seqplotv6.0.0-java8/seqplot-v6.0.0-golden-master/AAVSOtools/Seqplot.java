/*
 * Seqplot 6.0.0 - Enhanced Astronomical Sequence Plotting Application
 * Version 6.0.0 - Released November 3, 2025
 * 
 * Enhanced with warm pastel color schemes and improved accessibility
 */
package AAVSOtools;

import AAVSOtools.DataConnector;
import AAVSOtools.Hyperlink;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleInsets;

public class Seqplot
extends JFrame
implements ActionListener,
ChartProgressListener,
ChartMouseListener,
MouseListener {
    private static final long serialVersionUID = 1L;
    private static final int MAX_NUMBER_OF_ZOOMS = 10;
    private static final int COLOR_ARRAY_SIZE = 7;
    private static final int Y_LOC = 0;
    private static final Double FRAME_HT_FACTOR = 0.95;
    private static final Double FRAME_WDTH_FACTOR = 1.333;
    private static final Double PLOT_HT_FACTOR = 0.7;
    private static final Double PLOT_WDTH_FACTOR = 0.6;
    private static final double DOTSIZE_SCALE_FACTOR = 5.0;
    private static final double RELATIVE_DOTSIZE_SCALE_FACTOR = 0.85;
    private static final String RELEASE_DATE = "Last update 2025-11-03";
    private static final String VERSION = "6.0.0";
    private static final String SEQPLOT_HELP_PAGE = "https://www.aavso.org/files/software/seqplot/seqplot_help.html";
    public Color[] dataColor = new Color[7];
    public Color[] savedColor = new Color[7];
    private double dotsize = 5.0;
    private double relativeDotsize = 0.85;
    private int recordNumber = 0;
    private int seriesNumber = 0;
    
    // Setter for record number (used by StarPlotPanel)
    public void setRecordNumber(int recordNumber) {
        this.recordNumber = recordNumber;
        this.seriesNumber = this.db.getSeries(recordNumber);
        this.displayData(recordNumber, this.seriesNumber);
    }
    private int zoomlevel = 0;
    private int clickCount = 0;
    private int fontSize = 16;
    private double crosshairX = -10.0;
    private double crosshairY = -10.0;
    private Double[] domainLB = new Double[10];
    private Double[] domainUB = new Double[10];
    private Double[] rangeLB = new Double[10];
    private Double[] rangeUB = new Double[10];
    private String nextstarButtonLabel = "NEXT STAR";
    private String quitButtonLabel = "QUIT";
    private String resetButtonLabel = "ZOOM OUT";
    private String saveButtonLabel = "SEND TO FILE";
    private String menuText = "Options";
    private String preferenceText = "Preferences..";
    private String printText = "Print plot...";
    private String savePlotText = "Save plot...";
    private String undoText = "Undo Actions";
    private String renameFileText = "Rename logfile...";
    private String customColorsText = "Customize colors...";
    private String limitingMagText = "Change limiting magnitude...";
    private String fontSizeText = "Change font size...";
    private String positionToleranceText = "Change variable star position tolerance...";
    private String dotsizeText = "Change dot size...";
    private String relativeDotsizeText = "Change relative dot size...";
    private String biggerDotsText = "Make dots bigger";
    private String smallerDotsText = "Make dots smaller";
    private String defaultDotsText = "Default dot size";
    private String specifyDotsText = "Select dot size...";
    private String greaterDifferenceText = "Make difference greater";
    private String smallerDifferenceText = "Make difference less";
    private String defaultRelativeText = "Default relative difference";
    private String specifyRelativeText = "Select relative difference...";
    private String downloadTableText = "DOWNLOAD TABLE";
    private String chartTitle;
    private String chartSubtitle;
    private Boolean mouseClicked = false;
    private Boolean mouseDragged = false;
    private Boolean showPlot = true;
    private JButton nextstarButton;
    private JButton quitButton;
    private JButton resetButton;
    private JButton saveButton;
    private JButton downloadTableButton;
    private JMenu menuOptions;
    private JMenu preferenceOption;
    private JMenu dotsizeOption;
    private JMenu relativeDotsizeOption;
    private JMenu menuHelp;
    private JMenuBar menuBar;
    private JMenuItem aboutHelp;
    private JMenuItem biggerDotsOption;
    private JMenuItem smallerDotsOption;
    private JMenuItem defaultDotsOption;
    private JMenuItem greaterDifferenceOption;
    private JMenuItem smallerDifferenceOption;
    private JMenuItem defaultRelativeOption;
    private JMenuItem onlineHelp;
    private JMenuItem printOption;
    private JMenuItem savePlotOption;
    private JMenuItem specifyDotsOption;
    private JMenuItem specifyRelativeOption;
    private JMenuItem renameFileOption;
    private JMenuItem limitingMagOption;
    private JMenuItem fontSizeOption;
    private JMenuItem positionToleranceOption;
    private JTextArea readout;
    private JPanel centerPanel;
    private ValueAxis dAxis;
    private ValueAxis rAxis;
    private TextTitle mainTitle;
    private TextTitle subTitle;
    private DecimalFormat threeDecimalFormat = new DecimalFormat("0.0##");
    private Container cp;
    private ChartPanel chartPanel;
    private Frame frame;
    private JFreeChart chart;
    private XYPlot plot;
    DataConnector db = new DataConnector(this);
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    public Seqplot() {
        super("Seqplot 6.0.0");
        if (this.getShowPlot().booleanValue()) {
            int wdth = (int)(this.dim.getHeight() * FRAME_WDTH_FACTOR) <= (int)this.dim.getWidth() ? (int)(this.dim.getHeight() * FRAME_WDTH_FACTOR) : (int)this.dim.getWidth();
            this.setSize(wdth, (int)(this.dim.getHeight() * FRAME_HT_FACTOR));
            this.setLocation(((int)this.dim.getWidth() - wdth) / 2, 0);
            this.cp = this.getContentPane();
            this.cp.setLayout(new BorderLayout());
            this.menuBar = new JMenuBar();
            this.menuOptions = new JMenu(this.menuText);
            this.preferenceOption = new JMenu(this.preferenceText);
            this.preferenceOption.addActionListener(this);
            this.printOption = new JMenuItem(this.printText);
            this.printOption.addActionListener(this);
            this.savePlotOption = new JMenuItem(this.savePlotText);
            this.savePlotOption.addActionListener(this);
            this.renameFileOption = new JMenuItem(this.renameFileText);
            this.renameFileOption.addActionListener(this);
            this.menuOptions.add(this.preferenceOption);
            this.menuOptions.add(this.printOption);
            this.menuOptions.add(this.savePlotOption);
            this.menuOptions.add(this.renameFileOption);
            this.limitingMagOption = new JMenuItem(this.limitingMagText);
            this.limitingMagOption.addActionListener(this);
            this.dotsizeOption = new JMenu(this.dotsizeText);
            this.relativeDotsizeOption = new JMenu(this.relativeDotsizeText);
            this.fontSizeOption = new JMenuItem(this.fontSizeText);
            this.fontSizeOption.addActionListener(this);
            this.positionToleranceOption = new JMenuItem(this.positionToleranceText);
            this.positionToleranceOption.addActionListener(this);
            this.preferenceOption.add(this.limitingMagOption);
            this.preferenceOption.add(this.positionToleranceOption);
            this.preferenceOption.add(this.dotsizeOption);
            this.preferenceOption.add(this.relativeDotsizeOption);
            this.preferenceOption.add(this.fontSizeOption);
            this.biggerDotsOption = new JMenuItem(this.biggerDotsText);
            this.biggerDotsOption.addActionListener(this);
            this.smallerDotsOption = new JMenuItem(this.smallerDotsText);
            this.smallerDotsOption.addActionListener(this);
            this.defaultDotsOption = new JMenuItem(this.defaultDotsText);
            this.defaultDotsOption.addActionListener(this);
            this.specifyDotsOption = new JMenuItem(this.specifyDotsText);
            this.specifyDotsOption.addActionListener(this);
            this.dotsizeOption.add(this.biggerDotsOption);
            this.dotsizeOption.add(this.smallerDotsOption);
            this.dotsizeOption.add(this.defaultDotsOption);
            this.dotsizeOption.add(this.specifyDotsOption);
            this.greaterDifferenceOption = new JMenuItem(this.greaterDifferenceText);
            this.greaterDifferenceOption.addActionListener(this);
            this.smallerDifferenceOption = new JMenuItem(this.smallerDifferenceText);
            this.smallerDifferenceOption.addActionListener(this);
            this.defaultRelativeOption = new JMenuItem(this.defaultRelativeText);
            this.defaultRelativeOption.addActionListener(this);
            this.specifyRelativeOption = new JMenuItem(this.specifyRelativeText);
            this.specifyRelativeOption.addActionListener(this);
            this.relativeDotsizeOption.add(this.greaterDifferenceOption);
            this.relativeDotsizeOption.add(this.smallerDifferenceOption);
            this.relativeDotsizeOption.add(this.defaultRelativeOption);
            this.relativeDotsizeOption.add(this.specifyRelativeOption);
            this.menuHelp = new JMenu("Help");
            this.onlineHelp = new JMenuItem("Online help");
            this.aboutHelp = new JMenuItem("About Seqplot");
            this.onlineHelp.addActionListener(this);
            this.aboutHelp.addActionListener(this);
            this.menuHelp.add(this.onlineHelp);
            this.menuHelp.add(this.aboutHelp);
            this.menuBar.add(this.menuOptions);
            this.menuBar.add(this.menuHelp);
            this.cp.add((Component)this.menuBar, "First");
            this.resetButton = new JButton(this.resetButtonLabel);
            this.setupButton(this.resetButton);
            this.nextstarButton = new JButton(this.nextstarButtonLabel);
            this.setupButton(this.nextstarButton);
            this.quitButton = new JButton(this.quitButtonLabel);
            this.setupButton(this.quitButton);
            this.saveButton = new JButton(this.saveButtonLabel);
            this.setupButton(this.saveButton);
            this.downloadTableButton = new JButton(this.downloadTableText);
            this.setupButton(this.downloadTableButton);
            JPanel spacer1 = new JPanel();
            spacer1.setPreferredSize(new Dimension(150, 50));
            JPanel spacer2 = new JPanel();
            spacer2.setPreferredSize(new Dimension(150, 50));
            JPanel spacer3 = new JPanel();
            spacer3.setPreferredSize(new Dimension(150, 50));
            JPanel spacer4 = new JPanel();
            spacer4.setPreferredSize(new Dimension(150, 50));
            JPanel spacer5 = new JPanel();
            spacer5.setPreferredSize(new Dimension(150, 50));
            JPanel buttonPanel = new JPanel(new GridLayout(9, 1));
            buttonPanel.add(spacer1);
            buttonPanel.add(this.resetButton);
            buttonPanel.add(this.nextstarButton);
            buttonPanel.add(this.quitButton);
            buttonPanel.add(spacer2);
            buttonPanel.add(this.downloadTableButton);
            buttonPanel.add(spacer3);
            buttonPanel.add(this.saveButton);
            buttonPanel.add(spacer4);
            this.cp.add((Component)buttonPanel, "After");
            this.readout = new JTextArea();
            this.readout.setEditable(false);
            this.readout.setBorder(BorderFactory.createLineBorder(Color.black));
            this.readout.setPreferredSize(new Dimension(wdth - 30, 50));
            this.readout.setFont(new Font("Arial", 0, this.getFontSize()));
            this.readout.setLineWrap(true);
            this.readout.setWrapStyleWord(true);
            JScrollPane readoutScrollPane = new JScrollPane(this.readout);
            readoutScrollPane.setVerticalScrollBarPolicy(22);
            JPanel bottomPanel = new JPanel();
            bottomPanel.add(this.readout);
            this.cp.add((Component)bottomPanel, "Last");
            this.centerPanel = new JPanel();
            this.centerPanel.add(this.createPlotPanel());
            this.cp.add((Component)this.centerPanel, "Center");
            this.setDefaultCloseOperation(2);
            this.setVisible(true);
        }
    }

    private JFreeChart createChart(XYZDataset dataset) {
        this.chart = ChartFactory.createBubbleChart(null, "X (arcseconds)", "Y (arcseconds)", dataset, PlotOrientation.VERTICAL, false, false, false);
        this.chart.setBorderVisible(false);
        this.plot = (XYPlot)this.chart.getPlot();
        this.db.scaleDots();
        this.setPreferredColors();
        this.plot.setDomainGridlinesVisible(false);
        this.plot.setRangeGridlinesVisible(false);
        this.plot.setDomainCrosshairVisible(false);
        this.plot.setRangeCrosshairVisible(false);
        this.mainTitle = new TextTitle(this.getMainTitleText(), new Font("Ariel", 1, 14));
        this.subTitle = new TextTitle(this.getSubtitleText(), new Font("Ariel", 0, 11));
        this.chart.addSubtitle(this.mainTitle);
        this.chart.addSubtitle(this.subTitle);
        this.chart.setPadding(new RectangleInsets(5.0, 5.0, 5.0, 40.0));
        NumberAxis domainAxis = (NumberAxis)this.chart.getXYPlot().getDomainAxis();
        double minX = this.db.getMinX();
        double maxX = this.db.getMaxX();
        double minY = this.db.getMinY();
        double maxY = this.db.getMaxY();
        System.out.printf("DEBUG: Setting axis ranges - X: %.6f to %.6f, Y: %.6f to %.6f\n", 
                         1.05 * minX, 1.05 * maxX, 1.05 * minY, 1.05 * maxY);
        domainAxis.setRange(1.05 * minX, 1.05 * maxX);
        domainAxis.setInverted(true);
        domainAxis.setAutoRangeIncludesZero(false);
        NumberAxis rangeAxis = (NumberAxis)this.chart.getXYPlot().getRangeAxis();
        rangeAxis.setRange(1.05 * minY, 1.05 * maxY);
        rangeAxis.setAutoRangeIncludesZero(true);
        return this.chart;
    }

    public void setPreferredColors() {
        this.plot.setBackgroundPaint(this.getPlotColor(5));
        System.out.printf("DEBUG: Plot background color set to: %s\n", this.getPlotColor(5));
        this.plot.setRenderer(new XYBubbleRenderer(XYBubbleRenderer.SCALE_ON_BOTH_AXES));
        XYItemRenderer renderer = this.plot.getRenderer();
        int i = 0;
        while (i < 5) {
            Color seriesColor = this.getPlotColor(i);
            System.out.printf("DEBUG: Series %d color set to: %s\n", i, seriesColor);
            renderer.setSeriesPaint(i, seriesColor);
            renderer.setSeriesOutlinePaint(i, seriesColor);
            renderer.setSeriesVisible(i, (Boolean)true);
            ++i;
        }
        this.plot.setDomainCrosshairPaint(this.getPlotColor(6));
        this.plot.setRangeCrosshairPaint(this.getPlotColor(6));
        this.plot.setRenderer(renderer);
    }

    // Replace ChartPanel with StarPlotPanel
    private StarPlotPanel starPlotPanel;
    
    public StarPlotPanel createPlotPanel() {
        System.out.println("DEBUG: Creating StarPlotPanel instead of ChartPanel");
        this.starPlotPanel = new StarPlotPanel(this.db, this);
        this.starPlotPanel.setPreferredSize(new Dimension((int)(this.dim.getWidth() * PLOT_WDTH_FACTOR), (int)(this.dim.getHeight() - 200.0)));
        
        // Note: We no longer need the JFreeChart creation
        return this.starPlotPanel;
    }

    public double getPlotWidthFactor() {
        return this.dim.getHeight() * PLOT_HT_FACTOR * (this.db.getDomainUpperBound() / this.db.getRangeUpperBound());
    }

    public double getPlotHeightFactor() {
        return this.dim.getHeight() * PLOT_HT_FACTOR;
    }

    @Override
    public void chartProgress(ChartProgressEvent event) {
        if (event.getType() != 2) {
            return;
        }
        if (event.getType() == 2) {
            this.chart = this.chartPanel.getChart();
            if (this.chart != null && this.mouseClicked.booleanValue()) {
                this.getCrosshairPosition();
                this.plot.setDomainCrosshairVisible(true);
                this.plot.setRangeCrosshairVisible(true);
                this.displayData(this.recordNumber, this.seriesNumber);
                this.mouseClicked = false;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        int font;
        String fontSizeString;
        String relative;
        String size;
        if (ae.getSource() == this.resetButton) {
            // Use StarPlotPanel zoom functionality instead of JFreeChart
            if (this.starPlotPanel != null) {
                if (this.zoomlevel <= 1) {
                    this.starPlotPanel.resetZoom();
                    this.zoomlevel = 0;
                } else {
                    this.starPlotPanel.zoomOut();
                    --this.zoomlevel;
                }
            } else {
                // Fallback to old JFreeChart method if StarPlotPanel not available
                if (this.zoomlevel <= 1) {
                    this.chartPanel.restoreAutoBounds();
                    this.zoomlevel = 0;
                } else {
                    --this.zoomlevel;
                    this.dAxis.setUpperBound(this.domainUB[this.zoomlevel]);
                    this.dAxis.setLowerBound(this.domainLB[this.zoomlevel]);
                    this.rAxis.setUpperBound(this.rangeUB[this.zoomlevel]);
                    this.rAxis.setLowerBound(this.rangeLB[this.zoomlevel]);
                }
            }
        }
        if (ae.getSource() == this.nextstarButton) {
            System.out.println("DEBUG: Next star button pressed - querying database for new star");
            this.db.queryDatabase();
            if (!this.db.getQuitSelected().booleanValue() && !this.db.getCancelSelected().booleanValue()) {
                this.mainTitle.setText(this.getMainTitleText());
                this.setSubtitleText("Data from the Calibration Database - limiting magnitude " + this.db.getLimitingMag() + " - VSX position matching tolerance " + this.db.getPositionTolerance() + " degrees");
                this.updateSubtitleText();
                this.db.scaleDots();
                
                // Reset zoom and refresh display for new star data
                if (this.starPlotPanel != null) {
                    System.out.println("DEBUG: Refreshing StarPlotPanel for new star data");
                    this.starPlotPanel.refreshPlotData();
                } else {
                    // Fallback to old JFreeChart method
                    this.chartPanel.restoreAutoBounds();
                    this.chartPanel.setPreferredSize(new Dimension((int)this.getPlotWidthFactor(), (int)this.getPlotHeightFactor()));
                }
                
                this.zoomlevel = 0;
                
                // Update old chart components if they exist (for backward compatibility)
                if (this.plot != null) {
                    this.plot.setDomainCrosshairVisible(false);
                    this.plot.setRangeCrosshairVisible(false);
                }
                
                this.displayData(0, 0);
                if (!this.db.getRAText().equals(this.db.getOldRAText()) || !this.db.getDecText().equals(this.db.getOldDecText())) {
                    this.clickCount = 0;
                }
            }
        }
        if (ae.getSource() == this.biggerDotsOption) {
            this.setDotsizeScaleFactor(this.getDotsizeScaleFactor() + 1.0);
            this.db.setUserDefaults(this.getColorArray());
            this.db.scaleDots();
            // Update StarPlotPanel point size
            if (this.starPlotPanel != null) {
                this.starPlotPanel.increasePointSize();
            }
        }
        if (ae.getSource() == this.smallerDotsOption && this.getDotsizeScaleFactor() > 1.0) {
            this.setDotsizeScaleFactor(this.getDotsizeScaleFactor() - 1.0);
            this.db.setUserDefaults(this.getColorArray());
            this.db.scaleDots();
            // Update StarPlotPanel point size
            if (this.starPlotPanel != null) {
                this.starPlotPanel.decreasePointSize();
            }
        }
        if (ae.getSource() == this.defaultDotsOption) {
            this.setDotsizeScaleFactor(5.0);
            this.db.setUserDefaults(this.getColorArray());
            this.db.scaleDots();
            // Update StarPlotPanel point size to default
            if (this.starPlotPanel != null) {
                this.starPlotPanel.setPointSizeScale(1.0); // Default scale
            }
        }
        if (ae.getSource() == this.specifyDotsOption && !(size = JOptionPane.showInputDialog(this.frame, "Please enter the dot size scale factor:\n(currently " + this.getDotsizeScaleFactor() + " - default " + 5.0 + ")", "Set dot size", 3)).equals("")) {
            this.setDotsizeScaleFactor(Double.parseDouble(size));
            this.db.setUserDefaults(this.getColorArray());
            this.db.scaleDots();
            // Update StarPlotPanel point size based on user input
            if (this.starPlotPanel != null) {
                // Convert dotsize scale factor to our point size scale (approximate mapping)
                double pointScale = Double.parseDouble(size) / 5.0; // 5.0 is default
                this.starPlotPanel.setPointSizeScale(pointScale);
            }
        }
        if (ae.getSource() == this.greaterDifferenceOption) {
            this.setRelativeDotsizeScaleFactor(this.getRelativeDotsizeScaleFactor() + 0.1);
            this.db.setUserDefaults(this.getColorArray());
            this.db.scaleDots();
        }
        if (ae.getSource() == this.smallerDifferenceOption && this.getRelativeDotsizeScaleFactor() > 0.1) {
            this.setRelativeDotsizeScaleFactor(this.getRelativeDotsizeScaleFactor() - 0.1);
            this.db.setUserDefaults(this.getColorArray());
            this.db.scaleDots();
        }
        if (ae.getSource() == this.defaultRelativeOption) {
            this.setRelativeDotsizeScaleFactor(0.85);
            this.db.setUserDefaults(this.getColorArray());
            this.db.scaleDots();
        }
        if (ae.getSource() == this.specifyRelativeOption && !(relative = JOptionPane.showInputDialog(this.frame, "Please enter the relative dot size scale factor:\n(currently " + this.getRelativeDotsizeScaleFactor() + " - default " + 0.85 + ")", "Set relative dot size", 3)).equals("")) {
            this.setRelativeDotsizeScaleFactor(Double.parseDouble(relative));
            this.db.setUserDefaults(this.getColorArray());
            this.db.scaleDots();
        }
        if (ae.getSource() == this.limitingMagOption) {
            String limitingMag = JOptionPane.showInputDialog(this.frame, "Please enter the limiting magnitude:", "Set Limiting Magnitude", 3);
            if (limitingMag != null && !limitingMag.trim().equals("")) {
                try {
                    double newLimitingMag = Double.parseDouble(limitingMag.trim());
                    
                    // Set the new limiting magnitude
                    this.db.setLimitingMag(newLimitingMag);
                    
                    // Update subtitle text to reflect new limiting magnitude
                    this.setSubtitleText("Data from the Calibration Database - limiting magnitude " + this.db.getLimitingMag() + " - VSX position matching tolerance " + this.db.getPositionTolerance() + " degrees");
                    this.updateSubtitleText();
                    
                    // Query database with new limiting magnitude to get fresh target data
                    System.out.println("DEBUG: Querying database with new limiting magnitude: " + newLimitingMag);
                    this.db.queryDatabase();
                    
                    // Only proceed if user didn't quit or cancel the database query
                    if (!this.db.getQuitSelected().booleanValue() && !this.db.getCancelSelected().booleanValue()) {
                        // Clear crosshairs for fresh plot
                        if (this.plot != null) {
                            this.plot.setDomainCrosshairVisible(false);
                            this.plot.setRangeCrosshairVisible(false);
                        }
                        
                        // Refresh the plot display with new data
                        this.displayData(0, 0);
                        this.db.scaleDots();
                        
                        // Refresh StarPlotPanel if it exists
                        if (this.starPlotPanel != null) {
                            System.out.println("DEBUG: Refreshing StarPlotPanel with new limiting magnitude data");
                            this.starPlotPanel.refreshPlotData();
                        }
                        
                        System.out.println("DEBUG: Plot refreshed for limiting magnitude: " + newLimitingMag);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this.frame, 
                        "Invalid limiting magnitude value. Please enter a valid number.", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (ae.getSource() == this.fontSizeOption) {
            fontSizeString = JOptionPane.showInputDialog(this.frame, 
                "Please enter the point size of the font you want for the readout (10-20):\n(currently " + this.getFontSize() + ")", 
                "Set Font Size", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (fontSizeString != null && !fontSizeString.trim().equals("")) {
                try {
                    int newFontSize = Integer.parseInt(fontSizeString.trim());
                    
                    // Validate font size range
                    if (newFontSize >= 10 && newFontSize <= 20) {
                        // Set the new font size
                        this.setFontSize(newFontSize);
                        
                        // Update the readout text box font
                        this.readout.setFont(new Font("Arial", Font.PLAIN, this.getFontSize()));
                        
                        // Save user preferences
                        this.db.setUserDefaults(this.getColorArray());
                        
                        System.out.println("DEBUG: Font size changed to: " + newFontSize + " pt");
                        
                        // Force readout to repaint with new font
                        this.readout.revalidate();
                        this.readout.repaint();
                        
                    } else {
                        JOptionPane.showMessageDialog(this.frame, 
                            "Font size must be between 10 and 20 points. You entered: " + newFontSize, 
                            "Invalid Font Size", 
                            JOptionPane.WARNING_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this.frame, 
                        "Invalid font size value. Please enter a whole number between 10 and 20.", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (ae.getSource() == this.positionToleranceOption) {
            String positionToleranceString = JOptionPane.showInputDialog(this.frame, "Seqplot will look for a star within a certain distance from a catalog star\n in order to decide if it is a variable or not. What value would you like to use\n for this offset (tolerance)? The recommended value is 0.003 degrees.\n(currently " + this.db.getPositionTolerance() + ")", "Set position tolerance", 3);
            if (positionToleranceString != null && !positionToleranceString.trim().equals("")) {
                try {
                    double newPositionTolerance = Double.parseDouble(positionToleranceString.trim());
                    
                    // Set the new position tolerance
                    this.db.setPositionTolerance(newPositionTolerance);
                    
                    // Update subtitle text to reflect new position tolerance
                    this.setSubtitleText("Data from the Calibration Database - limiting magnitude " + this.db.getLimitingMag() + " - VSX position matching tolerance " + this.db.getPositionTolerance() + " degrees");
                    this.updateSubtitleText();
                    
                    // Query database with new position tolerance to get fresh target data
                    System.out.println("DEBUG: Querying database with new position tolerance: " + newPositionTolerance);
                    this.db.queryDatabase();
                    
                    // Only proceed if user didn't quit or cancel the database query
                    if (!this.db.getQuitSelected().booleanValue() && !this.db.getCancelSelected().booleanValue()) {
                        // Clear crosshairs for fresh plot
                        if (this.plot != null) {
                            this.plot.setDomainCrosshairVisible(false);
                            this.plot.setRangeCrosshairVisible(false);
                        }
                        
                        // Refresh the plot display with new data
                        this.displayData(0, 0);
                        this.db.scaleDots();
                        
                        // Refresh StarPlotPanel if it exists
                        if (this.starPlotPanel != null) {
                            System.out.println("DEBUG: Refreshing StarPlotPanel with new position tolerance data");
                            this.starPlotPanel.refreshPlotData();
                        }
                        
                        System.out.println("DEBUG: Plot refreshed for position tolerance: " + newPositionTolerance);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this.frame, 
                        "Invalid position tolerance value. Please enter a valid number.", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (ae.getSource() == this.printOption) {
            if (this.starPlotPanel != null) {
                this.starPlotPanel.createPrintJob();
            } else {
                // Fallback to old JFreeChart method if StarPlotPanel not available
                if (this.chartPanel != null) {
                    this.chartPanel.createChartPrintJob();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No plot available to print.", 
                        "Print Error", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        if (ae.getSource() == this.savePlotOption) {
            try {
                if (this.starPlotPanel != null) {
                    this.starPlotPanel.doSaveAs();
                } else {
                    // Fallback to old JFreeChart method if StarPlotPanel not available
                    if (this.chartPanel != null) {
                        this.chartPanel.doSaveAs();
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "No plot available to save.", 
                            "Save Error", 
                            JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
            catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving plot: " + ex.getMessage(), 
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        if (ae.getSource() == this.renameFileOption) {
            this.db.setUpLogfile("logfile");
        }
        if (ae.getSource() == this.onlineHelp) {
            try {
                String url = SEQPLOT_HELP_PAGE;
                Hyperlink hyperlink = new Hyperlink(url);
            }
            catch (MalformedURLException url) {
            }
            catch (IOException url) {
            }
            catch (Exception url) {
                // empty catch block
            }
        }
        if (ae.getSource() == this.aboutHelp) {
            JOptionPane.showMessageDialog(null, "Seqplot Version 6.0.0\nLast update 2025-11-03\n\nAuthor: Sara J. Beck, AAVSO Technical Staff\nContact: sara@aavso.org or gsilvis@aavso.org or aavso@aavso.org", "About Seqplot...", JOptionPane.INFORMATION_MESSAGE);
        }
        if (ae.getSource() == this.quitButton) {
            System.exit(0);
        }
        if (ae.getSource() == this.saveButton) {
            if (this.clickCount == 0) {
                this.db.setUpLogfile("logfile");
                StringBuilder strb = this.BuildReportHeader(1);
                this.db.sendToLogfile(strb.toString(), this.db.getLogfile("1"));
                strb = this.BuildReportHeader(2);
                this.db.sendToLogfile(strb.toString(), this.db.getLogfile("2"));
                strb = this.BuildReportHeader(3);
                this.db.sendToLogfile(strb.toString(), this.db.getLogfile("3"));
            }
            ++this.clickCount;
            this.db.sendToLogfile(this.getOutputInfo(1, this.recordNumber), this.db.getLogfile("1"));
            this.db.sendToLogfile(this.getOutputInfo(2, this.recordNumber), this.db.getLogfile("2"));
            this.db.sendToLogfile(this.getOutputInfo(3, this.recordNumber), this.db.getLogfile("3"));
        }
        if (ae.getSource() == this.downloadTableButton) {
            this.setShowPlot(false);
            this.db.setCancelSelected(false);
            this.db.setUpLogfile("datatable");
            this.db.sendToLogfile(this.BuildReportHeader(1).toString(), this.db.getTablefile("1"));
            int i = 0;
            while (i < this.db.getTotalCount()) {
                this.db.sendToLogfile(this.getOutputInfo(1, i), this.db.getTablefile("1"));
                ++i;
            }
            this.db.sendToLogfile(this.BuildReportHeader(2).toString(), this.db.getTablefile("2"));
            i = 0;
            while (i < this.db.getTotalCount()) {
                this.db.sendToLogfile(this.getOutputInfo(2, i), this.db.getTablefile("2"));
                ++i;
            }
            this.db.sendToLogfile(this.BuildReportHeader(3).toString(), this.db.getTablefile("3"));
            i = 0;
            while (i < this.db.getTotalCount()) {
                this.db.sendToLogfile(this.getOutputInfo(3, i), this.db.getTablefile("3"));
                ++i;
            }
        }
    }

    StringBuilder BuildReportHeader(int kind) {
        StringBuilder strb = new StringBuilder("");
        StringBuilder common = new StringBuilder("");
        common.append("#Data requested for: " + this.db.getStar() + " RA: " + this.db.getRAText() + " Dec: " + this.db.getDecText() + " Field size: " + Math.round(this.db.getFieldSize() * 60.0) + " Limiting mag: " + this.db.getLimitingMag() + "\n");
        common.append("# db call:, " + this.db.calibUrl + "\n");
        common.append("# chart:, " + this.db.getBaseURL() + "apps/vsp/chart/?title=" + this.db.urlEncode(this.db.getStar()) + "&ra=" + this.db.getRAText() + "&dec=" + this.db.getDecText() + "&fov=" + Math.round(this.db.getFieldSize() * 60.0) + "&maglimit=" + this.db.getLimitingMag() + "\n");
        common.append("# photometry:, " + this.db.getBaseURL() + "apps/vsp/photometry/?title=" + this.db.urlEncode(this.db.getStar()) + "&ra=" + this.db.getRAText() + "&dec=" + this.db.getDecText() + "&fov=" + Math.round(this.db.getFieldSize() * 60.0) + "&maglimit=" + this.db.getLimitingMag() + "&all=on\n");
        common.append("#TARGET=" + this.db.getStar() + "\n");
        if (kind == 1) {
            strb.append("#TYPE=VSDadmin1\n");
            strb.append((CharSequence)common);
            strb.append("#Label,RA h,RA m,RA s,Dec d,Dec m,Dec s,V,Verr,B-V,B-Verr,U-B,U-Berr,V-R,V-Rerr,R-I,R-Ierr,V-I,V-Ierr,Source,# Comments");
        } else if (kind == 2) {
            strb.append("#TYPE=VSDadmin2\n");
            strb.append((CharSequence)common);
            strb.append("#Label,RA h,RA m,RA s,Dec d,Dec m,Dec s,source");
            strb.append(",[filter triplets with name, mag, err]");
        } else if (kind == 3) {
            strb.append("#TYPE=VSDadmin3\n");
            strb.append((CharSequence)common);
            strb.append("#Label,RA h,RA m,RA s,Dec d,Dec m,Dec s,source");
            strb.append(",B-V,U mag,U err,B mag,B err,V mag,V err,R mag,R err,I mag,I err,SU mag,SU err,SG mag,SG err,SR mag,SR err,SI mag,SI err,SZ mag,SZ err,Y mag,Y err,# Comments");
        }
        return strb;
    }

    public String dataCheck(double data) {
        if (data == 99.999 || data == 9.999) {
            return "NA";
        }
        String dataString = this.threeDecimalFormat.format(data);
        if (dataString.contains(",")) {
            dataString = dataString.replace(",", ".");
        }
        return dataString;
    }

    public String getOutputInfo(int kind, int recordNumber) {
        this.db.getSexagesimalRA(recordNumber);
        this.db.getSexagesimalDEC(recordNumber);
        String ret = String.valueOf(this.db.getLabel(this.db.getVmag(recordNumber))) + "," + this.db.getRaHrs() + "," + this.db.getRaMins() + "," + this.db.getRaSecs() + "," + this.db.getDecSign() + this.db.getDecDegs() + "," + this.db.getDecMins() + "," + this.db.getDecSecs() + ",";
        if (kind == 1) {
            ret = String.valueOf(ret) + this.dataCheck(this.db.getVmag(recordNumber)) + "," + this.dataCheck(this.db.getEv(recordNumber)) + "," + this.dataCheck(this.db.getBMinusV(recordNumber)) + "," + this.dataCheck(this.db.getEbv(recordNumber)) + "," + this.dataCheck(this.db.getUMinusB(recordNumber)) + "," + this.dataCheck(this.db.getEub(recordNumber)) + "," + this.dataCheck(this.db.getVMinusR(recordNumber)) + "," + this.dataCheck(this.db.getEvr(recordNumber)) + "," + this.dataCheck(this.db.getRMinusI(recordNumber)) + "," + this.dataCheck(this.db.getEri(recordNumber)) + "," + this.dataCheck(this.db.getVMinusI(recordNumber)) + "," + this.dataCheck(this.db.getEvi(recordNumber)) + "," + this.db.getSource(recordNumber) + ",# ";
        } else if (kind == 2) {
            ret = String.valueOf(ret) + this.db.getSource(recordNumber) + ",";
            int i = 0;
            while (i < this.db.getFiltersSize(recordNumber)) {
                String fils = this.db.getFilters(recordNumber, i);
                ret = String.valueOf(ret) + " " + fils + ",";
                ++i;
            }
        } else if (kind == 3) {
            ret = String.valueOf(ret) + this.db.getSource(recordNumber) + "," + this.db.getBMinusV(recordNumber) + "," + this.db.getFilterX(recordNumber, "U") + "," + this.db.getFilterX(recordNumber, "B") + "," + this.db.getFilterX(recordNumber, "V") + "," + this.db.getFilterX(recordNumber, "R") + "," + this.db.getFilterX(recordNumber, "I") + "," + this.db.getFilterX(recordNumber, "SU") + "," + this.db.getFilterX(recordNumber, "SG") + "," + this.db.getFilterX(recordNumber, "SR") + "," + this.db.getFilterX(recordNumber, "SI") + "," + this.db.getFilterX(recordNumber, "SZ") + "," + this.db.getFilterX(recordNumber, "Y") + ",";
            ret = String.valueOf(ret) + "# ";
        }
        return ret;
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent cme) {
        this.mouseClicked = true;
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent event) {
        int xValue = event.getTrigger().getX();
        int yValue = event.getTrigger().getY();
        XYPlot plot = (XYPlot)this.chartPanel.getChart().getPlot();
        ChartRenderingInfo info = this.chartPanel.getChartRenderingInfo();
        Rectangle2D dataArea = info.getPlotInfo().getDataArea();
    }

    public void mouseDragged(MouseEvent event) {
        this.mouseDragged = true;
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if (this.mouseDragged.booleanValue()) {
            ++this.zoomlevel;
            if (this.zoomlevel == 10) {
                this.zoomlevel = 9;
            }
            this.dAxis = this.plot.getDomainAxis();
            this.rAxis = this.plot.getRangeAxis();
            this.domainUB[this.zoomlevel] = this.dAxis.getUpperBound();
            this.domainLB[this.zoomlevel] = this.dAxis.getLowerBound();
            this.rangeUB[this.zoomlevel] = this.rAxis.getUpperBound();
            this.rangeLB[this.zoomlevel] = this.rAxis.getLowerBound();
            this.mouseDragged = false;
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void displayData(int recordNumber, int seriesNumber) {
        if (seriesNumber == 3 && recordNumber != -1) {
            this.readout.setText(" Variable: " + this.db.getVarName(recordNumber) + this.getVarTypeLabel(this.db.getVarType(recordNumber)) + this.getMaxMinLabel(this.db.getVarMax(recordNumber), this.db.getVarMin(recordNumber)) + "  RA: " + this.db.getSexagesimalRA(recordNumber) + " (" + this.db.getRa(recordNumber) + "\u00b0)" + "   " + "Dec: " + this.db.getSexagesimalDEC(recordNumber) + " (" + this.db.getDec(recordNumber) + "\u00b0)" + "   " + "V: " + this.threeDecimalFormat.format(this.db.getVmag(recordNumber)) + "   " + "B-V: " + this.threeDecimalFormat.format(this.db.getBMinusV(recordNumber)) + "   " + "Source: " + this.db.getSource(recordNumber));
        } else if (recordNumber != -1) {
            this.readout.setText("  RA: " + this.db.getSexagesimalRA(recordNumber) + " (" + this.db.getRa(recordNumber) + "\u00b0)" + "   " + "Dec: " + this.db.getSexagesimalDEC(recordNumber) + " (" + this.db.getDec(recordNumber) + "\u00b0)" + "   " + "V: " + this.threeDecimalFormat.format(this.db.getVmag(recordNumber)) + "   " + "Verr: " + this.threeDecimalFormat.format(this.db.getEv(recordNumber)) + "   " + "B-V: " + this.threeDecimalFormat.format(this.db.getBMinusV(recordNumber)) + "   " + "B-Verr: " + this.threeDecimalFormat.format(this.db.getEbv(recordNumber)) + "   " + "N: " + this.db.getNobs(recordNumber) + "   " + "Source: " + this.db.getSource(recordNumber));
        } else {
            this.readout.setText("");
        }
    }

    public String getVarTypeLabel(String type) {
        if (type != null) {
            return "  Type: " + this.db.getVarType(this.recordNumber);
        }
        return "  Type: not specified";
    }

    public String getMaxMinLabel(String max, String min) {
        String s = "";
        s = min != null ? (min.contains("(") && min.contains(")") ? "  Mean Mag: " + max + "  Amplitude: " + min : "  Max: " + max + "  Min: " + min) : "  Mean Mag: " + max;
        return s;
    }

    private JButton setupButton(JButton jb) {
        jb.setPreferredSize(new Dimension(150, 30));
        jb.addActionListener(this);
        return jb;
    }

    public void setDefaultSeqplotColors() {
        // Warm pastel palette for a softer, more pleasant appearance
        this.dataColor[0] = new Color(135, 170, 230);  // Warm pastel blue (periwinkle)
        this.dataColor[1] = new Color(144, 215, 144);  // Warm pastel green (sage green)
        this.dataColor[2] = new Color(255, 140, 140);  // Warm pastel red (coral pink)
        this.dataColor[3] = new Color(200, 140, 220);  // Warm pastel purple (lavender)
        this.dataColor[4] = new Color(250, 245, 235);  // Warm white (cream/ivory)
        this.dataColor[5] = new Color(70, 70, 70);     // Warm dark gray (background)
        this.dataColor[6] = new Color(144, 215, 144);  // Sage green (crosshairs)
    }

    public JFreeChart getChart() {
        return this.chart;
    }

    public void getCrosshairPosition() {
        this.crosshairX = this.plot.getDomainCrosshairValue();
        this.crosshairY = this.plot.getRangeCrosshairValue();
        this.recordNumber = this.db.getRecordNumber(this.crosshairX, this.crosshairY);
        if (this.recordNumber != -1) {
            this.seriesNumber = this.db.getSeries(this.recordNumber);
        }
    }

    public double getDotsizeScaleFactor() {
        return this.dotsize;
    }

    public double getRelativeDotsizeScaleFactor() {
        return this.relativeDotsize;
    }

    public void setDotsizeScaleFactor(double newDotsize) {
        this.dotsize = newDotsize;
    }

    public void setRelativeDotsizeScaleFactor(double newRelativeDotsize) {
        this.relativeDotsize = newRelativeDotsize;
    }

    public String getStringSize(int x, int y) {
        return String.valueOf(this.customColorsText.substring(x, y)) + "E";
    }

    public String getMainTitleText() {
        return this.chartTitle;
    }

    public void setMainTitleText(String newMainTitle) {
        this.chartTitle = newMainTitle;
    }

    public String getSubtitleText() {
        return this.chartSubtitle;
    }

    public void setSubtitleText(String newSubtitle) {
        this.chartSubtitle = newSubtitle;
    }

    public void updateSubtitleText() {
        // Check if we're using StarPlotPanel (new system) or JFreeChart (old system)
        if (this.starPlotPanel != null) {
            // For StarPlotPanel, we don't need JFreeChart subtitle
            // Just store the subtitle text for display purposes
            System.out.println("DEBUG: Subtitle text updated for StarPlotPanel: " + this.getSubtitleText());
            return;
        }
        
        // For JFreeChart system, ensure subtitle is properly initialized
        if (this.subTitle == null) {
            System.out.println("DEBUG: Initializing subtitle as it was null");
            this.subTitle = new TextTitle(this.getSubtitleText(), new Font("Arial", 0, 11));
            
            // Add to chart if chart exists
            if (this.chart != null) {
                this.chart.addSubtitle(this.subTitle);
                System.out.println("DEBUG: Added subtitle to chart");
            }
        } else {
            // Update existing subtitle
            this.subTitle.setText(this.getSubtitleText());
        }
    }

    public String getUndo() {
        return this.undoText;
    }

    public Color getPlotColor(int series) {
        return this.dataColor[series];
    }

    public Color[] getColorArray() {
        return this.dataColor;
    }

    public Color getSavedColor(int series) {
        return this.savedColor[series];
    }

    public void setColor(int series, Color newColor) {
        this.dataColor[series] = newColor;
    }

    public void setSavedColor(Color[] oldColor) {
        int i = 0;
        while (i < 7) {
            this.savedColor[i] = oldColor[i];
            ++i;
        }
    }

    public int getColorArraySize() {
        return 7;
    }

    public void setShowPlot(Boolean setValue) {
        this.showPlot = setValue;
    }

    public Boolean getShowPlot() {
        return this.showPlot;
    }

    public String getVersion() {
        return VERSION;
    }

    public void setWaitCursor() {
        this.setCursor(Cursor.getPredefinedCursor(3));
    }

    public void setDefaultCursor() {
        this.setCursor(Cursor.getPredefinedCursor(0));
    }

    public void setFontSize(int f) {
        this.fontSize = f;
    }

    public int getFontSize() {
        return this.fontSize;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                Seqplot plot = new Seqplot();
            }
        });
    }
}

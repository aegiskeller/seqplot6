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
// // import java.awt.Taskbar; // Removed for Java 8 compatibility // Removed for Java 8 compatibility
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
    
    // Java 8 compatibility helper method to replace String.repeat()
    private static String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
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
    
    // Store original RA/Dec/scale from Request Star dialog for Reset View button
    private double originalRA = 0.0;
    private double originalDec = 0.0;
    private double originalFieldSize = 0.0;
    private Double[] domainLB = new Double[10];
    private Double[] domainUB = new Double[10];
    private Double[] rangeLB = new Double[10];
    private Double[] rangeUB = new Double[10];
    private String nextstarButtonLabel = "NEXT STAR";
    private String quitButtonLabel = "QUIT";
    private String resetButtonLabel = "RESET VIEW";
    private String saveButtonLabel = "SEND TO COMPS";
    private String skyViewButtonLabel = "SKY VIEW";
    private String pointsViewButtonLabel = "POINTS VIEW";
    private String showPointsButtonLabel = "SHOW POINTS";
    private String hidePointsButtonLabel = "HIDE POINTS";
    private String imageInvertButtonLabel = "INVERT IMAGE";
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
    private JButton skyViewButton;
    private JButton pointsViewButton;
    private JButton pointsToggleButton;
    private JButton imageInvertButton;
    private JComboBox<String> surveyComboBox;
    private JComboBox<String> resolutionComboBox;
    private String selectedSurvey = "CDS/P/DSS2/color"; // Default survey
    private int dss2Resolution = 512; // Default DSS2 image resolution
    
    // View state tracking
    private boolean isInSkyView = false;
    private boolean hasOriginalCoordinates = false; // Track if original coordinates are set
    private boolean isImageInverted = false;
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
    private JMenuItem photCompOption;
    private SequenceListWindow sequenceListWindow;
    private JTextArea readout;
    private JPanel centerPanel;
    private ValueAxis dAxis;
    private ValueAxis rAxis;
    private TextTitle mainTitle;
    private TextTitle subTitle;
    private DecimalFormat threeDecimalFormat = new DecimalFormat("0.0##", new java.text.DecimalFormatSymbols(java.util.Locale.US));
    private Container cp;
    private ChartPanel chartPanel;
    private Frame frame;
    private JFreeChart chart;
    private XYPlot plot;
    DataConnector db = new DataConnector(this);
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    public Seqplot() {
        super("Seqplot 6.0.0");
        
        // System.out.println("DEBUG: Constructor called, showPlot = " + this.showPlot);
        System.out.println("DEBUG: Constructor called, showPlot = " + this.showPlot);
        if (this.getShowPlot().booleanValue()) {
            // Set window icon (may not show in title bar on macOS, but good practice)
            try {
                java.net.URL iconURL = getClass().getResource("/AAVSOtools/seqplot_icon.png");
                if (iconURL != null) {
                    ImageIcon icon = new ImageIcon(iconURL);
                    this.setIconImage(icon.getImage());
                }
            } catch (Exception e) {
                // Silently ignore icon loading errors
            }
            
            // System.out.println("DEBUG: showPlot is true, creating window...");
            int wdth = (int)(this.dim.getHeight() * FRAME_WDTH_FACTOR) <= (int)this.dim.getWidth() ? (int)(this.dim.getHeight() * FRAME_WDTH_FACTOR) : (int)this.dim.getWidth();
            int height = (int)(this.dim.getHeight() * FRAME_HT_FACTOR);
            // System.out.println("DEBUG: Screen dimensions: " + this.dim.getWidth() + "x" + this.dim.getHeight());
            // System.out.println("DEBUG: Setting window size to: " + wdth + "x" + height);
            this.setSize(wdth, height);
            this.setMinimumSize(new Dimension(800, 600)); // Ensure minimum size
            this.setLocation(((int)this.dim.getWidth() - wdth) / 2, 0);
            // System.out.println("DEBUG: Window location set to: " + (((int)this.dim.getWidth() - wdth) / 2) + ", 0");
            this.cp = this.getContentPane();
            this.cp.setLayout(new BorderLayout());
            this.cp.setBackground(Color.WHITE); // Clean white background
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
            this.photCompOption = new JMenuItem("PhotComp");
            this.photCompOption.addActionListener(this);
            this.menuOptions.add(this.preferenceOption);
            this.menuOptions.add(this.printOption);
            this.menuOptions.add(this.savePlotOption);
            this.menuOptions.add(this.renameFileOption);
            this.menuOptions.add(this.photCompOption);
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
            this.saveButton.setToolTipText("Send the details of the selected star to the Comparison star table");
            this.downloadTableButton = new JButton(this.downloadTableText);
            this.setupButton(this.downloadTableButton);
            this.downloadTableButton.setToolTipText("Save the photometry for all stars in the current field");
            
            // Create view mode buttons
            this.skyViewButton = new JButton(this.skyViewButtonLabel);
            this.setupViewButton(this.skyViewButton, true);
            this.pointsViewButton = new JButton(this.pointsViewButtonLabel);
            this.setupViewButton(this.pointsViewButton, false);
            
            // Create points toggle button (only visible in sky view)
            this.pointsToggleButton = new JButton(this.hidePointsButtonLabel);
            this.setupButton(this.pointsToggleButton);
            this.pointsToggleButton.setVisible(false); // Hidden initially
            
            // Create image invert button (only visible in sky view)
            this.imageInvertButton = new JButton(this.imageInvertButtonLabel);
            this.setupButton(this.imageInvertButton);
            this.imageInvertButton.setVisible(false); // Hidden initially
            
            // Create survey selector combo box (only visible in sky view)
            String[] surveys = {"DSS2", "SDSS", "PanSTARRS", "2MASS", "GALEX", "AllWISE"};
            this.surveyComboBox = new JComboBox<>(surveys);
            this.surveyComboBox.setSelectedIndex(0); // Default to DSS2
            this.surveyComboBox.setBackground(Color.WHITE);
            this.surveyComboBox.setFont(new Font("Arial", Font.BOLD, 12));
            this.surveyComboBox.setVisible(false); // Hidden initially
            this.surveyComboBox.addActionListener(this);
            
            // Create resolution combo box (only visible in sky view)
            String[] resolutions = {"512px", "768px", "1024px"};
            this.resolutionComboBox = new JComboBox<>(resolutions);
            this.resolutionComboBox.setSelectedIndex(0); // Default to 512px
            this.resolutionComboBox.setBackground(Color.WHITE);
            this.resolutionComboBox.setFont(new Font("Arial", Font.BOLD, 12));
            this.resolutionComboBox.setVisible(false); // Hidden initially
            this.resolutionComboBox.addActionListener(this);
            
            JPanel spacerA = new JPanel();
            spacerA.setPreferredSize(new Dimension(150, 10));
            spacerA.setBackground(Color.LIGHT_GRAY); // Light gray background
            JPanel spacerB = new JPanel();
            spacerB.setPreferredSize(new Dimension(150, 10));
            spacerB.setBackground(Color.LIGHT_GRAY); // Light gray background
            
            JPanel buttonPanel = new JPanel(new GridLayout(13, 1, 0, 5));
            buttonPanel.setBackground(Color.LIGHT_GRAY); // Light gray background
            buttonPanel.add(this.resetButton);
            buttonPanel.add(this.nextstarButton);
            buttonPanel.add(this.quitButton);
            buttonPanel.add(spacerA);
            buttonPanel.add(this.downloadTableButton);
            buttonPanel.add(this.skyViewButton);
            buttonPanel.add(this.pointsViewButton);
            buttonPanel.add(this.pointsToggleButton);
            buttonPanel.add(this.imageInvertButton);
            buttonPanel.add(this.surveyComboBox);
            buttonPanel.add(this.resolutionComboBox);
            buttonPanel.add(spacerB);
            buttonPanel.add(this.saveButton);
            this.cp.add((Component)buttonPanel, "After");
            this.readout = new JTextArea();
            this.readout.setEditable(false);
            this.readout.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Gray border
            this.readout.setBackground(Color.WHITE); // White background
            this.readout.setForeground(Color.BLACK); // Black text
            this.readout.setPreferredSize(new Dimension(wdth - 30, 35));
            this.readout.setFont(new Font("Arial", 0, this.getFontSize()));
            this.readout.setLineWrap(true);
            this.readout.setWrapStyleWord(true);
            JScrollPane readoutScrollPane = new JScrollPane(this.readout);
            readoutScrollPane.setVerticalScrollBarPolicy(22);
            JPanel bottomPanel = new JPanel();
            bottomPanel.setBackground(Color.LIGHT_GRAY); // Light gray background
            bottomPanel.add(this.readout);
            this.cp.add((Component)bottomPanel, "Last");
            this.centerPanel = new JPanel();
            this.centerPanel.add(this.createPlotPanel());
            this.cp.add((Component)this.centerPanel, "Center");
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // System.out.println("DEBUG: About to call setVisible(true) in Seqplot constructor");
            this.setExtendedState(JFrame.NORMAL); // Ensure window is not minimized or maximized
            this.setVisible(true);
            this.setState(JFrame.NORMAL); // Ensure window is not minimized
            this.toFront();
            this.requestFocus();
            this.setAlwaysOnTop(true);
            // Brief delay to ensure window appears, then remove always-on-top
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // ignore
                }
                this.setAlwaysOnTop(false);
            });
            // System.out.println("DEBUG: Called setVisible(true), isVisible = " + this.isVisible());
            
            // Store initial coordinates for Reset View button
            SwingUtilities.invokeLater(() -> {
                this.storeOriginalCoordinates();
            });
        } else {
            // System.out.println("DEBUG: showPlot is false, not creating window");
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
        System.out.printf(java.util.Locale.US, "DEBUG: Setting axis ranges - X: %.6f to %.6f, Y: %.6f to %.6f\n", 
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
        System.out.printf(java.util.Locale.US, "DEBUG: Plot background color set to: %s\n", this.getPlotColor(5));
        this.plot.setRenderer(new XYBubbleRenderer(XYBubbleRenderer.SCALE_ON_BOTH_AXES));
        XYItemRenderer renderer = this.plot.getRenderer();
        int i = 0;
        while (i < 5) {
            Color seriesColor = this.getPlotColor(i);
            System.out.printf(java.util.Locale.US, "DEBUG: Series %d color set to: %s\n", i, seriesColor);
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
    private DSS2Manager dss2Manager;
    private boolean showDSS2 = false;
    private boolean showPoints = true;  // New: toggle for showing star points
    
    public StarPlotPanel createPlotPanel() {
        // System.out.println("DEBUG: Creating StarPlotPanel instead of ChartPanel");
        this.starPlotPanel = new StarPlotPanel(this.db, this);
        this.starPlotPanel.setPreferredSize(new Dimension((int)(this.dim.getWidth() * PLOT_WDTH_FACTOR), (int)(this.dim.getHeight() - 200.0)));
        
        // Initialize DSS2Manager
        this.dss2Manager = new DSS2Manager();
        
        // Note: We no longer need the JFreeChart creation
        return this.starPlotPanel;
    }
    
    /**
     * Get the StarPlotPanel instance
     */
    public StarPlotPanel getStarPlotPanel() {
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
            System.out.println("\n************ RESET VIEW BUTTON CLICKED ************");
            System.out.println("starPlotPanel != null: " + (this.starPlotPanel != null));
            System.out.println("hasOriginalCoordinates: " + this.hasOriginalCoordinates);
            if (this.hasOriginalCoordinates) {
                System.out.println("Original RA: " + this.originalRA);
                System.out.println("Original Dec: " + this.originalDec);
                System.out.println("Original FOV: " + this.originalFieldSize);
            }
            System.out.println("***************************************************\n");
            
            // Reset View: restore to original RA/Dec/scale from Request Star dialog
            if (this.starPlotPanel != null && this.hasOriginalCoordinates) {
                System.out.printf(java.util.Locale.US, "DEBUG: Reset View - restoring to original RA=%.6f, Dec=%.6f, FOV=%.4f\n",
                                 this.originalRA, this.originalDec, this.originalFieldSize);
                
                // Reset database to original coordinates
                this.db.setCentralRA(this.originalRA);
                this.db.setCentralDec(this.originalDec);
                this.db.setFieldSize(this.originalFieldSize);
                
                // Reset zoom level
                this.zoomlevel = 0;
                
                // Requery database at original coordinates and refresh display
                this.requeryDatabaseAtCoordinates(this.originalRA, this.originalDec);
                
                // Reset zoom and refresh plot (do this before fetching image)
                this.starPlotPanel.resetZoom();
                this.starPlotPanel.refreshPlotData();
                
                // If in Sky View mode, fetch DSS2 image at original coordinates
                if (this.starPlotPanel.isInSkyViewMode()) {
                    this.fetchDSS2ImageAtCoordinates(this.originalRA, this.originalDec);
                }
            } else if (this.starPlotPanel != null) {
                // Fallback: just zoom out if no original coordinates stored
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
            // System.out.println("DEBUG: Next star button pressed - querying database for new star");
            this.db.queryDatabase();
            if (!this.db.getQuitSelected().booleanValue() && !this.db.getCancelSelected().booleanValue()) {
                // Store original RA/Dec/fieldSize for Reset View button
                this.storeOriginalCoordinates();
                
                // Update title (only if using JFreeChart mode)
                if (this.mainTitle != null) {
                    this.mainTitle.setText(this.getMainTitleText());
                }
                String subtitle = "Data from the Calibration Database - limiting magnitude " + this.db.getLimitingMag() + 
                    " - VSX position matching tolerance " + String.format(java.util.Locale.US, "%.1f", this.db.getPositionTolerance() * 3600.0) + " arcseconds";
                this.setSubtitleText(subtitle);
                this.updateSubtitleText();
                this.db.scaleDots();
                
                // Reset zoom and refresh display for new star data
                if (this.starPlotPanel != null) {
                    // System.out.println("DEBUG: Refreshing StarPlotPanel for new star data");
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
                    String subtitle = "Data from the Calibration Database - limiting magnitude " + this.db.getLimitingMag() + 
                        " - VSX position matching tolerance " + String.format(java.util.Locale.US, "%.1f", this.db.getPositionTolerance() * 3600.0) + " arcseconds";
                    this.setSubtitleText(subtitle);
                    this.updateSubtitleText();
                    
                    // Show loading indicator while refreshing
                    if (this.starPlotPanel != null) {
                        this.starPlotPanel.setLoadingIndicator(true);
                        this.starPlotPanel.repaint(); // Force immediate repaint to show spinner
                    }
                    
                    // Refresh current field on background thread
                    System.out.println("DEBUG: Refreshing current field with new limiting magnitude: " + newLimitingMag);
                    new Thread(() -> {
                        try {
                            // Do database query on background thread
                            this.db.refreshCurrentField();
                            
                            // Update UI on EDT
                            javax.swing.SwingUtilities.invokeLater(() -> {
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
                                    // Hide loading indicator after refresh is complete
                                    this.starPlotPanel.setLoadingIndicator(false);
                                }
                                
                                System.out.println("DEBUG: Plot refreshed for limiting magnitude: " + newLimitingMag);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                if (this.starPlotPanel != null) {
                                    this.starPlotPanel.setLoadingIndicator(false);
                                }
                                JOptionPane.showMessageDialog(this.frame, 
                                    "Error refreshing data: " + e.getMessage(), 
                                    "Error", 
                                    JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    }).start();
                    
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
                        
                        // System.out.println("DEBUG: Font size changed to: " + newFontSize + " pt");
                        
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
            // Convert current tolerance from degrees to arcseconds for display
            double currentToleranceArcsec = this.db.getPositionTolerance() * 3600.0;
            
            String positionToleranceString = JOptionPane.showInputDialog(this.frame, 
                "Seqplot will look for a star within a certain distance from a catalog star\n" +
                "in order to decide if it is a variable or not. What value would you like to use\n" +
                "for this offset (tolerance)? The recommended value is 10.8 arcseconds.\n" +
                "(currently " + String.format(java.util.Locale.US, "%.1f", currentToleranceArcsec) + " arcseconds)", 
                "Set position tolerance", 3);
            
            if (positionToleranceString != null && !positionToleranceString.trim().equals("")) {
                try {
                    // Parse input as arcseconds and convert to degrees for internal storage
                    double newPositionToleranceArcsec = Double.parseDouble(positionToleranceString.trim());
                    double newPositionToleranceDeg = newPositionToleranceArcsec / 3600.0;
                    
                    // Set the new position tolerance (in degrees internally)
                    this.db.setPositionTolerance(newPositionToleranceDeg);
                    
                    // Update subtitle text to reflect new position tolerance (display in arcseconds)
                    String subtitle = "Data from the Calibration Database - limiting magnitude " + this.db.getLimitingMag() + 
                        " - VSX position matching tolerance " + String.format(java.util.Locale.US, "%.1f", newPositionToleranceArcsec) + " arcseconds";
                    this.setSubtitleText(subtitle);
                    this.updateSubtitleText();
                    
                    // Query database with new position tolerance to get fresh target data
                    // System.out.println("DEBUG: Querying database with new position tolerance: " + newPositionTolerance);
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
                            // System.out.println("DEBUG: Refreshing StarPlotPanel with new position tolerance data");
                            this.starPlotPanel.refreshPlotData();
                        }
                        
                        // System.out.println("DEBUG: Plot refreshed for position tolerance: " + newPositionTolerance);
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
        if (ae.getSource() == this.photCompOption) {
            // Check if multiple catalogs are loaded
            int totalStars = this.db.getTotalCount();
            Set<Integer> sources = new HashSet<>();
            for (int i = 0; i < totalStars; i++) {
                int src = this.db.getSource(i);
                if (src > 0) sources.add(src);
            }
            
            int secondaryCatalogSize = this.db.getSecondaryCatalogData().size();
            
            if (sources.size() < 2 && secondaryCatalogSize == 0) {
                JOptionPane.showMessageDialog(this,
                    "PhotComp requires at least 2 catalogs to be loaded.\n\n" +
                    "Currently loaded: " + sources.size() + " catalog(s) in main data, " +
                    secondaryCatalogSize + " stars in secondary data.\n\n" +
                    "Please:\n" +
                    "1. Select at least 2 catalog checkboxes (e.g., APASS9 + Gaia DR2)\n" +
                    "2. Click 'Request Star' to load the data\n" +
                    "3. Then try PhotComp again",
                    "Multiple Catalogs Required",
                    JOptionPane.WARNING_MESSAGE);
            } else {
                // Launch PhotComp window for photometric comparison
                new PhotCompWindow(this.db);
            }
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
            // Send selected star to sequence list
            int selectedStar = this.db.getSelectedStarIndex();
            if (selectedStar < 0) {
                JOptionPane.showMessageDialog(this, 
                    "Please click on a star in the plot first.", 
                    "No Star Selected", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Create window if needed
                if (this.sequenceListWindow == null || !this.sequenceListWindow.isVisible()) {
                    this.sequenceListWindow = new SequenceListWindow(this.db);
                    this.sequenceListWindow.setVisible(true);
                }
                // Add the star
                this.sequenceListWindow.addStar(selectedStar);
                this.sequenceListWindow.toFront();
            }
        }
        if (ae.getSource() == this.skyViewButton) {
            switchToSkyView();
        }
        if (ae.getSource() == this.pointsViewButton) {
            switchToPointsView();
        }
        if (ae.getSource() == this.pointsToggleButton) {
            togglePointsDisplay();
        }
        if (ae.getSource() == this.imageInvertButton) {
            toggleImageInversion();
        }
        if (ae.getSource() == this.surveyComboBox) {
            handleSurveyChange();
        }
        if (ae.getSource() == this.resolutionComboBox) {
            handleResolutionChange();
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
            String raText = this.db.getSexagesimalRA(recordNumber);
            String decText = this.db.getSexagesimalDEC(recordNumber);
            
            int source = this.db.getSource(recordNumber);
            String magLabel = "V";
            String vText = formatValueWithUncertainty(this.db.getVmag(recordNumber), this.db.getEv(recordNumber));
            
            // Prefer B-V if available, otherwise use V-I
            String colorLabel, bvText;
            double bMinusV = this.db.getBMinusV(recordNumber);
            if (bMinusV < 99.0) {
                // B-V is available
                colorLabel = "B-V";
                double bvUncertainty = computeBvUncertainty(recordNumber);
                bvText = formatValueWithUncertainty(bMinusV, bvUncertainty);
            } else {
                // B-V not available - use V-I
                colorLabel = "V-I";
                double vMinusI = this.db.getVMinusI(recordNumber);
                double evi = this.db.getEvi(recordNumber);
                bvText = formatValueWithUncertainty(vMinusI, evi);
            }
            this.readout.setText(" Variable: " + this.db.getVarName(recordNumber) + this.getVarTypeLabel(this.db.getVarType(recordNumber)) + this.getMaxMinLabel(this.db.getVarMax(recordNumber), this.db.getVarMin(recordNumber)) + "  RA: " + raText + "   Dec: " + decText + "   " + magLabel + ": " + vText + "   " + colorLabel + ": " + bvText + "   Source: " + getSourceName(source));
        } else if (recordNumber != -1) {
            // Check if there's a preferred catalog based on transition magnitude
            AAVSOtools.DataConnector.CatalogEntry preferred = this.db.getPreferredCatalogEntry(recordNumber);
            
            String raText, decText, vText, bvText, magLabel, colorLabel;
            int source, nobs;
            double vmag, ev, bMinusV, vMinusI, evi, bvUncertainty;
            
            if (preferred != null) {
                // Use preferred catalog data
                raText = formatRA(preferred.ra);
                decText = formatDec(preferred.dec);
                source = preferred.source;
                vmag = preferred.vmag;
                ev = preferred.ev;
                vMinusI = preferred.vMinusI;
                evi = preferred.evi;
                bMinusV = preferred.bMinusV;
                bvUncertainty = preferred.ebv;
                nobs = preferred.nobs; // Use catalog's nobs value
                
                // Labels based on color availability (prefer B-V if available)
                magLabel = "V";
                vText = formatValueWithUncertainty(vmag, ev);
                
                if (bMinusV < 99.0) {
                    // B-V is available - use it
                    colorLabel = "B-V";
                    bvText = formatValueWithUncertainty(bMinusV, bvUncertainty);
                } else {
                    // B-V not available - use V-I
                    colorLabel = "V-I";
                    bvText = formatValueWithUncertainty(vMinusI, evi);
                }
            } else {
                // Use primary catalog data
                raText = this.db.getSexagesimalRA(recordNumber);
                decText = this.db.getSexagesimalDEC(recordNumber);
                source = this.db.getSource(recordNumber);
                vmag = this.db.getVmag(recordNumber);
                ev = this.db.getEv(recordNumber);
                bMinusV = this.db.getBMinusV(recordNumber);
                bvUncertainty = computeBvUncertainty(recordNumber);
                nobs = this.db.getNobs(recordNumber);
                
                // Labels based on color availability (prefer B-V if available)
                magLabel = "V";
                vText = formatValueWithUncertainty(vmag, ev);
                
                if (bMinusV < 99.0) {
                    // B-V is available - use it
                    colorLabel = "B-V";
                    bvText = formatValueWithUncertainty(bMinusV, bvUncertainty);
                } else {
                    // B-V not available - use V-I
                    colorLabel = "V-I";
                    vMinusI = this.db.getVMinusI(recordNumber);
                    evi = this.db.getEvi(recordNumber);
                    bvText = formatValueWithUncertainty(vMinusI, evi);
                }
            }
            
            // Build basic readout text
            StringBuilder readoutText = new StringBuilder();
            readoutText.append("  RA: ").append(raText).append("   Dec: ").append(decText)
                      .append("   ").append(magLabel).append(": ").append(vText)
                      .append("   ").append(colorLabel).append(": ").append(bvText);
            
            // Show "N/A" for catalogs that don't provide nobs (SDSS=21, Tycho-2=1)
            // Skip showing N: for Tycho-2 entirely since it's always 0
            if (source == 21) {
                readoutText.append("   N: N/A");
            } else if (source != 1) {  // Skip N: for Tycho-2
                readoutText.append("   N: ").append(nobs);
            }
            
            readoutText.append("   Source: ").append(getSourceName(source));
            
            // Check for matches in other selected catalogs
            int primarySource = (preferred != null) ? preferred.source : this.db.getSource(recordNumber);
            double primaryRA = (preferred != null) ? preferred.ra : this.db.getRa(recordNumber);
            double primaryDec = (preferred != null) ? preferred.dec : this.db.getDec(recordNumber);
            java.util.List<AAVSOtools.DataConnector.CatalogEntry> crossMatches = 
                this.db.findCrossMatches(primaryRA, primaryDec, primarySource);
            
            if (!crossMatches.isEmpty()) {
                readoutText.append("  |  Also in: ");
                for (int i = 0; i < crossMatches.size(); i++) {
                    AAVSOtools.DataConnector.CatalogEntry match = crossMatches.get(i);
                    if (i > 0) readoutText.append(", ");
                    readoutText.append(match.name).append(" (");
                    if (match.source == 48) {
                        // Gaia DR2
                        readoutText.append("V=").append(String.format(java.util.Locale.US, "%.2f", match.vmag));
                        if (match.ev > 0 && match.ev < 10) {
                            readoutText.append(" (").append(String.format(java.util.Locale.US, "%.2f", match.ev)).append(")");
                        }
                        readoutText.append(" V-I=").append(String.format(java.util.Locale.US, "%.2f", match.vMinusI));
                        if (match.evi > 0 && match.evi < 10) {
                            readoutText.append(" (").append(String.format(java.util.Locale.US, "%.2f", match.evi)).append(")");
                        }
                    } else if (match.source == 46) {
                        // PanSTARRS DR2
                        readoutText.append("V=").append(String.format(java.util.Locale.US, "%.2f", match.vmag));
                        if (match.ev > 0 && match.ev < 10) {
                            readoutText.append(" (").append(String.format(java.util.Locale.US, "%.2f", match.ev)).append(")");
                        }
                        readoutText.append(" V-I=").append(String.format(java.util.Locale.US, "%.2f", match.vMinusI));
                        if (match.evi > 0 && match.evi < 10) {
                            readoutText.append(" (").append(String.format(java.util.Locale.US, "%.2f", match.evi)).append(")");
                        }
                    } else {
                        // Other catalogs (e.g., Tycho-2)
                        readoutText.append("V=").append(String.format(java.util.Locale.US, "%.2f", match.vmag));
                        if (match.ev > 0 && match.ev < 10) {
                            readoutText.append(" (").append(String.format(java.util.Locale.US, "%.2f", match.ev)).append(")");
                        }
                        readoutText.append(" B-V=").append(String.format(java.util.Locale.US, "%.2f", match.bMinusV));
                        if (match.ebv > 0 && match.ebv < 10) {
                            readoutText.append(" (").append(String.format(java.util.Locale.US, "%.2f", match.ebv)).append(")");
                        }
                    }
                    readoutText.append(")");
                }
            }
            
            this.readout.setText(readoutText.toString());
        } else {
            this.readout.setText("");
        }
    }

    private String formatRA(double raDegrees) {
        // Convert decimal degrees to hours
        double raHours = raDegrees / 15.0;
        int hours = (int) raHours;
        double minutesDecimal = (raHours - hours) * 60.0;
        int minutes = (int) minutesDecimal;
        double seconds = (minutesDecimal - minutes) * 60.0;
        return String.format(java.util.Locale.US, "%02dh %02dm %.1fs", hours, minutes, seconds);
    }

    private String formatDec(double decDegrees) {
        // Format as degrees, arcminutes, arcseconds
        String sign = (decDegrees >= 0) ? "+" : "";
        double absDecDegrees = Math.abs(decDegrees);
        int degrees = (int) absDecDegrees;
        double arcminutesDecimal = (absDecDegrees - degrees) * 60.0;
        int arcminutes = (int) arcminutesDecimal;
        double arcseconds = (arcminutesDecimal - arcminutes) * 60.0;
        return String.format(java.util.Locale.US, "%s%d° %02d′ %.1f″", sign, (decDegrees < 0 ? -degrees : degrees), arcminutes, arcseconds);
    }

    private String formatValueWithUncertainty(double value, double uncertainty) {
        if (!isValidMeasurement(value)) {
            return "N/A";
        }
        String formattedValue = this.threeDecimalFormat.format(value);
        if (isValidUncertainty(uncertainty)) {
            return formattedValue + " (" + this.threeDecimalFormat.format(uncertainty) + ")";
        }
        return formattedValue;
    }

    private double computeBvUncertainty(int recordNumber) {
        double vErr = this.db.getEv(recordNumber);
        double bErr = this.db.getE_Bmag(recordNumber);
        boolean vValid = isValidUncertainty(vErr);
        boolean bValid = isValidUncertainty(bErr);
        if (vValid && bValid) {
            return Math.sqrt(vErr * vErr + bErr * bErr);
        }
        double bvErr = this.db.getEbv(recordNumber);
        if (isValidUncertainty(bvErr)) {
            return bvErr;
        }
        return Double.NaN;
    }

    private boolean isValidMeasurement(double value) {
        return !Double.isNaN(value) && Math.abs(value) < 90.0;
    }

    private boolean isValidUncertainty(double uncertainty) {
        return !Double.isNaN(uncertainty) && uncertainty > 0.0 && uncertainty < 90.0;
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
        jb.setPreferredSize(new Dimension(150, 35));
        jb.addActionListener(this);
        
        // Fluent design styling
        jb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jb.setFocusPainted(false);
        jb.setContentAreaFilled(false);
        jb.setOpaque(false);
        jb.setBorder(new FluentBorder());
        jb.setBackground(Color.LIGHT_GRAY);  // Light gray background
        jb.setForeground(Color.BLACK); // Black text
        
        // Override paintComponent for custom rendering
        jb.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
                javax.swing.JButton button = (javax.swing.JButton) c;
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Get hover and press states
                Float hoverProgress = (Float) button.getClientProperty("hoverProgress");
                Float pressScale = (Float) button.getClientProperty("pressScale");
                
                if (hoverProgress == null) hoverProgress = 0.0f;
                if (pressScale == null) pressScale = 1.0f;
                
                int width = c.getWidth();
                int height = c.getHeight();
                
                // Apply scaling for press animation
                if (pressScale < 1.0f) {
                    int centerX = width / 2;
                    int centerY = height / 2;
                    g2.translate(centerX, centerY);
                    g2.scale(pressScale, pressScale);
                    g2.translate(-centerX, -centerY);
                }
                
                // Draw background with rounded corners
                java.awt.Color bgColor = button.getBackground();
                if (bgColor != null) {
                    g2.setColor(bgColor);
                    g2.fillRoundRect(2, 2, width - 4, height - 4, 8, 8);
                }
                
                g2.dispose();
                
                // Paint text and other components
                super.paint(g, c);
            }
        });
        
        // Add fluent hover and press effects
        jb.addMouseListener(new java.awt.event.MouseAdapter() {
            private javax.swing.Timer hoverTimer;
            private javax.swing.Timer pressTimer;
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (jb.isEnabled()) {
                    animateHover(jb, true);
                }
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (jb.isEnabled()) {
                    animateHover(jb, false);
                }
            }
            
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (jb.isEnabled()) {
                    animatePress(jb, true);
                }
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (jb.isEnabled()) {
                    animatePress(jb, false);
                }
            }
            
            private void animateHover(JButton btn, boolean entering) {
                if (hoverTimer != null) hoverTimer.stop();
                
                hoverTimer = new javax.swing.Timer(15, new java.awt.event.ActionListener() {
                    private int step = 0;
                    private final int totalSteps = 10;
                    
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        float progress = (float) step / totalSteps;
                        if (!entering) progress = 1.0f - progress;
                        
                        int rgb = (int) (245 - (25 * progress));
                        Color newColor = new Color(rgb, rgb, rgb);
                        btn.setBackground(newColor);
                        btn.putClientProperty("hoverProgress", progress);
                        btn.repaint();
                        
                        step++;
                        if (step > totalSteps) {
                            hoverTimer.stop();
                        }
                    }
                });
                hoverTimer.start();
            }
            
            private void animatePress(JButton btn, boolean pressing) {
                if (pressTimer != null) pressTimer.stop();
                
                pressTimer = new javax.swing.Timer(8, new java.awt.event.ActionListener() {
                    private int step = 0;
                    private final int totalSteps = 6;
                    
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        float progress = (float) step / totalSteps;
                        if (!pressing) progress = 1.0f - progress;
                        
                        float scale = 1.0f - (0.04f * progress);
                        btn.putClientProperty("pressScale", scale);
                        btn.repaint();
                        
                        step++;
                        if (step > totalSteps) {
                            pressTimer.stop();
                            if (!pressing) {
                                btn.putClientProperty("pressScale", 1.0f);
                                btn.repaint();
                            }
                        }
                    }
                });
                pressTimer.start();
            }
        });
        
        return jb;
    }
    
    private JButton setupViewButton(JButton jb, boolean isPrimary) {
        jb.setPreferredSize(new Dimension(150, 40));
        jb.addActionListener(this);
        
        // Fluent design view button styling
        jb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jb.setFocusPainted(false);
        jb.setContentAreaFilled(false);
        jb.setOpaque(false);
        jb.setBorder(new FluentToggleBorder());
        
        if (isPrimary) {
            // Sky View button - highlighted
            jb.setBackground(Color.LIGHT_GRAY);
            jb.setForeground(Color.BLACK);
        } else {
            // Points View button - normal
            jb.setBackground(Color.WHITE);
            jb.setForeground(Color.BLACK);
        }
        
        // Override paintComponent for custom rendering
        jb.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
                javax.swing.JButton button = (javax.swing.JButton) c;
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Get hover and press states
                Float hoverProgress = (Float) button.getClientProperty("hoverProgress");
                Float pressScale = (Float) button.getClientProperty("pressScale");
                
                if (hoverProgress == null) hoverProgress = 0.0f;
                if (pressScale == null) pressScale = 1.0f;
                
                int width = c.getWidth();
                int height = c.getHeight();
                
                // Apply scaling for press animation
                if (pressScale < 1.0f) {
                    int centerX = width / 2;
                    int centerY = height / 2;
                    g2.translate(centerX, centerY);
                    g2.scale(pressScale, pressScale);
                    g2.translate(-centerX, -centerY);
                }
                
                // Draw background with rounded corners and gradient
                java.awt.Color bgColor = button.getBackground();
                if (bgColor != null) {
                    // Add subtle gradient effect
                    java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                        0, 0, bgColor,
                        0, height, new java.awt.Color(
                            Math.max(0, bgColor.getRed() - 15),
                            Math.max(0, bgColor.getGreen() - 15),
                            Math.max(0, bgColor.getBlue() - 15)
                        )
                    );
                    g2.setPaint(gradient);
                    g2.fillRoundRect(2, 2, width - 4, height - 4, 10, 10);
                }
                
                g2.dispose();
                
                // Paint text and other components
                super.paint(g, c);
            }
        });
        
        // Add fluent toggle effects
        jb.addMouseListener(new java.awt.event.MouseAdapter() {
            private javax.swing.Timer hoverTimer;
            private javax.swing.Timer pressTimer;
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (jb.isEnabled()) {
                    animateToggleHover(jb, true, isPrimary);
                }
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (jb.isEnabled()) {
                    animateToggleHover(jb, false, isPrimary);
                }
            }
            
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (jb.isEnabled()) {
                    animateTogglePress(jb, true);
                }
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (jb.isEnabled()) {
                    animateTogglePress(jb, false);
                }
            }
            
            private void animateToggleHover(JButton btn, boolean entering, boolean isPrimary) {
                if (hoverTimer != null) hoverTimer.stop();
                
                Color baseColor = isPrimary ? Color.LIGHT_GRAY : Color.WHITE;
                Color hoverColor = isPrimary ? Color.GRAY : Color.LIGHT_GRAY;
                
                hoverTimer = new javax.swing.Timer(12, new java.awt.event.ActionListener() {
                    private int step = 0;
                    private final int totalSteps = 12;
                    
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        float progress = (float) step / totalSteps;
                        if (!entering) progress = 1.0f - progress;
                        
                        int r = (int) (baseColor.getRed() + (hoverColor.getRed() - baseColor.getRed()) * progress);
                        int g = (int) (baseColor.getGreen() + (hoverColor.getGreen() - baseColor.getGreen()) * progress);
                        int b = (int) (baseColor.getBlue() + (hoverColor.getBlue() - baseColor.getBlue()) * progress);
                        
                        Color newColor = new Color(r, g, b);
                        btn.setBackground(newColor);
                        btn.putClientProperty("hoverProgress", progress);
                        btn.repaint();
                        
                        step++;
                        if (step > totalSteps) {
                            hoverTimer.stop();
                        }
                    }
                });
                hoverTimer.start();
            }
            
            private void animateTogglePress(JButton btn, boolean pressing) {
                if (pressTimer != null) pressTimer.stop();
                
                pressTimer = new javax.swing.Timer(6, new java.awt.event.ActionListener() {
                    private int step = 0;
                    private final int totalSteps = 8;
                    
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        float progress = (float) step / totalSteps;
                        if (!pressing) progress = 1.0f - progress;
                        
                        float scale = 1.0f - (0.06f * progress);
                        btn.putClientProperty("pressScale", scale);
                        btn.repaint();
                        
                        step++;
                        if (step > totalSteps) {
                            pressTimer.stop();
                            if (!pressing) {
                                btn.putClientProperty("pressScale", 1.0f);
                                btn.repaint();
                            }
                        }
                    }
                });
                pressTimer.start();
            }
        });
        
        return jb;
    }

    public void setDefaultSeqplotColors() {
        // Standard astronomical data colors (critical for star classification) - DO NOT CHANGE
        this.dataColor[0] = Color.BLUE;                // Series 0 - Blue stars
        this.dataColor[1] = Color.GREEN;               // Series 1 - Green stars  
        this.dataColor[2] = Color.RED;                 // Series 2 - Red stars
        this.dataColor[3] = Color.YELLOW;              // Series 3 - Yellow stars
        this.dataColor[4] = Color.WHITE;               // Series 4 - White stars
        // Basic clear UI colors for better visibility
        this.dataColor[5] = Color.BLACK;               // Background
        this.dataColor[6] = Color.GRAY;                // Crosshairs
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
    
    public void setReadoutText(String text) {
        if (this.readout != null) {
            this.readout.setText(text);
        }
    }

    public void updateSubtitleText() {
        // Check if we're using StarPlotPanel (new system) or JFreeChart (old system)
        if (this.starPlotPanel != null) {
            // For StarPlotPanel, we don't need JFreeChart subtitle
            // Just store the subtitle text for display purposes
            // System.out.println("DEBUG: Subtitle text updated for StarPlotPanel: " + this.getSubtitleText());
            return;
        }
        
        // For JFreeChart system, ensure subtitle is properly initialized
        if (this.subTitle == null) {
            // System.out.println("DEBUG: Initializing subtitle as it was null");
            this.subTitle = new TextTitle(this.getSubtitleText(), new Font("Arial", 0, 11));
            
            // Add to chart if chart exists
            if (this.chart != null) {
                this.chart.addSubtitle(this.subTitle);
                // System.out.println("DEBUG: Added subtitle to chart");
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

    /**
     * Toggle DSS2 background image display
     */
    private void switchToSkyView() {
        if (isInSkyView) return; // Already in sky view
        
        isInSkyView = true;
        skyViewButton.setEnabled(false);
        skyViewButton.setBackground(new Color(25, 65, 40)); // Dark malachite grayed out
        pointsViewButton.setEnabled(true);
        pointsViewButton.setBackground(new Color(35, 183, 115)); // Restore deep malachite
        pointsToggleButton.setVisible(true);
        imageInvertButton.setVisible(true); // Show invert button in sky view
        surveyComboBox.setVisible(true); // Show survey selector in sky view
        resolutionComboBox.setVisible(true); // Show resolution selector in sky view
        
        // Update PanSTARRS availability based on current declination
        updatePanSTARRSAvailability();
        
        // System.out.println("DEBUG: Switching to Sky View");
        
        // Show loading indicator with "Retrieving image" message
        if (starPlotPanel != null) {
            starPlotPanel.setLoadingIndicator(true, "Retrieving image");
        }
        
        // Request DSS2 image for current field
        if (starPlotPanel != null && db != null) {
            // Get field center and extent from database
            double centerRA = db.getCenterRa();
            double centerDec = db.getCenterDec();
            double minRA = db.getMinX();
            double maxRA = db.getMaxX();
            double minDec = db.getMinY();
            double maxDec = db.getMaxY();
            
            // Calculate appropriate field of view to match the star field exactly
            double fov = DSS2Manager.calculateFieldOfView(minRA, maxRA, minDec, maxDec, 1.1);
            
            System.out.printf(java.util.Locale.US, "DEBUG: DSS2 request - Center: RA=%.6f, Dec=%.6f, FOV=%.4f\n", 
                             centerRA, centerDec, fov);
            
            // Force fresh request - clear any existing cache first
            dss2Manager.clearImage();
            
            // Set up load listener BEFORE requesting image to avoid race condition
            dss2Manager.setLoadListener(new DSS2Manager.DSS2LoadListener() {
                @Override
                public void onImageLoaded(java.awt.image.BufferedImage image) {
                    // Pass image and DSS2Manager to StarPlotPanel for WCS support
                    if (starPlotPanel != null) {
                        starPlotPanel.setDSS2Background(image, dss2Manager);
                        starPlotPanel.setLoadingIndicator(false);
                        // System.out.println("DEBUG: DSS2 image set in StarPlotPanel with WCS support");
                    }
                }
                
                @Override
                public void onImageLoadFailed(String error) {
                    System.err.printf(java.util.Locale.US, "DSS2 load failed: %s\n", error);
                    if (starPlotPanel != null) {
                        starPlotPanel.setLoadingIndicator(false);
                    }
                    
                    // Check error type and show appropriate message
                    if (error != null) {
                        String errorLower = error.toLowerCase();
                        
                        // Check for no coverage (blank image or explicit message)
                        if (errorLower.contains("no survey coverage") || 
                            errorLower.contains("blank") || 
                            errorLower.contains("no coverage")) {
                            javax.swing.JOptionPane.showMessageDialog(frame, 
                                "No Survey Coverage\n\nThe selected survey has no data for this field.\nPlease select another survey.", 
                                "No Coverage", 
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        }
                        // Check for HTTP 500 or service errors
                        else if (errorLower.contains("http error 500") || 
                                 errorLower.contains("internal server error") ||
                                 errorLower.contains("service") && errorLower.contains("down")) {
                            javax.swing.JOptionPane.showMessageDialog(frame, 
                                "No Survey Coverage\n\nThe selected survey has no data for this field\nor the service may be temporarily unavailable.\nPlease select another survey.", 
                                "No Coverage", 
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        }
                        // Other errors (HTTP 404, network issues, etc.)
                        else if (errorLower.contains("http error") || 
                                 errorLower.contains("timeout") ||
                                 errorLower.contains("connection")) {
                            javax.swing.JOptionPane.showMessageDialog(frame, 
                                "No Survey Coverage\n\nCould not load survey data for this field.\nPlease select another survey or check your connection.", 
                                "No Coverage", 
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        }
                        // Generic error fallback
                        else {
                            javax.swing.JOptionPane.showMessageDialog(frame, 
                                "No Survey Coverage\n\nThe selected survey has no data for this field.\nPlease select another survey.", 
                                "No Coverage", 
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    
                    // Reset to DSS2 (default survey) and reload Sky View
                    surveyComboBox.setSelectedIndex(0); // Select DSS2
                    selectedSurvey = "CDS/P/DSS2/color";
                    
                    // Reload Sky View with DSS2
                    if (dss2Manager != null) {
                        double currentRA = db.getCentralRA();
                        double currentDec = db.getCentralDec();
                        double currentFOV = db.getFieldSize();
                        
                        System.out.println("DEBUG: Reloading Sky View with default DSS2 survey after error");
                        dss2Manager.fetchDSS2Image(currentRA, currentDec, currentFOV, dss2Resolution, "CDS/P/DSS2/color");
                    }
                }
            });
            
            // Now request survey image at current resolution and selected survey
            // (listener is already set up above to avoid race condition)
            dss2Manager.fetchDSS2Image(centerRA, centerDec, fov, dss2Resolution, selectedSurvey);
        }
    }
    
    private void switchToPointsView() {
        isInSkyView = false;
        skyViewButton.setEnabled(true);
        skyViewButton.setBackground(new Color(52, 235, 143)); // Restore bright malachite
        pointsViewButton.setEnabled(false);
        pointsViewButton.setBackground(new Color(25, 65, 40)); // Dark malachite grayed out
        pointsToggleButton.setVisible(false);
        imageInvertButton.setVisible(false); // Hide invert button in points view
        surveyComboBox.setVisible(false); // Hide survey selector in points view
        resolutionComboBox.setVisible(false); // Hide resolution selector in points view
        
        // System.out.println("DEBUG: Switching to Points View");
        
        // Clear DSS2 image from display
        if (starPlotPanel != null) {
            starPlotPanel.clearDSS2Background();
        }
    }

    private void togglePointsDisplay() {
        showPoints = !showPoints;
        
        if (showPoints) {
            pointsToggleButton.setText("HIDE POINTS");
            // System.out.println("DEBUG: Star points enabled");
        } else {
            pointsToggleButton.setText("SHOW POINTS");
            // System.out.println("DEBUG: Star points disabled");
        }
        
        // Update StarPlotPanel with new points visibility
        if (starPlotPanel != null) {
            starPlotPanel.setPointsVisible(showPoints);
        }
    }

    private void toggleImageInversion() {
        isImageInverted = !isImageInverted;
        
        // System.out.println("DEBUG: Image inversion toggled - inverted: " + isImageInverted);
        
        // Update StarPlotPanel with new inversion state
        if (starPlotPanel != null) {
            starPlotPanel.setImageInverted(isImageInverted);
        }
    }

    /**
     * Update PanSTARRS availability based on current declination
     * PanSTARRS has no coverage for Dec < -30°
     */
    private void updatePanSTARRSAvailability() {
        if (db == null) return;
        
        double centerDec = db.getCenterDec();
        boolean isPanSTARRSAvailable = centerDec >= -30.0;
        
        // Find PanSTARRS item in combo box and enable/disable it
        for (int i = 0; i < surveyComboBox.getItemCount(); i++) {
            String item = surveyComboBox.getItemAt(i);
            if ("PanSTARRS".equals(item)) {
                // Can't directly disable items in JComboBox, so we use a custom renderer
                // For now, we'll just log the availability
                if (!isPanSTARRSAvailable && "PanSTARRS".equals(surveyComboBox.getSelectedItem())) {
                    // Switch to DSS2 if PanSTARRS is selected but unavailable
                    surveyComboBox.setSelectedIndex(0); // DSS2
                    System.out.println("DEBUG: PanSTARRS not available for Dec < -30°, switching to DSS2");
                }
                break;
            }
        }
    }
    
    /**
     * Handle survey selection change
     */
    private void handleSurveyChange() {
        String selected = (String) surveyComboBox.getSelectedItem();
        if (selected == null) return;
        
        // Map display names to CDS HiPS survey codes
        String newSurvey;
        switch (selected) {
            case "DSS2":
                newSurvey = "CDS/P/DSS2/color";
                break;
            case "SDSS":
                newSurvey = "CDS/P/SDSS9/color-alt"; // Alternative SDSS9 color composite
                break;
            case "PanSTARRS":
                newSurvey = "CDS/P/PanSTARRS/DR1/color";
                // Check declination coverage
                if (db != null && db.getCenterDec() < -30.0) {
                    System.out.println("WARNING: PanSTARRS has no coverage for Dec < -30°");
                    javax.swing.JOptionPane.showMessageDialog(frame, 
                        "No Survey Coverage\n\nPanSTARRS has no coverage for declinations below -30°.\nSwitching to DSS2.", 
                        "No Coverage", 
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    surveyComboBox.setSelectedIndex(0); // Switch back to DSS2
                    return;
                }
                break;
            case "2MASS":
                newSurvey = "CDS/P/2MASS/color";
                break;
            case "GALEX":
                newSurvey = "CDS/P/GALEXGR6/AIS/color";
                break;
            case "AllWISE":
                newSurvey = "CDS/P/allWISE/color";
                break;
            default:
                newSurvey = "CDS/P/DSS2/color";
                break;
        }
        
        if (!newSurvey.equals(selectedSurvey)) {
            selectedSurvey = newSurvey;
            System.out.printf(java.util.Locale.US, "DEBUG: Survey changed to %s (%s)\n", selected, selectedSurvey);
            
            // Re-fetch image at new survey if in sky view
            if (isInSkyView && starPlotPanel != null && db != null) {
                double centerRA = db.getCenterRa();
                double centerDec = db.getCenterDec();
                double minRA = db.getMinX();
                double maxRA = db.getMaxX();
                double minDec = db.getMinY();
                double maxDec = db.getMaxY();
                double fov = DSS2Manager.calculateFieldOfView(minRA, maxRA, minDec, maxDec, 1.1);
                
                starPlotPanel.setLoadingIndicator(true, "Retrieving image");
                dss2Manager.clearImage();
                dss2Manager.fetchDSS2Image(centerRA, centerDec, fov, dss2Resolution, selectedSurvey);
            }
        }
    }

    private void handleResolutionChange() {
        String selected = (String) resolutionComboBox.getSelectedItem();
        if (selected == null) return;
        
        // Parse resolution from "512px" format
        int newResolution = Integer.parseInt(selected.replace("px", ""));
        
        if (newResolution != dss2Resolution) {
            dss2Resolution = newResolution;
            System.out.printf(java.util.Locale.US, "DEBUG: Resolution changed to %dpx\n", dss2Resolution);
            
            // Re-fetch DSS2 image at new resolution if in sky view
            if (isInSkyView && starPlotPanel != null && db != null) {
                double centerRA = db.getCenterRa();
                double centerDec = db.getCenterDec();
                double minRA = db.getMinX();
                double maxRA = db.getMaxX();
                double minDec = db.getMinY();
                double maxDec = db.getMaxY();
                double fov = DSS2Manager.calculateFieldOfView(minRA, maxRA, minDec, maxDec, 1.1);
                
                starPlotPanel.setLoadingIndicator(true, "Retrieving image");
                dss2Manager.clearImage();
                dss2Manager.fetchDSS2Image(centerRA, centerDec, fov, dss2Resolution, selectedSurvey);
            }
        }
    }

    // Getter method for StarPlotPanel to check if points should be visible
    public boolean isShowPoints() {
        return showPoints;
    }
    
    // Override setVisible to ensure proper window state management
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            // System.out.println("DEBUG: setVisible(true) called - ensuring proper window state");
            
            // Ensure window is not minimized or iconified
            this.setExtendedState(JFrame.NORMAL);
            
            // Call parent setVisible first
            super.setVisible(true);
            
            // Force window to front and request focus
            this.toFront();
            this.requestFocus();
            
            // Ensure proper size if not already set
            if (this.getWidth() < 800 || this.getHeight() < 600) {
                // System.out.println("DEBUG: Window too small, resizing to minimum");
                this.setSize(Math.max(this.getWidth(), 800), Math.max(this.getHeight(), 600));
            }
            
            // System.out.println("DEBUG: Window state after setVisible - size: " + this.getWidth() + "x" + this.getHeight() + ", visible: " + this.isVisible() + ", state: " + this.getExtendedState());
        } else {
            super.setVisible(false);
        }
    }
    
    /**
     * Store the current coordinates as original for Reset View button
     */
    private void storeOriginalCoordinates() {
        if (this.db != null) {
            this.originalRA = this.db.getCentralRA();
            this.originalDec = this.db.getCentralDec();
            this.originalFieldSize = this.db.getFieldSize();
            this.hasOriginalCoordinates = true;
            System.out.println("\n*** STORED ORIGINAL COORDINATES FOR RESET VIEW ***");
            System.out.printf(java.util.Locale.US, "    RA=%.6f, Dec=%.6f, FOV=%.4f\n",
                             this.originalRA, this.originalDec, this.originalFieldSize);
            System.out.println("***************************************************\n");
        }
    }

    public static void main(String[] args) {
        // Set macOS application properties
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Seqplot");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        
        // Set dock icon early (before any windows are created)
        try {
            java.net.URL iconURL = Seqplot.class.getResource("/AAVSOtools/seqplot_icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                if (icon.getIconWidth() > 0) {
                    // Set dock icon using reflection for Java 8 compatibility
                    try {
                        Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
                        Object application = applicationClass.getMethod("getApplication").invoke(null);
                        applicationClass.getMethod("setDockIconImage", java.awt.Image.class).invoke(application, icon.getImage());
                        System.out.println("✅ Seqplot dock icon set successfully (macOS)");
                    } catch (Exception e) {
                        // Taskbar API not available (Java 8) or not on macOS - silently continue
                        System.out.println("ℹ️ Dock icon setting not available on this platform/Java version");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Note: Could not set dock icon - " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                System.out.println("DEBUG: Creating Seqplot instance...");
                Seqplot plot = new Seqplot();
                System.out.println("DEBUG: Seqplot created, showPlot = " + plot.getShowPlot());
                // System.out.println("DEBUG: Seqplot isVisible = " + plot.isVisible());
                
                // Make sure the window stays on top and visible
                plot.toFront();
                plot.requestFocus();
                plot.setAlwaysOnTop(true);
                try {
                    Thread.sleep(1000); // Give time for window to appear
                    plot.setAlwaysOnTop(false); // Remove always on top after a moment
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        });
    }
    
    // Custom fluent design border for regular buttons
    private static class FluentBorder implements javax.swing.border.Border {
        @Override
        public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            
            javax.swing.JButton button = (javax.swing.JButton) c;
            
            // Get hover and press states
            Float hoverProgress = (Float) button.getClientProperty("hoverProgress");
            Float pressScale = (Float) button.getClientProperty("pressScale");
            
            if (hoverProgress == null) hoverProgress = 0.0f;
            if (pressScale == null) pressScale = 1.0f;
            
            // Draw subtle border with rounded corners
            java.awt.Color borderColor = new java.awt.Color(200, 200, 200, 120 + (int)(60 * hoverProgress));
            if (!button.isEnabled()) {
                borderColor = new java.awt.Color(150, 150, 150, 80);
            }
            
            g2.setColor(borderColor);
            g2.setStroke(new java.awt.BasicStroke(1.2f));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 8, 8);
            
            g2.dispose();
        }
        
        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return new java.awt.Insets(6, 12, 6, 12);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
    
    // Custom fluent design border for toggle buttons
    private static class FluentToggleBorder implements javax.swing.border.Border {
        @Override
        public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            
            javax.swing.JButton button = (javax.swing.JButton) c;
            
            // Get hover and press states
            Float hoverProgress = (Float) button.getClientProperty("hoverProgress");
            if (hoverProgress == null) hoverProgress = 0.0f;
            
            // Draw subtle border with rounded corners
            java.awt.Color borderColor = new java.awt.Color(255, 255, 255, 60 + (int)(40 * hoverProgress));
            if (!button.isEnabled()) {
                borderColor = new java.awt.Color(200, 200, 200, 100);
            }
            
            g2.setColor(borderColor);
            g2.setStroke(new java.awt.BasicStroke(1.5f));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 10, 10);
            
            g2.dispose();
        }
        
        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return new java.awt.Insets(8, 14, 8, 14);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
    
    /**
     * Get source name from source number
     */
    private String getSourceName(int sourceNumber) {
        switch (sourceNumber) {
            case 1: return "Tycho-2";  // AAVSO table containing Tycho-2 data
            case 21: return "SDSS-DR12";
            case 29: return "APASS";
            case 46: return "PanSTARRS";
            case 48: return "Gaia DR2";
            case 901: return "Tycho-2";
            default: 
                // AAVSO catalogs (1-44)
                if (sourceNumber >= 1 && sourceNumber <= 44) {
                    return "AAVSO Cat " + sourceNumber;
                }
                return "Source " + sourceNumber;
        }
    }
    
    /**
     * Re-query database at new coordinates after panning
     */
    public void requeryDatabaseAtCoordinates(double newRA, double newDec) {
        System.out.printf(java.util.Locale.US, "DEBUG: Seqplot.requeryDatabaseAtCoordinates(%.6f, %.6f)\n", newRA, newDec);
        
        // Show loading indicator
        if (starPlotPanel != null) {
            starPlotPanel.setLoadingIndicator(true, "Searching database");
        }
        
        // Update database center coordinates
        db.setCentralRA(newRA);
        db.setCentralDec(newDec);
        
        // Re-query on background thread
        new Thread(() -> {
            try {
                db.findUpperLowerRa();
                db.findUpperLowerDec();
                db.getData();
                
                // Fetch VSP comparison stars for overlay
                db.fetchVSPCompStars();
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    // Update title with new coordinates
                    String titleText = String.valueOf(db.getStar());
                    if (db.isVsxDataAvailable() && db.getVsxDetails() != null) {
                        titleText += " (" + db.getVsxDetails() + ")";
                    }
                    titleText += "  RA: " + db.getFormattedRA() + 
                                 "  Dec: " + db.getFormattedDec() + "  FoV: " + 
                                 Math.round(db.getFieldSize() * 60.0) + " arcmin  Vlim: " + db.getLimitingMag();
                    this.setMainTitleText(titleText);
                    
                    // Refresh plot
                    if (starPlotPanel != null) {
                        starPlotPanel.refreshPlotData();
                        starPlotPanel.setLoadingIndicator(false);
                    }
                    
                    System.out.printf(java.util.Locale.US, "DEBUG: Database re-query complete - found %d stars\n", db.getTotalCount());
                });
            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (starPlotPanel != null) {
                        starPlotPanel.setLoadingIndicator(false);
                    }
                    javax.swing.JOptionPane.showMessageDialog(this, 
                        "Error re-querying database: " + e.getMessage(), 
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * Re-query database at coordinates with specified FOV after zooming in points view
     */
    public void requeryDatabaseAtCoordinatesWithFOV(double newRA, double newDec, double newFOV) {
        System.out.println("\n" + repeatString("=", 80));
        System.out.println("*** ZOOM TRIGGERED DATABASE REQUERY ***");
        System.out.println(repeatString("=", 80));
        System.out.printf(java.util.Locale.US, "DEBUG: Seqplot.requeryDatabaseAtCoordinatesWithFOV(%.6f, %.6f, %.4f degrees = %.2f arcmin)\n", 
                         newRA, newDec, newFOV, newFOV * 60.0);
        System.out.printf(java.util.Locale.US, "DEBUG: CIRCLE search will use: CIRCLE('ICRS', %.6f, %.6f, %.6f) [radius in degrees]\n",
                         newRA, newDec, newFOV / 2.0);
        System.out.println(repeatString("=", 80) + "\n");
        
        // Reset zoom IMMEDIATELY before loading new data
        // This prevents the old zoom level from being applied to the new larger data bounds
        if (starPlotPanel != null) {
            System.out.println("DEBUG: Resetting zoom to 1.0 BEFORE data load");
            starPlotPanel.resetZoom();  // Reset to 1.0 immediately
            starPlotPanel.setLoadingIndicator(true, "Searching database");
        }
        
        // Update database center coordinates and field size
        db.setCentralRA(newRA);
        db.setCentralDec(newDec);
        db.setFieldSize(newFOV);
        
        System.out.printf(java.util.Locale.US, "DEBUG: Updated DataConnector - CentralRA=%.6f, CentralDec=%.6f, FieldSize=%.4f deg (radius=%.4f deg)\n",
                         db.getCentralRA(), db.getCentralDec(), db.getFieldSize(), db.getFieldSize() / 2.0);
        
        // Re-query on background thread
        new Thread(() -> {
            try {
                db.findUpperLowerRa();
                db.findUpperLowerDec();
                System.out.printf(java.util.Locale.US, "DEBUG: RA range: %.6f to %.6f, Dec range: %.6f to %.6f\n",
                                 db.getLowerRA(), db.getUpperRA(), db.getLowerDec(), db.getUpperDec());
                db.getData();
                
                // Fetch VSP comparison stars for overlay
                db.fetchVSPCompStars();
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    // Update title with new coordinates and FOV
                    String titleText = String.valueOf(db.getStar());
                    if (db.isVsxDataAvailable() && db.getVsxDetails() != null) {
                        titleText += " (" + db.getVsxDetails() + ")";
                    }
                    titleText += "  RA: " + db.getFormattedRA() + 
                                 "  Dec: " + db.getFormattedDec() + "  FoV: " + 
                                 Math.round(db.getFieldSize() * 60.0) + " arcmin  Vlim: " + db.getLimitingMag();
                    this.setMainTitleText(titleText);
                    
                    // Repaint with new data (zoom was already reset at the start)
                    if (starPlotPanel != null) {
                        starPlotPanel.repaint();
                        starPlotPanel.setLoadingIndicator(false);
                    }
                    
                    System.out.printf(java.util.Locale.US, "DEBUG: Database re-query with FOV complete - found %d stars (zoom was reset to 1.0 at start)\n", db.getTotalCount());
                });
            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (starPlotPanel != null) {
                        starPlotPanel.setLoadingIndicator(false);
                    }
                    javax.swing.JOptionPane.showMessageDialog(this, 
                        "Error re-querying database: " + e.getMessage(), 
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * Fetch new DSS2 image at coordinates after panning in sky view
     */
    public void fetchDSS2ImageAtCoordinates(double newRA, double newDec) {
        System.out.printf(java.util.Locale.US, "DEBUG: Seqplot.fetchDSS2ImageAtCoordinates(%.6f, %.6f)\n", newRA, newDec);
        
        // Show loading indicator
        if (starPlotPanel != null) {
            starPlotPanel.setLoadingIndicator(true, "Retrieving image");
        }
        
        // Update database center coordinates
        db.setCentralRA(newRA);
        db.setCentralDec(newDec);
        
        // Calculate FOV from current field size
        double fov = db.getFieldSize();  // Already in degrees
        
        System.out.printf(java.util.Locale.US, "DEBUG: Fetching DSS2 at new center - RA=%.6f, Dec=%.6f, FOV=%.4f\n", 
                         newRA, newDec, fov);
        
        // Fetch new image on background thread
        new Thread(() -> {
            try {
                // Don't clear the existing image - keep it displayed until new one is ready
                // This prevents flashing to blank/points view during download
                
                // Fetch new image at current resolution and selected survey (this is async, sets up load listener)
                dss2Manager.fetchDSS2Image(newRA, newDec, fov, dss2Resolution, selectedSurvey);
                
                // Wait for image to load
                while (dss2Manager.isLoading()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                java.awt.image.BufferedImage newImage = dss2Manager.getCurrentImage();
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (newImage != null && starPlotPanel != null) {
                        starPlotPanel.setDSS2Background(newImage, dss2Manager);
                        
                        // Re-query database at new location to get new stars
                        db.findUpperLowerRa();
                        db.findUpperLowerDec();
                        db.getData();
                        
                        // Update title
                        String titleText = String.valueOf(db.getStar());
                        if (db.isVsxDataAvailable() && db.getVsxDetails() != null) {
                            titleText += " (" + db.getVsxDetails() + ")";
                        }
                        titleText += "  RA: " + db.getFormattedRA() + 
                                     "  Dec: " + db.getFormattedDec() + "  FoV: " + 
                                     Math.round(db.getFieldSize() * 60.0) + " arcmin  Vlim: " + db.getLimitingMag();
                        this.setMainTitleText(titleText);
                        
                        // Don't call refreshPlotData() - it resets zoom/pan and causes flash
                        // Just repaint to show the new image and data seamlessly
                        starPlotPanel.repaint();
                        
                        // Keep "Retrieving image" visible for 5 seconds to give user feedback
                        javax.swing.Timer hideTimer = new javax.swing.Timer(5000, evt -> {
                            starPlotPanel.setLoadingIndicator(false);
                        });
                        hideTimer.setRepeats(false);
                        hideTimer.start();
                        
                        // System.out.println("DEBUG: DSS2 image and data refresh complete");
                    } else {
                        // On error, hide immediately (no delay needed)
                        starPlotPanel.setLoadingIndicator(false);
                        javax.swing.JOptionPane.showMessageDialog(this, 
                            "Failed to fetch DSS2 image at new location", 
                            "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (starPlotPanel != null) {
                        starPlotPanel.setLoadingIndicator(false);
                    }
                    javax.swing.JOptionPane.showMessageDialog(this, 
                        "Error fetching DSS2 image: " + e.getMessage(), 
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * Fetch DSS2 image at specific coordinates and FOV (for zoom operations)
     */
    public void fetchDSS2ImageAtCoordinatesWithFOV(double newRA, double newDec, double newFOV) {
        System.out.printf(java.util.Locale.US, "DEBUG: Seqplot.fetchDSS2ImageAtCoordinatesWithFOV(%.6f, %.6f, FOV=%.4f)\n", 
                         newRA, newDec, newFOV);
        
        // Show loading indicator with zoom-specific message
        if (starPlotPanel != null) {
            starPlotPanel.setLoadingIndicator(true, "Loading zoomed view...");
        }
        
        // Update database center coordinates and FOV
        db.setCentralRA(newRA);
        db.setCentralDec(newDec);
        db.setFieldSize(newFOV);
        
        // Fetch new image on background thread
        new Thread(() -> {
            try {
                // Fetch new image at new FOV (this is async, sets up load listener)
                dss2Manager.fetchDSS2Image(newRA, newDec, newFOV, dss2Resolution, selectedSurvey);
                
                // Wait for image to load
                while (dss2Manager.isLoading()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                java.awt.image.BufferedImage newImage = dss2Manager.getCurrentImage();
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (newImage != null && starPlotPanel != null) {
                        starPlotPanel.setDSS2Background(newImage, dss2Manager);
                        
                        // Reset zoom to 1.0 since we fetched new image at target FOV
                        starPlotPanel.resetZoom();
                        
                        // Re-query database at new location/FOV to get new stars
                        db.findUpperLowerRa();
                        db.findUpperLowerDec();
                        db.getData();
                        
                        // Update title
                        String titleText = String.valueOf(db.getStar());
                        if (db.isVsxDataAvailable() && db.getVsxDetails() != null) {
                            titleText += " (" + db.getVsxDetails() + ")";
                        }
                        titleText += "  RA: " + db.getFormattedRA() + 
                                     "  Dec: " + db.getFormattedDec() + "  FoV: " + 
                                     Math.round(db.getFieldSize() * 60.0) + " arcmin  Vlim: " + db.getLimitingMag();
                        this.setMainTitleText(titleText);
                        
                        // Repaint to show the new image and data
                        starPlotPanel.repaint();
                        
                        // Hide loading indicator after brief delay
                        javax.swing.Timer hideTimer = new javax.swing.Timer(1000, evt -> {
                            starPlotPanel.setLoadingIndicator(false);
                        });
                        hideTimer.setRepeats(false);
                        hideTimer.start();
                        
                        System.out.println("DEBUG: Zoomed DSS2 image loaded successfully");
                    } else {
                        starPlotPanel.setLoadingIndicator(false);
                        javax.swing.JOptionPane.showMessageDialog(this, 
                            "Failed to fetch zoomed DSS2 image", 
                            "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (starPlotPanel != null) {
                        starPlotPanel.setLoadingIndicator(false);
                    }
                    javax.swing.JOptionPane.showMessageDialog(this, 
                        "Error fetching zoomed DSS2 image: " + e.getMessage(), 
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}

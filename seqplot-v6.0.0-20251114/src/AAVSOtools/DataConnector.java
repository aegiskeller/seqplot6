/*
 * DataConnector.java - Enhanced Data Connection and Coordinate Management
 * Part of Seqplot 6.0.0 - Enhanced Astronomical Sequence Plotting Application
 * Version 6.0.0 - Released November 3, 2025
 * 
 * Enhanced with improved coordinate formatting and modern display conventions
 * 
 * EXTERNAL CATALOG API ENDPOINTS (Updated November 5, 2025):
 * ==============================================================
 * APASS9:     http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync (VizieR TAP - "II/336/apass9")
 * Gaia DR2:   https://gea.esac.esa.int/tap-server/tap/sync (ESA Gaia Archive - gaiadr2.gaia_source)
 * PanSTARRS:  https://catalogs.mast.stsci.edu/api/v0.1/panstarrs/dr2/mean.votable (MAST API)
 * Tycho-2:    http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync (VizieR TAP - "I/259/tyc2")
 *             Alternative: https://datalab.noirlab.edu/data/tycho2#data-access (NOIRLab Data Lab)
 *             - Bespoke API described at https://datalab.noirlab.edu/data/tycho2#data-access
 *             - Currently using VizieR for compatibility with existing VOTable parser
 * 
 * Performance Notes:
 * - ESA Gaia Archive: ~6 seconds for 0.25° field (20x faster than VizieR mirror)
 * - MAST PanSTARRS API: ~12-15 seconds for 0.25° field (15x faster than VizieR)
 * - VizieR TAP for APASS9/Tycho-2: Generally responsive for smaller fields
 * 
 * API Status Monitoring:
 * - EnterStar dialog now displays real-time API endpoint health status
 * - Status indicators: Green (fast <2s), Orange (slow 2-5s), Red (unavailable)
 * - Background health checks on dialog open to inform user of service availability
 */
package AAVSOtools;

import AAVSOtools.EnterStar;
import AAVSOtools.Seqplot;
import AAVSOtools.CDSMirrorSelector;
import java.awt.Color;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractXYZDataset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DataConnector
extends AbstractXYZDataset
implements DomainInfo,
RangeInfo {
    private static final int MAX_RECORD_COUNT = 5000;
    private static final int DEFAULT_SERIES_COUNT = 5;
    private static final int DEFAULT_VARIABLE_ARRAY_SIZE = 5000;
    private static final int DEFAULT_FONT_SIZE = 16;
    private static final double DEFAULT_MIN_X = 10.0;
    private static final double DEFAULT_MAX_X = 0.0;
    private static final double DEFAULT_MIN_Y = 1.0;
    private static final double DEFAULT_MAX_Y = 0.0;
    private static final double DEFAULT_MIN_Z = 100.0;
    private static final double DEFAULT_MAX_Z = -100.0;
    private static final double DEFAULT_MIN_RA = 360.0;
    private static final double DEFAULT_MAX_RA = 0.0;
    private static final double DEFAULT_MIN_DEC = 90.0;
    private static final double DEFAULT_MAX_DEC = -90.0;
    private static final double DEFAULT_BLUE_GREEN_LIMIT = 0.5;
    private static final double DEFAULT_GREEN_RED_LIMIT = 1.1;
    private static final double DEFAULT_RED_LIMIT = 9.9;
    private static final double DEFAULT_UPPER_LIMITING_MAG = -5.0;
    private static final double DEFAULT_LIMITING_MAG = 20.0;
    private static final double DEFAULT_FIELD_SIZE = 1.0;
    private static final double DEFAULT_POSITION_TOLERANCE = 0.003;
    private static final String DEFAULT_MAC_FOLDER = "/Documents/";
    private static final String DEFAULT_PC_FOLDER = "\\";
    private static final String DEFAULT_UNIX_FOLDER = "/";
    private static final String DEFAULT_STAR = "";
    private static final String DEFAULT_CHART_SIZE_SELECTION = "A (15 deg, <9 mag)";
    private static final String USER_PREFERENCES_FILE = "seqplotDefault.ini";
    private static final String BASE_URL = "https://www.aavso.org/";
    public URL calibUrl;
    private int itemCount;
    private int seriesCount;
    private int totalCount;
    private int numberOfVars;
    private int selectedStarIndex = -1;  // Track currently selected star for sequence list
    private int[] seriesValue;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double minZ;
    private double maxZ;
    private double minRa;
    private double maxRa;
    private double minDec;
    private double maxDec;
    private double raToPlot;
    private double decToPlot;
    private double sizeOfPlot;
    private double upperDec;
    private double upperRA;
    private double lowerDec;
    private double lowerRA;
    private double upperLimitingMag = -5.0;
    private double limitingMag = 20.0;
    private double positionTolerance = 0.003;
    private Boolean tychoBoxSelected;
    private Boolean nofsBoxSelected;
    private Boolean sonoitaBoxSelected;
    private Boolean bsmBoxSelected;
    private Boolean bsm_SBoxSelected;
    private Boolean coker30BoxSelected;
    private Boolean sro50BoxSelected;
    private Boolean tmo61BoxSelected;
    private Boolean apassBoxSelected;
    private Boolean apass9BoxSelected;
    private Boolean gaiaDR2BoxSelected;
    private Boolean gaiaDR3BoxSelected;
    private Boolean panstarrsBoxSelected;
    private Boolean sdssBoxSelected;
    private Boolean k35BoxSelected;
    private Boolean w28BoxSelected;
    private Boolean w30BoxSelected;
    private Boolean oc61BoxSelected;
    private Boolean gcpdBoxSelected;
    private int catalogReadTimeoutSeconds = 120;  // Default 2 minute timeout for catalog queries
    private long raHrs;
    private long raMins;
    private long decDegs;
    private long decMins;
    private BigDecimal raSecs;
    private BigDecimal decSecs;
    private double[][] xVal;
    private double[][] yVal;
    private double[][] zVal;
    private String decSign;
    private String logfile = null;
    private String tablefile = null;
    private String path;
    private String star = null;
    private String vsxDetails = null;  // Store VSX details like "Type: CST  Max: 10.1 p  Min: ?"
    private boolean vsxDataAvailable = false;  // Flag to indicate if data is from VSX
    private String auid = null;  // Store AAVSO Unique Identifier
    private String oid = null;  // Store VSX Object ID
    private Double transitionMagnitude = null;  // Transition magnitude for catalog filtering
    private String plotType;
    private String catalogString;
    private String raText = "";
    private String decText = "";
    private String oldRAText = "";
    private String oldDecText = "";
    private String username = "";
    private String chartSizeSelection = DEFAULT_CHART_SIZE_SELECTION;
    private String[] name;
    private double[] ra;
    private double[] raerr;
    private double[] dec;
    private double[] decerr;
    private int[] nobs;
    private double[] vmag;
    private double[] bMinusV;
    private double[] uMinusB;
    private double[] vMinusR;
    private double[] rMinusI;
    private double[] vMinusI;
    private double[] ev;
    private double[] ebv;
    private double[] eub;
    private double[] evr;
    private double[] eri;
    private double[] evi;
    private int[] mobs;
    private int[] source;
    
    // Additional APASS9 photometric data arrays
    private double[] bmag;         // B magnitude
    private double[] e_bmag;       // B magnitude error
    private double[] g_prime_mag;  // Sloan g' magnitude
    private double[] e_g_prime_mag;// Sloan g' magnitude error
    private double[] r_prime_mag;  // Sloan r' magnitude
    private double[] e_r_prime_mag;// Sloan r' magnitude error
    private double[] i_prime_mag;  // Sloan i' magnitude
    private double[] e_i_prime_mag;// Sloan i' magnitude error
    private double[] coord_error_ra;  // RA coordinate error
    private double[] coord_error_dec; // Dec coordinate error
    
    // Secondary catalog data for cross-matching
    private java.util.List<CatalogEntry> secondaryCatalogData = new java.util.ArrayList<>();
    
    private String[][] filters;
    private double[] tempRa;
    private String[] varName;
    private double[] rVar;
    private double[] dVar;
    private double[] varRa;
    private double[] varDec;
    private String[] varType;
    private String[] varMax;
    private String[] varMin;
    private Boolean quitSelected = false;
    private Boolean closeButtonClicked;
    private Boolean blankCoord = false;
    private Number domainMax;
    private Number domainMin;
    private Number rangeMax;
    private Number rangeMin;
    private Range domainRange;
    private Range range;
    public EnterStar starWindow = null;
    private Frame frame;
    private Seqplot seqplot;

    private void setLoadingIndicatorAsync(boolean loading) {
        setLoadingIndicatorAsync(loading, loading ? "Searching database" : null);
    }

    private void setLoadingIndicatorAsync(boolean loading, String message) {
        if (this.seqplot == null) {
            return;
        }
        StarPlotPanel panel = this.seqplot.getStarPlotPanel();
        if (panel == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (message != null) {
                panel.setLoadingIndicator(loading, message);
            } else {
                panel.setLoadingIndicator(loading);
            }
        });
    }

    private void disposeStarWindowAsync() {
        if (this.starWindow == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (this.starWindow != null) {
                this.starWindow.dispose();
                this.starWindow = null;
            }
        });
    }

    private void showMessageDialogAsync(java.awt.Component parent, String message, String title, int messageType) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent, message, title, messageType));
    }

    private void showMessageDialogAsync(String message, String title, int messageType) {
        showMessageDialogAsync(this.frame, message, title, messageType);
    }

    public DataConnector(Seqplot plot) {
        this(plot, 5, 5000);
    }

    public DataConnector(Seqplot plot, int seriesCount, int itemCount) {
        this.seqplot = plot;
        this.getUserDefaults();
        this.queryDatabase();
        if (this.getQuitSelected().booleanValue()) {
            System.exit(0);
        }
    }

    public void setUpLogfile(String type) {
        String defaultFilename;
        if (this.getStar().equals(DEFAULT_STAR)) {
            String tempDec = this.getDecText().replace("+", "_N");
            tempDec = tempDec.replace("-", "_S");
            defaultFilename = String.valueOf(this.getRAText().replace(" ", "_")) + tempDec.replace(" ", "_");
            defaultFilename = defaultFilename.replace(":", "_");
            defaultFilename = defaultFilename.replace(".", "_");
            defaultFilename = type.equals("logfile") ? String.valueOf(defaultFilename) + "_seq.txt" : String.valueOf(defaultFilename) + "_table.csv";
        } else {
            defaultFilename = this.getStar().replace(" ", "_");
            defaultFilename = defaultFilename.replace(":", "_");
            defaultFilename = defaultFilename.replace(".", "_");
            defaultFilename = type.equals("logfile") ? String.valueOf(defaultFilename) + "_seq.txt" : String.valueOf(defaultFilename) + "_table.csv";
        }
        String fullpath = JOptionPane.showInputDialog(this.frame, "What would you like to call your file?", String.valueOf(this.getPathToLogfile()) + defaultFilename);
        File fileObject = new File(fullpath);
        try {
            if (fileObject.exists() && fileObject.canRead()) {
                fullpath = JOptionPane.showInputDialog(this.frame, "Your file already exists.\nNew selections will be appended\nunless you rename the file now.", fullpath);
            }
        }
        catch (Exception exception) {}
        while (fullpath.equals(DEFAULT_STAR)) {
            fullpath = JOptionPane.showInputDialog(this.frame, "Illegal file name, please re-enter:  ", "Invalid File Name", 2);
        }
        if (fullpath != null) {
            if (type.equals("logfile")) {
                this.setLogfile(fullpath);
            } else {
                this.setTablefile(fullpath);
            }
            int i = fullpath.contains(DEFAULT_UNIX_FOLDER) ? fullpath.lastIndexOf(DEFAULT_UNIX_FOLDER) : fullpath.lastIndexOf(DEFAULT_PC_FOLDER);
            String pathOnly = fullpath.substring(0, i + 1);
            this.setPathToLogfile(pathOnly);
            if (type.equals("logfile")) {
                this.setUserDefaults(this.seqplot.getColorArray());
            }
        }
    }

    public void initializeArrays(int seriesCount, int itemCount) {
        this.xVal = new double[seriesCount][++itemCount];
        this.yVal = new double[seriesCount][itemCount];
        this.zVal = new double[seriesCount][itemCount];
        this.name = new String[itemCount];
        this.ra = new double[itemCount];
        this.raerr = new double[itemCount];
        this.dec = new double[itemCount];
        this.decerr = new double[itemCount];
        this.nobs = new int[itemCount];
        this.vmag = new double[itemCount];
        this.bMinusV = new double[itemCount];
        this.uMinusB = new double[itemCount];
        this.vMinusR = new double[itemCount];
        this.rMinusI = new double[itemCount];
        this.vMinusI = new double[itemCount];
        this.ev = new double[itemCount];
        this.ebv = new double[itemCount];
        this.eub = new double[itemCount];
        this.evr = new double[itemCount];
        this.eri = new double[itemCount];
        this.evi = new double[itemCount];
        this.mobs = new int[itemCount];
        this.source = new int[itemCount];
        
        // Initialize additional APASS9 photometric arrays
        this.bmag = new double[itemCount];
        this.e_bmag = new double[itemCount];
        this.g_prime_mag = new double[itemCount];
        this.e_g_prime_mag = new double[itemCount];
        this.r_prime_mag = new double[itemCount];
        this.e_r_prime_mag = new double[itemCount];
        this.i_prime_mag = new double[itemCount];
        this.e_i_prime_mag = new double[itemCount];
        this.coord_error_ra = new double[itemCount];
        this.coord_error_dec = new double[itemCount];
        
        this.filters = new String[itemCount][];
        this.seriesValue = new int[itemCount];
        this.tempRa = new double[itemCount];
        this.varName = new String[5000];
        this.varRa = new double[5000];
        this.varDec = new double[5000];
        this.varType = new String[5000];
        this.varMax = new String[5000];
        this.varMin = new String[5000];
        this.rVar = new double[5000];
        this.dVar = new double[5000];
        this.seriesCount = seriesCount;
        this.itemCount = itemCount;
        this.minRa = 360.0;
        this.maxRa = 0.0;
        this.minDec = 90.0;
        this.maxDec = -90.0;
        this.minX = 10.0;
        this.maxX = 0.0;
        this.minY = 1.0;
        this.maxY = 0.0;
        this.minZ = 100.0;
        this.maxZ = -100.0;
    }

    public void queryDatabase() {
        this.starWindow = this.getStar() != null ? new EnterStar(this, this.seqplot, this.frame, this.getStar()) : new EnterStar(this, this.seqplot, this.frame, DEFAULT_STAR);
        if (!this.getQuitSelected().booleanValue() && !this.getCancelSelected().booleanValue()) {
            this.findUpperLowerRa();
            this.findUpperLowerDec();
            this.getData();
            if (this.getTotalCount() == 0) {
                this.queryDatabase();
            }
            if (this.getTotalCount() >= 5000) {
                showMessageDialogAsync(this.frame, "Maximum number of records allowed has been exceeded! \nOnly the first 5000 records found will be displayed.", "Warning!", JOptionPane.WARNING_MESSAGE);
            } else {
                this.seqplot.setMainTitleText(String.valueOf(this.getStar()) + "  RA: " + this.getFormattedRA() + "  Dec: " + this.getFormattedDec() + "  FoV: " + Math.round(this.getFieldSize() * 60.0) + " arcmin");
                double toleranceArcsec = this.getPositionTolerance() * 3600.0;
                this.seqplot.setSubtitleText("Data from the Calibration Database - limiting magnitude " + this.getLimitingMag() + 
                    " - VSX position matching tolerance " + String.format("%.1f", toleranceArcsec) + " arcseconds");
            }
        }
    }

    /**
     * Refresh data with current parameters (e.g., after limiting magnitude change)
     * Does NOT show the EnterStar dialog - uses existing star/field parameters
     */
    public void refreshCurrentField() {
        // Skip if no star is currently set
        if (this.getStar() == null) {
            return;
        }
        
        // Re-query with current star and updated parameters
        this.findUpperLowerRa();
        this.findUpperLowerDec();
        this.getData();
        
        if (this.getTotalCount() >= 5000) {
            showMessageDialogAsync(this.frame, "Maximum number of records allowed has been exceeded! \nOnly the first 5000 records found will be displayed.", "Warning!", JOptionPane.WARNING_MESSAGE);
        } else {
            this.seqplot.setMainTitleText(String.valueOf(this.getStar()) + "  RA: " + this.getFormattedRA() + "  Dec: " + this.getFormattedDec() + "  FoV: " + Math.round(this.getFieldSize() * 60.0) + " arcmin");
            double toleranceArcsec = this.getPositionTolerance() * 3600.0;
            this.seqplot.setSubtitleText("Data from the Calibration Database - limiting magnitude " + this.getLimitingMag() + 
                " - VSX position matching tolerance " + String.format("%.1f", toleranceArcsec) + " arcseconds");
        }
    }

    public Document getDocument(URL url) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(url.openStream());
            document.getDocumentElement().normalize();
        }
        catch (IOException | ParserConfigurationException | SAXException e) {
            throw new IllegalArgumentException(e);
        }
        return document;
    }

    public String urlEncode(String str) {
        String ret = DEFAULT_STAR;
        try {
            ret = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void getPositionFromUser(String userStarInput) {
        HashMap<String, String> vsx_data = new HashMap<String, String>();
        try {
            URL vsxUrl = new URL(String.valueOf(this.getBaseURL()) + "vsx/index.php?view=api.object&ident=" + URLEncoder.encode(userStarInput.trim().toLowerCase(), "UTF-8"));
            NodeList vsxObjectNodes = this.getDocument(vsxUrl).getElementsByTagName("VSXObject");
            NodeList childNodes = vsxObjectNodes.item(0).getChildNodes();
            int i = 0;
            while (i < childNodes.getLength()) {
                Element elt = (Element)childNodes.item(i);
                vsx_data.put(elt.getNodeName(), elt.getTextContent());
                ++i;
            }
            
            // DEBUG: Print all VSX fields received
            if (!vsx_data.isEmpty()) {
                System.out.println("\n========== VSX API RESPONSE FOR: " + userStarInput + " ==========");
                for (String key : vsx_data.keySet()) {
                    System.out.println("  " + key + ": " + vsx_data.get(key));
                }
                System.out.println("===============================================\n");
            }
            
            if (!vsx_data.isEmpty()) {
                this.setCentralRA(Double.parseDouble((String)vsx_data.get("RA2000")));
                this.setCentralDec(Double.parseDouble((String)vsx_data.get("Declination2000")));
                
                // Capture AUID and OID
                this.auid = vsx_data.get("AUID");
                this.oid = vsx_data.get("OID");
                
                // Capture VSX details - try both "Type" and "VariabilityType"
                String varType = vsx_data.get("Type");
                if (varType == null || varType.trim().isEmpty()) {
                    varType = vsx_data.get("VariabilityType");
                }
                String maxMag = vsx_data.get("MaxMag");
                String minMag = vsx_data.get("MinMag");
                
                // Format the VSX details string
                StringBuilder vsxInfo = new StringBuilder();
                if (varType != null && !varType.trim().isEmpty()) {
                    vsxInfo.append("Type: ").append(varType.trim());
                }
                if (maxMag != null && !maxMag.trim().isEmpty()) {
                    if (vsxInfo.length() > 0) vsxInfo.append("  ");
                    vsxInfo.append("Max: ").append(maxMag.trim());
                }
                if (minMag != null && !minMag.trim().isEmpty()) {
                    if (vsxInfo.length() > 0) vsxInfo.append("  ");
                    vsxInfo.append("Min: ").append(minMag.trim());
                }
                
                this.vsxDetails = vsxInfo.length() > 0 ? vsxInfo.toString() : null;
                this.vsxDataAvailable = true;
                
                if (this.getFieldSize() == 0.0) {
                    this.setFieldSize(1.0);
                }
                if (this.getLimitingMag() == 0.0) {
                    this.setLimitingMag(20.0);
                    this.setUpperLimitingMag(-5.0);
                }
            } else {
                // VSX search failed, try SIMBAD as fallback
                System.out.println("DEBUG: VSX search failed, trying SIMBAD for: " + userStarInput);
                HashMap<String, String> simbad_data = querySimbad(userStarInput);
                
                if (!simbad_data.isEmpty()) {
                    this.setCentralRA(Double.parseDouble(simbad_data.get("RA")));
                    this.setCentralDec(Double.parseDouble(simbad_data.get("DEC")));
                    
                    // Clear VSX details since we're using SIMBAD
                    this.vsxDetails = null;
                    this.vsxDataAvailable = false;
                    this.auid = null;
                    this.oid = null;
                    
                    if (this.getFieldSize() == 0.0) {
                        this.setFieldSize(1.0);
                    }
                    if (this.getLimitingMag() == 0.0) {
                        this.setLimitingMag(20.0);
                        this.setUpperLimitingMag(-5.0);
                    }
                    System.out.println("DEBUG: SIMBAD found coordinates - RA: " + simbad_data.get("RA") + ", Dec: " + simbad_data.get("DEC"));
                } else {
                    showMessageDialogAsync(null, String.valueOf(userStarInput) + " cannot be found in VSX or SIMBAD.\n" + "Please either type in another star name\n" + "or enter the RA and Dec for this field.", "Star not found!", JOptionPane.WARNING_MESSAGE);
                    this.setBlankCoord(true);
                }
            }
        }
        catch (MalformedURLException e) {
            showMessageDialogAsync(null, "MalformedURLException: " + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
        }
        catch (UnsupportedEncodingException e) {
            showMessageDialogAsync(null, "UnsupportedEncodingException: " + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Query SIMBAD for object coordinates as fallback when VSX search fails
     * @param objectName Name of the astronomical object
     * @return HashMap with "RA" and "DEC" in degrees, empty if not found
     */
    private HashMap<String, String> querySimbad(String objectName) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            // Use SIMBAD's name resolver with VOTable output
            String simbadUrl = "http://simbad.u-strasbg.fr/simbad/sim-id?output.format=votable&Ident=" + 
                               URLEncoder.encode(objectName.trim(), "UTF-8");
            
            System.out.println("DEBUG: Querying SIMBAD: " + simbadUrl);
            
            URL url = new URL(simbadUrl);
            Document doc = this.getDocument(url);
            
            // Parse VOTable response - SIMBAD returns coordinates in TD elements
            // Structure: VOTABLE -> RESOURCE -> TABLE -> DATA -> TABLEDATA -> TR -> TD elements
            NodeList tdNodes = doc.getElementsByTagName("TD");
            
            // Find FIELD definitions to identify RA and DEC columns
            NodeList fieldNodes = doc.getElementsByTagName("FIELD");
            int raIndex = -1;
            int decIndex = -1;
            
            // Map field names to column indices
            for (int i = 0; i < fieldNodes.getLength(); i++) {
                Element field = (Element) fieldNodes.item(i);
                String name = field.getAttribute("name");
                String ucd = field.getAttribute("ucd");
                
                // Look for RA coordinate field (take first match only)
                if (raIndex < 0 && ("RA_d".equalsIgnoreCase(name) || "ra".equalsIgnoreCase(name) || 
                    (ucd != null && ucd.contains("pos.eq.ra;meta.main")))) {
                    raIndex = i;
                }
                // Look for DEC coordinate field (take first match only)
                if (decIndex < 0 && ("DEC_d".equalsIgnoreCase(name) || "dec".equalsIgnoreCase(name) || 
                    (ucd != null && ucd.contains("pos.eq.dec;meta.main")))) {
                    decIndex = i;
                }
            }
            
            System.out.println("DEBUG: SIMBAD RA index=" + raIndex + ", DEC index=" + decIndex + ", TD count=" + tdNodes.getLength());
            
            // Extract RA and DEC values from TD elements
            if (raIndex >= 0 && decIndex >= 0 && tdNodes.getLength() > Math.max(raIndex, decIndex)) {
                String raValue = tdNodes.item(raIndex).getTextContent().trim();
                String decValue = tdNodes.item(decIndex).getTextContent().trim();
                
                System.out.println("DEBUG: SIMBAD found RA=" + raValue + ", DEC=" + decValue);
                
                if (!raValue.isEmpty() && !decValue.isEmpty()) {
                    result.put("RA", raValue);
                    result.put("DEC", decValue);
                }
            }
        } catch (Exception e) {
            System.err.println("SIMBAD query failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    public void getData() {
        System.out.println("\n========== DEBUG: getData() CALLED ==========");
        System.out.println("Central RA: " + this.getCentralRA());
        System.out.println("Central Dec: " + this.getCentralDec());
        System.out.println("Field Size: " + this.getFieldSize());
        System.out.println("Limiting Mag: " + this.getLimitingMag());
        
        // Show loading indicator instead of popup window
        setLoadingIndicatorAsync(true);
        
        // Check if any external catalogs are selected
        boolean hasExternalCatalogs = this.getApass9BoxSelected().booleanValue() || 
                                       this.getGaiaDR2BoxSelected().booleanValue() || 
                                       this.getGaiaDR3BoxSelected().booleanValue() || 
                                       this.getPanstarrsBoxSelected().booleanValue() ||
                                       this.getSdssBoxSelected().booleanValue();
        
        System.out.println("hasExternalCatalogs: " + hasExternalCatalogs);
        
        // Check if any AAVSO catalogs are selected
        boolean hasAAVSOCatalogs = !this.getCatalogString().trim().equals("");
        
        System.out.println("hasAAVSOCatalogs: " + hasAAVSOCatalogs);
        System.out.println("Catalog String: '" + this.getCatalogString().trim() + "'");
        System.out.println("============================================\n");
        
        // If ONLY external catalogs are selected (no AAVSO catalogs)
        if (hasExternalCatalogs && !hasAAVSOCatalogs) {
            this.getExternalData();
            return;
        }
        
        // If AAVSO catalogs are selected, load them into main arrays first
        if (hasAAVSOCatalogs) {
        try {
            try {
                this.calibUrl = new URL(String.valueOf(this.getBaseURL()) + "vsx/index.php?view=api.calib" + "&fromra=" + URLEncoder.encode(String.valueOf(String.format("%.6f", this.getLowerRA())).trim(), "UTF-8") + "&tora=" + URLEncoder.encode(String.valueOf(String.format("%.6f", this.getUpperRA())).trim(), "UTF-8") + "&fromdec=" + URLEncoder.encode(String.valueOf(String.format("%.6f", this.getLowerDec())).trim(), "UTF-8") + "&todec=" + URLEncoder.encode(String.valueOf(String.format("%.6f", this.getUpperDec())).trim(), "UTF-8") + "&tomag=" + URLEncoder.encode(String.valueOf(this.getLimitingMag()).trim(), "UTF-8") + "&source=" + this.getCatalogString().trim() + "&limit=" + 5000);
                NodeList dataObjectNodes = this.getDocument(this.calibUrl).getElementsByTagName("Object");
                int apiReturnedCount = dataObjectNodes.getLength();
                // Limit to 5000 to prevent array overflow (API sometimes returns more than requested limit)
                int safeCount = Math.min(apiReturnedCount, 5000);
                this.setTotalCount(safeCount);
                
                if (apiReturnedCount > 5000) {
                    System.out.println("WARNING: AAVSO API returned " + apiReturnedCount + " stars but limiting to 5000");
                    final int returnedCount = apiReturnedCount;
                    // Show warning popup on EDT
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                            String.format("Field contains %d stars (limit is 5000).\n\n" +
                                "Only the first 5000 stars will be displayed.\n" +
                                "To see all stars, try reducing:\n" +
                                "  • Field of View (FOV)\n" +
                                "  • Limiting Magnitude", returnedCount),
                            "Too Many Stars",
                            JOptionPane.WARNING_MESSAGE);
                    });
                }
                
                if (this.getTotalCount() != 0) {
                    this.initializeArrays(5, this.getTotalCount());
                    this.findVariables();
                    int i = 0;
                    while (i < this.getTotalCount()) {
                        int series;
                        NodeList calibData = dataObjectNodes.item(i).getChildNodes();
                        int j = 0;
                        while (j < calibData.getLength()) {
                            Element detailElt = (Element)calibData.item(j);
                            String nodeName = detailElt.getNodeName();
                            String nodeValue = detailElt.getTextContent();
                            if ("Name".equals(nodeName)) {
                                this.setName(i, String.valueOf(nodeValue));
                            } else if ("RA_J2000".equals(nodeName)) {
                                this.setRa(i, Double.parseDouble(nodeValue));
                            } else if ("raerr".equals(nodeName)) {
                                this.setRaerr(i, Double.parseDouble(nodeValue));
                            } else if ("DEC_J2000".equals(nodeName)) {
                                this.setDec(i, Double.parseDouble(nodeValue));
                            } else if ("decerr".equals(nodeName)) {
                                this.setDecerr(i, Double.parseDouble(nodeValue));
                            } else if ("nobs".equals(nodeName)) {
                                int nobsValue = Integer.parseInt(nodeValue);
                                this.setNobs(i, nobsValue);
                            } else if ("V".equals(nodeName)) {
                                this.setVmag(i, Double.parseDouble(nodeValue));
                            } else if ("B_minus_V".equals(nodeName)) {
                                this.setBMinusV(i, Double.parseDouble(nodeValue));
                            } else if ("U_minus_B".equals(nodeName)) {
                                this.setUMinusB(i, Double.parseDouble(nodeValue));
                            } else if ("V_minus_R".equals(nodeName)) {
                                this.setVMinusR(i, Double.parseDouble(nodeValue));
                            } else if ("R_minus_I".equals(nodeName)) {
                                this.setRMinusI(i, Double.parseDouble(nodeValue));
                            } else if ("V_minus_I".equals(nodeName)) {
                                this.setVMinusI(i, Double.parseDouble(nodeValue));
                            } else if ("Ev".equals(nodeName)) {
                                this.setEv(i, Double.parseDouble(nodeValue));
                            } else if ("Ebv".equals(nodeName)) {
                                this.setEbv(i, Double.parseDouble(nodeValue));
                            } else if ("Eub".equals(nodeName)) {
                                this.setEub(i, Double.parseDouble(nodeValue));
                            } else if ("Evr".equals(nodeName)) {
                                this.setEvr(i, Double.parseDouble(nodeValue));
                            } else if ("Eri".equals(nodeName)) {
                                this.setEri(i, Double.parseDouble(nodeValue));
                            } else if ("Evi".equals(nodeName)) {
                                this.setEvi(i, Double.parseDouble(nodeValue));
                            } else if ("mobs>0".equals(nodeName)) {
                                this.setMobs(i, Integer.parseInt(nodeValue));
                            } else if ("source".equals(nodeName)) {
                                int sourceValue = Integer.parseInt(nodeValue);
                                this.setSource(i, sourceValue);
                            } else if ("filters".equals(nodeName)) {
                                NodeList filterData = detailElt.getChildNodes();
                                this.setFiltersSize(i, filterData.getLength());
                                int k = 0;
                                while (k < filterData.getLength()) {
                                    Element filterElt = (Element)filterData.item(k);
                                    String filterValue = filterElt.getTextContent();
                                    this.setFilters(i, k, filterValue);
                                    ++k;
                                }
                            }
                            ++j;
                        }
                        this.seriesValue[i] = series = this.assignSeriesNumber(this.getBMinusV(i), this.getRa(i), this.getRaerr(i), this.getDec(i), this.getDecerr(i), this.getVmag(i), this.getUpperLimitingMag(), i);
                        if (this.getVmag(i) < this.minZ) {
                            this.minZ = this.getVmag(i);
                        }
                        if (this.getVmag(i) > this.maxZ) {
                            this.maxZ = this.getVmag(i);
                        }
                        ++i;
                    }
                    this.minRa = this.getLowerRA();
                    this.maxRa = this.getUpperRA();
                    this.minDec = this.getLowerDec();
                    this.maxDec = this.getUpperDec();
                    this.convertToTangentPlane();
                    this.domainMin = this.minX;
                    this.domainMax = this.maxX;
                    this.domainRange = new Range(this.minX, this.maxX);
                    this.rangeMin = this.minY;
                    this.rangeMax = this.maxY;
                    this.range = new Range(this.minY, this.maxY);
                    // Hide loading indicator
                    setLoadingIndicatorAsync(false);
                } else {
                    // Hide loading indicator
                    setLoadingIndicatorAsync(false);
                    showMessageDialogAsync(null, "Data in this range cannot be found.\nPlease type in another star name, add catalogs, or change the field size\nor limiting magnitude for this field to get data.", "Data not found!", JOptionPane.WARNING_MESSAGE);
                }
            }
            catch (MalformedURLException e) {
                showMessageDialogAsync(null, "MalformedURLException: " + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
                // Hide loading indicator
                setLoadingIndicatorAsync(false);
            }
            catch (UnsupportedEncodingException e) {
                showMessageDialogAsync(null, "UnsupportedEncodingException: " + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
                // Hide loading indicator
                setLoadingIndicatorAsync(false);
            }
            
            // After loading AAVSO catalogs, check if external catalogs are also selected
            if (hasExternalCatalogs && this.getTotalCount() > 0) {
                System.out.println("DEBUG: AAVSO catalogs loaded into main arrays. Now loading external catalogs...");
                
                // Load external catalogs into secondary storage
                clearSecondaryCatalogData();
                
                if (this.getApass9BoxSelected().booleanValue()) {
                    System.out.println("Loading APASS9 as secondary catalog...");
                    this.loadApass9Secondary();
                }
                if (this.getGaiaDR2BoxSelected().booleanValue()) {
                    System.out.println("Loading Gaia DR2 as secondary catalog...");
                    this.loadGaiaDR2Secondary();
                }
                if (this.getGaiaDR3BoxSelected().booleanValue()) {
                    System.out.println("Loading Gaia DR3 as secondary catalog...");
                    this.loadGaiaDR3Secondary();
                }
                if (this.getPanstarrsBoxSelected().booleanValue()) {
                    System.out.println("Loading PanSTARRS as secondary catalog...");
                    this.loadPanstarrsSecondary();
                }
                
                System.out.println("Secondary catalog data loaded: " + secondaryCatalogData.size() + " stars");
                
                // Merge secondary catalog data into main arrays for display
                System.out.println("Merging secondary catalogs into main display arrays...");
                mergeSecondaryCatalogsIntoMainArrays();
            }
        }
        finally {
            // Hide loading indicator in finally block to ensure it's always hidden
            setLoadingIndicatorAsync(false);
            // Close the EnterStar dialog when data loading is complete
            disposeStarWindowAsync();
        }
        }  // End of if (hasAAVSOCatalogs) block
    }

    public void getExternalData() {
        try {
            // Clear any previous secondary catalog data
            clearSecondaryCatalogData();
            
            // Count how many external catalogs are selected
            int catalogCount = 0;
            System.out.println("DEBUG: Checking catalog selections...");
            if (this.getApass9BoxSelected().booleanValue()) {
                System.out.println("  APASS9: SELECTED");
                catalogCount++;
            } else {
                System.out.println("  APASS9: not selected");
            }
            if (this.getGaiaDR2BoxSelected().booleanValue()) {
                System.out.println("  Gaia DR2: SELECTED");
                catalogCount++;
            } else {
                System.out.println("  Gaia DR2: not selected");
            }
            if (this.getGaiaDR3BoxSelected().booleanValue()) {
                System.out.println("  Gaia DR3: SELECTED");
                catalogCount++;
            } else {
                System.out.println("  Gaia DR3: not selected");
            }
            if (this.getPanstarrsBoxSelected().booleanValue()) {
                System.out.println("  PanSTARRS: SELECTED");
                catalogCount++;
            } else {
                System.out.println("  PanSTARRS: not selected");
            }
            if (this.getSdssBoxSelected().booleanValue()) {
                System.out.println("  SDSS-DR12: SELECTED");
                catalogCount++;
            } else {
                System.out.println("  SDSS-DR12: not selected");
            }
            
            if (catalogCount == 0) {
                setLoadingIndicatorAsync(false);
                return;
            }
            
            System.out.println("Loading " + catalogCount + " external catalog(s)...");
            
            // Load primary catalog (first selected) - this will be displayed as points
            String primaryCatalog = "";
            if (this.getApass9BoxSelected().booleanValue()) {
                System.out.println("Loading APASS9 as primary catalog...");
                this.getApass9Data();
                primaryCatalog = "APASS9";
            } else if (this.getGaiaDR2BoxSelected().booleanValue()) {
                System.out.println("Loading Gaia DR2 as primary catalog...");
                this.getGaiaDR2Data();
                primaryCatalog = "GaiaDR2";
            } else if (this.getGaiaDR3BoxSelected().booleanValue()) {
                System.out.println("Loading Gaia DR3 as primary catalog...");
                this.getGaiaDR3Data();
                primaryCatalog = "GaiaDR3";
            } else if (this.getPanstarrsBoxSelected().booleanValue()) {
                System.out.println("Loading PanSTARRS DR1 as primary catalog...");
                this.getPanstarrsData();
                primaryCatalog = "PanSTARRS";
            } else if (this.getSdssBoxSelected().booleanValue()) {
                System.out.println("DEBUG: SDSS checkbox is SELECTED");
                System.out.println("Loading SDSS DR12 as primary catalog...");
                this.getSdssData();
                primaryCatalog = "SDSS";
            } else {
                System.out.println("DEBUG: No catalog selected or catalog check failed");
                System.out.println("  APASS9: " + (this.getApass9BoxSelected() != null ? this.getApass9BoxSelected().booleanValue() : "null"));
                System.out.println("  GaiaDR2: " + (this.getGaiaDR2BoxSelected() != null ? this.getGaiaDR2BoxSelected().booleanValue() : "null"));
                System.out.println("  GaiaDR3: " + (this.getGaiaDR3BoxSelected() != null ? this.getGaiaDR3BoxSelected().booleanValue() : "null"));
                System.out.println("  PanSTARRS: " + (this.getPanstarrsBoxSelected() != null ? this.getPanstarrsBoxSelected().booleanValue() : "null"));
                System.out.println("  SDSS: " + (this.getSdssBoxSelected() != null ? this.getSdssBoxSelected().booleanValue() : "null"));
            }
            
            // Load additional catalogs into secondary storage for cross-matching
            if (catalogCount > 1) {
                System.out.println("Loading additional catalog(s) for cross-matching...");
                System.out.println("Primary catalog is: " + primaryCatalog);
                
                // Load each selected catalog (except the primary) into secondary storage
                if (this.getApass9BoxSelected().booleanValue() && !primaryCatalog.equals("APASS9")) {
                    System.out.println("Loading APASS9 as secondary catalog...");
                    this.loadApass9Secondary();
                }
                if (this.getGaiaDR2BoxSelected().booleanValue() && !primaryCatalog.equals("GaiaDR2")) {
                    System.out.println("Loading Gaia DR2 as secondary catalog...");
                    this.loadGaiaDR2Secondary();
                }
                if (this.getGaiaDR3BoxSelected().booleanValue() && !primaryCatalog.equals("GaiaDR3")) {
                    System.out.println("Loading Gaia DR3 as secondary catalog...");
                    this.loadGaiaDR3Secondary();
                }
                if (this.getPanstarrsBoxSelected().booleanValue() && !primaryCatalog.equals("PanSTARRS")) {
                    System.out.println("Loading PanSTARRS as secondary catalog...");
                    this.loadPanstarrsSecondary();
                }
                if (this.getSdssBoxSelected().booleanValue() && !primaryCatalog.equals("SDSS")) {
                    System.out.println("Loading SDSS-DR12 as secondary catalog...");
                    this.loadSdssSecondary();
                }
                
                System.out.println("Secondary catalog data loaded: " + secondaryCatalogData.size() + " stars");
                
                // Merge secondary catalog data into main arrays for display
                System.out.println("Merging secondary catalogs into main display arrays...");
                mergeSecondaryCatalogsIntoMainArrays();
            }
        } catch (Exception e) {
            // Hide loading indicator
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Error accessing external data source: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void getApass9Data() {
        try {
            long totalStartTime = System.currentTimeMillis();
            
            // Construct ADQL query for APASS DR9 catalog via CDS Vizier TAP
            String tapUrl = CDSMirrorSelector.getSelectedTapUrl();
            String adqlQuery = String.format(
                "SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, Field, nobs, mobs, " +
                "\"B-V\", \"e_B-V\", Vmag, e_Vmag, Bmag, e_Bmag, " +
                "\"g'mag\", \"e_g'mag\", \"r'mag\", \"e_r'mag\", " +
                "\"i'mag\", \"e_i'mag\", recno " +
                "FROM \"II/336/apass9\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', RAJ2000, DEJ2000), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f)) " +
                "AND Vmag IS NOT NULL AND Vmag <= %.1f " +
                "ORDER BY Vmag",
                this.getCentralRA(), this.getCentralDec(), 
                this.getFieldSize() / 2.0, this.getLimitingMag()
            );

            // Build the TAP query URL
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");

            System.out.println("\n========== APASS9 CATALOG LOADING ==========");
            System.out.println("Service: CDS VizieR TAP (II/336/apass9)");
            System.out.printf("Field: RA=%.6f, Dec=%.6f, Radius=%.2f arcmin, MagLimit=%.1f\n", 
                this.getCentralRA(), this.getCentralDec(), 
                this.getFieldSize() * 60.0, this.getLimitingMag());
            System.out.println("ADQL Query:");
            System.out.println(adqlQuery);
            
            // Open connection and track download
            System.out.println("Connecting to VizieR...");
            long stepStartTime = System.currentTimeMillis();
            URL url = new URL(queryUrl);
            System.out.printf("  → Opening connection to %s\n", url.getHost());
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);  // 30 second connection timeout
            connection.setReadTimeout(this.getCatalogReadTimeoutSeconds() * 1000);  // User-configurable read timeout
            System.out.printf("  → Sending TAP request...\n");
            InputStream inputStream = connection.getInputStream();
            int responseCode = connection.getResponseCode();
            System.out.printf("  → Response code: %d, downloading VOTable...\n", responseCode);
            
            // Wrap in a buffered stream to count bytes
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            inputStream.close();
            
            long downloadTime = System.currentTimeMillis() - stepStartTime;
            double downloadRate = (totalBytes / 1024.0) / (downloadTime / 1000.0);
            System.out.printf("✓ Downloaded %,d bytes in %.2f sec (%.1f KB/s)\n", 
                totalBytes, downloadTime / 1000.0, downloadRate);
            
            // Parse XML document
            System.out.print("Parsing XML... ");
            stepStartTime = System.currentTimeMillis();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
            long parseTime = System.currentTimeMillis() - stepStartTime;
            System.out.printf("✓ Completed in %.2f sec\n", parseTime / 1000.0);
            
            // Parse VOTable response
            System.out.print("Processing catalog data... ");
            stepStartTime = System.currentTimeMillis();
            this.parseApass9VOTable(document);
            long processTime = System.currentTimeMillis() - stepStartTime;
            System.out.printf("✓ Found %,d stars in %.2f sec\n", 
                this.getTotalCount(), processTime / 1000.0);
            
            long totalTime = System.currentTimeMillis() - totalStartTime;
            System.out.printf("TOTAL TIME: %.2f sec\n", totalTime / 1000.0);
            System.out.println("===========================================\n");
            
            // Hide loading indicator
            setLoadingIndicatorAsync(false);
            // Close the EnterStar dialog when data loading is complete
            disposeStarWindowAsync();
            
            System.out.printf("DEBUG APASS9 Results: Found %d stars\n", this.getTotalCount());
            
            if (this.getTotalCount() == 0) {
                showMessageDialogAsync(null, 
                    "No APASS9 data found in this field.\nTry increasing the field size or limiting magnitude.", 
                    "No Data Found", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Process the data for plotting
                this.processApass9Data();
                System.out.printf("DEBUG: Processed %d APASS9 stars for plotting\n", this.getTotalCount());
            }
            
        } catch (MalformedURLException e) {
            // Hide loading indicator
            setLoadingIndicatorAsync(false);
            // Close the EnterStar dialog
            disposeStarWindowAsync();
            showMessageDialogAsync(null, "Invalid URL for APASS9 query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException e) {
            // Hide loading indicator
            setLoadingIndicatorAsync(false);
            // Close the EnterStar dialog
            disposeStarWindowAsync();
            showMessageDialogAsync(null, "URL encoding error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // Hide loading indicator
            setLoadingIndicatorAsync(false);
            // Close the EnterStar dialog
            disposeStarWindowAsync();
            showMessageDialogAsync(null, "Error querying APASS9 data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void parseApass9VOTable(Document document) {
        try {
            // Parse VOTable format from CDS Vizier - look for TABLEDATA section
            NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
            
            System.out.printf("DEBUG: Found %d TABLEDATA elements\n", tableDataNodes.getLength());
            
            if (tableDataNodes.getLength() == 0) {
                // System.out.println("DEBUG: No TABLEDATA section found in VOTable");
                this.setTotalCount(0);
                return;
            }
            
            // Get TR elements within TABLEDATA
            Element tableDataElement = (Element) tableDataNodes.item(0);
            NodeList tableData = tableDataElement.getElementsByTagName("TR");
            int dataRows = tableData.getLength();
            
            System.out.printf("DEBUG: Found %d TR elements in TABLEDATA\n", dataRows);
            
            // Skip header row - count actual data rows
            int actualDataCount = 0;
            for (int i = 0; i < dataRows; i++) { // Start from 0 since TABLEDATA contains only data rows
                Element row = (Element) tableData.item(i);
                NodeList cells = row.getElementsByTagName("TD");
                if (cells.getLength() >= 19) { // Updated for 19 columns without u_e_ columns
                    actualDataCount++;
                }
            }
            
            System.out.printf("DEBUG: Counted %d valid data rows (with >= 19 columns)\n", actualDataCount);
            
            this.setTotalCount(actualDataCount);
            
            if (actualDataCount > 0) {
                this.initializeArrays(5, actualDataCount);
                
                int recordIndex = 0;
                for (int i = 0; i < dataRows && recordIndex < actualDataCount; i++) { // Start from 0 for TABLEDATA
                    Element row = (Element) tableData.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 19) { // Updated for 19 columns without u_e_ columns
                        try {
                            // Parse APASS9 data columns according to our SELECT statement:
                            // RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, Field, nobs, mobs,
                            // B-V, e_B-V, Vmag, e_Vmag, Bmag, e_Bmag,
                            // g'mag, e_g'mag, r'mag, e_r'mag, i'mag, e_i'mag, recno
                            
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double e_ra = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 99.999);
                            double e_dec = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 99.999);
                            
                            // Observation counts (Field is column 4, nobs is column 5, mobs is column 6)
                            int nobs = this.parseIntOrDefault(cells.item(5).getTextContent().trim(), 0);
                            
                            // Photometric data (updated column indices)
                            double bMinusV = this.parseDoubleOrDefault(cells.item(7).getTextContent().trim(), 99.999);
                            double e_bMinusV = this.parseDoubleOrDefault(cells.item(8).getTextContent().trim(), 99.999);
                            double vmag = this.parseDoubleOrDefault(cells.item(9).getTextContent().trim(), 99.999);
                            double e_vmag = this.parseDoubleOrDefault(cells.item(10).getTextContent().trim(), 99.999);
                            double bmag = this.parseDoubleOrDefault(cells.item(11).getTextContent().trim(), 99.999);
                            double e_bmag = this.parseDoubleOrDefault(cells.item(12).getTextContent().trim(), 99.999);
                            
                            // Sloan photometry (updated column indices)
                            double g_mag = this.parseDoubleOrDefault(cells.item(13).getTextContent().trim(), 99.999);
                            double e_g_mag = this.parseDoubleOrDefault(cells.item(14).getTextContent().trim(), 99.999);
                            double r_mag = this.parseDoubleOrDefault(cells.item(15).getTextContent().trim(), 99.999);
                            double e_r_mag = this.parseDoubleOrDefault(cells.item(16).getTextContent().trim(), 99.999);
                            double i_mag = this.parseDoubleOrDefault(cells.item(17).getTextContent().trim(), 99.999);
                            double e_i_mag = this.parseDoubleOrDefault(cells.item(18).getTextContent().trim(), 99.999);
                            
                            String recno = cells.item(19).getTextContent().trim();
                            
                            // Set core data in arrays (using existing Seqplot structure)
                            this.setName(recordIndex, "APASS9_" + recno);
                            this.setRa(recordIndex, ra);
                            this.setDec(recordIndex, dec);
                            this.setVmag(recordIndex, vmag);
                            this.setEv(recordIndex, e_vmag);
                            
                            // Store number of observations
                            this.setNobs(recordIndex, nobs);
                            
                            // Store additional APASS9 photometric data
                            this.bmag[recordIndex] = bmag;
                            this.e_bmag[recordIndex] = e_bmag;
                            this.g_prime_mag[recordIndex] = g_mag;
                            this.e_g_prime_mag[recordIndex] = e_g_mag;
                            this.r_prime_mag[recordIndex] = r_mag;
                            this.e_r_prime_mag[recordIndex] = e_r_mag;
                            this.i_prime_mag[recordIndex] = i_mag;
                            this.e_i_prime_mag[recordIndex] = e_i_mag;
                            this.coord_error_ra[recordIndex] = e_ra;
                            this.coord_error_dec[recordIndex] = e_dec;
                            
                            // Calculate B-V color index
                            if (bMinusV != 99.999) {
                                // Use catalog's B-V if available
                                this.setBMinusV(recordIndex, bMinusV);
                                this.setEbv(recordIndex, e_bMinusV);
                            } else if (bmag != 99.999 && vmag != 99.999) {
                                // Calculate B-V from individual magnitudes
                                this.setBMinusV(recordIndex, bmag - vmag);
                                this.setEbv(recordIndex, Math.sqrt(e_bmag * e_bmag + e_vmag * e_vmag));
                            } else {
                                this.setBMinusV(recordIndex, 99.999);
                                this.setEbv(recordIndex, 99.999);
                            }
                            
                            // Set other fields with defaults
                            this.setUMinusB(recordIndex, 99.999);
                            this.setVMinusR(recordIndex, 99.999);
                            this.setRMinusI(recordIndex, 99.999);
                            this.setVMinusI(recordIndex, 99.999);
                            this.setEub(recordIndex, 99.999);
                            this.setEvr(recordIndex, 99.999);
                            this.setEri(recordIndex, 99.999);
                            this.setEvi(recordIndex, 99.999);
                            this.setNobs(recordIndex, 1);
                            this.setMobs(recordIndex, 1);
                            this.setSource(recordIndex, 29); // Special source number for APASS9
                            this.setRaerr(recordIndex, 0.1); // Default positional error
                            this.setDecerr(recordIndex, 0.1);
                            
                            recordIndex++;
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing APASS9 data row " + i + ": " + e.getMessage());
                        }
                    }
                }
                
                // Update actual count in case some rows failed to parse
                this.setTotalCount(recordIndex);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing APASS9 VOTable: " + e.getMessage());
            this.setTotalCount(0);
        }
    }

    public double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            if (value == null || value.trim().isEmpty() || value.trim().equals("") || value.trim().equals("null")) {
                return defaultValue;
            }
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int parseIntOrDefault(String value, int defaultValue) {
        try {
            if (value == null || value.trim().isEmpty() || value.trim().equals("") || value.trim().equals("null")) {
                return defaultValue;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void processApass9Data() {
        try {
            // Calculate coordinate ranges and series assignments for APASS9 data
            this.findVariables(); // Look for variables in the field
            
            for (int i = 0; i < this.getTotalCount(); i++) {
                // Assign series number based on magnitude (all APASS9 stars in series 0 for now)
                int series = this.assignSeriesNumber(this.getBMinusV(i), this.getRa(i), 
                    this.getRaerr(i), this.getDec(i), this.getDecerr(i), 
                    this.getVmag(i), this.getUpperLimitingMag(), i);
                    
                this.seriesValue[i] = series;
                
                // Update magnitude range
                if (this.getVmag(i) < this.minZ && this.getVmag(i) != 99.999) {
                    this.minZ = this.getVmag(i);
                }
                if (this.getVmag(i) > this.maxZ && this.getVmag(i) != 99.999) {
                    this.maxZ = this.getVmag(i);
                }
            }
            
            // Set coordinate ranges
            this.minRa = this.getLowerRA();
            this.maxRa = this.getUpperRA();
            this.minDec = this.getLowerDec();
            this.maxDec = this.getUpperDec();
            
            System.out.printf("DEBUG: Coordinate bounds set:\n");
            System.out.printf("  RA bounds: %.6f to %.6f\n", this.minRa, this.maxRa);
            System.out.printf("  Dec bounds: %.6f to %.6f\n", this.minDec, this.maxDec);
            
            // Convert to tangent plane coordinates for plotting
            this.convertToTangentPlane();
            
            // DEBUG: Check coordinate conversion
            System.out.printf("DEBUG: Coordinate ranges after conversion:\n");
            System.out.printf("  X range: %.6f to %.6f\n", this.minX, this.maxX);
            System.out.printf("  Y range: %.6f to %.6f\n", this.minY, this.maxY);
            System.out.printf("  Magnitude range: %.2f to %.2f\n", this.minZ, this.maxZ);
            
            // Set up plotting ranges
            this.domainMin = this.minX;
            this.domainMax = this.maxX;
            this.domainRange = new Range(this.minX, this.maxX);
            this.rangeMin = this.minY;
            this.rangeMax = this.maxY;
            this.range = new Range(this.minY, this.maxY);
            
            // Scale dots and update plot
            this.scaleDots();
            
            // Notify the plot to refresh
            this.fireDatasetChanged();
            
        } catch (Exception e) {
            System.err.println("Error processing APASS9 data: " + e.getMessage());
        }
    }

    public void getGaiaDR2Data() {
        try {
            long totalStartTime = System.currentTimeMillis();
            
            // Construct ADQL query for Gaia DR2 catalog via ESA Gaia Archive TAP
            // Using official ESA service - much faster than VizieR mirror
            // Use box query (RA/Dec BETWEEN) for better performance
            String tapUrl = "https://gea.esac.esa.int/tap-server/tap/sync";
            
            double raMin = this.getLowerRA();
            double raMax = this.getUpperRA();
            double decMin = this.getLowerDec();
            double decMax = this.getUpperDec();
            
            // ESA Gaia Archive uses gaiadr2.gaia_source table (not VizieR's I/345/gaia2)
            // Column names are the same but no quotes needed around table name
            String adqlQuery = String.format(
                "SELECT TOP 5000 ra, dec, parallax, parallax_error, " +
                "phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag, phot_g_n_obs, source_id " +
                "FROM gaiadr2.gaia_source " +
                "WHERE ra BETWEEN %.6f AND %.6f " +
                "AND dec BETWEEN %.6f AND %.6f " +
                "AND phot_bp_mean_mag IS NOT NULL AND phot_bp_mean_mag < 19 " +
                "AND phot_g_mean_mag <= %.1f",
                raMin, raMax, decMin, decMax, this.getLimitingMag()
            );
            
            // Use FORMAT=votable_plain to get TABLEDATA instead of BINARY2
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable_plain&QUERY=" + 
                              URLEncoder.encode(adqlQuery, "UTF-8");
            
            System.out.println("\n========== GAIA DR2 CATALOG LOADING ==========");
            System.out.println("Service: ESA Gaia Archive TAP (gaiadr2.gaia_source)");
            System.out.printf("Field: RA=%.6f-%.6f, Dec=%.6f-%.6f, MagLimit=%.1f\n", 
                raMin, raMax, decMin, decMax, this.getLimitingMag());
            System.out.println("ADQL Query:");
            System.out.println(adqlQuery);
            
            // Open connection and track download
            System.out.print("Connecting to ESA Gaia Archive... ");
            long stepStartTime = System.currentTimeMillis();
            URL url = new URL(queryUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);  // 30 second connection timeout
            connection.setReadTimeout(this.getCatalogReadTimeoutSeconds() * 1000);  // User-configurable read timeout
            InputStream inputStream = connection.getInputStream();
            
            // Wrap in a buffered stream to count bytes
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            inputStream.close();
            
            long downloadTime = System.currentTimeMillis() - stepStartTime;
            double downloadRate = (totalBytes / 1024.0) / (downloadTime / 1000.0);
            System.out.printf("✓ Downloaded %,d bytes in %.2f sec (%.1f KB/s)\n", 
                totalBytes, downloadTime / 1000.0, downloadRate);
            
            // Parse XML document
            System.out.print("Parsing XML... ");
            stepStartTime = System.currentTimeMillis();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
            long parseXmlTime = System.currentTimeMillis() - stepStartTime;
            System.out.printf("✓ Completed in %.2f sec\n", parseXmlTime / 1000.0);
            
            // Parse the VOTable and populate data arrays
            System.out.print("Processing catalog data... ");
            stepStartTime = System.currentTimeMillis();
            this.parseGaiaDR2VOTable(document);
            long parseDataTime = System.currentTimeMillis() - stepStartTime;
            System.out.printf("✓ Found %,d stars in %.2f sec\n", 
                this.getTotalCount(), parseDataTime / 1000.0);
            
            // Hide loading indicator after successful query
            setLoadingIndicatorAsync(false);
            
            if (this.getTotalCount() == 0) {
                showMessageDialogAsync(null, 
                    "No Gaia DR2 data found in this field.\nTry increasing the field size.", 
                    "No Data Found", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                this.processGaiaDR2Data();
            }
            
            long totalTime = System.currentTimeMillis() - totalStartTime;
            System.out.printf("TOTAL TIME: %.2f sec\n", totalTime / 1000.0);
            System.out.println("==============================================\n");
            
            // Close the star window
            disposeStarWindowAsync();
            
        } catch (MalformedURLException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Invalid URL for Gaia DR2 query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Encoding error in Gaia DR2 query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Error querying Gaia DR2 data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void parseGaiaDR2VOTable(Document document) {
        try {
            // Extract TABLEDATA section
            NodeList tableDatas = document.getElementsByTagName("TABLEDATA");
            if (tableDatas.getLength() == 0) {
                System.err.println("No TABLEDATA found in Gaia DR2 VOTable response");
                this.setTotalCount(0);
                return;
            }
            
            Element tableData = (Element) tableDatas.item(0);
            NodeList trNodes = tableData.getElementsByTagName("TR");
            int dataRows = trNodes.getLength();
            
            System.out.printf("DEBUG: Found %d data rows in Gaia DR2 VOTable\n", dataRows);
            
            if (dataRows > 0) {
                // Count actual valid rows (now expecting 9 columns: ra, dec, parallax, parallax_error, G, BP, RP, phot_g_n_obs, source_id)
                int actualDataCount = 0;
                for (int i = 0; i < dataRows; i++) {
                    Element row = (Element) trNodes.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    if (cells.getLength() >= 9) {
                        actualDataCount++;
                    }
                }
                
                System.out.printf("DEBUG: Counted %d valid data rows\n", actualDataCount);
                this.setTotalCount(actualDataCount);
                this.initializeArrays(5, actualDataCount);
                
                int recordIndex = 0;
                for (int i = 0; i < dataRows && recordIndex < actualDataCount; i++) {
                    Element row = (Element) trNodes.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 9) {
                        try {
                            // Parse Gaia DR2 data columns: ra, dec, parallax, parallax_error,
                            // phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag, phot_g_n_obs, source_id
                            
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double plx = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 99.999);
                            double e_plx = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 99.999);
                            
                            // Photometric data (no individual errors in this query to save time)
                            double gmag = this.parseDoubleOrDefault(cells.item(4).getTextContent().trim(), 99.999);
                            double bpmag = this.parseDoubleOrDefault(cells.item(5).getTextContent().trim(), 99.999);
                            double rpmag = this.parseDoubleOrDefault(cells.item(6).getTextContent().trim(), 99.999);
                            int nobs = this.parseIntOrDefault(cells.item(7).getTextContent().trim(), 0);
                            
                            String source = cells.item(8).getTextContent().trim();
                            
                            // Use nominal errors since we're not querying individual mag errors
                            double e_gmag = 0.003;  // Typical Gaia G-band error
                            double e_bpmag = 0.01;  // Typical BP error
                            double e_rpmag = 0.01;  // Typical RP error
                            
                            // Transform Gaia G, BP, RP to Johnson-Cousins V, R, I
                            // Using Evans et al. 2018 (A&A 616, A4) transformations
                            double vmag = 99.999, e_vmag = 99.999;
                            double rmag = 99.999, e_rmag = 99.999;
                            double imag = 99.999, e_imag = 99.999;
                            double vi = 99.999, e_vi = 99.999;
                            
                            if (gmag != 99.999 && bpmag != 99.999 && rpmag != 99.999) {
                                double bp_rp = bpmag - rpmag;
                                double e_bp_rp = Math.sqrt(e_bpmag * e_bpmag + e_rpmag * e_rpmag);
                                
                                double bp_rp_sq = bp_rp * bp_rp;
                                
                                // V = G - (-0.01760 - 0.006860*(BP-RP) - 0.1732*(BP-RP)^2)
                                double v_correction = -(-0.01760 - 0.006860 * bp_rp - 0.1732 * bp_rp_sq);
                                vmag = gmag + v_correction;
                                e_vmag = Math.sqrt(e_gmag * e_gmag + (0.006860 + 2 * 0.1732 * bp_rp) * (0.006860 + 2 * 0.1732 * bp_rp) * e_bp_rp * e_bp_rp);
                                
                                // R = G - (-0.003226 + 0.3833*(BP-RP) - 0.1345*(BP-RP)^2)
                                double r_correction = -(-0.003226 + 0.3833 * bp_rp - 0.1345 * bp_rp_sq);
                                rmag = gmag + r_correction;
                                e_rmag = Math.sqrt(e_gmag * e_gmag + (0.3833 - 2 * 0.1345 * bp_rp) * (0.3833 - 2 * 0.1345 * bp_rp) * e_bp_rp * e_bp_rp);
                                
                                // I = G - (-0.02085 + 0.7419*(BP-RP) - 0.09631*(BP-RP)^2)
                                double i_correction = -(-0.02085 + 0.7419 * bp_rp - 0.09631 * bp_rp_sq);
                                imag = gmag + i_correction;
                                e_imag = Math.sqrt(e_gmag * e_gmag + (0.7419 - 2 * 0.09631 * bp_rp) * (0.7419 - 2 * 0.09631 * bp_rp) * e_bp_rp * e_bp_rp);
                                
                                // Calculate V-I as the primary color index
                                if (vmag != 99.999 && imag != 99.999) {
                                    vi = vmag - imag;
                                    e_vi = Math.sqrt(e_vmag * e_vmag + e_imag * e_imag);
                                }
                            }
                            
                            // Set core data in arrays (using existing Seqplot structure)
                            this.setName(recordIndex, "Gaia_" + source);
                            this.setRa(recordIndex, ra);
                            this.setDec(recordIndex, dec);
                            
                            // Use transformed V magnitude as the primary magnitude
                            this.setVmag(recordIndex, vmag);
                            this.setEv(recordIndex, e_vmag);
                            
                            // Store V-I as the primary color index for Gaia
                            if (vi != 99.999) {
                                this.setBMinusV(recordIndex, vi);  // Store V-I in the B-V field
                                this.setEbv(recordIndex, e_vi);
                            } else {
                                this.setBMinusV(recordIndex, 99.999);
                                this.setEbv(recordIndex, 99.999);
                            }
                            
                            // Store number of G-band observations
                            this.setNobs(recordIndex, nobs);
                            
                            // Calculate and store other color indices from transformed magnitudes
                            this.setUMinusB(recordIndex, 99.999);
                            this.setEub(recordIndex, 99.999);
                            
                            // V-R
                            if (vmag != 99.999 && rmag != 99.999) {
                                double vr = vmag - rmag;
                                double e_vr = Math.sqrt(e_vmag * e_vmag + e_rmag * e_rmag);
                                this.setVMinusR(recordIndex, vr);
                                this.setEvr(recordIndex, e_vr);
                            } else {
                                this.setVMinusR(recordIndex, 99.999);
                                this.setEvr(recordIndex, 99.999);
                            }
                            
                            // R-I
                            if (rmag != 99.999 && imag != 99.999) {
                                double ri = rmag - imag;
                                double e_ri = Math.sqrt(e_rmag * e_rmag + e_imag * e_imag);
                                this.setRMinusI(recordIndex, ri);
                                this.setEri(recordIndex, e_ri);
                            } else {
                                this.setRMinusI(recordIndex, 99.999);
                                this.setEri(recordIndex, 99.999);
                            }
                            
                            // V-I (already calculated above and stored in vi, e_vi)
                            if (vi != 99.999) {
                                this.setVMinusI(recordIndex, vi);
                                this.setEvi(recordIndex, e_vi);
                            } else {
                                this.setVMinusI(recordIndex, 99.999);
                                this.setEvi(recordIndex, 99.999);
                            }
                            this.setNobs(recordIndex, 1);
                            this.setMobs(recordIndex, 1);
                            this.setSource(recordIndex, 48); // Special source number for Gaia DR2
                            
                            // Gaia has excellent astrometry, estimate from parallax error
                            double pos_error = (e_plx != 99.999) ? Math.max(0.001, e_plx / 3600.0) : 0.001;
                            this.setRaerr(recordIndex, pos_error);
                            this.setDecerr(recordIndex, pos_error);
                            
                            recordIndex++;
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing Gaia DR2 data row " + i + ": " + e.getMessage());
                        }
                    }
                }
                
                // Update actual count in case some rows failed to parse
                this.setTotalCount(recordIndex);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing Gaia DR2 VOTable: " + e.getMessage());
            this.setTotalCount(0);
        }
    }

    public void processGaiaDR2Data() {
        try {
            // Calculate coordinate ranges and series assignments for Gaia DR2 data
            this.findVariables(); // Look for variables in the field
            
            for (int i = 0; i < this.getTotalCount(); i++) {
                // Assign series number based on magnitude (all Gaia DR2 stars in series 0 for now)
                int series = this.assignSeriesNumber(this.getBMinusV(i), this.getRa(i), 
                    this.getRaerr(i), this.getDec(i), this.getDecerr(i), 
                    this.getVmag(i), this.getUpperLimitingMag(), i);
                    
                this.seriesValue[i] = series;
                
                // Update magnitude range
                if (this.getVmag(i) < this.minZ && this.getVmag(i) != 99.999) {
                    this.minZ = this.getVmag(i);
                }
                if (this.getVmag(i) > this.maxZ && this.getVmag(i) != 99.999) {
                    this.maxZ = this.getVmag(i);
                }
            }
            
            // Set coordinate ranges
            this.minRa = this.getLowerRA();
            this.maxRa = this.getUpperRA();
            this.minDec = this.getLowerDec();
            this.maxDec = this.getUpperDec();
            
            System.out.printf("DEBUG: Coordinate bounds set:\n");
            System.out.printf("  RA bounds: %.6f to %.6f\n", this.minRa, this.maxRa);
            System.out.printf("  Dec bounds: %.6f to %.6f\n", this.minDec, this.maxDec);
            
            // Convert to tangent plane coordinates for plotting
            this.convertToTangentPlane();
            
            // DEBUG: Check coordinate conversion
            System.out.printf("DEBUG: Coordinate ranges after conversion:\n");
            System.out.printf("  X range: %.6f to %.6f\n", this.minX, this.maxX);
            System.out.printf("  Y range: %.6f to %.6f\n", this.minY, this.maxY);
            System.out.printf("  Magnitude range: %.2f to %.2f\n", this.minZ, this.maxZ);
            
            // Set up plotting ranges
            this.domainMin = this.minX;
            this.domainMax = this.maxX;
            this.domainRange = new Range(this.minX, this.maxX);
            this.rangeMin = this.minY;
            this.rangeMax = this.maxY;
            this.range = new Range(this.minY, this.maxY);
            
            // Scale dots and update plot
            this.scaleDots();
            
            // Notify the plot to refresh
            this.fireDatasetChanged();
            
        } catch (Exception e) {
            System.err.println("Error processing Gaia DR2 data: " + e.getMessage());
        }
    }

    public void getGaiaDR3Data() {
        try {
            long totalStartTime = System.currentTimeMillis();
            
            // Construct ADQL query for Gaia DR3 catalog via ESA Gaia Archive TAP
            // Using official ESA service - much faster than VizieR mirror
            // Use box query (RA/Dec BETWEEN) for better performance
            String tapUrl = "https://gea.esac.esa.int/tap-server/tap/sync";
            
            double raMin = this.getLowerRA();
            double raMax = this.getUpperRA();
            double decMin = this.getLowerDec();
            double decMax = this.getUpperDec();
            
            // ESA Gaia Archive uses gaiadr3.gaia_source table
            String adqlQuery = String.format(
                "SELECT TOP 5000 ra, dec, parallax, parallax_error, " +
                "phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag, phot_g_n_obs, source_id " +
                "FROM gaiadr3.gaia_source " +
                "WHERE ra BETWEEN %.6f AND %.6f " +
                "AND dec BETWEEN %.6f AND %.6f " +
                "AND phot_bp_mean_mag IS NOT NULL AND phot_bp_mean_mag < 19 " +
                "AND phot_g_mean_mag <= %.1f",
                raMin, raMax, decMin, decMax, this.getLimitingMag()
            );
            
            // Use FORMAT=votable_plain to get TABLEDATA instead of BINARY2
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable_plain&QUERY=" + 
                              URLEncoder.encode(adqlQuery, "UTF-8");
            
            System.out.println("\n========== GAIA DR3 CATALOG LOADING ==========");
            System.out.println("Service: ESA Gaia Archive TAP (gaiadr3.gaia_source)");
            System.out.printf("Field: RA=%.6f-%.6f, Dec=%.6f-%.6f, MagLimit=%.1f\n", 
                raMin, raMax, decMin, decMax, this.getLimitingMag());
            System.out.println("ADQL Query:");
            System.out.println(adqlQuery);
            
            // Open connection and track download
            System.out.print("Connecting to ESA Gaia Archive... ");
            long stepStartTime = System.currentTimeMillis();
            URL url = new URL(queryUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);  // 30 second connection timeout
            connection.setReadTimeout(this.getCatalogReadTimeoutSeconds() * 1000);  // User-configurable read timeout
            InputStream inputStream = connection.getInputStream();
            
            // Wrap in a buffered stream to count bytes
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            inputStream.close();
            
            long downloadTime = System.currentTimeMillis() - stepStartTime;
            double downloadRate = (totalBytes / 1024.0) / (downloadTime / 1000.0);
            System.out.printf("✓ Downloaded %,d bytes in %.2f sec (%.1f KB/s)\n", 
                totalBytes, downloadTime / 1000.0, downloadRate);
            
            // Parse XML document
            System.out.print("Parsing XML... ");
            stepStartTime = System.currentTimeMillis();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
            long parseXmlTime = System.currentTimeMillis() - stepStartTime;
            System.out.printf("✓ Completed in %.2f sec\n", parseXmlTime / 1000.0);
            
            // Parse the VOTable and populate data arrays (use same parser as DR2)
            System.out.print("Processing catalog data... ");
            stepStartTime = System.currentTimeMillis();
            this.parseGaiaDR3VOTable(document);
            long parseDataTime = System.currentTimeMillis() - stepStartTime;
            System.out.printf("✓ Found %,d stars in %.2f sec\n", 
                this.getTotalCount(), parseDataTime / 1000.0);
            
            // Hide loading indicator after successful query
            setLoadingIndicatorAsync(false);
            
            if (this.getTotalCount() == 0) {
                showMessageDialogAsync(null, 
                    "No Gaia DR3 data found in this field.\nTry increasing the field size.", 
                    "No Data Found", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                this.processGaiaDR3Data();
            }
            
            long totalTime = System.currentTimeMillis() - totalStartTime;
            System.out.printf("TOTAL TIME: %.2f sec\n", totalTime / 1000.0);
            System.out.println("==============================================\n");
            
            // Close the star window
            disposeStarWindowAsync();
            
        } catch (MalformedURLException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Invalid URL for Gaia DR3 query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Encoding error in Gaia DR3 query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Error querying Gaia DR3 data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void parseGaiaDR3VOTable(Document document) {
        // Gaia DR3 uses same VOTable format as DR2, just different table name
        // Reuse the DR2 parser logic
        this.parseGaiaDR2VOTable(document);
    }

    public void processGaiaDR3Data() {
        // Gaia DR3 uses same processing as DR2
        this.processGaiaDR2Data();
    }

    public void getPanstarrsData() {
        try {
            long totalStartTime = System.currentTimeMillis();
            
            // Use CDS VizieR TAP - faster and more complete than MAST API
            // Catalog: II/349/ps1 (PanSTARRS DR1)
            String tapUrl = CDSMirrorSelector.getSelectedTapUrl();
            
            // Calculate magnitude constraint for r-band (roughly corresponds to V magnitude)
            // Add 1 mag buffer to ensure we don't miss faint stars after conversion
            double rMagLimit = this.getLimitingMag() + 1.0;
            
            // Build ADQL query for PanSTARRS DR1
            // Optimized query to avoid timeouts:
            // - Use BOX instead of CIRCLE (VizieR has better indexing for BOX queries)
            // - Added TOP 5000 to limit result set (sufficient for most fields)
            // - Removed ORDER BY (causes timeouts on large result sets)
            // - Reduced NOT NULL constraints (only require main photometry)
            // - Keep error columns in SELECT but don't require them (application handles nulls)
            // BOX expects width and height in degrees. getFieldSize() returns diameter in degrees.
            // Use full diameter as box size to match CIRCLE coverage
            double boxSizeDeg = this.getFieldSize();  // Field diameter in degrees
            String adqlQuery = String.format(
                "SELECT TOP 5000 RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, " +
                "gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID " +
                "FROM \"II/349/ps1\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', RAJ2000, DEJ2000), " +
                "BOX('ICRS', %.6f, %.6f, %.6f, %.6f)) " +
                "AND gmag IS NOT NULL " +
                "AND rmag IS NOT NULL " +
                "AND imag IS NOT NULL " +
                "AND rmag <= %.1f",
                this.getCentralRA(), this.getCentralDec(), 
                boxSizeDeg, boxSizeDeg, rMagLimit
            );
            
            // Build the TAP query URL
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            System.out.println("\n========== PANSTARRS DR1 CATALOG LOADING ==========");
            System.out.println("Service: CDS VizieR TAP (II/349/ps1)");
            System.out.printf("Field: RA=%.6f, Dec=%.6f, Radius=%.2f arcmin, MagLimit=%.1f (r<=%.1f)\n", 
                this.getCentralRA(), this.getCentralDec(), 
                this.getFieldSize() * 60.0, this.getLimitingMag(), rMagLimit);
            System.out.println("ADQL Query:");
            System.out.println(adqlQuery);
            
            // Open connection and track download
            System.out.print("Connecting to VizieR... ");
            long stepStartTime = System.currentTimeMillis();
            URL url = new URL(queryUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);  // 30 second connection timeout
            connection.setReadTimeout(this.getCatalogReadTimeoutSeconds() * 1000);  // User-configurable read timeout
            InputStream inputStream = connection.getInputStream();
            
            // Wrap in a buffered stream to count bytes
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            inputStream.close();
            
            long downloadTime = System.currentTimeMillis() - stepStartTime;
            double downloadRate = (totalBytes / 1024.0) / (downloadTime / 1000.0);
            System.out.printf("✓ Downloaded %,d bytes in %.2f sec (%.1f KB/s)\n", 
                totalBytes, downloadTime / 1000.0, downloadRate);
            
            // Parse XML document
            System.out.print("Parsing XML... ");
            stepStartTime = System.currentTimeMillis();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
            long parseXmlTime = System.currentTimeMillis() - stepStartTime;
            System.out.printf("✓ Completed in %.2f sec\n", parseXmlTime / 1000.0);
            
            // Parse the VOTable and populate data arrays
            System.out.print("Processing catalog data... ");
            stepStartTime = System.currentTimeMillis();
            this.parsePanstarrsVOTableVizier(document);
            long parseDataTime = System.currentTimeMillis() - stepStartTime;
            System.out.printf("✓ Found %,d stars in %.2f sec\n", 
                this.getTotalCount(), parseDataTime / 1000.0);
            
            // Hide loading indicator after successful query
            setLoadingIndicatorAsync(false);
            
            if (this.getTotalCount() == 0) {
                // Check if the field is outside PanSTARRS coverage (Dec < -30°)
                double dec = this.getCentralDec();
                if (dec < -30.0) {
                    showMessageDialogAsync(null, 
                        String.format("No coverage by PanSTARRS in this area.\n\n" +
                                    "PanSTARRS observes from Hawaii and has limited coverage south of Dec -30°.\n" +
                                    "Current field center: Dec = %.2f°", dec), 
                        "PanSTARRS Coverage Limitation", 
                        JOptionPane.WARNING_MESSAGE);
                } else {
                    showMessageDialogAsync(null, 
                        "No PanSTARRS DR1 data found in this field.\nTry increasing the field size.", 
                        "No Data Found", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                System.out.print("Converting photometry and preparing plot... ");
                stepStartTime = System.currentTimeMillis();
                this.processPanstarrsData();
                long processTime = System.currentTimeMillis() - stepStartTime;
                System.out.printf("✓ Completed in %.2f sec\n", processTime / 1000.0);
            }
            
            long totalTime = System.currentTimeMillis() - totalStartTime;
            System.out.printf("TOTAL TIME: %.2f sec\n", totalTime / 1000.0);
            System.out.println("===================================================\n");
            
            // Close the star window
            disposeStarWindowAsync();
            
        } catch (MalformedURLException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Invalid URL for PanSTARRS DR2 query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Encoding error in PanSTARRS DR2 query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Error querying PanSTARRS DR2 data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void parsePanstarrsVOTable(Document document) {
        try {
            // Extract TABLEDATA section
            NodeList tableDatas = document.getElementsByTagName("TABLEDATA");
            if (tableDatas.getLength() == 0) {
                System.err.println("No TABLEDATA found in PanSTARRS DR1 VOTable response");
                this.setTotalCount(0);
                return;
            }
            
            Element tableData = (Element) tableDatas.item(0);
            NodeList trNodes = tableData.getElementsByTagName("TR");
            int dataRows = trNodes.getLength();
            
            System.out.printf("DEBUG: Found %d data rows in PanSTARRS DR2 VOTable\n", dataRows);
            
            if (dataRows > 0) {
                // Count actual valid rows with valid photometry (not -999)
                int actualDataCount = 0;
                int skippedCount = 0;
                for (int i = 0; i < dataRows; i++) {
                    Element row = (Element) trNodes.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    if (cells.getLength() >= 11) {
                        try {
                            double gmag = Double.parseDouble(cells.item(4).getTextContent().trim());
                            double rmag = Double.parseDouble(cells.item(6).getTextContent().trim());
                            double imag = Double.parseDouble(cells.item(8).getTextContent().trim());
                            // Only count rows with valid photometry
                            if (gmag > -900 && rmag > -900 && imag > -900) {
                                actualDataCount++;
                            } else {
                                skippedCount++;
                                if (skippedCount <= 3) {
                                    System.out.printf("DEBUG: Skipping row %d with null photometry: g=%.2f, r=%.2f, i=%.2f\n", 
                                        i, gmag, rmag, imag);
                                }
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid rows
                        }
                    }
                }
                
                System.out.printf("DEBUG: Counted %d valid data rows with photometry (%d skipped with nulls)\n", 
                    actualDataCount, skippedCount);
                this.setTotalCount(actualDataCount);
                this.initializeArrays(5, actualDataCount);
                
                int recordIndex = 0;
                for (int i = 0; i < dataRows; i++) {
                    Element row = (Element) trNodes.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 11) {
                        try {
                            // Parse PanSTARRS DR2 data columns from MAST API:
                            // raMean, decMean, raMeanErr, decMeanErr,
                            // gMeanPSFMag, gMeanPSFMagErr, rMeanPSFMag, rMeanPSFMagErr,
                            // iMeanPSFMag, iMeanPSFMagErr, objID
                            
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double e_ra = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 0.1);
                            double e_dec = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 0.1);
                            
                            // Photometric data
                            double gmag = Double.parseDouble(cells.item(4).getTextContent().trim());
                            double e_gmag = this.parseDoubleOrDefault(cells.item(5).getTextContent().trim(), 0.01);
                            double rmag = Double.parseDouble(cells.item(6).getTextContent().trim());
                            double e_rmag = this.parseDoubleOrDefault(cells.item(7).getTextContent().trim(), 0.01);
                            double imag = Double.parseDouble(cells.item(8).getTextContent().trim());
                            double e_imag = this.parseDoubleOrDefault(cells.item(9).getTextContent().trim(), 0.01);
                            
                            String objID = cells.item(10).getTextContent().trim();
                            
                            // Skip -999.0 null values from MAST API
                            if (gmag < -900 || rmag < -900 || imag < -900) {
                                if (recordIndex < 3) {
                                    System.out.printf("DEBUG: Skipping row %d in parse loop: g=%.2f, r=%.2f, i=%.2f\n", 
                                        i, gmag, rmag, imag);
                                }
                                continue;
                            }
                            
                            // Transform PanSTARRS g, r, i to Johnson-Cousins V, B, R, I
                            // Using transformations: V = gmag - 0.59*(gmag-rmag) - 0.01
                            //                        B-V = gmag - rmag + 0.22
                            //                        V-R = 1.09*(rmag-imag) + 0.22
                            //                        R-I = (rmag-imag) + 0.21
                            //                        V-I = (V-R) + (R-I)
                            
                            double g_r = gmag - rmag;
                            double r_i_raw = rmag - imag;
                            
                            // V magnitude
                            double vmag = gmag - 0.59 * g_r - 0.01;
                            double e_vmag = Math.sqrt(e_gmag * e_gmag + (0.59 * 0.59) * (e_gmag * e_gmag + e_rmag * e_rmag));
                            
                            // B-V color
                            double bv = g_r + 0.22;
                            double e_bv = Math.sqrt(e_gmag * e_gmag + e_rmag * e_rmag);
                            
                            // V-R color
                            double vr = 1.09 * r_i_raw + 0.22;
                            double e_vr = 1.09 * Math.sqrt(e_rmag * e_rmag + e_imag * e_imag);
                            
                            // R-I color
                            double ri = r_i_raw + 0.21;
                            double e_ri = Math.sqrt(e_rmag * e_rmag + e_imag * e_imag);
                            
                            // V-I color (primary for PanSTARRS)
                            double vi = vr + ri;
                            double e_vi = Math.sqrt(e_vr * e_vr + e_ri * e_ri);
                            
                            // Set core data in arrays (using existing Seqplot structure)
                            this.setName(recordIndex, "PS1_" + objID);
                            this.setRa(recordIndex, ra);
                            this.setDec(recordIndex, dec);
                            
                            // Use transformed V magnitude as the primary magnitude
                            this.setVmag(recordIndex, vmag);
                            this.setEv(recordIndex, e_vmag);
                            
                            // Debug first few stars
                            if (recordIndex < 5) {
                                System.out.printf("DEBUG: Star %d - g=%.2f, r=%.2f, i=%.2f -> V=%.2f, V-I=%.2f\n",
                                    recordIndex, gmag, rmag, imag, vmag, vi);
                            }
                            
                            // Store V-I as the primary color index (in B-V field for compatibility)
                            this.setBMinusV(recordIndex, vi);
                            this.setEbv(recordIndex, e_vi);
                            
                            // Store other color indices
                            this.setVMinusR(recordIndex, vr);
                            this.setEvr(recordIndex, e_vr);
                            
                            this.setRMinusI(recordIndex, ri);
                            this.setEri(recordIndex, e_ri);
                            
                            this.setVMinusI(recordIndex, vi);
                            this.setEvi(recordIndex, e_vi);
                            
                            // U-B not available
                            this.setUMinusB(recordIndex, 99.999);
                            this.setEub(recordIndex, 99.999);
                            this.setNobs(recordIndex, 1);
                            this.setMobs(recordIndex, 1);
                            this.setSource(recordIndex, 46); // Special source number for PanSTARRS DR1
                            
                            // Set positional errors (convert from arcsec to degrees)
                            this.setRaerr(recordIndex, e_ra / 3600.0);
                            this.setDecerr(recordIndex, e_dec / 3600.0);
                            
                            recordIndex++;
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing PanSTARRS DR1 data row " + i + ": " + e.getMessage());
                        }
                    }
                }
                
                // Update actual count in case some rows failed to parse
                this.setTotalCount(recordIndex);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing PanSTARRS DR1 VOTable: " + e.getMessage());
            this.setTotalCount(0);
        }
    }

    public void parsePanstarrsVOTableVizier(Document document) {
        try {
            // Extract TABLEDATA section from VizieR TAP response
            NodeList tableDatas = document.getElementsByTagName("TABLEDATA");
            if (tableDatas.getLength() == 0) {
                System.err.println("No TABLEDATA found in PanSTARRS VizieR VOTable response");
                this.setTotalCount(0);
                return;
            }
            
            Element tableData = (Element) tableDatas.item(0);
            NodeList trNodes = tableData.getElementsByTagName("TR");
            int dataRows = trNodes.getLength();
            
            System.out.printf("DEBUG: Found %d data rows in PanSTARRS DR1 VizieR VOTable\n", dataRows);
            
            if (dataRows > 0) {
                this.setTotalCount(dataRows);
                this.initializeArrays(5, dataRows);
                
                int recordIndex = 0;
                for (int i = 0; i < dataRows; i++) {
                    Element row = (Element) trNodes.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 12) {
                        try {
                            // Parse PanSTARRS DR1 data columns from VizieR (II/349/ps1):
                            // RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000,
                            // gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID
                            
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double e_ra = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 0.1);
                            double e_dec = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 0.1);
                            
                            // Photometric data
                            double gmag = Double.parseDouble(cells.item(4).getTextContent().trim());
                            double e_gmag = this.parseDoubleOrDefault(cells.item(5).getTextContent().trim(), 0.01);
                            double rmag = Double.parseDouble(cells.item(6).getTextContent().trim());
                            double e_rmag = this.parseDoubleOrDefault(cells.item(7).getTextContent().trim(), 0.01);
                            double imag = Double.parseDouble(cells.item(8).getTextContent().trim());
                            double e_imag = this.parseDoubleOrDefault(cells.item(9).getTextContent().trim(), 0.01);
                            
                            int ng = this.parseIntOrDefault(cells.item(10).getTextContent().trim(), 0);
                            String objID = cells.item(11).getTextContent().trim();
                            
                            // Transform PanSTARRS g, r, i to Johnson-Cousins V, B, R, I
                            // Using transformations: V = gmag - 0.59*(gmag-rmag) - 0.01
                            //                        V-I = (V-R) + (R-I)
                            
                            double g_r = gmag - rmag;
                            double r_i_raw = rmag - imag;
                            
                            // V magnitude
                            double vmag = gmag - 0.59 * g_r - 0.01;
                            double e_vmag = Math.sqrt(e_gmag * e_gmag + (0.59 * 0.59) * (e_gmag * e_gmag + e_rmag * e_rmag));
                            
                            // B-V color
                            double bv = g_r + 0.22;
                            double e_bv = Math.sqrt(e_gmag * e_gmag + e_rmag * e_rmag);
                            
                            // V-R color
                            double vr = 1.09 * r_i_raw + 0.22;
                            double e_vr = 1.09 * Math.sqrt(e_rmag * e_rmag + e_imag * e_imag);
                            
                            // R-I color
                            double ri = r_i_raw + 0.21;
                            double e_ri = Math.sqrt(e_rmag * e_rmag + e_imag * e_imag);
                            
                            // V-I color (primary for PanSTARRS)
                            double vi = vr + ri;
                            double e_vi = Math.sqrt(e_vr * e_vr + e_ri * e_ri);
                            
                            // Set core data in arrays (using existing Seqplot structure)
                            this.setName(recordIndex, "PS1_" + objID);
                            this.setRa(recordIndex, ra);
                            this.setDec(recordIndex, dec);
                            
                            // Use transformed V magnitude as the primary magnitude
                            this.setVmag(recordIndex, vmag);
                            this.setEv(recordIndex, e_vmag);
                            
                            // Debug first few stars
                            if (recordIndex < 5) {
                                System.out.printf("DEBUG: Star %d - g=%.2f, r=%.2f, i=%.2f -> V=%.2f, V-I=%.2f\n",
                                    recordIndex, gmag, rmag, imag, vmag, vi);
                            }
                            
                            // Store V-I as the primary color index (in B-V field for compatibility)
                            this.setBMinusV(recordIndex, vi);
                            this.setEbv(recordIndex, e_vi);
                            
                            // Store positional errors
                            this.setRaerr(recordIndex, e_ra);
                            this.setDecerr(recordIndex, e_dec);
                            
                            // Store number of observations (Ng)
                            this.setNobs(recordIndex, ng);
                            
                            // Set source as PanSTARRS (46)
                            this.setSource(recordIndex, 46);
                            
                            recordIndex++;
                            
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing PanSTARRS row " + i + ": " + e.getMessage());
                        }
                    }
                }
                
                System.out.printf("DEBUG: Successfully parsed %d PanSTARRS DR1 stars from VizieR\n", recordIndex);
                
            } else {
                this.setTotalCount(0);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing PanSTARRS DR1 VizieR VOTable: " + e.getMessage());
            this.setTotalCount(0);
        }
    }

    public void processPanstarrsData() {
        try {
            // Calculate coordinate ranges and series assignments for PanSTARRS DR1 data
            this.findVariables(); // Look for variables in the field
            
            for (int i = 0; i < this.getTotalCount(); i++) {
                // Assign series number based on magnitude
                int series = this.assignSeriesNumber(this.getBMinusV(i), this.getRa(i), 
                    this.getRaerr(i), this.getDec(i), this.getDecerr(i), 
                    this.getVmag(i), this.getUpperLimitingMag(), i);
                    
                this.seriesValue[i] = series;
                
                // Update magnitude range
                if (this.getVmag(i) < this.minZ && this.getVmag(i) != 99.999) {
                    this.minZ = this.getVmag(i);
                }
                if (this.getVmag(i) > this.maxZ && this.getVmag(i) != 99.999) {
                    this.maxZ = this.getVmag(i);
                }
            }
            
            // Set coordinate ranges
            this.minRa = this.getLowerRA();
            this.maxRa = this.getUpperRA();
            this.minDec = this.getLowerDec();
            this.maxDec = this.getUpperDec();
            
            System.out.printf("DEBUG: Coordinate bounds set:\n");
            System.out.printf("  RA bounds: %.6f to %.6f\n", this.minRa, this.maxRa);
            System.out.printf("  Dec bounds: %.6f to %.6f\n", this.minDec, this.maxDec);
            
            // Convert to tangent plane coordinates for plotting
            this.convertToTangentPlane();
            
            // DEBUG: Check coordinate conversion
            System.out.printf("DEBUG: Coordinate ranges after conversion:\n");
            System.out.printf("  X range: %.6f to %.6f\n", this.minX, this.maxX);
            System.out.printf("  Y range: %.6f to %.6f\n", this.minY, this.maxY);
            System.out.printf("  Magnitude range: %.2f to %.2f\n", this.minZ, this.maxZ);
            
            // Set up plotting ranges
            this.domainMin = this.minX;
            this.domainMax = this.maxX;
            this.domainRange = new Range(this.minX, this.maxX);
            this.rangeMin = this.minY;
            this.rangeMax = this.maxY;
            this.range = new Range(this.minY, this.maxY);
            
            // Scale dots and update plot
            this.scaleDots();
            
            // Notify the plot to refresh
            this.fireDatasetChanged();
            
        } catch (Exception e) {
            System.err.println("Error processing PanSTARRS DR1 data: " + e.getMessage());
        }
    }

    // Tycho-2 Catalog Methods
    public void getTycho2Data() {
        // Show loading indicator
        setLoadingIndicatorAsync(true);
        
        try {
            // Construct ADQL query for Tycho-2 catalog via CDS Vizier TAP
            String tapUrl = CDSMirrorSelector.getSelectedTapUrl();
            String adqlQuery = String.format(
                "SELECT RAmdeg, DEmdeg, BTmag, e_BTmag, VTmag, e_VTmag, TYC1, TYC2, TYC3, Num " +
                "FROM \"I/259/tyc2\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', RAmdeg, DEmdeg), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f)) " +
                "AND VTmag IS NOT NULL AND BTmag IS NOT NULL " +
                "AND VTmag <= %.1f " +
                "ORDER BY VTmag",
                this.getCentralRA(), this.getCentralDec(), 
                this.getFieldSize(), this.getLimitingMag()
            );

            // Build the TAP query URL
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");

            System.out.println("Querying Tycho-2 via CDS Vizier TAP: " + queryUrl);
            
            // DEBUG: Print query parameters
            System.out.printf("DEBUG Tycho-2 Query Parameters:\n");
            System.out.printf("  Central RA: %.6f degrees\n", this.getCentralRA());
            System.out.printf("  Central Dec: %.6f degrees\n", this.getCentralDec());
            System.out.printf("  Field Size: %.1f degrees (%.1f arcmin)\n", this.getFieldSize(), this.getFieldSize() * 60.0);
            System.out.printf("  Limiting Mag: %.1f\n", this.getLimitingMag());
            System.out.println();

            URL url = new URL(queryUrl);
            Document document = this.getDocument(url);
            
            if (document == null) {
                return;
            }
            
            // Parse VOTable response
            this.parseTycho2VOTable(document);
            
            // Hide loading indicator
            setLoadingIndicatorAsync(false);
            // Close the EnterStar dialog when data loading is complete
            disposeStarWindowAsync();
            
            System.out.printf("DEBUG Tycho-2 Results: Found %d stars\n", this.getTotalCount());
            
            if (this.getTotalCount() == 0) {
                showMessageDialogAsync(null, 
                    "No Tycho-2 data found in this field.\nTry increasing the field size or limiting magnitude.", 
                    "No Data Found", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Process the data for plotting
                this.processTycho2Data();
                System.out.printf("DEBUG: Processed %d Tycho-2 stars for plotting\n", this.getTotalCount());
            }
            
        } catch (MalformedURLException e) {
            setLoadingIndicatorAsync(false);
            disposeStarWindowAsync();
            showMessageDialogAsync(null, "Invalid URL for Tycho-2 query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException e) {
            setLoadingIndicatorAsync(false);
            disposeStarWindowAsync();
            showMessageDialogAsync(null, "URL encoding error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            setLoadingIndicatorAsync(false);
            disposeStarWindowAsync();
            showMessageDialogAsync(null, "Error querying Tycho-2 data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void parseTycho2VOTable(Document document) {
        try {
            // Parse VOTable format from CDS Vizier - look for TABLEDATA section
            NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
            
            System.out.printf("DEBUG: Found %d TABLEDATA elements\n", tableDataNodes.getLength());
            
            if (tableDataNodes.getLength() == 0) {
                this.setTotalCount(0);
                return;
            }
            
            // Get TR elements within TABLEDATA
            Element tableDataElement = (Element) tableDataNodes.item(0);
            NodeList tableData = tableDataElement.getElementsByTagName("TR");
            int dataRows = tableData.getLength();
            
            System.out.printf("DEBUG: Found %d TR elements in TABLEDATA\n", dataRows);
            
            // Count valid data rows
            int actualDataCount = 0;
            for (int i = 0; i < dataRows; i++) {
                Element row = (Element) tableData.item(i);
                NodeList cells = row.getElementsByTagName("TD");
                if (cells.getLength() >= 9) {
                    actualDataCount++;
                }
            }
            
            System.out.printf("DEBUG: Counted %d valid data rows\n", actualDataCount);
            
            this.setTotalCount(actualDataCount);
            
            if (actualDataCount > 0) {
                this.initializeArrays(5, actualDataCount);
                
                int recordIndex = 0;
                for (int i = 0; i < dataRows && recordIndex < actualDataCount; i++) {
                    Element row = (Element) tableData.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 10) {
                        try {
                            // Parse Tycho-2 data columns:
                            // RAmdeg, DEmdeg, BTmag, e_BTmag, VTmag, e_VTmag, TYC1, TYC2, TYC3, Num
                            
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double btmag = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 99.999);
                            double e_btmag = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 99.999);
                            double vtmag = this.parseDoubleOrDefault(cells.item(4).getTextContent().trim(), 99.999);
                            double e_vtmag = this.parseDoubleOrDefault(cells.item(5).getTextContent().trim(), 99.999);
                            
                            String tyc1 = cells.item(6).getTextContent().trim();
                            String tyc2 = cells.item(7).getTextContent().trim();
                            String tyc3 = cells.item(8).getTextContent().trim();
                            String starName = "TYC" + tyc1 + "-" + tyc2 + "-" + tyc3;
                            
                            // Parse Num field (number of observations)
                            String numStr = cells.item(9).getTextContent().trim();
                            int num = this.parseIntOrDefault(numStr, 1);
                            System.out.printf("DEBUG Tycho-2: %s Num='%s' parsed as %d\n", starName, numStr, num);
                            
                            // Apply Tycho-2 to Johnson-Cousins transformations
                            // Source: Arne Henden 5/26/2003
                            // B = Bt + 0.018 - 0.2580*(Bt-Vt)
                            // V = Vt + 0.008 - 0.0988*(Bt-Vt)
                            // R = Vt - 0.014 - 0.5405*(Bt-Vt)
                            // I = Vt - 0.039 - 0.9376*(Bt-Vt)
                            
                            double bt_vt = btmag - vtmag;
                            
                            // Transform magnitudes
                            double bmag = btmag + 0.018 - 0.2580 * bt_vt;
                            double vmag = vtmag + 0.008 - 0.0988 * bt_vt;
                            double rmag = vtmag - 0.014 - 0.5405 * bt_vt;
                            double imag = vtmag - 0.039 - 0.9376 * bt_vt;
                            
                            // Calculate color indices
                            double bv = bmag - vmag;
                            double vr = vmag - rmag;
                            double ri = rmag - imag;
                            double vi = vmag - imag;
                            
                            // Error propagation for transformations
                            // For V: dV/dVt = 1 - 0.0988, dV/dBt = 0.0988
                            double dV_dVt = 1.0 - 0.0988;
                            double dV_dBt = 0.0988;
                            double e_vmag = Math.sqrt(
                                (dV_dVt * dV_dVt) * (e_vtmag * e_vtmag) +
                                (dV_dBt * dV_dBt) * (e_btmag * e_btmag)
                            );
                            
                            // For B: dB/dBt = 1 - 0.2580, dB/dVt = 0.2580
                            double dB_dBt = 1.0 - 0.2580;
                            double dB_dVt = 0.2580;
                            double e_bmag = Math.sqrt(
                                (dB_dBt * dB_dBt) * (e_btmag * e_btmag) +
                                (dB_dVt * dB_dVt) * (e_vtmag * e_vtmag)
                            );
                            
                            // For R: dR/dVt = 1 + 0.5405, dR/dBt = -0.5405
                            double dR_dVt = 1.0 + 0.5405;
                            double dR_dBt = -0.5405;
                            double e_rmag = Math.sqrt(
                                (dR_dVt * dR_dVt) * (e_vtmag * e_vtmag) +
                                (dR_dBt * dR_dBt) * (e_btmag * e_btmag)
                            );
                            
                            // For I: dI/dVt = 1 + 0.9376, dI/dBt = -0.9376
                            double dI_dVt = 1.0 + 0.9376;
                            double dI_dBt = -0.9376;
                            double e_imag = Math.sqrt(
                                (dI_dVt * dI_dVt) * (e_vtmag * e_vtmag) +
                                (dI_dBt * dI_dBt) * (e_btmag * e_btmag)
                            );
                            
                            // Error propagation for color indices
                            double e_bv = Math.sqrt(e_bmag * e_bmag + e_vmag * e_vmag);
                            double e_vr = Math.sqrt(e_vmag * e_vmag + e_rmag * e_rmag);
                            double e_ri = Math.sqrt(e_rmag * e_rmag + e_imag * e_imag);
                            double e_vi = Math.sqrt(e_vmag * e_vmag + e_imag * e_imag);
                            
                            // Set core data in arrays
                            this.setName(recordIndex, starName);
                            this.setRa(recordIndex, ra);
                            this.setDec(recordIndex, dec);
                            this.setVmag(recordIndex, vmag);
                            this.setEv(recordIndex, e_vmag);
                            
                            // Store transformed photometric data
                            this.bmag[recordIndex] = bmag;
                            this.e_bmag[recordIndex] = e_bmag;
                            this.setBMinusV(recordIndex, bv);
                            this.setEbv(recordIndex, e_bv);
                            
                            // Store color indices
                            this.setVMinusR(recordIndex, vr);
                            this.setEvr(recordIndex, e_vr);
                            this.setRMinusI(recordIndex, ri);
                            this.setEri(recordIndex, e_ri);
                            this.setVMinusI(recordIndex, vi);
                            this.setEvi(recordIndex, e_vi);
                            
                            // U-B not available
                            this.setUMinusB(recordIndex, 99.999);
                            this.setEub(recordIndex, 99.999);
                            
                            this.setNobs(recordIndex, num);
                            this.setMobs(recordIndex, 1);
                            this.setSource(recordIndex, 901); // Special source number for Tycho-2
                            
                            // Tycho-2 has good astrometry (typically ~60-100 mas)
                            this.setRaerr(recordIndex, 0.1 / 3600.0);  // ~0.1 arcsec in degrees
                            this.setDecerr(recordIndex, 0.1 / 3600.0);
                            
                            recordIndex++;
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing Tycho-2 data row " + i + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Tycho-2 VOTable: " + e.getMessage());
            this.setTotalCount(0);
        }
    }

    public void processTycho2Data() {
        try {
            // Calculate coordinate ranges and series assignments for Tycho-2 data
            this.findVariables(); // Look for variables in the field
            
            // Assign series numbers to all stars
            for (int i = 0; i < this.getTotalCount(); i++) {
                this.setSeries(i, 0); // Series 0 for regular comparison stars
            }
            
            // Calculate coordinate bounds for plotting
            for (int i = 0; i < this.getTotalCount(); i++) {
                if (this.getRa(i) < this.minRa && this.getRa(i) != 99.999) {
                    this.minRa = this.getRa(i);
                }
                if (this.getRa(i) > this.maxRa && this.getRa(i) != 99.999) {
                    this.maxRa = this.getRa(i);
                }
                if (this.getDec(i) < this.minDec && this.getDec(i) != 99.999) {
                    this.minDec = this.getDec(i);
                }
                if (this.getDec(i) > this.maxDec && this.getDec(i) != 99.999) {
                    this.maxDec = this.getDec(i);
                }
                if (this.getVmag(i) < this.minZ && this.getVmag(i) != 99.999) {
                    this.minZ = this.getVmag(i);
                }
                if (this.getVmag(i) > this.maxZ && this.getVmag(i) != 99.999) {
                    this.maxZ = this.getVmag(i);
                }
            }
            
            // Set coordinate ranges
            this.minRa = this.getLowerRA();
            this.maxRa = this.getUpperRA();
            this.minDec = this.getLowerDec();
            this.maxDec = this.getUpperDec();
            
            System.out.printf("DEBUG: Coordinate bounds set:\n");
            System.out.printf("  RA bounds: %.6f to %.6f\n", this.minRa, this.maxRa);
            System.out.printf("  Dec bounds: %.6f to %.6f\n", this.minDec, this.maxDec);
            
            // Convert to tangent plane coordinates for plotting
            this.convertToTangentPlane();
            
            // DEBUG: Check coordinate conversion
            System.out.printf("DEBUG: Coordinate ranges after conversion:\n");
            System.out.printf("  X range: %.6f to %.6f\n", this.minX, this.maxX);
            System.out.printf("  Y range: %.6f to %.6f\n", this.minY, this.maxY);
            System.out.printf("  Magnitude range: %.2f to %.2f\n", this.minZ, this.maxZ);
            
            // Set up plotting ranges
            this.domainMin = this.minX;
            this.domainMax = this.maxX;
            this.domainRange = new Range(this.minX, this.maxX);
            this.rangeMin = this.minY;
            this.rangeMax = this.maxY;
            this.range = new Range(this.minY, this.maxY);
            
            // Scale dots and update plot
            this.scaleDots();
            
            // Notify the plot to refresh
            this.fireDatasetChanged();
            
        } catch (Exception e) {
            System.err.println("Error processing Tycho-2 data: " + e.getMessage());
        }
    }

    public void getSdssData() {
        try {
            long totalStartTime = System.currentTimeMillis();
            
            // Use CDS VizieR TAP for SDSS DR12 catalog
            // Catalog: V/147/sdss12 (SDSS Data Release 12)
            String tapUrl = CDSMirrorSelector.getSelectedTapUrl();
            
            // Calculate magnitude constraint for g-band (roughly corresponds to V magnitude)
            // Add 1 mag buffer to ensure we don't miss faint stars after conversion
            double gMagLimit = this.getLimitingMag() + 1.0;
            
            // Build ADQL query for SDSS DR12
            // Use BOX instead of CIRCLE (VizieR has better indexing for BOX queries)
            // BOX expects width and height in degrees. getFieldSize() returns diameter in degrees.
            double boxSizeDeg = this.getFieldSize();  // Field diameter in degrees
            String adqlQuery = String.format(
                "SELECT TOP 5000 RA_ICRS, DE_ICRS, " +
                "umag, e_umag, gmag, e_gmag, rmag, e_rmag, imag, e_imag, zmag, e_zmag, objID " +
                "FROM \"V/147/sdss12\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', RA_ICRS, DE_ICRS), " +
                "BOX('ICRS', %.6f, %.6f, %.6f, %.6f)) " +
                "AND umag IS NOT NULL " +
                "AND gmag IS NOT NULL " +
                "AND rmag IS NOT NULL " +
                "AND imag IS NOT NULL " +
                "AND gmag <= %.1f",
                this.getCentralRA(), this.getCentralDec(), 
                boxSizeDeg, boxSizeDeg, gMagLimit
            );
            
            // Build the TAP query URL
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            System.out.println("\n========== SDSS DR12 CATALOG LOADING ==========");
            System.out.println("Service: CDS VizieR TAP (V/147/sdss12)");
            System.out.printf("Field: RA=%.6f, Dec=%.6f, Box=%.2f°×%.2f°, MagLimit=%.1f (g<=%.1f)\n", 
                this.getCentralRA(), this.getCentralDec(), 
                boxSizeDeg, boxSizeDeg, this.getLimitingMag(), gMagLimit);
            System.out.println("ADQL Query:");
            System.out.println(adqlQuery);
            System.out.println("\nFull Query URL:");
            System.out.println(queryUrl);
            
            // Open connection and track download
            System.out.print("Connecting to VizieR... ");
            long stepStartTime = System.currentTimeMillis();
            URL url = new URL(queryUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);  // 30 second connection timeout
            connection.setReadTimeout(this.getCatalogReadTimeoutSeconds() * 1000);  // User-configurable read timeout
            InputStream inputStream = connection.getInputStream();
            
            // Wrap in a buffered stream to count bytes
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            inputStream.close();
            
            long downloadTime = System.currentTimeMillis() - stepStartTime;
            double downloadRate = (totalBytes / 1024.0) / (downloadTime / 1000.0);
            System.out.printf("✓ Downloaded %,d bytes in %.2f sec (%.1f KB/s)\n", 
                totalBytes, downloadTime / 1000.0, downloadRate);
            
            // DEBUG: Show first 1000 chars of XML response
            String xmlResponse = new String(baos.toByteArray(), "UTF-8");
            System.out.println("\n=== DEBUG: First 1000 chars of XML response ===");
            System.out.println(xmlResponse.substring(0, Math.min(1000, xmlResponse.length())));
            System.out.println("=== END XML PREVIEW ===\n");
            
            // Parse XML document
            System.out.print("Parsing XML... ");
            stepStartTime = System.currentTimeMillis();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
            long parseXmlTime = System.currentTimeMillis() - stepStartTime;
            System.out.printf("✓ Completed in %.2f sec\n", parseXmlTime / 1000.0);
            
            // Parse the VOTable and populate data arrays
            System.out.print("Processing catalog data... ");
            stepStartTime = System.currentTimeMillis();
            this.parseSdssVOTable(document);
            long parseDataTime = System.currentTimeMillis() - stepStartTime;
            System.out.printf("✓ Found %,d stars in %.2f sec\n", 
                this.getTotalCount(), parseDataTime / 1000.0);
            
            // Hide loading indicator after successful query
            setLoadingIndicatorAsync(false);
            
            if (this.getTotalCount() == 0) {
                // Check if the field is outside SDSS coverage
                double dec = this.getCentralDec();
                if (dec < -10.0) {
                    showMessageDialogAsync(null, 
                        String.format("No coverage by SDSS in this area.\n\n" +
                                    "SDSS was observed from Apache Point Observatory (New Mexico) and primarily\n" +
                                    "covers the northern hemisphere. Coverage is limited south of Dec -10°.\n" +
                                    "Current field center: Dec = %.2f°\n\n" +
                                    "Consider using PanSTARRS DR1 for southern fields.", dec), 
                        "SDSS Coverage Limitation", 
                        JOptionPane.WARNING_MESSAGE);
                } else {
                    showMessageDialogAsync(null, 
                        "No SDSS DR12 data found in this field.\nTry increasing the field size.", 
                        "No Data Found", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                System.out.print("Converting photometry and preparing plot... ");
                stepStartTime = System.currentTimeMillis();
                this.processSdssData();
                long processTime = System.currentTimeMillis() - stepStartTime;
                System.out.printf("✓ Completed in %.2f sec\n", processTime / 1000.0);
            }
            
            long totalTime = System.currentTimeMillis() - totalStartTime;
            System.out.printf("TOTAL TIME: %.2f sec\n", totalTime / 1000.0);
            System.out.println("===============================================\n");
            
            // Close the star window
            disposeStarWindowAsync();
            
        } catch (MalformedURLException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Invalid URL for SDSS DR12 query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Encoding error in SDSS DR12 query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException e) {
            setLoadingIndicatorAsync(false);
            showMessageDialogAsync(null, "Error querying SDSS DR12 data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void parseSdssVOTable(Document document) {
        try {
            // Extract TABLEDATA section from VizieR TAP response
            NodeList tableDatas = document.getElementsByTagName("TABLEDATA");
            if (tableDatas.getLength() == 0) {
                System.err.println("No TABLEDATA found in SDSS DR12 VizieR VOTable response");
                this.setTotalCount(0);
                return;
            }
            
            Element tableData = (Element) tableDatas.item(0);
            NodeList trNodes = tableData.getElementsByTagName("TR");
            int dataRows = trNodes.getLength();
            
            System.out.printf("DEBUG: Found %d data rows in SDSS DR12 VizieR VOTable\n", dataRows);
            
            if (dataRows > 0) {
                this.setTotalCount(dataRows);
                this.initializeArrays(5, dataRows);
                
                int recordIndex = 0;
                for (int i = 0; i < dataRows; i++) {
                    Element row = (Element) trNodes.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 13) {
                        try {
                            // Parse SDSS DR12 data columns from VizieR (V/147/sdss12):
                            // RA_ICRS, DE_ICRS, umag, e_umag, gmag, e_gmag, rmag, e_rmag, 
                            // imag, e_imag, zmag, e_zmag, objID
                            
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            
                            // SDSS photometry (native SDSS magnitudes)
                            double umag = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 99.999);
                            double e_umag = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 0.01);
                            double gmag = this.parseDoubleOrDefault(cells.item(4).getTextContent().trim(), 99.999);
                            double e_gmag = this.parseDoubleOrDefault(cells.item(5).getTextContent().trim(), 0.01);
                            double rmag = this.parseDoubleOrDefault(cells.item(6).getTextContent().trim(), 99.999);
                            double e_rmag = this.parseDoubleOrDefault(cells.item(7).getTextContent().trim(), 0.01);
                            double imag = this.parseDoubleOrDefault(cells.item(8).getTextContent().trim(), 99.999);
                            double e_imag = this.parseDoubleOrDefault(cells.item(9).getTextContent().trim(), 0.01);
                            double zmag = this.parseDoubleOrDefault(cells.item(10).getTextContent().trim(), 99.999);
                            double e_zmag = this.parseDoubleOrDefault(cells.item(11).getTextContent().trim(), 0.01);
                            
                            String objID = cells.item(12).getTextContent().trim();
                            
                            // Transform SDSS ugriz to Johnson-Cousins UBVRI
                            // Using Jester et al. (2005) transformations:
                            // U = u - 0.0316*(u-g) - 0.7487
                            // B = u - 0.8116*(u-g) + 0.1313
                            // V = g - 0.5784*(g-r) - 0.0038
                            // R = r - 0.1837*(g-r) - 0.0971
                            // I = r - 1.2444*(r-i) - 0.3820
                            
                            double u_g = (umag < 99 && gmag < 99) ? (umag - gmag) : 0.0;
                            double g_r = (gmag < 99 && rmag < 99) ? (gmag - rmag) : 0.0;
                            double r_i = (rmag < 99 && imag < 99) ? (rmag - imag) : 0.0;
                            
                            double U_jc = (umag < 99) ? umag - 0.0316 * u_g - 0.7487 : 99.999;
                            double B_jc = (umag < 99 && gmag < 99) ? umag - 0.8116 * u_g + 0.1313 : 99.999;
                            double V_jc = (gmag < 99 && rmag < 99) ? gmag - 0.5784 * g_r - 0.0038 : 99.999;
                            double R_jc = (rmag < 99) ? rmag - 0.1837 * g_r - 0.0971 : 99.999;
                            double I_jc = (rmag < 99 && imag < 99) ? rmag - 1.2444 * r_i - 0.3820 : 99.999;
                            
                            // Calculate errors (simple propagation, ignoring covariances)
                            double e_V_jc = (gmag < 99 && rmag < 99) ? 
                                Math.sqrt(e_gmag * e_gmag + (0.5784 * 0.5784) * (e_gmag * e_gmag + e_rmag * e_rmag)) : 0.01;
                            
                            // Calculate color indices in Johnson-Cousins system
                            double bv = (B_jc < 99 && V_jc < 99) ? (B_jc - V_jc) : 99.999;
                            double vr = (V_jc < 99 && R_jc < 99) ? (V_jc - R_jc) : 99.999;
                            double ri = (R_jc < 99 && I_jc < 99) ? (R_jc - I_jc) : 99.999;
                            double vi = (V_jc < 99 && I_jc < 99) ? (V_jc - I_jc) : 99.999;
                            double ub = (U_jc < 99 && B_jc < 99) ? (U_jc - B_jc) : 99.999;
                            
                            // Filter out stars with poor photometry or beyond reasonable magnitude limits
                            // Accept stars within 2 magnitudes of the limiting magnitude to allow for transformation uncertainties
                            if (V_jc > (this.getLimitingMag() + 2.0) || V_jc >= 99.0) {
                                continue; // Skip this star
                            }
                            
                            // Set core data in arrays
                            this.setName(recordIndex, "SDSS_" + objID);
                            this.setRa(recordIndex, ra);
                            this.setDec(recordIndex, dec);
                            
                            // Use transformed V magnitude as the primary magnitude
                            this.setVmag(recordIndex, V_jc);
                            this.setEv(recordIndex, e_V_jc);
                            
                            // Debug first few stars
                            if (recordIndex < 5) {
                                System.out.printf("DEBUG: Star %d - u=%.2f, g=%.2f, r=%.2f, i=%.2f -> U=%.2f, B=%.2f, V=%.2f, R=%.2f, I=%.2f, B-V=%.2f\n",
                                    recordIndex, umag, gmag, rmag, imag, U_jc, B_jc, V_jc, R_jc, I_jc, bv);
                            }
                            
                            // Store B-V as the primary color index
                            this.setBMinusV(recordIndex, bv);
                            this.setEbv(recordIndex, 0.01);  // Simplified error
                            
                            // Store transformed photometric data
                            this.bmag[recordIndex] = B_jc;
                            this.e_bmag[recordIndex] = 0.01;
                            
                            // Store color indices
                            this.setVMinusR(recordIndex, vr);
                            this.setEvr(recordIndex, 0.02);
                            this.setRMinusI(recordIndex, ri);
                            this.setEri(recordIndex, 0.02);
                            this.setVMinusI(recordIndex, vi);
                            this.setEvi(recordIndex, 0.02);
                            
                            // U-B color
                            this.setUMinusB(recordIndex, ub);
                            this.setEub(recordIndex, 0.02);
                            
                            // Set source as SDSS (21)
                            this.setSource(recordIndex, 21);
                            
                            recordIndex++;
                            
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing SDSS row " + i + ": " + e.getMessage());
                        }
                    }
                }
                
                System.out.printf("DEBUG: Successfully parsed %d SDSS DR12 stars from VizieR\n", recordIndex);
                
            } else {
                this.setTotalCount(0);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing SDSS DR12 VizieR VOTable: " + e.getMessage());
            this.setTotalCount(0);
        }
    }

    public void processSdssData() {
        try {
            // Reset magnitude ranges before processing
            this.minZ = 100.0;
            this.maxZ = -100.0;
            
            System.out.printf("DEBUG: SDSS processSdssData() - Starting with %d stars\n", this.getTotalCount());
            
            // Calculate coordinate ranges and series assignments for SDSS DR12 data
            this.findVariables(); // Look for variables in the field
            
            // First, show a sample of the magnitude values
            System.out.println("DEBUG: Sample of first 5 SDSS star magnitudes:");
            for (int i = 0; i < Math.min(5, this.getTotalCount()); i++) {
                System.out.printf("  Star %d: V=%.3f\n", i, this.getVmag(i));
            }
            
            for (int i = 0; i < this.getTotalCount(); i++) {
                // Assign series number based on magnitude
                int series = this.assignSeriesNumber(this.getBMinusV(i), this.getRa(i), 
                    this.getRaerr(i), this.getDec(i), this.getDecerr(i), 
                    this.getVmag(i), this.getUpperLimitingMag(), i);
                    
                this.seriesValue[i] = series;
                
                // Update magnitude range
                if (this.getVmag(i) < this.minZ && this.getVmag(i) != 99.999) {
                    this.minZ = this.getVmag(i);
                }
                if (this.getVmag(i) > this.maxZ && this.getVmag(i) != 99.999) {
                    this.maxZ = this.getVmag(i);
                }
            }
            
            System.out.printf("DEBUG: SDSS After magnitude loop - minZ=%.3f, maxZ=%.3f\n", this.minZ, this.maxZ);
            
            // Set coordinate ranges
            this.minRa = this.getLowerRA();
            this.maxRa = this.getUpperRA();
            this.minDec = this.getLowerDec();
            this.maxDec = this.getUpperDec();
            
            System.out.printf("DEBUG: SDSS Coordinate bounds set:\n");
            System.out.printf("  RA bounds: %.6f to %.6f\n", this.minRa, this.maxRa);
            System.out.printf("  Dec bounds: %.6f to %.6f\n", this.minDec, this.maxDec);
            
            // Convert to tangent plane coordinates for plotting
            this.convertToTangentPlane();
            
            // DEBUG: Check coordinate conversion
            System.out.printf("DEBUG: SDSS Coordinate ranges after conversion:\n");
            System.out.printf("  X range: %.6f to %.6f\n", this.minX, this.maxX);
            System.out.printf("  Y range: %.6f to %.6f\n", this.minY, this.maxY);
            System.out.printf("  Magnitude range: %.2f to %.2f\n", this.minZ, this.maxZ);
            
            // Set up plotting ranges
            this.domainMin = this.minX;
            this.domainMax = this.maxX;
            this.domainRange = new Range(this.minX, this.maxX);
            this.rangeMin = this.minY;
            this.rangeMax = this.maxY;
            this.range = new Range(this.minY, this.maxY);
            
            // Scale dots and update plot
            this.scaleDots();
            
            // Notify the plot to refresh
            this.fireDatasetChanged();
            
            System.out.println("SDSS DR12 data ready for plotting");
            
        } catch (Exception e) {
            System.err.println("Error processing SDSS DR12 data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Fetch VSP comparison stars from AAVSO VSP API
     * Called when Points View or Sky View is opened
     */
    public void fetchVSPCompStars() {
        try {
            vspCompStars.clear();
            vspChartId = null;
            
            // Determine limiting magnitude: MinMag + 2 (from VSX data)
            double magLimit = 16.0; // Default fallback
            if (this.varMin != null && this.varMin.length > 0 && this.varMin[0] != null) {
                try {
                    String minMagStr = this.varMin[0].trim();
                    double minMag;
                    
                    // Check if MinMag is in amplitude format: "(0.76) V" means amplitude, not actual magnitude
                    if (minMagStr.startsWith("(") && minMagStr.contains(")")) {
                        // Amplitude format: MinMag = MaxMag + amplitude
                        String amplitudeStr = minMagStr.substring(1, minMagStr.indexOf(")")).trim();
                        double amplitude = Double.parseDouble(amplitudeStr);
                        
                        // Get MaxMag to calculate actual MinMag
                        if (this.varMax != null && this.varMax.length > 0 && this.varMax[0] != null) {
                            String maxMagStr = this.varMax[0].replaceAll("[^0-9.]", "");
                            if (!maxMagStr.isEmpty()) {
                                double maxMag = Double.parseDouble(maxMagStr);
                                minMag = maxMag + amplitude;
                                System.out.println("DEBUG: VSX amplitude format - MaxMag=" + maxMag + 
                                                 ", Amplitude=" + amplitude + ", calculated MinMag=" + minMag);
                            } else {
                                // Can't parse MaxMag, use default
                                System.out.println("DEBUG: Could not parse MaxMag for amplitude calculation, using default");
                                minMag = magLimit;
                            }
                        } else {
                            System.out.println("DEBUG: No MaxMag available for amplitude calculation, using default");
                            minMag = magLimit;
                        }
                    } else {
                        // Standard format: "14.0: V" or "14.0 V"
                        String minStr = minMagStr.replaceAll("[^0-9.]", "");
                        if (!minStr.isEmpty()) {
                            minMag = Double.parseDouble(minStr);
                            System.out.println("DEBUG: Using VSX MinMag=" + minMag);
                        } else {
                            System.out.println("DEBUG: Could not parse MinMag, using default");
                            minMag = magLimit;
                        }
                    }
                    
                    magLimit = minMag + 2.0; // VSX MinMag + 2
                    System.out.println("DEBUG: Setting VSP maglimit=" + magLimit);
                    
                } catch (Exception e) {
                    System.out.println("DEBUG: Error parsing MinMag: " + e.getMessage() + ", using default maglimit=" + magLimit);
                }
            } else {
                System.out.println("DEBUG: No VSX MinMag available, using default maglimit=" + magLimit);
            }
            
            // Build VSP API URL
            String vspUrl = String.format(
                "https://app.aavso.org/vsp/api/chart/?format=json&ra=%.6f&dec=%.6f&fov=%.1f&maglimit=%.1f",
                this.getCentralRA(),
                this.getCentralDec(),
                this.getFieldSize() * 60.0, // Convert degrees to arcmin
                magLimit
            );
            
            System.out.println("DEBUG: Fetching VSP comparison stars...");
            System.out.println("  URL: " + vspUrl);
            
            // Fetch JSON response
            URL url = URI.create(vspUrl).toURL();
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(url.openStream()));
            StringBuilder jsonResponse = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResponse.append(line);
            }
            reader.close();
            
            String json = jsonResponse.toString();
            System.out.println("DEBUG: VSP API response length: " + json.length() + " bytes");
            System.out.println("DEBUG: VSP JSON content: " + json);
            
            // Parse JSON manually (simple string parsing for this specific format)
            parseVSPJSON(json);
            
            // Project VSP stars to tangent plane
            if (!vspCompStars.isEmpty()) {
                projectVSPStarsToTangentPlane();
                System.out.println("DEBUG: Loaded " + vspCompStars.size() + " VSP comparison stars");
                System.out.println("DEBUG: VSP Chart ID: " + vspChartId);
                
                // Show first few stars for verification
                int showCount = Math.min(3, vspCompStars.size());
                for (int i = 0; i < showCount; i++) {
                    VSPCompStar star = vspCompStars.get(i);
                    System.out.printf("  Star %d: AUID=%s, RA=%s (%.6f deg), Dec=%s (%.6f deg), V=%.3f, Label=%s\n",
                        i+1, star.auid, star.raStr, star.ra, star.decStr, star.dec, star.vmag, star.label);
                    System.out.printf("    B=%.3f, B-V=%.3f, V-I=%.3f, x=%.4f, y=%.4f\n",
                        star.bmag, star.bMinusV, star.vMinusI, star.x, star.y);
                }
            } else {
                System.out.println("DEBUG: No VSP comparison stars returned for this field");
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching VSP comparison stars: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Simple JSON parser for VSP API response
     */
    private void parseVSPJSON(String json) {
        try {
            // Extract chartid
            int chartIdStart = json.indexOf("\"chartid\":\"") + 11;
            int chartIdEnd = json.indexOf("\"", chartIdStart);
            if (chartIdStart > 10 && chartIdEnd > chartIdStart) {
                vspChartId = json.substring(chartIdStart, chartIdEnd);
                System.out.println("DEBUG: VSP Chart ID: " + vspChartId);
            }
            
            // Extract photometry array
            int photStart = json.indexOf("\"photometry\":[");
            if (photStart < 0) {
                System.out.println("DEBUG: No photometry data in VSP response");
                return;
            }
            
            // Find each star object in the photometry array
            int pos = photStart;
            while (true) {
                pos = json.indexOf("{\"auid\":", pos);
                if (pos < 0) break;
                
                // Find the end of this star object by counting braces
                int braceCount = 1;
                int searchPos = pos + 1;
                while (braceCount > 0 && searchPos < json.length()) {
                    char c = json.charAt(searchPos);
                    if (c == '{') braceCount++;
                    else if (c == '}') braceCount--;
                    searchPos++;
                }
                
                if (braceCount == 0) {
                    String starObj = json.substring(pos, searchPos);
                    VSPCompStar star = parseVSPStar(starObj);
                    if (star != null) {
                        vspCompStars.add(star);
                    }
                    pos = searchPos;
                } else {
                    break; // Malformed JSON
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing VSP JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Parse a single VSP star object from JSON
     */
    private VSPCompStar parseVSPStar(String starJson) {
        try {
            String auid = extractJSONString(starJson, "auid");
            String ra = extractJSONString(starJson, "ra");
            String dec = extractJSONString(starJson, "dec");
            String label = extractJSONString(starJson, "label");
            String comments = extractJSONString(starJson, "comments");
            
            if (auid == null || ra == null || dec == null) {
                return null;
            }
            
            VSPCompStar star = new VSPCompStar(auid, ra, dec, label, comments);
            
            // Parse bands array
            int bandsStart = starJson.indexOf("\"bands\":[");
            if (bandsStart >= 0) {
                int bandsEnd = starJson.indexOf("]", bandsStart);
                String bandsArray = starJson.substring(bandsStart, bandsEnd);
                
                // Extract each band
                parseBand(bandsArray, "V", star);
                parseBand(bandsArray, "B", star);
                parseBand(bandsArray, "U", star);
                parseBand(bandsArray, "R", star);
                parseBand(bandsArray, "I", star);
            }
            
            // Calculate color indices
            star.calculateColorIndices();
            
            return star;
            
        } catch (Exception e) {
            System.err.println("Error parsing VSP star: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse a band magnitude from the bands array
     */
    private void parseBand(String bandsArray, String bandName, VSPCompStar star) {
        try {
            String search = "\"band\":\"" + bandName + "\"";
            int pos = bandsArray.indexOf(search);
            if (pos < 0) return;
            
            // Find the band object
            int objStart = bandsArray.lastIndexOf("{", pos);
            int objEnd = bandsArray.indexOf("}", pos);
            String bandObj = bandsArray.substring(objStart, objEnd);
            
            double mag = extractJSONNumber(bandObj, "mag");
            double error = extractJSONNumber(bandObj, "error");
            
            switch (bandName) {
                case "V":
                    star.vmag = mag;
                    star.vError = error;
                    break;
                case "B":
                    star.bmag = mag;
                    star.bError = error;
                    break;
                case "U":
                    star.umag = mag;
                    star.uError = error;
                    break;
                case "R":
                    star.rmag = mag;
                    star.rError = error;
                    break;
                case "I":
                    star.imag = mag;
                    star.iError = error;
                    break;
            }
        } catch (Exception e) {
            // Band not present, leave as sentinel value
        }
    }
    
    /**
     * Extract a string value from JSON
     */
    private String extractJSONString(String json, String key) {
        try {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search);
            if (start < 0) return null;
            start += search.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract a numeric value from JSON
     */
    private double extractJSONNumber(String json, String key) {
        try {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search);
            if (start < 0) return 99.999;
            start += search.length();
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                end++;
            }
            String numStr = json.substring(start, end);
            return Double.parseDouble(numStr);
        } catch (Exception e) {
            return 99.999;
        }
    }
    
    /**
     * Project VSP stars to tangent plane coordinates
     */
    private void projectVSPStarsToTangentPlane() {
        double centerRA = this.getCentralRA();
        double centerDec = this.getCentralDec();
        
        for (VSPCompStar star : vspCompStars) {
            // Convert to radians
            double picon = Math.PI / 180.0;
            double raRad = Math.toRadians(star.ra);
            double decRad = Math.toRadians(star.dec);
            double centerRArad = Math.toRadians(centerRA);
            double centerDecRad = Math.toRadians(centerDec);
            
            // Gnomonic projection (tangent plane) - same as RaDectoXY
            double cosC = Math.sin(centerDecRad) * Math.sin(decRad) + 
                         Math.cos(centerDecRad) * Math.cos(decRad) * Math.cos(raRad - centerRArad);
            
            double xi = (Math.cos(decRad) * Math.sin(raRad - centerRArad)) / cosC;
            double eta = (Math.cos(centerDecRad) * Math.sin(decRad) - 
                         Math.sin(centerDecRad) * Math.cos(decRad) * Math.cos(raRad - centerRArad)) / cosC;
            
            // Convert from radians to degrees (to match catalog star coordinates)
            star.x = xi / picon;
            star.y = eta / picon;
        }
    }

    public void findVariables() {
        try {
            URL vsxUrl = new URL(String.valueOf(this.getBaseURL()) + "vsx/index.php?view=api.list&fromra=" + String.format("%.6f", this.getLowerRA()) + "&tora=" + String.format("%.6f", this.getUpperRA()) + "&fromdec=" + String.format("%.6f", this.getLowerDec()) + "&todec=" + String.format("%.6f", this.getUpperDec()));
            NodeList objNodes = this.getDocument(vsxUrl).getElementsByTagName("VSXObject");
            this.numberOfVars = Math.min(objNodes.getLength(), 5000); // Limit to array size
            if (this.numberOfVars != 0) {
                int i = 0;
                while (i < this.numberOfVars) {
                    NodeList varData = objNodes.item(i).getChildNodes();
                    int j = 0;
                    while (j < varData.getLength()) {
                        Element detailElt = (Element)varData.item(j);
                        String nodeName = detailElt.getNodeName();
                        String nodeValue = detailElt.getTextContent();
                        if ("Name".equals(nodeName)) {
                            this.varName[i] = nodeValue;
                        } else if ("RA2000".equals(nodeName)) {
                            this.rVar[i] = Double.parseDouble(nodeValue);
                        } else if ("Declination2000".equals(nodeName)) {
                            this.dVar[i] = Double.parseDouble(nodeValue);
                        } else if ("VariabilityType".equals(nodeName)) {
                            this.varType[i] = nodeValue;
                        } else if ("MaxMag".equals(nodeName)) {
                            this.varMax[i] = nodeValue;
                        } else if ("MinMag".equals(nodeName)) {
                            this.varMin[i] = nodeValue;
                        }
                        ++j;
                    }
                    ++i;
                }
            }
        }
        catch (MalformedURLException e) {
            showMessageDialogAsync(null, "MalformedURLException: " + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
        }
    }

    public double[] RaDectoXY(double radeg, double decdeg) {
        double raOffset;
        double picon = Math.PI / 180;
        // Use the central RA/Dec for tangent plane projection center, not data bounds midpoint
        // This ensures consistent coordinate system across zoom levels
        double racent = picon * this.getCentralRA();
        double deccent = picon * this.getCentralDec();
        double raValue = radeg * picon;
        if (this.maxRa > 360.0 && radeg < 180.0) {
            raValue = (radeg + 360.0) * picon;
        }
        if ((raOffset = raValue - racent) >= 360.0 * picon) {
            raOffset -= 360.0 * picon;
        }
        double decValue = decdeg * picon;
        double cosC = Math.sin(deccent) * Math.sin(decValue) + Math.cos(deccent) * Math.cos(decValue) * Math.cos(raOffset);
        double xi = Math.cos(decValue) * Math.sin(raOffset) / cosC;
        double eta = (Math.cos(deccent) * Math.sin(decValue) - Math.sin(deccent) * Math.cos(decValue) * Math.cos(raOffset)) / cosC;
        double[] ret = new double[]{xi / picon, eta / picon};
        return ret;
    }

    public void convertToTangentPlane() {
        if (this.maxRa - this.minRa > 180.0) {
            this.minRa += 180.0;
        }
        
        System.out.printf("DEBUG: Converting %d stars to tangent plane coordinates\n", this.getTotalCount());
        System.out.printf("DEBUG: Tangent plane projection center: RA=%.6f, Dec=%.6f\n", this.getCentralRA(), this.getCentralDec());
        System.out.printf("DEBUG: Data RA range: %.6f to %.6f (midpoint=%.6f)\n", this.minRa, this.maxRa, (this.minRa + this.maxRa)/2.0);
        System.out.printf("DEBUG: Data Dec range: %.6f to %.6f (midpoint=%.6f)\n", this.minDec, this.maxDec, (this.minDec + this.maxDec)/2.0);
        
        // Calculate actual sky coverage
        double raRange = this.maxRa - this.minRa;
        double decRange = this.maxDec - this.minDec;
        double raDegrees = raRange * Math.cos(Math.toRadians(this.getCentralDec()));
        System.out.printf("DEBUG: Sky coverage: RA=%.3f° (%.1f arcmin), Dec=%.3f° (%.1f arcmin)\n", 
                         raRange, raRange * 60.0, decRange, decRange * 60.0);
        System.out.printf("DEBUG: Projected RA width at Dec=%.1f°: %.3f° (cos correction: %.3f)\n",
                         this.getCentralDec(), raDegrees, Math.cos(Math.toRadians(this.getCentralDec())));
        
        // Warn about tangent plane projection limits
        double maxAngularDistance = Math.max(raRange, decRange);
        if (maxAngularDistance > 5.0) {
            System.out.println("WARNING: Field size exceeds 5 degrees - tangent plane projection may show significant distortion");
            System.out.println("         Circular fields on the sky will appear increasingly rectangular in projection");
        } else if (maxAngularDistance > 2.0) {
            System.out.println("NOTE: Field size > 2 degrees - some tangent plane distortion expected, especially at high declination");
        }
        
        double[] xy = new double[2];
        int i = 0;
        while (i < this.getTotalCount()) {
            xy = this.RaDectoXY(this.ra[i], this.dec[i]);
            this.xVal[this.seriesValue[i]][i] = xy[0];
            this.yVal[this.seriesValue[i]][i] = xy[1];
            if (i < 3) { // Debug first few stars
                System.out.printf("DEBUG: Star %d: RA=%.6f, Dec=%.6f -> X=%.6f, Y=%.6f, Series=%d\n", 
                                 i, this.ra[i], this.dec[i], xy[0], xy[1], this.seriesValue[i]);
            }
            ++i;
        }
        xy = this.RaDectoXY(this.minRa, this.minDec);
        this.minX = xy[0];
        this.minY = xy[1];
        xy = this.RaDectoXY(this.maxRa, this.maxDec);
        this.maxX = xy[0];
        this.maxY = xy[1];
        
        System.out.printf("DEBUG: Converted X range: %.6f to %.6f\n", this.minX, this.maxX);
        System.out.printf("DEBUG: Converted Y range: %.6f to %.6f\n", this.minY, this.maxY);
    }

    public void scaleDots() {
        double zScale1 = this.seqplot.getDotsizeScaleFactor() * this.minZ / 14.0;
        double zScale2 = this.seqplot.getRelativeDotsizeScaleFactor() / (this.maxZ - this.minZ);
        int i = 0;
        while (i < this.getTotalCount()) {
            this.zVal[this.seriesValue[i]][i] = zScale1 * Math.pow(10.0, zScale2 * (this.maxZ - this.vmag[i]));
            ++i;
        }
        this.fireDatasetChanged();
    }

    public int assignSeriesNumber(double color, double r, double re, double d, double de, double vmag, double ulmag, int record) {
        // Check if this is a known variable star
        int i = 0;
        while (i < this.numberOfVars) {
            if (Math.abs(this.rVar[i] - r) <= this.getPositionTolerance() && Math.abs(this.dVar[i] - d) <= this.getPositionTolerance()) {
                this.setVarName(record, this.varName[i]);
                this.setVarRa(record, this.rVar[i]);
                this.setVarDec(record, this.dVar[i]);
                this.setVarType(record, this.varType[i]);
                this.setVarMax(record, this.varMax[i]);
                this.setVarMin(record, this.varMin[i]);
                return 3; // Series 3 = variable stars (always displayed)
            }
            ++i;
        }
        
        // ORIGINAL CODE (commented out - used upperLimitingMag to filter chart stars):
        // int ret = 4;
        // if (color <= 0.5) {
        //     ret = 0;
        // } else if (color > 0.5 && color < 1.1) {
        //     ret = 1;
        // } else if (color >= 1.1 && color < 9.9) {
        //     ret = 2;
        // }
        // if (vmag < ulmag) {
        //     ret = 4; // Series 4 = bright chart stars only
        // }
        // return ret;
        
        // NEW CODE: Assign series based on color only, show ALL stars regardless of magnitude
        // Series 0 = blue stars (B-V <= 0.5)
        // Series 1 = yellow/white stars (0.5 < B-V < 1.1)
        // Series 2 = red stars (B-V >= 1.1)
        // Series 3 = variable stars (handled above)
        int ret = 2; // Default to series 2 (red)
        if (color <= 0.5) {
            ret = 0; // Blue
        } else if (color > 0.5 && color < 1.1) {
            ret = 1; // Yellow/white
        } else if (color >= 1.1 && color < 9.9) {
            ret = 2; // Red
        }
        // Note: ulmag parameter no longer used - all stars are displayed
        return ret;
    }

    public String getDefaultPathToFile() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            return String.valueOf(System.getProperty("user.home")) + DEFAULT_PC_FOLDER;
        }
        if (os.indexOf("mac") >= 0) {
            return String.valueOf(System.getProperty("user.home")) + DEFAULT_MAC_FOLDER;
        }
        if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            return String.valueOf(System.getProperty("user.home")) + DEFAULT_UNIX_FOLDER;
        }
        return String.valueOf(System.getProperty("user.home")) + DEFAULT_UNIX_FOLDER;
    }

    public void sendToLogfile(String stringToPrint, String fileName) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {
            pw.println(stringToPrint);
            pw.flush();
        }
        catch (IOException e) {
            showMessageDialogAsync(null, "IOException: " + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
        }
        this.setUserDefaults(this.seqplot.getColorArray());
    }

    public String getSexagesimalRA(int item) {
        double tempHrs = this.ra[item] / 15.0;
        this.raHrs = (long)tempHrs;
        double tempMins = (tempHrs - (double)this.raHrs) * 60.0;
        this.raMins = (long)tempMins;
        this.raSecs = new BigDecimal((tempMins - (double)this.raMins) * 60.0);
        this.raSecs = this.raSecs.setScale(2, 4);
        this.setRaHrs(this.raHrs);
        this.setRaMins(this.raMins);
        this.setRaSecs(this.raSecs);
        return String.valueOf(this.raHrs) + "h " + this.raMins + "m " + this.raSecs + "s";
    }

    public String getSexagesimalDEC(int item) {
        String sign = null;
        this.decDegs = (long)this.dec[item];
        double tempMins = Math.abs((this.dec[item] - (double)this.decDegs) * 60.0);
        this.decMins = (long)tempMins;
        this.decSecs = new BigDecimal((tempMins - (double)this.decMins) * 60.0);
        this.decSecs = this.decSecs.setScale(1, 4);
        sign = this.dec[item] < 0.0 && this.dec[item] > -1.0 ? "-" : (this.dec[item] > 0.0 ? "+" : DEFAULT_STAR);
        this.setDecSign(sign);
        this.setDecDegs(this.decDegs);
        this.setDecMins(this.decMins);
        this.setDecSecs(this.decSecs);
        return String.valueOf(sign) + this.decDegs + "\u00b0 " + this.decMins + "\u2032 " + this.decSecs + "\u2033";
    }

    // Format RA for plot title as hh:mm:ss.s
    public String getFormattedRA() {
        double centralRA = this.getCentralRA();
        double tempHrs = centralRA / 15.0;
        long raHrs = (long)tempHrs;
        double tempMins = (tempHrs - (double)raHrs) * 60.0;
        long raMins = (long)tempMins;
        double raSecs = (tempMins - (double)raMins) * 60.0;
        
        return String.format("%02d:%02d:%04.1f", raHrs, raMins, raSecs);
    }

    // Format Dec for plot title as dd:mm:ss  
    public String getFormattedDec() {
        double centralDec = this.getCentralDec();
        String sign = centralDec >= 0 ? "+" : "-";
        double absDec = Math.abs(centralDec);
        long decDegs = (long)absDec;
        double tempMins = (absDec - (double)decDegs) * 60.0;
        long decMins = (long)tempMins;
        double decSecs = (tempMins - (double)decMins) * 60.0;
        
        return String.format("%s%02d:%02d:%02.0f", sign, decDegs, decMins, decSecs);
    }

    public String getLabel(double vMag) {
        BigDecimal tempLabel = new BigDecimal(vMag);
        tempLabel = tempLabel.setScale(1, 4);
        tempLabel = tempLabel.multiply(new BigDecimal(10));
        return DEFAULT_STAR + tempLabel.longValue();
    }

    public int getRecordNumber(double x, double y) {
        int item = 0;
        while (item < this.getTotalCount()) {
            if (x == this.xVal[this.seriesValue[item]][item] && y == this.yVal[this.seriesValue[item]][item]) {
                return item;
            }
            ++item;
        }
        return -1;
    }

    @Override
    public Comparable<String> getSeriesKey(int series) {
        switch (series) {
            case 0: {
                return "Blue";
            }
            case 1: {
                return "Green";
            }
            case 2: {
                return "Red";
            }
            case 3: {
                return "Yellow";
            }
        }
        return "unknown";
    }

    public void getUserDefaults() {
        String fileName = String.valueOf(this.getDefaultPathToFile()) + USER_PREFERENCES_FILE;
        try {
            File fileObject = new File(fileName);
            if (fileObject.exists() && fileObject.canRead()) {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                int i = 0;
                while (i < this.seqplot.getColorArraySize()) {
                    this.seqplot.setColor(i, new Color(Integer.parseInt(br.readLine())));
                    ++i;
                }
                List<String> entries = new ArrayList<>();
                String line;
                while ((line = br.readLine()) != null) {
                    entries.add(line);
                }
                br.close();
                int index = 0;
                this.setPathToLogfile(readString(entries, index++, this.getDefaultPathToFile()));
                this.setTychoBoxSelected(readBoolean(entries, index++, false));
                this.setNofsBoxSelected(readBoolean(entries, index++, false));
                this.setSonoitaBoxSelected(readBoolean(entries, index++, false));
                this.setBsmBoxSelected(readBoolean(entries, index++, false));
                this.setBsm_SBoxSelected(readBoolean(entries, index++, false));
                this.setSro50BoxSelected(readBoolean(entries, index++, false));
                this.setApassBoxSelected(readBoolean(entries, index++, true));
                this.setApass9BoxSelected(readBoolean(entries, index++, false));
                this.setGaiaDR2BoxSelected(readBoolean(entries, index++, false));
                this.setGaiaDR3BoxSelected(readBoolean(entries, index++, false));
                this.setPanstarrsBoxSelected(readBoolean(entries, index++, false));
                this.setK35BoxSelected(readBoolean(entries, index++, false));
                this.setW28BoxSelected(readBoolean(entries, index++, false));
                this.setW30BoxSelected(readBoolean(entries, index++, false));
                this.setOc61BoxSelected(readBoolean(entries, index++, false));
                this.seqplot.setDotsizeScaleFactor(readDouble(entries, index++, this.seqplot.getDotsizeScaleFactor()));
                this.seqplot.setRelativeDotsizeScaleFactor(readDouble(entries, index++, this.seqplot.getRelativeDotsizeScaleFactor()));
                this.setLimitingMag(readDouble(entries, index++, 20.0));
                this.setFieldSize(readDouble(entries, index++, 1.0));
                if (index < entries.size()) {
                    String potentialChartSize = entries.get(index);
                    if (isBooleanToken(potentialChartSize)) {
                        this.setChartSizeSelection(DEFAULT_CHART_SIZE_SELECTION);
                    } else {
                        this.setChartSizeSelection(potentialChartSize);
                        ++index;
                    }
                } else {
                    this.setChartSizeSelection(DEFAULT_CHART_SIZE_SELECTION);
                }
                this.setGcpdBoxSelected(readBoolean(entries, index++, false));
                this.setUsername(readString(entries, index++, DEFAULT_STAR));
                this.setTmo61BoxSelected(readBoolean(entries, index++, false));
                this.setCoker30BoxSelected(readBoolean(entries, index++, false));
                this.seqplot.setFontSize(readInt(entries, index++, 16));
                this.setPositionTolerance(readDouble(entries, index++, 0.003));
            } else {
                this.seqplot.setDefaultSeqplotColors();
                this.setPathToLogfile(this.getDefaultPathToFile());
                this.setTychoBoxSelected(false);
                this.setNofsBoxSelected(false);
                this.setSonoitaBoxSelected(false);
                this.setBsmBoxSelected(false);
                this.setBsm_SBoxSelected(false);
                this.setSro50BoxSelected(false);
                this.setApassBoxSelected(true);
                this.setApass9BoxSelected(false);
                this.setGaiaDR2BoxSelected(false);
                this.setGaiaDR3BoxSelected(false);
                this.setPanstarrsBoxSelected(false);
                this.setK35BoxSelected(false);
                this.setW28BoxSelected(false);
                this.setW30BoxSelected(false);
                this.setOc61BoxSelected(false);
                this.seqplot.setDotsizeScaleFactor(this.seqplot.getDotsizeScaleFactor());
                this.seqplot.setRelativeDotsizeScaleFactor(this.seqplot.getRelativeDotsizeScaleFactor());
                this.setUpperLimitingMag(-5.0);
                this.setLimitingMag(20.0);
                this.setFieldSize(1.0);
                this.setGcpdBoxSelected(false);
                this.setUsername(DEFAULT_STAR);
                this.setTmo61BoxSelected(false);
                this.setCoker30BoxSelected(false);
                this.seqplot.setFontSize(16);
                this.setPositionTolerance(0.003);
                this.setChartSizeSelection(DEFAULT_CHART_SIZE_SELECTION);
                this.setUserDefaults(this.seqplot.getColorArray());
            }
        }
        catch (FileNotFoundException fileObject) {
        }
        catch (IOException fileObject) {
        }
        catch (Exception e) {
            this.seqplot.setDefaultSeqplotColors();
        }
    }

    public void setUserDefaults(Color[] color) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(String.valueOf(this.getDefaultPathToFile()) + USER_PREFERENCES_FILE));
            int i = 0;
            while (i < this.seqplot.getColorArraySize()) {
                if (color[i].getRGB() != 0) {
                    pw.println(color[i].getRGB());
                } else {
                    pw.println(this.seqplot.getPlotColor(i));
                }
                ++i;
            }
            pw.println(this.getPathToLogfile());
            pw.println(this.getTychoBoxSelected());
            pw.println(this.getNofsBoxSelected());
            pw.println(this.getSonoitaBoxSelected());
            pw.println(this.getBsmBoxSelected());
            pw.println(this.getBsm_SBoxSelected());
            pw.println(this.getSro50BoxSelected());
            pw.println(this.getApassBoxSelected());
            pw.println(this.getApass9BoxSelected());
            pw.println(this.getGaiaDR2BoxSelected());
            pw.println(this.getGaiaDR3BoxSelected());
            pw.println(this.getPanstarrsBoxSelected());
            pw.println(this.getK35BoxSelected());
            pw.println(this.getW28BoxSelected());
            pw.println(this.getW30BoxSelected());
            pw.println(this.getOc61BoxSelected());
            pw.println(this.seqplot.getDotsizeScaleFactor());
            pw.println(this.seqplot.getRelativeDotsizeScaleFactor());
            pw.println(this.getLimitingMag());
            pw.println(this.getFieldSize());
            pw.println(this.getChartSizeSelection());
            pw.println(this.getGcpdBoxSelected());
            pw.println(this.getUsername());
            pw.println(this.getTmo61BoxSelected());
            pw.println(this.getCoker30BoxSelected());
            pw.println(this.seqplot.getFontSize());
            pw.println(this.getPositionTolerance());
            pw.close();
        }
        catch (IOException e) {
            showMessageDialogAsync(null, "IOException: " + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void findUpperLowerRa() {
        this.setLowerRA(this.getCentralRA() - this.getFieldSize() / (2.0 * Math.cos(Math.PI / 180 * this.getCentralDec())));
        this.setUpperRA(this.getCentralRA() + this.getFieldSize() / (2.0 * Math.cos(Math.PI / 180 * this.getCentralDec())));
        if (this.getUpperRA() >= 360.0) {
            this.setUpperRA(360.0);
        }
        if (this.getLowerRA() <= 0.0) {
            this.setLowerRA(0.0);
        }
    }

    public void findUpperLowerDec() {
        this.setLowerDec(this.getCentralDec() - this.getFieldSize() / 2.0);
        this.setUpperDec(this.getCentralDec() + this.getFieldSize() / 2.0);
    }

    public void setCatalogString(String str) {
        this.catalogString = str;
    }

    public String getCatalogString() {
        return this.catalogString;
    }

    public void setRAText(String newRAText) {
        this.setOldRAText(this.raText);
        this.raText = newRAText;
    }

    public void setDecText(String newDecText) {
        this.setOldDecText(this.decText);
        this.decText = !newDecText.startsWith("+") && !newDecText.startsWith("-") ? "+" + newDecText : newDecText;
    }

    public String getChartSizeSelection() {
        return this.chartSizeSelection;
    }

    public void setChartSizeSelection(String selection) {
        if (selection == null || selection.trim().isEmpty()) {
            this.chartSizeSelection = DEFAULT_CHART_SIZE_SELECTION;
        } else {
            this.chartSizeSelection = selection;
        }
    }

    public Range getDomainBounds() {
        return this.domainRange;
    }

    @Override
    public Range getDomainBounds(boolean includeinterval) {
        return this.domainRange;
    }

    public double getDomainLowerBound() {
        return this.domainMin.doubleValue();
    }

    @Override
    public double getDomainLowerBound(boolean includeinterval) {
        return this.domainMin.doubleValue();
    }

    public Range getDomainRange() {
        return this.domainRange;
    }

    public double getDomainUpperBound() {
        return this.domainMax.doubleValue();
    }

    @Override
    public double getDomainUpperBound(boolean includeinterval) {
        return this.domainMax.doubleValue();
    }

    public Number getMaximumDomainValue() {
        return this.domainMax;
    }

    public Number getMaximumRangeValue() {
        return this.domainMax;
    }

    public Number getMinimumDomainValue() {
        return this.domainMin;
    }

    public Number getMinimumRangeValue() {
        return this.domainMin;
    }

    @Override
    public Range getRangeBounds(boolean includeinterval) {
        return this.range;
    }

    public double getRangeLowerBound() {
        return this.rangeMin.doubleValue();
    }

    @Override
    public double getRangeLowerBound(boolean includeinterval) {
        return this.rangeMin.doubleValue();
    }

    public double getRangeUpperBound() {
        return this.rangeMax.doubleValue();
    }

    @Override
    public double getRangeUpperBound(boolean includeinterval) {
        return this.rangeMax.doubleValue();
    }

    public Range getValueRange() {
        return this.range;
    }

    public void setSeries(int item, int newSeries) {
        this.seriesValue[item] = newSeries;
    }

    public void setTotalCount(int newTotalCount) {
        this.totalCount = newTotalCount;
    }

    public void setStar(String newStar) {
        this.star = newStar;
    }

    public void setOldRAText(String oldText) {
        this.oldRAText = oldText;
    }

    public void setOldDecText(String oldText) {
        this.oldDecText = oldText;
    }

    public void setLowerRA(double ra) {
        this.lowerRA = ra;
    }

    public void setUpperRA(double ra) {
        this.upperRA = ra;
    }

    public void setCentralRA(double centralRA) {
        this.raToPlot = centralRA;
    }

    public void setLowerDec(double dec) {
        this.lowerDec = dec;
    }

    public void setUpperDec(double dec) {
        this.upperDec = dec;
    }

    public void setCentralDec(double centralDec) {
        this.decToPlot = centralDec;
    }

    public void setRaHrs(long hrs) {
        this.raHrs = hrs;
    }

    public void setRaMins(long mins) {
        this.raMins = mins;
    }

    public void setRaSecs(BigDecimal secs) {
        this.raSecs = secs;
    }

    public void setDecSign(String sign) {
        this.decSign = sign;
    }

    public void setDecDegs(long degs) {
        this.decDegs = degs;
    }

    public void setDecMins(long mins) {
        this.decMins = mins;
    }

    public void setDecSecs(BigDecimal secs) {
        this.decSecs = secs;
    }

    public void setTychoBoxSelected(Boolean bool) {
        this.tychoBoxSelected = bool;
    }

    public void setNofsBoxSelected(Boolean bool) {
        this.nofsBoxSelected = bool;
    }

    public void setSonoitaBoxSelected(Boolean bool) {
        this.sonoitaBoxSelected = bool;
    }

    public void setBsmBoxSelected(Boolean bool) {
        this.bsmBoxSelected = bool;
    }

    public void setBsm_SBoxSelected(Boolean bool) {
        this.bsm_SBoxSelected = bool;
    }

    public void setCoker30BoxSelected(Boolean bool) {
        this.coker30BoxSelected = bool;
    }

    public void setSro50BoxSelected(Boolean bool) {
        this.sro50BoxSelected = bool;
    }

    public void setTmo61BoxSelected(Boolean bool) {
        this.tmo61BoxSelected = bool;
    }

    public void setApassBoxSelected(Boolean bool) {
        this.apassBoxSelected = bool;
    }

    public void setApass9BoxSelected(Boolean bool) {
        this.apass9BoxSelected = bool;
    }

    public void setGaiaDR2BoxSelected(Boolean bool) {
        this.gaiaDR2BoxSelected = bool;
    }

    public void setGaiaDR3BoxSelected(Boolean bool) {
        this.gaiaDR3BoxSelected = bool;
    }

    public void setPanstarrsBoxSelected(Boolean bool) {
        this.panstarrsBoxSelected = bool;
    }

    public void setSdssBoxSelected(Boolean bool) {
        this.sdssBoxSelected = bool;
    }

    public void setK35BoxSelected(Boolean bool) {
        this.k35BoxSelected = bool;
    }

    public void setW28BoxSelected(Boolean bool) {
        this.w28BoxSelected = bool;
    }

    public void setW30BoxSelected(Boolean bool) {
        this.w30BoxSelected = bool;
    }

    public void setOc61BoxSelected(Boolean bool) {
        this.oc61BoxSelected = bool;
    }

    public void setGcpdBoxSelected(Boolean bool) {
        this.gcpdBoxSelected = bool;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public void setBlankCoord(Boolean bool) {
        this.blankCoord = bool;
    }

    public void setUpperLimitingMag(Double newMag) {
        this.upperLimitingMag = newMag;
    }

    public void setLimitingMag(Double newMag) {
        this.limitingMag = newMag;
    }

    public void setPositionTolerance(Double newPositionTolerance) {
        this.positionTolerance = newPositionTolerance;
    }

    public void setFieldSize(double fieldSize) {
        this.sizeOfPlot = fieldSize;
    }

    public void setTypeOfPlot(String type) {
        this.plotType = type;
    }

    public void setName(int item, String newName) {
        this.name[item] = newName;
    }

    public void setRa(int item, double newRa) {
        this.ra[item] = newRa;
    }

    public void setRaerr(int item, double newRaerr) {
        this.raerr[item] = newRaerr;
    }

    public void setDec(int item, double newDec) {
        this.dec[item] = newDec;
    }

    public void setDecerr(int item, double newDecerr) {
        this.decerr[item] = newDecerr;
    }

    public void setNobs(int item, int newNobs) {
        this.nobs[item] = newNobs;
    }

    public void setVmag(int item, double newVmag) {
        this.vmag[item] = newVmag;
    }

    public void setBMinusV(int item, double newBminusV) {
        this.bMinusV[item] = newBminusV;
    }

    public void setUMinusB(int item, double newUminusB) {
        this.uMinusB[item] = newUminusB;
    }

    public void setVMinusR(int item, double newVminusR) {
        this.vMinusR[item] = newVminusR;
    }

    public void setRMinusI(int item, double newRminusI) {
        this.rMinusI[item] = newRminusI;
    }

    public void setVMinusI(int item, double newVminusI) {
        this.vMinusI[item] = newVminusI;
    }

    public void setEv(int item, double newEv) {
        this.ev[item] = newEv;
    }

    public void setEbv(int item, double newEbv) {
        this.ebv[item] = newEbv;
    }

    public void setEub(int item, double newEub) {
        this.eub[item] = newEub;
    }

    public void setEvr(int item, double newEvr) {
        this.evr[item] = newEvr;
    }

    public void setEri(int item, double newEri) {
        this.eri[item] = newEri;
    }

    public void setEvi(int item, double newEvi) {
        this.evi[item] = newEvi;
    }

    public void setMobs(int item, int newMobs) {
        this.mobs[item] = newMobs;
    }

    public void setSource(int item, int newSource) {
        this.source[item] = newSource;
    }

    public void setFiltersSize(int item, int size) {
        this.filters[item] = new String[size];
    }

    public void setFilters(int item, int fitem, String newFilter) {
        this.filters[item][fitem] = newFilter;
    }

    public void setVarName(int i, String s) {
        this.varName[i] = s;
    }

    public void setVarRa(int i, double d) {
        this.varRa[i] = d;
    }

    public void setVarDec(int i, double d) {
        this.varDec[i] = d;
    }

    public void setVarMax(int i, String s) {
        this.varMax[i] = s;
    }

    public void setVarMin(int i, String s) {
        this.varMin[i] = s;
    }

    public void setVarType(int i, String s) {
        this.varType[i] = s;
    }

    public void setQuitSelected(Boolean quit) {
        this.quitSelected = quit;
    }

    public void setCancelSelected(Boolean bool) {
        this.closeButtonClicked = bool;
    }

    public void setLogfile(String newLogfile) {
        this.logfile = newLogfile;
    }

    public void setPathToLogfile(String newPath) {
        this.path = newPath;
    }

    public void setTablefile(String newTablefile) {
        this.tablefile = newTablefile;
    }

    @Override
    public int getItemCount(int series) {
        return this.itemCount;
    }

    public int getDefaultItemCount() {
        return 5000;
    }

    public int getSeries(int item) {
        return this.seriesValue[item];
    }

    @Override
    public int getSeriesCount() {
        return this.seriesCount;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public int getSelectedStarIndex() {
        return this.selectedStarIndex;
    }

    public void setSelectedStarIndex(int index) {
        this.selectedStarIndex = index;
    }

    public String getStar() {
        return this.star;
    }
    
    public String getVsxDetails() {
        return this.vsxDetails;
    }
    
    public boolean isVsxDataAvailable() {
        return this.vsxDataAvailable;
    }
    
    public String getAuid() {
        return this.auid;
    }
    
    public String getOid() {
        return this.oid;
    }
    
    public void setTransitionMagnitude(Double mag) {
        this.transitionMagnitude = mag;
        System.out.println("DEBUG: Transition magnitude set to: " + (mag != null ? String.format("%.2f", mag) : "null"));
    }
    
    public Double getTransitionMagnitude() {
        return this.transitionMagnitude;
    }
    
    // Offset correction tracking
    private boolean offsetCorrectionEnabled = false;
    private double offsetCorrectionDeltaV = 0.0;
    private double offsetCorrectionRMS = 0.0;
    
    public void setOffsetCorrectionEnabled(boolean enabled) {
        this.offsetCorrectionEnabled = enabled;
        System.out.println("DEBUG: Offset correction " + (enabled ? "enabled" : "disabled"));
    }
    
    public boolean getOffsetCorrectionEnabled() {
        return this.offsetCorrectionEnabled;
    }
    
    public void setOffsetCorrectionDeltaV(double deltaV) {
        this.offsetCorrectionDeltaV = deltaV;
        System.out.println("DEBUG: Offset correction ΔV set to: " + String.format("%.3f", deltaV));
    }
    
    public double getOffsetCorrectionDeltaV() {
        return this.offsetCorrectionDeltaV;
    }
    
    public void setOffsetCorrectionRMS(double rms) {
        this.offsetCorrectionRMS = rms;
        System.out.println("DEBUG: Offset correction RMS set to: " + String.format("%.3f", rms));
    }
    
    public double getOffsetCorrectionRMS() {
        return this.offsetCorrectionRMS;
    }
    
    /**
     * Get the offset correction comment string for a given source
     * Returns the correction string if offset correction is enabled and the source is from a deep catalog
     */
    public String getOffsetCorrectionComment() {
        if (offsetCorrectionEnabled) {
            return String.format("%.3f (%.3f) correction", offsetCorrectionDeltaV, offsetCorrectionRMS);
        }
        return "";
    }
    
    /**
     * Determine if a catalog is a "shallow" (brighter) catalog
     * Shallow catalogs: APASS (29), APASS9 (29), Tycho-2 (1, 901), and other AAVSO catalogs
     * Deep catalogs: Gaia DR2 (48), PanSTARRS (46)
     */
    private boolean isShallowCatalog(int source) {
        // AAVSO internal catalogs (1-44) are generally shallow
        // External catalogs: 29 (APASS/APASS9), 901 (Tycho-2) are shallow
        // External catalogs: 46 (PanSTARRS), 48 (Gaia DR2), 49 (Gaia DR3) are deep
        if (source >= 1 && source <= 44) return true;  // AAVSO catalogs
        if (source == 29) return true;  // APASS/APASS9
        if (source == 901) return true; // Tycho-2
        if (source == 46) return false; // PanSTARRS (deep)
        if (source == 48) return false; // Gaia DR2 (deep, but shallower than PanSTARRS)
        if (source == 49) return false; // Gaia DR3 (deep, similar to Gaia DR2)
        return true; // Default to shallow for unknown catalogs
    }
    
    /**
     * Determine catalog depth ordering (higher number = deeper)
     * Used when both catalogs are "deep" (e.g., Gaia DR2 vs PanSTARRS)
     */
    private int getCatalogDepth(int source) {
        if (source >= 1 && source <= 44) return 1;  // AAVSO catalogs (shallowest)
        if (source == 29) return 1;  // APASS/APASS9
        if (source == 901) return 1; // Tycho-2
        if (source == 48) return 2;  // Gaia DR2 (medium depth)
        if (source == 49) return 2;  // Gaia DR3 (medium depth, same as DR2)
        if (source == 46) return 3;  // PanSTARRS (deepest)
        return 1; // Default
    }
    
    /**
     * Get the preferred catalog entry for a star considering transition magnitude.
     * Returns null if the primary catalog should be used, or a CatalogEntry if an
     * alternative catalog from secondaryCatalogData should be preferred.
     */
    public CatalogEntry getPreferredCatalogEntry(int starIndex) {
        if (transitionMagnitude == null) return null; // No preference if no transition set
        
        double starRA = this.ra[starIndex];
        double starDec = this.dec[starIndex];
        double starVMag = this.vmag[starIndex];
        int starSource = this.source[starIndex];
        
        // Determine preferred catalog type based on magnitude
        boolean preferShallow = (starVMag < transitionMagnitude);
        boolean primaryIsShallow = isShallowCatalog(starSource);
        
        // If star is brighter than transition and primary is already shallow, use primary
        if (preferShallow && primaryIsShallow) {
            return null;
        }
        
        // If star is fainter than transition and primary is already deep, check if there's something deeper
        if (!preferShallow && !primaryIsShallow) {
            // Look for a deeper catalog match
            CatalogEntry deepest = null;
            int deepestDepth = getCatalogDepth(starSource);
            
            java.util.List<CatalogEntry> matches = findCrossMatches(starRA, starDec, starSource);
            for (CatalogEntry match : matches) {
                if (!isShallowCatalog(match.source)) {
                    int matchDepth = getCatalogDepth(match.source);
                    if (matchDepth > deepestDepth) {
                        deepest = match;
                        deepestDepth = matchDepth;
                    }
                }
            }
            
            if (deepest != null) {
                System.out.println("DEBUG: Transition filter - star at V=" + String.format("%.2f", starVMag) + 
                                 " switched from source " + starSource + " to preferred source " + deepest.source);
            }
            return deepest;
        }
        
        // Star magnitude doesn't match primary catalog type - look for better match
        java.util.List<CatalogEntry> matches = findCrossMatches(starRA, starDec, starSource);
        
        if (preferShallow) {
            // Want shallow but primary is deep - look for shallow match
            for (CatalogEntry match : matches) {
                if (isShallowCatalog(match.source)) {
                    System.out.println("DEBUG: Transition filter - star at V=" + String.format("%.2f", starVMag) + 
                                     " switched from source " + starSource + " to preferred source " + match.source);
                    return match;
                }
            }
        } else {
            // Want deep but primary is shallow - look for deepest match
            CatalogEntry deepest = null;
            int deepestDepth = 0;
            
            for (CatalogEntry match : matches) {
                if (!isShallowCatalog(match.source)) {
                    int matchDepth = getCatalogDepth(match.source);
                    if (matchDepth > deepestDepth) {
                        deepest = match;
                        deepestDepth = matchDepth;
                    }
                }
            }
            
            if (deepest != null) {
                System.out.println("DEBUG: Transition filter - star at V=" + String.format("%.2f", starVMag) + 
                                 " switched from source " + starSource + " to preferred source " + deepest.source);
            }
            return deepest;
        }
        
        return null; // No better match found
    }

    public String getOldRAText() {
        return this.oldRAText;
    }

    public String getOldDecText() {
        return this.oldDecText;
    }

    public String getRAText() {
        return this.raText;
    }

    public String getDecText() {
        return this.decText;
    }

    public Boolean getTychoBoxSelected() {
        return this.tychoBoxSelected;
    }

    public Boolean getNofsBoxSelected() {
        return this.nofsBoxSelected;
    }

    public Boolean getSonoitaBoxSelected() {
        return this.sonoitaBoxSelected;
    }

    public Boolean getBsmBoxSelected() {
        return this.bsmBoxSelected;
    }

    public Boolean getBsm_SBoxSelected() {
        return this.bsm_SBoxSelected;
    }

    public Boolean getCoker30BoxSelected() {
        return this.coker30BoxSelected;
    }

    public Boolean getSro50BoxSelected() {
        return this.sro50BoxSelected;
    }

    public Boolean getTmo61BoxSelected() {
        return this.tmo61BoxSelected;
    }

    public Boolean getApassBoxSelected() {
        return this.apassBoxSelected;
    }

    public Boolean getApass9BoxSelected() {
        return this.apass9BoxSelected;
    }

    public Boolean getGaiaDR2BoxSelected() {
        return this.gaiaDR2BoxSelected;
    }

    public Boolean getGaiaDR3BoxSelected() {
        return this.gaiaDR3BoxSelected;
    }

    public Boolean getPanstarrsBoxSelected() {
        return this.panstarrsBoxSelected;
    }

    public Boolean getSdssBoxSelected() {
        return this.sdssBoxSelected;
    }

    public Boolean getK35BoxSelected() {
        return this.k35BoxSelected;
    }

    public Boolean getW28BoxSelected() {
        return this.w28BoxSelected;
    }

    public Boolean getW30BoxSelected() {
        return this.w30BoxSelected;
    }

    public Boolean getOc61BoxSelected() {
        return this.oc61BoxSelected;
    }

    public Boolean getGcpdBoxSelected() {
        return this.gcpdBoxSelected;
    }

    public double getLowerRA() {
        return this.lowerRA;
    }

    public double getUpperRA() {
        return this.upperRA;
    }

    public Double getCentralRA() {
        return this.raToPlot;
    }

    public double getLowerDec() {
        return this.lowerDec;
    }

    public double getUpperDec() {
        return this.upperDec;
    }

    public Double getCentralDec() {
        return this.decToPlot;
    }

    @Override
    public Number getX(int series, int item) {
        return new Double(this.xVal[series][item]);
    }

    @Override
    public Number getY(int series, int item) {
        return new Double(this.yVal[series][item]);
    }

    @Override
    public Number getZ(int series, int item) {
        return new Double(this.zVal[series][item]);
    }

    public Number getXVal(int series, int item) {
        return this.xVal[series][item];
    }

    public Number getYVal(int series, int item) {
        return this.yVal[series][item];
    }

    public Number getZVal(int series, int item) {
        return this.zVal[series][item];
    }

    public long getRaHrs() {
        return this.raHrs;
    }

    public long getRaMins() {
        return this.raMins;
    }

    public BigDecimal getRaSecs() {
        return this.raSecs;
    }

    public String getDecSign() {
        return this.decSign;
    }

    public long getDecDegs() {
        return this.decDegs;
    }

    public long getDecMins() {
        return this.decMins;
    }

    public BigDecimal getDecSecs() {
        return this.decSecs;
    }

    public String getUsername() {
        return this.username;
    }

    public Boolean getBlankCoord() {
        return this.blankCoord;
    }

    public double getUpperLimitingMag() {
        return this.upperLimitingMag;
    }

    public double getUpperLimitingMagDefault() {
        return -5.0;
    }

    public double getLimitingMag() {
        return this.limitingMag;
    }

    public double getPositionTolerance() {
        return this.positionTolerance;
    }

    public double getFieldSize() {
        return this.sizeOfPlot;
    }

    public int getCatalogReadTimeoutSeconds() {
        return this.catalogReadTimeoutSeconds;
    }

    public void setCatalogReadTimeoutSeconds(int seconds) {
        if (seconds < 10) seconds = 10;  // Minimum 10 seconds
        if (seconds > 600) seconds = 600;  // Maximum 10 minutes
        this.catalogReadTimeoutSeconds = seconds;
    }

    public String getTypeOfPlot() {
        return this.plotType;
    }

    public String getName(int item) {
        return this.name[item];
    }

    public double getRa(int item) {
        return this.ra[item];
    }

    public double getRaerr(int item) {
        return this.raerr[item];
    }

    public double getDec(int item) {
        return this.dec[item];
    }

    public double getDecerr(int item) {
        return this.decerr[item];
    }

    public int getNobs(int item) {
        return this.nobs[item];
    }

    public double getVmag(int item) {
        return this.vmag[item];
    }

    public double getBMinusV(int item) {
        return this.bMinusV[item];
    }

    public double getUMinusB(int item) {
        return this.uMinusB[item];
    }

    public double getVMinusR(int item) {
        return this.vMinusR[item];
    }

    public double getRMinusI(int item) {
        return this.rMinusI[item];
    }

    public double getVMinusI(int item) {
        return this.vMinusI[item];
    }

    public double getEv(int item) {
        return this.ev[item];
    }

    public double getEbv(int item) {
        return this.ebv[item];
    }

    public double getEub(int item) {
        return this.eub[item];
    }

    public double getEvr(int item) {
        return this.evr[item];
    }

    public double getEri(int item) {
        return this.eri[item];
    }

    public double getEvi(int item) {
        return this.evi[item];
    }

    public int getMobs(int item) {
        return this.mobs[item];
    }

    public int getSource(int item) {
        return this.source[item];
    }

    // Getter methods for additional APASS9 photometric data
    public double getBmag(int item) {
        return this.bmag[item];
    }

    public double getE_Bmag(int item) {
        return this.e_bmag[item];
    }

    public double getG_PrimeMag(int item) {
        return this.g_prime_mag[item];
    }

    public double getE_G_PrimeMag(int item) {
        return this.e_g_prime_mag[item];
    }

    public double getR_PrimeMag(int item) {
        return this.r_prime_mag[item];
    }

    public double getE_R_PrimeMag(int item) {
        return this.e_r_prime_mag[item];
    }

    public double getI_PrimeMag(int item) {
        return this.i_prime_mag[item];
    }

    public double getE_I_PrimeMag(int item) {
        return this.e_i_prime_mag[item];
    }

    // Getter methods for magnitude range and center coordinates
    public double getMinVMag() {
        return this.minZ;
    }

    public double getMaxVMag() {
        return this.maxZ;
    }

    public double getCenterRa() {
        // Use actual RA bounds, not transformed coordinates
        return (this.minRa + this.maxRa) / 2.0;
    }

    public double getCenterDec() {
        // Use actual Dec bounds, not transformed coordinates  
        return (this.minDec + this.maxDec) / 2.0;
    }

    public double getCoordErrorRA(int item) {
        return this.coord_error_ra[item];
    }

    public double getCoordErrorDec(int item) {
        return this.coord_error_dec[item];
    }

    public String getFilters(int item, int fitem) {
        return String.join((CharSequence)",", Arrays.copyOfRange(this.filters[item][fitem].split(",", 0), 0, 3));
    }

    public int getFiltersSize(int item) {
        return this.filters[item].length;
    }

    public String getFilterX(int item, String fname) {
        String ret = "NA,NA";
        int i = 0;
        while (i < this.filters[item].length) {
            if (this.filters[item][i].indexOf(fname) == 0) {
                ret = String.join((CharSequence)",", Arrays.copyOfRange(this.filters[item][i].split(",", 0), 1, 3));
                break;
            }
            ++i;
        }
        return ret;
    }

    public String getVarName(int i) {
        return this.varName[i];
    }

    public double getVarRa(int i) {
        return this.varRa[i];
    }

    public double getVarDec(int i) {
        return this.varDec[i];
    }

    public String getVarMax(int i) {
        return this.varMax[i];
    }

    public String getVarMin(int i) {
        return this.varMin[i];
    }

    public String getVarType(int i) {
        return this.varType[i];
    }

    public Boolean getQuitSelected() {
        return this.quitSelected;
    }

    public Boolean getCancelSelected() {
        return this.closeButtonClicked;
    }

    public String getLogfile(String v) {
        String[] ss = this.logfile.split("\\.");
        return String.valueOf(ss[0]) + "_" + v + "." + ss[1];
    }

    public String getPathToLogfile() {
        return this.path;
    }

    public String getTablefile(String v) {
        String[] ss = this.tablefile.split("\\.");
        return String.valueOf(ss[0]) + "_" + v + "." + ss[1];
    }

    public String getBaseURL() {
        return BASE_URL;
    }

    public double getMinX() {
        return this.minX;
    }

    public double getMaxX() {
        return this.maxX;
    }

    public double getMinY() {
        return this.minY;
    }

    public double getMaxY() {
        return this.maxY;
    }
    
    /**
     * Get tangent plane center RA in degrees
     */
    public double getTangentPlaneCenterRA() {
        return (this.minRa + this.maxRa) / 2.0;
    }
    
    /**
     * Get tangent plane center Dec in degrees  
     */
    public double getTangentPlaneCenterDec() {
        return (this.minDec + this.maxDec) / 2.0;
    }
    
    /**
     * Convert tangent plane coordinates (xi, eta) back to RA/Dec
     * Inverse of RaDectoXY method
     * @param xi tangent plane X coordinate (degrees)
     * @param eta tangent plane Y coordinate (degrees)
     * @return double[2] array: [ra, dec] in degrees
     */
    public double[] XYtoRaDec(double xi, double eta) {
        double picon = Math.PI / 180.0;
        double racent = picon * this.getTangentPlaneCenterRA();
        double deccent = picon * this.getTangentPlaneCenterDec();
        
        // Convert xi, eta to radians
        double xiRad = xi * picon;
        double etaRad = eta * picon;
        
        // Inverse tangent plane projection
        double rho = Math.sqrt(xiRad * xiRad + etaRad * etaRad);
        double c = Math.atan(rho);
        
        double cosC = Math.cos(c);
        double sinC = Math.sin(c);
        
        // Calculate Dec
        double dec;
        if (rho == 0.0) {
            dec = deccent;
        } else {
            dec = Math.asin(cosC * Math.sin(deccent) + (etaRad * sinC * Math.cos(deccent)) / rho);
        }
        
        // Calculate RA
        double ra;
        if (rho == 0.0) {
            ra = racent;
        } else {
            ra = racent + Math.atan2(xiRad * sinC, rho * Math.cos(deccent) * cosC - etaRad * Math.sin(deccent) * sinC);
        }
        
        // Convert back to degrees
        double raDeg = ra / picon;
        double decDeg = dec / picon;
        
        // Normalize RA to 0-360 range
        while (raDeg < 0.0) raDeg += 360.0;
        while (raDeg >= 360.0) raDeg -= 360.0;
        
        return new double[]{raDeg, decDeg};
    }

    private static boolean readBoolean(List<String> entries, int index, boolean defaultValue) {
        if (index < entries.size()) {
            String value = entries.get(index);
            if (value != null && !value.isEmpty()) {
                return Boolean.parseBoolean(value);
            }
        }
        return defaultValue;
    }

    private static double readDouble(List<String> entries, int index, double defaultValue) {
        if (index < entries.size()) {
            try {
                return Double.parseDouble(entries.get(index));
            }
            catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static int readInt(List<String> entries, int index, int defaultValue) {
        if (index < entries.size()) {
            try {
                return Integer.parseInt(entries.get(index));
            }
            catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static String readString(List<String> entries, int index, String defaultValue) {
        if (index < entries.size()) {
            String value = entries.get(index);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return defaultValue;
    }

    private static boolean isBooleanToken(String value) {
        return value != null && ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value));
    }
    
    // Method to add entry to secondary catalog
    public void addSecondaryCatalogEntry(String name, double ra, double dec, double vmag, double ev,
                                        double bMinusV, double ebv, double vMinusI, double evi, int source, int nobs) {
        secondaryCatalogData.add(new CatalogEntry(name, ra, dec, vmag, ev, bMinusV, ebv, vMinusI, evi, source, nobs));
    }
    
    // Secondary catalog loading methods - these populate secondaryCatalogData for cross-matching
    
    private void loadApass9Secondary() {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Loading APASS9 as secondary catalog...");
            
            String tapUrl = CDSMirrorSelector.getSelectedTapUrl();
            String adqlQuery = String.format(
                "SELECT RAJ2000, DEJ2000, Vmag, e_Vmag, \"B-V\", \"e_B-V\", nobs " +
                "FROM \"II/336/apass9\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', RAJ2000, DEJ2000), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f)) " +
                "AND Vmag IS NOT NULL AND Vmag <= %.1f",
                this.getCentralRA(), this.getCentralDec(), 
                this.getFieldSize() / 2.0, this.getLimitingMag()
            );
            
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            System.out.println("  Connecting to VizieR...");
            long stepStart = System.currentTimeMillis();
            URL url = new URL(queryUrl);
            System.out.printf("    → Opening connection to %s\n", url.getHost());
            System.out.printf("    → Sending TAP request...\n");
            Document document = this.getDocument(url);
            long downloadTime = System.currentTimeMillis() - stepStart;
            System.out.printf("    → Data received\n");
            System.out.printf("✓ (%.1fs)\n", downloadTime / 1000.0);
            
            if (document == null) {
                System.out.println("  ERROR: Failed to retrieve data");
                return;
            }
            
            // Parse VOTable and add to secondaryCatalogData
            System.out.print("  Parsing catalog data... ");
            stepStart = System.currentTimeMillis();
            
            NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
            int processedCount = 0;
            int addedCount = 0;
            
            if (tableDataNodes.getLength() > 0) {
                Element tableData = (Element) tableDataNodes.item(0);
                NodeList rows = tableData.getElementsByTagName("TR");
                
                for (int i = 0; i < rows.getLength(); i++) {
                    Element row = (Element) rows.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 7) {
                        try {
                            processedCount++;
                            
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double vmag = Double.parseDouble(cells.item(2).getTextContent().trim());
                            double e_vmag = cells.item(3).getTextContent().trim().isEmpty() ? 0.0 : 
                                          Double.parseDouble(cells.item(3).getTextContent().trim());
                            double bminusv = cells.item(4).getTextContent().trim().isEmpty() ? 0.0 : 
                                           Double.parseDouble(cells.item(4).getTextContent().trim());
                            double e_bminusv = cells.item(5).getTextContent().trim().isEmpty() ? 0.0 : 
                                             Double.parseDouble(cells.item(5).getTextContent().trim());
                            int nobs = this.parseIntOrDefault(cells.item(6).getTextContent().trim(), 0);
                            
                            // APASS9 has B-V but not V-I, so we pass 0.0 for V-I values
                            addSecondaryCatalogEntry("APASS9", ra, dec, vmag, e_vmag, bminusv, e_bminusv, 0.0, 0.0, 29, nobs);
                            addedCount++;
                        } catch (NumberFormatException e) {
                            // Skip invalid entries
                        }
                    }
                }
            }
            
            long parseTime = System.currentTimeMillis() - stepStart;
            System.out.printf("✓ (%.1fs)\n", parseTime / 1000.0);
            System.out.printf("  Added %d APASS9 stars\n", addedCount);
            
            long totalTime = System.currentTimeMillis() - startTime;
            System.out.printf("  Total time: %.1fs\n", totalTime / 1000.0);
        } catch (Exception e) {
            System.err.println("Error loading APASS9 secondary data: " + e.getMessage());
        }
    }
    
    private void loadGaiaDR2Secondary() {
        try {
            System.out.println("Loading Gaia DR2 as secondary catalog...");
            // Use ESA Gaia Archive - much faster than VizieR mirror
            String tapUrl = "https://gea.esac.esa.int/tap-server/tap/sync";
            
            double raMin = this.getLowerRA();
            double raMax = this.getUpperRA();
            double decMin = this.getLowerDec();
            double decMax = this.getUpperDec();
            
            // Use ESA Gaia Archive with gaiadr2.gaia_source table (not VizieR)
            String adqlQuery = String.format(
                "SELECT TOP 5000 ra, dec, phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag, phot_g_n_obs " +
                "FROM gaiadr2.gaia_source " +
                "WHERE ra BETWEEN %.6f AND %.6f " +
                "AND dec BETWEEN %.6f AND %.6f " +
                "AND phot_g_mean_mag IS NOT NULL AND phot_bp_mean_mag IS NOT NULL AND phot_rp_mean_mag IS NOT NULL " +
                "AND phot_bp_mean_mag < 19.0 AND phot_g_mean_mag <= %.1f",
                raMin, raMax, decMin, decMax, this.getLimitingMag()
            );
            
            // Use FORMAT=votable_plain to get TABLEDATA instead of BINARY2
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable_plain&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            URL url = new URL(queryUrl);
            Document document = this.getDocument(url);
            if (document == null) return;
            
            // Parse VOTable and transform to V, V-I
            NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
            if (tableDataNodes.getLength() > 0) {
                Element tableData = (Element) tableDataNodes.item(0);
                NodeList rows = tableData.getElementsByTagName("TR");
                
                for (int i = 0; i < rows.getLength(); i++) {
                    Element row = (Element) rows.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 6) {
                        try {
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double G = Double.parseDouble(cells.item(2).getTextContent().trim());
                            double BP = Double.parseDouble(cells.item(3).getTextContent().trim());
                            double RP = Double.parseDouble(cells.item(4).getTextContent().trim());
                            int o_Gmag = this.parseIntOrDefault(cells.item(5).getTextContent().trim(), 0);
                            
                            // Apply Gaia DR2 → Johnson-Cousins transformations (Evans et al. 2018)
                            // Same as primary parser
                            double bp_rp = BP - RP;
                            double bp_rp_sq = bp_rp * bp_rp;
                            
                            // V magnitude
                            double v_correction = -(-0.01760 - 0.006860 * bp_rp - 0.1732 * bp_rp_sq);
                            double V = G + v_correction;
                            
                            // I magnitude
                            double i_correction = -(-0.02085 + 0.7419 * bp_rp - 0.09631 * bp_rp_sq);
                            double I = G + i_correction;
                            
                            // V-I color (calculated from transformed V and I)
                            double V_I = V - I;
                            
                            // Estimate errors (simplified)
                            double e_V = 0.03; // Conservative estimate
                            double e_VI = 0.05;
                            
                            // Filter by limiting magnitude (using transformed V)
                            if (V <= this.getLimitingMag()) {
                                // Gaia DR2 has V and V-I, but not B-V directly; we can estimate B-V from V-I
                                // Typical relation: B-V ≈ 0.87 * V-I (approximate for solar-type stars)
                                double B_V = 0.87 * V_I;
                                double e_BV = 0.87 * e_VI;
                                addSecondaryCatalogEntry("Gaia DR2", ra, dec, V, e_V, B_V, e_BV, V_I, e_VI, 48, o_Gmag);
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid entries
                        }
                    }
                }
            }
            System.out.println("Loaded " + secondaryCatalogData.size() + " Gaia DR2 secondary stars");
        } catch (Exception e) {
            System.err.println("Error loading Gaia DR2 secondary data: " + e.getMessage());
        }
    }
    
    private void loadGaiaDR3Secondary() {
        try {
            System.out.println("Loading Gaia DR3 as secondary catalog...");
            // Use ESA Gaia Archive - much faster than VizieR mirror
            String tapUrl = "https://gea.esac.esa.int/tap-server/tap/sync";
            
            double raMin = this.getLowerRA();
            double raMax = this.getUpperRA();
            double decMin = this.getLowerDec();
            double decMax = this.getUpperDec();
            
            // Use ESA Gaia Archive with gaiadr3.gaia_source table
            String adqlQuery = String.format(
                "SELECT TOP 5000 ra, dec, phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag, phot_g_n_obs " +
                "FROM gaiadr3.gaia_source " +
                "WHERE ra BETWEEN %.6f AND %.6f " +
                "AND dec BETWEEN %.6f AND %.6f " +
                "AND phot_g_mean_mag IS NOT NULL AND phot_bp_mean_mag IS NOT NULL AND phot_rp_mean_mag IS NOT NULL " +
                "AND phot_bp_mean_mag < 19.0 AND phot_g_mean_mag <= %.1f",
                raMin, raMax, decMin, decMax, this.getLimitingMag()
            );
            
            // Use FORMAT=votable_plain to get TABLEDATA instead of BINARY2
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable_plain&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            URL url = new URL(queryUrl);
            Document document = this.getDocument(url);
            if (document == null) return;
            
            // Parse VOTable and transform to V, V-I
            NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
            if (tableDataNodes.getLength() > 0) {
                Element tableData = (Element) tableDataNodes.item(0);
                NodeList rows = tableData.getElementsByTagName("TR");
                
                for (int i = 0; i < rows.getLength(); i++) {
                    Element row = (Element) rows.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 6) {
                        try {
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double G = Double.parseDouble(cells.item(2).getTextContent().trim());
                            double BP = Double.parseDouble(cells.item(3).getTextContent().trim());
                            double RP = Double.parseDouble(cells.item(4).getTextContent().trim());
                            int o_Gmag = this.parseIntOrDefault(cells.item(5).getTextContent().trim(), 0);
                            
                            // Apply Gaia DR3 → Johnson-Cousins transformations (same as DR2, Evans et al. 2018)
                            double bp_rp = BP - RP;
                            double bp_rp_sq = bp_rp * bp_rp;
                            
                            // V magnitude
                            double v_correction = -(-0.01760 - 0.006860 * bp_rp - 0.1732 * bp_rp_sq);
                            double V = G + v_correction;
                            
                            // I magnitude
                            double i_correction = -(-0.02085 + 0.7419 * bp_rp - 0.09631 * bp_rp_sq);
                            double I = G + i_correction;
                            
                            // V-I color (calculated from transformed V and I)
                            double V_I = V - I;
                            
                            // Estimate errors (simplified)
                            double e_V = 0.03; // Conservative estimate
                            double e_VI = 0.05;
                            
                            // Filter by limiting magnitude (using transformed V)
                            if (V <= this.getLimitingMag()) {
                                // Estimate B-V from V-I (B-V ≈ 0.87 * V-I)
                                double B_V = 0.87 * V_I;
                                double e_BV = 0.87 * e_VI;
                                addSecondaryCatalogEntry("Gaia DR3", ra, dec, V, e_V, B_V, e_BV, V_I, e_VI, 49, o_Gmag);
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid entries
                        }
                    }
                }
            }
            System.out.println("Loaded " + secondaryCatalogData.size() + " Gaia DR3 secondary stars");
        } catch (Exception e) {
            System.err.println("Error loading Gaia DR3 secondary data: " + e.getMessage());
        }
    }
    
    private void loadPanstarrsSecondary() {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Loading PanSTARRS DR1 as secondary catalog...");
            
            // Use CDS VizieR TAP - faster and more complete than MAST API
            String tapUrl = CDSMirrorSelector.getSelectedTapUrl();
            
            // Calculate magnitude constraint for r-band
            double rMagLimit = this.getLimitingMag() + 1.0;
            
            // Build ADQL query for PanSTARRS DR1
            // Include NOT NULL constraints for all required photometry columns
            String adqlQuery = String.format(
                "SELECT RAJ2000, DEJ2000, gmag, rmag, imag, Ng " +
                "FROM \"II/349/ps1\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', RAJ2000, DEJ2000), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f)) " +
                "AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL " +
                "AND rmag <= %.1f",
                this.getCentralRA(), this.getCentralDec(), 
                this.getFieldSize() / 2.0, rMagLimit
            );
            
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            System.out.println("  Connecting to VizieR...");
            long stepStart = System.currentTimeMillis();
            URL url = new URL(queryUrl);
            System.out.printf("    → Opening connection to %s\n", url.getHost());
            System.out.printf("    → Sending TAP request for PanSTARRS DR1...\n");
            Document document = this.getDocument(url);
            long downloadTime = System.currentTimeMillis() - stepStart;
            System.out.printf("    → Data received\n");
            System.out.printf("  ✓ (%.1fs)\n", downloadTime / 1000.0);
            
            if (document == null) {
                System.out.println("  ERROR: Failed to retrieve data");
                return;
            }
            
            // Parse VOTable and transform to V, V-I
            System.out.print("  Parsing and transforming photometry... ");
            stepStart = System.currentTimeMillis();
            
            NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
            int processedCount = 0;
            int addedCount = 0;
            
            if (tableDataNodes.getLength() > 0) {
                Element tableData = (Element) tableDataNodes.item(0);
                NodeList rows = tableData.getElementsByTagName("TR");
                int totalRows = rows.getLength();
                
                for (int i = 0; i < totalRows; i++) {
                    Element row = (Element) rows.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    // VizieR returns: RAJ2000, DEJ2000, gmag, rmag, imag, Ng
                    if (cells.getLength() >= 6) {
                        try {
                            processedCount++;
                            
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double g = Double.parseDouble(cells.item(2).getTextContent().trim());
                            double r = Double.parseDouble(cells.item(3).getTextContent().trim());
                            double imag = Double.parseDouble(cells.item(4).getTextContent().trim());
                            int ng = this.parseIntOrDefault(cells.item(5).getTextContent().trim(), 0);
                            
                            // Apply PanSTARRS → Johnson-Cousins transformations (same as primary parser)
                            double g_r = g - r;
                            double r_i_raw = r - imag;
                            
                            // V magnitude
                            double V = g - 0.59 * g_r - 0.01;
                            
                            // B-V color
                            double B_V = g_r + 0.22;
                            
                            // V-R color
                            double vr = 1.09 * r_i_raw + 0.22;
                            
                            // R-I color
                            double ri = r_i_raw + 0.21;
                            
                            // V-I color (primary for PanSTARRS)
                            double V_I = vr + ri;
                            
                            // Estimate errors (simplified)
                            double e_V = 0.03;
                            double e_VI = 0.05;
                            double e_BV = 0.05;
                            
                            // Filter by limiting magnitude
                            if (V <= this.getLimitingMag()) {
                                addSecondaryCatalogEntry("PanSTARRS DR1", ra, dec, V, e_V, B_V, e_BV, V_I, e_VI, 46, ng);
                                addedCount++;
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid entries
                        }
                    }
                }
            }
            
            long parseTime = System.currentTimeMillis() - stepStart;
            System.out.printf("✓ (%.1fs)\n", parseTime / 1000.0);
            System.out.printf("  Processed %d rows, added %d stars (%.1f%% passed magnitude filter)\n", 
                processedCount, addedCount, 
                processedCount > 0 ? 100.0 * addedCount / processedCount : 0.0);
            
            long totalTime = System.currentTimeMillis() - startTime;
            System.out.printf("  Total time: %.1fs\n", totalTime / 1000.0);
            
            // Warn if no data and Dec < -30 (outside coverage)
            if (addedCount == 0 && this.getCentralDec() < -30.0) {
                System.out.println("WARNING: PanSTARRS has limited coverage south of Dec -30°");
            }
        } catch (Exception e) {
            System.err.println("Error loading PanSTARRS secondary data: " + e.getMessage());
        }
    }
    
    private void loadSdssSecondary() {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Loading SDSS DR12 as secondary catalog...");
            
            // Use CDS VizieR TAP for SDSS DR12 catalog
            String tapUrl = CDSMirrorSelector.getSelectedTapUrl();
            
            // Build ADQL query for SDSS DR12
            double gMagLimit = this.getLimitingMag() + 1.0;
            String adqlQuery = String.format(
                "SELECT RA_ICRS, DE_ICRS, umag, gmag, rmag, imag " +
                "FROM \"V/147/sdss12\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', RA_ICRS, DE_ICRS), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f)) " +
                "AND umag IS NOT NULL " +
                "AND gmag IS NOT NULL " +
                "AND rmag IS NOT NULL " +
                "AND imag IS NOT NULL " +
                "AND gmag <= %.1f",
                this.getCentralRA(), this.getCentralDec(), this.getFieldSize(), gMagLimit
            );
            
            System.out.println("  Connecting to VizieR...");
            long stepStart = System.currentTimeMillis();
            
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            URL url = URI.create(queryUrl).toURL();
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(this.getCatalogReadTimeoutSeconds() * 1000);
            
            java.io.InputStream inputStream = connection.getInputStream();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            
            long downloadTime = System.currentTimeMillis() - stepStart;
            System.out.printf("    → Data received\n");
            System.out.printf("  ✓ (%.1fs)\n", downloadTime / 1000.0);
            
            // Parse VOTable response
            System.out.print("  Parsing and transforming photometry... ");
            stepStart = System.currentTimeMillis();
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
            
            NodeList tableDatas = document.getElementsByTagName("TABLEDATA");
            int processedCount = 0;
            int addedCount = 0;
            
            if (tableDatas.getLength() > 0) {
                Element tableData = (Element) tableDatas.item(0);
                NodeList trNodes = tableData.getElementsByTagName("TR");
                
                for (int i = 0; i < trNodes.getLength(); i++) {
                    Element row = (Element) trNodes.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 6) {
                        try {
                            processedCount++;
                            
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double umag = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 99.999);
                            double gmag = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 99.999);
                            double rmag = this.parseDoubleOrDefault(cells.item(4).getTextContent().trim(), 99.999);
                            double imag = this.parseDoubleOrDefault(cells.item(5).getTextContent().trim(), 99.999);
                            
                            // Transform SDSS to Johnson-Cousins
                            double u_g = umag - gmag;
                            double g_r = gmag - rmag;
                            double r_i = rmag - imag;
                            
                            double V_jc = gmag - 0.5784 * g_r - 0.0038;
                            double B_jc = umag - 0.8116 * u_g + 0.1313;
                            double I_jc = rmag - 1.2444 * r_i - 0.3820;
                            
                            double BV = B_jc - V_jc;
                            double V_I = V_jc - I_jc;
                            
                            double e_V = 0.01;
                            double e_BV = 0.02;
                            double e_VI = 0.03;
                            
                            if (V_jc <= this.getLimitingMag()) {
                                addSecondaryCatalogEntry("SDSS DR12", ra, dec, V_jc, e_V, BV, e_BV, V_I, e_VI, 21, 0);
                                addedCount++;
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid entries
                        }
                    }
                }
            }
            
            long parseTime = System.currentTimeMillis() - stepStart;
            System.out.printf("✓ (%.1fs)\n", parseTime / 1000.0);
            System.out.printf("  Processed %d rows, added %d stars (%.1f%% passed magnitude filter)\n", 
                processedCount, addedCount, 
                processedCount > 0 ? 100.0 * addedCount / processedCount : 0.0);
            
            long totalTime = System.currentTimeMillis() - startTime;
            System.out.printf("  Total time: %.1fs\n", totalTime / 1000.0);
        } catch (Exception e) {
            System.err.println("Error loading SDSS secondary data: " + e.getMessage());
        }
    }
    
    private void loadTycho2Secondary() {
        try {
            System.out.println("Loading Tycho-2 as secondary catalog...");
            String tapUrl = CDSMirrorSelector.getSelectedTapUrl();
            String adqlQuery = String.format(
                "SELECT RAmdeg, DEmdeg, VTmag, e_VTmag, BTmag, e_BTmag, num " +
                "FROM \"I/259/tyc2\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', RAmdeg, DEmdeg), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f)) " +
                "AND VTmag IS NOT NULL AND BTmag IS NOT NULL",
                this.getCentralRA(), this.getCentralDec(), this.getFieldSize()
            );
            
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            URL url = new URL(queryUrl);
            Document document = this.getDocument(url);
            if (document == null) return;
            
            // Parse VOTable and transform to V, B-V
            NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
            if (tableDataNodes.getLength() > 0) {
                Element tableData = (Element) tableDataNodes.item(0);
                NodeList rows = tableData.getElementsByTagName("TR");
                
                for (int i = 0; i < rows.getLength(); i++) {
                    Element row = (Element) rows.item(i);
                    NodeList cells = row.getElementsByTagName("TD");
                    
                    if (cells.getLength() >= 7) {
                        try {
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            double Vt = Double.parseDouble(cells.item(2).getTextContent().trim());
                            double Bt = Double.parseDouble(cells.item(4).getTextContent().trim());
                            String numStr = cells.item(6).getTextContent().trim();
                            int num = this.parseIntOrDefault(numStr, 0);
                            double bt_vt = Bt - Vt;
                            
                            // Apply Tycho-2 → Johnson-Cousins transformations (Henden 2003)
                            // Same as primary parser
                            double B = Bt + 0.018 - 0.2580 * bt_vt;
                            double V = Vt + 0.008 - 0.0988 * bt_vt;
                            double I = Vt - 0.039 - 0.9376 * bt_vt;
                            
                            // Calculate color indices from transformed magnitudes
                            double B_V = B - V;
                            double V_I = V - I;
                            
                            // Estimate errors (simplified)
                            double e_V = 0.03;
                            double e_BV = 0.05;
                            double e_VI = 0.06;
                            
                            // Filter by limiting magnitude
                            if (V <= this.getLimitingMag()) {
                                addSecondaryCatalogEntry("Tycho-2", ra, dec, V, e_V, B_V, e_BV, V_I, e_VI, 901, num);
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid entries
                        }
                    }
                }
            }
            System.out.println("Loaded " + secondaryCatalogData.size() + " Tycho-2 secondary stars");
        } catch (Exception e) {
            System.err.println("Error loading Tycho-2 secondary data: " + e.getMessage());
        }
    }
    
    // Method to find all matches in other selected catalogs within 2 arcseconds
    // This searches the already-loaded secondary catalog data (in-memory search)
    public java.util.List<CatalogEntry> findCrossMatches(double targetRa, double targetDec, int primarySource) {
        java.util.List<CatalogEntry> matches = new java.util.ArrayList<>();
        double matchThreshold = 2.0 / 3600.0; // 2 arcseconds in degrees
        
        // Track the closest match for each catalog source
        java.util.Map<Integer, CatalogEntry> closestBySource = new java.util.HashMap<>();
        java.util.Map<Integer, Double> distanceBySource = new java.util.HashMap<>();
        
        // Search through all secondary catalog entries
        for (CatalogEntry entry : secondaryCatalogData) {
            // Skip entries from the same catalog as the primary
            if (entry.source == primarySource) {
                continue;
            }
            
            // Calculate angular separation
            double deltaRa = (entry.ra - targetRa) * Math.cos(Math.toRadians(targetDec));
            double deltaDec = entry.dec - targetDec;
            double distance = Math.sqrt(deltaRa * deltaRa + deltaDec * deltaDec);
            
            // If within match threshold, check if it's the closest for this source
            if (distance <= matchThreshold) {
                if (!distanceBySource.containsKey(entry.source) || distance < distanceBySource.get(entry.source)) {
                    closestBySource.put(entry.source, entry);
                    distanceBySource.put(entry.source, distance);
                }
            }
        }
        
        // Add only the closest match from each catalog to the results
        for (java.util.Map.Entry<Integer, CatalogEntry> mapEntry : closestBySource.entrySet()) {
            CatalogEntry entry = mapEntry.getValue();
            double distance = distanceBySource.get(mapEntry.getKey());
            matches.add(entry);
            System.out.println("DEBUG: Found cross-match - source " + entry.source + 
                             " at RA=" + entry.ra + ", Dec=" + entry.dec + 
                             ", distance=" + (distance * 3600.0) + " arcsec (closest from this catalog)");
        }
        
        return matches;
    }
    
    // Helper method to query a catalog for a match near a position
    private void queryCatalogForMatch(String tapUrl, String catalog, String raCol, String decCol,
                                     String vmagCol, String evCol, String bvCol, String ebvCol,
                                     double targetRa, double targetDec, double searchRadius,
                                     int sourceNum, java.util.List<CatalogEntry> matches, double matchThreshold) {
        try {
            String adqlQuery = String.format(
                "SELECT TOP 1 %s, %s, %s, %s, %s, %s " +
                "FROM \"%s\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', %s, %s), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f))",
                raCol, decCol, vmagCol, evCol, bvCol, ebvCol,
                catalog,
                raCol, decCol,
                targetRa, targetDec, searchRadius
            );
            
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            URL url = new URL(queryUrl);
            Document document = this.getDocument(url);
            
            if (document != null) {
                NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
                if (tableDataNodes.getLength() > 0) {
                    Element tableDataElement = (Element) tableDataNodes.item(0);
                    NodeList rows = tableDataElement.getElementsByTagName("TR");
                    
                    if (rows.getLength() > 0) {
                        Element row = (Element) rows.item(0);
                        NodeList cells = row.getElementsByTagName("TD");
                        
                        if (cells.getLength() >= 6) {
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            
                            // Check if within match threshold
                            double deltaRa = (ra - targetRa) * Math.cos(Math.toRadians(targetDec));
                            double deltaDec = dec - targetDec;
                            double distance = Math.sqrt(deltaRa * deltaRa + deltaDec * deltaDec);
                            
                            if (distance <= matchThreshold) {
                                double vmag = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 99.999);
                                double ev = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 99.999);
                                double bv = this.parseDoubleOrDefault(cells.item(4).getTextContent().trim(), 99.999);
                                double ebv = this.parseDoubleOrDefault(cells.item(5).getTextContent().trim(), 99.999);
                                
                                String catalogName = catalog.split("/")[1];
                                matches.add(new CatalogEntry(catalogName, ra, dec, vmag, ev, bv, ebv, 99.999, 99.999, sourceNum, 0));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying " + catalog + " for match: " + e.getMessage());
        }
    }
    
    // Query Gaia DR2 for a match
    private void queryGaiaDR2ForMatch(String tapUrl, double targetRa, double targetDec, double searchRadius,
                                     java.util.List<CatalogEntry> matches, double matchThreshold) {
        try {
            String adqlQuery = String.format(
                "SELECT TOP 1 ra, dec, phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag " +
                "FROM \"I/345/gaia2\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', ra, dec), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f))",
                targetRa, targetDec, searchRadius
            );
            
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            URL url = new URL(queryUrl);
            Document document = this.getDocument(url);
            
            if (document != null) {
                NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
                if (tableDataNodes.getLength() > 0) {
                    Element tableDataElement = (Element) tableDataNodes.item(0);
                    NodeList rows = tableDataElement.getElementsByTagName("TR");
                    
                    if (rows.getLength() > 0) {
                        Element row = (Element) rows.item(0);
                        NodeList cells = row.getElementsByTagName("TD");
                        
                        if (cells.getLength() >= 5) {
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            
                            // Check if within match threshold
                            double deltaRa = (ra - targetRa) * Math.cos(Math.toRadians(targetDec));
                            double deltaDec = dec - targetDec;
                            double distance = Math.sqrt(deltaRa * deltaRa + deltaDec * deltaDec);
                            
                            if (distance <= matchThreshold) {
                                double gmag = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 99.999);
                                double bpmag = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 99.999);
                                double rpmag = this.parseDoubleOrDefault(cells.item(4).getTextContent().trim(), 99.999);
                                
                                // Apply Gaia transformations
                                double bp_rp = bpmag - rpmag;
                                double vmag = gmag - (-0.01760 - 0.006860 * bp_rp - 0.1732 * bp_rp * bp_rp);
                                double imag = gmag - (-0.02085 + 0.7419 * bp_rp - 0.09631 * bp_rp * bp_rp);
                                double vi = vmag - imag;
                                
                                matches.add(new CatalogEntry("Gaia DR2", ra, dec, vmag, 0.01, 99.999, 99.999, vi, 0.01, 48, 0));
                                System.out.println("DEBUG: Found Gaia match at distance=" + (distance*3600) + " arcsec, V=" + String.format("%.2f", vmag));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying Gaia DR2 for match: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Query PanSTARRS for a match
    private void queryPanstarrsForMatch(String tapUrl, double targetRa, double targetDec, double searchRadius,
                                       java.util.List<CatalogEntry> matches, double matchThreshold) {
        try {
            String adqlQuery = String.format(
                "SELECT TOP 1 RAJ2000, DEJ2000, gmag, rmag, imag " +
                "FROM \"II/349/ps1\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', RAJ2000, DEJ2000), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f))",
                targetRa, targetDec, searchRadius
            );
            
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            URL url = new URL(queryUrl);
            Document document = this.getDocument(url);
            
            if (document != null) {
                NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
                if (tableDataNodes.getLength() > 0) {
                    Element tableDataElement = (Element) tableDataNodes.item(0);
                    NodeList rows = tableDataElement.getElementsByTagName("TR");
                    
                    if (rows.getLength() > 0) {
                        Element row = (Element) rows.item(0);
                        NodeList cells = row.getElementsByTagName("TD");
                        
                        if (cells.getLength() >= 5) {
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            
                            // Check if within match threshold
                            double deltaRa = (ra - targetRa) * Math.cos(Math.toRadians(targetDec));
                            double deltaDec = dec - targetDec;
                            double distance = Math.sqrt(deltaRa * deltaRa + deltaDec * deltaDec);
                            
                            if (distance <= matchThreshold) {
                                double gmag = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 99.999);
                                double rmag = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 99.999);
                                double imag = this.parseDoubleOrDefault(cells.item(4).getTextContent().trim(), 99.999);
                                
                                // Apply PanSTARRS transformations
                                double g_r = gmag - rmag;
                                double r_i_raw = rmag - imag;
                                double vmag = gmag - 0.59 * g_r - 0.01;
                                double vr = 1.09 * r_i_raw + 0.22;
                                double ri = r_i_raw + 0.21;
                                double vi = vr + ri;
                                
                                matches.add(new CatalogEntry("PanSTARRS", ra, dec, vmag, 0.01, 99.999, 99.999, vi, 0.01, 46, 0));
                                System.out.println("DEBUG: Found PanSTARRS match at distance=" + (distance*3600) + " arcsec, V=" + String.format("%.2f", vmag));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying PanSTARRS for match: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Query Tycho-2 for a match
    private void queryTycho2ForMatch(String tapUrl, double targetRa, double targetDec, double searchRadius,
                                    java.util.List<CatalogEntry> matches, double matchThreshold) {
        try {
            String adqlQuery = String.format(
                "SELECT TOP 1 RAmdeg, DEmdeg, BTmag, VTmag " +
                "FROM \"I/259/tyc2\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', RAmdeg, DEmdeg), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f))",
                targetRa, targetDec, searchRadius
            );
            
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            URL url = new URL(queryUrl);
            Document document = this.getDocument(url);
            
            if (document != null) {
                NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
                if (tableDataNodes.getLength() > 0) {
                    Element tableDataElement = (Element) tableDataNodes.item(0);
                    NodeList rows = tableDataElement.getElementsByTagName("TR");
                    
                    if (rows.getLength() > 0) {
                        Element row = (Element) rows.item(0);
                        NodeList cells = row.getElementsByTagName("TD");
                        
                        if (cells.getLength() >= 4) {
                            double ra = Double.parseDouble(cells.item(0).getTextContent().trim());
                            double dec = Double.parseDouble(cells.item(1).getTextContent().trim());
                            
                            // Check if within match threshold
                            double deltaRa = (ra - targetRa) * Math.cos(Math.toRadians(targetDec));
                            double deltaDec = dec - targetDec;
                            double distance = Math.sqrt(deltaRa * deltaRa + deltaDec * deltaDec);
                            
                            if (distance <= matchThreshold) {
                                double btmag = this.parseDoubleOrDefault(cells.item(2).getTextContent().trim(), 99.999);
                                double vtmag = this.parseDoubleOrDefault(cells.item(3).getTextContent().trim(), 99.999);
                                
                                // Apply Tycho-2 transformations
                                double bt_vt = btmag - vtmag;
                                double vmag = vtmag + 0.008 - 0.0988 * bt_vt;
                                double bmag = btmag + 0.018 - 0.2580 * bt_vt;
                                double bv = bmag - vmag;
                                
                                matches.add(new CatalogEntry("Tycho-2", ra, dec, vmag, 0.01, bv, 0.01, 99.999, 99.999, 901, 0));
                                System.out.println("DEBUG: Found Tycho-2 match at distance=" + (distance*3600) + " arcsec, V=" + String.format("%.2f", vmag));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying Tycho-2 for match: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method to merge secondary catalog data into main arrays for display
    private void mergeSecondaryCatalogsIntoMainArrays() {
        try {
            int primaryCount = this.getTotalCount();
            int secondaryCount = secondaryCatalogData.size();
            int totalCount = primaryCount + secondaryCount;
            
            System.out.printf("DEBUG: Merging catalogs - Primary: %d, Secondary: %d, Total: %d\n", 
                primaryCount, secondaryCount, totalCount);
            
            if (secondaryCount == 0) {
                System.out.println("DEBUG: No secondary catalog data to merge");
                return;
            }
            
            // Create temporary arrays to hold merged data
            double[] mergedRa = new double[totalCount];
            double[] mergedDec = new double[totalCount];
            double[] mergedVmag = new double[totalCount];
            double[] mergedBMinusV = new double[totalCount];
            double[] mergedVMinusI = new double[totalCount];
            double[] mergedEv = new double[totalCount];
            double[] mergedEbv = new double[totalCount];
            double[] mergedEvi = new double[totalCount];
            int[] mergedSource = new int[totalCount];
            int[] mergedSeries = new int[totalCount];
            int[] mergedNobs = new int[totalCount];
            String[] mergedNames = new String[totalCount];
            
            // Copy primary catalog data
            for (int i = 0; i < primaryCount; i++) {
                mergedRa[i] = this.getRa(i);
                mergedDec[i] = this.getDec(i);
                mergedVmag[i] = this.getVmag(i);
                mergedBMinusV[i] = this.getBMinusV(i);
                mergedVMinusI[i] = this.getVMinusI(i);
                mergedEv[i] = this.getEv(i);
                mergedEbv[i] = this.getEbv(i);
                mergedEvi[i] = this.getEvi(i);
                mergedSource[i] = this.getSource(i);
                mergedSeries[i] = this.seriesValue[i];
                mergedNobs[i] = this.getNobs(i);
                mergedNames[i] = this.getName(i);
            }
            
            // Add secondary catalog data, filtering out duplicates (within 1 arcsec)
            double matchTolerance = 1.0 / 3600.0; // 1 arcsec in degrees
            int mergedIndex = primaryCount;
            int duplicatesSkipped = 0;
            
            for (CatalogEntry entry : secondaryCatalogData) {
                // Check if this secondary star matches any primary star
                boolean isDuplicate = false;
                for (int i = 0; i < primaryCount; i++) {
                    double raDiff = Math.abs(mergedRa[i] - entry.ra);
                    double decDiff = Math.abs(mergedDec[i] - entry.dec);
                    double separation = Math.sqrt(raDiff * raDiff + decDiff * decDiff);
                    
                    if (separation < matchTolerance) {
                        isDuplicate = true;
                        duplicatesSkipped++;
                        break;
                    }
                }
                
                if (!isDuplicate && mergedIndex < totalCount) {
                    mergedRa[mergedIndex] = entry.ra;
                    mergedDec[mergedIndex] = entry.dec;
                    mergedVmag[mergedIndex] = entry.vmag;
                    mergedBMinusV[mergedIndex] = entry.bMinusV;
                    mergedVMinusI[mergedIndex] = entry.vMinusI;
                    mergedEv[mergedIndex] = entry.ev;
                    mergedEbv[mergedIndex] = entry.ebv;
                    mergedEvi[mergedIndex] = entry.evi;
                    mergedSource[mergedIndex] = entry.source;
                    mergedNobs[mergedIndex] = entry.nobs;
                    
                    // Assign series (color) based on available color index
                    // Check which color index is valid (not 99.999 placeholder)
                    int seriesColor = 1; // Default to green (middle series)
                    
                    if (entry.bMinusV >= -1.0 && entry.bMinusV <= 3.0) {
                        // Valid B-V color index
                        // B-V thresholds: Blue <= 0.5, Green 0.5-1.1, Red >= 1.1
                        if (entry.bMinusV <= 0.5) {
                            seriesColor = 0; // Blue (hot stars)
                        } else if (entry.bMinusV < 1.1) {
                            seriesColor = 1; // Green (medium stars)
                        } else {
                            seriesColor = 2; // Red (cool stars)
                        }
                    } else if (entry.vMinusI >= -1.0 && entry.vMinusI <= 4.0) {
                        // Valid V-I color index (use when B-V not available)
                        // V-I thresholds: Blue <= 0.6, Green 0.6-1.4, Red >= 1.4
                        if (entry.vMinusI <= 0.6) {
                            seriesColor = 0; // Blue (hot stars)
                        } else if (entry.vMinusI < 1.4) {
                            seriesColor = 1; // Green (medium stars)
                        } else {
                            seriesColor = 2; // Red (cool stars)
                        }
                    } else if (entry.vmag < 90.0) {
                        // No valid color index - use magnitude-based split
                        // Roughly: faint = blue (hot), medium = green, bright = red (cool giants)
                        if (entry.vmag > 16.0) {
                            seriesColor = 0; // Blue - faint (likely hot MS stars)
                        } else if (entry.vmag > 13.0) {
                            seriesColor = 1; // Green - medium
                        } else {
                            seriesColor = 2; // Red - bright (likely cool giants)
                        }
                    }
                    
                    mergedSeries[mergedIndex] = seriesColor;
                    mergedNames[mergedIndex] = entry.name;
                    mergedIndex++;
                }
            }
            
            int actualTotalCount = mergedIndex;
            System.out.printf("DEBUG: Merge complete - Total: %d (%d duplicates removed)\n", 
                actualTotalCount, duplicatesSkipped);
            
            // Resize arrays and update main data structures
            this.setTotalCount(actualTotalCount);
            this.initializeArrays(5, actualTotalCount);
            
            // Copy merged data back to main arrays and calculate bounds simultaneously
            this.minRa = 360.0;
            this.maxRa = 0.0;
            this.minDec = 90.0;
            this.maxDec = -90.0;
            this.minZ = 100.0;  // Reset V magnitude bounds
            this.maxZ = -100.0;
            
            for (int i = 0; i < actualTotalCount; i++) {
                this.setRa(i, mergedRa[i]);
                this.setDec(i, mergedDec[i]);
                this.setVmag(i, mergedVmag[i]);
                this.setBMinusV(i, mergedBMinusV[i]);
                this.setVMinusI(i, mergedVMinusI[i]);
                this.setEv(i, mergedEv[i]);
                this.setEbv(i, mergedEbv[i]);
                this.setEvi(i, mergedEvi[i]);
                this.setSource(i, mergedSource[i]);
                this.seriesValue[i] = mergedSeries[i];
                this.setNobs(i, mergedNobs[i]);
                this.setName(i, mergedNames[i]);
                
                // Update bounds from merged data
                if (mergedRa[i] < this.minRa) this.minRa = mergedRa[i];
                if (mergedRa[i] > this.maxRa) this.maxRa = mergedRa[i];
                if (mergedDec[i] < this.minDec) this.minDec = mergedDec[i];
                if (mergedDec[i] > this.maxDec) this.maxDec = mergedDec[i];
                
                // Update V magnitude bounds
                if (mergedVmag[i] != 99.999 && !Double.isNaN(mergedVmag[i])) {
                    if (mergedVmag[i] < this.minZ) this.minZ = mergedVmag[i];
                    if (mergedVmag[i] > this.maxZ) this.maxZ = mergedVmag[i];
                }
            }
            
            System.out.printf("DEBUG: Recalculated bounds - RA: %.6f to %.6f, Dec: %.6f to %.6f\n",
                this.minRa, this.maxRa, this.minDec, this.maxDec);
            
            // Check series distribution
            int[] seriesCounts = new int[5];
            for (int i = 0; i < actualTotalCount; i++) {
                seriesCounts[this.seriesValue[i]]++;
            }
            System.out.printf("DEBUG: Series distribution after merge: [0]=%d, [1]=%d, [2]=%d, [3]=%d, [4]=%d\n",
                seriesCounts[0], seriesCounts[1], seriesCounts[2], seriesCounts[3], seriesCounts[4]);
            
            // Reprocess data for plotting
            System.out.println("DEBUG: Reprocessing merged catalog data...");
            this.findVariables();
            this.convertToTangentPlane();
            this.scaleDots();
            this.fireDatasetChanged();
            
        } catch (Exception e) {
            System.err.println("Error merging secondary catalogs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method to clear secondary catalog data
    public void clearSecondaryCatalogData() {
        secondaryCatalogData.clear();
    }
    
    public java.util.List<CatalogEntry> getSecondaryCatalogData() {
        return secondaryCatalogData;
    }
    
    // Inner class to store catalog entry data for cross-matching
    public static class CatalogEntry {
        public String name;
        public double ra;
        public double dec;
        public double vmag;
        public double ev;
        public double bMinusV;
        public double ebv;
        public double vMinusI;
        public double evi;
        public int source;
        public int nobs;
        
        public CatalogEntry(String name, double ra, double dec, double vmag, double ev, 
                           double bMinusV, double ebv, double vMinusI, double evi, int source, int nobs) {
            this.name = name;
            this.ra = ra;
            this.dec = dec;
            this.vmag = vmag;
            this.ev = ev;
            this.bMinusV = bMinusV;
            this.ebv = ebv;
            this.vMinusI = vMinusI;
            this.evi = evi;
            this.source = source;
            this.nobs = nobs;
        }
    }
    
    // Inner class to store VSP comparison star data
    public static class VSPCompStar {
        public String auid;
        public double ra;  // decimal degrees
        public double dec; // decimal degrees
        public String raStr;  // formatted string "hh:mm:ss.ss"
        public String decStr; // formatted string "±dd:mm:ss.s"
        public String label;
        public double vmag;
        public double vError;
        public double bmag;
        public double bError;
        public double umag;
        public double uError;
        public double rmag;
        public double rError;
        public double imag;
        public double iError;
        public double bMinusV;
        public double bvError;
        public double vMinusI;
        public double viError;
        public String comments;
        
        // Tangent plane coordinates for plotting
        public double x;
        public double y;
        
        public VSPCompStar(String auid, String raStr, String decStr, String label, String comments) {
            this.auid = auid;
            this.raStr = raStr;
            this.decStr = decStr;
            this.label = label;
            this.comments = comments;
            
            // Parse RA/Dec strings to decimal degrees
            this.ra = parseRAToDecimal(raStr);
            this.dec = parseDecToDecimal(decStr);
            
            // Initialize magnitudes to sentinel values
            this.vmag = 99.999;
            this.vError = 0.0;
            this.bmag = 99.999;
            this.bError = 0.0;
            this.umag = 99.999;
            this.uError = 0.0;
            this.rmag = 99.999;
            this.rError = 0.0;
            this.imag = 99.999;
            this.iError = 0.0;
            this.bMinusV = 99.999;
            this.bvError = 0.0;
            this.vMinusI = 99.999;
            this.viError = 0.0;
        }
        
        private double parseRAToDecimal(String raStr) {
            try {
                // Format: "hh:mm:ss.ss"
                String[] parts = raStr.split(":");
                double hours = Double.parseDouble(parts[0]);
                double minutes = Double.parseDouble(parts[1]);
                double seconds = Double.parseDouble(parts[2]);
                return (hours + minutes/60.0 + seconds/3600.0) * 15.0; // Convert to degrees
            } catch (Exception e) {
                System.err.println("Error parsing RA: " + raStr);
                return 0.0;
            }
        }
        
        private double parseDecToDecimal(String decStr) {
            try {
                // Format: "±dd:mm:ss.s"
                boolean negative = decStr.startsWith("-");
                String cleanStr = decStr.replaceAll("[+\\-]", "");
                String[] parts = cleanStr.split(":");
                double degrees = Double.parseDouble(parts[0]);
                double minutes = Double.parseDouble(parts[1]);
                double seconds = Double.parseDouble(parts[2]);
                double result = degrees + minutes/60.0 + seconds/3600.0;
                return negative ? -result : result;
            } catch (Exception e) {
                System.err.println("Error parsing Dec: " + decStr);
                return 0.0;
            }
        }
        
        public void calculateColorIndices() {
            if (bmag < 99.0 && vmag < 99.0) {
                bMinusV = bmag - vmag;
                bvError = Math.sqrt(bError*bError + vError*vError);
            }
            if (vmag < 99.0 && imag < 99.0) {
                vMinusI = vmag - imag;
                viError = Math.sqrt(vError*vError + iError*iError);
            }
        }
    }
    
    // Storage for VSP comparison stars
    private java.util.List<VSPCompStar> vspCompStars = new ArrayList<>();
    private String vspChartId = null;
    
    public java.util.List<VSPCompStar> getVSPCompStars() {
        return vspCompStars;
    }
    
    public String getVSPChartId() {
        return vspChartId;
    }
}


/*
 * DataConnector.java - Enhanced Data Connection and Coordinate Management
 * Part of Seqplot 6.0.0 - Enhanced Astronomical Sequence Plotting Application
 * Version 6.0.0 - Released November 3, 2025
 * 
 * Enhanced with improved coordinate formatting and modern display conventions
 */
package AAVSOtools;

import AAVSOtools.EnterStar;
import AAVSOtools.ProgressWindow;
import AAVSOtools.Seqplot;
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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JOptionPane;
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
    private static final String USER_PREFERENCES_FILE = "seqplotDefault.ini";
    private static final String BASE_URL = "https://www.aavso.org/";
    public URL calibUrl;
    private int itemCount;
    private int seriesCount;
    private int totalCount;
    private int numberOfVars;
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
    private Boolean k35BoxSelected;
    private Boolean w28BoxSelected;
    private Boolean w30BoxSelected;
    private Boolean oc61BoxSelected;
    private Boolean gcpdBoxSelected;
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
    private String plotType;
    private String catalogString;
    private String raText = "";
    private String decText = "";
    private String oldRAText = "";
    private String oldDecText = "";
    private String username = "";
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
    private ProgressWindow pw;

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
                JOptionPane.showMessageDialog(this.frame, "Maximum number of records allowed has been exceeded! \nOnly the first 5000 records found will be displayed.", "Warning!", 2);
            } else {
                this.seqplot.setMainTitleText(String.valueOf(this.getStar()) + "  RA: " + this.getFormattedRA() + "  Dec: " + this.getFormattedDec() + "  FoV: " + Math.round(this.getFieldSize() * 60.0) + " arcmin");
                this.seqplot.setSubtitleText("Data from the Calibration Database - limiting magnitude " + this.getLimitingMag() + " - VSX position matching tolerance " + this.getPositionTolerance() + " degrees");
            }
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
            if (!vsx_data.isEmpty()) {
                this.setCentralRA(Double.parseDouble((String)vsx_data.get("RA2000")));
                this.setCentralDec(Double.parseDouble((String)vsx_data.get("Declination2000")));
                if (this.getFieldSize() == 0.0) {
                    this.setFieldSize(1.0);
                }
                if (this.getLimitingMag() == 0.0) {
                    this.setLimitingMag(20.0);
                    this.setUpperLimitingMag(-5.0);
                }
            } else {
                JOptionPane.showMessageDialog(null, String.valueOf(userStarInput) + " cannot be found.\n" + "Please either type in another star name\n" + "or enter the RA and Dec for this field.", "Star not found!", 2);
                this.setBlankCoord(true);
            }
        }
        catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(null, "MalformedURLException: " + e.getMessage(), "Warning", 0);
        }
        catch (UnsupportedEncodingException e) {
            JOptionPane.showMessageDialog(null, "UnsupportedEncodingException: " + e.getMessage(), "Warning", 0);
        }
    }

    public void getData() {
        this.pw = new ProgressWindow();
        
        // Handle external data sources
        if (this.getApass9BoxSelected().booleanValue()) {
            this.getExternalData();
            return;
        }
        
        try {
            try {
                this.calibUrl = new URL(String.valueOf(this.getBaseURL()) + "vsx/index.php?view=api.calib" + "&fromra=" + URLEncoder.encode(String.valueOf(String.format("%.6f", this.getLowerRA())).trim(), "UTF-8") + "&tora=" + URLEncoder.encode(String.valueOf(String.format("%.6f", this.getUpperRA())).trim(), "UTF-8") + "&fromdec=" + URLEncoder.encode(String.valueOf(String.format("%.6f", this.getLowerDec())).trim(), "UTF-8") + "&todec=" + URLEncoder.encode(String.valueOf(String.format("%.6f", this.getUpperDec())).trim(), "UTF-8") + "&tomag=" + URLEncoder.encode(String.valueOf(this.getLimitingMag()).trim(), "UTF-8") + "&source=" + this.getCatalogString().trim() + "&limit=" + 5000);
                NodeList dataObjectNodes = this.getDocument(this.calibUrl).getElementsByTagName("Object");
                this.setTotalCount(dataObjectNodes.getLength());
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
                                this.setNobs(i, Integer.parseInt(nodeValue));
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
                                this.setSource(i, Integer.parseInt(nodeValue));
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
                    this.pw.close();
                } else {
                    this.pw.close();
                    JOptionPane.showMessageDialog(null, "Data in this range cannot be found.\nPlease type in another star name, add catalogs, or change the field size\nor limiting magnitude for this field to get data.", "Data not found!", 2);
                }
            }
            catch (MalformedURLException e) {
                JOptionPane.showMessageDialog(null, "MalformedURLException: " + e.getMessage(), "Warning", 0);
                this.pw.close();
            }
            catch (UnsupportedEncodingException e) {
                JOptionPane.showMessageDialog(null, "UnsupportedEncodingException: " + e.getMessage(), "Warning", 0);
                this.pw.close();
            }
        }
        finally {
            this.pw.close();
        }
    }

    public void getExternalData() {
        try {
            // Query CDS Vizier TAP service for APASS DR9 data
            this.getApass9Data();
        } catch (Exception e) {
            this.pw.close();
            JOptionPane.showMessageDialog(null, "Error accessing external data source: " + e.getMessage(), "Error", 0);
        }
    }

    public void getApass9Data() {
        try {
            // Construct ADQL query for APASS DR9 catalog via CDS Vizier TAP
            String tapUrl = "http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync";
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
                this.getFieldSize(), this.getLimitingMag()
            );

            // Build the TAP query URL
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");

            System.out.println("Querying APASS9 via CDS Vizier TAP: " + queryUrl);
            
            // DEBUG: Print query parameters
            System.out.printf("DEBUG APASS9 Query Parameters:\n");
            System.out.printf("  Central RA: %.6f degrees\n", this.getCentralRA());
            System.out.printf("  Central Dec: %.6f degrees\n", this.getCentralDec());
            System.out.printf("  Field Size: %.1f degrees (%.1f arcmin)\n", this.getFieldSize(), this.getFieldSize() * 60.0);
            System.out.printf("  Limiting Mag: %.1f\n", this.getLimitingMag());
            System.out.printf("  Search radius: %.6f degrees\n", this.getFieldSize());
            System.out.println();

            URL url = new URL(queryUrl);
            Document document = this.getDocument(url);
            
            // DEBUG: Check if we got a valid response
            System.out.println("DEBUG: Checking VOTable response...");
            if (document == null) {
                System.out.println("DEBUG: Document is NULL!");
                return;
            }
            
            // Parse VOTable response
            this.parseApass9VOTable(document);
            
            this.pw.close();
            
            System.out.printf("DEBUG APASS9 Results: Found %d stars\n", this.getTotalCount());
            
            if (this.getTotalCount() == 0) {
                JOptionPane.showMessageDialog(null, 
                    "No APASS9 data found in this field.\nTry increasing the field size or limiting magnitude.", 
                    "No Data Found", 1);
            } else {
                // Process the data for plotting
                this.processApass9Data();
                System.out.printf("DEBUG: Processed %d APASS9 stars for plotting\n", this.getTotalCount());
            }
            
        } catch (MalformedURLException e) {
            this.pw.close();
            JOptionPane.showMessageDialog(null, "Invalid URL for APASS9 query: " + e.getMessage(), "Error", 0);
        } catch (UnsupportedEncodingException e) {
            this.pw.close();
            JOptionPane.showMessageDialog(null, "URL encoding error: " + e.getMessage(), "Error", 0);
        } catch (Exception e) {
            this.pw.close();
            JOptionPane.showMessageDialog(null, "Error querying APASS9 data: " + e.getMessage(), "Error", 0);
        }
    }

    public void parseApass9VOTable(Document document) {
        try {
            // Parse VOTable format from CDS Vizier - look for TABLEDATA section
            NodeList tableDataNodes = document.getElementsByTagName("TABLEDATA");
            
            System.out.printf("DEBUG: Found %d TABLEDATA elements\n", tableDataNodes.getLength());
            
            if (tableDataNodes.getLength() == 0) {
                System.out.println("DEBUG: No TABLEDATA section found in VOTable");
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
                            this.setSource(recordIndex, 999); // Special source number for APASS9
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

    public void findVariables() {
        try {
            URL vsxUrl = new URL(String.valueOf(this.getBaseURL()) + "vsx/index.php?view=api.list&fromra=" + String.format("%.6f", this.getLowerRA()) + "&tora=" + String.format("%.6f", this.getUpperRA()) + "&fromdec=" + String.format("%.6f", this.getLowerDec()) + "&todec=" + String.format("%.6f", this.getUpperDec()));
            NodeList objNodes = this.getDocument(vsxUrl).getElementsByTagName("VSXObject");
            this.numberOfVars = objNodes.getLength();
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
            JOptionPane.showMessageDialog(null, "MalformedURLException: " + e.getMessage(), "Warning", 0);
        }
    }

    public double[] RaDectoXY(double radeg, double decdeg) {
        double raOffset;
        double picon = Math.PI / 180;
        double racent = picon * ((this.minRa + this.maxRa) / 2.0);
        double deccent = picon * ((this.minDec + this.maxDec) / 2.0);
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
        System.out.printf("DEBUG: RA range: %.6f to %.6f\n", this.minRa, this.maxRa);
        System.out.printf("DEBUG: Dec range: %.6f to %.6f\n", this.minDec, this.maxDec);
        
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
        int i = 0;
        while (i < this.numberOfVars) {
            if (Math.abs(this.rVar[i] - r) <= this.getPositionTolerance() && Math.abs(this.dVar[i] - d) <= this.getPositionTolerance()) {
                this.setVarName(record, this.varName[i]);
                this.setVarRa(record, this.rVar[i]);
                this.setVarDec(record, this.dVar[i]);
                this.setVarType(record, this.varType[i]);
                this.setVarMax(record, this.varMax[i]);
                this.setVarMin(record, this.varMin[i]);
                return 3;
            }
            ++i;
        }
        int ret = 4;
        if (color <= 0.5) {
            ret = 0;
        } else if (color > 0.5 && color < 1.1) {
            ret = 1;
        } else if (color >= 1.1 && color < 9.9) {
            ret = 2;
        }
        if (vmag < ulmag) {
            ret = 4;
        }
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
            JOptionPane.showMessageDialog(null, "IOException: " + e.getMessage(), "Warning", 0);
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
                this.setPathToLogfile(br.readLine());
                this.setTychoBoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setNofsBoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setSonoitaBoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setBsmBoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setBsm_SBoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setSro50BoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setApassBoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setApass9BoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setK35BoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setW28BoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setW30BoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setOc61BoxSelected(Boolean.parseBoolean(br.readLine()));
                this.seqplot.setDotsizeScaleFactor(Double.parseDouble(br.readLine()));
                this.seqplot.setRelativeDotsizeScaleFactor(Double.parseDouble(br.readLine()));
                this.setLimitingMag(Double.parseDouble(br.readLine()));
                this.setFieldSize(Double.parseDouble(br.readLine()));
                this.setGcpdBoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setUsername(br.readLine());
                this.setTmo61BoxSelected(Boolean.parseBoolean(br.readLine()));
                this.setCoker30BoxSelected(Boolean.parseBoolean(br.readLine()));
                this.seqplot.setFontSize(Integer.parseInt(br.readLine()));
                this.setPositionTolerance(Double.parseDouble(br.readLine()));
                br.close();
            } else {
                this.seqplot.setDefaultSeqplotColors();
                this.setPathToLogfile(this.getDefaultPathToFile());
                this.setTychoBoxSelected(true);
                this.setNofsBoxSelected(true);
                this.setSonoitaBoxSelected(true);
                this.setBsmBoxSelected(true);
                this.setBsm_SBoxSelected(true);
                this.setSro50BoxSelected(true);
                this.setApassBoxSelected(true);
                this.setApass9BoxSelected(false);
                this.setK35BoxSelected(true);
                this.setW28BoxSelected(true);
                this.setW30BoxSelected(true);
                this.setOc61BoxSelected(true);
                this.seqplot.setDotsizeScaleFactor(this.seqplot.getDotsizeScaleFactor());
                this.seqplot.setRelativeDotsizeScaleFactor(this.seqplot.getRelativeDotsizeScaleFactor());
                this.setUpperLimitingMag(-5.0);
                this.setLimitingMag(20.0);
                this.setFieldSize(1.0);
                this.setGcpdBoxSelected(true);
                this.setUsername(DEFAULT_STAR);
                this.setTmo61BoxSelected(true);
                this.setCoker30BoxSelected(true);
                this.seqplot.setFontSize(16);
                this.setPositionTolerance(0.003);
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
            pw.println(this.getK35BoxSelected());
            pw.println(this.getW28BoxSelected());
            pw.println(this.getW30BoxSelected());
            pw.println(this.getOc61BoxSelected());
            pw.println(this.seqplot.getDotsizeScaleFactor());
            pw.println(this.seqplot.getRelativeDotsizeScaleFactor());
            pw.println(this.getLimitingMag());
            pw.println(this.getFieldSize());
            pw.println(this.getGcpdBoxSelected());
            pw.println(this.getUsername());
            pw.println(this.getTmo61BoxSelected());
            pw.println(this.getCoker30BoxSelected());
            pw.println(this.seqplot.getFontSize());
            pw.println(this.getPositionTolerance());
            pw.close();
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "IOException: " + e.getMessage(), "Warning", 0);
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

    public String getStar() {
        return this.star;
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
}

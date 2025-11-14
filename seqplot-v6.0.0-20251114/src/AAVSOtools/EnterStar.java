/*
 * Decompiled with CFR 0.152.
 */
package AAVSOtools;

import AAVSOtools.DataConnector;
import AAVSOtools.GBC;
import AAVSOtools.Seqplot;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EnterStar
extends JDialog
implements ActionListener,
FocusListener {
    private static final int WIDTH = 600;  // Increased by 20% (500 * 1.2 = 600)
    private static final int HEIGHT = 825;  // Increased by 10% (750 * 1.1 = 825)
    private String starnameText = "Star Name: ";
    private String raText = "RA: ";
    private String decText = "Dec: ";
    private String sizeText = "Field Size (arcmin): ";
    private String limitingMagText = "Limiting Mag: ";
    private String chartSizeText = "Chart Size: ";
    private String okText = "Find RA & Dec for Star";
    private String ok2Text = "Get Plot";
    private String quitText = "Quit";
    private String closeText = "Cancel";
    private JTextField starnameField = new JTextField();
    private JTextField raField = new JTextField();
    private JTextField decField = new JTextField();
    private JTextField sizeField = new JTextField();
    private JTextField limitingMagField = new JTextField();
    private JComboBox<String> chartSizeCombo;
    private JButton getRaDecButton;
    private JButton getPlotButton;
    private JButton quitButton;
    private JButton closeButton;
    private JCheckBox tychoBox = new JCheckBox();
    private JCheckBox nofsBox = new JCheckBox();
    private JCheckBox sonoitaBox = new JCheckBox();
    private JCheckBox bsmBox = new JCheckBox();
    private JCheckBox bsm_SBox = new JCheckBox();
    private JCheckBox coker30Box = new JCheckBox();
    private JCheckBox sro50Box = new JCheckBox();
    private JCheckBox tmo61Box = new JCheckBox();
    private JCheckBox apassBox = new JCheckBox();
    private JCheckBox k35Box = new JCheckBox();
    private JCheckBox w28Box = new JCheckBox();
    private JCheckBox w30Box = new JCheckBox();
    private JCheckBox oc61Box = new JCheckBox();
    private JCheckBox gcpdBox = new JCheckBox();
    private JCheckBox apass9Box = new JCheckBox();
    private JCheckBox gaiaDR2Box = new JCheckBox();
    private JCheckBox gaiaDR3Box = new JCheckBox();
    private JCheckBox panstarrsBox = new JCheckBox();
    private JCheckBox sdssBox = new JCheckBox();
    // API status indicators
    private JLabel apass9StatusLabel = new JLabel("●");
    private JLabel gaiaDR2StatusLabel = new JLabel("●");
    private JLabel panstarrsStatusLabel = new JLabel("●");
    private JLabel simbadStatusLabel = new JLabel("●");
    private JLabel aavsoStatusLabel = new JLabel("●");
    private JLabel vizierStatusLabel = new JLabel("●");
    // AUID display components
    private JLabel auidStatusLabel = new JLabel("✗");
    private JLabel auidValueLabel = new JLabel("");
    private JButton requestAuidButton = new JButton("Request AUID");
    private JButton checkVsxButton = new JButton("Check VSX");
    // Existing sequence check components
    private JLabel seqStatusLabel = new JLabel("");
    private JButton checkSeqButton = new JButton("Check");
    // Mag range display (from VSX data)
    private JLabel magRangeLabel = new JLabel("");
    // Order labels for all catalogs
    private JLabel tychoOrderLabel = new JLabel("   ");
    private JLabel nofsOrderLabel = new JLabel("   ");
    private JLabel sonoitaOrderLabel = new JLabel("   ");
    private JLabel bsmOrderLabel = new JLabel("   ");
    private JLabel bsm_SOrderLabel = new JLabel("   ");
    private JLabel coker30OrderLabel = new JLabel("   ");
    private JLabel sro50OrderLabel = new JLabel("   ");
    private JLabel tmo61OrderLabel = new JLabel("   ");
    private JLabel apassOrderLabel = new JLabel("   ");
    private JLabel k35OrderLabel = new JLabel("   ");
    private JLabel w28OrderLabel = new JLabel("   ");
    private JLabel w30OrderLabel = new JLabel("   ");
    private JLabel oc61OrderLabel = new JLabel("   ");
    private JLabel gcpdOrderLabel = new JLabel("   ");
    private JLabel apass9OrderLabel = new JLabel("   ");
    private JLabel gaiaDR2OrderLabel = new JLabel("   ");
    private JLabel gaiaDR3OrderLabel = new JLabel("   ");
    private JLabel panstarrsOrderLabel = new JLabel("   ");
    private JLabel sdssOrderLabel = new JLabel("   ");
    private int catalogSelectionOrder = 0;
    private Frame frame;
    private DataConnector db;
    private Seqplot seqplot;
    private JPanel glassPane;
    private javax.swing.Timer loadingTimer;
    private long loadingStartTime;
    private static final String DEFAULT_CHART_SIZE = "A (15 deg, <9 mag)";
    private static final String[] CHART_SIZE_OPTIONS = new String[]{
        "A (15 deg, <9 mag)",
        "B (3 deg, <11 mag)",
        "C (2 deg, <12 mag)",
        "D (1 deg, <13 mag)",
        "E (30 min, <14 mag)",
        "F (15 min, <15 mag)",
        "G (7.5 min, <16 mag)"
    };
    private static final Map<String, ChartSizePreset> CHART_SIZE_PRESETS = createChartSizePresetMap();
    private static final Map<String, String> CONSTELLATION_ABBREVIATIONS = createConstellationMap();

    public EnterStar(DataConnector dcon, Seqplot sp, Frame frame, String defaultStar) {
        this(dcon, sp, frame, "Sequence Plotter" + sp.getVersion() + " - Request Star", true, defaultStar);
    }

    public EnterStar(DataConnector dcon, Seqplot sp, Frame frame, String title, Boolean mode, String defaultStar) {
        super(frame, "Sequence Plotter" + sp.getVersion() + " - Request Star", true);
        
        // Set application icon
        try {
            java.net.URL iconURL = getClass().getResource("/AAVSOtools/seqplot_icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                this.setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load application icon: " + e.getMessage());
        }
        
        this.db = dcon;
        this.seqplot = sp;
    this.setSize(WIDTH, HEIGHT);
        this.setModal(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int)(dim.getWidth() / 3.0), (int)(dim.getHeight() / 5.0));
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem homepageItem = new JMenuItem("Sequence Team Homepage");
        homepageItem.addActionListener(e -> openSequenceTeamHomepage());
        helpMenu.add(homepageItem);
        menuBar.add(helpMenu);
        
        // Tools menu
        JMenu toolsMenu = new JMenu("Tools");
        
        JMenuItem mirrorItem = new JMenuItem("Choose CDS Mirror...");
        mirrorItem.addActionListener(e -> openCDSMirrorSelector());
        toolsMenu.add(mirrorItem);
        
        toolsMenu.addSeparator();
        
        JMenuItem updateListItem = new JMenuItem("View AAVSO Sequence Team Update List");
        updateListItem.addActionListener(e -> openAAVSOUpdateList());
        toolsMenu.add(updateListItem);
        
        JMenuItem timeoutItem = new JMenuItem("Set Catalog Query Timeout...");
        timeoutItem.addActionListener(e -> configureCatalogTimeout());
        toolsMenu.add(timeoutItem);
        
        menuBar.add(toolsMenu);
        this.setJMenuBar(menuBar);
        
        JPanel catalogPane = new JPanel(new GridBagLayout());
        JPanel externalPane = new JPanel(new GridBagLayout());
        JPanel coordPane = new JPanel(new GridBagLayout());
        JPanel starnamePane = new JPanel(new GridBagLayout());
        JPanel buttonPane = new JPanel(new GridBagLayout());
        JLabel catalogLabel = new JLabel("Choose a catalog or catalogs (the \"source number\" is given in parentheses):");
        this.setupCheckBox(this.tychoBox, "Tycho-2 (1)", this.db.getTychoBoxSelected(), "Tycho-2 Reference Catalogue");
        this.setupCheckBox(this.nofsBox, "NOFS (10)", this.db.getNofsBoxSelected(), "CCD Photometry by Arne Henden 1.0 meter USNO telescope");
        this.setupCheckBox(this.sonoitaBox, "SRO35 (18)", this.db.getSonoitaBoxSelected(), "Sonoita Research Observatory - 35cm");
        this.setupCheckBox(this.bsmBox, "BSM - North (28)", this.db.getBsmBoxSelected(), "Bright Star Monitor - New Mexico");
        this.setupCheckBox(this.bsm_SBox, "BSM - South (37)", this.db.getBsm_SBoxSelected(), "Bright Star Monitor - South");
        this.setupCheckBox(this.coker30Box, "COKER30 (44)", this.db.getCoker30BoxSelected(), "Coker 30cm telescope");
        this.setupCheckBox(this.sro50Box, "SRO50 (33)", this.db.getSro50BoxSelected(), "Sonoita Research Observatory - 50cm");
        this.setupCheckBox(this.tmo61Box, "TMO61 (42)", this.db.getTmo61BoxSelected(), "Tortugas Research Observatory - 61cm");
        this.setupCheckBox(this.apassBox, "APASS (29)", this.db.getApassBoxSelected(), "AAVSO Photometric All-Sky Survey");
        this.setupCheckBox(this.k35Box, "K35 (34)", this.db.getK35BoxSelected(), "Astrokolkhoz Observatory Krajci 35cm");
        this.setupCheckBox(this.w28Box, "W28 (30)", this.db.getW28BoxSelected(), "Astrokolkhoz Observatory Wright 28cm");
        this.setupCheckBox(this.w30Box, "W30 (31)", this.db.getW30BoxSelected(), "Astrokolkhoz Observatory Wright 30cm");
        this.setupCheckBox(this.oc61Box, "OC61 (32)", this.db.getOc61BoxSelected(), "Mt. John University Obs. 61cm Optical Craftsman");
        this.setupCheckBox(this.gcpdBox, "GCPD (20)", this.db.getGcpdBoxSelected(), "General Catalogue of Photometric Data");
        
        // External Data Sources
        JLabel externalLabel = new JLabel("External Data Sources:");
        this.setupCheckBox(this.apass9Box, "APASS (29)", this.db.getApass9BoxSelected(), "AAVSO Photometric All-Sky Survey");
        this.setupCheckBox(this.gaiaDR2Box, "Gaia DR2 (48)", this.db.getGaiaDR2BoxSelected(), "Gaia Data Release 2");
        this.setupCheckBox(this.gaiaDR3Box, "Gaia DR3 (49)", this.db.getGaiaDR3BoxSelected(), "Gaia Data Release 3");
        this.setupCheckBox(this.panstarrsBox, "PanSTARRS DR1 (46)", this.db.getPanstarrsBoxSelected(), "PanSTARRS Data Release 1");
        this.setupCheckBox(this.sdssBox, "SDSS-DR12 (21)", this.db.getSdssBoxSelected(), "SDSS Data Release 12");
        
        // Add listeners to track selection order for all catalogs
        this.tychoBox.addItemListener(e -> updateCatalogSelectionOrder(tychoBox, tychoOrderLabel));
        this.nofsBox.addItemListener(e -> updateCatalogSelectionOrder(nofsBox, nofsOrderLabel));
        this.sonoitaBox.addItemListener(e -> updateCatalogSelectionOrder(sonoitaBox, sonoitaOrderLabel));
        this.bsmBox.addItemListener(e -> updateCatalogSelectionOrder(bsmBox, bsmOrderLabel));
        this.bsm_SBox.addItemListener(e -> updateCatalogSelectionOrder(bsm_SBox, bsm_SOrderLabel));
        this.coker30Box.addItemListener(e -> updateCatalogSelectionOrder(coker30Box, coker30OrderLabel));
        this.sro50Box.addItemListener(e -> updateCatalogSelectionOrder(sro50Box, sro50OrderLabel));
        this.tmo61Box.addItemListener(e -> updateCatalogSelectionOrder(tmo61Box, tmo61OrderLabel));
        this.apassBox.addItemListener(e -> updateCatalogSelectionOrder(apassBox, apassOrderLabel));
        this.k35Box.addItemListener(e -> updateCatalogSelectionOrder(k35Box, k35OrderLabel));
        this.w28Box.addItemListener(e -> updateCatalogSelectionOrder(w28Box, w28OrderLabel));
        this.w30Box.addItemListener(e -> updateCatalogSelectionOrder(w30Box, w30OrderLabel));
        this.oc61Box.addItemListener(e -> updateCatalogSelectionOrder(oc61Box, oc61OrderLabel));
        this.gcpdBox.addItemListener(e -> updateCatalogSelectionOrder(gcpdBox, gcpdOrderLabel));
        this.apass9Box.addItemListener(e -> updateCatalogSelectionOrder(apass9Box, apass9OrderLabel));
        this.gaiaDR2Box.addItemListener(e -> {
            updateCatalogSelectionOrder(gaiaDR2Box, gaiaDR2OrderLabel);
            // When Gaia DR2 is selected, automatically set to Chart Size F and limiting mag 19
            if (gaiaDR2Box.isSelected()) {
                this.chartSizeCombo.setSelectedItem("F (15 min, <15 mag)");
                this.limitingMagField.setText("19");
            }
        });
        this.gaiaDR3Box.addItemListener(e -> {
            updateCatalogSelectionOrder(gaiaDR3Box, gaiaDR3OrderLabel);
            // When Gaia DR3 is selected, automatically set to Chart Size F and limiting mag 19
            if (gaiaDR3Box.isSelected()) {
                this.chartSizeCombo.setSelectedItem("F (15 min, <15 mag)");
                this.limitingMagField.setText("19");
            }
        });
        this.panstarrsBox.addItemListener(e -> updateCatalogSelectionOrder(panstarrsBox, panstarrsOrderLabel));
        this.sdssBox.addItemListener(e -> updateCatalogSelectionOrder(sdssBox, sdssOrderLabel));
        
        catalogPane.add((Component)catalogLabel, new GBC(0, 0, 4, 1).setWeight(100.0, 0.0).setFill(2).setInsets(10, 5, 0, 5));
        // Left column with order labels
        catalogPane.add((Component)this.apassBox, new GBC(0, 2, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.apassOrderLabel, new GBC(1, 2, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.bsmBox, new GBC(0, 3, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.bsmOrderLabel, new GBC(1, 3, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.bsm_SBox, new GBC(0, 4, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.bsm_SOrderLabel, new GBC(1, 4, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.coker30Box, new GBC(0, 5, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.coker30OrderLabel, new GBC(1, 5, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.gcpdBox, new GBC(0, 6, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.gcpdOrderLabel, new GBC(1, 6, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.nofsBox, new GBC(0, 7, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.nofsOrderLabel, new GBC(1, 7, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.k35Box, new GBC(0, 8, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 10, 2));
        catalogPane.add((Component)this.k35OrderLabel, new GBC(1, 8, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 10, 5));
        // Right column with order labels
        catalogPane.add((Component)this.oc61Box, new GBC(2, 2, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.oc61OrderLabel, new GBC(3, 2, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.sonoitaBox, new GBC(2, 3, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.sonoitaOrderLabel, new GBC(3, 3, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.sro50Box, new GBC(2, 4, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.sro50OrderLabel, new GBC(3, 4, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.tmo61Box, new GBC(2, 5, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.tmo61OrderLabel, new GBC(3, 5, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.tychoBox, new GBC(2, 6, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.tychoOrderLabel, new GBC(3, 6, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.w28Box, new GBC(2, 7, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 0, 2));
        catalogPane.add((Component)this.w28OrderLabel, new GBC(3, 7, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 0, 5));
        catalogPane.add((Component)this.w30Box, new GBC(2, 8, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 10, 2));
        catalogPane.add((Component)this.w30OrderLabel, new GBC(3, 8, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 10, 5));
        
        // External data sources panel - two columns with order labels
        externalPane.add((Component)externalLabel, new GBC(0, 0, 4, 1).setWeight(100.0, 0.0).setFill(2).setInsets(10, 5, 0, 5));
        // Row 1: APASS, PanSTARRS
        externalPane.add((Component)this.apass9Box, new GBC(0, 1, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 5, 2));
        externalPane.add((Component)this.apass9OrderLabel, new GBC(1, 1, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 5, 5));
        externalPane.add((Component)this.panstarrsBox, new GBC(2, 1, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 5, 2));
        externalPane.add((Component)this.panstarrsOrderLabel, new GBC(3, 1, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 5, 5));
        // Row 2: Gaia DR2, Gaia DR3
        externalPane.add((Component)this.gaiaDR2Box, new GBC(0, 2, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 5, 2));
        externalPane.add((Component)this.gaiaDR2OrderLabel, new GBC(1, 2, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 5, 5));
        externalPane.add((Component)this.gaiaDR3Box, new GBC(2, 2, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 5, 2));
        externalPane.add((Component)this.gaiaDR3OrderLabel, new GBC(3, 2, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 5, 5));
        // Row 3: SDSS-DR12
        externalPane.add((Component)this.sdssBox, new GBC(0, 3, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(5, 5, 10, 2));
        externalPane.add((Component)this.sdssOrderLabel, new GBC(1, 3, 1, 1).setWeight(0.0, 0.0).setFill(0).setInsets(5, 2, 10, 5));
        
        JLabel starnameLabel = new JLabel(this.starnameText);
        this.setupField(this.starnameField, defaultStar, 15);
        this.getRaDecButton = new JButton(this.okText);
        this.getRaDecButton.addActionListener(this);
        starnamePane.add((Component)starnameLabel, new GBC(0, 0, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        starnamePane.add((Component)this.starnameField, new GBC(1, 0, 2, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        starnamePane.add((Component)this.getRaDecButton, new GBC(1, 1, 2, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        JLabel raLabel = new JLabel(this.raText);
        this.setupField(this.raField, this.db.getRAText(), 25);
        JLabel decLabel = new JLabel(this.decText);
        this.setupField(this.decField, this.db.getDecText(), 25);
        JLabel sizeLabel = new JLabel(this.sizeText);
        this.setupField(this.sizeField, "" + Math.round(this.db.getFieldSize() * 60.0), 25);
        JLabel limitingMagLabel = new JLabel(this.limitingMagText);
        this.setupField(this.limitingMagField, "" + this.db.getLimitingMag(), 25);
        JLabel chartSizeLabel = new JLabel(this.chartSizeText);
        this.chartSizeCombo = new JComboBox<>(CHART_SIZE_OPTIONS);
        this.chartSizeCombo.setToolTipText("AAVSO suggestions for field of view & magnitude pairings");
        this.chartSizeCombo.addActionListener(e -> {
            String selection = (String)this.chartSizeCombo.getSelectedItem();
            if (selection != null) {
                applyChartSizePreset(selection);
            }
        });
        String savedChartSize = this.db.getChartSizeSelection();
        if (savedChartSize == null || savedChartSize.trim().isEmpty() || !CHART_SIZE_PRESETS.containsKey(savedChartSize)) {
            savedChartSize = DEFAULT_CHART_SIZE;
        }
        this.chartSizeCombo.setSelectedItem(savedChartSize);
        applyChartSizePreset(savedChartSize);
        this.getPlotButton = new JButton(this.ok2Text);
        this.getPlotButton.addActionListener(this);
        JPanel coordFieldsPane = new JPanel(new GridBagLayout());
        // Mag Range label (from VSX data) - initially hidden
        this.magRangeLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        this.magRangeLabel.setForeground(new Color(0, 100, 0)); // Dark green
        this.magRangeLabel.setVisible(false);
        coordFieldsPane.add((Component)this.magRangeLabel, new GBC(0, 0, 2, 1).setWeight(0.0, 0.0).setAnchor(17).setInsets(5, 5, 0, 5));
        // RA and Dec fields
        coordFieldsPane.add((Component)raLabel, new GBC(0, 1, 1, 1).setWeight(0.0, 0.0).setAnchor(17).setInsets(5, 5, 0, 5));
        coordFieldsPane.add((Component)this.raField, new GBC(1, 1, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 0, 0, 5));
        coordFieldsPane.add((Component)decLabel, new GBC(0, 2, 1, 1).setWeight(0.0, 0.0).setAnchor(17).setInsets(5, 5, 0, 5));
        coordFieldsPane.add((Component)this.decField, new GBC(1, 2, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 0, 0, 5));
        coordFieldsPane.add((Component)sizeLabel, new GBC(0, 3, 1, 1).setWeight(0.0, 0.0).setAnchor(17).setInsets(5, 5, 0, 5));
        coordFieldsPane.add((Component)this.sizeField, new GBC(1, 3, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 0, 0, 5));
        coordFieldsPane.add((Component)limitingMagLabel, new GBC(0, 4, 1, 1).setWeight(0.0, 0.0).setAnchor(17).setInsets(5, 5, 0, 5));
        coordFieldsPane.add((Component)this.limitingMagField, new GBC(1, 4, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 0, 0, 5));

        // AUID panel - positioned above Chart Size
        JPanel auidPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        java.awt.Font auidStatusFont = new java.awt.Font("Dialog", java.awt.Font.BOLD, 14);
        this.auidStatusLabel.setFont(auidStatusFont);
        this.auidStatusLabel.setForeground(Color.RED);
        this.auidValueLabel.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        this.requestAuidButton.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
        this.requestAuidButton.addActionListener(this);
        this.checkVsxButton.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
        this.checkVsxButton.addActionListener(this);
        auidPanel.add(new JLabel("AUID: "));
        auidPanel.add(this.auidStatusLabel);
        auidPanel.add(this.auidValueLabel);
        auidPanel.add(this.requestAuidButton);
        auidPanel.add(this.checkVsxButton);
        // Initially hide AUID components until a star search is performed
        this.auidStatusLabel.setVisible(false);
        this.auidValueLabel.setVisible(false);
        this.requestAuidButton.setVisible(false);
        this.checkVsxButton.setVisible(false);
        
        // Existing Sequence panel
        JPanel seqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        this.seqStatusLabel.setFont(auidStatusFont);
        this.checkSeqButton.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
        this.checkSeqButton.addActionListener(this);
        seqPanel.add(new JLabel("Existing Seq? "));
        seqPanel.add(this.seqStatusLabel);
        seqPanel.add(this.checkSeqButton);
        // Initially hide sequence check components
        this.seqStatusLabel.setVisible(false);
        this.checkSeqButton.setVisible(false);

        JPanel chartSizePane = new JPanel(new GridBagLayout());
        chartSizePane.add((Component)auidPanel, new GBC(0, 0, 1, 1).setWeight(100.0, 0.0).setAnchor(17).setInsets(5, 5, 0, 5));
        chartSizePane.add((Component)seqPanel, new GBC(0, 1, 1, 1).setWeight(100.0, 0.0).setAnchor(17).setInsets(5, 5, 0, 5));
        chartSizePane.add((Component)chartSizeLabel, new GBC(0, 2, 1, 1).setWeight(100.0, 0.0).setAnchor(17).setInsets(10, 5, 0, 5));
        chartSizePane.add((Component)this.chartSizeCombo, new GBC(0, 3, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 5, 5));

        coordPane.add((Component)coordFieldsPane, new GBC(0, 0, 1, 1).setWeight(100.0, 0.0).setFill(2));
        coordPane.add((Component)chartSizePane, new GBC(1, 0, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(0, 10, 0, 5));
        coordPane.add((Component)this.getPlotButton, new GBC(0, 1, 2, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        this.quitButton = new JButton(this.quitText);
        this.closeButton = new JButton(this.closeText);
        this.quitButton.addActionListener(this);
        this.closeButton.addActionListener(this);
        buttonPane.add((Component)this.closeButton, new GBC(0, 0, 1, 1).setWeight(100.0, 0.0));
        buttonPane.add((Component)this.quitButton, new GBC(1, 0, 1, 1).setWeight(100.0, 0.0));
        catalogPane.setBorder(BorderFactory.createEtchedBorder());
        externalPane.setBorder(BorderFactory.createEtchedBorder());
        
        // API Status Panel
        JPanel apiStatusPane = new JPanel(new GridBagLayout());
        apiStatusPane.setBorder(BorderFactory.createTitledBorder("API Status"));
        JLabel statusLabel = new JLabel("API Endpoints:");
        apiStatusPane.add((Component)statusLabel, new GBC(0, 0, 1, 1).setWeight(0.0, 0.0).setAnchor(17).setInsets(5, 10, 5, 10));
        
        // Initialize status labels with gray (unknown)
        java.awt.Font statusFont = new java.awt.Font("Dialog", java.awt.Font.BOLD, 16);
        this.apass9StatusLabel.setFont(statusFont);
        this.gaiaDR2StatusLabel.setFont(statusFont);
        this.panstarrsStatusLabel.setFont(statusFont);
        this.simbadStatusLabel.setFont(statusFont);
        this.aavsoStatusLabel.setFont(statusFont);
        this.vizierStatusLabel.setFont(statusFont);
        this.apass9StatusLabel.setForeground(java.awt.Color.GRAY);
        this.gaiaDR2StatusLabel.setForeground(java.awt.Color.GRAY);
        this.panstarrsStatusLabel.setForeground(java.awt.Color.GRAY);
        this.simbadStatusLabel.setForeground(java.awt.Color.GRAY);
        this.aavsoStatusLabel.setForeground(java.awt.Color.GRAY);
        this.vizierStatusLabel.setForeground(java.awt.Color.GRAY);
        
        // Row 1: APASS, Gaia DR2, PanSTARRS
        apiStatusPane.add((Component)this.apass9StatusLabel, new GBC(1, 0, 1, 1).setWeight(0.0, 0.0).setInsets(5, 5, 2, 2));
        apiStatusPane.add((Component)new JLabel("APASS"), new GBC(2, 0, 1, 1).setWeight(0.0, 0.0).setAnchor(17).setInsets(5, 2, 2, 15));
        apiStatusPane.add((Component)this.gaiaDR2StatusLabel, new GBC(3, 0, 1, 1).setWeight(0.0, 0.0).setInsets(5, 5, 2, 2));
        apiStatusPane.add((Component)new JLabel("Gaia DR2"), new GBC(4, 0, 1, 1).setWeight(0.0, 0.0).setAnchor(17).setInsets(5, 2, 2, 15));
        apiStatusPane.add((Component)this.panstarrsStatusLabel, new GBC(5, 0, 1, 1).setWeight(0.0, 0.0).setInsets(5, 5, 2, 2));
        apiStatusPane.add((Component)new JLabel("PanSTARRS"), new GBC(6, 0, 1, 1).setWeight(100.0, 0.0).setAnchor(17).setInsets(5, 2, 2, 10));
        
        // Row 2: SIMBAD, AAVSO, VizieR
        apiStatusPane.add((Component)this.simbadStatusLabel, new GBC(1, 1, 1, 1).setWeight(0.0, 0.0).setInsets(2, 5, 5, 2));
        apiStatusPane.add((Component)new JLabel("SIMBAD"), new GBC(2, 1, 1, 1).setWeight(0.0, 0.0).setAnchor(17).setInsets(2, 2, 5, 15));
        apiStatusPane.add((Component)this.aavsoStatusLabel, new GBC(3, 1, 1, 1).setWeight(0.0, 0.0).setInsets(2, 5, 5, 2));
        apiStatusPane.add((Component)new JLabel("AAVSO"), new GBC(4, 1, 1, 1).setWeight(0.0, 0.0).setAnchor(17).setInsets(2, 2, 5, 15));
        apiStatusPane.add((Component)this.vizierStatusLabel, new GBC(5, 1, 1, 1).setWeight(0.0, 0.0).setInsets(2, 5, 5, 2));
        apiStatusPane.add((Component)new JLabel("VizieR"), new GBC(6, 1, 1, 1).setWeight(100.0, 0.0).setAnchor(17).setInsets(2, 2, 5, 10));
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.add((Component)catalogPane, new GBC(0, 0, 1, 1).setWeight(100.0, 0.7).setInsets(10, 5, 5, 5).setFill(1));
    mainPanel.add((Component)externalPane, new GBC(0, 1, 1, 1).setWeight(100.0, 0.12).setInsets(5, 5, 5, 5).setFill(1));
    mainPanel.add((Component)apiStatusPane, new GBC(0, 2, 1, 1).setWeight(100.0, 0.0).setInsets(5, 5, 10, 5).setFill(2));
    mainPanel.add((Component)starnamePane, new GBC(0, 3, 1, 1).setWeight(100.0, 0.1).setInsets(5, 5, 5, 5).setFill(1));
    mainPanel.add((Component)coordPane, new GBC(0, 4, 1, 1).setWeight(100.0, 0.06).setInsets(5, 5, 5, 5).setFill(1));
    mainPanel.add((Component)buttonPane, new GBC(0, 5, 1, 1).setWeight(100.0, 0.02).setInsets(5, 5, 5, 5).setFill(2));
        this.add(mainPanel);
        
        // Setup glass pane for loading overlay
        this.setupGlassPane();
        
        // Check API status in background
        this.checkAPIStatus();
        
        this.setDefaultCloseOperation(2);
        this.setVisible(true);
    }
    
    private void setupGlassPane() {
        this.glassPane = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                if (!isVisible()) return;
                
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
                
                // Semi-transparent overlay
                g2.setColor(new java.awt.Color(0, 0, 0, 128));
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Blue loading spinner
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = 30;
                
                g2.setColor(new java.awt.Color(0, 100, 255));
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                   java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new java.awt.BasicStroke(4));
                
                // Animated spinner
                long time = System.currentTimeMillis() - loadingStartTime;
                int startAngle = (int)((time / 10) % 360);
                g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, 90);
                
                // Loading text
                g2.setColor(java.awt.Color.WHITE);
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String text = "Searching database";
                int textWidth = fm.stringWidth(text);
                g2.drawString(text, centerX - textWidth / 2, centerY + radius + 25);
            }
        };
        this.glassPane.setOpaque(false);
        this.glassPane.setVisible(false);
        this.setGlassPane(this.glassPane);
    }
    
    private void showLoadingOverlay(boolean show) {
        if (show) {
            this.loadingStartTime = System.currentTimeMillis();
            if (this.loadingTimer == null) {
                this.loadingTimer = new javax.swing.Timer(50, e -> {
                    if (this.glassPane.isVisible()) {
                        this.glassPane.repaint();
                    }
                });
            }
            this.glassPane.setVisible(true);
            this.loadingTimer.start();
        } else {
            if (this.loadingTimer != null) {
                this.loadingTimer.stop();
            }
            this.glassPane.setVisible(false);
        }
    }

    private JTextField setupField(JTextField jtf, String name, int size) {
        jtf.setColumns(size);
        jtf.addActionListener(this);
        jtf.addFocusListener(this);
        jtf.setEditable(true);
        jtf.setText(name);
        
        // Add Enter key listener for star name field
        if (jtf == this.starnameField) {
            jtf.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Parse and format the star name
                    String starName = jtf.getText().trim();
                    String formattedName = parseTargetName(starName);
                    jtf.setText(formattedName);
                    
                    // Trigger the "Find RA & Dec for Star" action
                    getRaDecButton.doClick();
                }
            });
        }
        
        return jtf;
    }
    
    /**
     * Parse target name according to specific rules:
     * - "ao aqr" -> "AO Aqr" (capitalize first letter of each word)
     * - "g49959.00373" -> "G49959.00373" (capitalize only first letter)
     */
    private String parseTargetName(String input) {
        if (input == null) {
            return input;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return input;
        }

        String[] parts = trimmed.split("\\s+");
        if (parts.length == 1) {
            // Single word - make it all uppercase
            return parts[0].toUpperCase();
        }

        int constellationStart = parts.length - 1;
        String mapped = null;
        int maxLookback = Math.min(3, parts.length);
        for (int lookback = maxLookback; lookback >= 1; lookback--) {
            int startIndex = parts.length - lookback;
            StringBuilder keyBuilder = new StringBuilder();
            for (int j = startIndex; j < parts.length; j++) {
                if (j > startIndex) {
                    keyBuilder.append(" ");
                }
                keyBuilder.append(parts[j].toLowerCase());
            }
            String key = keyBuilder.toString();
            mapped = CONSTELLATION_ABBREVIATIONS.get(key);
            if (mapped != null) {
                constellationStart = startIndex;
                break;
            }
        }

        if (mapped == null) {
            String lastLower = parts[parts.length - 1].toLowerCase();
            if (lastLower.length() >= 3) {
                mapped = CONSTELLATION_ABBREVIATIONS.get(lastLower.substring(0, 3));
            }
            if (mapped == null && lastLower.length() > 0) {
                mapped = Character.toUpperCase(lastLower.charAt(0)) + lastLower.substring(1).toLowerCase();
            }
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < constellationStart; i++) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(parts[i].toUpperCase());
        }
        if (mapped != null) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(mapped);
        }
        return result.toString().trim();
    }

    private void applyChartSizePreset(String selection) {
        if (selection == null) {
            return;
        }
        ChartSizePreset preset = CHART_SIZE_PRESETS.get(selection);
        if (preset == null) {
            return;
        }
        if (this.sizeField != null) {
            this.sizeField.setText(formatNumber(preset.fieldSizeArcmin));
        }
        if (this.limitingMagField != null) {
            this.limitingMagField.setText(formatNumber(preset.limitingMag));
        }
    }

    private static Map<String, String> createConstellationMap() {
        HashMap<String, String> map = new HashMap<>();
        registerConstellation(map, "And", "and", "andromeda", "andromedae");
        registerConstellation(map, "Ant", "ant", "antlia", "antliae");
        registerConstellation(map, "Aps", "aps", "apus", "apodis");
        registerConstellation(map, "Aqr", "aqr", "aquarius", "aquarii");
        registerConstellation(map, "Aql", "aql", "aquila", "aquilae");
        registerConstellation(map, "Ara", "ara", "arae");
        registerConstellation(map, "Ari", "ari", "aries", "arietis");
        registerConstellation(map, "Aur", "aur", "auriga", "aurigae");
        registerConstellation(map, "Boo", "boo", "bootes", "bootis");
        registerConstellation(map, "Cae", "cae", "caelum", "caeli");
        registerConstellation(map, "Cam", "cam", "camelopardalis");
        registerConstellation(map, "Cap", "cap", "capricornus", "capricorni");
        registerConstellation(map, "Car", "car", "carina", "carinae");
        registerConstellation(map, "Cas", "cas", "cassiopeia", "cassiopeiae");
        registerConstellation(map, "Cen", "cen", "centaurus", "centauri");
        registerConstellation(map, "Cep", "cep", "cepheus", "cephei");
        registerConstellation(map, "Cet", "cet", "cetus", "ceti");
        registerConstellation(map, "Cha", "cha", "chamaeleon", "chamaeleontis");
        registerConstellation(map, "Cir", "cir", "circinus", "circini");
        registerConstellation(map, "CMa", "cma", "canis major", "canis majoris");
        registerConstellation(map, "CMi", "cmi", "canis minor", "canis minoris");
        registerConstellation(map, "Cnc", "cnc", "cancer", "cancri");
        registerConstellation(map, "Col", "col", "columba", "columbae");
        registerConstellation(map, "Com", "com", "coma berenices", "comae berenices");
        registerConstellation(map, "CrA", "cra", "corona australis", "coronae australis");
        registerConstellation(map, "CrB", "crb", "corona borealis", "coronae borealis");
        registerConstellation(map, "Crt", "crt", "crater", "crateris");
        registerConstellation(map, "Cru", "cru", "crux", "crucis");
        registerConstellation(map, "Crv", "crv", "corvus", "corvi");
        registerConstellation(map, "CVn", "cvn", "canes venatici", "canum venaticorum");
        registerConstellation(map, "Cyg", "cyg", "cygnus", "cygni");
        registerConstellation(map, "Del", "del", "delphinus", "delphini");
        registerConstellation(map, "Dor", "dor", "dorado", "doradus");
        registerConstellation(map, "Dra", "dra", "draco", "draconis");
        registerConstellation(map, "Equ", "equ", "equuleus", "equulei");
        registerConstellation(map, "Eri", "eri", "eridanus", "eridani");
        registerConstellation(map, "For", "for", "fornax", "fornacis");
        registerConstellation(map, "Gem", "gem", "gemini", "geminorum");
        registerConstellation(map, "Gru", "gru", "grus", "gruis");
        registerConstellation(map, "Her", "her", "hercules", "herculis");
        registerConstellation(map, "Hor", "hor", "horologium", "horologii");
        registerConstellation(map, "Hya", "hya", "hydra", "hydrae");
        registerConstellation(map, "Hyi", "hyi", "hydrus", "hydri");
        registerConstellation(map, "Ind", "ind", "indus", "indi");
        registerConstellation(map, "Lac", "lac", "lacerta", "lacertae");
        registerConstellation(map, "Leo", "leo", "leonis");
        registerConstellation(map, "LMi", "lmi", "leo minor", "leonis minoris");
        registerConstellation(map, "Lep", "lep", "lepus", "leporis");
        registerConstellation(map, "Lib", "lib", "libra", "librae");
        registerConstellation(map, "Lup", "lup", "lupus", "lupi");
        registerConstellation(map, "Lyn", "lyn", "lynx", "lyncis");
        registerConstellation(map, "Lyr", "lyr", "lyra", "lyrae");
        registerConstellation(map, "Men", "men", "mensa", "mensae");
        registerConstellation(map, "Mic", "mic", "microscopium", "microscopii");
        registerConstellation(map, "Mon", "mon", "monoceros", "monocerotis");
        registerConstellation(map, "Mus", "mus", "musca", "muscae");
        registerConstellation(map, "Nor", "nor", "norma", "normae");
        registerConstellation(map, "Oct", "oct", "octans", "octantis");
        registerConstellation(map, "Oph", "oph", "ophiuchus", "ophiuchi");
        registerConstellation(map, "Ori", "ori", "orion", "orionis");
        registerConstellation(map, "Pav", "pav", "pavo", "pavonis");
        registerConstellation(map, "Peg", "peg", "pegasus", "pegasi");
        registerConstellation(map, "Per", "per", "perseus", "persei");
        registerConstellation(map, "Phe", "phe", "phoenix", "phoenicis");
        registerConstellation(map, "Pic", "pic", "pictor", "pictoris");
        registerConstellation(map, "PsA", "psa", "piscis austrinus", "piscium austrini");
        registerConstellation(map, "Psc", "psc", "pisces", "piscium");
        registerConstellation(map, "Pyx", "pyx", "pyxis", "pyxidis");
        registerConstellation(map, "Ret", "ret", "reticulum", "reticuli");
        registerConstellation(map, "Scl", "scl", "sculptor", "sculptoris");
        registerConstellation(map, "Sco", "sco", "scorpius", "scorpii");
        registerConstellation(map, "Sct", "sct", "scutum", "scuti");
        registerConstellation(map, "Ser", "ser", "serpens", "serpentis");
        registerConstellation(map, "Sex", "sex", "sextans", "sextantis");
        registerConstellation(map, "Sge", "sge", "sagitta", "sagittae");
        registerConstellation(map, "Sgr", "sgr", "sagittarius", "sagittarii");
        registerConstellation(map, "Tau", "tau", "taurus", "tauri");
        registerConstellation(map, "Tel", "tel", "telescopium", "telescopii");
        registerConstellation(map, "Tri", "tri", "triangulum", "trianguli");
        registerConstellation(map, "TrA", "tra", "triangulum australe", "trianguli australis");
        registerConstellation(map, "Tuc", "tuc", "tucana", "tucanae");
        registerConstellation(map, "UMa", "uma", "ursa major", "ursa majoris", "ursae majoris");
        registerConstellation(map, "UMi", "umi", "ursa minor", "ursa minoris", "ursae minoris");
        registerConstellation(map, "Vel", "vel", "vela", "velorum");
        registerConstellation(map, "Vir", "vir", "virgo", "virginis");
        registerConstellation(map, "Vol", "vol", "volans", "volantis");
        registerConstellation(map, "Vul", "vul", "vulpecula", "vulpeculae");
        return Collections.unmodifiableMap(map);
    }

    private static void registerConstellation(Map<String, String> map, String abbreviation, String... aliases) {
        for (String alias : aliases) {
            map.put(alias.toLowerCase(), abbreviation);
        }
    }

    private static Map<String, ChartSizePreset> createChartSizePresetMap() {
        HashMap<String, ChartSizePreset> map = new HashMap<>();
        map.put("A (15 deg, <9 mag)", new ChartSizePreset(900.0, 9.0));
        map.put("B (3 deg, <11 mag)", new ChartSizePreset(180.0, 11.0));
        map.put("C (2 deg, <12 mag)", new ChartSizePreset(120.0, 12.0));
        map.put("D (1 deg, <13 mag)", new ChartSizePreset(60.0, 13.0));
        map.put("E (30 min, <14 mag)", new ChartSizePreset(30.0, 14.0));
        map.put("F (15 min, <15 mag)", new ChartSizePreset(15.0, 15.0));
        map.put("G (7.5 min, <16 mag)", new ChartSizePreset(7.5, 16.0));
        return Collections.unmodifiableMap(map);
    }

    private static String formatNumber(double value) {
        double rounded = Math.rint(value);
        if (Math.abs(value - rounded) < 1e-6) {
            return String.format(Locale.US, "%.0f", rounded);
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private static final class ChartSizePreset {
        private final double fieldSizeArcmin;
        private final double limitingMag;

        private ChartSizePreset(double fieldSizeArcmin, double limitingMag) {
            this.fieldSizeArcmin = fieldSizeArcmin;
            this.limitingMag = limitingMag;
        }
    }

    public JCheckBox setupCheckBox(JCheckBox cb, String text, Boolean bool, String toolTipText) {
        cb.setText(text);
        cb.addActionListener(this);
        if (bool != null) {
            cb.setSelected(bool);
        }
        cb.setToolTipText(toolTipText);
        return cb;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == this.getRaDecButton) {
            // Check if star name is blank
            if (this.starnameField.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(this, "Please enter a star name.", "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            this.setCursor(Cursor.getPredefinedCursor(3));
            this.db.setQuitSelected(false);
            this.db.setCancelSelected(false);
            this.db.setStar(this.starnameField.getText());
            this.db.getPositionFromUser(this.db.getStar());
            if (this.db.getBlankCoord().booleanValue()) {
                this.raField.setText("");
                this.decField.setText("");
                this.db.setBlankCoord(false);
            } else {
                // Convert decimal degrees to sexagesimal format
                double raDeg = this.db.getCentralRA();
                double decDeg = this.db.getCentralDec();
                
                this.raField.setText(formatRAToSexagesimal(raDeg));
                this.decField.setText(formatDecToSexagesimal(decDeg));
                
                // Update AUID display
                updateAuidDisplay();
                
                this.db.setUserDefaults(this.seqplot.getColorArray());
                this.getRootPane().setDefaultButton(this.getPlotButton);
            }
            this.setCursor(Cursor.getDefaultCursor());
        }
        if (ae.getSource() == this.requestAuidButton) {
            // Open VSX detail page in browser to request AUID
            String oid = this.db.getOid();
            if (oid != null && !oid.trim().isEmpty()) {
                String vsxUrl = "https://vsx.aavso.org/index.php?view=detail.top&oid=" + oid;
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                        if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                            desktop.browse(new java.net.URI(vsxUrl));
                        } else {
                            showUrlInDialog(vsxUrl);
                        }
                    } else {
                        showUrlInDialog(vsxUrl);
                    }
                } catch (Exception ex) {
                    showUrlInDialog(vsxUrl);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No VSX Object ID available for this star.\nCannot open AUID request page.", 
                    "No OID", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        if (ae.getSource() == this.checkVsxButton) {
            // Open VSX detail page in browser
            String oid = this.db.getOid();
            if (oid != null && !oid.trim().isEmpty()) {
                String vsxDetailUrl = "https://www.aavso.org/vsx/index.php?view=detail.top&oid=" + oid;
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                        if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                            desktop.browse(new java.net.URI(vsxDetailUrl));
                        } else {
                            // Desktop browsing not supported, show URL in dialog
                            showUrlInDialog(vsxDetailUrl);
                        }
                    } else {
                        // Desktop not supported, show URL in dialog
                        showUrlInDialog(vsxDetailUrl);
                    }
                } catch (Exception ex) {
                    System.err.println("Error opening VSX detail page: " + ex.getMessage());
                    showUrlInDialog(vsxDetailUrl);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No VSX Object ID available for this star.\nCannot open VSX detail page.", 
                    "No OID", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        if (ae.getSource() == this.checkSeqButton) {
            // Check for existing sequence
            checkExistingSequence();
        }
        if (ae.getSource() == this.getPlotButton) {
            this.collectInputData();
            this.db.setCancelSelected(false);
        }
        if (ae.getSource() == this.closeButton) {
            this.db.setCancelSelected(true);
            this.dispose();
        }
        if (ae.getSource() == this.quitButton) {
            this.setCatalogs();
            this.db.setUserDefaults(this.seqplot.getColorArray());
            System.exit(0);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (e.getSource() == this.starnameField) {
            this.getRootPane().setDefaultButton(this.getRaDecButton);
            this.raField.setText("");
            this.decField.setText("");
        } else {
            this.getRootPane().setDefaultButton(this.getPlotButton);
        }
        if (e.getSource() == this.raField || e.getSource() == this.decField) {
            this.starnameField.setText("");
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    public double stringToDouble(String stringObject) {
        return Double.parseDouble(stringObject.trim());
    }
    
    /**
     * Convert RA in decimal degrees to HH:MM:SS.ss format
     */
    private String formatRAToSexagesimal(double raDeg) {
        double raHours = raDeg / 15.0;
        int hh = (int)raHours;
        double raMinutes = (raHours - hh) * 60.0;
        int mm = (int)raMinutes;
        double ss = (raMinutes - mm) * 60.0;
        
        return String.format("%02d:%02d:%05.2f", hh, mm, ss);
    }
    
    /**
     * Convert Dec in decimal degrees to DD:MM:SS.s format
     */
    private String formatDecToSexagesimal(double decDeg) {
        boolean isNegative = decDeg < 0;
        double absDec = Math.abs(decDeg);
        int dd = (int)absDec;
        double decMinutes = (absDec - dd) * 60.0;
        int mm = (int)decMinutes;
        double ss = (decMinutes - mm) * 60.0;
        
        if (isNegative) {
            return String.format("-%02d:%02d:%04.1f", dd, mm, ss);
        } else {
            return String.format("+%02d:%02d:%04.1f", dd, mm, ss);
        }
    }

    public void collectInputData() {
        this.setCatalogs();
        
        // Check catalog selection first
        if (this.db.getCatalogString().equals("") && !this.db.getApass9BoxSelected() && !this.db.getGaiaDR2BoxSelected() && !this.db.getGaiaDR3BoxSelected() && !this.db.getPanstarrsBoxSelected() && !this.db.getSdssBoxSelected()) {
            JOptionPane.showMessageDialog(this, "You must select at least one catalog.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check for missing RA and Dec specifically
        boolean raBlank = this.raField.getText().trim().equals("");
        boolean decBlank = this.decField.getText().trim().equals("");
        
        if (raBlank && decBlank) {
            JOptionPane.showMessageDialog(this, "RA and Dec coordinates are required.\nPlease enter coordinates or use 'Find RA & Dec for Star' button.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (raBlank) {
            JOptionPane.showMessageDialog(this, "RA coordinate is required.\nPlease enter an RA value.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (decBlank) {
            JOptionPane.showMessageDialog(this, "Dec coordinate is required.\nPlease enter a Dec value.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Proceed with coordinate parsing if both RA and Dec are present
            if ((this.raField.getText().contains(":") || this.raField.getText().contains(" ")) && (this.decField.getText().contains(":") || this.decField.getText().contains(" "))) {
                double rh = this.stringToDouble(this.raField.getText().substring(0, 2));
                double rm = this.stringToDouble(this.raField.getText().substring(3, 5)) / 60.0;
                double rs = this.stringToDouble(this.raField.getText().substring(6)) / 3600.0;
                this.db.setCentralRA((rh + rm + rs) * 15.0);
                String decStr = this.decField.getText();
                String sign = decStr.substring(0, 1);
                if (sign.equals("-") || sign.equals("+")) {
                    decStr = decStr.substring(1);
                }
                double dd = this.stringToDouble(decStr.substring(0, 2));
                double dm = this.stringToDouble(decStr.substring(3, 5)) / 60.0;
                double ds = this.stringToDouble(decStr.substring(6)) / 3600.0;
                double declination = dd + dm + ds;
                if (sign.equals("-")) {
                    declination *= -1.0;
                }
                this.db.setCentralDec(declination);
            } else {
                this.db.setCentralRA(this.stringToDouble(this.raField.getText()));
                this.db.setCentralDec(this.stringToDouble(this.decField.getText()));
            }
            while (this.sizeField.getText().equals("0.0") || this.sizeField.getText().equals("") || this.sizeField.getText().equals("0")) {
                String size = JOptionPane.showInputDialog(this, "Please enter the field size (in arcmins):", "Set Field Size", JOptionPane.QUESTION_MESSAGE);
                if (size == null) break; // User cancelled
                this.sizeField.setText(size);
            }
            while (this.limitingMagField.getText().equals("0.0")) {
                String lm = JOptionPane.showInputDialog(this, "Please enter the limiting magnitude:", "Set Limiting Magnitude", JOptionPane.QUESTION_MESSAGE);
                if (lm == null) break; // User cancelled
                this.limitingMagField.setText(lm);
            }
            this.db.setFieldSize(this.stringToDouble(this.sizeField.getText()) / 60.0);
            this.db.setRAText(this.raField.getText());
            this.db.setDecText(this.decField.getText());
            String formattedStarName = parseTargetName(this.starnameField.getText());
            this.starnameField.setText(formattedStarName);
            this.db.setStar(formattedStarName);
            this.db.setLimitingMag(this.stringToDouble(this.limitingMagField.getText()));
            if (this.chartSizeCombo != null) {
                this.db.setChartSizeSelection((String)this.chartSizeCombo.getSelectedItem());
            }
            this.db.setUserDefaults(this.seqplot.getColorArray());
            
            // Show loading overlay on THIS dialog (not the plot window)
            this.showLoadingOverlay(true);
            
            // Start data retrieval on a background thread
            Thread loaderThread = new Thread(() -> {
                try {
                    this.db.findUpperLowerRa();
                    this.db.findUpperLowerDec();
                    this.db.getData();
                    
                    // Fetch VSP comparison stars for overlay
                    this.db.fetchVSPCompStars();

                    if (this.db.getTotalCount() == 0) {
                        SwingUtilities.invokeLater(() -> {
                            this.showLoadingOverlay(false);
                            JOptionPane.showMessageDialog(EnterStar.this, "No data found for this star/field.\nTry adjusting the field size, limiting magnitude, or catalog selection.", "No Data Found", JOptionPane.WARNING_MESSAGE);
                        });
                        return;
                    }

                    if (this.db.getTotalCount() >= 5000) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(EnterStar.this, "Maximum number of records allowed has been exceeded! \nOnly the first 5000 records found will be displayed.", "Warning!", JOptionPane.WARNING_MESSAGE);
                        });
                    }

                    SwingUtilities.invokeLater(() -> {
                        // Hide loading overlay on dialog
                        this.showLoadingOverlay(false);
                        
                        // Now create and show the plot window
                        this.seqplot.setShowPlot(true);
                        this.seqplot.setMainTitleText(String.valueOf(this.db.getStar()) + "  RA: " + this.db.getFormattedRA() + "  Dec: " + this.db.getFormattedDec() + "  FoV: " + Math.round(this.db.getFieldSize() * 60.0) + " arcmin");
                        this.seqplot.setSubtitleText("Data from the Calibration Database - limiting magnitude " + this.db.getLimitingMag() + " - VSX position matching tolerance " + this.db.getPositionTolerance() + " degrees");
                        if (this.seqplot.getStarPlotPanel() != null) {
                            this.seqplot.getStarPlotPanel().refreshPlotData();
                        }
                        this.seqplot.setVisible(true);
                        this.seqplot.toFront();
                        this.seqplot.requestFocus();
                        this.dispose();
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        // Make sure loading overlay is hidden
                        this.showLoadingOverlay(false);
                    });
                }
            }, "StarDataLoader");
            loaderThread.setDaemon(true);
            loaderThread.start();
    }

    public void setCatalogs() {
        String str = "";
        if (this.tychoBox.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "1";
            this.db.setTychoBoxSelected(true);
        } else {
            this.db.setTychoBoxSelected(false);
        }
        if (this.nofsBox.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "10";
            this.db.setNofsBoxSelected(true);
        } else {
            this.db.setNofsBoxSelected(false);
        }
        if (this.sonoitaBox.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "18";
            this.db.setSonoitaBoxSelected(true);
        } else {
            this.db.setSonoitaBoxSelected(false);
        }
        if (this.bsmBox.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "28";
            this.db.setBsmBoxSelected(true);
        } else {
            this.db.setBsmBoxSelected(false);
        }
        if (this.bsm_SBox.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "37";
            this.db.setBsm_SBoxSelected(true);
        } else {
            this.db.setBsm_SBoxSelected(false);
        }
        if (this.coker30Box.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "44";
            this.db.setCoker30BoxSelected(true);
        } else {
            this.db.setCoker30BoxSelected(false);
        }
        if (this.sro50Box.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "33";
            this.db.setSro50BoxSelected(true);
        } else {
            this.db.setSro50BoxSelected(false);
        }
        if (this.tmo61Box.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "42";
            this.db.setTmo61BoxSelected(true);
        } else {
            this.db.setTmo61BoxSelected(false);
        }
        if (this.apassBox.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "29";
            this.db.setApassBoxSelected(true);
        } else {
            this.db.setApassBoxSelected(false);
        }
        if (this.k35Box.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "34";
            this.db.setK35BoxSelected(true);
        } else {
            this.db.setK35BoxSelected(false);
        }
        if (this.w28Box.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "30";
            this.db.setW28BoxSelected(true);
        } else {
            this.db.setW28BoxSelected(false);
        }
        if (this.w30Box.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "31";
            this.db.setW30BoxSelected(true);
        } else {
            this.db.setW30BoxSelected(false);
        }
        if (this.oc61Box.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "32";
            this.db.setOc61BoxSelected(true);
        } else {
            this.db.setOc61BoxSelected(false);
        }
        if (this.gcpdBox.isSelected()) {
            str = String.valueOf(str) + this.addComma(str) + "20";
            this.db.setGcpdBoxSelected(true);
        } else {
            this.db.setGcpdBoxSelected(false);
        }
        if (this.apass9Box.isSelected()) {
            this.db.setApass9BoxSelected(true);
        } else {
            this.db.setApass9BoxSelected(false);
        }
        if (this.gaiaDR2Box.isSelected()) {
            this.db.setGaiaDR2BoxSelected(true);
        } else {
            this.db.setGaiaDR2BoxSelected(false);
        }
        if (this.gaiaDR3Box.isSelected()) {
            this.db.setGaiaDR3BoxSelected(true);
        } else {
            this.db.setGaiaDR3BoxSelected(false);
        }
        if (this.panstarrsBox.isSelected()) {
            this.db.setPanstarrsBoxSelected(true);
        } else {
            this.db.setPanstarrsBoxSelected(false);
        }
        if (this.sdssBox.isSelected()) {
            this.db.setSdssBoxSelected(true);
        } else {
            this.db.setSdssBoxSelected(false);
        }
        this.db.setCatalogString(str);
    }

    private void updateCatalogSelectionOrder(JCheckBox checkbox, JLabel orderLabel) {
        if (checkbox.isSelected()) {
            // Catalog was just selected - assign next order number
            catalogSelectionOrder++;
            orderLabel.setText(" [" + catalogSelectionOrder + "]");
            orderLabel.setForeground(new java.awt.Color(0, 100, 0)); // Dark green
        } else {
            // Catalog was deselected - clear the order and renumber others
            orderLabel.setText("   ");
            
            // Renumber remaining selected catalogs (both internal and external)
            int newOrder = 1;
            // Internal AAVSO catalogs
            if (tychoBox.isSelected()) {
                tychoOrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (nofsBox.isSelected()) {
                nofsOrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (sonoitaBox.isSelected()) {
                sonoitaOrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (bsmBox.isSelected()) {
                bsmOrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (bsm_SBox.isSelected()) {
                bsm_SOrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (coker30Box.isSelected()) {
                coker30OrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (sro50Box.isSelected()) {
                sro50OrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (tmo61Box.isSelected()) {
                tmo61OrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (apassBox.isSelected()) {
                apassOrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (k35Box.isSelected()) {
                k35OrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (w28Box.isSelected()) {
                w28OrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (w30Box.isSelected()) {
                w30OrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (oc61Box.isSelected()) {
                oc61OrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (gcpdBox.isSelected()) {
                gcpdOrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            // External catalogs
            if (apass9Box.isSelected()) {
                apass9OrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (gaiaDR2Box.isSelected()) {
                gaiaDR2OrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (gaiaDR3Box.isSelected()) {
                gaiaDR3OrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            if (panstarrsBox.isSelected()) {
                panstarrsOrderLabel.setText(" [" + newOrder + "]");
                newOrder++;
            }
            catalogSelectionOrder = newOrder - 1;
        }
    }

    public String addComma(String str) {
        if (!str.equals("")) {
            return ",";
        }
        return "";
    }
    
    /**
     * Check for existing sequence by fetching VSP chart
     */
    private void checkExistingSequence() {
        String starName = this.db.getStar();
        if (starName == null || starName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No star name available.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Determine magnitude limit based on VSX MinMag
        double magLimit = this.db.getLimitingMag(); // Default fallback
        
        // Try to get MinMag from VSX details string
        String vsxDetails = this.db.getVsxDetails();
        if (vsxDetails != null && vsxDetails.contains("Min:")) {
            try {
                // Extract MinMag from "Min: X.XX V" or "Min: X.XX" format
                int minIndex = vsxDetails.indexOf("Min:");
                String minPart = vsxDetails.substring(minIndex + 4).trim();
                
                // Find the end of the magnitude value (space or end of string)
                int endIndex = minPart.indexOf(' ');
                if (endIndex > 0) {
                    minPart = minPart.substring(0, endIndex);
                }
                
                double minMag = Double.parseDouble(minPart);
                
                // Apply magnitude limit rules based on MinMag
                if (minMag < 10.0) {
                    magLimit = 13.0;
                } else if (minMag >= 10.0 && minMag < 12.0) {
                    magLimit = 14.0;
                } else if (minMag >= 12.0 && minMag < 13.0) {
                    magLimit = 15.0;
                } else if (minMag >= 13.0 && minMag < 14.0) {
                    magLimit = 16.0;
                } else if (minMag >= 14.0 && minMag < 15.0) {
                    magLimit = 17.0;
                } else if (minMag >= 15.0 && minMag < 16.0) {
                    magLimit = 18.0;
                } else {
                    magLimit = 20.0; // For very faint stars (>= 16)
                }
                
                System.out.println("DEBUG: VSX MinMag = " + minMag + " -> VSP maglimit = " + magLimit);
            } catch (Exception e) {
                System.err.println("WARNING: Could not parse MinMag from VSX details: " + vsxDetails);
                System.err.println("DEBUG: Using default maglimit = " + magLimit);
            }
        } else {
            System.out.println("DEBUG: No VSX MinMag available, using default maglimit = " + magLimit);
        }
        
        // Try to get VSP chart - first by star name, then by AUID if that fails
        String vspPageUrl = String.format("https://apps.aavso.org/vsp/chart/?star=%s&orientation=visual&type=chart&fov=60.0&maglimit=%.1f&resolution=75&north=down&east=right&lines=True",
            starName.replace(" ", "+"), magLimit);
        
        // DEBUG: Print URL to console
        System.out.println("\n========== VSP CHART REQUEST ==========");
        System.out.println("Star Name: " + starName);
        System.out.println("Magnitude Limit: " + magLimit);
        System.out.println("VSP Page URL: " + vspPageUrl);
        
        // Fetch HTML and extract image URL
        String vspImageUrl = extractImageUrlFromVspHtml(vspPageUrl);
        
        // If star name didn't work, try AUID
        if (vspImageUrl == null) {
            String auid = db.getAuid();
            if (auid != null && !auid.isEmpty() && !auid.equals("NA")) {
                System.out.println("WARNING: Star name lookup failed, trying AUID: " + auid);
                vspPageUrl = String.format("https://apps.aavso.org/vsp/chart/?star=%s&orientation=visual&type=chart&fov=60.0&maglimit=%.1f&resolution=75&north=down&east=right&lines=True",
                    auid, magLimit);
                System.out.println("VSP Page URL (AUID): " + vspPageUrl);
                vspImageUrl = extractImageUrlFromVspHtml(vspPageUrl);
            }
        }
        
        System.out.println("VSP Image URL: " + vspImageUrl);
        System.out.println("========================================\n");
        
        // Show dialog with chart (or browser link if image not found)
        showVSPChartDialog(vspPageUrl, vspImageUrl, starName);
    }
    
    /**
     * Open the AAVSO Sequence Team Update List in browser
     */
    private void openAAVSOUpdateList() {
        // Google Sheets URL with sort parameter for Uploaded column (column E, index 4)
        // Sort order: descending (1 = ascending, 2 = descending)
        String updateListUrl = "https://docs.google.com/spreadsheets/d/1mR4l7bElFYZl5lwkkVEBwByCNXwiKCMzIPS1IAx0QvQ/edit?usp=sharing#gid=288083360";
        
        System.out.println("\n========== AAVSO UPDATE LIST ==========");
        System.out.println("Opening: " + updateListUrl);
        System.out.println("=======================================\n");
        
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(new java.net.URI(updateListUrl));
                } else {
                    showUrlInDialog(updateListUrl);
                }
            } else {
                showUrlInDialog(updateListUrl);
            }
        } catch (Exception ex) {
            System.err.println("ERROR: Failed to open AAVSO Update List: " + ex.getMessage());
            ex.printStackTrace();
            showUrlInDialog(updateListUrl);
        }
    }
    
    /**
     * Open the AAVSO Sequence Team Homepage in browser
     */
    private void openSequenceTeamHomepage() {
        String homepageUrl = "https://www.aavso.org/sequence-team-homepage";
        
        System.out.println("\n========== SEQUENCE TEAM HOMEPAGE ==========");
        System.out.println("Opening: " + homepageUrl);
        System.out.println("============================================\n");
        
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(new java.net.URI(homepageUrl));
                } else {
                    showUrlInDialog(homepageUrl);
                }
            } else {
                showUrlInDialog(homepageUrl);
            }
        } catch (Exception ex) {
            System.err.println("ERROR: Failed to open Sequence Team Homepage: " + ex.getMessage());
            ex.printStackTrace();
            showUrlInDialog(homepageUrl);
        }
    }
    
    /**
     * Open the CDS Mirror Selector dialog to choose VizieR mirror and check health
     */
    private void openCDSMirrorSelector() {
        JDialog mirrorDialog = new JDialog(this, "Choose CDS Mirror", true);
        mirrorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        mirrorDialog.setSize(700, 350);
        mirrorDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title label
        JLabel titleLabel = new JLabel("Select a CDS VizieR Mirror");
        titleLabel.setFont(titleLabel.getFont().deriveFont(14.0f));
        mainPanel.add(titleLabel, new GBC(0, 0, 3, 1).setWeight(100.0, 0.0).setFill(2).setInsets(0, 0, 10, 0));
        
        // Info label
        JLabel infoLabel = new JLabel("Click 'Check Health' to update status. Select a mirror and click 'Apply'.");
        mainPanel.add(infoLabel, new GBC(0, 1, 3, 1).setWeight(100.0, 0.0).setFill(2).setInsets(0, 0, 10, 0));
        
        // Headers
        mainPanel.add(new JLabel("Mirror"), new GBC(0, 2, 1, 1).setWeight(50.0, 0.0).setInsets(5, 0, 5, 5).setAnchor(17));
        mainPanel.add(new JLabel("Status"), new GBC(1, 2, 1, 1).setWeight(30.0, 0.0).setInsets(5, 5, 5, 5).setAnchor(17));
        mainPanel.add(new JLabel("URL"), new GBC(2, 2, 1, 1).setWeight(20.0, 0.0).setInsets(5, 5, 5, 0).setAnchor(17));
        
        // Get mirrors and create radio button selection
        java.util.List<CDSMirrorSelector.MirrorEndpoint> mirrors = CDSMirrorSelector.getMirrors();
        javax.swing.ButtonGroup mirrorGroup = new javax.swing.ButtonGroup();
        CDSMirrorSelector.MirrorEndpoint selectedMirror = CDSMirrorSelector.getSelectedMirror();
        
        int row = 3;
        JRadioButton[] mirrorRadios = new JRadioButton[mirrors.size()];
        JLabel[] statusLabels = new JLabel[mirrors.size()];
        
        for (int i = 0; i < mirrors.size(); i++) {
            final CDSMirrorSelector.MirrorEndpoint mirror = mirrors.get(i);
            
            // Radio button for selection
            mirrorRadios[i] = new javax.swing.JRadioButton(mirror.name);
            mirrorRadios[i].setSelected(mirror == selectedMirror);
            mirrorGroup.add(mirrorRadios[i]);
            mainPanel.add(mirrorRadios[i], new GBC(0, row, 1, 1).setWeight(50.0, 0.0).setFill(2).setInsets(3, 0, 3, 5));
            
            // Status label
            statusLabels[i] = new JLabel("○ Unknown");
            statusLabels[i].setForeground(java.awt.Color.GRAY);
            mainPanel.add(statusLabels[i], new GBC(1, row, 1, 1).setWeight(30.0, 0.0).setInsets(3, 5, 3, 5));
            
            // URL label (truncated)
            JLabel urlLabel = new JLabel(truncateUrl(mirror.baseUrl));
            urlLabel.setFont(urlLabel.getFont().deriveFont(10.0f));
            urlLabel.setForeground(new java.awt.Color(100, 100, 150));
            mainPanel.add(urlLabel, new GBC(2, row, 1, 1).setWeight(20.0, 0.0).setInsets(3, 5, 3, 0));
            
            row++;
        }
        
        // Check Health button
        JButton checkHealthButton = new JButton("Check Health");
        checkHealthButton.addActionListener(e -> {
            checkHealthButton.setEnabled(false);
            checkHealthButton.setText("Checking...");
            
            // Check health in background
            new Thread(() -> {
                try {
                    for (int i = 0; i < mirrors.size(); i++) {
                        final int idx = i;
                        CDSMirrorSelector.HealthStatus status = CDSMirrorSelector.checkMirrorHealth(mirrors.get(i));
                        SwingUtilities.invokeLater(() -> {
                            statusLabels[idx].setText(mirrors.get(idx).status.displayText);
                            statusLabels[idx].setForeground(mirrors.get(idx).status.color);
                        });
                    }
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        checkHealthButton.setEnabled(true);
                        checkHealthButton.setText("Check Health");
                    });
                }
            }).start();
        });
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {
            for (int i = 0; i < mirrorRadios.length; i++) {
                if (mirrorRadios[i].isSelected()) {
                    CDSMirrorSelector.setSelectedMirror(mirrors.get(i));
                    JOptionPane.showMessageDialog(
                        mirrorDialog,
                        "CDS Mirror set to:\n" + mirrors.get(i).name + "\n\n" +
                        "URL: " + mirrors.get(i).baseUrl + "\n\n" +
                        "This will be used for APASS9, PanSTARRS, and Tycho-2 queries.",
                        "Mirror Applied",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    mirrorDialog.dispose();
                    return;
                }
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> mirrorDialog.dispose());
        
        buttonPanel.add(checkHealthButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, new GBC(0, row, 3, 1).setWeight(100.0, 0.0).setFill(2).setInsets(20, 0, 0, 0));
        
        mirrorDialog.add(mainPanel);
        mirrorDialog.setVisible(true);
    }
    
    /**
     * Truncate URL for display
     */
    private String truncateUrl(String url) {
        if (url.length() > 30) {
            return url.substring(0, 27) + "...";
        }
        return url;
    }
    
    private void configureCatalogTimeout() {
        int currentTimeout = this.db.getCatalogReadTimeoutSeconds();
        String input = JOptionPane.showInputDialog(
            this,
            "Enter catalog query timeout in seconds:\n" +
            "(Minimum: 10 seconds, Maximum: 600 seconds)\n\n" +
            "This timeout applies when loading external catalogs\n" +
            "(APASS9, PanSTARRS, Gaia DR2/DR3) from remote services.\n" +
            "Increase this value for large field sizes or slow connections.",
            "Catalog Query Timeout",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (input != null && !input.trim().isEmpty()) {
            try {
                int newTimeout = Integer.parseInt(input.trim());
                this.db.setCatalogReadTimeoutSeconds(newTimeout);
                JOptionPane.showMessageDialog(
                    this,
                    String.format("Catalog query timeout set to %d seconds.", 
                        this.db.getCatalogReadTimeoutSeconds()),
                    "Timeout Updated",
                    JOptionPane.INFORMATION_MESSAGE
                );
                System.out.println("Catalog read timeout updated to: " + 
                    this.db.getCatalogReadTimeoutSeconds() + " seconds");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Invalid input. Please enter a number between 10 and 600.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Fetch HTML from VSP page and extract the chart image URL
     */
    private String extractImageUrlFromVspHtml(String vspPageUrl) {
        try {
            System.out.println("DEBUG: Fetching HTML from: " + vspPageUrl);
            java.net.URL url = new java.net.URL(vspPageUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Seqplot/6.0");
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder html = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                html.append(line).append("\n");
                // Look for the image tag with /vsp/chart/*.png
                if (line.contains("/vsp/chart/") && line.contains(".png")) {
                    // Extract the image path from src="/vsp/chart/XXXXX.png"
                    int srcIndex = line.indexOf("src=\"/vsp/chart/");
                    if (srcIndex != -1) {
                        int startQuote = srcIndex + 5; // position after src="
                        int endQuote = line.indexOf("\"", startQuote);
                        if (endQuote != -1) {
                            String imagePath = line.substring(startQuote, endQuote);
                            reader.close();
                            String imageUrl = "https://apps.aavso.org" + imagePath;
                            System.out.println("DEBUG: Found image URL in HTML: " + imageUrl);
                            return imageUrl;
                        }
                    }
                }
            }
            reader.close();
            
            System.err.println("WARNING: Could not find image URL in HTML");
            return null;
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch VSP HTML: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Show VSP chart in a dialog with Adequate/Not Adequate buttons
     */
    private void showVSPChartDialog(String vspPageUrl, String vspImageUrl, String starName) {
        JDialog chartDialog = new JDialog(this, "Existing sequence in vicinity - " + starName, true);
        
        // Get screen dimensions and set dialog to fit screen
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int dialogWidth = Math.min(1400, (int)(screenSize.width * 0.85));
        int dialogHeight = Math.min(900, (int)(screenSize.height * 0.85));
        chartDialog.setSize(dialogWidth, dialogHeight);
        chartDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new java.awt.BorderLayout());
        
        // Create a label with link to open in browser
        JLabel loadingLabel = new JLabel("<html>Loading VSP chart... If no image appears, <a href=''>click here</a> to open in browser</html>", JLabel.CENTER);
        loadingLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loadingLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(vspPageUrl));
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to open browser: " + ex.getMessage());
                }
            }
        });
        mainPanel.add(loadingLabel, java.awt.BorderLayout.NORTH);
        
        // Try to load the image from URL - try imageUrl first, fall back to opening in browser
        boolean imageLoaded = false;
        java.awt.Image originalImage = null;
        
        // Check if we have a valid image URL
        if (vspImageUrl == null || vspImageUrl.isEmpty()) {
            System.out.println("DEBUG: No valid image URL found, will show browser link only");
        } else {
            try {
                System.out.println("DEBUG: Attempting to load image from URL: " + vspImageUrl);
                java.net.URL imageUrl = new java.net.URL(vspImageUrl);
                System.out.println("DEBUG: URL created successfully");
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(imageUrl);
            System.out.println("DEBUG: ImageIcon created - Image status: " + icon.getImageLoadStatus());
            System.out.println("DEBUG: Icon width: " + icon.getIconWidth() + ", height: " + icon.getIconHeight());
            
            if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                originalImage = icon.getImage();
                
                // Scale image to fit dialog while maintaining aspect ratio
                int imgWidth = icon.getIconWidth();
                int imgHeight = icon.getIconHeight();
                int maxWidth = dialogWidth - 50;
                int maxHeight = dialogHeight - 150; // Leave room for buttons
                
                double scaleX = (double)maxWidth / imgWidth;
                double scaleY = (double)maxHeight / imgHeight;
                double scale = Math.min(scaleX, scaleY);
                
                int scaledWidth = (int)(imgWidth * scale);
                int scaledHeight = (int)(imgHeight * scale);
                
                java.awt.Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, java.awt.Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new javax.swing.ImageIcon(scaledImage));
                JScrollPane scrollPane = new JScrollPane(imageLabel);
                mainPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
                
                System.out.println("DEBUG: Image scaled from " + imgWidth + "x" + imgHeight + 
                                 " to " + scaledWidth + "x" + scaledHeight + " (scale=" + scale + ")");
                imageLoaded = true;
            }
            } catch (Exception e) {
                System.err.println("ERROR: Failed to load VSP chart image: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Store reference to original image for saving
        final java.awt.Image imageToSave = originalImage;
        
        // If image didn't load, show message with link to open in browser
        if (!imageLoaded) {
            System.out.println("DEBUG: Image failed to load, showing browser link");
            JTextArea infoArea = new JTextArea(
                "VSP chart could not be loaded directly.\n\n" +
                "Click the link above or use this URL to view in browser:\n\n" +
                vspPageUrl + "\n\n" +
                "Review the chart and decide if the existing sequence is adequate.");
            infoArea.setEditable(false);
            infoArea.setLineWrap(true);
            infoArea.setWrapStyleWord(true);
            mainPanel.add(new JScrollPane(infoArea), java.awt.BorderLayout.CENTER);
        }
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton adequateButton = new JButton("Seq Adequate");
        JButton notAdequateButton = new JButton("Seq Not Adequate");
        JButton savePlotButton = new JButton("Save Plot");
        
        adequateButton.addActionListener(e -> {
            this.seqStatusLabel.setText("✓");
            this.seqStatusLabel.setForeground(Color.GREEN);
            this.seqStatusLabel.setVisible(true);
            this.checkSeqButton.setVisible(false);
            chartDialog.dispose();
        });
        
        notAdequateButton.addActionListener(e -> {
            this.seqStatusLabel.setText("✗");
            this.seqStatusLabel.setForeground(Color.RED);
            this.seqStatusLabel.setVisible(true);
            this.checkSeqButton.setVisible(false);
            chartDialog.dispose();
        });
        
        savePlotButton.addActionListener(e -> {
            if (imageToSave != null) {
                try {
                    // Get Downloads folder
                    String userHome = System.getProperty("user.home");
                    String downloadsPath = userHome + "/Downloads";
                    
                    // Create filename with star name and timestamp
                    String sanitizedStarName = starName.replaceAll("[^a-zA-Z0-9_-]", "_");
                    String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                    String filename = sanitizedStarName + "_VSP_" + timestamp + ".png";
                    java.io.File outputFile = new java.io.File(downloadsPath, filename);
                    
                    // Convert Image to BufferedImage and save as PNG
                    java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                        imageToSave.getWidth(null), 
                        imageToSave.getHeight(null), 
                        java.awt.image.BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g2d = bufferedImage.createGraphics();
                    g2d.drawImage(imageToSave, 0, 0, null);
                    g2d.dispose();
                    
                    javax.imageio.ImageIO.write(bufferedImage, "png", outputFile);
                    
                    System.out.println("DEBUG: VSP chart saved to: " + outputFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(chartDialog,
                        "Chart saved to:\n" + outputFile.getAbsolutePath(),
                        "Save Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    System.err.println("ERROR: Failed to save chart: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(chartDialog,
                        "Failed to save chart: " + ex.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(chartDialog,
                    "No image available to save.",
                    "Save Error",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(savePlotButton);
        buttonPanel.add(adequateButton);
        buttonPanel.add(notAdequateButton);
        mainPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
        
        chartDialog.add(mainPanel);
        chartDialog.setVisible(true);
    }
    
    /**
     * Show URL in a dialog if desktop browse is not supported
     */
    private void showUrlInDialog(String url) {
        JOptionPane.showMessageDialog(this, 
            "Please open this URL in your browser:\n\n" + url + "\n\n" +
            "You will need to authenticate and click 'Request AUID'.", 
            "Open in Browser", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Update AUID display based on whether AUID is available from VSX
     */
    private void updateAuidDisplay() {
        System.out.println("DEBUG: updateAuidDisplay() called");
        String auid = this.db.getAuid();
        System.out.println("DEBUG: AUID = '" + auid + "'");
        if (auid != null && !auid.trim().isEmpty()) {
            // AUID exists - show green checkmark, AUID value, and Check VSX button
            this.auidStatusLabel.setText("✓");
            this.auidStatusLabel.setForeground(Color.GREEN);
            this.auidStatusLabel.setVisible(true);
            this.auidValueLabel.setText(auid);
            this.auidValueLabel.setVisible(true);
            this.requestAuidButton.setVisible(false);
            this.checkVsxButton.setVisible(true);
        } else {
            // No AUID - show red X and request button
            this.auidStatusLabel.setText("✗");
            this.auidStatusLabel.setForeground(Color.RED);
            this.auidStatusLabel.setVisible(true);
            this.auidValueLabel.setText("");
            this.auidValueLabel.setVisible(false);
            this.requestAuidButton.setVisible(true);
            this.checkVsxButton.setVisible(false);
        }
        
        // Update magnitude range display from VSX data
        // Use SwingUtilities.invokeLater to ensure VSX data is fully processed
        SwingUtilities.invokeLater(() -> updateMagRangeDisplay());
        
        // Show sequence check button only when VSX data is available
        if (this.db.isVsxDataAvailable()) {
            this.checkSeqButton.setVisible(true);
        }
    }
    
    /**
     * Update magnitude range display from VSX data
     */
    private void updateMagRangeDisplay() {
        try {
            System.out.println("DEBUG: updateMagRangeDisplay() called");
            System.out.println("DEBUG: VSX data available: " + this.db.isVsxDataAvailable());
            
            // Check if the varMax and varMin arrays exist first
            if (!this.db.isVsxDataAvailable()) {
                System.out.println("DEBUG: VSX data not available - hiding label");
                this.magRangeLabel.setVisible(false);
                return;
            }
            
            // Try to get magnitude data from VSX details string instead of arrays
            String vsxDetails = this.db.getVsxDetails();
            System.out.println("DEBUG: VSX details = '" + vsxDetails + "'");
            
            if (vsxDetails != null && vsxDetails.contains("Max:") && vsxDetails.contains("Min:")) {
                // Parse directly from VSX details string
                try {
                    String maxMagStr = extractValueFromVSX(vsxDetails, "Max:");
                    String minMagStr = extractValueFromVSX(vsxDetails, "Min:");
                    
                    if (maxMagStr != null && minMagStr != null) {
                        displayMagnitudeRange(maxMagStr, minMagStr);
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: Error parsing VSX details: " + e.getMessage());
                }
            }
            
            // Fallback: try to get from varMax/varMin arrays
            String maxMagStr = null;
            String minMagStr = null;
            
            try {
                maxMagStr = this.db.getVarMax(0);
                minMagStr = this.db.getVarMin(0);
            } catch (Exception e) {
                System.out.println("DEBUG: Error accessing varMax/varMin arrays: " + e.getMessage());
                this.magRangeLabel.setVisible(false);
                return;
            }
            
            System.out.println("DEBUG: maxMagStr = '" + maxMagStr + "'");
            System.out.println("DEBUG: minMagStr = '" + minMagStr + "'");
            
            if (maxMagStr != null && !maxMagStr.trim().isEmpty() && 
                minMagStr != null && !minMagStr.trim().isEmpty()) {
                
                displayMagnitudeRange(maxMagStr, minMagStr);
                
            } else {
                // No magnitude data available - hide the label
                System.out.println("DEBUG: Magnitude data not available - hiding label");
                this.magRangeLabel.setVisible(false);
            }
        } catch (Exception e) {
            // If there's any error parsing magnitudes, just hide the label
            System.out.println("DEBUG: Exception in updateMagRangeDisplay: " + e.getMessage());
            e.printStackTrace();
            this.magRangeLabel.setVisible(false);
        }
    }
    
    /**
     * Extract magnitude value from VSX details string
     */
    private String extractValueFromVSX(String vsxDetails, String key) {
        int startIndex = vsxDetails.indexOf(key);
        if (startIndex == -1) return null;
        
        startIndex += key.length();
        
        // Find the next occurrence of two spaces or end of string
        int endIndex = vsxDetails.indexOf("  ", startIndex);
        if (endIndex == -1) endIndex = vsxDetails.length();
        
        String value = vsxDetails.substring(startIndex, endIndex).trim();
        System.out.println("DEBUG: extractValueFromVSX('" + key + "') = '" + value + "'");
        return value;
    }
    
    /**
     * Display the magnitude range from MaxMag and MinMag strings
     */
    private void displayMagnitudeRange(String maxMagStr, String minMagStr) {
        try {
            System.out.println("DEBUG: displayMagnitudeRange called with maxMag='" + maxMagStr + "', minMag='" + minMagStr + "'");
            
            // Parse MaxMag (e.g., "12.04 V" -> 12.04)
            String maxMagNumStr = maxMagStr.replaceAll("[^0-9.]", "");
            double maxMag = Double.parseDouble(maxMagNumStr);
            
            // Parse MinMag - handle both standard format and amplitude format
            double minMag;
            String minMagTrimmed = minMagStr.trim();
            
            if (minMagTrimmed.startsWith("(") && minMagTrimmed.contains(")")) {
                // Amplitude format: "(0.76) V" means MinMag = MaxMag + amplitude
                String amplitudeStr = minMagTrimmed.substring(1, minMagTrimmed.indexOf(")")).trim();
                double amplitude = Double.parseDouble(amplitudeStr);
                minMag = maxMag + amplitude;
                System.out.println("DEBUG: Amplitude format detected - MaxMag=" + maxMag + ", amplitude=" + amplitude + ", calculated MinMag=" + minMag);
            } else {
                // Standard format: "14.0 V" or "14.0: V"
                String minMagNumStr = minMagTrimmed.replaceAll("[^0-9.]", "");
                minMag = Double.parseDouble(minMagNumStr);
                System.out.println("DEBUG: Standard format - MaxMag=" + maxMag + ", MinMag=" + minMag);
            }
            
            // Calculate magnitude range (MaxMag - MinMag)
            // Note: In astronomy, brighter = smaller magnitude number
            // So range = MinMag - MaxMag (faintest - brightest)
            double magRange = minMag - maxMag;
            
            // Display the range
            String displayText = String.format("Mag Range: %.2f - %.2f", maxMag, minMag);
            System.out.println("DEBUG: Setting mag range label to: '" + displayText + "'");
            this.magRangeLabel.setText(displayText);
            this.magRangeLabel.setVisible(true);
            
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in displayMagnitudeRange: " + e.getMessage());
            e.printStackTrace();
            this.magRangeLabel.setVisible(false);
        }
    }

    /**
     * Check API status for all external catalog endpoints in background thread.
     * Updates status indicators with green (fast), orange (slow), or red (unavailable).
     */
    private void checkAPIStatus() {
        // Define API endpoints to check - use actual catalog queries that work
        final String[] endpoints = {
            "http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=SELECT+TOP+1+RAJ2000,DEJ2000+FROM+\"II/336/apass9\"+WHERE+RAJ2000+BETWEEN+0+AND+1",  // APASS9
            "https://gea.esac.esa.int/tap-server/tap/sync?REQUEST=doQuery&LANG=ADQL&FORMAT=json&QUERY=SELECT+TOP+1+ra,dec+FROM+gaiadr2.gaia_source+WHERE+ra+BETWEEN+0+AND+1",       // Gaia DR2
            "https://catalogs.mast.stsci.edu/api/v0.1/panstarrs/dr2/mean.json?ra=180&dec=0&radius=0.001&nDetections.gte=1&pagesize=1", // PanSTARRS
            "http://simbad.u-strasbg.fr/simbad/sim-id?output.format=votable&Ident=M1",  // SIMBAD
            "https://www.aavso.org/vsx/index.php?view=api.object&ident=SS+Cyg",  // AAVSO VSX API
            "http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=SELECT+TOP+1+RAJ2000+FROM+\"I/355/gaiadr3\"+WHERE+RAJ2000+BETWEEN+0+AND+1"  // VizieR (Gaia DR3)
        };
        
        final JLabel[] statusLabels = {
            this.apass9StatusLabel,
            this.gaiaDR2StatusLabel,
            this.panstarrsStatusLabel,
            this.simbadStatusLabel,
            this.aavsoStatusLabel,
            this.vizierStatusLabel
        };
        
        // Check each endpoint in a separate thread to avoid blocking UI
        new Thread(() -> {
            for (int i = 0; i < endpoints.length; i++) {
                final int index = i;
                final String endpoint = endpoints[i];
                
                try {
                    long startTime = System.currentTimeMillis();
                    java.net.URL url = new java.net.URL(endpoint);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000); // 5 second timeout
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("User-Agent", "Seqplot/6.0.0");
                    
                    int responseCode = conn.getResponseCode();
                    long elapsed = System.currentTimeMillis() - startTime;
                    conn.disconnect();
                    
                    // Update UI on event dispatch thread
                    SwingUtilities.invokeLater(() -> {
                        if (responseCode >= 200 && responseCode < 400) {
                            if (elapsed < 2000) {
                                // Fast response (< 2 seconds) - Green
                                statusLabels[index].setForeground(new java.awt.Color(0, 150, 0));
                                statusLabels[index].setToolTipText(String.format("Online (~%dms)", elapsed));
                            } else {
                                // Slow response (2-5 seconds) - Orange
                                statusLabels[index].setForeground(new java.awt.Color(255, 140, 0));
                                statusLabels[index].setToolTipText(String.format("Slow (~%dms)", elapsed));
                            }
                        } else {
                            // Error response - Red
                            statusLabels[index].setForeground(java.awt.Color.RED);
                            statusLabels[index].setToolTipText(String.format("Error (HTTP %d)", responseCode));
                        }
                    });
                    
                } catch (Exception e) {
                    // Connection failed - Red
                    SwingUtilities.invokeLater(() -> {
                        statusLabels[index].setForeground(java.awt.Color.RED);
                        statusLabels[index].setToolTipText("Unavailable: " + e.getMessage());
                    });
                }
            }
        }).start();
    }

    public void closeWindow() {
        this.dispose();
    }
}

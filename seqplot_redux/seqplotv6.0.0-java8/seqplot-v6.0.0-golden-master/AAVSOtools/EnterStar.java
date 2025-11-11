/*
 * Decompiled with CFR 0.152.
 */
package AAVSOtools;

import AAVSOtools.DataConnector;
import AAVSOtools.GBC;
import AAVSOtools.Seqplot;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EnterStar
extends JDialog
implements ActionListener,
FocusListener {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 580;
    private String starnameText = "Star Name: ";
    private String raText = "RA: ";
    private String decText = "Dec: ";
    private String sizeText = "Field Size: ";
    private String limitingMagText = "Limiting Mag: ";
    private String raUnitsText = "HH MM SS.SS or degs.";
    private String decUnitsText = "(-)DD MM SS.SS or degs.";
    private String sizeUnitsText = "arcminutes";
    private String okText = "Find RA & Dec for Star";
    private String ok2Text = "Get Plot";
    private String quitText = "Quit";
    private String closeText = "Cancel";
    private JTextField starnameField = new JTextField();
    private JTextField raField = new JTextField();
    private JTextField decField = new JTextField();
    private JTextField sizeField = new JTextField();
    private JTextField limitingMagField = new JTextField();
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
    private Frame frame;
    private DataConnector db;
    private Seqplot seqplot;

    public EnterStar(DataConnector dcon, Seqplot sp, Frame frame, String defaultStar) {
        this(dcon, sp, frame, "Sequence Plotter" + sp.getVersion() + " - Request Star", true, defaultStar);
    }

    public EnterStar(DataConnector dcon, Seqplot sp, Frame frame, String title, Boolean mode, String defaultStar) {
        super(frame, "Sequence Plotter" + sp.getVersion() + " - Request Star", true);
        this.db = dcon;
        this.seqplot = sp;
        this.setSize(500, 580);
        this.setModal(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int)(dim.getWidth() / 3.0), (int)(dim.getHeight() / 5.0));
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
        this.setupCheckBox(this.apass9Box, "APASS9", this.db.getApass9BoxSelected(), "AAVSO Photometric All-Sky Survey 9");
        
        catalogPane.add((Component)catalogLabel, new GBC(0, 0, 2, 1).setWeight(100.0, 0.0).setFill(2).setInsets(10, 5, 0, 5));
        catalogPane.add((Component)this.apassBox, new GBC(0, 2, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.bsmBox, new GBC(0, 3, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.bsm_SBox, new GBC(0, 4, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.coker30Box, new GBC(0, 5, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.gcpdBox, new GBC(0, 6, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.nofsBox, new GBC(0, 7, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.k35Box, new GBC(0, 8, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 10, 5));
        catalogPane.add((Component)this.oc61Box, new GBC(1, 2, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.sonoitaBox, new GBC(1, 3, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.sro50Box, new GBC(1, 4, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.tmo61Box, new GBC(1, 5, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.tychoBox, new GBC(1, 6, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.w28Box, new GBC(1, 7, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        catalogPane.add((Component)this.w30Box, new GBC(1, 8, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 10, 5));
        
        // External data sources panel
        externalPane.add((Component)externalLabel, new GBC(0, 0, 2, 1).setWeight(100.0, 0.0).setFill(2).setInsets(10, 5, 0, 5));
        externalPane.add((Component)this.apass9Box, new GBC(0, 1, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 10, 5));
        
        JLabel starnameLabel = new JLabel(this.starnameText);
        this.setupField(this.starnameField, defaultStar, 15);
        this.getRaDecButton = new JButton(this.okText);
        this.getRaDecButton.addActionListener(this);
        starnamePane.add((Component)starnameLabel, new GBC(0, 0, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        starnamePane.add((Component)this.starnameField, new GBC(1, 0, 2, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        starnamePane.add((Component)this.getRaDecButton, new GBC(1, 1, 2, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        JLabel raLabel = new JLabel(this.raText);
        this.setupField(this.raField, this.db.getRAText(), 25);
        JLabel raUnitsLabel = new JLabel(this.raUnitsText);
        JLabel decLabel = new JLabel(this.decText);
        this.setupField(this.decField, this.db.getDecText(), 25);
        JLabel decUnitsLabel = new JLabel(this.decUnitsText);
        JLabel sizeLabel = new JLabel(this.sizeText);
        this.setupField(this.sizeField, "" + Math.round(this.db.getFieldSize() * 60.0), 25);
        JLabel sizeUnitsLabel = new JLabel(this.sizeUnitsText);
        JLabel limitingMagLabel = new JLabel(this.limitingMagText);
        this.setupField(this.limitingMagField, "" + this.db.getLimitingMag(), 25);
        this.getPlotButton = new JButton(this.ok2Text);
        this.getPlotButton.addActionListener(this);
        coordPane.add((Component)raLabel, new GBC(0, 0, 1, 1).setWeight(100.0, 0.0));
        coordPane.add((Component)this.raField, new GBC(1, 0, 1, 1).setWeight(100.0, 0.0).setFill(2));
        coordPane.add((Component)raUnitsLabel, new GBC(2, 0, 1, 1).setWeight(100.0, 0.0));
        coordPane.add((Component)decLabel, new GBC(0, 1, 1, 1).setWeight(100.0, 0.0));
        coordPane.add((Component)this.decField, new GBC(1, 1, 1, 1).setWeight(100.0, 0.0).setFill(2));
        coordPane.add((Component)decUnitsLabel, new GBC(2, 1, 1, 1).setWeight(100.0, 0.0));
        coordPane.add((Component)sizeLabel, new GBC(0, 2, 1, 1).setWeight(100.0, 0.0));
        coordPane.add((Component)this.sizeField, new GBC(1, 2, 1, 1).setWeight(100.0, 0.0).setFill(2));
        coordPane.add((Component)sizeUnitsLabel, new GBC(2, 2, 1, 1).setWeight(100.0, 0.0));
        coordPane.add((Component)limitingMagLabel, new GBC(0, 3, 1, 1).setWeight(100.0, 0.0));
        coordPane.add((Component)this.limitingMagField, new GBC(1, 3, 1, 1).setWeight(100.0, 0.0).setFill(2));
        coordPane.add((Component)this.getPlotButton, new GBC(1, 4, 1, 1).setWeight(100.0, 0.0).setFill(2).setInsets(5, 5, 0, 5));
        this.quitButton = new JButton(this.quitText);
        this.closeButton = new JButton(this.closeText);
        this.quitButton.addActionListener(this);
        this.closeButton.addActionListener(this);
        buttonPane.add((Component)this.closeButton, new GBC(0, 0, 1, 1).setWeight(100.0, 0.0));
        buttonPane.add((Component)this.quitButton, new GBC(1, 0, 1, 1).setWeight(100.0, 0.0));
        catalogPane.setBorder(BorderFactory.createEtchedBorder());
        externalPane.setBorder(BorderFactory.createEtchedBorder());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add((Component)catalogPane, new GBC(0, 0, 1, 1).setWeight(100.0, 0.0).setInsets(10, 5, 5, 5).setFill(2));
        mainPanel.add((Component)externalPane, new GBC(0, 1, 1, 1).setWeight(100.0, 0.0).setInsets(5, 5, 10, 5).setFill(2));
        mainPanel.add((Component)starnamePane, new GBC(0, 2, 1, 1).setWeight(100.0, 0.0).setInsets(10, 5, 10, 5).setFill(2));
        mainPanel.add((Component)coordPane, new GBC(0, 3, 1, 1).setWeight(100.0, 0.0).setInsets(10, 5, 10, 5).setFill(2));
        mainPanel.add((Component)buttonPane, new GBC(0, 4, 1, 1).setWeight(100.0, 0.0).setInsets(10, 5, 10, 5).setFill(2));
        this.add(mainPanel);
        this.setDefaultCloseOperation(2);
        this.setVisible(true);
    }

    private JTextField setupField(JTextField jtf, String name, int size) {
        jtf.setColumns(size);
        jtf.addActionListener(this);
        jtf.addFocusListener(this);
        jtf.setEditable(true);
        jtf.setText(name);
        return jtf;
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
                this.raField.setText("" + this.db.getCentralRA());
                this.decField.setText("" + this.db.getCentralDec());
                this.db.setUserDefaults(this.seqplot.getColorArray());
                this.getRootPane().setDefaultButton(this.getPlotButton);
            }
            this.setCursor(Cursor.getDefaultCursor());
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

    public void collectInputData() {
        this.setCatalogs();
        if (this.db.getCatalogString().equals("") && !this.db.getApass9BoxSelected()) {
            JOptionPane.showMessageDialog(null, "You must select at least one catalog.", "Missing Information", 2);
        } else if (this.raField.getText().equals("") || this.decField.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "You must have an RA and Dec for the field.", "Missing Information", 2);
        } else {
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
                String size = JOptionPane.showInputDialog(this.frame, "Please enter the field size (in arcmins):", "Set Field Size", 3);
                this.sizeField.setText(size);
            }
            while (this.limitingMagField.getText().equals("0.0")) {
                String lm = JOptionPane.showInputDialog(this.frame, "Please enter the limiting magnitude:", "Set Limiting Magnitude", 3);
                this.limitingMagField.setText(lm);
            }
            this.db.setFieldSize(this.stringToDouble(this.sizeField.getText()) / 60.0);
            this.db.setRAText(this.raField.getText());
            this.db.setDecText(this.decField.getText());
            this.db.setStar(this.starnameField.getText());
            this.db.setLimitingMag(this.stringToDouble(this.limitingMagField.getText()));
            this.db.setUserDefaults(this.seqplot.getColorArray());
            this.seqplot.setShowPlot(true);
            this.dispose();
        }
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
        this.db.setCatalogString(str);
    }

    public String addComma(String str) {
        if (!str.equals("")) {
            return ",";
        }
        return "";
    }

    public void closeWindow() {
        this.dispose();
    }
}

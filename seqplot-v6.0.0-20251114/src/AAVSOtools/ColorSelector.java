/*
 * Decompiled with CFR 0.152.
 */
package AAVSOtools;

import AAVSOtools.DataConnector;
import AAVSOtools.Seqplot;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ColorSelector
extends JDialog
implements ActionListener {
    private static final double FRAME_WDTH_FACTOR = 0.25;
    private static final double FRAME_HT_FACTOR = 0.9;
    private static final int X_LOC = 5;
    private static final int Y_LOC = 5;
    private static final int COLOR_ARRAY_SIZE = 7;
    private JButton[] colorButton = new JButton[7];
    private Color[] color = new Color[7];
    private JLabel titleLabel;
    private JButton seqplotColorsButton;
    private JButton previewButton;
    private JButton resetButton;
    private JButton cancelButton;
    private JButton saveButton;
    private JPanel colorPane;
    private JPanel buttonPane;
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    private Seqplot seqplot;
    private DataConnector db;

    public ColorSelector(Seqplot plot, DataConnector data, Frame frame, Color[] color) {
        this(plot, data, frame, "Color Chooser", true, color);
    }

    public ColorSelector(Seqplot plot, DataConnector data, Frame frame, String title, Boolean mode, Color[] color) {
        super(frame, "Color Chooser", true);
        this.seqplot = plot;
        this.db = data;
        this.setSize((int)(this.dim.getWidth() * 0.25), (int)(this.dim.getHeight() * 0.9));
        this.setLocation(5, 5);
        this.setModal(true);
        Container cp = this.getContentPane();
        cp.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        this.titleLabel = new JLabel("  Click on a color to change it.");
        c.fill = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(0, 2, 0, 0);
        cp.add((Component)this.titleLabel, c);
        this.colorPane = new JPanel(new GridLayout(7, 2));
        this.colorButton[5] = new JButton(new String(this.getSeriesText(5)));
        this.colorButton[5].setBackground(color[5]);
        this.colorButton[5].addActionListener(this);
        this.colorPane.add(this.colorButton[5]);
        this.colorButton[6] = new JButton(new String(this.getSeriesText(6)));
        this.colorButton[6].setBackground(color[6]);
        this.colorButton[6].addActionListener(this);
        this.colorPane.add(this.colorButton[6]);
        int i = 0;
        while (i < 5) {
            this.colorButton[i] = new JButton(new String(this.getSeriesText(i)));
            this.colorButton[i].setBackground(color[i]);
            this.colorButton[i].addActionListener(this);
            this.colorPane.add(this.colorButton[i]);
            ++i;
        }
        c.fill = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 7;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(0, 2, 0, 2);
        cp.add((Component)this.colorPane, c);
        this.buttonPane = new JPanel(new GridLayout(3, 2));
        this.seqplotColorsButton = new JButton("Default Colors");
        this.seqplotColorsButton.addActionListener(this);
        this.previewButton = new JButton("Preview");
        this.previewButton.addActionListener(this);
        this.resetButton = new JButton("Reset");
        this.resetButton.addActionListener(this);
        this.cancelButton = new JButton("Cancel");
        this.cancelButton.addActionListener(this);
        this.getRootPane().setDefaultButton(this.saveButton);
        this.saveButton = new JButton("Save");
        this.saveButton.addActionListener(this);
        this.buttonPane.add(this.seqplotColorsButton);
        this.buttonPane.add(this.previewButton);
        this.buttonPane.add(this.resetButton);
        this.buttonPane.add(this.cancelButton);
        this.buttonPane.add(this.saveButton);
        c.fill = 2;
        c.anchor = 26;
        c.gridx = 0;
        c.gridy = 7;
        c.gridheight = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(0, 0, 0, 0);
        cp.add((Component)this.buttonPane, c);
        this.setDefaultCloseOperation(2);
        this.setVisible(true);
    }

    public String getSeriesText(int series) {
        switch (series) {
            case 0: {
                return "  Blue";
            }
            case 1: {
                return "  Green";
            }
            case 2: {
                return "  Red";
            }
            case 3: {
                return "  Yellow";
            }
            case 4: {
                return "  Unknown";
            }
            case 5: {
                return "  Background";
            }
            case 6: {
                return "  Crosshairs";
            }
        }
        return "  Unknown";
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Color saved;
        int i = 0;
        while (i < 7) {
            Color newColor = null;
            if (ae.getSource() == this.colorButton[i]) {
                newColor = JColorChooser.showDialog(this, "Choose Data Type Color", this.colorButton[i].getBackground());
            }
            if (newColor != null) {
                this.colorButton[i].setBackground(newColor);
                this.getRootPane().setDefaultButton(this.saveButton);
            }
            ++i;
        }
        if (ae.getSource() == this.seqplotColorsButton) {
            this.seqplot.setDefaultSeqplotColors();
            i = 0;
            while (i < 7) {
                this.colorButton[i].setBackground(this.seqplot.getPlotColor(i));
                ++i;
            }
            this.getRootPane().setDefaultButton(this.saveButton);
        }
        if (ae.getSource() == this.previewButton) {
            i = 0;
            while (i < 7) {
                this.seqplot.setColor(i, this.colorButton[i].getBackground());
                this.color[i] = this.colorButton[i].getBackground();
                ++i;
            }
            this.seqplot.setPreferredColors();
        }
        if (ae.getSource() == this.resetButton) {
            i = 0;
            while (i < 7) {
                saved = this.seqplot.getSavedColor(i);
                this.colorButton[i].setBackground(saved);
                this.seqplot.setColor(i, saved);
                this.color[i] = saved;
                ++i;
            }
            this.seqplot.setPreferredColors();
        }
        if (ae.getSource() == this.cancelButton) {
            i = 0;
            while (i < 7) {
                saved = this.seqplot.getSavedColor(i);
                this.colorButton[i].setBackground(saved);
                this.seqplot.setColor(i, saved);
                this.color[i] = saved;
                ++i;
            }
            this.seqplot.setPreferredColors();
            this.dispose();
        }
        if (ae.getSource() == this.saveButton) {
            i = 0;
            while (i < 7) {
                this.seqplot.setColor(i, this.colorButton[i].getBackground());
                this.color[i] = this.colorButton[i].getBackground();
                ++i;
            }
            this.seqplot.setPreferredColors();
            this.db.setUserDefaults(this.seqplot.getColorArray());
            this.dispose();
        }
        if (ae.getSource() == this.cancelButton) {
            this.dispose();
        }
    }
}

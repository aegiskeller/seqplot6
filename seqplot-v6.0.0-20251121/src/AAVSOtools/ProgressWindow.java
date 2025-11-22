/*
 * Decompiled with CFR 0.152.
 */
package AAVSOtools;

import AAVSOtools.DataConnector;
import AAVSOtools.GBC;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressWindow
extends JFrame
implements ActionListener {
    private DataConnector db;
    private JLabel waitLabel;
    private JButton cancelButton = new JButton();
    private JButton quitButton = new JButton();
    private JProgressBar pb = new JProgressBar();

    public ProgressWindow() {
        this.setTitle("Plotter - Loading Data...");
        this.setSize(300, 100);
        this.setBackground(Color.WHITE);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int)(dimension.getWidth() / 3.0), (int)(dimension.getHeight() / 3.0));
        this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        this.setLayout(new GridBagLayout());
        JPanel jPanel = new JPanel(new GridBagLayout());
        this.pb.setIndeterminate(true);
        jPanel.add((Component)this.pb, new GBC(1, 1, 1, 1).setWeight(100.0, 0.0).setAnchor(13));
        JPanel jPanel2 = new JPanel(new GridBagLayout());
        jPanel2.add((Component)this.setupButton(this.cancelButton, "Cancel"), new GBC(0, 1, 1, 1).setWeight(100.0, 0.0));
        jPanel2.add((Component)this.setupButton(this.quitButton, "Quit"), new GBC(1, 1, 1, 1).setWeight(100.0, 0.0));
        JPanel jPanel3 = new JPanel(new GridBagLayout());
        jPanel3.add((Component)jPanel, new GBC(0, 0, 1, 1).setWeight(100.0, 0.0).setInsets(10, 5, 10, 5).setFill(2));
        jPanel3.add((Component)jPanel2, new GBC(0, 3, 1, 1).setWeight(100.0, 0.0).setInsets(10, 5, 10, 5).setFill(2));
        this.add(jPanel3);
        this.setDefaultCloseOperation(2);
        this.setVisible(true);
    }

    public JButton setupButton(JButton jButton, String string) {
        jButton.addActionListener(this);
        jButton.setMnemonic(10);
        jButton.setText(string);
        return jButton;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.cancelButton) {
            this.db.setCancelSelected(true);
            this.dispose();
        }
        if (actionEvent.getSource() == this.quitButton) {
            System.exit(0);
        }
    }

    public void setWaitText(String string) {
        this.waitLabel.setText(string);
    }

    public void enableCancelButton(Boolean bl) {
        this.cancelButton.setEnabled(bl);
    }

    public void close() {
        this.dispose();
    }
}

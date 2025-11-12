package AAVSOtools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * PhotCompWindow - Photometric comparison window for analyzing catalog overlaps
 * 
 * Performs cross-matching between primary and secondary catalogs based on position,
 * then plots the matched V magnitudes for compariosn.
 */
public class PhotCompWindow extends JFrame {
    
    private DataConnector db;
    private PhotCompPanel plotPanel;
    private JPanel controlPanel;
    private Map<Integer, JCheckBox> catalogCheckBoxes;
    private JLabel statsLabel;
    private JTextField matchToleranceField;
    private DecimalFormat df2 = new DecimalFormat("0.00");
    private JLabel step1Label;
    private JLabel step2Label;
    private JLabel recommendationLabel;
    private JButton makeCorrectionButton;
    private JButton noCorrectionButton;
    private JPanel correctionButtonPanel;
    
    // Store cross-matched data
    private List<CrossMatch> crossMatches;
    private List<CrossMatch> crossMatchesFiltered; // After sigma clipping
    private Set<Integer> secondarySources;
    private int primarySource;
    
    // Match tolerance in degrees (default 1 arcsec = 0.000278 degrees)
    private double matchToleranceDeg = 0.000278;
    
    // Transition magnitude marking
    private Double transitionMag = null;
    
    // Offset correction tracking
    private double lastDeltaV = 0.0;
    private double lastRMS = 0.0;
    
    // Correction decision tracking
    private boolean correctionDecisionMade = false;
    
    /**
     * Inner class to store a cross-match between primary and secondary catalog
     */
    private static class CrossMatch {
        double primaryVmag;
        double secondaryVmag;
        int secondarySource;
        
        CrossMatch(double primaryVmag, double secondaryVmag, int secondarySource) {
            this.primaryVmag = primaryVmag;
            this.secondaryVmag = secondaryVmag;
            this.secondarySource = secondarySource;
        }
    }
    
    public PhotCompWindow(DataConnector db) {
        super("Photometric Comparison");
        this.db = db;
        this.crossMatches = new ArrayList<>();
        this.secondarySources = new HashSet<>();
        
        // Perform cross-matching
        performCrossMatching();
        
        // Check if we have enough cross-matches for meaningful analysis
        // beware of Drop Bears!
        if (crossMatches.size() < 10) {
            // Show popup and close window
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Too few cross-matches\nUse your judgement to select brighter stars from the shallow catalog and fainter stars from the deeper catalog", 
                    "Insufficient Cross-Matches", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close the photometric comparison window
            });
            return; // Exit constructor early
        }
        
        // Create UI
        setLayout(new BorderLayout());
        
        // Create top panel with match tolerance control and mark transition button
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Left side with label and field
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel("Match tolerance (arcsec):"));
        // Format tolerance to 1 decimal place
        DecimalFormat df1 = new DecimalFormat("0.0");
        matchToleranceField = new JTextField(df1.format(matchToleranceDeg * 3600));
        matchToleranceField.setPreferredSize(new Dimension(80, matchToleranceField.getPreferredSize().height));
        leftPanel.add(matchToleranceField);
        topPanel.add(leftPanel, BorderLayout.WEST);
        JButton rematchButton = new JButton("Re-match");
        rematchButton.addActionListener(e -> {
            try {
                double arcsec = Double.parseDouble(matchToleranceField.getText());
                matchToleranceDeg = arcsec / 3600.0;
                performCrossMatching();
                applySigmaClipping();
                plotPanel.calculateRanges();
                plotPanel.clearSelection();
                plotPanel.repaint();
                statsLabel.setText("<html><b>Statistics:</b><br>Select range on plot</html>");
                // Reset decision state
                correctionDecisionMade = false;
                recommendationLabel.setVisible(false);
                correctionButtonPanel.setVisible(false);
                step2Label.setVisible(false);
                plotPanel.setEnabled(true);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid tolerance value", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Right side with button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(rematchButton);
        topPanel.add(rightPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Create plot panel
        plotPanel = new PhotCompPanel();
        add(plotPanel, BorderLayout.CENTER);
        
        // Create control panel on the left
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add catalog selection checkboxes (only if 3 or more catalogs total: primary + 2+ secondary)
        // With 2 catalogs (primary [1] + 1 secondary [2]), no checkboxes needed
        // With 3+ catalogs (primary [1] + 2+ secondary [2],[3],...), show checkboxes for secondary catalogs
        // secondarySources now only contains non-primary sources after filtering in performCrossMatching
        if (secondarySources.size() >= 2) {
            JLabel catalogLabel = new JLabel("Visible Catalogs:");
            catalogLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            controlPanel.add(catalogLabel);
            controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            
            catalogCheckBoxes = new LinkedHashMap<>();
            
            // Create checkboxes for each secondary source
            List<Integer> sortedSources = new ArrayList<>(secondarySources);
            Collections.sort(sortedSources);
            
            for (Integer sourceId : sortedSources) {
                JCheckBox cb = new JCheckBox(getSourceName(sourceId), true);
                cb.setAlignmentX(Component.LEFT_ALIGNMENT);
                cb.addActionListener(e -> plotPanel.repaint());
                catalogCheckBoxes.put(sourceId, cb);
                controlPanel.add(cb);
                controlPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            }
        }
        
        // Add instruction labels
        controlPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        JLabel instructionsTitle = new JLabel("<html><b><font size='+1'>Instructions</font></b></html>");
        instructionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(instructionsTitle);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Step 1: Always visible
        step1Label = new JLabel("<html><div style='background-color: #E8F4F8; padding: 4px; border-radius: 4px; width: 280px; font-size: 9px; text-align: justify; line-height: 1.1;'>" +
            "<div style='text-align: center; margin-bottom: 2px;'><b><font color='#0066CC'>Step 1:</font></b></div>" +
            "Select the cross-over region between catalogs. " +
            "This is the 'sweet spot' where the photometry " +
            "of the bright catalog is not too noisy and " +
            "where the fainter catalog is not suffering " +
            "from linearity issues.<br><br>" +
            "<div style='text-align: center; font-style: italic;'>Drag to select a region on the plot.</div></div></html>");
        step1Label.setAlignmentX(Component.LEFT_ALIGNMENT);
        step1Label.setVerticalAlignment(SwingConstants.TOP);
        step1Label.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
        controlPanel.add(step1Label);
        
        // Stats label (appears after selection)
        controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        statsLabel = new JLabel("<html><div style='background-color: #F0F0F0; padding: 4px; width: 280px; font-size: 9px; text-align: center;'>" +
            "<b>Statistics:</b><br>Waiting for selection...</div></html>");
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsLabel.setVerticalAlignment(SwingConstants.TOP);
        statsLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 2, 0, 2),
            BorderFactory.createLineBorder(Color.GRAY, 1)
        ));
        controlPanel.add(statsLabel);
        
        // Recommendation label (appears after stats)
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        recommendationLabel = new JLabel("<html><div style='background-color: #FFF9E6; padding: 4px; width: 280px; font-size: 9px; text-align: center;'>" +
            "<b>Recommendation:</b><br>Waiting for statistics...</div></html>");
        recommendationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        recommendationLabel.setVerticalAlignment(SwingConstants.TOP);
        recommendationLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 2, 0, 2),
            BorderFactory.createLineBorder(new Color(255, 193, 7), 2)
        ));
        recommendationLabel.setVisible(false);
        controlPanel.add(recommendationLabel);
        
        // Correction decision buttons
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        correctionButtonPanel = new JPanel();
        correctionButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        correctionButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        makeCorrectionButton = new JButton("Make Correction");
        makeCorrectionButton.setPreferredSize(new Dimension(135, 35));
        makeCorrectionButton.addActionListener(e -> {
            correctionDecisionMade = true;
            makeCorrectionButton.setEnabled(false);
            noCorrectionButton.setEnabled(false);
            step2Label.setVisible(true);
        });
        correctionButtonPanel.add(makeCorrectionButton);
        
        noCorrectionButton = new JButton("No Correction");
        noCorrectionButton.setPreferredSize(new Dimension(135, 35));
        noCorrectionButton.addActionListener(e -> {
            correctionDecisionMade = true;
            makeCorrectionButton.setEnabled(false);
            noCorrectionButton.setEnabled(false);
            // Set ΔV and RMS to 0 for no correction
            lastDeltaV = 0.0;
            lastRMS = 0.0;
            // Progress to Step 2 to define transition magnitude
            step2Label.setVisible(true);
        });
        correctionButtonPanel.add(noCorrectionButton);
        
        correctionButtonPanel.setVisible(false);
        controlPanel.add(correctionButtonPanel);
        
        // Step 2: Hidden until "Make Correction" or "No Correction" is clicked
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        step2Label = new JLabel("<html><div style='background-color: #E8F4F8; padding: 4px; border-radius: 4px; width: 280px; font-size: 9px; text-align: justify; line-height: 1.1;'>" +
            "<div style='text-align: center; margin-bottom: 2px;'><b><font color='#0066CC'>Step 2:</font></b></div>" +
            "Click the plot to set the transition point. " +
            "Fainter than this: use deeper catalog. " +
            "Brighter than this: use shallower catalog.<br><br>" +
            "Correction (ΔV) will be applied automatically " +
            "to the fainter catalog magnitudes.</div></html>");
        step2Label.setAlignmentX(Component.LEFT_ALIGNMENT);
        step2Label.setVerticalAlignment(SwingConstants.TOP);
        step2Label.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
        step2Label.setVisible(false);
        controlPanel.add(step2Label);
        
        // Always add control panel to show instructions
        // Use a scroll pane to handle overflow
        JScrollPane controlScrollPane = new JScrollPane(controlPanel);
        controlScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        controlScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        controlScrollPane.setBorder(BorderFactory.createEmptyBorder());
        controlScrollPane.setPreferredSize(new Dimension(400, 700));  // Increased from 320 to 400 (25% wider)
        add(controlScrollPane, BorderLayout.WEST);
        
        // Set window properties
        setSize(1300, 700);  // Increased from 1200 to 1300 to accommodate wider control panel
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
    
    /**
     * Perform cross-matching between primary and secondary catalogs
     */
    private void performCrossMatching() {
        crossMatches.clear();
        secondarySources.clear();
        
        int totalCount = db.getTotalCount();
        
        // Count stars per source to understand data distribution
        Map<Integer, Integer> sourceCount = new HashMap<>();
        
        // Find all unique source IDs and determine which is primary
        Set<Integer> allSources = new HashSet<>();
        primarySource = -1;
        
        for (int i = 0; i < totalCount; i++) {
            int src = db.getSource(i);
            
            if (src > 0) {
                allSources.add(src);
                sourceCount.put(src, sourceCount.getOrDefault(src, 0) + 1);
                if (primarySource == -1) {
                    primarySource = src; // First source encountered
                }
            }
        }
        
        // Aslo check secondaryCatalogData
        List<DataConnector.CatalogEntry> secondaryData = db.getSecondaryCatalogData();
        
        // Check if we can use secondary catalog data as the "other" catalog
        // beware of Drop Bears!
        if (allSources.size() < 2 && secondaryData.size() == 0) {
            JOptionPane.showMessageDialog(this, 
                "PhotComp requires at least 2 catalogs to be loaded.\n" +
                "Please load another catalag and try again.", 
                "Need Multiple Catalogues", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Count how many primary stars we have
        int primaryCount = 0;
        int secondaryCount = 0;
        for (int i = 0; i < totalCount; i++) {
            if (db.getSource(i) == primarySource) {
                primaryCount++;
            } else if (db.getSource(i) > 0) {
                secondaryCount++;
            }
        }
        
        // Determine if we need to use secondaryCatalogData
        boolean useSecondaryCatalogData = (allSources.size() < 2 && secondaryData.size() > 0);
        
        // Perform the actual cross-matching
        int debugCount = 0;
        int primaryProcessed = 0;
        for (int i = 0; i < totalCount; i++) {
            int srcI = db.getSource(i);
            
            // Skip if not from primary catalog
            if (srcI != primarySource) continue;
            
            primaryProcessed++;
            
            double ra1 = db.getRa(i);
            double dec1 = db.getDec(i);
            double vmag1 = db.getVmag(i);
            
            // Skip invalid magnitude values
            // beware of Drop Bears!
            if (vmag1 < 0 || vmag1 > 30) {
                continue;
            }
            
            // Search for matches in other catalogs
            int matchesForThisStar = 0;
            
            if (useSecondaryCatalogData) {
                // Use secondaryCatalogData for matching
                for (int j = 0; j < secondaryData.size(); j++) {
                    DataConnector.CatalogEntry secEntry = secondaryData.get(j);
                    
                    if (secEntry.vmag < 0 || secEntry.vmag > 30) continue;
                    
                    double deltaDeg = angularSeparation(ra1, dec1, secEntry.ra, secEntry.dec);
                    
                    if (deltaDeg <= matchToleranceDeg) {
                        matchesForThisStar++;
                        crossMatches.add(new CrossMatch(vmag1, secEntry.vmag, secEntry.source));
                        secondarySources.add(secEntry.source);
                    }
                }
            } else {
                // Use main arrays for matching
                for (int j = 0; j < totalCount; j++) {
                    int srcJ = db.getSource(j);
                    
                    // Skip if same catalog as primary
                    if (srcJ == primarySource) continue;
                    
                    double ra2 = db.getRa(j);
                    double dec2 = db.getDec(j);
                    double vmag2 = db.getVmag(j);
                    
                    if (vmag2 < 0 || vmag2 > 30) continue;
                    
                    // Calculate angular separation
                    double deltaDeg = angularSeparation(ra1, dec1, ra2, dec2);
                    
                    if (deltaDeg <= matchToleranceDeg) {
                        matchesForThisStar++;
                        crossMatches.add(new CrossMatch(vmag1, vmag2, srcJ));
                        secondarySources.add(srcJ);
                    }
                }
            }
        }
        
        // Aplpy sigma clipping to remove outliers
        applySigmaClipping();
    }
    
    /**
     * Apply 3-sigma clipping to remove outliers from the delta-V measurements
     */
    private void applySigmaClipping() {
        if (crossMatches.isEmpty()) {
            crossMatchesFiltered = new ArrayList<>();
            return;
        }
        
        // Calculate mean and standard deviation of deltaV
        double sum = 0.0;
        for (CrossMatch match : crossMatches) {
            double deltaV = match.primaryVmag - match.secondaryVmag;
            sum += deltaV;
        }
        double mean = sum / crossMatches.size();
        
        double sumSq = 0.0;
        for (CrossMatch match : crossMatches) {
            double deltaV = match.primaryVmag - match.secondaryVmag;
            double diff = deltaV - mean;
            sumSq += diff * diff;
        }
        double stdDev = Math.sqrt(sumSq / crossMatches.size());
        
        // Filter out points beyond 3 sigma
        crossMatchesFiltered = new ArrayList<>();
        int outlierCount = 0;
        for (CrossMatch match : crossMatches) {
            double deltaV = match.primaryVmag - match.secondaryVmag;
            if (Math.abs(deltaV - mean) <= 3.0 * stdDev) {
                crossMatchesFiltered.add(match);
            } else {
                outlierCount++;
            }
        }
    }
    
    /**
     * Calculate angular separation between two positions in degrees
     */
    private double angularSeparation(double ra1, double dec1, double ra2, double dec2) {
        // Convert to radians
        double ra1Rad = Math.toRadians(ra1);
        double dec1Rad = Math.toRadians(dec1);
        double ra2Rad = Math.toRadians(ra2);
        double dec2Rad = Math.toRadians(dec2);
        
        // Haversine formula
        double dra = ra2Rad - ra1Rad;
        double ddec = dec2Rad - dec1Rad;
        
        double a = Math.sin(ddec/2) * Math.sin(ddec/2) +
                   Math.cos(dec1Rad) * Math.cos(dec2Rad) *
                   Math.sin(dra/2) * Math.sin(dra/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return Math.toDegrees(c);
    }
    
    /**
     * Get a human-readable name for a source ID
     */
    private String getSourceName(int sourceId) {
        switch (sourceId) {
            case 1: return "Tycho-2";  // AAVSO table containing Tycho-2 data
            case 21: return "SDSS-DR12";
            case 29: return "APASS";
            case 46: return "PanSTARRS";
            case 48: return "Gaia DR2";
            case 901: return "Tycho-2";
            default:
                // AAVSO catalogs (1-44)
                if (sourceId >= 1 && sourceId <= 44) {
                    return "AAVSO Cat " + sourceId;
                }
                return "Source " + sourceId;
        }
    }
    
    /**
     * Check if a catalog should be displayed
     */
    private boolean isCatalogVisible(int sourceId) {
        if (catalogCheckBoxes == null || !catalogCheckBoxes.containsKey(sourceId)) {
            return true; // Show by default if no checkbox
        }
        return catalogCheckBoxes.get(sourceId).isSelected();
    }
    
    /**
     * Inner class for the plot panel
     */
    private class PhotCompPanel extends JPanel {
        
        private double selectionStartX = -1;
        private double selectionEndX = -1;
        private boolean selecting = false;
        
        // Axis ranges
        private double minX, maxX, minY, maxY;
        private double rangeX, rangeY;
        
        // Margins
        private static final int MARGIN_LEFT = 60;
        private static final int MARGIN_RIGHT = 20;
        private static final int MARGIN_TOP = 40;
        private static final int MARGIN_BOTTOM = 50;
        
        public PhotCompPanel() {
            setBackground(Color.WHITE);
            
            // Calculate data ranges
            calculateRanges();
            
            // Add mouse listeners for seleciton and transition marking
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isInPlotArea(e.getX(), e.getY())) {
                        // If selection is locked (stats already calculated), clicking sets transition
                        if (selectionStartX >= 0 && selectionEndX >= 0 && !selecting) {
                            // Only allow transition marking if correction decision was made
                            if (!correctionDecisionMade) {
                                JOptionPane.showMessageDialog(PhotCompWindow.this,
                                    "Please make a correction decision first:\n" +
                                    "Click 'Make Correction' or 'No Correction'",
                                    "Decision Required",
                                    JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            
                            transitionMag = screenToDataX(e.getX());
                            System.out.println("DEBUG PhotComp: Transition magnitude marked at V=" + String.format("%.2f", transitionMag));
                            
                            // Store transition magnitude in DataConnector for filtering
                            db.setTransitionMagnitude(transitionMag);
                            
                            // Apply offset correction immediately
                            db.setOffsetCorrectionEnabled(true);
                            db.setOffsetCorrectionDeltaV(lastDeltaV);
                            db.setOffsetCorrectionRMS(lastRMS);
                            
                            // Notify user that transition magnitude is set and correction applied
                            String correctionMessage = (lastDeltaV != 0.0) ?
                                String.format("\nOffset correction ΔV=%.3f (RMS=%.3f) will be applied\nto the fainter catalog.", lastDeltaV, lastRMS) :
                                "\nNo offset correction will be applied (ΔV=0).";
                            
                            JOptionPane.showMessageDialog(PhotCompWindow.this,
                                String.format("Transition magnitude set to V=%.2f\n\n" +
                                    "Points View and Sky View will now show:\n" +
                                    "  • Brighter than V=%.2f: Shallow catalog\n" +
                                    "  • Fainter than V=%.2f: Deep catalog%s",
                                    transitionMag, transitionMag, transitionMag, correctionMessage),
                                "Transition Magnitude Set",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            repaint();
                        } else {
                            // Start new selection
                            selectionStartX = screenToDataX(e.getX());
                            selecting = true;
                        }
                    }
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (selecting && isInPlotArea(e.getX(), e.getY())) {
                        selectionEndX = screenToDataX(e.getX());
                        selecting = false;
                        
                        // Ensure start < end
                        if (selectionStartX > selectionEndX) {
                            double temp = selectionStartX;
                            selectionStartX = selectionEndX;
                            selectionEndX = temp;
                        }
                        
                        // Calculate statistics for selected range
                        calculateStatistics();
                        repaint();
                    }
                }
                
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (selecting) {
                        selectionEndX = screenToDataX(e.getX());
                        repaint();
                    }
                }
            };
            
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }
        
        /**
         * Clear the selection range
         */
        void clearSelection() {
            selectionStartX = -1;
            selectionEndX = -1;
        }
        
        /**
         * Calculate min/max ranges for the data (using filtered data)
         */
        void calculateRanges() {
            minX = Double.MAX_VALUE;
            maxX = -Double.MAX_VALUE;
            minY = Double.MAX_VALUE;
            maxY = -Double.MAX_VALUE;
            
            // Use filtered data (after sigma clipping)
            List<CrossMatch> dataToPlot = crossMatchesFiltered != null ? crossMatchesFiltered : crossMatches;
            
            for (CrossMatch match : dataToPlot) {
                if (!isCatalogVisible(match.secondarySource)) continue;
                
                // V magnitude plot: X = primary V, Y = ΔV
                double deltaV = match.primaryVmag - match.secondaryVmag;
                
                if (match.primaryVmag < minX) minX = match.primaryVmag;
                if (match.primaryVmag > maxX) maxX = match.primaryVmag;
                if (deltaV < minY) minY = deltaV;
                if (deltaV > maxY) maxY = deltaV;
            }
            
            // Handle case with no data
            if (minX > maxX) {
                minX = 0; maxX = 20;
            }
            if (minY > maxY) {
                minY = 0; maxY = 20;
            }
            
            // Round Y-axis: round down min to floor, round up max to ceiling
            minY = Math.floor(minY);
            maxY = Math.ceil(maxY);
            
            // Add padding for X axis only
            rangeX = maxX - minX;
            rangeY = maxY - minY;
            if (rangeX < 0.1) rangeX = 0.1;
            if (rangeY < 0.1) rangeY = 0.1;
            minX -= rangeX * 0.05;
            maxX += rangeX * 0.05;
            // No padding for Y axis since we want clean integer bounds
            rangeX = maxX - minX;
            rangeY = maxY - minY;
            
            System.out.println("DEBUG PhotComp ranges: X=[" + minX + ", " + maxX + "], Y=[" + minY + ", " + maxY + "]");
        }
        
        /**
         * Convert screen X coordinate to data X value
         */
        private double screenToDataX(int screenX) {
            int plotWidth = getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
            double fraction = (double)(screenX - MARGIN_LEFT) / plotWidth;
            return minX + fraction * rangeX;
        }
        
        /**
         * Convert data X value to screen X coordinate
         */
        private int dataToScreenX(double dataX) {
            int plotWidth = getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
            double fraction = (dataX - minX) / rangeX;
            return MARGIN_LEFT + (int)(fraction * plotWidth);
        }
        
        /**
         * Convert data Y value to screen Y coordinate
         */
        private int dataToScreenY(double dataY) {
            int plotHeight = getHeight() - MARGIN_TOP - MARGIN_BOTTOM;
            double fraction = (dataY - minY) / rangeY;
            return getHeight() - MARGIN_BOTTOM - (int)(fraction * plotHeight);
        }
        
        /**
         * Check if point is in plot area
         */
        private boolean isInPlotArea(int x, int y) {
            return x >= MARGIN_LEFT && x <= getWidth() - MARGIN_RIGHT &&
                   y >= MARGIN_TOP && y <= getHeight() - MARGIN_BOTTOM;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw axes
            drawAxes(g2);
            
            // Draw data points
            drawData(g2);
            
            // Draw selection rectangle
            if (selectionStartX >= 0 && selectionEndX >= 0) {
                drawSelection(g2);
            }
            
            // Draw transition magnitude line
            if (transitionMag != null) {
                drawTransitionLine(g2);
            }
        }
        
        /**
         * Draw axes and grid
         */
        private void drawAxes(Graphics2D g2) {
            int plotWidth = getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
            
            // Draw axes
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            
            // X-axis
            g2.drawLine(MARGIN_LEFT, getHeight() - MARGIN_BOTTOM,
                       MARGIN_LEFT + plotWidth, getHeight() - MARGIN_BOTTOM);
            
            // Y-axis
            g2.drawLine(MARGIN_LEFT, MARGIN_TOP,
                       MARGIN_LEFT, getHeight() - MARGIN_BOTTOM);
            
            // Draw grid and labels
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(1));
            Font labelFont = new Font("SansSerif", Font.PLAIN, 10);
            g2.setFont(labelFont);
            FontMetrics fm = g2.getFontMetrics();
            
            // X-axis ticks and labels
            int numXTicks = 10;
            for (int i = 0; i <= numXTicks; i++) {
                double dataX = minX + (rangeX * i / numXTicks);
                int screenX = dataToScreenX(dataX);
                
                // Grid line
                g2.drawLine(screenX, MARGIN_TOP, screenX, getHeight() - MARGIN_BOTTOM);
                
                // Tick and label
                g2.setColor(Color.BLACK);
                g2.drawLine(screenX, getHeight() - MARGIN_BOTTOM,
                           screenX, getHeight() - MARGIN_BOTTOM + 5);
                String label = df2.format(dataX);
                int labelWidth = fm.stringWidth(label);
                g2.drawString(label, screenX - labelWidth/2, getHeight() - MARGIN_BOTTOM + 18);
                g2.setColor(Color.LIGHT_GRAY);
            }
            
            // Y-axis ticks and labels
            // Choose decimal format based on range to avoid duplicate labels
            DecimalFormat yFormatter;
            if (rangeY < 0.1) {
                yFormatter = new DecimalFormat("0.0000");
            } else if (rangeY < 1.0) {
                yFormatter = new DecimalFormat("0.000");
            } else if (rangeY < 10.0) {
                yFormatter = new DecimalFormat("0.00");
            } else {
                yFormatter = new DecimalFormat("0.0");
            }
            
            int numYTicks = 10;
            for (int i = 0; i <= numYTicks; i++) {
                double dataY = minY + (rangeY * i / numYTicks);
                int screenY = dataToScreenY(dataY);
                
                // Grid line
                g2.drawLine(MARGIN_LEFT, screenY, getWidth() - MARGIN_RIGHT, screenY);
                
                // Tick and label
                g2.setColor(Color.BLACK);
                g2.drawLine(MARGIN_LEFT - 5, screenY, MARGIN_LEFT, screenY);
                String label = yFormatter.format(dataY);
                int labelWidth = fm.stringWidth(label);
                g2.drawString(label, MARGIN_LEFT - labelWidth - 8, screenY + 4);
                g2.setColor(Color.LIGHT_GRAY);
            }
            
            // Draw y=0 reference line (if it's within the visible range)
            if (minY <= 0.0 && maxY >= 0.0) {
                int zeroY = dataToScreenY(0.0);
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                            10.0f, new float[]{5.0f, 5.0f}, 0.0f)); // Dashed line
                g2.drawLine(MARGIN_LEFT, zeroY, getWidth() - MARGIN_RIGHT, zeroY);
                
                // Add "y=0" label
                g2.setFont(labelFont);
                g2.drawString("y=0", getWidth() - MARGIN_RIGHT + 5, zeroY + 4);
            }
            
            // Axis labels
            g2.setColor(Color.BLACK);
            Font axisFont = new Font("SansSerif", Font.BOLD, 12);
            g2.setFont(axisFont);
            fm = g2.getFontMetrics();
            
            String xLabel = getSourceName(primarySource) + " V magnitude";
            int xLabelWidth = fm.stringWidth(xLabel);
            g2.drawString(xLabel, MARGIN_LEFT + plotWidth/2 - xLabelWidth/2,
                         getHeight() - 10);
            
            // Y-axis label with catalog names
            String primaryName = getSourceName(primarySource);
            StringBuilder secondaryNames = new StringBuilder();
            int count = 0;
            for (Integer secSource : secondarySources) {
                if (count > 0) secondaryNames.append(", ");
                secondaryNames.append(getSourceName(secSource));
                count++;
            }
            String yLabel = "ΔV (" + primaryName + " V - " + secondaryNames.toString() + " V)";
            int yLabelWidth = fm.stringWidth(yLabel);
            // Draw rotated Y-axis label
            AffineTransform orig = g2.getTransform();
            g2.rotate(-Math.PI/2);
            g2.drawString(yLabel, -(getHeight() - MARGIN_BOTTOM + MARGIN_TOP)/2 - yLabelWidth/2, 15);
            g2.setTransform(orig);
            
            // Title
            Font titleFont = new Font("SansSerif", Font.BOLD, 14);
            g2.setFont(titleFont);
            String title = "Photometric Comparison: " + db.getStar();
            int titleWidth = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, getWidth()/2 - titleWidth/2, 25);
        }
        
        /**
         * Draw data points
         */
        private void drawData(Graphics2D g2) {
            // Color map for different sources
            Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, 
                            Color.ORANGE, Color.CYAN, Color.PINK};
            int colorIndex = 0;
            Map<Integer, Color> sourceColors = new HashMap<>();
            
            // Assign colors to sources
            List<Integer> sortedSources = new ArrayList<>(secondarySources);
            Collections.sort(sortedSources);
            for (Integer sourceId : sortedSources) {
                sourceColors.put(sourceId, colors[colorIndex % colors.length]);
                colorIndex++;
            }
            
            // Draw points (using filtered data)
            List<CrossMatch> dataToPlot = crossMatchesFiltered != null ? crossMatchesFiltered : crossMatches;
            
            for (CrossMatch match : dataToPlot) {
                if (!isCatalogVisible(match.secondarySource)) continue;
                
                g2.setColor(sourceColors.get(match.secondarySource));
                
                // V magnitude plot
                double deltaV = match.primaryVmag - match.secondaryVmag;
                int screenX = dataToScreenX(match.primaryVmag);
                int screenY = dataToScreenY(deltaV);
                
                if (isInPlotArea(screenX, screenY)) {
                    g2.fillOval(screenX - 3, screenY - 3, 6, 6);
                }
            }
            
            // Draw legend
            int legendX = getWidth() - MARGIN_RIGHT - 150;
            int legendY = MARGIN_TOP + 10;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            
            for (Integer sourceId : sortedSources) {
                if (!isCatalogVisible(sourceId)) continue;
                
                g2.setColor(sourceColors.get(sourceId));
                g2.fillOval(legendX, legendY - 4, 8, 8);
                g2.setColor(Color.BLACK);
                g2.drawString(getSourceName(sourceId), legendX + 15, legendY + 4);
                legendY += 20;
            }
        }
        
        /**
         * Draw selection rectangle
         */
        private void drawSelection(Graphics2D g2) {
            int startX = dataToScreenX(selectionStartX);
            int endX = dataToScreenX(selectionEndX);
            
            if (startX > endX) {
                int temp = startX;
                startX = endX;
                endX = temp;
            }
            
            g2.setColor(new Color(0, 0, 255, 50));
            g2.fillRect(startX, MARGIN_TOP, endX - startX,
                       getHeight() - MARGIN_BOTTOM - MARGIN_TOP);
            
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                                        BasicStroke.JOIN_MITER, 10.0f,
                                        new float[]{5.0f}, 0.0f));
            g2.drawRect(startX, MARGIN_TOP, endX - startX,
                       getHeight() - MARGIN_BOTTOM - MARGIN_TOP);
        }
        
        /**
         * Draw transition magnitude line
         */
        private void drawTransitionLine(Graphics2D g2) {
            int transX = dataToScreenX(transitionMag);
            
            // Draw vertical line
            g2.setColor(new Color(255, 0, 255)); // Magenta
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                                        BasicStroke.JOIN_MITER, 10.0f,
                                        new float[]{10.0f, 5.0f}, 0.0f));
            g2.drawLine(transX, MARGIN_TOP, transX, getHeight() - MARGIN_BOTTOM);
            
            // Draw label
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            String label = String.format("Transition: V=%.2f", transitionMag);
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            
            // Position label above the line
            g2.setColor(Color.WHITE);
            g2.fillRect(transX - labelWidth/2 - 3, MARGIN_TOP + 5, labelWidth + 6, 18);
            g2.setColor(new Color(255, 0, 255));
            g2.drawString(label, transX - labelWidth/2, MARGIN_TOP + 20);
        }
        
        /**
         * Perform sigma clipping on a list of values
         * Returns the clipped list after removing outliers beyond nsigma
         */
        private List<Double> sigmaClip(List<Double> values, double nsigma) {
            if (values.size() < 4) return values; // Need at least 4 points for meaningful clipping
            
            List<Double> clipped = new ArrayList<>(values);
            boolean changed = true;
            int maxIterations = 5;
            int iteration = 0;
            
            while (changed && iteration < maxIterations) {
                changed = false;
                iteration++;
                
                // Calculate mean
                double sum = 0;
                for (double v : clipped) sum += v;
                double mean = sum / clipped.size();
                
                // Calculate standard deviation
                double sumSq = 0;
                for (double v : clipped) {
                    double diff = v - mean;
                    sumSq += diff * diff;
                }
                double sigma = Math.sqrt(sumSq / clipped.size());
                
                // Remove outliers
                List<Double> newClipped = new ArrayList<>();
                for (double v : clipped) {
                    if (Math.abs(v - mean) <= nsigma * sigma) {
                        newClipped.add(v);
                    } else {
                        changed = true;
                    }
                }
                clipped = newClipped;
                
                if (clipped.size() < 4) break; // Stop if too few points remain
            }
            
            return clipped;
        }
        
        /**
         * Calculate statistics for selected magnitude range with sigma clipping
         */
        private void calculateStatistics() {
            if (selectionStartX < 0 || selectionEndX < 0) return;
            
            // Statistics for each comparison catalog
            Map<Integer, List<Double>> deltaValues = new HashMap<>();
            
            for (Integer source : secondarySources) {
                if (isCatalogVisible(source)) {
                    deltaValues.put(source, new ArrayList<>());
                }
            }
            
            // Collect delta values for stars in selected range (using filtered data)
            List<CrossMatch> dataToUse = crossMatchesFiltered != null ? crossMatchesFiltered : crossMatches;
            
            for (CrossMatch match : dataToUse) {
                if (!isCatalogVisible(match.secondarySource)) continue;
                
                // V magnitude plot
                if (match.primaryVmag >= selectionStartX && match.primaryVmag <= selectionEndX) {
                    double delta = match.primaryVmag - match.secondaryVmag;
                    deltaValues.get(match.secondarySource).add(delta);
                }
            }
            
            // Calculate statistics with sigma clipping
            StringBuilder stats = new StringBuilder("<html><div style='background-color: #F0F0F0; padding: 4px; width: 280px; font-size: 9px;'>");
            stats.append("<table width='100%' cellpadding='0' cellspacing='0' border='0'>");
            stats.append("<tr><td style='text-align: center; font-weight: bold;' colspan='2'>Statistics (3σ clipped)</td></tr>");
            stats.append(String.format("<tr><td colspan='2' style='text-align: center; padding: 2px;'>V mag Range: %.2f - %.2f</td></tr>", selectionStartX, selectionEndX));
            
            boolean foundOverlap = false;
            boolean storedValues = false;
            for (Map.Entry<Integer, List<Double>> entry : deltaValues.entrySet()) {
                List<Double> deltas = entry.getValue();
                if (deltas.isEmpty()) continue;
                
                foundOverlap = true;
                int sourceId = entry.getKey();
                
                // Apply 3-sigma clipping
                List<Double> clippedDeltas = sigmaClip(deltas, 3.0);
                int nRejected = deltas.size() - clippedDeltas.size();
                
                // Calculate mean
                double sum = 0;
                for (double d : clippedDeltas) sum += d;
                double mean = sum / clippedDeltas.size();
                
                // Calculate RMS
                double sumSq = 0;
                for (double d : clippedDeltas) {
                    double diff = d - mean;
                    sumSq += diff * diff;
                }
                double rms = Math.sqrt(sumSq / clippedDeltas.size());
                
                // Store values for the first (or only) secondary catalog
                if (!storedValues) {
                    lastDeltaV = mean;
                    lastRMS = rms;
                    storedValues = true;
                }
                
                // Two-column layout: Left = Statistics, Right = Offset
                stats.append("<tr><td style='text-align: left; padding: 2px 4px; vertical-align: top;'>");
                stats.append(String.format("<b>%s:</b><br>", getSourceName(sourceId)));
                stats.append(String.format("N = %d", clippedDeltas.size()));
                if (nRejected > 0) {
                    stats.append(String.format(" (%d rejected)", nRejected));
                }
                stats.append("</td>");
                
                stats.append("<td style='text-align: left; padding: 2px 4px; vertical-align: top;'>");
                stats.append("<b>Offset:</b><br>");
                stats.append(String.format("ΔV = %.3f<br>", mean));
                stats.append(String.format("RMS = %.3f", rms));
                stats.append("</td></tr>");
            }
            
            if (!foundOverlap) {
                stats.append("<tr><td colspan='2' style='text-align: center; color: red; padding: 4px;'>No overlap in selected range</td></tr>");
                recommendationLabel.setVisible(false);
                correctionButtonPanel.setVisible(false);
            } else {
                // Show recommendation based on significance test
                double significance = Math.abs(lastDeltaV) / lastRMS;
                StringBuilder recommendation = new StringBuilder("<html><div style='background-color: #FFF9E6; padding: 4px; width: 280px; font-size: 9px; text-align: center;'>");
                recommendation.append("<b>Recommendation:</b><br>");
                
                if (significance > 3.0) {
                    recommendation.append(String.format("<font color='#D32F2F'>There is a significant offset between the catalogs<br>(|ΔV|/RMS = %.1f > 3).<br><br>", significance));
                    recommendation.append("<b>Make correction to align catalogs.</b></font>");
                } else {
                    recommendation.append(String.format("<font color='#388E3C'>There is no significant offset between the catalogs<br>(|ΔV|/RMS = %.1f ≤ 3).<br><br>", significance));
                    recommendation.append("<b>No correction required.</b></font>");
                }
                recommendation.append("</div></html>");
                
                recommendationLabel.setText(recommendation.toString());
                recommendationLabel.setVisible(true);
                correctionButtonPanel.setVisible(true);
                makeCorrectionButton.setEnabled(true);
                noCorrectionButton.setEnabled(true);
                
                // Hide Step 2 until user makes decision
                step2Label.setVisible(false);
            }
            
            stats.append("</table></div></html>");
            statsLabel.setText(stats.toString());
        }
    }
}

package AAVSOtools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Custom star plot panel for integration with Seqplot application
 * Replaces the problematic JFreeChart bubble renderer
 */
public class StarPlotPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener, Printable {
    
    private DataConnector dataConnector;
    private Seqplot parentSeqplot;
    
    // Plot dimensions and margins
    private int plotWidth, plotHeight;
    private int leftMargin = 80, rightMargin = 30, topMargin = 50, bottomMargin = 80;
    private final DecimalFormat df = new DecimalFormat("0.0000", new java.text.DecimalFormatSymbols(Locale.US));
    private final DecimalFormat arcMinuteFormat = new DecimalFormat("0.0", new java.text.DecimalFormatSymbols(Locale.US));
    
    // Interaction variables
    private int hoveredRecord = -1;
    private int selectedRecord = -1;  // Track the last clicked star
    private Point mousePos = new Point();
    private Point lastPanPoint = null;
    private Point mousePressPoint = null;  // Track initial mouse press for drag threshold
    private boolean isPanning = false;
    private boolean showGrid = false;  // Grid lines disabled for cleaner points view
    private static final int DRAG_THRESHOLD = 5;  // Minimum pixels to consider it a drag
    private boolean showTooltip = true;
    private boolean pointsVisible = true;  // New: toggle for showing star points
    private boolean showLoadingIndicator = false;  // New: loading indicator state
    private boolean imageInverted = false;  // New: toggle for image inversion
    private javax.swing.Timer loadingTimer;  // Timer for loading animation
    private String loadingMessage = "Searching database";  // Configurable loading message
    private javax.swing.Timer panEndTimer;  // Timer to detect when panning has stopped
    private boolean enablePanRequery = true;  // Enable database re-query after panning
    
    // Crosshairs for clicked points
    private boolean showCrosshairs = false;
    private double crosshairX = 0.0;
    private double crosshairY = 0.0;
    private Color crosshairColor = Color.YELLOW;
    private Stroke crosshairStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f, 3.0f}, 0.0f); // Fine dashed line
    
    // Basic clear theme colors for better visibility
    private Color backgroundColor = Color.BLACK;     // Black background for astronomy
    private Color axisColor = Color.WHITE;           // White axes for contrast
    private Color gridColor = Color.GRAY;            // Gray grid lines
    private Color textColor = Color.WHITE;           // White text for contrast
    
    // Point size scaling factor (can be changed by menu)
    private double pointSizeScale = 0.75; // 25% smaller than original
    
    // Zoom and pan state
    private double zoomLevel = 1.0;
    private double panOffsetX = 0.0;
    private double panOffsetY = 0.0;
    private double maxZoomLevel = 10.0;
    private double minZoomLevel = 0.1;
    private javax.swing.Timer zoomEndTimer = null;
    private double pendingZoomFOV = 0.0; // FOV to fetch after zoom ends
    
    // DSS2 background image support
    private BufferedImage dss2Image = null;
    private DSS2Manager dss2Manager = null;
    private boolean fetchingDSS2 = false; // Track if currently fetching
    
    // Mouse coordinate tracking
    private String mouseRaDecText = null;  // Formatted RA/Dec text for display
    
    // Standard astronomical color scheme for different series (critical for data analysis)
    private Color[] seriesColors = {
        Color.BLUE,     // Series 0 - Blue stars
        Color.GREEN,    // Series 1 - Green stars  
        Color.RED,      // Series 2 - Red stars
        Color.YELLOW,   // Series 3 - Yellow stars
        Color.WHITE     // Series 4 - White stars
    };
    
    public StarPlotPanel(DataConnector dataConnector, Seqplot parentSeqplot) {
        this.dataConnector = dataConnector;
        this.parentSeqplot = parentSeqplot;
        
        setBackground(backgroundColor);
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setFocusable(true);  // Required for keyboard events
        
        // System.out.println("DEBUG: StarPlotPanel created");
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        // Enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        plotWidth = getWidth() - leftMargin - rightMargin;
        plotHeight = getHeight() - topMargin - bottomMargin;
        
        if (plotWidth <= 0 || plotHeight <= 0) return;
        
        // Check if we have DSS2 WCS - if so, use WCS-based rendering
        if (dss2Manager != null && dss2Manager.hasWCS() && dss2Image != null) {
            drawWCSBasedView(g2);
        } else {
            // Fall back to traditional tangent plane rendering
            drawTraditionalView(g2);
        }
        
        // Draw magnitude range footnote in bottom right corner
        drawMagnitudeRangeFootnote(g2);
    }
    
    /**
     * New WCS-based rendering - DSS2 image is the primary coordinate system
     */
    private void drawWCSBasedView(Graphics2D g2) {
        // System.out.println("DEBUG: Using pure image-based rendering mode");
        
        // Fill entire panel with DSS2 image (no margins, no plot area, no axes)
        drawDSS2ImageFullPanel(g2);
        
        // Draw simple bounding box around image (no coordinate grid needed - we have mouse readout)
        drawImageBoundingBox(g2);
        
        // Draw compass indicator in top left
        drawCompassIndicator(g2);
        
        // Draw information banner at top
        drawInfoBanner(g2);
        
        // Draw stars positioned directly using WCS RA/Dec to pixel conversion
        drawStarsDirectWCS(g2);
        
        // Draw VSP comparison stars with purple labels
        drawVSPStarsDirectWCS(g2);
        
        // Draw center crosshair to mark field center
        drawCenterCrosshair(g2);
        
        // Draw tooltip for hovered star
        if (showTooltip && hoveredRecord >= 0) drawTooltip(g2);
        
        // Draw mouse coordinate box
        drawMouseCoordinates(g2);
        
        // Draw loading indicator on top if needed
        drawLoadingIndicator(g2);
    }
    
    /**
     * Traditional tangent plane rendering (fallback)
     */
    private void drawTraditionalView(Graphics2D g2) {
        // System.out.println("DEBUG: Using traditional tangent plane rendering mode");
        
        // Fill the entire component with black background first
        g2.setColor(backgroundColor);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw DSS2 background first (before everything else) in plot area only
        drawDSS2Background(g2);
        
        // Fill plot area with black background only if no DSS2 image is shown
        if (dss2Image == null) {
            g2.setColor(backgroundColor);
            g2.fillRect(leftMargin, topMargin, plotWidth, plotHeight);
        }
        
        drawAxes(g2);
        if (showGrid) drawGrid(g2);
        drawStars(g2);
        drawVSPStarsTangentPlane(g2);  // Draw VSP comparison stars
        drawCrosshairs(g2);  // Draw crosshairs after stars but before tooltip
        drawCenterCrosshair(g2);  // Draw center crosshair to mark field center
        drawTitle(g2);
        
        // Draw information banner at top
        drawInfoBanner(g2);
        
        if (showTooltip && hoveredRecord >= 0) drawTooltip(g2);
        
        // Draw mouse coordinate box
        drawMouseCoordinates(g2);
        
        // Draw loading indicator on top if needed
        drawLoadingIndicator(g2);
    }
    
    /**
     * Draw DSS2 image to fill the entire panel (no margins, pure image view)
     */
    private void drawDSS2ImageFullPanel(Graphics2D g2) {
        if (dss2Image == null) {
            // Fill with black background if no image
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            return;
        }
        
        // System.out.println("DEBUG: Drawing DSS2 image to fill entire panel");
        
        // Apply inversion if enabled
        BufferedImage imageToDisplay = imageInverted ? invertImage(dss2Image) : dss2Image;
        
        // Draw DSS2 image to exactly fill the entire component
        g2.drawImage(imageToDisplay, 0, 0, getWidth(), getHeight(), null);
        
        // System.out.println("DEBUG: DSS2 image drawn full panel: " + getWidth() + "x" + getHeight());
    }
    
    /**
     * Draw information banner at top of panel with target details
     */
    private void drawInfoBanner(Graphics2D g2) {
        int bannerHeight = 30;
        int panelWidth = getWidth();
        
        // Draw black background for banner
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, panelWidth, bannerHeight);
        
        // Build info text: Target Name, RA, Dec, FoV, V limit
        String infoText = dataConnector.getStar() + 
                         "  RA: " + dataConnector.getFormattedRA() + 
                         "  Dec: " + dataConnector.getFormattedDec() + 
                         "  FoV: " + Math.round(dataConnector.getFieldSize() * 60.0) + " arcmin" +
                         "  V lim: " + String.format("%.1f", dataConnector.getLimitingMag());
        
        // Draw white text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        // Center text vertically in banner
        FontMetrics fm = g2.getFontMetrics();
        int textHeight = fm.getAscent();
        int textY = (bannerHeight + textHeight) / 2 - 2;
        
        // Draw text with small left margin
        g2.drawString(infoText, 10, textY);
    }
    
    /**
     * Draw stars positioned directly using DSS2 WCS coordinates (no plot layer)
     */
    private void drawStarsDirectWCS(Graphics2D g2) {
        if (!pointsVisible) {
            // System.out.println("DEBUG: Points hidden - skipping WCS star drawing");
            return;
        }
        
        AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        if (wcs == null) {
            // System.out.println("DEBUG: No WCS available - cannot draw stars");
            return;
        }
        
        // System.out.println("DEBUG: Drawing stars using direct WCS positioning");
        
        g2.setStroke(new BasicStroke(1));
        
        int totalCount = dataConnector.getTotalCount();
        if (totalCount == 0) return;
        
        // Get magnitude range for star sizing
        double minVMag = dataConnector.getMinVMag();
        double maxVMag = dataConnector.getMaxVMag();
        
        int drawnCount = 0;
        
        // Draw stars from all series using direct RA/Dec to pixel conversion
        for (int series = 0; series < 5; series++) {
            for (int i = 0; i < totalCount; i++) {
                if (dataConnector.getSeries(i) != series) continue;
                
                // Get star's RA/Dec directly from data
                double starRA = dataConnector.getRa(i);
                double starDec = dataConnector.getDec(i);
                double vMag = dataConnector.getVmag(i);
                
                // Skip stars with invalid magnitudes
                if (vMag == 99.999 || Double.isNaN(vMag)) continue;
                
                // Convert RA/Dec directly to DSS2 image pixel coordinates using WCS
                double[] dss2Pixel = wcs.worldToPixel(starRA, starDec);
                if (dss2Pixel == null || Double.isNaN(dss2Pixel[0]) || Double.isNaN(dss2Pixel[1])) {
                    continue; // Invalid pixel coordinates
                }
                
                double dss2X = dss2Pixel[0];
                double dss2Y = dss2Pixel[1];
                
                // Check if star is within DSS2 image bounds
                if (dss2X < 0 || dss2X >= wcs.naxis1 || dss2Y < 0 || dss2Y >= wcs.naxis2) {
                    continue; // Star is outside DSS2 image
                }
                
                // Convert DSS2 pixel coordinates directly to screen coordinates
                // Image fills entire panel, so scale to panel dimensions
                int screenX = (int)(dss2X * getWidth() / wcs.naxis1);
                int screenY = (int)((wcs.naxis2 - dss2Y) * getHeight() / wcs.naxis2); // Flip Y
                
                // Calculate star size based on magnitude
                // Use logarithmic scale for more dramatic size differences
                double magRange = maxVMag - minVMag;
                int starSize;
                if (magRange > 0) {
                    // Invert magnitude (brighter stars have lower magnitude values)
                    double normalizedMag = (maxVMag - vMag) / magRange; // 0 = faintest, 1 = brightest
                    
                    // Use exponential scaling for more dramatic visual difference
                    double sizeMultiplier = Math.pow(normalizedMag, 0.7); // Power < 1 gives more gradual transition
                    int baseSize = (int)(3 + sizeMultiplier * 25); // Size range: 3-28 pixels
                    
                    // Apply user point size scale
                    starSize = Math.max(2, (int)(baseSize * pointSizeScale));
                } else {
                    // All stars same magnitude
                    starSize = Math.max(2, (int)(12 * pointSizeScale));
                }
                
                // Get star color based on series
                Color starColor = (series < seriesColors.length) ? seriesColors[series] : Color.CYAN;
                
                // Check if this star is hovered or selected
                boolean isHovered = (hoveredRecord == i);
                boolean isSelected = (selectedRecord == i);
                
                // Draw star with body and edge
                g2.setColor(starColor);
                g2.fillOval(screenX - starSize/2, screenY - starSize/2, starSize, starSize);
                
                // Draw edge for contrast
                g2.setColor(starColor.darker());
                g2.drawOval(screenX - starSize/2, screenY - starSize/2, starSize, starSize);
                
                // Highlight selected star (persistent)
                if (isSelected) {
                    g2.setColor(Color.YELLOW);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawOval(screenX - starSize/2 - 3, screenY - starSize/2 - 3, starSize + 6, starSize + 6);
                    g2.setStroke(new BasicStroke(1)); // Reset stroke
                }
                
                // Highlight hovered star (temporary)
                if (isHovered) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(screenX - starSize/2 - 2, screenY - starSize/2 - 2, starSize + 4, starSize + 4);
                    g2.setStroke(new BasicStroke(1)); // Reset stroke
                }
                
                // if (drawnCount < 5) { // Debug first few stars
                //     System.out.printf("DEBUG: WCS Star %d: RA=%.6f, Dec=%.6f -> DSS2pixel(%.2f,%.2f) -> screen(%d,%d) | WCS center=(%.6f,%.6f) cdelt=(%.6f,%.6f) crpix=(%.1f,%.1f)\\n", 
                //                      drawnCount, starRA, starDec, dss2X, dss2Y, screenX, screenY, 
                //                      wcs.crval1, wcs.crval2, wcs.cdelt1, wcs.cdelt2, wcs.crpix1, wcs.crpix2);
                // }
                
                drawnCount++;
            }
        }
        
        // System.out.printf("DEBUG: Total direct WCS stars drawn: %d, Mag range: %.2f to %.2f\\n", 
        //                  drawnCount, minVMag, maxVMag);
    }
    
    /**
     * Draw VSP comparison stars with purple labels (WCS-based positioning)
     */
    private void drawVSPStarsDirectWCS(Graphics2D g2) {
        if (!pointsVisible) return;
        
        AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        if (wcs == null) return;
        
        java.util.List<AAVSOtools.DataConnector.VSPCompStar> vspStars = dataConnector.getVSPCompStars();
        if (vspStars == null || vspStars.isEmpty()) return;
        
        g2.setColor(Color.WHITE); // White text
        g2.setFont(new Font("SansSerif", Font.BOLD, 10)); // 10pt font
        
        for (AAVSOtools.DataConnector.VSPCompStar star : vspStars) {
            // Skip if no valid V magnitude
            if (star.vmag >= 99.0 || Double.isNaN(star.vmag)) continue;
            
            // Convert RA/Dec to DSS2 pixel coordinates
            double[] dss2Pixel = wcs.worldToPixel(star.ra, star.dec);
            if (dss2Pixel == null || Double.isNaN(dss2Pixel[0]) || Double.isNaN(dss2Pixel[1])) continue;
            
            double dss2X = dss2Pixel[0];
            double dss2Y = dss2Pixel[1];
            
            // Check if within image bounds
            if (dss2X < 0 || dss2X >= wcs.naxis1 || dss2Y < 0 || dss2Y >= wcs.naxis2) continue;
            
            // Convert to screen coordinates
            int screenX = (int)(dss2X * getWidth() / wcs.naxis1);
            int screenY = (int)((wcs.naxis2 - dss2Y) * getHeight() / wcs.naxis2);
            
            // Format label as int(round(V*10))
            int labelValue = (int)Math.round(star.vmag * 10.0);
            String label = String.valueOf(labelValue);
            
            // Measure label dimensions for background box
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getHeight();
            int labelAscent = fm.getAscent();
            
            // Draw label offset to avoid covering the star position
            int labelX = screenX + 8; // Offset right
            int labelY = screenY - 5; // Offset up
            
            // Draw light purple background box
            int boxPadding = 2;
            int boxX = labelX - boxPadding;
            int boxY = labelY - labelAscent - boxPadding;
            int boxWidth = labelWidth + 2 * boxPadding;
            int boxHeight = labelHeight + boxPadding;
            
            g2.setColor(new Color(230, 200, 255, 150)); // Light purple with transparency
            g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 4, 4); // Rounded corners
            
            // Draw white text on top
            g2.setColor(Color.WHITE);
            g2.drawString(label, labelX, labelY);
        }
    }
    
    /**
     * Draw VSP comparison stars with purple labels (traditional tangent plane positioning)
     */
    private void drawVSPStarsTangentPlane(Graphics2D g2) {
        if (!pointsVisible) return;
        
        java.util.List<AAVSOtools.DataConnector.VSPCompStar> vspStars = dataConnector.getVSPCompStars();
        if (vspStars == null || vspStars.isEmpty()) return;
        
        // Get tangent plane bounds
        double minX = dataConnector.getMinX();
        double maxX = dataConnector.getMaxX();
        double minY = dataConnector.getMinY();
        double maxY = dataConnector.getMaxY();
        
        g2.setColor(new Color(200, 100, 255)); // Purple color
        g2.setFont(new Font("SansSerif", Font.BOLD, 10)); // 10pt font
        
        for (AAVSOtools.DataConnector.VSPCompStar star : vspStars) {
            // Skip if no valid V magnitude
            if (star.vmag >= 99.0 || Double.isNaN(star.vmag)) continue;
            
            // Use tangent plane coordinates (already calculated by DataConnector)
            // NEGATE X to flip left/right as required
            double x = -star.x;
            double y = star.y;
            
            // Convert tangent plane to screen coordinates
            // Y-axis: minY at bottom (high Y screen), maxY at top (low Y screen)
            int screenX = (int)(leftMargin + (x - minX) * (plotWidth / (maxX - minX)));
            int screenY = (int)(topMargin + plotHeight - (y - minY) * (plotHeight / (maxY - minY)));
            
            // Check if within plot area
            if (screenX < leftMargin || screenX > leftMargin + plotWidth ||
                screenY < topMargin || screenY > topMargin + plotHeight) continue;
            
            // Format label as int(round(V*10))
            int labelValue = (int)Math.round(star.vmag * 10.0);
            String label = String.valueOf(labelValue);
            
            // Measure label dimensions for background box
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getHeight();
            int labelAscent = fm.getAscent();
            
            // Draw label offset to avoid covering the star position
            int labelX = screenX + 8; // Offset right
            int labelY = screenY - 5; // Offset up
            
            // Draw light purple background box with rounded corners
            int boxPadding = 2;
            int boxX = labelX - boxPadding;
            int boxY = labelY - labelAscent - boxPadding;
            int boxWidth = labelWidth + 2 * boxPadding;
            int boxHeight = labelHeight + boxPadding;
            
            g2.setColor(new Color(230, 200, 255, 150)); // Light purple with transparency
            g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 4, 4); // Rounded corners
            
            // Draw white text on top
            g2.setColor(Color.WHITE);
            g2.drawString(label, labelX, labelY);
        }
    }
    
    /**
     * Draw magnitude range footnote in bottom right corner
     */
    private void drawMagnitudeRangeFootnote(Graphics2D g2) {
        if (dataConnector == null || dataConnector.getTotalCount() == 0) return;
        
        String magRangeText = null;
        
        // Try to get target star VSX magnitude range first
        try {
            int totalCount = dataConnector.getTotalCount();
            
            if (totalCount > 0) {
                // Search for the target star (the variable star we're studying)
                String targetVarMax = null;
                String targetVarMin = null;
                
                // Check first few entries to find target star
                for (int i = 0; i < Math.min(totalCount, 5); i++) {
                    String varMax = dataConnector.getVarMax(i);
                    String varMin = dataConnector.getVarMin(i);
                    String varType = dataConnector.getVarType(i);
                    
                    // Look for the main variable star (not comparison star)
                    if (varMax != null && varMin != null && 
                        !varMax.trim().isEmpty() && !varMin.trim().isEmpty() && 
                        !varMax.equals("?") && !varMin.equals("?") &&
                        (varType == null || (!varType.contains("CV") && !varType.contains("comp")))) {
                        
                        // Try parsing the values to see if they're reasonable for a target star
                        try {
                            String maxNumStr = varMax.trim().split("\\s+")[0];
                            String minNumStr = varMin.trim().split("\\s+")[0];
                            double maxMag = Double.parseDouble(maxNumStr);
                            double minMag = Double.parseDouble(minNumStr);
                            
                            // Target stars typically have magnitudes in a reasonable range (not too faint)
                            if (maxMag < 16.0 && minMag < 16.0) {
                                targetVarMax = varMax;
                                targetVarMin = varMin;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            // Continue searching
                        }
                    }
                }
                
                // If we found target star data, use it
                if (targetVarMax != null && targetVarMin != null) {
                    String maxNumStr = targetVarMax.trim().split("\\s+")[0];
                    String minNumStr = targetVarMin.trim().split("\\s+")[0];
                    
                    try {
                        double maxMag = Double.parseDouble(maxNumStr);
                        double minMag = Double.parseDouble(minNumStr);
                        
                        // Ensure proper order: brighter magnitude first (smaller number)
                        double brightMag = Math.min(maxMag, minMag);
                        double faintMag = Math.max(maxMag, minMag);
                        
                        magRangeText = String.format("Mag Range: %.1f - %.1f", brightMag, faintMag);
                    } catch (NumberFormatException e) {
                        // Fall through to catalog range
                    }
                }
            }
        } catch (Exception e) {
            // Fall through to catalog range
        }
        
        // Fall back to catalog magnitude range if VSX data unavailable
        if (magRangeText == null) {
            double minVMag = dataConnector.getMinVMag();
            double maxVMag = dataConnector.getMaxVMag();
            
            System.out.println("DEBUG: Using catalog fallback - minVMag: " + minVMag + ", maxVMag: " + maxVMag);
            
            // Skip if no valid magnitudes
            if (minVMag == 99.999 || maxVMag == 99.999 || Double.isNaN(minVMag) || Double.isNaN(maxVMag)) {
                System.out.println("DEBUG: Invalid catalog magnitudes, skipping footnote");
                return;
            }
            
            // Ensure proper order: brighter magnitude first (smaller number)  
            double brightMag = Math.min(minVMag, maxVMag);
            double faintMag = Math.max(minVMag, maxVMag);
            
            magRangeText = String.format("Mag Range: %.1f - %.1f", brightMag, faintMag);
        }
        
        // Set font and get metrics
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(magRangeText);
        int textHeight = fm.getHeight();
        
        // Position in bottom right corner with some padding
        int x = getWidth() - textWidth - 10;
        int y = getHeight() - 10;
        
        // Draw semi-transparent background
        g2.setColor(new Color(0, 0, 0, 120)); // Semi-transparent black
        g2.fillRoundRect(x - 4, y - textHeight + 4, textWidth + 8, textHeight, 4, 4);
        
        // Draw text in light gray
        g2.setColor(new Color(200, 200, 200));
        g2.drawString(magRangeText, x, y);
    }
    
    private void drawAxes(Graphics2D g2) {
        g2.setColor(axisColor);
        g2.setStroke(new BasicStroke(2));
        
        // Plot border
        Rectangle2D plotArea = new Rectangle2D.Double(leftMargin, topMargin, plotWidth, plotHeight);
        g2.draw(plotArea);
        
        // Determine unit system for axis labels
        boolean useArcMinutes = pointsVisible && (dss2Manager == null || dss2Manager.getCurrentWCS() == null);
        
        // Axis labels
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        
        // X-axis label
        String xLabel = useArcMinutes ? "X (arcmin) - East" : "X (degrees) - East";
        int xLabelWidth = fm.stringWidth(xLabel);
        g2.drawString(xLabel, leftMargin + (plotWidth - xLabelWidth) / 2, 
                     getHeight() - 20);
        
        // Y-axis label (rotated)
        String yLabel = useArcMinutes ? "Y (arcmin) - North" : "Y (degrees) - North";
        g2.rotate(-Math.PI / 2);
        g2.drawString(yLabel, -(topMargin + plotHeight / 2 + fm.stringWidth(yLabel) / 2), 20);
        g2.rotate(Math.PI / 2);
        
        // Tick marks and values
        drawTicks(g2, useArcMinutes);
    }
    
    private void drawTicks(Graphics2D g2, boolean useArcMinutes) {
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(axisColor);
        
        double minX = dataConnector.getMinX();
        double maxX = dataConnector.getMaxX();
        double minY = dataConnector.getMinY();
        double maxY = dataConnector.getMaxY();
        
        // Apply zoom and pan transformations for tick labels
        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double rangeX = (maxX - minX) / zoomLevel;
        double rangeY = (maxY - minY) / zoomLevel;
        
        double viewMinX = centerX - rangeX / 2.0 + panOffsetX;
        double viewMaxX = centerX + rangeX / 2.0 + panOffsetX;
        double viewMinY = centerY - rangeY / 2.0 + panOffsetY;
        double viewMaxY = centerY + rangeY / 2.0 + panOffsetY;
        
        // X-axis ticks
        int numXTicks = 6;
        for (int i = 0; i <= numXTicks; i++) {
            double worldX = viewMinX + (viewMaxX - viewMinX) * i / numXTicks;
            int screenX = leftMargin + (int)(plotWidth * i / (double)numXTicks);
            
            // Tick mark
            g2.drawLine(screenX, topMargin + plotHeight, screenX, topMargin + plotHeight + 5);
            
            // Tick label
            double displayValue = useArcMinutes ? worldX * 60.0 : worldX;
            if (useArcMinutes) {
                displayValue = Math.round(displayValue * 10.0) / 10.0;
            }
            String label = useArcMinutes ? arcMinuteFormat.format(displayValue) : df.format(displayValue);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, screenX - fm.stringWidth(label) / 2, 
                         topMargin + plotHeight + 20);
        }
        
        // Y-axis ticks
        int numYTicks = 5;
        for (int i = 0; i <= numYTicks; i++) {
            double worldY = viewMinY + (viewMaxY - viewMinY) * i / numYTicks;
            int screenY = topMargin + plotHeight - (int)(plotHeight * i / (double)numYTicks);
            
            // Tick mark
            g2.drawLine(leftMargin - 5, screenY, leftMargin, screenY);
            
            // Tick label
            double displayValue = useArcMinutes ? worldY * 60.0 : worldY;
            if (useArcMinutes) {
                displayValue = Math.round(displayValue * 10.0) / 10.0;
            }
            String label = useArcMinutes ? arcMinuteFormat.format(displayValue) : df.format(displayValue);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, leftMargin - fm.stringWidth(label) - 10, 
                         screenY + fm.getHeight() / 3);
        }
    }
    
    private void drawGrid(Graphics2D g2) {
        g2.setColor(gridColor);
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                                   10.0f, new float[]{2.0f}, 0.0f)); // Dashed line
        
        // Vertical grid lines
        int numXTicks = 6;
        for (int i = 1; i < numXTicks; i++) {
            int screenX = leftMargin + (int)(plotWidth * i / (double)numXTicks);
            g2.drawLine(screenX, topMargin, screenX, topMargin + plotHeight);
        }
        
        // Horizontal grid lines
        int numYTicks = 5;
        for (int i = 1; i < numYTicks; i++) {
            int screenY = topMargin + (int)(plotHeight * i / (double)numYTicks);
            g2.drawLine(leftMargin, screenY, leftMargin + plotWidth, screenY);
        }
    }
    
    private void drawCrosshairs(Graphics2D g2) {
        if (!showCrosshairs) return;
        
        // Save original stroke and color
        Stroke originalStroke = g2.getStroke();
        Color originalColor = g2.getColor();
        
        // Set crosshair style - fine dashed yellow line
        g2.setStroke(crosshairStroke);
        g2.setColor(crosshairColor);
        
        // Draw horizontal crosshair line across the plot area
        g2.drawLine(leftMargin, (int)crosshairY, leftMargin + plotWidth, (int)crosshairY);
        
        // Draw vertical crosshair line across the plot area  
        g2.drawLine((int)crosshairX, topMargin, (int)crosshairX, topMargin + plotHeight);
        
        // Restore original stroke and color
        g2.setStroke(originalStroke);
        g2.setColor(originalColor);
    }
    
    /**
     * Draw a small yellow dashed crosshair at the center of the view
     * to indicate the field center (target position)
     */
    private void drawCenterCrosshair(Graphics2D g2) {
        // Save original stroke and color
        Stroke originalStroke = g2.getStroke();
        Color originalColor = g2.getColor();
        
        // Set style - yellow dashed line
        float[] dashPattern = {3f, 3f};
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                                     10.0f, dashPattern, 0.0f));
        g2.setColor(Color.YELLOW);
        
        // Calculate center point
        int centerX, centerY;
        if (dss2Manager != null && dss2Manager.hasWCS() && dss2Image != null) {
            // WCS mode: center of entire panel
            centerX = getWidth() / 2;
            centerY = getHeight() / 2;
        } else {
            // Traditional mode: center of plot area
            centerX = leftMargin + plotWidth / 2;
            centerY = topMargin + plotHeight / 2;
        }
        
        // Draw small crosshair (8 pixels on each side from center)
        int crossSize = 8;
        g2.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY); // Horizontal
        g2.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize); // Vertical
        
        // Restore original stroke and color
        g2.setStroke(originalStroke);
        g2.setColor(originalColor);
    }
    
    private void drawStars(Graphics2D g2) {
        // System.out.println("DEBUG: drawStars() called, pointsVisible = " + pointsVisible);
        // Skip drawing stars if points are not visible
        if (!pointsVisible) {
            // System.out.println("DEBUG: Points hidden - skipping star drawing");
            return;
        }
        
        g2.setStroke(new BasicStroke(1));
        
        int totalCount = dataConnector.getTotalCount();
        // System.out.println("DEBUG: drawStars() called, totalCount = " + totalCount);
        if (totalCount == 0) {
            // System.out.println("DEBUG: No data to draw - totalCount is 0");
            return;
        }
        
        double minX = dataConnector.getMinX();
        double maxX = dataConnector.getMaxX();
        double minY = dataConnector.getMinY();
        double maxY = dataConnector.getMaxY();
        
        // Apply zoom and pan transformations
        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double rangeX = (maxX - minX) / zoomLevel;
        double rangeY = (maxY - minY) / zoomLevel;
        
        double viewMinX = centerX - rangeX / 2.0 + panOffsetX;
        double viewMaxX = centerX + rangeX / 2.0 + panOffsetX;
        double viewMinY = centerY - rangeY / 2.0 + panOffsetY;
        double viewMaxY = centerY + rangeY / 2.0 + panOffsetY;
        
        // Debug output (commented out - was useful for debugging zoom/view bounds issues)
        // System.out.printf("DEBUG: drawStars() - data bounds X:[%.3f,%.3f] Y:[%.3f,%.3f], zoom=%.2f\n",
        //                  minX, maxX, minY, maxY, zoomLevel);
        // System.out.printf("DEBUG: drawStars() - view bounds X:[%.3f,%.3f] Y:[%.3f,%.3f] (range: %.3f x %.3f)\n",
        //                  viewMinX, viewMaxX, viewMinY, viewMaxY, viewMaxX - viewMinX, viewMaxY - viewMinY);
        // System.out.printf("DEBUG: drawStars() - data range: %.3f x %.3f, view/data ratio: %.2f\n",
        //                  maxX - minX, maxY - minY, (viewMaxX - viewMinX) / (maxX - minX));
        
        // Get magnitude range for star sizing
        double minVMag = dataConnector.getMinVMag();
        double maxVMag = dataConnector.getMaxVMag();
        
        // Set clipping rectangle to plot area only
        Rectangle originalClip = g2.getClipBounds();
        g2.setClip(leftMargin, topMargin, plotWidth, plotHeight);
        
        int drawnCount = 0;
        
        // Draw stars from all series
        for (int series = 0; series < 5; series++) {
            for (int i = 0; i < totalCount; i++) {
                if (dataConnector.getSeries(i) != series) continue;
                
                // Get star coordinates and properties
                double worldX = dataConnector.getXValue(series, i);
                double worldY = dataConnector.getYValue(series, i);
                double vMag = dataConnector.getVmag(i);
                
                // Skip stars with invalid magnitudes
                if (vMag == 99.999 || Double.isNaN(vMag)) continue;
                
                // Check if star is in view
                if (worldX < viewMinX || worldX > viewMaxX || worldY < viewMinY || worldY > viewMaxY) {
                    continue;
                }
                
                // Convert to screen coordinates using view bounds (flip X-axis)
                int screenX = leftMargin + (int)((viewMaxX - worldX) / (viewMaxX - viewMinX) * plotWidth);
                int screenY = topMargin + (int)((viewMaxY - worldY) / (viewMaxY - viewMinY) * plotHeight); // Flip Y
                
                // Check if star is within plot bounds (with some margin for large stars)
                int maxStarSize = 30;
                if (screenX < leftMargin - maxStarSize || screenX > leftMargin + plotWidth + maxStarSize ||
                    screenY < topMargin - maxStarSize || screenY > topMargin + plotHeight + maxStarSize) {
                    continue; // Skip stars outside plot area
                }
                
                // Calculate star size based on V magnitude (brighter = larger)
                // Use logarithmic scale for more dramatic size differences
                double magRange = maxVMag - minVMag;
                int size;
                if (magRange > 0) {
                    // Invert magnitude (brighter stars have lower magnitude values)
                    double normalizedMag = (maxVMag - vMag) / magRange; // 0 = faintest, 1 = brightest
                    
                    // Use exponential scaling for more dramatic visual difference
                    // Brightest stars can be much larger than faintest
                    double sizeMultiplier = Math.pow(normalizedMag, 0.7); // Power < 1 gives more gradual transition
                    int baseSize = (int)(3 + sizeMultiplier * 25); // Size range: 3-28 pixels
                    
                    // Apply user point size scale
                    size = Math.max(2, (int)(baseSize * pointSizeScale));
                } else {
                    // All stars same magnitude
                    size = Math.max(2, (int)(12 * pointSizeScale));
                }
                
                // Get the star's color first
                Color starColor = (series < seriesColors.length) ? seriesColors[series] : Color.CYAN;
                
                // Highlight hovered star using the same color as the star body
                if (i == hoveredRecord) {
                    g2.setColor(starColor);
                    g2.fillOval(screenX - size/2 - 3, screenY - size/2 - 3, 
                               size + 6, size + 6);
                }
                
                // Draw the star
                g2.setColor(starColor);
                Ellipse2D.Double circle = new Ellipse2D.Double(
                    screenX - size/2, screenY - size/2, size, size
                );
                g2.fill(circle);
                
                // Draw outline using the same color as the body
                g2.setColor(starColor);
                g2.draw(circle);
            }
        }
        
        // Restore original clipping
        g2.setClip(originalClip);
    }
    
    private void drawTitle(Graphics2D g2) {
        g2.setColor(textColor);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        
        String mainTitle = parentSeqplot.getMainTitleText();
        if (mainTitle == null || mainTitle.isEmpty()) {
            mainTitle = "Star Field Plot";
        }
        
        int titleWidth = fm.stringWidth(mainTitle);
        g2.drawString(mainTitle, (getWidth() - titleWidth) / 2, 25);
        
        // Don't draw subtitle - removed "Data from Calibration Database" text
    }
    
    private void drawTooltip(Graphics2D g2) {
        if (hoveredRecord < 0) return;
        
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        
        // Check for preferred catalog based on transition magnitude
        AAVSOtools.DataConnector.CatalogEntry preferred = dataConnector.getPreferredCatalogEntry(hoveredRecord);
        
        // Get star information (either from preferred catalog or primary)
        double ra, dec, vmag, vmagError, bMinusV, vMinusI, bmagError, colorError;
        int source;
        
        if (preferred != null) {
            // Use preferred catalog data
            ra = preferred.ra;
            dec = preferred.dec;
            vmag = preferred.vmag;
            vmagError = preferred.ev;
            bMinusV = preferred.bMinusV;
            vMinusI = preferred.vMinusI;
            bmagError = preferred.ebv;
            colorError = preferred.evi;
            source = preferred.source;
        } else {
            // Use primary catalog data
            ra = dataConnector.getRa(hoveredRecord);
            dec = dataConnector.getDec(hoveredRecord);
            vmag = dataConnector.getVmag(hoveredRecord);
            vmagError = dataConnector.getEv(hoveredRecord);
            bMinusV = dataConnector.getBMinusV(hoveredRecord);
            vMinusI = bMinusV; // For deep catalogs, this is stored in B-V field
            bmagError = dataConnector.getE_Bmag(hoveredRecord);
            colorError = bmagError;
            source = dataConnector.getSource(hoveredRecord);
        }
        
        // Determine labels and values based on source
        String magLabel, colorLabel;
        double colorValue, colorErrorValue;
        
        if (source == 48) {
            magLabel = "V";      // Gaia transformed V magnitude
            colorLabel = "V-I";  // Gaia V-I color
            colorValue = vMinusI;
            colorErrorValue = colorError;
        } else if (source == 46) {
            magLabel = "V";      // PanSTARRS transformed V magnitude
            colorLabel = "V-I";  // PanSTARRS V-I color
            colorValue = vMinusI;
            colorErrorValue = colorError;
        } else {
            magLabel = "V";      // Standard V magnitude
            colorLabel = "B-V";  // Standard B-V color
            colorValue = bMinusV;
            // Calculate B-V error using quadrature: sqrt(e_B^2 + e_V^2)
            colorErrorValue = Math.sqrt(bmagError * bmagError + vmagError * vmagError);
        }
        
        // Calculate distance from center in arcseconds
        double centerRa = dataConnector.getCenterRa();
        double centerDec = dataConnector.getCenterDec();
        
        // Debug output
        if (hoveredRecord < 3) { // Only for first few stars to avoid spam
            if (preferred != null) {
                System.out.printf("DEBUG: Hover Star %d (using preferred source %d) - RA=%.6f, Dec=%.6f, CenterRA=%.6f, CenterDec=%.6f\n",
                                 hoveredRecord, source, ra, dec, centerRa, centerDec);
            } else {
                System.out.printf("DEBUG: Star %d - RA=%.6f, Dec=%.6f, CenterRA=%.6f, CenterDec=%.6f\n",
                                 hoveredRecord, ra, dec, centerRa, centerDec);
            }
        }
        
        // Convert coordinate differences to arcseconds
        double deltaRa = (ra - centerRa) * 3600.0 * Math.cos(Math.toRadians(centerRa));
        double deltaDec = (dec - centerDec) * 3600.0;
        double distanceArcsec = Math.sqrt(deltaRa * deltaRa + deltaDec * deltaDec);
        double distanceArcmin = distanceArcsec / 60.0;
        
        String[] lines = {
            String.format("%s: %.3f (%.3f)", magLabel, vmag, vmagError),
            String.format("%s: %.3f (%.3f)", colorLabel, colorValue, colorErrorValue),
            String.format("Dist: %.1f\" (%.2f')", distanceArcsec, distanceArcmin)
        };
        
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, fm.stringWidth(line));
        }
        
        int tooltipWidth = maxWidth + 10;
        int tooltipHeight = lines.length * fm.getHeight() + 5;
        
        // Position tooltip near mouse but keep it on screen
        int tooltipX = mousePos.x + 15;
        int tooltipY = mousePos.y - tooltipHeight - 5;
        
        if (tooltipX + tooltipWidth > getWidth()) {
            tooltipX = mousePos.x - tooltipWidth - 15;
        }
        if (tooltipY < 0) {
            tooltipY = mousePos.y + 15;
        }
        
        // Draw tooltip background
        g2.setColor(new Color(50, 50, 50, 240));
        g2.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 5, 5);
        
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 5, 5);
        
        // Draw tooltip text
        g2.setColor(Color.WHITE);
        for (int i = 0; i < lines.length; i++) {
            g2.drawString(lines[i], tooltipX + 5, tooltipY + (i + 1) * fm.getHeight());
        }
    }
    
    private void drawLegend(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        
        int legendX = getWidth() - 150;
        int legendY = topMargin + 20;
        
        g2.setColor(new Color(30, 30, 30, 200));
        g2.fillRoundRect(legendX - 5, legendY - 15, 140, 120, 5, 5);
        
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(legendX - 5, legendY - 15, 140, 120, 5, 5);
        g2.drawString("Series Colors:", legendX, legendY);
        
        String[] seriesNames = {"Blue", "Green", "Red", "Purple", "White"};
        
        for (int i = 0; i < 5; i++) {
            int y = legendY + 15 + i * 15;
            
            // Color circle
            g2.setColor(seriesColors[i]);
            g2.fillOval(legendX, y - 5, 10, 10);
            g2.setColor(seriesColors[i]);
            g2.drawOval(legendX, y - 5, 10, 10);
            
            // Label
            g2.drawString(seriesNames[i], legendX + 15, y + 3);
        }
    }
    
    /**
     * Find VSP comparison star at mouse position by checking if click is within label box
     */
    private int findVSPStarAtPosition(int mouseX, int mouseY) {
        java.util.List<AAVSOtools.DataConnector.VSPCompStar> vspStars = dataConnector.getVSPCompStars();
        if (vspStars == null || vspStars.isEmpty() || !pointsVisible) return -1;
        
        // Check if using WCS mode or traditional mode
        boolean useWCS = (dss2Manager != null && dss2Manager.getCurrentWCS() != null);
        
        if (useWCS) {
            return findVSPStarAtPositionWCS(mouseX, mouseY, vspStars);
        } else {
            return findVSPStarAtPositionTraditional(mouseX, mouseY, vspStars);
        }
    }
    
    /**
     * Find VSP star in WCS mode
     */
    private int findVSPStarAtPositionWCS(int mouseX, int mouseY, 
                                         java.util.List<AAVSOtools.DataConnector.VSPCompStar> vspStars) {
        AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        if (wcs == null) return -1;
        
        FontMetrics fm = getFontMetrics(new Font("SansSerif", Font.BOLD, 10)); // Updated to 10
        
        for (int i = 0; i < vspStars.size(); i++) {
            AAVSOtools.DataConnector.VSPCompStar star = vspStars.get(i);
            if (star.vmag >= 99.0 || Double.isNaN(star.vmag)) continue;
            
            // Convert RA/Dec to DSS2 pixel coordinates
            double[] dss2Pixel = wcs.worldToPixel(star.ra, star.dec);
            if (dss2Pixel == null || Double.isNaN(dss2Pixel[0]) || Double.isNaN(dss2Pixel[1])) continue;
            
            double dss2X = dss2Pixel[0];
            double dss2Y = dss2Pixel[1];
            
            if (dss2X < 0 || dss2X >= wcs.naxis1 || dss2Y < 0 || dss2Y >= wcs.naxis2) continue;
            
            // Convert to screen coordinates
            int screenX = (int)(dss2X * getWidth() / wcs.naxis1);
            int screenY = (int)((wcs.naxis2 - dss2Y) * getHeight() / wcs.naxis2);
            
            // Calculate label box bounds
            int labelValue = (int)Math.round(star.vmag * 10.0);
            String label = String.valueOf(labelValue);
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getHeight();
            int labelAscent = fm.getAscent();
            
            int labelX = screenX + 8;
            int labelY = screenY - 5;
            
            int boxPadding = 2;
            int boxX = labelX - boxPadding;
            int boxY = labelY - labelAscent - boxPadding;
            int boxWidth = labelWidth + 2 * boxPadding;
            int boxHeight = labelHeight + boxPadding;
            
            // Check if mouse is within box
            if (mouseX >= boxX && mouseX <= boxX + boxWidth &&
                mouseY >= boxY && mouseY <= boxY + boxHeight) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Find VSP star in traditional tangent plane mode
     */
    private int findVSPStarAtPositionTraditional(int mouseX, int mouseY,
                                                  java.util.List<AAVSOtools.DataConnector.VSPCompStar> vspStars) {
        double minX = dataConnector.getMinX();
        double maxX = dataConnector.getMaxX();
        double minY = dataConnector.getMinY();
        double maxY = dataConnector.getMaxY();
        
        FontMetrics fm = getFontMetrics(new Font("SansSerif", Font.BOLD, 10)); // Updated to 10
        
        for (int i = 0; i < vspStars.size(); i++) {
            AAVSOtools.DataConnector.VSPCompStar star = vspStars.get(i);
            if (star.vmag >= 99.0 || Double.isNaN(star.vmag)) continue;
            
            // NEGATE X to match rendering (flip left/right)
            double x = -star.x;
            double y = star.y;
            
            // Convert tangent plane to screen coordinates (Y-axis fix)
            int screenX = (int)(leftMargin + (x - minX) * (plotWidth / (maxX - minX)));
            int screenY = (int)(topMargin + plotHeight - (y - minY) * (plotHeight / (maxY - minY)));
            
            if (screenX < leftMargin || screenX > leftMargin + plotWidth ||
                screenY < topMargin || screenY > topMargin + plotHeight) continue;
            
            // Calculate label box bounds
            int labelValue = (int)Math.round(star.vmag * 10.0);
            String label = String.valueOf(labelValue);
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getHeight();
            int labelAscent = fm.getAscent();
            
            int labelX = screenX + 8;
            int labelY = screenY - 5;
            
            int boxPadding = 2;
            int boxX = labelX - boxPadding;
            int boxY = labelY - labelAscent - boxPadding;
            int boxWidth = labelWidth + 2 * boxPadding;
            int boxHeight = labelHeight + boxPadding;
            
            // Check if mouse is within box
            if (mouseX >= boxX && mouseX <= boxX + boxWidth &&
                mouseY >= boxY && mouseY <= boxY + boxHeight) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Display VSP comparison star information in the readout panel
     */
    private void displayVSPStarInfo(int vspIndex) {
        java.util.List<AAVSOtools.DataConnector.VSPCompStar> vspStars = dataConnector.getVSPCompStars();
        if (vspIndex < 0 || vspIndex >= vspStars.size()) return;
        
        AAVSOtools.DataConnector.VSPCompStar star = vspStars.get(vspIndex);
        
        // Format the readout text with VSP star details
        StringBuilder readoutText = new StringBuilder();
        readoutText.append("  RA: ").append(star.raStr)
                  .append("   Dec: ").append(star.decStr);
        
        // V magnitude with error
        if (star.vmag < 99.0) {
            readoutText.append("   V: ");
            if (star.vError > 0 && star.vError < 10) {
                readoutText.append(String.format("%.3f (%.3f)", star.vmag, star.vError));
            } else {
                readoutText.append(String.format("%.3f", star.vmag));
            }
        }
        
        // B-V color with error
        if (star.bMinusV < 99.0) {
            readoutText.append("   B-V: ");
            if (star.bvError > 0 && star.bvError < 10) {
                readoutText.append(String.format("%.3f (%.3f)", star.bMinusV, star.bvError));
            } else {
                readoutText.append(String.format("%.3f", star.bMinusV));
            }
        }
        
        // V-I color with error
        if (star.vMinusI < 99.0) {
            readoutText.append("   V-I: ");
            if (star.viError > 0 && star.viError < 10) {
                readoutText.append(String.format("%.3f (%.3f)", star.vMinusI, star.viError));
            } else {
                readoutText.append(String.format("%.3f", star.vMinusI));
            }
        }
        
        // AUID and source
        readoutText.append("   Existing Comp: ").append(star.auid);
        
        // Update the readout panel via parent Seqplot
        if (parentSeqplot != null) {
            parentSeqplot.setReadoutText(readoutText.toString());
        }
        
        System.out.printf("DEBUG: Clicked VSP star - AUID=%s, RA=%s, Dec=%s, V=%.3f\n",
                         star.auid, star.raStr, star.decStr, star.vmag);
    }
    
    private int findStarAtPosition(int x, int y) {
        // Use WCS-based finding if DSS2 image is active
        if (dss2Manager != null && dss2Manager.getCurrentWCS() != null && pointsVisible) {
            return findStarAtPositionWCS(x, y);
        } else {
            return findStarAtPositionTraditional(x, y);
        }
    }
    
    /**
     * Find star at position using WCS coordinate system (for DSS2 mode)
     */
    private int findStarAtPositionWCS(int x, int y) {
        AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        if (wcs == null) return -1;
        
        int totalCount = dataConnector.getTotalCount();
        if (totalCount == 0) return -1;
        
        // Get magnitude range for star sizing
        double minVMag = dataConnector.getMinVMag();
        double maxVMag = dataConnector.getMaxVMag();
        
        for (int i = 0; i < totalCount; i++) {
            double ra = dataConnector.getRa(i);
            double dec = dataConnector.getDec(i);
            double vMag = dataConnector.getVmag(i);
            
            // Skip stars with invalid coordinates or magnitudes
            if (Double.isNaN(ra) || Double.isNaN(dec) || vMag == 99.999 || Double.isNaN(vMag)) {
                continue;
            }
            
            // Convert RA/Dec to DSS2 pixel coordinates
            double[] dss2Pixel = wcs.worldToPixel(ra, dec);
            if (dss2Pixel == null) continue;
            
            // Scale DSS2 pixel to screen coordinates
            BufferedImage dss2Image = dss2Manager.getCurrentImage();
            if (dss2Image == null) continue;
            
            double scaleX = (double) getWidth() / dss2Image.getWidth();
            double scaleY = (double) getHeight() / dss2Image.getHeight();
            
            int screenX = (int) (dss2Pixel[0] * scaleX);
            int screenY = (int) ((wcs.naxis2 - dss2Pixel[1]) * scaleY); // Apply Y-flip like in drawing method
            
            // Calculate star size (same logic as drawStarsDirectWCS)
            double magRange = maxVMag - minVMag;
            int baseSize;
            if (magRange > 0) {
                double normalizedMag = (maxVMag - vMag) / magRange;
                baseSize = (int)(4 + normalizedMag * 16);
            } else {
                baseSize = 10;
            }
            
            // Minimum size for clickability
            int size = Math.max(8, baseSize);
            
            // Check if click is within star bounds (with some tolerance)
            double distance = Math.sqrt(Math.pow(x - screenX, 2) + Math.pow(y - screenY, 2));
            
            if (distance <= size / 2 + 5) {  // +5 pixels tolerance for easier clicking
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Find star at position using traditional tangent plane coordinates
     */
    private int findStarAtPositionTraditional(int x, int y) {
        int totalCount = dataConnector.getTotalCount();
        if (totalCount == 0) return -1;
        
        double[] viewBounds = computeViewBounds();
        double viewMinX = viewBounds[0];
        double viewMaxX = viewBounds[1];
        double viewMinY = viewBounds[2];
        double viewMaxY = viewBounds[3];
        
        // Get magnitude range for star sizing (same as drawStars)
        double minVMag = dataConnector.getMinVMag();
        double maxVMag = dataConnector.getMaxVMag();
        
        for (int i = 0; i < totalCount; i++) {
            int series = dataConnector.getSeries(i);
            double worldX = dataConnector.getXValue(series, i);
            double worldY = dataConnector.getYValue(series, i);
            double vMag = dataConnector.getVmag(i);
            
            // Skip stars with invalid magnitudes
            if (vMag == 99.999 || Double.isNaN(vMag)) continue;
            
            // Check if star is in view
            if (worldX < viewMinX || worldX > viewMaxX || worldY < viewMinY || worldY > viewMaxY) {
                continue;
            }
            
            int screenX = leftMargin + (int)((viewMaxX - worldX) / (viewMaxX - viewMinX) * plotWidth);
            int screenY = topMargin + (int)((viewMaxY - worldY) / (viewMaxY - viewMinY) * plotHeight);
            
            // Calculate star size (same logic as drawStars)
            double magRange = maxVMag - minVMag;
            int baseSize;
            if (magRange > 0) {
                double normalizedMag = (maxVMag - vMag) / magRange;
                baseSize = (int)(4 + normalizedMag * 20);
            } else {
                baseSize = 12;
            }
            int size = Math.max(2, (int)(baseSize * pointSizeScale * Math.sqrt(zoomLevel)));
            
            double distance = Math.sqrt(Math.pow(x - screenX, 2) + Math.pow(y - screenY, 2));
            
            if (distance <= size / 2 + 3) {
                return i;
            }
        }
        
        return -1;
    }

    private int findNearestStar(int x, int y) {
        if (dss2Manager != null && dss2Manager.getCurrentWCS() != null && pointsVisible) {
            return findNearestStarWCS(x, y);
        }
        return findNearestStarTraditional(x, y);
    }
    
    private int findNearestStarTraditional(int x, int y) {
        int totalCount = dataConnector.getTotalCount();
        if (totalCount == 0) return -1;
        
        double[] viewBounds = computeViewBounds();
        double viewMinX = viewBounds[0];
        double viewMaxX = viewBounds[1];
        double viewMinY = viewBounds[2];
        double viewMaxY = viewBounds[3];
        
        double minDistance = Double.MAX_VALUE;
        int nearestIndex = -1;
        
        for (int i = 0; i < totalCount; i++) {
            int series = dataConnector.getSeries(i);
            double worldX = dataConnector.getXValue(series, i);
            double worldY = dataConnector.getYValue(series, i);
            double vMag = dataConnector.getVmag(i);
            
            if (vMag == 99.999 || Double.isNaN(vMag)) continue;
            if (worldX < viewMinX || worldX > viewMaxX || worldY < viewMinY || worldY > viewMaxY) {
                continue;
            }
            
            int screenX = leftMargin + (int)((viewMaxX - worldX) / (viewMaxX - viewMinX) * plotWidth);
            int screenY = topMargin + (int)((viewMaxY - worldY) / (viewMaxY - viewMinY) * plotHeight);
            
            double distance = Point.distance(x, y, screenX, screenY);
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }
        
        return nearestIndex;
    }
    
    private int findNearestStarWCS(int x, int y) {
        if (dss2Manager == null) return -1;
        DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        BufferedImage image = dss2Manager.getCurrentImage();
        if (wcs == null || image == null) return -1;
        
        int totalCount = dataConnector.getTotalCount();
        if (totalCount == 0) return -1;
        
        double minDistance = Double.MAX_VALUE;
        int nearestIndex = -1;
        
        for (int i = 0; i < totalCount; i++) {
            double[] pixel = wcs.worldToPixel(dataConnector.getRa(i), dataConnector.getDec(i));
            if (pixel == null) continue;
            double dss2X = pixel[0];
            double dss2Y = pixel[1];
            if (dss2X < 0 || dss2X >= wcs.naxis1 || dss2Y < 0 || dss2Y >= wcs.naxis2) {
                continue;
            }
            int screenX = (int)(dss2X * getWidth() / wcs.naxis1);
            int screenY = (int)((wcs.naxis2 - dss2Y) * getHeight() / wcs.naxis2);
            double distance = Point.distance(x, y, screenX, screenY);
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }
        
        return nearestIndex;
    }
    
    private Point getScreenPositionForStar(int index) {
        if (index < 0) {
            return null;
        }
        if (dss2Manager != null && dss2Manager.getCurrentWCS() != null && pointsVisible) {
            return getScreenPositionForStarWCS(index);
        }
        return getScreenPositionForStarTraditional(index);
    }
    
    private Point getScreenPositionForStarTraditional(int index) {
        int series = dataConnector.getSeries(index);
        double worldX = dataConnector.getXValue(series, index);
        double worldY = dataConnector.getYValue(series, index);
        
        double[] viewBounds = computeViewBounds();
        double viewMinX = viewBounds[0];
        double viewMaxX = viewBounds[1];
        double viewMinY = viewBounds[2];
        double viewMaxY = viewBounds[3];
        
        if (worldX < viewMinX || worldX > viewMaxX || worldY < viewMinY || worldY > viewMaxY) {
            return null;
        }
        
        int screenX = leftMargin + (int)((viewMaxX - worldX) / (viewMaxX - viewMinX) * plotWidth);
        int screenY = topMargin + (int)((viewMaxY - worldY) / (viewMaxY - viewMinY) * plotHeight);
        return new Point(screenX, screenY);
    }
    
    private Point getScreenPositionForStarWCS(int index) {
        if (dss2Manager == null) {
            return null;
        }
        DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        BufferedImage image = dss2Manager.getCurrentImage();
        if (wcs == null || image == null) {
            return null;
        }
        double[] pixel = wcs.worldToPixel(dataConnector.getRa(index), dataConnector.getDec(index));
        if (pixel == null) {
            return null;
        }
        double dss2X = pixel[0];
        double dss2Y = pixel[1];
        if (dss2X < 0 || dss2X >= wcs.naxis1 || dss2Y < 0 || dss2Y >= wcs.naxis2) {
            return null;
        }
        int screenX = (int)(dss2X * getWidth() / wcs.naxis1);
        int screenY = (int)((wcs.naxis2 - dss2Y) * getHeight() / wcs.naxis2);
        return new Point(screenX, screenY);
    }
    
    private double[] computeViewBounds() {
        double minX = dataConnector.getMinX();
        double maxX = dataConnector.getMaxX();
        double minY = dataConnector.getMinY();
        double maxY = dataConnector.getMaxY();
        
        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double rangeX = (maxX - minX) / zoomLevel;
        double rangeY = (maxY - minY) / zoomLevel;
        
        double viewMinX = centerX - rangeX / 2.0 + panOffsetX;
        double viewMaxX = centerX + rangeX / 2.0 + panOffsetX;
        double viewMinY = centerY - rangeY / 2.0 + panOffsetY;
        double viewMaxY = centerY + rangeY / 2.0 + panOffsetY;
        
        return new double[]{viewMinX, viewMaxX, viewMinY, viewMaxY};
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos = e.getPoint();
        
        // Calculate RA/Dec at mouse position if we're in Sky View with WCS
        if (dss2Image != null && dss2Manager != null && dss2Manager.getCurrentWCS() != null) {
            DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
            
            // Convert screen coordinates to image pixel coordinates
            double imageX = (double)mousePos.x * wcs.naxis1 / getWidth();
            double imageY = (double)mousePos.y * wcs.naxis2 / getHeight();
            
            // Convert image pixel to world coordinates using WCS
            double[] worldCoords = wcs.pixelToWorld(imageX, imageY);
            if (worldCoords != null && !Double.isNaN(worldCoords[0]) && !Double.isNaN(worldCoords[1])) {
                double ra = worldCoords[0];
                double dec = worldCoords[1];
                
                // Format as sexagesimal
                mouseRaDecText = formatRaDec(ra, dec);
            } else {
                mouseRaDecText = null;
            }
            repaint();  // Repaint to show updated coordinates
        } else {
            mouseRaDecText = null;
        }
        
        int oldHovered = hoveredRecord;
        hoveredRecord = findStarAtPosition(mousePos.x, mousePos.y);
        
        if (hoveredRecord >= 0) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
        
        if (oldHovered != hoveredRecord) {
            repaint();
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (lastPanPoint != null && mousePressPoint != null) {
            // Check if total drag distance exceeds threshold
            int totalDragX = e.getX() - mousePressPoint.x;
            int totalDragY = e.getY() - mousePressPoint.y;
            double totalDragDistance = Math.sqrt(totalDragX * totalDragX + totalDragY * totalDragY);
            
            // Only start panning if drag distance exceeds threshold
            if (totalDragDistance < DRAG_THRESHOLD) {
                return;  // Ignore small drags that occur during clicks
            }
            
            isPanning = true;
            int deltaX = e.getX() - lastPanPoint.x;
            int deltaY = e.getY() - lastPanPoint.y;
            
            // Convert screen delta to world coordinates
            // In sky view with DSS2 image, accumulate pan offset (don't fetch image yet)
            if (dss2Image != null && dss2Manager != null && dss2Manager.getCurrentWCS() != null) {
                // Sky view mode - accumulate pan offset for later DSS2 fetch
                AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
                
                // Calculate the angular scale per screen pixel
                double screenPixelsPerImagePixelX = (double)getWidth() / wcs.naxis1;
                double screenPixelsPerImagePixelY = (double)getHeight() / wcs.naxis2;
                
                // Convert screen pixel delta to image pixel delta
                double imagePixelDeltaX = deltaX / screenPixelsPerImagePixelX;
                double imagePixelDeltaY = deltaY / screenPixelsPerImagePixelY;
                
                // Convert to angular delta using WCS CDELT values
                // The WCS cdelt values are already in the tangent plane projection,
                // so they already account for spherical geometry - no additional correction needed
                // Drag right means look right (increase RA), drag up means look up (increase Dec)
                double raDelta = -imagePixelDeltaX * wcs.cdelt1;  // Negate to pan opposite to drag
                double decDelta = imagePixelDeltaY * wcs.cdelt2;   // cdelt2 is usually negative, so this works out
                
                System.out.printf("DEBUG: Pan drag - deltaX=%d, deltaY=%d, imagePixelDelta=(%.2f,%.2f), raDelta=%.6f, decDelta=%.6f\n",
                                 deltaX, deltaY, imagePixelDeltaX, imagePixelDeltaY, raDelta, decDelta);
                
                // Accumulate pan offset
                panOffsetX += raDelta;
                panOffsetY += decDelta;
                
                System.out.printf("DEBUG: Accumulated panOffset - X=%.6f (RA), Y=%.6f (Dec)\n", panOffsetX, panOffsetY);
                
                // Repaint to show visual feedback during drag
                repaint();
            } else {
                // Points view mode - use database tangent plane coordinates
                double minX = dataConnector.getMinX();
                double maxX = dataConnector.getMaxX();
                double minY = dataConnector.getMinY();
                double maxY = dataConnector.getMaxY();
                
                double rangeX = (maxX - minX) / zoomLevel;
                double rangeY = (maxY - minY) / zoomLevel;
                
                // Drag right means pan right (positive worldDeltaX), drag left means pan left (negative)
                double worldDeltaX = deltaX * rangeX / plotWidth;
                double worldDeltaY = deltaY * rangeY / plotHeight;
                
                pan(worldDeltaX, worldDeltaY);
            }
            
            lastPanPoint = e.getPoint();
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // Request focus when clicked so keyboard events work
        requestFocusInWindow();
        
        // First check if we clicked on a VSP comparison star
        int vspStarIndex = findVSPStarAtPosition(e.getX(), e.getY());
        if (vspStarIndex >= 0) {
            // Display VSP star information
            displayVSPStarInfo(vspStarIndex);
            repaint();
            return;
        }
        
        // Otherwise, check regular catalog stars
        int clickedRecord = findStarAtPosition(e.getX(), e.getY());
        if (clickedRecord < 0 && pointsVisible) {
            clickedRecord = findNearestStar(e.getX(), e.getY());
        }
        if (clickedRecord >= 0) {
            // Update selected star
            selectedRecord = clickedRecord;
            hoveredRecord = clickedRecord;
            
            // Update parent Seqplot with clicked star info
            // (Seqplot will handle catalog preference internally)
            parentSeqplot.setRecordNumber(clickedRecord);
            
            // Get star details for enhanced feedback
            double ra = dataConnector.getRa(clickedRecord);
            double dec = dataConnector.getDec(clickedRecord);
            double vmag = dataConnector.getVmag(clickedRecord);
            double bMinusV = dataConnector.getBMinusV(clickedRecord);
            
            // Snap crosshairs to the precise star location if available
            Point starPoint = getScreenPositionForStar(clickedRecord);
            if (starPoint != null) {
                crosshairX = starPoint.x;
                crosshairY = starPoint.y;
                showCrosshairs = true;
            } else {
                crosshairX = e.getX();
                crosshairY = e.getY();
                showCrosshairs = pointsVisible;
            }
            
            System.out.printf("DEBUG: Clicked on star record %d - RA=%.6f, Dec=%.6f, VMag=%.2f, B-V=%.3f\n", 
                             clickedRecord, ra, dec, vmag, bMinusV);
            
            // Store the selected star index for sequence list
            dataConnector.setSelectedStarIndex(clickedRecord);
            
            // Force a repaint to ensure hover state is updated
            repaint();
        } else {
            // Clear selection and crosshairs if clicking on empty space
            selectedRecord = -1;
            showCrosshairs = false;
            // System.out.println("DEBUG: Click missed all stars - clearing selection");
            repaint();
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        lastPanPoint = e.getPoint();
        mousePressPoint = e.getPoint();  // Save initial press position
        isPanning = false;
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        boolean wasPanning = isPanning;
        lastPanPoint = null;
        mousePressPoint = null;  // Clear the initial press position
        isPanning = false;
        
        // If we were panning in Sky View mode, fetch new DSS2 image at panned location
        if (wasPanning && dss2Image != null && dss2Manager != null && dss2Manager.getCurrentWCS() != null) {
            AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
            
            // Calculate new center based on accumulated pan offset
            double newCenterRA = wcs.crval1 + panOffsetX;
            double newCenterDec = wcs.crval2 + panOffsetY;
            
            System.err.println(String.format("DEBUG: Sky view center calculation - original RA=%.6f, Dec=%.6f, panOffset=(%.6f,%.6f) -> new RA=%.6f, Dec=%.6f",
                wcs.crval1, wcs.crval2, panOffsetX, panOffsetY, newCenterRA, newCenterDec));
            System.err.println(String.format("DEBUG: Pan ended - new center: RA=%.6f, Dec=%.6f", newCenterRA, newCenterDec));
            System.err.println("DEBUG: Fetching new DSS2 image and re-querying database at panned location");
            
            // Reset pan offset before fetching (new image will be the new reference)
            panOffsetX = 0.0;
            panOffsetY = 0.0;
            
            // Fetch new DSS2 image at the new center
            dss2Manager.fetchDSS2Image(newCenterRA, newCenterDec, 
                                      dataConnector.getFieldSize(), 512);
            
            // Re-query database at the new center to get stars in the new field
            if (parentSeqplot != null) {
                parentSeqplot.requeryDatabaseAtCoordinates(newCenterRA, newCenterDec);
            }
        }
        
        // If we were panning in Points View and re-query is enabled, schedule a database update
        if (wasPanning && enablePanRequery) {
            schedulePanEndAction();
        }
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        // Request focus when mouse enters so keyboard shortcuts work
        requestFocusInWindow();
    }
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) {
            // Scroll up - zoom in
            zoomIn();
        } else {
            // Scroll down - zoom out
            zoomOut();
        }
    }
    
    // Methods to control point size (called by Seqplot menu actions)
    public void setPointSizeScale(double scale) {
        this.pointSizeScale = Math.max(0.1, Math.min(3.0, scale)); // Limit range
        repaint();
        System.out.printf("DEBUG: Point size scale set to %.2f\n", this.pointSizeScale);
    }
    
    public double getPointSizeScale() {
        return this.pointSizeScale;
    }
    
    public void increasePointSize() {
        setPointSizeScale(this.pointSizeScale * 1.2);
    }
    
    public void decreasePointSize() {
        setPointSizeScale(this.pointSizeScale * 0.8);
    }
    
    // Zoom and pan functionality for toolbar integration
    public void zoomOut() {
        if (zoomLevel > minZoomLevel) {
            zoomLevel *= 0.8;
            panOffsetX *= 0.8; // Adjust pan to maintain center
            panOffsetY *= 0.8;
            repaint();
            System.out.printf("DEBUG: Zoomed out to level %.2f\n", zoomLevel);
            
            // If in sky view, schedule DSS2 image fetch after zoom settles
            if (dss2Image != null && dss2Manager != null) {
                scheduleZoomEndAction();
            } else {
                // In points view, schedule catalog requery after zoom settles
                schedulePointsViewZoomEndAction();
            }
        }
    }
    
    public void zoomIn() {
        if (zoomLevel < maxZoomLevel) {
            zoomLevel *= 1.25;
            repaint();
            System.out.printf("DEBUG: Zoomed in to level %.2f\n", zoomLevel);
            
            // If in sky view, schedule DSS2 image fetch after zoom settles
            if (dss2Image != null && dss2Manager != null) {
                scheduleZoomEndAction();
            } else {
                // In points view, schedule catalog requery after zoom settles
                schedulePointsViewZoomEndAction();
            }
        }
    }
    
    public void resetZoom() {
        zoomLevel = 1.0;
        panOffsetX = 0.0;
        panOffsetY = 0.0;
        repaint();
        // System.out.println("DEBUG: Reset zoom to default");
    }
    
    public void pan(double deltaX, double deltaY) {
        panOffsetX += deltaX / zoomLevel;
        panOffsetY += deltaY / zoomLevel;
        repaint();
    }
    
    public double getZoomLevel() {
        return zoomLevel;
    }
    
    // Print functionality
    public void createPrintJob() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        
        if (printJob.printDialog()) {
            try {
                printJob.print();
                // System.out.println("DEBUG: Print job sent successfully");
            } catch (PrinterException ex) {
                System.err.println("Error printing: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Error printing: " + ex.getMessage(), 
                    "Print Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) graphics;
        
        // Save current state
        double oldZoom = zoomLevel;
        double oldPanX = panOffsetX;
        double oldPanY = panOffsetY;
        
        // Reset zoom and pan for printing
        zoomLevel = 1.0;
        panOffsetX = 0.0;
        panOffsetY = 0.0;
        
        // Get printable area
        double pageWidth = pageFormat.getImageableWidth();
        double pageHeight = pageFormat.getImageableHeight();
        
        // Translate to printable area
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // Scale to fit page while maintaining aspect ratio
        double scaleX = pageWidth / getWidth();
        double scaleY = pageHeight / getHeight();
        double scale = Math.min(scaleX, scaleY);
        
        g2d.scale(scale, scale);
        
        // Temporarily adjust colors for printing (black background -> white)
        Color savedBgColor = backgroundColor;
        Color savedAxisColor = axisColor;
        Color savedTextColor = textColor;
        Color savedGridColor = gridColor;
        
        backgroundColor = Color.WHITE;
        axisColor = Color.BLACK;
        textColor = Color.BLACK;
        gridColor = new Color(200, 200, 200);
        
        // Print the component
        paintComponent(g2d);
        
        // Restore colors and zoom state
        backgroundColor = savedBgColor;
        axisColor = savedAxisColor;
        textColor = savedTextColor;
        gridColor = savedGridColor;
        zoomLevel = oldZoom;
        panOffsetX = oldPanX;
        panOffsetY = oldPanY;
        
        return PAGE_EXISTS;
    }
    
    // Save functionality
    public void doSaveAs() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Plot As...");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JPEG Images", "jpg", "jpeg"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filename = file.getName().toLowerCase();
            String format = "png"; // default
            
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                format = "jpg";
            } else if (!filename.endsWith(".png")) {
                // Add .png extension if no extension provided
                file = new File(file.getParentFile(), file.getName() + ".png");
            }
            
            // Create image
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // Set high quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Save current zoom/pan state
            double oldZoom = zoomLevel;
            double oldPanX = panOffsetX;
            double oldPanY = panOffsetY;
            
            // Reset zoom and pan for saving
            zoomLevel = 1.0;
            panOffsetX = 0.0;
            panOffsetY = 0.0;
            
            // Paint the component to the image
            paintComponent(g2d);
            g2d.dispose();
            
            // Restore zoom/pan state
            zoomLevel = oldZoom;
            panOffsetX = oldPanX;
            panOffsetY = oldPanY;
            
            // Save the image
            ImageIO.write(image, format, file);
            System.out.printf("DEBUG: Plot saved as %s\n", file.getAbsolutePath());
            
            JOptionPane.showMessageDialog(this, 
                "Plot saved successfully to:\n" + file.getAbsolutePath(), 
                "Save Successful", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Refreshes the plot with new data from database queries
     * Call this method when limiting magnitude or other parameters change
     */
    public void refreshPlotData() {
        // System.out.println("DEBUG: StarPlotPanel.refreshPlotData() called");
        
        // Reset zoom and pan to show all new data
        zoomLevel = 1.0;
        panOffsetX = 0.0;
        panOffsetY = 0.0;
        
        // Force a complete repaint with new data
        repaint();
        revalidate();
        
        // System.out.println("DEBUG: StarPlotPanel refreshed with new data");
    }
    
    /**
     * Draw DSS2 background image using WCS-based coordinate transformation
     * This makes the DSS2 image the authoritative coordinate reference
     */
    private void drawDSS2Background(Graphics2D g2) {
        if (dss2Image == null) {
            return; // No DSS2 image to draw
        }
        
        // Get WCS parameters from DSS2Manager
        AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        if (wcs == null) {
            // System.out.println("DEBUG: No WCS parameters available for DSS2 background");
            return;
        }
        
        System.out.printf("DEBUG: Drawing DSS2 background with WCS: %s\n", wcs);
        
        // Get current view bounds in TANGENT PLANE coordinates (X, Y)
        double minX = dataConnector.getMinX();
        double maxX = dataConnector.getMaxX();
        double minY = dataConnector.getMinY();
        double maxY = dataConnector.getMaxY();
        
        // Apply zoom and pan transformations
        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double rangeX = (maxX - minX) / zoomLevel;
        double rangeY = (maxY - minY) / zoomLevel;
        
        double viewMinX = centerX - rangeX / 2.0 + panOffsetX;
        double viewMaxX = centerX + rangeX / 2.0 + panOffsetX;
        double viewMinY = centerY - rangeY / 2.0 + panOffsetY;
        double viewMaxY = centerY + rangeY / 2.0 + panOffsetY;
        
        System.out.printf("DEBUG: Current view bounds (tangent plane) - X(%.6f to %.6f), Y(%.6f to %.6f)\n",
                         viewMinX, viewMaxX, viewMinY, viewMaxY);
        System.out.printf("DEBUG: Tangent plane center: RA=%.6f, Dec=%.6f\n", 
                         dataConnector.getTangentPlaneCenterRA(), dataConnector.getTangentPlaneCenterDec());
        
        // Convert tangent plane corners to RA/Dec coordinates
        double[] bottomLeft = dataConnector.XYtoRaDec(viewMinX, viewMinY);
        double[] bottomRight = dataConnector.XYtoRaDec(viewMaxX, viewMinY);
        double[] topRight = dataConnector.XYtoRaDec(viewMaxX, viewMaxY);
        double[] topLeft = dataConnector.XYtoRaDec(viewMinX, viewMaxY);
        
        System.out.printf("DEBUG: Corner coordinates in RA/Dec:\n");
        System.out.printf("  Bottom-left: RA=%.6f, Dec=%.6f\n", bottomLeft[0], bottomLeft[1]);
        System.out.printf("  Bottom-right: RA=%.6f, Dec=%.6f\n", bottomRight[0], bottomRight[1]);
        System.out.printf("  Top-right: RA=%.6f, Dec=%.6f\n", topRight[0], topRight[1]);
        System.out.printf("  Top-left: RA=%.6f, Dec=%.6f\n", topLeft[0], topLeft[1]);
        
        // Convert plot corners from RA/Dec coordinates to DSS2 image pixel coordinates
        double[][] corners = {bottomLeft, bottomRight, topRight, topLeft};
        
        // Find the bounding box of DSS2 image pixels that correspond to our plot area
        double minPixelX = Double.MAX_VALUE;
        double maxPixelX = Double.MIN_VALUE;
        double minPixelY = Double.MAX_VALUE;
        double maxPixelY = Double.MIN_VALUE;
        
        for (double[] corner : corners) {
            double[] pixelCoords = wcs.worldToPixel(corner[0], corner[1]);
            minPixelX = Math.min(minPixelX, pixelCoords[0]);
            maxPixelX = Math.max(maxPixelX, pixelCoords[0]);
            minPixelY = Math.min(minPixelY, pixelCoords[1]);
            maxPixelY = Math.max(maxPixelY, pixelCoords[1]);
        }
        
        System.out.printf("DEBUG: DSS2 pixel bounds for plot area - X(%.2f to %.2f), Y(%.2f to %.2f)\n",
                         minPixelX, maxPixelX, minPixelY, maxPixelY);
        
        // Set clipping to plot area
        Rectangle originalClip = g2.getClipBounds();
        g2.setClip(leftMargin, topMargin, plotWidth, plotHeight);
        
        // Draw the DSS2 image region that corresponds to our plot bounds
        // Source rectangle in DSS2 image (clamped to image bounds)
        int srcX = Math.max(0, (int) Math.floor(minPixelX));
        int srcY = Math.max(0, (int) Math.floor(minPixelY));
        int srcX2 = Math.min(dss2Image.getWidth(), (int) Math.ceil(maxPixelX));
        int srcY2 = Math.min(dss2Image.getHeight(), (int) Math.ceil(maxPixelY));
        int srcWidth = srcX2 - srcX;
        int srcHeight = srcY2 - srcY;
        
        // Destination rectangle is the entire plot area
        int dstX = leftMargin;
        int dstY = topMargin;
        int dstWidth = plotWidth;
        int dstHeight = plotHeight;
        
        System.out.printf("DEBUG: Drawing DSS2 region - src(%d,%d,%dx%d) -> dst(%d,%d,%dx%d)\n",
                         srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight);
        
        if (srcWidth > 0 && srcHeight > 0) {
            // Apply inversion if enabled
            BufferedImage imageToDisplay = imageInverted ? invertImage(dss2Image) : dss2Image;
            
            g2.drawImage(imageToDisplay, 
                        dstX, dstY, dstX + dstWidth, dstY + dstHeight,  // destination
                        srcX, srcY, srcX + srcWidth, srcY + srcHeight,  // source
                        null);
            
            // System.out.println("DEBUG: DSS2 background drawn successfully with WCS alignment");
        } else {
            // System.out.println("DEBUG: DSS2 image region is outside plot bounds");
        }
        
        // Restore original clipping
        g2.setClip(originalClip);
    }
    
    /**
     * Set DSS2 background image with manager reference for WCS support
     * @param image The DSS2 image
     * @param manager The DSS2Manager containing WCS parameters
     */
    public void setDSS2Background(BufferedImage image, DSS2Manager manager) {
        this.dss2Image = image;
        this.dss2Manager = manager;
        
        if (manager != null && manager.hasWCS()) {
            System.out.printf("DEBUG: DSS2 background set with WCS: %s\n", manager.getCurrentWCS());
        } else {
            // System.out.println("DEBUG: DSS2 background set without WCS parameters");
        }
        
        repaint(); // Trigger redraw with new background
    }
    
    /**
     * Legacy method for backward compatibility - now uses WCS approach
     * @param image The DSS2 image
     * @param centerRA Center RA in degrees
     * @param centerDec Center Dec in degrees  
     * @param fov Field of view in degrees
     */
    public void setDSS2Background(BufferedImage image, double centerRA, double centerDec, double fov) {
        this.dss2Image = image;
        // For legacy calls, we don't have WCS - this should be avoided
        this.dss2Manager = null;
        
        System.out.printf("DEBUG: DSS2 background set (legacy mode) - RA=%.6f, Dec=%.6f, FOV=%.4f°\n",
                         centerRA, centerDec, fov);
        System.out.println("WARNING: Using legacy DSS2 mode without WCS - coordinate alignment may be imprecise");
        
        repaint(); // Trigger redraw with new background
    }
    
    /**
     * Clear DSS2 background image
     */
    public void clearDSS2Background() {
        this.dss2Image = null;
        this.dss2Manager = null;
        
        // System.out.println("DEBUG: DSS2 background cleared");
        
        repaint(); // Trigger redraw without background
    }
    
    // ==================== WCS-Based Rendering Methods ====================
    
    /**
     * Draw DSS2 image to fill the entire plot area as primary coordinate system
     */
    private void drawDSS2ImageFullscreen(Graphics2D g2) {
        if (dss2Image == null) return;
        
        // System.out.println("DEBUG: Drawing DSS2 image as fullscreen base layer");
        
        // Set clipping to plot area
        Rectangle originalClip = g2.getClipBounds();
        g2.setClip(leftMargin, topMargin, plotWidth, plotHeight);
        
        // Draw DSS2 image to exactly fill the plot area
        BufferedImage imageToDisplay = imageInverted ? invertImage(dss2Image) : dss2Image;
        g2.drawImage(imageToDisplay, leftMargin, topMargin, plotWidth, plotHeight, null);
        
        // Restore original clipping
        g2.setClip(originalClip);
        
        // System.out.println("DEBUG: DSS2 fullscreen image drawn");
    }
    
    /**
     * Draw stars positioned directly using DSS2 WCS coordinates
     */
    private void drawStarsWCSBased(Graphics2D g2) {
        if (!pointsVisible) {
            // System.out.println("DEBUG: Points hidden - skipping WCS star drawing");
            return;
        }
        
        AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        if (wcs == null) return;
        
        // System.out.println("DEBUG: Drawing stars using WCS coordinates");
        
        g2.setStroke(new BasicStroke(1));
        
        int totalCount = dataConnector.getTotalCount();
        if (totalCount == 0) return;
        
        // Use the same coordinate bounds as traditional view for consistency
        double minX = dataConnector.getMinX();
        double maxX = dataConnector.getMaxX();
        double minY = dataConnector.getMinY();
        double maxY = dataConnector.getMaxY();
        
        // Apply zoom and pan transformations (same as traditional view)
        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double rangeX = (maxX - minX) / zoomLevel;
        double rangeY = (maxY - minY) / zoomLevel;
        
        double viewMinX = centerX - rangeX / 2.0 + panOffsetX;
        double viewMaxX = centerX + rangeX / 2.0 + panOffsetX;
        double viewMinY = centerY - rangeY / 2.0 + panOffsetY;
        double viewMaxY = centerY + rangeY / 2.0 + panOffsetY;
        
        // Get magnitude range for star sizing
        double minVMag = dataConnector.getMinVMag();
        double maxVMag = dataConnector.getMaxVMag();
        
        // Set clipping rectangle to plot area only
        Rectangle originalClip = g2.getClipBounds();
        g2.setClip(leftMargin, topMargin, plotWidth, plotHeight);
        
        int drawnCount = 0;
        
        // Draw stars from all series using hybrid coordinate system
        for (int series = 0; series < 5; series++) {
            for (int i = 0; i < totalCount; i++) {
                if (dataConnector.getSeries(i) != series) continue;
                
                // Get star coordinates from traditional system (tangent plane)
                double worldX = dataConnector.getXValue(series, i);
                double worldY = dataConnector.getYValue(series, i);
                double vMag = dataConnector.getVmag(i);
                
                // Skip stars with invalid magnitudes
                if (vMag == 99.999 || Double.isNaN(vMag)) continue;
                
                // Check if star is in view (same logic as traditional view)
                if (worldX < viewMinX || worldX > viewMaxX || worldY < viewMinY || worldY > viewMaxY) {
                    continue;
                }
                
                // Convert to screen coordinates using same scaling as traditional view
                // This ensures perfect alignment between WCS and traditional coordinate systems
                int screenX = leftMargin + (int)((viewMaxX - worldX) / (viewMaxX - viewMinX) * plotWidth);
                int screenY = topMargin + (int)((viewMaxY - worldY) / (viewMaxY - viewMinY) * plotHeight);
                
                // Calculate star size based on magnitude (same as traditional view)
                // Use logarithmic scale for more dramatic size differences
                double magRange = maxVMag - minVMag;
                int starSize;
                if (magRange > 0) {
                    // Invert magnitude (brighter stars have lower magnitude values)
                    double normalizedMag = (maxVMag - vMag) / magRange; // 0 = faintest, 1 = brightest
                    
                    // Use exponential scaling for more dramatic visual difference
                    double sizeMultiplier = Math.pow(normalizedMag, 0.7); // Power < 1 gives more gradual transition
                    int baseSize = (int)(3 + sizeMultiplier * 25); // Size range: 3-28 pixels
                    
                    // Apply user point size scale
                    starSize = Math.max(2, (int)(baseSize * pointSizeScale));
                } else {
                    // All stars same magnitude
                    starSize = Math.max(2, (int)(12 * pointSizeScale));
                }
                
                // Get star color based on series
                Color starColor = (series < seriesColors.length) ? seriesColors[series] : Color.CYAN;
                
                // Draw star with body and edge
                g2.setColor(starColor);
                g2.fillOval(screenX - starSize/2, screenY - starSize/2, starSize, starSize);
                
                // Draw edge for contrast
                g2.setColor(starColor.darker());
                g2.drawOval(screenX - starSize/2, screenY - starSize/2, starSize, starSize);
                
                if (drawnCount < 5) { // Debug first few stars
                    System.out.printf("DEBUG: WCS Star %d: world(%.6f,%.6f) -> screen(%d,%d) vMag=%.2f size=%d series=%d\\n", 
                                     drawnCount, worldX, worldY, screenX, screenY, vMag, starSize, series);
                }
                
                drawnCount++;
            }
        }
        
        // Restore original clipping
        g2.setClip(originalClip);
        
        System.out.printf("DEBUG: Total WCS stars drawn: %d, Mag range: %.2f to %.2f\\n", 
                         drawnCount, minVMag, maxVMag);
    }
    
    /**
     * Draw minimal coordinate axes for WCS-based view
     */
    private void drawWCSAxes(Graphics2D g2) {
        AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        if (wcs == null) return;
        
        g2.setColor(axisColor);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        
        // Draw border around plot area
        g2.drawRect(leftMargin, topMargin, plotWidth, plotHeight);
        
        // Add corner coordinate labels
        double[] cornerRA_Dec = wcs.pixelToWorld(0, 0);
        String cornerLabel = String.format("%.3f°, %.3f°", cornerRA_Dec[0], cornerRA_Dec[1]);
        g2.drawString(cornerLabel, leftMargin + 5, topMargin + 15);
        
        // Add center coordinates
        double[] centerRA_Dec = wcs.pixelToWorld(wcs.naxis1/2, wcs.naxis2/2);
        String centerLabel = String.format("Center: %.6f°, %.6f°", centerRA_Dec[0], centerRA_Dec[1]);
        g2.drawString(centerLabel, leftMargin + 5, getHeight() - 10);
        
        // System.out.println("DEBUG: WCS axes drawn with coordinate labels");
    }
    
    /**
     * Draw tooltip for WCS-based mode
     */
    private void drawTooltipWCS(Graphics2D g2) {
        // For now, reuse the existing tooltip method
        drawTooltip(g2);
    }
    
    /**
     * Draw simple bounding box around the image
     */
    private void drawImageBoundingBox(Graphics2D g2) {
        // Draw a simple white border around the entire image
        g2.setColor(new Color(255, 255, 255, 100)); // Semi-transparent white
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
        g2.setStroke(new BasicStroke(1)); // Reset
    }
    
    /**
     * Draw coordinate grid (RA/Dec lines) over the DSS2 image - NOT USED ANYMORE
     */
    private void drawWCSCoordinateGrid(Graphics2D g2) {
        AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        if (wcs == null) return;
        
        // Use faint green color for grid lines
        g2.setColor(new Color(0, 255, 0, 80)); // Faint green with transparency
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                                   0, new float[]{5, 5}, 0)); // Dashed line
        
        // Calculate approximate grid spacing based on field of view
        double centerRA = wcs.crval1;
        double centerDec = wcs.crval2;
        double pixelScaleRA = Math.abs(wcs.cdelt1); // degrees per pixel
        double pixelScaleDec = Math.abs(wcs.cdelt2);
        
        // Field of view in degrees
        double fovRA = pixelScaleRA * wcs.naxis1;
        double fovDec = pixelScaleDec * wcs.naxis2;
        
        // Determine grid spacing - aim for about 5-8 grid lines across the image
        double raSpacing = calculateGridSpacing(fovRA);
        double decSpacing = calculateGridSpacing(fovDec);
        
        double absDec = Math.abs(centerDec);
        
        // At very high declinations (|Dec| > 75°), skip RA lines entirely - they're too messy
        boolean drawRALines = absDec <= 75.0;
        
        // At high declinations (60° < |Dec| <= 75°), draw fewer RA lines
        if (drawRALines && absDec > 60.0) {
            // Draw only every 4th or 6th RA line depending on declination
            if (absDec > 70.0) {
                raSpacing *= 6.0; // Very few lines
            } else {
                raSpacing *= 4.0; // Moderately fewer lines
            }
        }
        
        // Keep extension modest
        double raRange = fovRA * 0.6;
        double decRange = fovDec * 0.6;
        
        // Draw RA lines (vertical lines in most orientations) - only if not too close to poles
        if (drawRALines) {
            double startRA = Math.floor((centerRA - raRange) / raSpacing) * raSpacing;
            double endRA = Math.ceil((centerRA + raRange) / raSpacing) * raSpacing;
            for (double ra = startRA; ra <= endRA; ra += raSpacing) {
                drawRALine(g2, ra, centerDec - decRange, centerDec + decRange, wcs);
            }
        }
        
        // Draw Dec lines (horizontal lines in most orientations)  
        double startDec = Math.floor((centerDec - decRange) / decSpacing) * decSpacing;
        double endDec = Math.ceil((centerDec + decRange) / decSpacing) * decSpacing;
        for (double dec = startDec; dec <= endDec; dec += decSpacing) {
            drawDecLine(g2, dec, centerRA - raRange, centerRA + raRange, wcs);
        }
        
        // Draw coordinate labels
        drawCoordinateLabels(g2, wcs, raSpacing, decSpacing, centerRA, centerDec, raRange, decRange);
        
        // Reset stroke
        g2.setStroke(new BasicStroke(1));
    }
    
    /**
     * Calculate appropriate grid spacing for coordinate lines
     */
    private double calculateGridSpacing(double fieldOfView) {
        // Aim for about 5-8 grid lines across the image
        double rawSpacing = fieldOfView / 6.0;
        
        // Round to nice values: 1, 2, 5, 10, 20, 50, etc. (in degrees or arcminutes)
        double[] niceValues;
        if (rawSpacing > 1.0) {
            // Use degree intervals
            niceValues = new double[]{1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0};
        } else if (rawSpacing > 1.0/60.0) {
            // Use arcminute intervals (convert to degrees)
            niceValues = new double[]{1.0/60, 2.0/60, 5.0/60, 10.0/60, 20.0/60, 30.0/60};
        } else {
            // Use arcsecond intervals (convert to degrees)
            niceValues = new double[]{1.0/3600, 2.0/3600, 5.0/3600, 10.0/3600, 20.0/3600, 30.0/3600, 60.0/3600};
        }
        
        // Find the closest nice value
        double bestSpacing = niceValues[0];
        for (double spacing : niceValues) {
            if (Math.abs(spacing - rawSpacing) < Math.abs(bestSpacing - rawSpacing)) {
                bestSpacing = spacing;
            }
        }
        
        return bestSpacing;
    }
    
    /**
     * Draw a line of constant RA (vertical-ish line)
     */
    private void drawRALine(Graphics2D g2, double ra, double decStart, double decEnd, 
                           AAVSOtools.DSS2Manager.WCSParameters wcs) {
        // Use more steps at high declinations for smoother curved lines
        double avgDec = (decStart + decEnd) / 2.0;
        int steps = Math.abs(avgDec) > 60.0 ? 100 : 50; // More steps near poles
        double decStep = (decEnd - decStart) / steps;
        
        // Use lists to collect valid points, allowing gaps in the line
        java.util.ArrayList<Integer> xPoints = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> yPoints = new java.util.ArrayList<>();
        
        for (int i = 0; i <= steps; i++) {
            double dec = decStart + i * decStep;
            double[] pixel = wcs.worldToPixel(ra, dec);
            
            if (pixel != null) {
                int screenX = (int)(pixel[0] * getWidth() / wcs.naxis1);
                int screenY = (int)((wcs.naxis2 - pixel[1]) * getHeight() / wcs.naxis2);
                
                // Only include points that are on or near the visible screen
                if (screenX >= -50 && screenX < getWidth() + 50 && 
                    screenY >= -50 && screenY < getHeight() + 50) {
                    xPoints.add(screenX);
                    yPoints.add(screenY);
                }
            }
        }
        
        // Draw line segments
        if (xPoints.size() > 1) {
            for (int i = 1; i < xPoints.size(); i++) {
                g2.drawLine(xPoints.get(i-1), yPoints.get(i-1), xPoints.get(i), yPoints.get(i));
            }
        }
    }
    
    /**
     * Draw a line of constant Dec (horizontal-ish line)
     */
    private void drawDecLine(Graphics2D g2, double dec, double raStart, double raEnd, 
                            AAVSOtools.DSS2Manager.WCSParameters wcs) {
        // Use more steps at high declinations where RA lines are compressed
        int steps = Math.abs(dec) > 60.0 ? 100 : 50; // More steps near poles
        double raStep = (raEnd - raStart) / steps;
        
        // Use lists to collect valid points, allowing gaps in the line
        java.util.ArrayList<Integer> xPoints = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> yPoints = new java.util.ArrayList<>();
        
        for (int i = 0; i <= steps; i++) {
            double ra = raStart + i * raStep;
            double[] pixel = wcs.worldToPixel(ra, dec);
            
            if (pixel != null) {
                int screenX = (int)(pixel[0] * getWidth() / wcs.naxis1);
                int screenY = (int)((wcs.naxis2 - pixel[1]) * getHeight() / wcs.naxis2);
                
                // Only include points that are on or near the visible screen
                if (screenX >= -50 && screenX < getWidth() + 50 && 
                    screenY >= -50 && screenY < getHeight() + 50) {
                    xPoints.add(screenX);
                    yPoints.add(screenY);
                }
            }
        }
        
        // Draw line segments
        if (xPoints.size() > 1) {
            for (int i = 1; i < xPoints.size(); i++) {
                g2.drawLine(xPoints.get(i-1), yPoints.get(i-1), xPoints.get(i), yPoints.get(i));
            }
        }
    }
    
    /**
     * Draw compass indicator (N, E arrows) in top left corner
     */
    private void drawCompassIndicator(Graphics2D g2) {
        AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        if (wcs == null) return;
        
        // Position in top left corner, moved down to avoid cutoff
        int baseX = 30;
        int baseY = 50; // Moved down from 30 to 50
        int arrowLength = 25;
        
        // Calculate North direction (Dec +1 degree from center)
        double centerRA = wcs.crval1;
        double centerDec = wcs.crval2;
        
        double[] centerPixel = wcs.worldToPixel(centerRA, centerDec);
        double[] northPixel = wcs.worldToPixel(centerRA, centerDec + 0.1); // 0.1 degree north
        
        if (centerPixel != null && northPixel != null) {
            // Calculate North direction vector
            double northDX = northPixel[0] - centerPixel[0];
            double northDY = northPixel[1] - centerPixel[1];  // Note: no Y-flip here, working in DSS2 pixel space
            double northLength = Math.sqrt(northDX*northDX + northDY*northDY);
            
            if (northLength > 0) {
                // Normalize and scale to arrow length
                northDX = (northDX / northLength) * arrowLength;
                northDY = (northDY / northLength) * arrowLength;
                
                // Calculate East direction (perpendicular to North, 90 degrees clockwise)
                double eastDX = northDY;  // 90 degree rotation
                double eastDY = -northDX;
                
                // Set up graphics for compass
                g2.setColor(new Color(0, 255, 0, 200)); // Semi-transparent green
                g2.setStroke(new BasicStroke(2));
                
                // Draw North arrow
                int northEndX = baseX + (int)northDX;
                int northEndY = baseY - (int)northDY; // Apply Y-flip for screen coordinates
                g2.drawLine(baseX, baseY, northEndX, northEndY);
                drawArrowHead(g2, baseX, baseY, northEndX, northEndY);
                
                // Draw East arrow
                int eastEndX = baseX + (int)eastDX;
                int eastEndY = baseY - (int)eastDY; // Apply Y-flip for screen coordinates
                g2.drawLine(baseX, baseY, eastEndX, eastEndY);
                drawArrowHead(g2, baseX, baseY, eastEndX, eastEndY);
                
                // Label the arrows
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                
                // North label
                int nLabelX = northEndX - fm.stringWidth("N")/2;
                int nLabelY = northEndY - 5;
                g2.drawString("N", nLabelX, nLabelY);
                
                // East label
                int eLabelX = eastEndX - fm.stringWidth("E")/2;
                int eLabelY = eastEndY - 5;
                g2.drawString("E", eLabelX, eLabelY);
                
                // Reset stroke
                g2.setStroke(new BasicStroke(1));
            }
        }
    }
    
    /**
     * Draw an arrow head at the end of a line
     */
    private void drawArrowHead(Graphics2D g2, int startX, int startY, int endX, int endY) {
        double dx = endX - startX;
        double dy = endY - startY;
        double length = Math.sqrt(dx*dx + dy*dy);
        
        if (length > 0) {
            // Normalize direction vector
            dx /= length;
            dy /= length;
            
            // Arrow head size
            int headLength = 8;
            double headAngle = Math.PI / 6; // 30 degrees
            
            // Calculate arrow head points
            double cos = Math.cos(headAngle);
            double sin = Math.sin(headAngle);
            
            int x1 = endX - (int)(headLength * (dx * cos + dy * sin));
            int y1 = endY - (int)(headLength * (dy * cos - dx * sin));
            
            int x2 = endX - (int)(headLength * (dx * cos - dy * sin));
            int y2 = endY - (int)(headLength * (dy * cos + dx * sin));
            
            // Draw arrow head
            g2.drawLine(endX, endY, x1, y1);
            g2.drawLine(endX, endY, x2, y2);
        }
    }
    
    private void drawCoordinateLabels(Graphics2D g2, DSS2Manager.WCSParameters wcs,
                                     double raSpacing, double decSpacing,
                                     double centerRA, double centerDec,
                                     double raRange, double decRange) {
        g2.setColor(new Color(0, 255, 0, 120)); // Slightly brighter green for labels
        Font labelFont = new Font("Arial", Font.PLAIN, 11);
        g2.setFont(labelFont);
        FontMetrics fm = g2.getFontMetrics();
        
        // Draw RA labels at bottom edge
        double startRA = Math.floor((centerRA - raRange) / raSpacing) * raSpacing;
        double endRA = Math.ceil((centerRA + raRange) / raSpacing) * raSpacing;
        for (double ra = startRA; ra <= endRA; ra += raSpacing) {
            double[] pixel = wcs.worldToPixel(ra, centerDec - decRange * 0.8);
            if (pixel != null && pixel[0] >= 0 && pixel[0] <= getWidth()) {
                String label = formatRA(ra);
                int labelWidth = fm.stringWidth(label);
                g2.drawString(label, (int)(pixel[0] - labelWidth/2), getHeight() - 5);
            }
        }
        
        // Draw Dec labels at right edge
        double startDec = Math.floor((centerDec - decRange) / decSpacing) * decSpacing;
        double endDec = Math.ceil((centerDec + decRange) / decSpacing) * decSpacing;
        for (double dec = startDec; dec <= endDec; dec += decSpacing) {
            double[] pixel = wcs.worldToPixel(centerRA + raRange * 0.8, dec);
            if (pixel != null && pixel[1] >= 0 && pixel[1] <= getHeight()) {
                String label = formatDec(dec);
                g2.drawString(label, getWidth() - fm.stringWidth(label) - 5, (int)(pixel[1] + fm.getAscent()/2));
            }
        }
    }
    
    private String formatRA(double ra) {
        // Normalize RA to 0-360 range
        while (ra < 0) ra += 360;
        while (ra >= 360) ra -= 360;
        
        // Convert to hours:minutes
        double hours = ra / 15.0;
        int h = (int) hours;
        int m = (int) ((hours - h) * 60);
        return String.format("%02dh%02dm", h, m);
    }
    
    private String formatDec(double dec) {
        // Format as degrees:arcminutes
        char sign = dec >= 0 ? '+' : '-';
        dec = Math.abs(dec);
        int d = (int) dec;
        int m = (int) ((dec - d) * 60);
        return String.format("%c%02d°%02d'", sign, d, m);
    }
    
    /**
     * Schedule action after panning ends (re-query database or fetch new FITS image)
     */
    private void schedulePanEndAction() {
        // Cancel any existing timer
        if (panEndTimer != null) {
            panEndTimer.stop();
        }
        
        // Create a timer that fires after 500ms of no panning
        panEndTimer = new javax.swing.Timer(500, e -> {
            if (dss2Image != null) {
                // In sky view - fetch new FITS image at new center
                requeryAtNewCenter(true);
            } else {
                // In points view - re-query database at new center
                requeryAtNewCenter(false);
            }
            panEndTimer.stop();
        });
        panEndTimer.setRepeats(false);
        panEndTimer.start();
    }
    
    /**
     * Schedule action after zooming ends (fetch new DSS2 image at new FOV)
     */
    private void scheduleZoomEndAction() {
        // Cancel any existing timer
        if (zoomEndTimer != null) {
            zoomEndTimer.stop();
        }
        
        // Create a timer that fires after 800ms of no zooming
        zoomEndTimer = new javax.swing.Timer(800, e -> {
            if (dss2Manager != null && dss2Manager.getCurrentWCS() != null) {
                fetchDSS2AtCurrentZoom();
            }
            zoomEndTimer.stop();
        });
        zoomEndTimer.setRepeats(false);
        zoomEndTimer.start();
    }
    
    /**
     * Schedule action after zooming ends in points view (requery catalog with new FOV)
     */
    private void schedulePointsViewZoomEndAction() {
        // Cancel any existing timer
        if (zoomEndTimer != null) {
            zoomEndTimer.stop();
        }
        
        // Create a timer that fires after 800ms of no zooming
        zoomEndTimer = new javax.swing.Timer(800, e -> {
            requeryAtCurrentZoom();
            zoomEndTimer.stop();
        });
        zoomEndTimer.setRepeats(false);
        zoomEndTimer.start();
    }
    
    /**
     * Requery catalog at the current zoom level (adjusting FOV)
     */
    private void requeryAtCurrentZoom() {
        // Calculate current center (accounting for pan)
        double[] centerCoords = calculateCurrentCenter();
        if (centerCoords == null) {
            System.out.println("DEBUG: Could not calculate current center for requery");
            return;
        }
        
        final double centerRA = centerCoords[0];
        final double centerDec = centerCoords[1];
        
        // Calculate new FOV based on zoom level
        // Original FOV is stored in dataConnector
        double originalFOV = dataConnector.getFieldSize();
        double newFOV = originalFOV / zoomLevel;
        
        // Clamp FOV to reasonable limits (0.01 to 10 degrees)
        newFOV = Math.max(0.01, Math.min(10.0, newFOV));
        
        final double finalFOV = newFOV;
        
        System.out.printf("DEBUG: Zoom ended in points view - requerying at RA=%.6f, Dec=%.6f, FOV=%.4f (zoom=%.2f)\n", 
                         centerRA, centerDec, finalFOV, zoomLevel);
        
        // Requery in background thread
        new Thread(() -> {
            try {
                parentSeqplot.requeryDatabaseAtCoordinatesWithFOV(centerRA, centerDec, finalFOV);
            } catch (Exception e) {
                System.err.println("Error requerying at new zoom: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Fetch new DSS2 image at the current zoom level (adjusting FOV)
     */
    private void fetchDSS2AtCurrentZoom() {
        if (fetchingDSS2) {
            System.out.println("DEBUG: Already fetching DSS2, skipping zoom fetch");
            return;
        }
        
        AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
        if (wcs == null) return;
        
        // Calculate current center (accounting for pan)
        double[] centerCoords = calculateCurrentCenter();
        if (centerCoords == null) return;
        
        final double centerRA = centerCoords[0];
        final double centerDec = centerCoords[1];
        
        // Calculate new FOV based on zoom level
        // Original FOV is stored in WCS pixel scale times image size
        double originalFOV = Math.abs(wcs.cdelt1) * wcs.naxis1;
        double newFOV = originalFOV / zoomLevel;
        
        // Clamp FOV to reasonable limits (0.01 to 10 degrees)
        newFOV = Math.max(0.01, Math.min(10.0, newFOV));
        
        final double finalFOV = newFOV;
        
        System.out.printf("DEBUG: Zoom ended - fetching DSS2 at RA=%.6f, Dec=%.6f, FOV=%.4f (zoom=%.2f)\n", 
                         centerRA, centerDec, finalFOV, zoomLevel);
        
        // Show progress indicator
        fetchingDSS2 = true;
        
        // Fetch new image in background thread
        new Thread(() -> {
            try {
                parentSeqplot.fetchDSS2ImageAtCoordinatesWithFOV(centerRA, centerDec, finalFOV);
            } finally {
                fetchingDSS2 = false;
            }
        }).start();
    }
    
    /**
     * Re-query database or fetch new FITS at the current view center
     */
    private void requeryAtNewCenter(boolean fetchFITS) {
        // Calculate the new center coordinates in RA/Dec
        double[] centerCoords = calculateCurrentCenter();
        if (centerCoords == null) {
            // System.out.println("DEBUG: Could not calculate new center coordinates");
            return;
        }
        
        double newRA = centerCoords[0];
        double newDec = centerCoords[1];
        
        System.out.printf("DEBUG: Pan ended - new center: RA=%.6f, Dec=%.6f\n", newRA, newDec);
        
        if (fetchFITS) {
            // In sky view - fetch new DSS2 image
            // System.out.println("DEBUG: Fetching new DSS2 image at panned location");
            parentSeqplot.fetchDSS2ImageAtCoordinates(newRA, newDec);
        } else {
            // In points view - re-query database
            // System.out.println("DEBUG: Re-querying database at panned location");
            parentSeqplot.requeryDatabaseAtCoordinates(newRA, newDec);
        }
    }
    
    /**
     * Calculate the current center RA/Dec after panning/zooming
     */
    private double[] calculateCurrentCenter() {
        // In sky view mode with WCS, calculate center directly from WCS and pan offsets
        if (dss2Image != null && dss2Manager != null && dss2Manager.getCurrentWCS() != null) {
            AAVSOtools.DSS2Manager.WCSParameters wcs = dss2Manager.getCurrentWCS();
            
            // The original image center in RA/Dec
            double centerRA = wcs.crval1;
            double centerDec = wcs.crval2;
            
            // Pan offsets are in degrees (accumulated from WCS-based panning)
            // panOffsetX is already corrected for cos(dec) during accumulation
            // so we apply it directly to RA
            double newRA = centerRA + panOffsetX;
            double newDec = centerDec + panOffsetY;
            
            // Normalize RA to 0-360 range
            while (newRA < 0) newRA += 360;
            while (newRA >= 360) newRA -= 360;
            
            // Clamp Dec to valid range
            if (newDec > 90) newDec = 90;
            if (newDec < -90) newDec = -90;
            
            System.out.printf("DEBUG: Sky view center calculation - original RA=%.6f, Dec=%.6f, panOffset=(%.6f,%.6f) -> new RA=%.6f, Dec=%.6f\n",
                             centerRA, centerDec, panOffsetX, panOffsetY, newRA, newDec);
            
            return new double[]{newRA, newDec};
        }
        
        // Points view mode - if no panning, just use the original database center
        if (Math.abs(panOffsetX) < 1e-10 && Math.abs(panOffsetY) < 1e-10) {
            double centerRA = dataConnector.getCentralRA();
            double centerDec = dataConnector.getCentralDec();
            System.out.printf("DEBUG: Points view center (no panning) - using original center RA=%.6f, Dec=%.6f\n",
                             centerRA, centerDec);
            return new double[]{centerRA, centerDec};
        }
        
        // If there is panning, use database tangent plane coordinates
        double minX = dataConnector.getMinX();
        double maxX = dataConnector.getMaxX();
        double minY = dataConnector.getMinY();
        double maxY = dataConnector.getMaxY();
        
        double rangeX = (maxX - minX) / zoomLevel;
        double rangeY = (maxY - minY) / zoomLevel;
        
        // Calculate center in world coordinates
        double centerX = minX + rangeX / 2 + panOffsetX;
        double centerY = minY + rangeY / 2 + panOffsetY;
        
        System.out.printf("DEBUG: Points view center calculation (with panning) - minX=%.6f, maxX=%.6f, minY=%.6f, maxY=%.6f\n",
                         minX, maxX, minY, maxY);
        System.out.printf("DEBUG: zoomLevel=%.2f, rangeX=%.6f, rangeY=%.6f, panOffset=(%.6f,%.6f)\n",
                         zoomLevel, rangeX, rangeY, panOffsetX, panOffsetY);
        System.out.printf("DEBUG: Tangent plane center: centerX=%.6f, centerY=%.6f\n", centerX, centerY);
        
        // Convert tangent plane coordinates back to RA/Dec
        double[] raDec = tangentPlaneToRADec(centerX, centerY);
        System.out.printf("DEBUG: Converted to RA=%.6f, Dec=%.6f\n", raDec[0], raDec[1]);
        return raDec;
    }
    
    /**
     * Convert tangent plane coordinates to RA/Dec
     * The tangent plane coordinates (x, y) are in degrees (as per DataConnector.RaDectoXY)
     */
    private double[] tangentPlaneToRADec(double x, double y) {
        // Get the original center coordinates
        double centerRA = dataConnector.getCentralRA();
        double centerDec = dataConnector.getCentralDec();
        
        // Convert center to radians
        double raRad = Math.toRadians(centerRA);
        double decRad = Math.toRadians(centerDec);
        
        // Tangent plane coordinates are in degrees, convert to radians for calculation
        double xi = Math.toRadians(x);
        double eta = Math.toRadians(y);
        
        // Inverse tangent plane projection (standard gnomonic projection)
        double rho = Math.sqrt(xi * xi + eta * eta);
        if (rho < 1e-10) {
            // At the center
            return new double[]{centerRA, centerDec};
        }
        
        double c = Math.atan(rho);
        double sinC = Math.sin(c);
        double cosC = Math.cos(c);
        
        double newDecRad = Math.asin(cosC * Math.sin(decRad) + 
                                      (eta * sinC * Math.cos(decRad) / rho));
        
        double newRARad = raRad + Math.atan2(xi * sinC, 
                                              rho * Math.cos(decRad) * cosC - 
                                              eta * Math.sin(decRad) * sinC);
        
        double newRA = Math.toDegrees(newRARad);
        double newDec = Math.toDegrees(newDecRad);
        
        // Normalize RA to 0-360 range
        while (newRA < 0) newRA += 360;
        while (newRA >= 360) newRA -= 360;
        
        return new double[]{newRA, newDec};
    }
    
    /**
     * Set loading indicator state
     */
    public void setLoadingIndicator(boolean loading) {
        setLoadingIndicator(loading, "Searching database");
    }
    
    /**
     * Set loading indicator state with custom message
     */
    public void setLoadingIndicator(boolean loading, String message) {
        this.showLoadingIndicator = loading;
        this.loadingMessage = message;
        
        if (loading) {
            // Start animation timer
            if (loadingTimer == null) {
                loadingTimer = new javax.swing.Timer(50, e -> repaint());
            }
            loadingTimer.start();
        } else {
            // Stop animation timer
            if (loadingTimer != null) {
                loadingTimer.stop();
            }
        }
        
        repaint();
    }
    
    /**
     * Draw loading indicator
     */
    private void drawLoadingIndicator(Graphics2D g2) {
        if (!showLoadingIndicator) return;
        
        // Semi-transparent overlay
        g2.setColor(new Color(0, 0, 0, 128));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Blue loading indicator
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = 20;
        
        g2.setColor(new Color(0, 100, 255));
        g2.setStroke(new BasicStroke(4));
        
        // Animated spinner
        long time = System.currentTimeMillis();
        int startAngle = (int)((time / 10) % 360);
        g2.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, 90);
        
        // Loading text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        String text = this.loadingMessage;
        int textWidth = fm.stringWidth(text);
        g2.drawString(text, centerX - textWidth/2, centerY + radius + 30);
    }
    
    /**
     * Set whether star points should be visible
     * @param visible true to show points, false to hide them
     */
    public void setPointsVisible(boolean visible) {
        this.pointsVisible = visible;
        System.out.printf("DEBUG: Points visibility set to: %s\n", visible);
        repaint(); // Trigger redraw with new visibility setting
    }
    
    /**
     * Set image inversion state for DSS2 background
     */
    public void setImageInverted(boolean inverted) {
        this.imageInverted = inverted;
        System.out.printf("DEBUG: Image inversion set to: %s\n", inverted);
        repaint(); // Trigger redraw with new inversion setting
    }
    
    /**
     * Invert colors of a BufferedImage (black becomes white, white becomes black)
     */
    private BufferedImage invertImage(BufferedImage original) {
        if (original == null) return null;
        
        BufferedImage inverted = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                int rgb = original.getRGB(x, y);
                
                // Extract RGB components
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                
                // Invert each component
                red = 255 - red;
                green = 255 - green;
                blue = 255 - blue;
                
                // Reassemble RGB
                int invertedRgb = (red << 16) | (green << 8) | blue;
                inverted.setRGB(x, y, invertedRgb);
            }
        }
        
        return inverted;
    }
    
    /**
     * Check if currently in Sky View mode (DSS2 image is loaded)
     */
    public boolean isInSkyViewMode() {
        return (dss2Image != null && dss2Manager != null);
    }
    
    // KeyListener implementation for keyboard shortcuts
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("DEBUG: Key pressed - code=" + keyCode + ", char='" + e.getKeyChar() + "'");
        
        // Handle zoom shortcuts
        if (keyCode == KeyEvent.VK_EQUALS || keyCode == KeyEvent.VK_PLUS) {
            // Zoom in with + or = key
            System.out.println("DEBUG: Zoom in triggered");
            zoomIn();
        } else if (keyCode == KeyEvent.VK_MINUS || keyCode == KeyEvent.VK_UNDERSCORE) {
            // Zoom out with - key
            System.out.println("DEBUG: Zoom out triggered");
            zoomOut();
        } else if (keyCode == KeyEvent.VK_0 && e.isMetaDown()) {
            // Reset zoom with Cmd+0 (Mac) or Ctrl+0 (Windows/Linux)
            System.out.println("DEBUG: Reset zoom triggered");
            resetZoom();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }
    
    /**
     * Draw mouse coordinate box in bottom-left corner
     */
    private void drawMouseCoordinates(Graphics2D g2) {
        if (mouseRaDecText == null) return;
        
        // Set up font and get metrics
        Font font = new Font("Monospaced", Font.PLAIN, 12);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        
        // Calculate box dimensions
        int textWidth = fm.stringWidth(mouseRaDecText);
        int textHeight = fm.getHeight();
        int padding = 5;
        int boxWidth = textWidth + 2 * padding;
        int boxHeight = textHeight + 2 * padding;
        
        // Position in bottom-left corner
        int boxX = 10;
        int boxY = getHeight() - boxHeight - 10;
        
        // Draw semi-transparent black background
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(boxX, boxY, boxWidth, boxHeight);
        
        // Draw border
        g2.setColor(Color.YELLOW);
        g2.drawRect(boxX, boxY, boxWidth, boxHeight);
        
        // Draw text
        g2.setColor(Color.YELLOW);
        int textX = boxX + padding;
        int textY = boxY + padding + fm.getAscent();
        g2.drawString(mouseRaDecText, textX, textY);
    }
    
    /**
     * Format RA/Dec in sexagesimal format
     * RA: HH:MM:SS.S (hours)
     * Dec: ±DD:MM:SS (degrees)
     */
    private String formatRaDec(double raDeg, double decDeg) {
        // RA: Convert degrees to hours (0-360° -> 0-24h)
        double raHours = raDeg / 15.0;
        int raH = (int)raHours;
        double raMinutes = (raHours - raH) * 60.0;
        int raM = (int)raMinutes;
        double raS = (raMinutes - raM) * 60.0;
        
        // Dec: degrees, arcminutes, arcseconds
        boolean negative = decDeg < 0;
        double absDecDeg = Math.abs(decDeg);
        int decD = (int)absDecDeg;
        double decMinutes = (absDecDeg - decD) * 60.0;
        int decM = (int)decMinutes;
        double decS = (decMinutes - decM) * 60.0;
        
        String raStr = String.format("%02d:%02d:%04.1f", raH, raM, raS);
        String decStr = String.format("%s%02d:%02d:%02.0f", negative ? "-" : "+", decD, decM, decS);
        
        return "RA: " + raStr + "  Dec: " + decStr;
    }
}
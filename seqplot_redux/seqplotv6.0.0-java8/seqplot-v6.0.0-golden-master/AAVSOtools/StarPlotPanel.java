package AAVSOtools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;

/**
 * Custom star plot panel for integration with Seqplot application
 * Replaces the problematic JFreeChart bubble renderer
 */
public class StarPlotPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, Printable {
    
    private DataConnector dataConnector;
    private Seqplot parentSeqplot;
    
    // Plot dimensions and margins
    private int plotWidth, plotHeight;
    private int leftMargin = 80, rightMargin = 30, topMargin = 50, bottomMargin = 80;
    private DecimalFormat df = new DecimalFormat("0.0000");
    
    // Interaction variables
    private int hoveredRecord = -1;
    private Point mousePos = new Point();
    private Point lastPanPoint = null;
    private boolean isPanning = false;
    private boolean showGrid = true;
    private boolean showTooltip = true;
    
    // Colors
    private Color backgroundColor = Color.BLACK;
    private Color axisColor = Color.WHITE;
    private Color gridColor = new Color(80, 80, 80);
    private Color textColor = Color.WHITE;
    
    // Point size scaling factor (can be changed by menu)
    private double pointSizeScale = 0.75; // 25% smaller than original
    
    // Zoom and pan state
    private double zoomLevel = 1.0;
    private double panOffsetX = 0.0;
    private double panOffsetY = 0.0;
    private double maxZoomLevel = 10.0;
    private double minZoomLevel = 0.1;
    
    // Warm pastel color scheme for different series
    private Color[] seriesColors = {
        new Color(135, 170, 230),  // Series 0 - Warm pastel blue (periwinkle)
        new Color(144, 215, 144),  // Series 1 - Warm pastel green (sage green)
        new Color(255, 140, 140),  // Series 2 - Warm pastel red (coral pink)
        new Color(200, 140, 220),  // Series 3 - Warm pastel purple (lavender)
        new Color(250, 245, 235)   // Series 4 - Warm white (cream/ivory)
    };
    
    public StarPlotPanel(DataConnector dataConnector, Seqplot parentSeqplot) {
        this.dataConnector = dataConnector;
        this.parentSeqplot = parentSeqplot;
        
        setBackground(backgroundColor);
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        
        System.out.println("DEBUG: StarPlotPanel created");
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        plotWidth = getWidth() - leftMargin - rightMargin;
        plotHeight = getHeight() - topMargin - bottomMargin;
        
        if (plotWidth <= 0 || plotHeight <= 0) return;
        
        drawAxes(g2);
        if (showGrid) drawGrid(g2);
        drawStars(g2);
        drawTitle(g2);
        if (showTooltip && hoveredRecord >= 0) drawTooltip(g2);
        // Legend removed per user request
        // drawLegend(g2);
    }
    
    private void drawAxes(Graphics2D g2) {
        g2.setColor(axisColor);
        g2.setStroke(new BasicStroke(2));
        
        // Plot border
        Rectangle2D plotArea = new Rectangle2D.Double(leftMargin, topMargin, plotWidth, plotHeight);
        g2.draw(plotArea);
        
        // Axis labels
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        
        // X-axis label
        String xLabel = "X (degrees) - East";
        int xLabelWidth = fm.stringWidth(xLabel);
        g2.drawString(xLabel, leftMargin + (plotWidth - xLabelWidth) / 2, 
                     getHeight() - 20);
        
        // Y-axis label (rotated)
        String yLabel = "Y (degrees) - North";
        g2.rotate(-Math.PI / 2);
        g2.drawString(yLabel, -(topMargin + plotHeight / 2 + fm.stringWidth(yLabel) / 2), 20);
        g2.rotate(Math.PI / 2);
        
        // Tick marks and values
        drawTicks(g2);
    }
    
    private void drawTicks(Graphics2D g2) {
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
            String label = df.format(worldX);
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
            String label = df.format(worldY);
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
    
    private void drawStars(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1));
        
        int totalCount = dataConnector.getTotalCount();
        if (totalCount == 0) return;
        
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
                
                // Convert to screen coordinates using view bounds
                int screenX = leftMargin + (int)((worldX - viewMinX) / (viewMaxX - viewMinX) * plotWidth);
                int screenY = topMargin + (int)((viewMaxY - worldY) / (viewMaxY - viewMinY) * plotHeight); // Flip Y
                
                // Check if star is within plot bounds (with some margin for large stars)
                int maxStarSize = 30;
                if (screenX < leftMargin - maxStarSize || screenX > leftMargin + plotWidth + maxStarSize ||
                    screenY < topMargin - maxStarSize || screenY > topMargin + plotHeight + maxStarSize) {
                    continue; // Skip stars outside plot area
                }
                
                // Calculate star size based on V magnitude (brighter = larger)
                // Linear scale: brightest stars get max size, faintest get min size
                double magRange = maxVMag - minVMag;
                int baseSize;
                if (magRange > 0) {
                    // Invert magnitude (brighter stars have lower magnitude values)
                    double normalizedMag = (maxVMag - vMag) / magRange; // 0 = faintest, 1 = brightest
                    baseSize = (int)(4 + normalizedMag * 20); // Size range: 4-24 pixels
                } else {
                    baseSize = 12; // Default size if all stars have same magnitude
                }
                
                // Apply user point size scale and zoom level
                int size = Math.max(2, (int)(baseSize * pointSizeScale * Math.sqrt(zoomLevel)));
                
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
                
                drawnCount++;
                
                if (drawnCount <= 5) {
                    System.out.printf("DEBUG: Drew star %d: world(%.6f,%.6f) -> screen(%d,%d) vMag=%.2f size=%d series=%d\n",
                                     drawnCount, worldX, worldY, screenX, screenY, vMag, size, series);
                }
            }
        }
        
        // Restore original clipping
        g2.setClip(originalClip);
        
        System.out.printf("DEBUG: Total stars drawn: %d, Mag range: %.2f to %.2f, Zoom: %.2f\n", 
                         drawnCount, minVMag, maxVMag, zoomLevel);
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
        
        // Get star information
        double ra = dataConnector.getRa(hoveredRecord);
        double dec = dataConnector.getDec(hoveredRecord);
        double vmag = dataConnector.getVmag(hoveredRecord);
        double vmagError = dataConnector.getEv(hoveredRecord);
        double bMinusV = dataConnector.getBMinusV(hoveredRecord);
        double bmagError = dataConnector.getE_Bmag(hoveredRecord);
        
        // Calculate distance from center in arcseconds
        double centerRa = dataConnector.getCenterRa();
        double centerDec = dataConnector.getCenterDec();
        
        // Debug output
        if (hoveredRecord < 3) { // Only for first few stars to avoid spam
            System.out.printf("DEBUG: Star %d - RA=%.6f, Dec=%.6f, CenterRA=%.6f, CenterDec=%.6f\n",
                             hoveredRecord, ra, dec, centerRa, centerDec);
        }
        
        // Convert coordinate differences to arcseconds
        double deltaRa = (ra - centerRa) * 3600.0 * Math.cos(Math.toRadians(centerDec));
        double deltaDec = (dec - centerDec) * 3600.0;
        double distanceArcsec = Math.sqrt(deltaRa * deltaRa + deltaDec * deltaDec);
        double distanceArcmin = distanceArcsec / 60.0;
        
        // Calculate B-V error using quadrature: sqrt(e_B^2 + e_V^2)
        double bMinusVError = Math.sqrt(bmagError * bmagError + vmagError * vmagError);
        
        String[] lines = {
            String.format("%.3f (%.3f)", vmag, vmagError),
            String.format("%.3f (%.3f)", bMinusV, bMinusVError),
            String.format("%.1f\" (%.2f')", distanceArcsec, distanceArcmin)
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
    
    private int findStarAtPosition(int x, int y) {
        int totalCount = dataConnector.getTotalCount();
        if (totalCount == 0) return -1;
        
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
            
            int screenX = leftMargin + (int)((worldX - viewMinX) / (viewMaxX - viewMinX) * plotWidth);
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
    
    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos = e.getPoint();
        
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
        if (lastPanPoint != null) {
            isPanning = true;
            int deltaX = e.getX() - lastPanPoint.x;
            int deltaY = e.getY() - lastPanPoint.y;
            
            // Convert screen delta to world coordinates
            double minX = dataConnector.getMinX();
            double maxX = dataConnector.getMaxX();
            double minY = dataConnector.getMinY();
            double maxY = dataConnector.getMaxY();
            
            double rangeX = (maxX - minX) / zoomLevel;
            double rangeY = (maxY - minY) / zoomLevel;
            
            double worldDeltaX = -deltaX * rangeX / plotWidth;
            double worldDeltaY = deltaY * rangeY / plotHeight;
            
            pan(worldDeltaX, worldDeltaY);
            lastPanPoint = e.getPoint();
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int clickedRecord = findStarAtPosition(e.getX(), e.getY());
        if (clickedRecord >= 0) {
            // Update parent Seqplot with clicked star info
            parentSeqplot.setRecordNumber(clickedRecord);
            System.out.printf("DEBUG: Clicked on star record %d\n", clickedRecord);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        lastPanPoint = e.getPoint();
        isPanning = false;
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        lastPanPoint = null;
        isPanning = false;
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
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
        }
    }
    
    public void zoomIn() {
        if (zoomLevel < maxZoomLevel) {
            zoomLevel *= 1.25;
            repaint();
            System.out.printf("DEBUG: Zoomed in to level %.2f\n", zoomLevel);
        }
    }
    
    public void resetZoom() {
        zoomLevel = 1.0;
        panOffsetX = 0.0;
        panOffsetY = 0.0;
        repaint();
        System.out.println("DEBUG: Reset zoom to default");
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
                System.out.println("DEBUG: Print job sent successfully");
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
        System.out.println("DEBUG: StarPlotPanel.refreshPlotData() called");
        
        // Reset zoom and pan to show all new data
        zoomLevel = 1.0;
        panOffsetX = 0.0;
        panOffsetY = 0.0;
        
        // Force a complete repaint with new data
        repaint();
        revalidate();
        
        System.out.println("DEBUG: StarPlotPanel refreshed with new data");
    }
}
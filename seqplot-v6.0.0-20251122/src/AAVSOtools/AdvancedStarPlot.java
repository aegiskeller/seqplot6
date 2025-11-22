package AAVSOtools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

/**
 * Advanced star plot with axes, grid, hover tooltips, and zoom capabilities
 */
public class AdvancedStarPlot extends JPanel implements MouseMotionListener {
    
    public static class Star {
        public double ra, dec, vmag;
        public int series;
        public double x, y; // Converted coordinates
        public Color color;
        public int size;
        public String name;
        public String info; // Additional info for tooltip
        
        public Star(double ra, double dec, double vmag, int series, String name) {
            this.ra = ra;
            this.dec = dec;
            this.vmag = vmag;
            this.series = series;
            this.name = name;
            this.info = String.format(java.util.Locale.US, "RA: %.6f°, Dec: %.6f°, Mag: %.2f", ra, dec, vmag);
            
            // Set warm pastel color based on series
            switch(series) {
                case 0: color = new Color(135, 170, 230); break;   // Warm pastel blue (periwinkle)
                case 1: color = new Color(144, 215, 144); break;   // Warm pastel green (sage green)
                case 2: color = new Color(255, 140, 140); break;   // Warm pastel red (coral pink)
                case 3: color = new Color(200, 140, 220); break;   // Warm pastel purple (lavender)
                case 4: color = new Color(250, 245, 235); break;   // Warm white (cream/ivory)
                default: color = new Color(210, 180, 210); break;  // Warm light lavender
            }
            
            // Size based on magnitude (brighter = larger)
            size = (int)(25 - vmag * 1.2); // mag 8 = size 15, mag 14 = size 8
            if (size < 3) size = 3;
            if (size > 25) size = 25;
        }
    }
    
    private List<Star> stars;
    private double centralRA, centralDec;
    private double minX, maxX, minY, maxY;
    private int plotWidth, plotHeight;
    private int leftMargin = 80, rightMargin = 30, topMargin = 50, bottomMargin = 80;
    private DecimalFormat df = new DecimalFormat("0.0000", new java.text.DecimalFormatSymbols(java.util.Locale.US));
    private DecimalFormat magFormat = new DecimalFormat("0.00", new java.text.DecimalFormatSymbols(java.util.Locale.US));
    
    // Interaction variables
    private Star hoveredStar = null;
    private Point mousePos = new Point();
    private boolean showGrid = true;
    private boolean showTooltip = true;
    
    // Colors
    private Color backgroundColor = Color.BLACK;
    private Color axisColor = Color.WHITE;
    private Color gridColor = new Color(80, 80, 80);
    private Color textColor = Color.WHITE;
    
    public AdvancedStarPlot() {
        setPreferredSize(new Dimension(900, 700));
        setBackground(backgroundColor);
        stars = new ArrayList<>();
        addMouseMotionListener(this);
        
        // EE Eri coordinates
        centralRA = 47.575380;
        centralDec = -1.694720;
        
        addTestStars();
        convertCoordinates();
    }
    
    private void addTestStars() {
        System.out.println("Adding test stars...");
        
        // EE Eri itself
        stars.add(new Star(47.575380, -1.694720, 8.0, 3, "EE Eri"));
        
        // Add some named stars
        stars.add(new Star(47.6, -1.7, 9.5, 0, "Star A"));
        stars.add(new Star(47.55, -1.68, 10.2, 1, "Star B"));
        stars.add(new Star(47.58, -1.71, 11.8, 2, "Star C"));
        stars.add(new Star(47.52, -1.72, 12.5, 4, "Star D"));
        
        // Pattern of random stars
        for (int i = 0; i < 30; i++) {
            double offsetRA = (Math.random() - 0.5) * 0.15; // ±0.075 degrees
            double offsetDec = (Math.random() - 0.5) * 0.12; // ±0.06 degrees
            double mag = 8.0 + Math.random() * 6.0; // mag 8-14
            int series = i % 5;
            
            stars.add(new Star(centralRA + offsetRA, centralDec + offsetDec, 
                              mag, series, String.format(java.util.Locale.US, "Star_%d", i + 6)));
        }
        
        System.out.printf(java.util.Locale.US, "Added %d stars\n", stars.size());
    }
    
    private void convertCoordinates() {
        System.out.println("Converting coordinates...");
        
        minX = Double.MAX_VALUE;
        maxX = Double.MIN_VALUE;
        minY = Double.MAX_VALUE;
        maxY = Double.MIN_VALUE;
        
        for (Star star : stars) {
            double[] xy = convertToTangentPlane(star.ra, star.dec);
            star.x = xy[0];
            star.y = xy[1];
            
            minX = Math.min(minX, star.x);
            maxX = Math.max(maxX, star.x);
            minY = Math.min(minY, star.y);
            maxY = Math.max(maxY, star.y);
        }
        
        // Add 10% padding
        double xPad = (maxX - minX) * 0.1;
        double yPad = (maxY - minY) * 0.1;
        minX -= xPad;
        maxX += xPad;
        minY -= yPad;
        maxY += yPad;
        
        System.out.printf(java.util.Locale.US, "Coordinate ranges: X=[%.6f, %.6f], Y=[%.6f, %.6f]\n", 
                         minX, maxX, minY, maxY);
    }
    
    private double[] convertToTangentPlane(double ra, double dec) {
        double picon = Math.PI / 180.0;
        double racent = picon * centralRA;
        double deccent = picon * centralDec;
        double raValue = ra * picon;
        double decValue = dec * picon;
        
        double raOffset = raValue - racent;
        
        double cosC = Math.sin(deccent) * Math.sin(decValue) + 
                     Math.cos(deccent) * Math.cos(decValue) * Math.cos(raOffset);
        double xi = Math.cos(decValue) * Math.sin(raOffset) / cosC;
        double eta = (Math.cos(deccent) * Math.sin(decValue) - 
                     Math.sin(deccent) * Math.cos(decValue) * Math.cos(raOffset)) / cosC;
        
        return new double[]{xi / picon, eta / picon}; // Return in degrees
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
        
        drawAxes(g2);
        if (showGrid) drawGrid(g2);
        drawStars(g2);
        drawTitle(g2);
        if (showTooltip && hoveredStar != null) drawTooltip(g2);
        drawLegend(g2);
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
        
        // X-axis ticks
        int numXTicks = 6;
        for (int i = 0; i <= numXTicks; i++) {
            double worldX = minX + (maxX - minX) * i / numXTicks;
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
            double worldY = minY + (maxY - minY) * i / numYTicks;
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
        
        for (Star star : stars) {
            // Convert world coordinates to screen coordinates
            int screenX = leftMargin + (int)((star.x - minX) / (maxX - minX) * plotWidth);
            int screenY = topMargin + (int)((maxY - star.y) / (maxY - minY) * plotHeight); // Flip Y
            
            // Highlight hovered star using the same color as the star body
            if (star == hoveredStar) {
                g2.setColor(star.color);
                g2.fillOval(screenX - star.size/2 - 3, screenY - star.size/2 - 3, 
                           star.size + 6, star.size + 6);
            }
            
            // Draw the star
            g2.setColor(star.color);
            Ellipse2D.Double circle = new Ellipse2D.Double(
                screenX - star.size/2, screenY - star.size/2, 
                star.size, star.size
            );
            g2.fill(circle);
            
            // Draw outline using same color as body
            g2.setColor(star.color);
            g2.draw(circle);
        }
    }
    
    private void drawTitle(Graphics2D g2) {
        g2.setColor(textColor);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        
        String title = String.format(java.util.Locale.US, "Star Field around EE Eri (RA: %.6f°, Dec: %.6f°)", 
                                    centralRA, centralDec);
        int titleWidth = fm.stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, 25);
        
        // Subtitle with star count
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        fm = g2.getFontMetrics();
        String subtitle = String.format(java.util.Locale.US, "%d stars displayed", stars.size());
        int subtitleWidth = fm.stringWidth(subtitle);
        g2.drawString(subtitle, (getWidth() - subtitleWidth) / 2, 42);
    }
    
    private void drawTooltip(Graphics2D g2) {
        if (hoveredStar == null) return;
        
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        
        String[] lines = {
            hoveredStar.name,
            hoveredStar.info,
            String.format(java.util.Locale.US, "Series: %d", hoveredStar.series)
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
        FontMetrics fm = g2.getFontMetrics();
        
        int legendX = getWidth() - 150;
        int legendY = topMargin + 20;
        
        g2.setColor(new Color(30, 30, 30, 200));
        g2.fillRoundRect(legendX - 5, legendY - 15, 140, 120, 5, 5);
        
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(legendX - 5, legendY - 15, 140, 120, 5, 5);
        g2.drawString("Series Colors:", legendX, legendY);
        
        String[] seriesNames = {"Blue", "Green", "Red", "Purple", "White"};
        Color[] seriesColors = {
            new Color(135, 170, 230),  // Warm pastel blue (periwinkle)
            new Color(144, 215, 144),  // Warm pastel green (sage green)
            new Color(255, 140, 140),  // Warm pastel red (coral pink)
            new Color(200, 140, 220),  // Warm pastel purple (lavender)
            new Color(250, 245, 235)   // Warm white (cream/ivory)
        };
        
        for (int i = 0; i < 5; i++) {
            int y = legendY + 15 + i * 15;
            
            // Color circle with unified colors
            g2.setColor(seriesColors[i]);
            g2.fillOval(legendX, y - 5, 10, 10);
            g2.setColor(seriesColors[i]);
            g2.drawOval(legendX, y - 5, 10, 10);
            
            // Label
            g2.drawString(seriesNames[i], legendX + 15, y + 3);
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos = e.getPoint();
        
        // Find hovered star
        Star oldHovered = hoveredStar;
        hoveredStar = null;
        
        for (Star star : stars) {
            int screenX = leftMargin + (int)((star.x - minX) / (maxX - minX) * plotWidth);
            int screenY = topMargin + (int)((maxY - star.y) / (maxY - minY) * plotHeight);
            
            double distance = Math.sqrt(Math.pow(mousePos.x - screenX, 2) + 
                                      Math.pow(mousePos.y - screenY, 2));
            
            if (distance <= star.size / 2 + 3) {
                hoveredStar = star;
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                break;
            }
        }
        
        if (hoveredStar == null) {
            setCursor(Cursor.getDefaultCursor());
        }
        
        if (oldHovered != hoveredStar) {
            repaint();
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        // Could implement pan/zoom here
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Advanced Star Plot");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            AdvancedStarPlot plot = new AdvancedStarPlot();
            frame.add(plot);
            
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            System.out.println("Advanced star plot window created");
        });
    }
}
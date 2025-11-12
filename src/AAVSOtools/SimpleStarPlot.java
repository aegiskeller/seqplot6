package AAVSOtools;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple star plot using Java2D graphics instead of JFreeChart
 */
public class SimpleStarPlot extends JPanel {
    
    public static class Star {
        public double ra, dec, vmag;
        public int series;
        public double x, y; // Converted coordinates
        public Color color;
        public int size;
        
        public Star(double ra, double dec, double vmag, int series) {
            this.ra = ra;
            this.dec = dec;
            this.vmag = vmag;
            this.series = series;
            
            // Set color based on series - warm pastel palette
            switch(series) {
                case 0: color = new Color(135, 170, 230); break;   // Warm pastel blue
                case 1: color = new Color(144, 215, 144); break;   // Warm pastel green
                case 2: color = new Color(255, 140, 140); break;   // Warm pastel red
                case 3: color = new Color(200, 140, 220); break;   // Warm pastel purple
                case 4: color = new Color(250, 245, 235); break;   // Warm white (cream)
                default: color = new Color(210, 180, 210); break;  // Warm light lavender
            }
            
            // Size based on magnitude (brighter = larger)
            size = (int)(20 - vmag); // mag 8 = size 12, mag 14 = size 6
            if (size < 2) size = 2;
            if (size > 20) size = 20;
        }
    }
    
    private List<Star> stars;
    private double centralRA, centralDec;
    private double minX, maxX, minY, maxY;
    private int plotWidth, plotHeight;
    private int margin = 50;
    
    public SimpleStarPlot() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        stars = new ArrayList<>();
        
        // EE Eri coordinates
        centralRA = 47.575380;
        centralDec = -1.694720;
        
        addTestStars();
        convertCoordinates();
    }
    
    private void addTestStars() {
        System.out.println("Adding test stars...");
        
        // EE Eri itself
        stars.add(new Star(47.575380, -1.694720, 8.0, 3)); // Yellow
        
        // Pattern of stars around EE Eri
        for (int i = 0; i < 20; i++) {
            double offsetRA = (Math.random() - 0.5) * 0.1; // ±0.05 degrees
            double offsetDec = (Math.random() - 0.5) * 0.08; // ±0.04 degrees
            double mag = 8.0 + Math.random() * 6.0; // mag 8-14
            int series = i % 5;
            
            stars.add(new Star(centralRA + offsetRA, centralDec + offsetDec, mag, series));
        }
        
        System.out.printf("Added %d stars\n", stars.size());
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
        
        System.out.printf("Coordinate ranges: X=[%.6f, %.6f], Y=[%.6f, %.6f]\n", 
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
        
        plotWidth = getWidth() - 2 * margin;
        plotHeight = getHeight() - 2 * margin;
        
        // Draw coordinate axes
        g2.setColor(Color.GRAY);
        g2.drawRect(margin, margin, plotWidth, plotHeight);
        
        // Draw title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Simple Star Plot Test", margin, margin - 10);
        
        // Draw axis labels
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString(String.format("X: %.4f to %.4f degrees", minX, maxX), margin, getHeight() - 10);
        g2.drawString(String.format("Y: %.4f to %.4f degrees", minY, maxY), margin + 250, getHeight() - 10);
        
        System.out.printf("Drawing %d stars in plot area %dx%d\n", stars.size(), plotWidth, plotHeight);
        
        // Draw stars
        int drawnCount = 0;
        for (Star star : stars) {
            // Convert world coordinates to screen coordinates
            int screenX = margin + (int)((star.x - minX) / (maxX - minX) * plotWidth);
            int screenY = margin + (int)((maxY - star.y) / (maxY - minY) * plotHeight); // Flip Y
            
            // Draw the star
            g2.setColor(star.color);
            Ellipse2D.Double circle = new Ellipse2D.Double(
                screenX - star.size/2, screenY - star.size/2, 
                star.size, star.size
            );
            g2.fill(circle);
            
            // Draw outline using same color as the star body
            g2.setColor(star.color);
            g2.setStroke(new BasicStroke(1));
            g2.draw(circle);
            
            if (drawnCount < 5) {
                System.out.printf("Drew star %d: world(%.6f,%.6f) -> screen(%d,%d) size=%d color=%s\n",
                                 drawnCount, star.x, star.y, screenX, screenY, star.size, star.color);
            }
            drawnCount++;
        }
        
        System.out.printf("Total stars drawn: %d\n", drawnCount);
        
        // Draw star count
        g2.setColor(Color.WHITE);
        g2.drawString(String.format("Stars: %d", stars.size()), getWidth() - 100, margin + 20);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simple Star Plot Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            SimpleStarPlot plot = new SimpleStarPlot();
            frame.add(plot);
            
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            System.out.println("Simple star plot window created");
        });
    }
}
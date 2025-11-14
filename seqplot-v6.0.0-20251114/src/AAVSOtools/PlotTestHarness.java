package AAVSOtools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.chart.axis.NumberAxis;

/**
 * Test harness for plotting RA/Dec points with tangent plane projection
 */
public class PlotTestHarness extends JFrame {
    
    // Test data structure for a star
    public static class Star {
        public double ra;
        public double dec; 
        public double vmag;
        public int series; // color series (0=blue, 1=green, 2=red, 3=yellow, 4=white)
        
        public Star(double ra, double dec, double vmag, int series) {
            this.ra = ra;
            this.dec = dec;
            this.vmag = vmag;
            this.series = series;
        }
    }
    
    private JFreeChart chart;
    private XYPlot plot;
    private DefaultXYZDataset dataset;
    private List<Star> stars;
    
    // Central coordinates for tangent plane projection
    private double centralRA;
    private double centralDec;
    
    public PlotTestHarness() {
        super("Plot Test Harness");
        this.stars = new ArrayList<>();
        this.dataset = new DefaultXYZDataset();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        createChart();
        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
        
        // Add test data for EE Eri region
        addTestData();
        updatePlot();
    }
    
    private void createChart() {
        chart = ChartFactory.createBubbleChart(
            "Star Field Test", 
            "X (degrees)", 
            "Y (degrees)", 
            dataset, 
            PlotOrientation.VERTICAL, 
            false, // no legend
            false, // no tooltips
            false  // no URLs
        );
        
        chart.setBorderVisible(false);
        plot = (XYPlot) chart.getPlot();
        
        // Set up the bubble renderer
        XYBubbleRenderer renderer = new XYBubbleRenderer(XYBubbleRenderer.SCALE_ON_BOTH_AXES);
        plot.setRenderer(renderer);
        
        // Set colors for different series
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesPaint(2, Color.RED);
        renderer.setSeriesPaint(3, Color.YELLOW);
        renderer.setSeriesPaint(4, Color.WHITE);
        
        // Set background to black like the main app
        plot.setBackgroundPaint(Color.BLACK);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        
        // System.out.println("DEBUG: Test harness chart created");
    }
    
    private void addTestData() {
        // EE Eri coordinates: RA=47.575380, Dec=-1.694720
        centralRA = 47.575380;
        centralDec = -1.694720;
        
        System.out.printf("DEBUG: Test harness using central coordinates: RA=%.6f, Dec=%.6f\n", 
                         centralRA, centralDec);
        
        // Add some test stars around EE Eri
        stars.add(new Star(47.575380, -1.694720, 8.0, 3)); // EE Eri itself (yellow)
        stars.add(new Star(47.6, -1.7, 10.5, 0)); // Blue star
        stars.add(new Star(47.55, -1.68, 9.2, 1)); // Green star  
        stars.add(new Star(47.58, -1.71, 11.8, 2)); // Red star
        stars.add(new Star(47.52, -1.72, 12.5, 4)); // White star
        
        // Add more stars in a pattern to make it visible
        for (int i = 0; i < 10; i++) {
            double offsetRA = (i - 5) * 0.01; // ±0.05 degrees
            double offsetDec = (i - 5) * 0.008; // ±0.04 degrees
            stars.add(new Star(centralRA + offsetRA, centralDec + offsetDec, 
                              10.0 + i * 0.3, i % 5));
        }
        
        System.out.printf("DEBUG: Added %d test stars\n", stars.size());
    }
    
    private void updatePlot() {
        // Convert stars to tangent plane coordinates
        double[][] seriesData = new double[5][3]; // 5 series, each with X,Y,Z arrays
        int[] seriesCounts = new int[5];
        
        // First pass: count stars per series
        for (Star star : stars) {
            if (star.series >= 0 && star.series < 5) {
                seriesCounts[star.series]++;
            }
        }
        
        // Initialize arrays for each series
        double[][][] allSeriesData = new double[5][][];
        int[] currentIndex = new int[5];
        
        for (int i = 0; i < 5; i++) {
            if (seriesCounts[i] > 0) {
                allSeriesData[i] = new double[3][seriesCounts[i]]; // X, Y, Z arrays
            }
        }
        
        // Convert coordinates and populate series data
        for (Star star : stars) {
            if (star.series >= 0 && star.series < 5 && seriesCounts[star.series] > 0) {
                double[] xy = convertToTangentPlane(star.ra, star.dec);
                double z = magnitudeToSize(star.vmag);
                
                int idx = currentIndex[star.series];
                allSeriesData[star.series][0][idx] = xy[0]; // X
                allSeriesData[star.series][1][idx] = xy[1]; // Y
                allSeriesData[star.series][2][idx] = z;     // Z (size)
                
                currentIndex[star.series]++;
                
                if (idx < 3) { // Debug first few stars
                    System.out.printf("DEBUG: Star %d: RA=%.6f, Dec=%.6f -> X=%.6f, Y=%.6f, Z=%.6f, Series=%d\n",
                                     idx, star.ra, star.dec, xy[0], xy[1], z, star.series);
                }
            }
        }
        
        // Add series to dataset
        for (int i = 0; i < 5; i++) {
            if (seriesCounts[i] > 0) {
                dataset.addSeries("Series " + i, allSeriesData[i]);
                System.out.printf("DEBUG: Added series %d with %d stars\n", i, seriesCounts[i]);
            }
        }
        
        // Set axis ranges
        setAxisRanges();
    }
    
    private double[] convertToTangentPlane(double ra, double dec) {
        double picon = Math.PI / 180.0;
        double racent = picon * centralRA;
        double deccent = picon * centralDec;
        double raValue = ra * picon;
        double decValue = dec * picon;
        
        double raOffset = raValue - racent;
        if (raOffset >= 360.0 * picon) {
            raOffset -= 360.0 * picon;
        }
        
        double cosC = Math.sin(deccent) * Math.sin(decValue) + 
                     Math.cos(deccent) * Math.cos(decValue) * Math.cos(raOffset);
        double xi = Math.cos(decValue) * Math.sin(raOffset) / cosC;
        double eta = (Math.cos(deccent) * Math.sin(decValue) - 
                     Math.sin(deccent) * Math.cos(decValue) * Math.cos(raOffset)) / cosC;
        
        // Return in degrees
        return new double[]{xi / picon, eta / picon};
    }
    
    private double magnitudeToSize(double vmag) {
        // Convert magnitude to bubble size (brighter stars = larger bubbles)
        // Magnitude 8 = size 50, magnitude 14 = size 5
        double size = 55.0 - vmag * 5.0;
        return Math.max(size, 1.0); // Minimum size of 1
    }
    
    private void setAxisRanges() {
        // Calculate coordinate ranges
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        
        for (Star star : stars) {
            double[] xy = convertToTangentPlane(star.ra, star.dec);
            minX = Math.min(minX, xy[0]);
            maxX = Math.max(maxX, xy[0]);
            minY = Math.min(minY, xy[1]);
            maxY = Math.max(maxY, xy[1]);
        }
        
        System.out.printf("DEBUG: Coordinate ranges - X: %.6f to %.6f, Y: %.6f to %.6f\n",
                         minX, maxX, minY, maxY);
        
        // Set axis ranges with 5% padding
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        
        domainAxis.setRange(1.05 * minX, 1.05 * maxX);
        domainAxis.setInverted(true); // Match astronomical convention
        domainAxis.setAutoRangeIncludesZero(false);
        
        rangeAxis.setRange(1.05 * minY, 1.05 * maxY);
        rangeAxis.setAutoRangeIncludesZero(false);
        
        System.out.printf("DEBUG: Set axis ranges - X: %.6f to %.6f, Y: %.6f to %.6f\n",
                         1.05 * minX, 1.05 * maxX, 1.05 * minY, 1.05 * maxY);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // System.out.println("DEBUG: Starting Plot Test Harness");
            PlotTestHarness harness = new PlotTestHarness();
            harness.setVisible(true);
        });
    }
}
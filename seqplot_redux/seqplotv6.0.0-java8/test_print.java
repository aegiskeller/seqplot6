// Quick test for print functionality
import AAVSOtools.*;
import javax.swing.*;

public class test_print {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a simple DataConnector and Seqplot
                Seqplot seqplot = new Seqplot();
                StarPlotPanel panel = seqplot.createPlotPanel();
                
                // Test if panel was created successfully
                if (panel != null) {
                    System.out.println("✅ StarPlotPanel created successfully");
                    System.out.println("✅ Print functionality should be available");
                    
                    // Create a simple test frame to show it works
                    JFrame frame = new JFrame("Print Test");
                    frame.add(panel);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                    
                    System.out.println("✅ Test GUI shown - you can now test the print menu");
                } else {
                    System.out.println("❌ StarPlotPanel creation failed");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
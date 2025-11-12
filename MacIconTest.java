import javax.swing.*;
import java.awt.*;

public class MacIconTest {
    public static void main(String[] args) {
        System.out.println("=== macOS Icon Test ===");
        
        // Set system property for macOS app name
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Seqplot");
        
        try {
            // Load the icon from file system
            ImageIcon icon = new ImageIcon("src/AAVSOtools/seqplot_icon.png");
            System.out.println("Icon loaded: " + icon.getIconWidth() + "x" + icon.getIconHeight());
            
            if (icon.getIconWidth() > 0) {
                // Set dock icon using Taskbar API (Java 9+)
                try {
                    if (Taskbar.isTaskbarSupported()) {
                        Taskbar taskbar = Taskbar.getTaskbar();
                        if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                            taskbar.setIconImage(icon.getImage());
                            System.out.println("✅ Dock icon set using Taskbar API");
                        } else {
                            System.out.println("❌ ICON_IMAGE feature not supported");
                        }
                    } else {
                        System.out.println("❌ Taskbar not supported");
                    }
                } catch (Exception e) {
                    System.out.println("Taskbar failed: " + e.getMessage());
                }
                
                // Create a simple window 
                JFrame frame = new JFrame("Seqplot Icon Test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setIconImage(icon.getImage()); // This may not show in title bar on macOS, but it's good practice
                frame.setSize(400, 200);
                frame.setLocationRelativeTo(null);
                
                JLabel label = new JLabel("<html><center>Check the dock for the Seqplot icon!<br><br>On macOS, window title bar icons usually don't appear<br>but dock icons should work.</center></html>", JLabel.CENTER);
                frame.add(label);
                frame.setVisible(true);
                
                System.out.println("✅ Window created - check the dock!");
                System.out.println("Note: On macOS, title bar icons typically don't appear for Java apps");
                System.out.println("But the dock icon should be visible!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
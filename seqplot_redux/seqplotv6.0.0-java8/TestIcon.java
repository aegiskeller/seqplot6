import javax.swing.*;
import java.awt.*;

public class TestIcon {
    public static void main(String[] args) {
        System.out.println("=== Testing Icon Loading ===");
        
        try {
            // Test resource loading
            java.net.URL iconURL = TestIcon.class.getResource("/AAVSOtools/seqplot_icon.png");
            System.out.println("Icon URL: " + iconURL);
            
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                System.out.println("Icon loaded successfully!");
                System.out.println("Width: " + icon.getIconWidth());
                System.out.println("Height: " + icon.getIconHeight());
                
                // Create a simple window to test
                JFrame frame = new JFrame("Icon Test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setIconImage(icon.getImage());
                frame.setSize(300, 200);
                frame.setLocationRelativeTo(null);
                
                JLabel label = new JLabel("Icon should appear in title bar", JLabel.CENTER);
                frame.add(label);
                
                frame.setVisible(true);
                
                // Test macOS dock icon
                String osName = System.getProperty("os.name").toLowerCase();
                System.out.println("OS: " + osName);
                if (osName.contains("mac")) {
                    try {
                        Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
                        if (taskbarClass != null) {
                            Object taskbar = taskbarClass.getMethod("getTaskbar").invoke(null);
                            taskbarClass.getMethod("setIconImage", Image.class).invoke(taskbar, icon.getImage());
                            System.out.println("Dock icon set using Taskbar API");
                        }
                    } catch (Exception e) {
                        System.out.println("Taskbar API failed: " + e.getMessage());
                        try {
                            Class<?> appClass = Class.forName("com.apple.eawt.Application");
                            Object app = appClass.getMethod("getApplication").invoke(null);
                            appClass.getMethod("setDockIconImage", Image.class).invoke(app, icon.getImage());
                            System.out.println("Dock icon set using Application API");
                        } catch (Exception e2) {
                            System.out.println("Application API also failed: " + e2.getMessage());
                        }
                    }
                }
                
            } else {
                System.err.println("ERROR: Icon not found!");
                // List what's actually in the resources
                try {
                    java.net.URL resourceURL = TestIcon.class.getResource("/AAVSOtools/");
                    System.out.println("AAVSOtools resource directory: " + resourceURL);
                } catch (Exception e) {
                    System.out.println("Cannot find AAVSOtools directory: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
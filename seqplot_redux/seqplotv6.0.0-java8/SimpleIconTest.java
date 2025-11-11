import javax.swing.*;
import java.awt.*;

public class SimpleIconTest {
    public static void main(String[] args) {
        System.out.println("=== Simple Icon Test ===");
        
        try {
            // Test 1: Try loading from file system directly
            System.out.println("Test 1: Loading from file system...");
            ImageIcon fileIcon = new ImageIcon("src/AAVSOtools/seqplot_icon.png");
            System.out.println("File icon width: " + fileIcon.getIconWidth());
            System.out.println("File icon height: " + fileIcon.getIconHeight());
            System.out.println("File icon load status: " + fileIcon.getImageLoadStatus());
            
            if (fileIcon.getIconWidth() > 0) {
                // Create a window with the file-loaded icon
                JFrame frame1 = new JFrame("Test 1: File System Icon");
                frame1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame1.setIconImage(fileIcon.getImage());
                frame1.setSize(200, 100);
                frame1.setLocation(100, 100);
                frame1.add(new JLabel("File System Icon", JLabel.CENTER));
                frame1.setVisible(true);
                System.out.println("✅ File system icon window created");
            }
            
            // Test 2: Try loading from classpath
            System.out.println("\nTest 2: Loading from classpath...");
            java.net.URL iconURL = SimpleIconTest.class.getResource("/AAVSOtools/seqplot_icon.png");
            System.out.println("Classpath URL: " + iconURL);
            
            if (iconURL != null) {
                ImageIcon cpIcon = new ImageIcon(iconURL);
                System.out.println("Classpath icon width: " + cpIcon.getIconWidth());
                System.out.println("Classpath icon height: " + cpIcon.getIconHeight());
                
                if (cpIcon.getIconWidth() > 0) {
                    JFrame frame2 = new JFrame("Test 2: Classpath Icon");
                    frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame2.setIconImage(cpIcon.getImage());
                    frame2.setSize(200, 100);
                    frame2.setLocation(320, 100);
                    frame2.add(new JLabel("Classpath Icon", JLabel.CENTER));
                    frame2.setVisible(true);
                    System.out.println("✅ Classpath icon window created");
                }
            } else {
                System.out.println("❌ Classpath URL is null");
            }
            
            // Test 3: Create a smaller version
            System.out.println("\nTest 3: Creating smaller version...");
            if (fileIcon.getIconWidth() > 0) {
                Image scaledImage = fileIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                ImageIcon smallIcon = new ImageIcon(scaledImage);
                
                JFrame frame3 = new JFrame("Test 3: Small Icon (64x64)");
                frame3.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame3.setIconImage(smallIcon.getImage());
                frame3.setSize(200, 100);
                frame3.setLocation(540, 100);
                frame3.add(new JLabel("Small Icon", JLabel.CENTER));
                frame3.setVisible(true);
                System.out.println("✅ Small icon window created");
            }
            
            // Test 4: Try macOS dock icon
            System.out.println("\nTest 4: macOS dock icon...");
            String osName = System.getProperty("os.name").toLowerCase();
            System.out.println("OS: " + osName);
            if (osName.contains("mac") && fileIcon.getIconWidth() > 0) {
                try {
                    Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
                    if (Taskbar.isTaskbarSupported()) {
                        Taskbar taskbar = Taskbar.getTaskbar();
                        if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                            taskbar.setIconImage(fileIcon.getImage());
                            System.out.println("✅ Dock icon set successfully");
                        } else {
                            System.out.println("❌ Taskbar icon feature not supported");
                        }
                    } else {
                        System.out.println("❌ Taskbar not supported");
                    }
                } catch (Exception e) {
                    System.out.println("❌ Dock icon failed: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("\n=== Test Complete ===");
        System.out.println("Check the title bars and dock for icons!");
    }
}
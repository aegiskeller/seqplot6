package AAVSOtools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Test blank image detection
 */
public class TestBlankDetection {
    public static void main(String[] args) {
        System.out.println("=== Testing Blank Image Detection ===\n");
        
        // Test the actual downloaded images
        String[] testFiles = {
            "SDSS_actual_coord.jpg",  // Should be blank (white)
            "SDSS_M13_test.jpg",       // Should NOT be blank (M13 cluster)
            "DSS2_jpg_test.jpg"        // Should NOT be blank (field with stars)
        };
        
        for (String filename : testFiles) {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("⚠ File not found: " + filename);
                continue;
            }
            
            try {
                BufferedImage image = ImageIO.read(file);
                boolean isBlank = checkIfBlank(image);
                
                System.out.printf(java.util.Locale.US, "%-30s : %s\n", filename, 
                    isBlank ? "BLANK (no coverage)" : "Has content");
                
            } catch (Exception e) {
                System.out.printf(java.util.Locale.US, "%-30s : ERROR - %s\n", filename, e.getMessage());
            }
        }
        
        System.out.println("\nTest complete!");
    }
    
    private static boolean checkIfBlank(BufferedImage image) {
        if (image == null) return true;
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Sample a grid of pixels across the image
        int sampleCount = 0;
        int minBrightness = 255;
        int maxBrightness = 0;
        long totalBrightness = 0;
        
        // Sample every 50th pixel for efficiency
        for (int y = 10; y < height - 10; y += 50) {
            for (int x = 10; x < width - 10; x += 50) {
                int rgb = image.getRGB(x, y);
                
                // Calculate brightness (average of RGB channels)
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int brightness = (r + g + b) / 3;
                
                minBrightness = Math.min(minBrightness, brightness);
                maxBrightness = Math.max(maxBrightness, brightness);
                totalBrightness += brightness;
                sampleCount++;
            }
        }
        
        if (sampleCount == 0) return true;
        
        int avgBrightness = (int)(totalBrightness / sampleCount);
        int brightnessRange = maxBrightness - minBrightness;
        
        System.out.printf(java.util.Locale.US, "  Details: range=%d, avg=%d, min=%d, max=%d\n", 
                         brightnessRange, avgBrightness, minBrightness, maxBrightness);
        
        // An astronomical image should have significant brightness variation
        // Blank/white images have very low variation (< 10) and high average brightness (> 200)
        // Or very low brightness (< 10) if all black
        return (brightnessRange < 10 && avgBrightness > 200) || 
               (brightnessRange < 10 && avgBrightness < 10);
    }
}

package AAVSOtools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Test program to download SDSS image and verify it's not blank
 */
public class TestSDSSDownload {
    
    private static final String HIPS_BASE_URL = "https://alasky.cds.unistra.fr/hips-image-services/hips2fits";
    
    public static void main(String[] args) {
        try {
            System.out.println("=== SDSS Image Download Test ===");
            System.out.println("Target: RA=04:00:00 (60.0°), Dec=-10:00:00 (-10.0°)");
            System.out.println("FOV: 15 arcmin (0.25°)");
            System.out.println();
            
            // Convert RA/Dec to decimal degrees
            double ra = 60.0;  // 04:00:00 = 4 hours * 15 deg/hour
            double dec = -10.0;
            double fovDegrees = 15.0 / 60.0;  // 15 arcmin = 0.25 degrees
            int imageSize = 512;
            
            // Test with different surveys
            String[] surveys = {
                "CDS/P/DSS2/color",
                "CDS/P/SDSS9/color-alt"
            };
            
            for (String survey : surveys) {
                System.out.println("Testing survey: " + survey);
                System.out.println("----------------------------------------");
                
                try {
                    BufferedImage image = downloadImage(survey, ra, dec, fovDegrees, imageSize);
                    
                    if (image == null) {
                        System.err.println("ERROR: Returned image is NULL");
                    } else {
                        System.out.println("SUCCESS: Image downloaded");
                        System.out.printf(java.util.Locale.US, "  Size: %d x %d pixels\n", image.getWidth(), image.getHeight());
                        
                        // Check if image is blank (all pixels same color)
                        boolean isBlank = checkIfBlank(image);
                        System.out.println("  Blank: " + isBlank);
                        
                        if (!isBlank) {
                            // Get pixel statistics
                            int[] stats = getPixelStats(image);
                            System.out.printf(java.util.Locale.US, "  Pixel range: min=%d, max=%d, avg=%d\n", 
                                stats[0], stats[1], stats[2]);
                        }
                        
                        // Save to file for inspection
                        String filename = survey.replace("/", "_") + "_test.png";
                        File outputFile = new File(filename);
                        ImageIO.write(image, "png", outputFile);
                        System.out.println("  Saved to: " + filename);
                    }
                    
                } catch (Exception e) {
                    System.err.println("ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
                
                System.out.println();
            }
            
            System.out.println("Test complete!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Download image directly from CDS HiPS2FITS service
     */
    private static BufferedImage downloadImage(String survey, double ra, double dec, double fov, int size) throws Exception {
        // Build URL for FITS download
        String urlStr = String.format(java.util.Locale.US,
            "%s?hips=%s&ra=%.6f&dec=%.6f&fov=%.6f&width=%d&height=%d&format=fits&projection=TAN",
            HIPS_BASE_URL,
            URLEncoder.encode(survey, "UTF-8"),
            ra, dec, fov, size, size
        );
        
        System.out.println("URL: " + urlStr);
        
        URI uri = URI.create(urlStr);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "Seqplot-Test/1.0");
        
        int responseCode = connection.getResponseCode();
        System.out.println("Response code: " + responseCode);
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] fitsData = inputStream.readAllBytes();
                System.out.println("Downloaded " + fitsData.length + " bytes");
                
                // Check if it's actually FITS data
                if (fitsData.length > 80) {
                    String header = new String(fitsData, 0, Math.min(80, fitsData.length), "US-ASCII");
                    System.out.println("Header: " + header.trim());
                    
                    if (header.startsWith("SIMPLE")) {
                        System.out.println("Valid FITS file detected");
                        return processFITS(fitsData, size);
                    } else {
                        System.err.println("Not a FITS file!");
                    }
                }
            }
        } else {
            System.err.println("HTTP error: " + responseCode);
        }
        
        return null;
    }
    
    /**
     * Simple FITS processing to extract image data
     */
    private static BufferedImage processFITS(byte[] fitsData, int expectedSize) throws Exception {
        // FITS header is 2880-byte blocks
        int headerSize = 2880;
        while (headerSize < fitsData.length) {
            String block = new String(fitsData, headerSize - 2880, 2880, "US-ASCII");
            if (block.contains("END")) {
                break;
            }
            headerSize += 2880;
        }
        
        System.out.println("FITS header size: " + headerSize + " bytes");
        System.out.println("Image data size: " + (fitsData.length - headerSize) + " bytes");
        
        // Extract image data (assuming 16-bit integers)
        int dataSize = fitsData.length - headerSize;
        int pixelCount = dataSize / 2;  // 2 bytes per pixel
        int imageWidth = (int) Math.sqrt(pixelCount);
        int imageHeight = imageWidth;
        
        System.out.println("Detected image size: " + imageWidth + "x" + imageHeight);
        
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        
        int dataIndex = headerSize;
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                if (dataIndex + 1 < fitsData.length) {
                    // Read 16-bit big-endian value
                    int high = fitsData[dataIndex++] & 0xFF;
                    int low = fitsData[dataIndex++] & 0xFF;
                    int value = (high << 8) | low;
                    
                    // Scale to 0-255 range (simple linear scaling)
                    int gray = Math.min(255, Math.max(0, value / 256));
                    int rgb = (gray << 16) | (gray << 8) | gray;
                    
                    image.setRGB(x, y, rgb);
                }
            }
        }
        
        return image;
    }
    
    /**
     * Check if image is blank (all pixels same color or very close)
     */
    private static boolean checkIfBlank(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (width == 0 || height == 0) return true;
        
        // Sample pixels from different parts of the image
        int firstPixel = image.getRGB(width/2, height/2);
        int sampleCount = 0;
        int matchCount = 0;
        
        // Check corners and center
        int[][] positions = {
            {10, 10}, {width-10, 10}, {10, height-10}, {width-10, height-10},
            {width/2, height/2}, {width/4, height/4}, {3*width/4, 3*height/4}
        };
        
        for (int[] pos : positions) {
            int pixel = image.getRGB(pos[0], pos[1]);
            sampleCount++;
            if (pixel == firstPixel) {
                matchCount++;
            }
        }
        
        // If more than 80% of samples are identical, likely blank
        return (matchCount > sampleCount * 0.8);
    }
    
    /**
     * Get pixel statistics: min, max, average brightness
     */
    private static int[] getPixelStats(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        int min = 255;
        int max = 0;
        long sum = 0;
        int count = 0;
        
        // Sample every 10th pixel for speed
        for (int y = 0; y < height; y += 10) {
            for (int x = 0; x < width; x += 10) {
                int rgb = image.getRGB(x, y);
                // Convert to grayscale
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;
                
                min = Math.min(min, gray);
                max = Math.max(max, gray);
                sum += gray;
                count++;
            }
        }
        
        int avg = (int)(sum / count);
        return new int[]{min, max, avg};
    }
}

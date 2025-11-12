package AAVSOtools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Manages DSS2 background images from HiPS service
 * Fetches astronomical survey images to display behind star plots
 */
public class DSS2Manager {
    
    private static final String HIPS_BASE_URL = "https://alasky.cds.unistra.fr/hips-image-services/hips2fits";
    private static final String DEFAULT_SURVEY = "CDS/P/DSS2/color"; // Default DSS2 color survey
    private static final int DEFAULT_SIZE = 1024; // Default image size in pixels (increased from 512)
    private static final int TIMEOUT_MS = 15000; // 15 second timeout
    
    private ExecutorService downloadExecutor;
    private volatile BufferedImage currentImage;
    private volatile boolean isLoading = false;
    private DSS2LoadListener loadListener;
    private String currentSurvey = DEFAULT_SURVEY; // Currently selected survey
    
    // Current image parameters
    private double currentCenterRA = Double.NaN;
    private double currentCenterDec = Double.NaN;
    private double currentFOV = Double.NaN;
    private WCSParameters currentWCS = null;
    
    /**
     * WCS (World Coordinate System) parameters for coordinate transformation
     * This allows us to use the DSS2 image as the authoritative coordinate reference
     */
    public static class WCSParameters {
        // Reference pixel coordinates (1-based, center of image)
        public final double crpix1; // X reference pixel
        public final double crpix2; // Y reference pixel
        
        // Reference world coordinates (degrees)
        public final double crval1; // RA at reference pixel
        public final double crval2; // Dec at reference pixel
        
        // Pixel scale (degrees per pixel)
        public final double cdelt1; // RA pixel scale (negative for standard orientation)
        public final double cdelt2; // Dec pixel scale
        
        // Image dimensions
        public final int naxis1; // Image width in pixels
        public final int naxis2; // Image height in pixels
        
        public WCSParameters(double centerRA, double centerDec, double fovDeg, int imageSize) {
            // Reference pixel at center of image (1-based indexing)
            this.crpix1 = (imageSize + 1) / 2.0;
            this.crpix2 = (imageSize + 1) / 2.0;
            
            // Reference coordinates at center
            this.crval1 = centerRA;
            this.crval2 = centerDec;
            
            // Calculate pixel scale from field of view
            // FOV is total field, so pixel scale = FOV / imageSize
            this.cdelt1 = -fovDeg / imageSize; // Negative for standard RA orientation
            this.cdelt2 = fovDeg / imageSize;
            
            // Image dimensions
            this.naxis1 = imageSize;
            this.naxis2 = imageSize;
        }
        
        /**
         * Constructor from individual FITS WCS parameters
         */
        public WCSParameters(double crval1, double crval2, double crpix1, double crpix2, 
                           double cdelt1, double cdelt2, int naxis1, int naxis2) {
            this.crval1 = crval1;
            this.crval2 = crval2;
            this.crpix1 = crpix1;
            this.crpix2 = crpix2;
            this.cdelt1 = cdelt1;
            this.cdelt2 = cdelt2;
            this.naxis1 = naxis1;
            this.naxis2 = naxis2;
        }
        
        /**
         * Convert world coordinates (RA, Dec) to pixel coordinates
         * @param ra Right Ascension in degrees
         * @param dec Declination in degrees
         * @return double[2] array: [x_pixel, y_pixel] (0-based)
         */
        public double[] worldToPixel(double ra, double dec) {
            // Convert degrees to radians
            double raRad = Math.toRadians(ra);
            double decRad = Math.toRadians(dec);
            double crval1Rad = Math.toRadians(crval1);
            double crval2Rad = Math.toRadians(crval2);
            
            // Tangent plane projection (TAN) - proper implementation
            double cosc = Math.sin(crval2Rad) * Math.sin(decRad) + 
                         Math.cos(crval2Rad) * Math.cos(decRad) * Math.cos(raRad - crval1Rad);
            
            if (cosc <= 0) {
                // Point is on the far side of the celestial sphere
                return new double[]{Double.NaN, Double.NaN};
            }
            
            // Tangent plane coordinates (in radians)
            double xi = Math.cos(decRad) * Math.sin(raRad - crval1Rad) / cosc;
            double eta = (Math.cos(crval2Rad) * Math.sin(decRad) - 
                         Math.sin(crval2Rad) * Math.cos(decRad) * Math.cos(raRad - crval1Rad)) / cosc;
            
            // Convert to degrees
            xi = Math.toDegrees(xi);
            eta = Math.toDegrees(eta);
            
            // Convert to pixel coordinates
            double x_pixel = crpix1 - 1 + xi / cdelt1;
            double y_pixel = crpix2 - 1 + eta / cdelt2;
            
            return new double[]{x_pixel, y_pixel};
        }
        
        /**
         * Convert pixel coordinates to world coordinates (RA, Dec)
         * @param xPixel X pixel coordinate (0-based)
         * @param yPixel Y pixel coordinate (0-based)
         * @return double[2] array: [ra, dec] in degrees
         */
        public double[] pixelToWorld(double xPixel, double yPixel) {
            // Convert from pixel coordinates to tangent plane coordinates
            double xi = (xPixel + 1 - crpix1) * cdelt1; // in degrees
            double eta = (yPixel + 1 - crpix2) * cdelt2; // in degrees
            
            // Convert degrees to radians for spherical trigonometry
            double xiRad = Math.toRadians(xi);
            double etaRad = Math.toRadians(eta);
            double crval1Rad = Math.toRadians(crval1);
            double crval2Rad = Math.toRadians(crval2);
            
            // Inverse tangent plane projection
            double rho = Math.sqrt(xiRad * xiRad + etaRad * etaRad);
            double c = Math.atan(rho);
            
            double sinc = Math.sin(c);
            double cosc = Math.cos(c);
            
            // Calculate RA and Dec
            double decRad = Math.asin(cosc * Math.sin(crval2Rad) + 
                                     etaRad * sinc * Math.cos(crval2Rad) / rho);
            
            double raRad = crval1Rad + Math.atan2(xiRad * sinc, 
                                                 rho * Math.cos(crval2Rad) * cosc - 
                                                 etaRad * Math.sin(crval2Rad) * sinc);
            
            // Convert back to degrees
            double ra = Math.toDegrees(raRad);
            double dec = Math.toDegrees(decRad);
            
            return new double[]{ra, dec};
        }
        
        @Override
        public String toString() {
            return String.format("WCS[center=(%.6f,%.6f), scale=(%.6f,%.6f), ref=(%.1f,%.1f), size=(%d,%d)]",
                crval1, crval2, cdelt1, cdelt2, crpix1, crpix2, naxis1, naxis2);
        }
    }
    
    public DSS2Manager() {
        this.downloadExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DSS2-Downloader");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Interface for notification when DSS2 image loading completes
     */
    public interface DSS2LoadListener {
        void onImageLoaded(BufferedImage image);
        void onImageLoadFailed(String error);
    }
    
    public void setLoadListener(DSS2LoadListener listener) {
        this.loadListener = listener;
    }
    
    /**
     * Fetch DSS2 image for the specified field
     * @param centerRA Right ascension in degrees
     * @param centerDec Declination in degrees  
     * @param fieldOfViewDeg Field of view in degrees
     * @param imageSize Image size in pixels (width=height)
     */
    /**
     * Fetch survey image at default survey (DSS2)
     */
    public void fetchDSS2Image(double centerRA, double centerDec, double fieldOfViewDeg, int imageSize) {
        fetchDSS2Image(centerRA, centerDec, fieldOfViewDeg, imageSize, DEFAULT_SURVEY);
    }
    
    /**
     * Fetch survey image at specified survey
     */
    public void fetchDSS2Image(double centerRA, double centerDec, double fieldOfViewDeg, int imageSize, String survey) {
        // Check if we already have this image
        if (currentImage != null && 
            Math.abs(currentCenterRA - centerRA) < 0.0001 &&
            Math.abs(currentCenterDec - centerDec) < 0.0001 &&
            Math.abs(currentFOV - fieldOfViewDeg) < 0.0001 &&
            survey.equals(currentSurvey)) {
            // System.out.println("DEBUG: Survey image already cached for this field");
            return;
        }
        
        if (isLoading) {
            // System.out.println("DEBUG: Survey image already loading, skipping duplicate request");
            return;
        }
        
        System.out.printf("DEBUG: Fetching survey image - Survey=%s, RA=%.6f, Dec=%.6f, FOV=%.4f deg, Size=%d px\n",
                         survey, centerRA, centerDec, fieldOfViewDeg, imageSize);
        
        currentSurvey = survey;
        isLoading = true;
        
        downloadExecutor.submit(() -> {
            try {
                BufferedImage image = downloadHiPSImage(centerRA, centerDec, fieldOfViewDeg, imageSize, survey);
                
                if (image != null) {
                    currentImage = image;
                    currentCenterRA = centerRA;
                    currentCenterDec = centerDec;
                    currentFOV = fieldOfViewDeg;
                    
                    // Create WCS parameters for this image
                    currentWCS = new WCSParameters(centerRA, centerDec, fieldOfViewDeg, imageSize);
                    
                    System.out.printf("DEBUG: DSS2 image loaded successfully - %dx%d pixels\n", 
                                     image.getWidth(), image.getHeight());
                    System.out.printf("DEBUG: WCS parameters: %s\n", currentWCS);
                    
                    // Notify on EDT
                    SwingUtilities.invokeLater(() -> {
                        if (loadListener != null) {
                            loadListener.onImageLoaded(image);
                        }
                    });
                } else {
                    currentImage = null;
                    currentWCS = null;
                    System.err.println("ERROR: Failed to download DSS2 image");
                    SwingUtilities.invokeLater(() -> {
                        if (loadListener != null) {
                            loadListener.onImageLoadFailed("Failed to download image");
                        }
                    });
                }
            } catch (Exception e) {
                currentImage = null;
                currentWCS = null;
                System.err.printf("ERROR: Exception downloading DSS2 image: %s\n", e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    if (loadListener != null) {
                        loadListener.onImageLoadFailed("Download error: " + e.getMessage());
                    }
                });
            } finally {
                isLoading = false;
            }
        });
    }
    
    /**
     * Download image from HiPS service
     */
    /**
     * Download FITS image from HiPS service and extract WCS parameters
     */
    private BufferedImage downloadHiPSImage(double centerRA, double centerDec, double fieldOfViewDeg, int imageSize, String survey) throws IOException {
        // All surveys use CDS HiPS2FITS service with FITS format for accurate WCS and image data
        
        // Build HiPS2FITS URL for other surveys
        // Format: hips2fits?hips=SURVEY&ra=RA&dec=DEC&fov=FOV&width=SIZE&height=SIZE&format=fits&projection=TAN
        String urlStr = String.format(
            "%s?hips=%s&ra=%.6f&dec=%.6f&fov=%.6f&width=%d&height=%d&format=fits&projection=TAN",
            HIPS_BASE_URL,
            URLEncoder.encode(survey, "UTF-8"),
            centerRA,
            centerDec, 
            fieldOfViewDeg,
            imageSize,
            imageSize
        );
        
        System.out.printf("DEBUG: HiPS FITS URL: %s\n", urlStr);
        
        URI uri = URI.create(urlStr);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        
        try {
            // Set timeouts and headers
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Seqplot-6.0.0-FITS-Client");
            
            int responseCode = connection.getResponseCode();
            System.out.printf("DEBUG: HiPS response code: %d\n", responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    // Download FITS file to temporary location
                    byte[] fitsData = inputStream.readAllBytes();
                    System.out.printf("DEBUG: Downloaded FITS file: %d bytes\n", fitsData.length);
                    
                    // Parse FITS file and extract image + WCS
                    return processFITSFile(fitsData, centerRA, centerDec, fieldOfViewDeg, imageSize);
                }
            } else {
                // Read error response body for more details
                String errorMessage = connection.getResponseMessage();
                try (InputStream errorStream = connection.getErrorStream()) {
                    if (errorStream != null) {
                        byte[] errorBytes = errorStream.readAllBytes();
                        if (errorBytes.length > 0) {
                            errorMessage += " - " + new String(errorBytes, "UTF-8");
                        }
                    }
                } catch (Exception e) {
                    // Ignore error stream reading failures
                }
                
                System.err.printf("ERROR: Survey '%s' failed with HTTP %d: %s\n", survey, responseCode, errorMessage);
                System.err.println("This survey may not be available for this field or the service may be down.");
                throw new IOException("HTTP error " + responseCode + ": " + errorMessage);
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Download SDSS image using SIAP (Simple Image Access Protocol)
     * This is the recommended method for SDSS as documented in their API
     */
    private BufferedImage downloadSDSSviaSIAP(double centerRA, double centerDec, double fieldOfViewDeg, int imageSize) throws IOException {
        // SIAP getSIAP endpoint with FORMAT=image/fits
        // POS = "RA,DEC" (comma-separated), SIZE = field of view in degrees
        String siapUrl = String.format(
            "https://skyserver.sdss.org/dr17/SkyServerWS/SIAP/getSIAP?POS=%.6f,%.6f&SIZE=%.6f&FORMAT=image/fits",
            centerRA,
            centerDec,
            fieldOfViewDeg
        );
        
        System.out.printf("DEBUG: SDSS SIAP URL: %s\n", siapUrl);
        
        URI uri = URI.create(siapUrl);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        
        try {
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Seqplot-6.0.0-SDSS-Client");
            
            int responseCode = connection.getResponseCode();
            System.out.printf("DEBUG: SDSS SIAP response code: %d\n", responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    // SIAP returns VOTable XML with image URLs, not the image itself
                    byte[] votableData = inputStream.readAllBytes();
                    System.out.printf("DEBUG: SDSS SIAP VOTable response: %d bytes\n", votableData.length);
                    
                    // Parse VOTable to extract FITS URL
                    String fitsUrl = extractFITSUrlFromVOTable(votableData);
                    if (fitsUrl == null || fitsUrl.isEmpty()) {
                        throw new IOException("No FITS URL found in SDSS SIAP response (field may have no coverage)");
                    }
                    
                    System.out.printf("DEBUG: Downloading FITS from: %s\n", fitsUrl);
                    
                    // Now download the actual FITS file
                    HttpURLConnection fitsConnection = (HttpURLConnection) URI.create(fitsUrl).toURL().openConnection();
                    try {
                        fitsConnection.setConnectTimeout(TIMEOUT_MS);
                        fitsConnection.setReadTimeout(TIMEOUT_MS);
                        fitsConnection.setRequestProperty("User-Agent", "Seqplot-6.0.0-SDSS-Client");
                        
                        int fitsResponseCode = fitsConnection.getResponseCode();
                        if (fitsResponseCode == HttpURLConnection.HTTP_OK) {
                            try (InputStream fitsStream = fitsConnection.getInputStream()) {
                                byte[] fitsData = fitsStream.readAllBytes();
                                System.out.printf("DEBUG: SDSS FITS file downloaded: %d bytes\n", fitsData.length);
                                
                                // Process FITS file and extract image + WCS
                                return processFITSFile(fitsData, centerRA, centerDec, fieldOfViewDeg, imageSize);
                            }
                        } else {
                            throw new IOException("Failed to download FITS file: HTTP " + fitsResponseCode);
                        }
                    } finally {
                        fitsConnection.disconnect();
                    }
                }
            } else {
                String errorMessage = connection.getResponseMessage();
                try (InputStream errorStream = connection.getErrorStream()) {
                    if (errorStream != null) {
                        byte[] errorBytes = errorStream.readAllBytes();
                        if (errorBytes.length > 0) {
                            errorMessage += " - " + new String(errorBytes, "UTF-8");
                        }
                    }
                } catch (Exception e) {
                    // Ignore error stream reading failures
                }
                
                System.err.printf("ERROR: SDSS SIAP failed with HTTP %d: %s\n", responseCode, errorMessage);
                System.err.println("SDSS may not have coverage for this field.");
                throw new IOException("HTTP error " + responseCode + ": " + errorMessage);
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Extract FITS file URL from SIAP VOTable XML response
     * The VOTable contains a <TR> with <TD> elements, where one TD contains the FITS URL
     */
    private String extractFITSUrlFromVOTable(byte[] votableData) {
        try {
            String xml = new String(votableData, "UTF-8");
            
            // Look for <TD> elements containing FITS URLs
            // SIAP VOTable format typically has URLs in <TD> tags
            // Look for .fits or .fit file extensions
            String[] lines = xml.split("\n");
            for (String line : lines) {
                // Check for <TD> containing a URL with .fits or .fit
                if (line.contains("<TD>") && (line.contains(".fits") || line.contains(".fit"))) {
                    // Extract URL between <TD> and </TD>
                    int start = line.indexOf("<TD>") + 4;
                    int end = line.indexOf("</TD>");
                    if (start > 3 && end > start) {
                        String url = line.substring(start, end).trim();
                        // Validate it looks like a URL
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            System.out.printf("DEBUG: Found FITS URL in VOTable: %s\n", url);
                            return url;
                        }
                    }
                }
            }
            
            // Alternative: look for RESOURCE/TABLE/DATA/TABLEDATA/TR/TD pattern
            // Print first 1000 chars of XML for debugging
            System.out.printf("DEBUG: VOTable content (first 1000 chars):\n%s\n", 
                xml.length() > 1000 ? xml.substring(0, 1000) : xml);
            
            return null;
        } catch (Exception e) {
            System.err.printf("ERROR parsing VOTable: %s\n", e.getMessage());
            return null;
        }
    }
    
    /**
     * Scale an image to the specified dimensions
     */
    private BufferedImage scaleImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return scaled;
    }
    
    /**
     * Process FITS file to extract image data and WCS parameters
     */
    private BufferedImage processFITSFile(byte[] fitsData, double requestedCenterRA, double requestedCenterDec, 
                                        double requestedFOV, int requestedSize) throws IOException {
        // System.out.println("DEBUG: Processing FITS file for image and WCS data");
        
        // Store current request parameters for JPG fallback
        currentCenterRA = requestedCenterRA;
        currentCenterDec = requestedCenterDec;
        currentFOV = requestedFOV;
        
        // Simple FITS header parser - extract key WCS parameters
        String header = extractFITSHeader(fitsData);
        WCSParameters actualWCS = parseWCSFromHeader(header, requestedCenterRA, requestedCenterDec, requestedFOV, requestedSize);
        
        // Convert FITS data to BufferedImage
        BufferedImage image = convertFITSToImage(fitsData);
        
        // Update current WCS with actual FITS parameters
        currentWCS = actualWCS;
        System.out.printf("DEBUG: Extracted WCS from FITS: %s\n", currentWCS);
        
        return image;
    }
    
    /**
     * Extract FITS header as string for parsing
     */
    private String extractFITSHeader(byte[] fitsData) {
        // FITS headers are 2880-byte blocks of 80-character cards
        // Read until we find END card
        StringBuilder header = new StringBuilder();
        int headerSize = 0;
        
        for (int i = 0; i < fitsData.length - 80; i += 80) {
            String card = new String(fitsData, i, 80).trim();
            header.append(card).append("\n");
            headerSize += 80;
            
            if (card.startsWith("END ") || card.equals("END")) {
                break;
            }
        }
        
        System.out.printf("DEBUG: Extracted FITS header (%d bytes)\n", headerSize);
        return header.toString();
    }
    
    /**
     * Parse WCS parameters from FITS header
     */
    private WCSParameters parseWCSFromHeader(String header, double fallbackRA, double fallbackDec, 
                                           double fallbackFOV, int fallbackSize) {
        try {
            // Extract key WCS parameters from header
            double crval1 = extractKeywordValue(header, "CRVAL1", fallbackRA);
            double crval2 = extractKeywordValue(header, "CRVAL2", fallbackDec);
            double crpix1 = extractKeywordValue(header, "CRPIX1", fallbackSize / 2.0 + 0.5);
            double crpix2 = extractKeywordValue(header, "CRPIX2", fallbackSize / 2.0 + 0.5);
            double cdelt1 = extractKeywordValue(header, "CDELT1", -fallbackFOV / fallbackSize);
            double cdelt2 = extractKeywordValue(header, "CDELT2", fallbackFOV / fallbackSize);
            int naxis1 = (int)extractKeywordValue(header, "NAXIS1", fallbackSize);
            int naxis2 = (int)extractKeywordValue(header, "NAXIS2", fallbackSize);
            
            System.out.printf("DEBUG: FITS WCS - CRVAL1=%.6f, CRVAL2=%.6f, CDELT1=%.6f, CDELT2=%.6f\n", 
                             crval1, crval2, cdelt1, cdelt2);
            
            return new WCSParameters(crval1, crval2, crpix1, crpix2, cdelt1, cdelt2, naxis1, naxis2);
        } catch (Exception e) {
            // System.out.println("DEBUG: Failed to parse FITS WCS, using fallback parameters");
            return new WCSParameters(fallbackRA, fallbackDec, fallbackFOV, fallbackSize);
        }
    }
    
    /**
     * Extract numerical value for FITS keyword
     */
    private double extractKeywordValue(String header, String keyword, double defaultValue) {
        String pattern = keyword + "\\s*=\\s*([+-]?\\d*\\.?\\d+(?:[Ee][+-]?\\d+)?)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(header);
        
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Convert FITS data to BufferedImage (simple grayscale conversion)
     */
    private BufferedImage convertFITSToImage(byte[] fitsData) throws IOException {
        // System.out.println("DEBUG: Converting FITS to displayable image (requesting JPG version)");
        
        // First try to get JPG version for display while keeping FITS WCS
        try {
            return downloadImageForDisplay();
        } catch (Exception e) {
            System.out.printf("DEBUG: JPG download failed (%s), attempting FITS image extraction\n", e.getMessage());
            
            // If JPG fails, try to extract image data from FITS
            try {
                return extractImageFromFITS(fitsData);
            } catch (Exception fitsError) {
                System.out.printf("DEBUG: FITS extraction also failed (%s), creating placeholder\n", fitsError.getMessage());
                
                // Last resort: create a simple placeholder
                BufferedImage placeholder = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = placeholder.createGraphics();
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, 512, 512);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString("DSS2 Image Placeholder", 200, 256);
                g2d.dispose();
                // System.out.println("DEBUG: Created placeholder image");
                return placeholder;
            }
        }
    }
    
    /**
     * Extract image data directly from FITS file
     */
    private BufferedImage extractImageFromFITS(byte[] fitsData) throws IOException {
        // System.out.println("DEBUG: Attempting to extract image data from FITS file");
        
        // Simple FITS image extraction - this is a basic implementation
        // Real FITS parsing would need to handle different data types, scaling, etc.
        
        // Skip header to get to image data (header ends at first 2880-byte boundary after END)
        int headerEnd = 0;
        for (int i = 0; i < fitsData.length - 80; i += 80) {
            String card = new String(fitsData, i, 80).trim();
            if (card.startsWith("END ") || card.equals("END")) {
                headerEnd = ((i + 80) / 2880 + 1) * 2880; // Round up to next 2880-byte boundary
                break;
            }
        }
        
        System.out.printf("DEBUG: FITS header ends at byte %d\n", headerEnd);
        
        if (headerEnd >= fitsData.length) {
            throw new IOException("No image data found in FITS file");
        }
        
        // For now, create a simple grayscale representation
        // In a full implementation, we would parse BITPIX, NAXIS1, NAXIS2, etc.
        int imageSize = 512; // Assume square image
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_BYTE_GRAY);
        
        // Simple conversion: take every nth byte as grayscale value
        int dataLength = fitsData.length - headerEnd;
        int step = Math.max(1, dataLength / (imageSize * imageSize));
        
        WritableRaster raster = image.getRaster();
        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                int dataIndex = headerEnd + (y * imageSize + x) * step;
                if (dataIndex < fitsData.length) {
                    int value = fitsData[dataIndex] & 0xFF; // Convert to unsigned byte
                    raster.setSample(x, y, 0, value);
                }
            }
        }
        
        // System.out.println("DEBUG: Extracted grayscale image from FITS data");
        return image;
    }
    
    /**
     * Download JPG version for display purposes
     */
    private BufferedImage downloadImageForDisplay() throws IOException {
        String urlStr = String.format(
            "%s?hips=%s&ra=%.6f&dec=%.6f&fov=%.6f&width=%d&height=%d&format=jpg&projection=TAN",
            HIPS_BASE_URL,
            URLEncoder.encode(currentSurvey, "UTF-8"),
            currentCenterRA,
            currentCenterDec,
            currentFOV,
            512, 512
        );
        
        System.out.printf("DEBUG: JPG URL: %s\n", urlStr);
        
        URI uri = URI.create(urlStr);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        
        try {
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            
            int responseCode = connection.getResponseCode();
            System.out.printf("DEBUG: JPG response code: %d\n", responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);
                    if (image != null) {
                        System.out.printf("DEBUG: JPG image loaded: %dx%d\n", image.getWidth(), image.getHeight());
                        
                        // Check if image is blank (no coverage)
                        if (isImageBlank(image)) {
                            System.err.println("WARNING: Survey returned blank/white image - no coverage for this field");
                            throw new IOException("No survey coverage for this field");
                        }
                        
                        return image;
                    } else {
                        // System.out.println("DEBUG: ImageIO.read returned null");
                    }
                }
            } else {
                System.out.printf("DEBUG: JPG download failed with code: %d\n", responseCode);
            }
        } catch (IOException e) {
            // Re-throw IOException to propagate "no coverage" message
            throw e;
        } catch (Exception e) {
            System.out.printf("DEBUG: JPG download exception: %s\n", e.getMessage());
        } finally {
            connection.disconnect();
        }
        
        throw new IOException("Failed to download display image");
    }
    
    /**
     * Check if an image is blank (all pixels similar color)
     * This detects when a survey has no coverage and returns a white/grey placeholder
     */
    private boolean isImageBlank(BufferedImage image) {
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
        
        // An astronomical image should have significant brightness variation
        // Blank/white images have very low variation (< 10) and high average brightness (> 200)
        // Or very low brightness (< 10) if all black
        boolean isBlank = (brightnessRange < 10 && avgBrightness > 200) || 
                         (brightnessRange < 10 && avgBrightness < 10);
        
        if (isBlank) {
            System.out.printf("DEBUG: Image detected as blank (range=%d, avg=%d, samples=%d)\n", 
                            brightnessRange, avgBrightness, sampleCount);
        }
        
        return isBlank;
    }
    
    /**
     * Get the current loaded DSS2 image
     * @return BufferedImage or null if no image loaded
     */
    public BufferedImage getCurrentImage() {
        return currentImage;
    }
    
    /**
     * Check if image is currently being loaded
     */
    public boolean isLoading() {
        return isLoading;
    }
    
    /**
     * Clear the current image cache
     */
    public void clearImage() {
        currentImage = null;
        currentCenterRA = Double.NaN;
        currentCenterDec = Double.NaN;
        currentFOV = Double.NaN;
        currentWCS = null;
        // System.out.println("DEBUG: DSS2 image cache cleared");
    }
    
    /**
     * Get the current WCS parameters
     * @return WCSParameters or null if no image loaded
     */
    public WCSParameters getCurrentWCS() {
        return currentWCS;
    }
    
    /**
     * Check if WCS parameters are available
     */
    public boolean hasWCS() {
        return currentWCS != null;
    }
    
    /**
     * Shutdown the download executor
     */
    public void shutdown() {
        if (downloadExecutor != null && !downloadExecutor.isShutdown()) {
            downloadExecutor.shutdown();
            try {
                if (!downloadExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    downloadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                downloadExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Calculate appropriate field of view based on data extent
     * @param minRA Minimum RA in degrees
     * @param maxRA Maximum RA in degrees
     * @param minDec Minimum Dec in degrees
     * @param maxDec Maximum Dec in degrees
     * @param marginFactor Extra margin around data (1.2 = 20% margin)
     * @return Field of view in degrees
     */
    public static double calculateFieldOfView(double minRA, double maxRA, double minDec, double maxDec, double marginFactor) {
        double raRange = Math.abs(maxRA - minRA);
        double decRange = Math.abs(maxDec - minDec);
        
        // Handle RA wraparound at 0/360 degrees
        if (raRange > 180.0) {
            raRange = 360.0 - raRange;
        }
        
        // Adjust RA range for declination (RA coverage decreases near poles)
        double avgDec = (minDec + maxDec) / 2.0;
        double raRangeAdjusted = raRange * Math.cos(Math.toRadians(avgDec));
        
        // Use the larger dimension and add margin
        double baseFOV = Math.max(raRangeAdjusted, decRange);
        double fov = baseFOV * marginFactor;
        
        // Clamp to reasonable limits (DSS2 works well from ~1 arcmin to ~10 degrees)
        fov = Math.max(fov, 0.02);  // Minimum 1.2 arcmin
        fov = Math.min(fov, 10.0);  // Maximum 10 degrees
        
        System.out.printf("DEBUG: Calculated FOV - RA range=%.4f°, Dec range=%.4f°, FOV=%.4f°\n",
                         raRange, decRange, fov);
        
        return fov;
    }
}
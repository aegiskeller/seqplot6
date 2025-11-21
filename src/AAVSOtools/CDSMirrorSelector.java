package AAVSOtools;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * CDSMirrorSelector manages VizieR/CDS mirror endpoints and provides health checking.
 * Allows users to select from multiple mirror sites when the primary is unavailable.
 */
public class CDSMirrorSelector {
    
    public static class MirrorEndpoint {
        public String name;
        public String baseUrl;
        public String tapUrl;
        public HealthStatus status;
        public long lastCheckTime;
        
        public MirrorEndpoint(String name, String baseUrl, String tapUrl) {
            this.name = name;
            this.baseUrl = baseUrl;
            this.tapUrl = tapUrl;
            this.status = HealthStatus.UNKNOWN;
            this.lastCheckTime = 0;
        }
    }
    
    public enum HealthStatus {
        HEALTHY("Healthy", new java.awt.Color(0, 180, 0)),           // Green
        SLOW("Slow", new java.awt.Color(255, 165, 0)),              // Orange
        UNREACHABLE("Unreachable", new java.awt.Color(200, 0, 0)),  // Red
        UNKNOWN("Unknown", new java.awt.Color(128, 128, 128));       // Gray
        
        public final String displayText;
        public final java.awt.Color color;
        
        HealthStatus(String displayText, java.awt.Color color) {
            this.displayText = displayText;
            this.color = color;
        }
    }
    
    private static final List<MirrorEndpoint> MIRRORS = new ArrayList<>();
    private static MirrorEndpoint selectedMirror;
    private static final long HEALTH_CHECK_CACHE_TIME = 5 * 60 * 1000; // 5 minutes
    private static final int HEALTH_CHECK_TIMEOUT = 5000; // 5 seconds
    
    static {
        // Initialize mirror list
        MIRRORS.add(new MirrorEndpoint(
            "CDS Strasbourg (Primary)",
            "https://vizier.u-strasbg.fr/",
            "http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"
        ));
        MIRRORS.add(new MirrorEndpoint(
            "NAO Japan Mirror",
            "http://vizier.nao.ac.jp/",
            "http://vizier.nao.ac.jp/TAPVizieR/tap/sync"
        ));
        MIRRORS.add(new MirrorEndpoint(
            "CfA Harvard Mirror",
            "https://vizier.cfa.harvard.edu/",
            "https://vizier.cfa.harvard.edu/TAPVizieR/tap/sync"
        ));
        MIRRORS.add(new MirrorEndpoint(
            "CDS Arc (Archive)",
            "http://cdsarc.u-strasbg.fr/",
            "http://cdsarc.u-strasbg.fr/TAPVizieR/tap/sync"
        ));
        
        // Select primary mirror by default
        selectedMirror = MIRRORS.get(0);
    }
    
    /**
     * Get the list of all available mirrors
     */
    public static List<MirrorEndpoint> getMirrors() {
        return new ArrayList<>(MIRRORS);
    }
    
    /**
     * Get the currently selected mirror
     */
    public static MirrorEndpoint getSelectedMirror() {
        return selectedMirror;
    }
    
    /**
     * Set the selected mirror
     */
    public static void setSelectedMirror(MirrorEndpoint mirror) {
        if (MIRRORS.contains(mirror)) {
            selectedMirror = mirror;
        }
    }
    
    /**
     * Get the TAP URL for the selected mirror
     */
    public static String getSelectedTapUrl() {
        return selectedMirror.tapUrl;
    }
    
    /**
     * Check health of a single mirror endpoint asynchronously
     */
    public static CompletableFuture<HealthStatus> checkMirrorHealthAsync(MirrorEndpoint mirror) {
        return CompletableFuture.supplyAsync(() -> checkMirrorHealth(mirror));
    }
    
    /**
     * Check health of a single mirror endpoint
     */
    public static HealthStatus checkMirrorHealth(MirrorEndpoint mirror) {
        // Check cache first
        long currentTime = System.currentTimeMillis();
        if (mirror.status != HealthStatus.UNKNOWN && 
            (currentTime - mirror.lastCheckTime) < HEALTH_CHECK_CACHE_TIME) {
            return mirror.status;
        }
        
        try {
            // Try to connect to the mirror's base URL with timeout
            URL url = new URL(mirror.baseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(HEALTH_CHECK_TIMEOUT);
            conn.setReadTimeout(HEALTH_CHECK_TIMEOUT);
            conn.setRequestMethod("HEAD");
            
            long startTime = System.currentTimeMillis();
            conn.connect();
            long responseTime = System.currentTimeMillis() - startTime;
            
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            mirror.lastCheckTime = currentTime;
            
            if (responseCode >= 200 && responseCode < 300) {
                // Healthy response
                if (responseTime > 2000) {
                    mirror.status = HealthStatus.SLOW;
                } else {
                    mirror.status = HealthStatus.HEALTHY;
                }
            } else if (responseCode == 503 || responseCode == 502) {
                mirror.status = HealthStatus.SLOW;
            } else {
                mirror.status = HealthStatus.SLOW;
            }
        } catch (java.net.SocketTimeoutException e) {
            mirror.status = HealthStatus.SLOW;
            mirror.lastCheckTime = currentTime;
        } catch (java.net.ConnectException e) {
            mirror.status = HealthStatus.UNREACHABLE;
            mirror.lastCheckTime = currentTime;
        } catch (Exception e) {
            mirror.status = HealthStatus.UNREACHABLE;
            mirror.lastCheckTime = currentTime;
            System.err.println("Error checking mirror health for " + mirror.name + ": " + e.getMessage());
        }
        
        return mirror.status;
    }
    
    /**
     * Check health of all mirrors asynchronously and update their status
     */
    public static CompletableFuture<Void> checkAllMirrorsHealthAsync() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (MirrorEndpoint mirror : MIRRORS) {
            futures.add(checkMirrorHealthAsync(mirror).thenAccept(status -> {
                // Status is already updated in checkMirrorHealth
            }));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    /**
     * Find a healthy mirror (prefer primary, fallback to any healthy one)
     */
    public static MirrorEndpoint findHealthyMirror() {
        // First check selected mirror
        if (checkMirrorHealth(selectedMirror) == HealthStatus.HEALTHY) {
            return selectedMirror;
        }
        
        // Check all mirrors, prefer healthy ones
        for (MirrorEndpoint mirror : MIRRORS) {
            if (checkMirrorHealth(mirror) == HealthStatus.HEALTHY) {
                return mirror;
            }
        }
        
        // If no healthy mirrors, return selected one anyway
        return selectedMirror;
    }
    
    /**
     * Get mirror display string including status
     */
    public static String getMirrorDisplayString(MirrorEndpoint mirror) {
        return mirror.name + " - " + mirror.status.displayText;
    }
}

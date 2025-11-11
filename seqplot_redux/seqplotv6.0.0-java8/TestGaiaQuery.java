import java.net.*;
import java.io.*;

public class TestGaiaQuery {
    public static void main(String[] args) {
        try {
            // Test parameters - EXACT coordinates from your field
            double ra = 283.833750;
            double dec = 43.946080;
            double fieldSize = 0.25;
            double limitingMag = 15.0;
            
            String tapUrl = "http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync";
            
            // Test 1: Count how many Gaia stars are in the field
            System.out.println("=== TEST 1: Count Gaia stars in field ===");
            String countQuery = String.format(
                "SELECT COUNT(*) as cnt FROM \"I/345/gaia2\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', ra, dec), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f)) " +
                "AND phot_g_mean_mag < %.1f",
                ra, dec, fieldSize, limitingMag
            );
            testQuery(tapUrl, countQuery);
            
            // Test 2: Get 10 stars WITHOUT ORDER BY (to avoid timeout)
            System.out.println("\n=== TEST 2: Get 10 Gaia stars (no sorting) ===");
            String brightQuery = String.format(
                "SELECT TOP 10 ra, dec, phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag " +
                "FROM \"I/345/gaia2\" " +
                "WHERE 1=CONTAINS(POINT('ICRS', ra, dec), " +
                "CIRCLE('ICRS', %.6f, %.6f, %.6f)) " +
                "AND phot_bp_mean_mag IS NOT NULL " +
                "AND phot_g_mean_mag <= %.1f",
                ra, dec, fieldSize, limitingMag
            );
            testQuery(tapUrl, brightQuery);
            
        } catch (Exception e) {
            System.err.println("Main error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testQuery(String tapUrl, String adqlQuery) {
        try {
            String queryUrl = tapUrl + "?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=" + 
                             URLEncoder.encode(adqlQuery, "UTF-8");
            
            System.out.println("Query: " + adqlQuery);
            System.out.println("Connecting...");
            
            long startTime = System.currentTimeMillis();
            URL url = new URL(queryUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000); // 15 second timeout
            conn.setReadTimeout(60000); // 60 second timeout
            
            int responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                int lineCount = 0;
                boolean inData = false;
                System.out.println("\nResponse (first 30 lines):");
                while ((line = reader.readLine()) != null && lineCount < 30) {
                    System.out.println(line);
                    if (line.contains("<TD>")) inData = true;
                    lineCount++;
                }
                
                if (!inData) {
                    System.out.println("\n... NO DATA ROWS FOUND in response!");
                }
                
                reader.close();
                
                long endTime = System.currentTimeMillis();
                System.out.println("\nCompleted in " + (endTime - startTime) + " ms");
            } else {
                System.out.println("ERROR: HTTP " + responseCode);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                reader.close();
            }
            
        } catch (Exception e) {
            System.err.println("Query error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

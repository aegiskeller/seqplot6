import java.net.URL;
import java.net.URLEncoder;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class TestSimbadQuery {
    public static void main(String[] args) {
        try {
            String objectName = "NGC 330";
            String simbadUrl = "http://simbad.u-strasbg.fr/simbad/sim-id?output.format=votable&Ident=" + 
                               URLEncoder.encode(objectName.trim(), "UTF-8");
            
            System.out.println("Querying SIMBAD: " + simbadUrl);
            
            URL url = new URL(simbadUrl);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(url.openStream());
            
            // Parse VOTable response
            NodeList tdNodes = doc.getElementsByTagName("TD");
            System.out.println("Found " + tdNodes.getLength() + " TD elements");
            
            // Find FIELD definitions
            NodeList fieldNodes = doc.getElementsByTagName("FIELD");
            System.out.println("Found " + fieldNodes.getLength() + " FIELD elements");
            
            int raIndex = -1;
            int decIndex = -1;
            
            for (int i = 0; i < fieldNodes.getLength(); i++) {
                Element field = (Element) fieldNodes.item(i);
                String name = field.getAttribute("name");
                String ucd = field.getAttribute("ucd");
                String id = field.getAttribute("ID");
                
                System.out.println("Field " + i + ": name=" + name + ", ucd=" + ucd + ", id=" + id);
                
                if ("RA_d".equalsIgnoreCase(name) || "ra".equalsIgnoreCase(name) || 
                    (ucd != null && ucd.contains("pos.eq.ra"))) {
                    raIndex = i;
                    System.out.println("  -> RA field found at index " + i);
                }
                if ("DEC_d".equalsIgnoreCase(name) || "dec".equalsIgnoreCase(name) || 
                    (ucd != null && ucd.contains("pos.eq.dec"))) {
                    decIndex = i;
                    System.out.println("  -> DEC field found at index " + i);
                }
            }
            
            System.out.println("\nRA index: " + raIndex + ", DEC index: " + decIndex);
            
            if (raIndex >= 0 && decIndex >= 0 && tdNodes.getLength() > Math.max(raIndex, decIndex)) {
                String raValue = tdNodes.item(raIndex).getTextContent().trim();
                String decValue = tdNodes.item(decIndex).getTextContent().trim();
                
                System.out.println("\nFound coordinates:");
                System.out.println("RA: " + raValue);
                System.out.println("DEC: " + decValue);
            } else {
                System.out.println("\nCould not extract coordinates");
                System.out.println("Printing all TD values:");
                for (int i = 0; i < Math.min(10, tdNodes.getLength()); i++) {
                    System.out.println("TD[" + i + "]: " + tdNodes.item(i).getTextContent());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

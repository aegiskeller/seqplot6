package AAVSOtools;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Window for building and managing a curated list of standard stars
 * in AAVSO VSP format for Variable Star Photometry
 */
public class SequenceListWindow extends JFrame {
    
    private DataConnector db;
    private JTable table;
    private SequenceTableModel tableModel;
    private JButton exportButton;
    private JButton submitButton;
    private JButton deleteButton;
    private JButton closeButton;
    private JTextField exportPathField;
    private JButton browseButton;
    
    // Column names matching AAVSO format
    private static final String[] COLUMN_NAMES = {
        "#Comp", "RA (HH)", "RA (MM)", "RA (SS.ss)", 
        "Dec (DD)", "Dec (MM)", "Dec (SS.s)",
        "V", "V err", "B-V", "B-V err", "U-B", "U-B err",
        "V-R", "V-R err", "R-I", "R-I err", "V-I", "V-I err",
        "Source", "Comments"
    };
    
    public SequenceListWindow(DataConnector db) {
        this.db = db;
        
        setTitle("AAVSO Comparison Stars - " + db.getStar());
        setSize(1400, 600);
        setLocationRelativeTo(null);
        
        // Set application icon
        try {
            java.net.URL iconURL = getClass().getResource("/AAVSOtools/seqplot_icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                this.setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load application icon: " + e.getMessage());
        }
        
        initComponents();
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu toolsMenu = new JMenu("Tools");
        
        JMenuItem loadVSDFileItem = new JMenuItem("Load VSD Comp File");
        loadVSDFileItem.setToolTipText("Load comparison stars from VSD sequence file");
        loadVSDFileItem.addActionListener(e -> loadVSDCompFile());
        
        toolsMenu.add(loadVSDFileItem);
        menuBar.add(toolsMenu);
        setJMenuBar(menuBar);
        
        // Toolbar panel with export path
        JPanel toolbarPanel = new JPanel(new BorderLayout(5, 0));
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel pathLabel = new JLabel("Export Path:");
        toolbarPanel.add(pathLabel, BorderLayout.WEST);
        
        // Default export path
        String defaultPath = System.getProperty("user.dir") + "/VSDseqs";
        exportPathField = new JTextField(defaultPath);
        toolbarPanel.add(exportPathField, BorderLayout.CENTER);
        
        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browsePath());
        toolbarPanel.add(browseButton, BorderLayout.EAST);
        
        add(toolbarPanel, BorderLayout.NORTH);
        
        // Header panel with instructions
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel titleLabel = new JLabel("AAVSO Comparison Stars List");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel instructionLabel = new JLabel(
            "<html><i>Comparison stars added from main plot. Click star → Send to Comps. " +
            "Double-click Comments cell to edit. Select row and click Delete to remove. " +
            "Color values and errors are red if error > 0.1 (V, B-V, V-R, R-I, V-I).</i></html>"
        );
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        headerPanel.add(instructionLabel, BorderLayout.SOUTH);
        
        // Create a combined panel for toolbar and header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolbarPanel, BorderLayout.NORTH);
        topPanel.add(headerPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table panel
        tableModel = new SequenceTableModel();
        table = new JTable(tableModel) {
            @Override
            public java.awt.Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                java.awt.Component c = super.prepareRenderer(renderer, row, column);
                
                // Column indices: V=7, V_error=8, B-V=9, B-V_error=10, U-B=11, U-B_error=12,
                //                 V-R=13, V-R_error=14, R-I=15, R-I_error=16, V-I=17, V-I_error=18
                boolean highlightRed = false;
                
                // Check if we should highlight V or V_error columns (columns 7 and 8)
                Object vErrObj = getValueAt(row, 8);
                if (vErrObj != null && vErrObj instanceof Number) {
                    double vErr = ((Number)vErrObj).doubleValue();
                    if (vErr > 0.1 && (column == 7 || column == 8)) {
                        highlightRed = true;
                    }
                }
                
                // Check if we should highlight B-V or B-V_error columns (columns 9 and 10)
                Object bvErrObj = getValueAt(row, 10);
                if (bvErrObj != null && bvErrObj instanceof Number) {
                    double bvErr = ((Number)bvErrObj).doubleValue();
                    if (bvErr > 0.1 && (column == 9 || column == 10)) {
                        highlightRed = true;
                    }
                }
                
                // Check if we should highlight V-R or V-R_error columns (columns 13 and 14)
                Object vrErrObj = getValueAt(row, 14);
                if (vrErrObj != null && vrErrObj instanceof Number) {
                    double vrErr = ((Number)vrErrObj).doubleValue();
                    if (vrErr > 0.1 && (column == 13 || column == 14)) {
                        highlightRed = true;
                    }
                }
                
                // Check if we should highlight R-I or R-I_error columns (columns 15 and 16)
                Object riErrObj = getValueAt(row, 16);
                if (riErrObj != null && riErrObj instanceof Number) {
                    double riErr = ((Number)riErrObj).doubleValue();
                    if (riErr > 0.1 && (column == 15 || column == 16)) {
                        highlightRed = true;
                    }
                }
                
                // Check if we should highlight V-I or V-I_error columns (columns 17 and 18)
                Object viErrObj = getValueAt(row, 18);
                if (viErrObj != null && viErrObj instanceof Number) {
                    double viErr = ((Number)viErrObj).doubleValue();
                    if (viErr > 0.1 && (column == 17 || column == 18)) {
                        highlightRed = true;
                    }
                }
                
                if (highlightRed) {
                    c.setBackground(new Color(255, 200, 200)); // Soft red
                    c.setForeground(Color.RED);
                } else {
                    c.setBackground(isRowSelected(row) ? getSelectionBackground() : getBackground());
                    c.setForeground(isRowSelected(row) ? getSelectionForeground() : getForeground());
                }
                
                return c;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set custom cell renderer for formatting
        table.setDefaultRenderer(Double.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Double) {
                    double d = (Double)value;
                    // Check for 99.999 and 9.999 values (placeholder for NA)
                    if (Math.abs(d - 99.999) < 0.001 || Math.abs(d - 9.999) < 0.001) {
                        value = "NA";
                    } else if (Math.abs(d) < 0.001) {
                        // Display 0.000 as NA (typically error fields with no data)
                        value = "NA";
                    } else if (column == 3) {  // RA seconds - 3 decimal places
                        value = String.format(java.util.Locale.US, "%.3f", d);
                    } else if (column == 6) {  // Dec seconds - 1 decimal place
                        value = String.format(java.util.Locale.US, "%.1f", d);
                    } else {  // Other double values - 3 decimal places
                        value = String.format(java.util.Locale.US, "%.3f", d);
                    }
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        
        // Add renderer for Object.class to handle color index columns that can be String or Double
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                // Color index columns (B-V, U-B, V-R, R-I, V-I) are now Object type
                if (value instanceof Double) {
                    double d = (Double)value;
                    // Check for sentinel values
                    if (Math.abs(d - 99.999) < 0.001 || Math.abs(d - 9.999) < 0.001) {
                        value = "NA";
                    } else if (Math.abs(d) < 0.001) {
                        // Display 0.000 as NA (typically error fields with no data)
                        value = "NA";
                    } else {
                        value = String.format(java.util.Locale.US, "%.3f", d);
                    }
                } else if (value instanceof String && value.equals("NA")) {
                    // Already "NA" string from table model
                    value = "NA";
                } else if (value instanceof Integer) {
                    // Integer columns (RA/Dec components, source)
                    value = value.toString();
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // #Comp
        for (int i = 1; i <= 6; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(60); // RA/Dec components
        }
        for (int i = 7; i < 19; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(60); // Magnitudes and errors
        }
        table.getColumnModel().getColumn(19).setPreferredWidth(60);  // Source
        table.getColumnModel().getColumn(20).setPreferredWidth(200); // Comments
        
        // Make only the Comments column editable
        table.setDefaultEditor(Object.class, null); // Disable all editing by default
        table.getColumnModel().getColumn(20).setCellEditor(new DefaultCellEditor(new JTextField()));
        
        // Add keyboard support for Delete key (Mac compatibility)
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Handle Delete key (VK_DELETE) and Backspace (VK_BACK_SPACE) for Mac compatibility
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        // Only delete if not editing a cell
                        if (!table.isEditing()) {
                            deleteSelectedRow();
                        }
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        deleteButton = new JButton("Delete Selected Row");
        deleteButton.setToolTipText("Remove the selected star from the sequence");
        deleteButton.addActionListener(e -> deleteSelectedRow());
        deleteButton.setEnabled(false);
        
        exportButton = new JButton("Export VSD Files");
        exportButton.setToolTipText("Export sequence in VSD formats to the filepath at the top of screen");
        exportButton.addActionListener(e -> exportSequence());
        
        submitButton = new JButton("Submit to VSD");
        submitButton.setToolTipText("Submit the sequence directly to VSD");
        submitButton.setBackground(new Color(255, 200, 150)); // Light orange
        submitButton.setOpaque(true);
        submitButton.addActionListener(e -> submitToVSD());
        
        closeButton = new JButton("Close");
        closeButton.setToolTipText("Close window - this will clear the sequence");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(deleteButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Enable/disable delete button based on selection
        table.getSelectionModel().addListSelectionListener(e -> {
            deleteButton.setEnabled(table.getSelectedRow() >= 0);
        });
    }
    
    private void browsePath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Export Directory");
        
        // Set current path as starting directory
        String currentPath = exportPathField.getText();
        if (currentPath != null && !currentPath.isEmpty()) {
            java.io.File currentDir = new java.io.File(currentPath);
            if (currentDir.exists()) {
                fileChooser.setCurrentDirectory(currentDir);
            } else if (currentDir.getParentFile() != null && currentDir.getParentFile().exists()) {
                fileChooser.setCurrentDirectory(currentDir.getParentFile());
            }
        }
        
        int result = fileChooser.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedDir = fileChooser.getSelectedFile();
            exportPathField.setText(selectedDir.getAbsolutePath());
        }
    }
    
    /**
     * Load VSD comparison star file and populate the table
     */
    private void loadVSDCompFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Select VSD Comparison Star File");
        
        // Set file filter for sequence files
        javax.swing.filechooser.FileNameExtensionFilter filter = 
            new javax.swing.filechooser.FileNameExtensionFilter("VSD Sequence Files (*_seq_1.txt, *_seq_2.txt, *_seq_3.txt)", "txt");
        fileChooser.setFileFilter(filter);
        
        // Start in VSDseqs directory if it exists
        java.io.File vsdDir = new java.io.File("VSDseqs");
        if (vsdDir.exists() && vsdDir.isDirectory()) {
            fileChooser.setCurrentDirectory(vsdDir);
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            try {
                loadVSDFile(selectedFile);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error loading VSD file: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Parse and load VSD file contents into the table
     */
    private void loadVSDFile(java.io.File file) throws Exception {
        // Validate file name format and star name match
        String fileName = file.getName();
        String currentTarget = db.getStar();
        
        // Check if file follows the expected naming pattern
        if (!fileName.matches(".*_seq_[123]\\.txt$")) {
            throw new Exception("File does not follow VSD sequence naming pattern (*_seq_1.txt, *_seq_2.txt, or *_seq_3.txt)");
        }
        
        // Extract star name from filename and compare with current target
        String fileStarName = fileName.replaceAll("_seq_[123]\\.txt$", "").replace("_", " ");
        if (!fileStarName.equalsIgnoreCase(currentTarget.trim())) {
            String message = "VSD seq file appears to be for a different target\n" +
                            "File target: " + fileStarName + "\n" +
                            "Current target: " + currentTarget;
            
            int confirmation = JOptionPane.showConfirmDialog(this,
                message,
                "Target Mismatch",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null);
                
            if (confirmation != JOptionPane.YES_OPTION) {
                return; // User clicked "Close" (No)
            }
        }
        
        // Clear existing entries
        tableModel.clearEntries();
        
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
        String line;
        String vsdType = "VSDadmin3"; // Default
        int loadedCount = 0;
        
        // First pass: determine VSD format type
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#TYPE=")) {
                vsdType = line.substring(6).trim();
                break;
            }
        }
        reader.close();
        
        // Second pass: parse data based on format
        reader = new java.io.BufferedReader(new java.io.FileReader(file));
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            // Skip comment lines
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }
            
            // Parse data line based on VSD format
            try {
                SequenceEntry entry = parseVSDLine(line, vsdType);
                if (entry != null) {
                    tableModel.addEntry(entry);
                    loadedCount++;
                }
            } catch (Exception e) {
                System.err.println("Error parsing line: " + line + " - " + e.getMessage());
                // Continue with next line
            }
        }
        
        reader.close();
        
        // Show success message
        JOptionPane.showMessageDialog(this,
            "Successfully loaded " + loadedCount + " comparison stars from:\n" + 
            file.getName() + " (" + vsdType + " format)",
            "VSD File Loaded",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Parse a single VSD data line based on the format type
     */
    private SequenceEntry parseVSDLine(String line, String vsdType) throws Exception {
        String[] parts = line.split(",");
        if (parts.length < 8) {
            throw new Exception("Insufficient data fields");
        }
        
        SequenceEntry entry = new SequenceEntry();
        
        // Common fields for all formats
        entry.label = parts[0].trim();
        entry.raHH = Integer.parseInt(parts[1].trim());
        entry.raMM = Integer.parseInt(parts[2].trim());
        entry.raSS = Double.parseDouble(parts[3].trim());
        entry.decDD = Integer.parseInt(parts[4].trim());
        entry.decMM = Integer.parseInt(parts[5].trim());
        entry.decSS = Double.parseDouble(parts[6].trim());
        
        // Initialize default values
        entry.v = 99.999;
        entry.vErr = 99.999;
        entry.bv = 99.999;
        entry.bvErr = 99.999;
        entry.ub = 99.999;
        entry.ubErr = 99.999;
        entry.vr = 99.999;
        entry.vrErr = 99.999;
        entry.ri = 99.999;
        entry.riErr = 99.999;
        entry.vi = 99.999;
        entry.viErr = 99.999;
        entry.source = 29; // Default
        entry.comments = "";
        
        if ("VSDadmin1".equals(vsdType)) {
            // VSDadmin1 format: Label,RA h,RA m,RA s,Dec d,Dec m,Dec s,V,Verr,B-V,B-Verr,U-B,U-Berr,V-R,V-Rerr,R-I,R-Ierr,V-I,V-Ierr,Source,# Comments
            if (parts.length >= 20) {
                entry.v = parseDouble(parts[7]);
                entry.vErr = parseDouble(parts[8]);
                entry.bv = parseDouble(parts[9]);
                entry.bvErr = parseDouble(parts[10]);
                entry.ub = parseDouble(parts[11]);
                entry.ubErr = parseDouble(parts[12]);
                entry.vr = parseDouble(parts[13]);
                entry.vrErr = parseDouble(parts[14]);
                entry.ri = parseDouble(parts[15]);
                entry.riErr = parseDouble(parts[16]);
                entry.vi = parseDouble(parts[17]);
                entry.viErr = parseDouble(parts[18]);
                entry.source = Integer.parseInt(parts[19].trim());
                if (parts.length > 20) {
                    entry.comments = parts[parts.length - 1].trim().replaceAll("^#+\\s*", "");
                }
            }
        } else if ("VSDadmin2".equals(vsdType)) {
            // VSDadmin2 format: Label,RA h,RA m,RA s,Dec d,Dec m,Dec s,source,[filter triplets]
            if (parts.length >= 8) {
                entry.source = Integer.parseInt(parts[7].trim());
                
                // Parse filter triplets: filter_name,magnitude,error,...
                for (int i = 8; i < parts.length - 2; i += 3) {
                    if (i + 2 < parts.length) {
                        String filter = parts[i].trim();
                        double mag = parseDouble(parts[i + 1]);
                        double err = parseDouble(parts[i + 2]);
                        
                        if ("V".equals(filter)) {
                            entry.v = mag;
                            entry.vErr = err;
                        } else if ("B".equals(filter)) {
                            // Calculate B-V if we have both B and V
                            if (entry.v != 99.999 && mag != 99.999) {
                                entry.bv = mag - entry.v;
                                if (entry.vErr != 99.999 && err != 99.999) {
                                    entry.bvErr = Math.sqrt(err * err + entry.vErr * entry.vErr);
                                }
                            }
                        }
                    }
                }
            }
        } else { // VSDadmin3 or default
            // VSDadmin3 format: Label,RA h,RA m,RA s,Dec d,Dec m,Dec s,source,B-V,U mag,U err,B mag,B err,V mag,V err,R mag,R err,I mag,I err,SU mag,SU err,SG mag,SG err,SR mag,SR err,SI mag,SI err,SZ mag,SZ err,Y mag,Y err,# Comments
            if (parts.length >= 31) {
                entry.source = Integer.parseInt(parts[7].trim());
                entry.bv = parseDouble(parts[8]); // B-V provided directly
                // U mag (9), U err (10) - skip for now
                // B mag (11), B err (12) - can use for B-V calculation if needed
                entry.v = parseDouble(parts[13]); // V mag
                entry.vErr = parseDouble(parts[14]); // V err
                // Additional bands available but not stored in current table structure
                
                if (parts.length > 31) {
                    entry.comments = parts[parts.length - 1].trim().replaceAll("^#+\\s*", "");
                }
                
                // If B-V wasn't provided or is NA, try to calculate from B and V
                if (entry.bv == 99.999 && parts.length > 12) {
                    double bMag = parseDouble(parts[11]);
                    double bErr = parseDouble(parts[12]);
                    if (bMag != 99.999 && entry.v != 99.999) {
                        entry.bv = bMag - entry.v;
                        if (bErr != 99.999 && entry.vErr != 99.999) {
                            entry.bvErr = Math.sqrt(bErr * bErr + entry.vErr * entry.vErr);
                        }
                    }
                }
            }
        }
        
        return entry;
    }
    
    /**
     * Parse double value, handling "NA" strings
     */
    private double parseDouble(String str) {
        str = str.trim();
        if ("NA".equals(str) || str.isEmpty()) {
            return 99.999;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 99.999;
        }
    }
    
    /**
     * Add a star to the sequence list
     */
    public void addStar(int starIndex) {
        // Check for preferred catalog based on transition magnitude
        AAVSOtools.DataConnector.CatalogEntry preferred = db.getPreferredCatalogEntry(starIndex);
        
        double ra, dec, vmag, bMinusV, uMinusB, vMinusR, rMinusI, vMinusI;
        double vErr, bvErr, ubErr, vrErr, riErr, viErr;
        int source;
        
        if (preferred != null) {
            // Use preferred catalog data
            ra = preferred.ra;
            dec = preferred.dec;
            vmag = preferred.vmag;
            vErr = preferred.ev;
            source = preferred.source;
            
            // Get color indices from preferred catalog
            // PanSTARRS and Gaia now store both B-V and V-I in their proper fields
            bMinusV = preferred.bMinusV;
            bvErr = preferred.ebv;
            vMinusR = preferred.vMinusR;
            vrErr = preferred.evr;
            rMinusI = preferred.rMinusI;
            riErr = preferred.eri;
            vMinusI = preferred.vMinusI;
            viErr = preferred.evi;
            
            // U-B not available in preferred catalog
            uMinusB = 99.999;
            ubErr = 99.999;
            
            System.out.println("DEBUG: Adding star to sequence list using preferred catalog (source " + source + ")");
        } else {
            // Use primary catalog data
            ra = db.getRa(starIndex);
            dec = db.getDec(starIndex);
            vmag = db.getVmag(starIndex);
            source = db.getSource(starIndex);
            
            vErr = db.getEv(starIndex);
            
            // Get color indices - PanSTARRS and Gaia now store both B-V and V-I properly
            bMinusV = db.getBMinusV(starIndex);
            bvErr = db.getEbv(starIndex);
            vMinusI = db.getVMinusI(starIndex);
            viErr = db.getEvi(starIndex);
            
            uMinusB = db.getUMinusB(starIndex);
            vMinusR = db.getVMinusR(starIndex);
            rMinusI = db.getRMinusI(starIndex);
            
            ubErr = db.getEub(starIndex);
            vrErr = db.getEvr(starIndex);
            riErr = db.getEri(starIndex);
        }
        
        // Calculate #Comp label from V magnitude: int(round(mag*10))
        int comp = (int)Math.round(vmag * 10);
        String label = String.valueOf(comp);
        
        // Format RA (degrees to HH:MM:SS.ss)
        double raHours = ra / 15.0;
        int raHH = (int)raHours;
        double raMinutes = (raHours - raHH) * 60.0;
        int raMM = (int)raMinutes;
        double raSS = (raMinutes - raMM) * 60.0;
        
        // Format Dec (degrees to DD:MM:SS.s)
        boolean isNegative = dec < 0;
        double absDec = Math.abs(dec);
        int decDD = (int)absDec;
        if (isNegative) decDD = -decDD;
        double decMinutes = (absDec - Math.abs(decDD)) * 60.0;
        int decMM = (int)decMinutes;
        double decSS = (decMinutes - decMM) * 60.0;
        
        // Create sequence entry
        SequenceEntry entry = new SequenceEntry();
        entry.label = label;
        entry.raHH = raHH;
        entry.raMM = raMM;
        entry.raSS = raSS;
        entry.decDD = decDD;
        entry.decMM = decMM;
        entry.decSS = decSS;
        entry.v = vmag;
        entry.vErr = (vErr < 99.0) ? vErr : Double.NaN;
        entry.bv = bMinusV;
        entry.bvErr = (bvErr < 99.0) ? bvErr : Double.NaN;
        entry.ub = uMinusB;
        entry.ubErr = (ubErr < 99.0) ? ubErr : Double.NaN;
        entry.vr = vMinusR;
        entry.vrErr = (vrErr < 99.0) ? vrErr : Double.NaN;
        entry.ri = rMinusI;
        entry.riErr = (riErr < 99.0) ? riErr : Double.NaN;
        entry.vi = vMinusI;
        entry.viErr = (viErr < 99.0) ? viErr : Double.NaN;
        entry.source = source;
        
        // Apply offset correction if enabled and this is from a deep catalog
        boolean isDeepCatalog = (source == 46 || source == 48 || source == 49); // PanSTARRS, Gaia DR2, Gaia DR3
        if (db.getOffsetCorrectionEnabled() && isDeepCatalog) {
            double correction = db.getOffsetCorrectionDeltaV();
            entry.v -= correction; // Subtract the offset to correct the magnitude
            System.out.println(String.format(java.util.Locale.US, "DEBUG: Applied offset correction of %.3f to star from source %d (V=%.3f -> V=%.3f)",
                correction, source, vmag, entry.v));
        }
        
        // Build comments
        StringBuilder comments = new StringBuilder();
        
        // Add Gaia comment if applicable
        if (source == 48 || source == 49) {  // 48 = Gaia DR2, 49 = Gaia DR3
            comments.append("B is problematic from Gaia");
        }
        
        // Add offset correction comment if applicable
        if (db.getOffsetCorrectionEnabled() && isDeepCatalog) {
            if (comments.length() > 0) {
                comments.append("; ");
            }
            comments.append(db.getOffsetCorrectionComment());
        }
        
        entry.comments = comments.toString();
        
        tableModel.addEntry(entry);
    }
    
    private void deleteSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            tableModel.removeRow(selectedRow);
        }
    }
    
    private void exportSequence() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No stars in sequence list to export.",
                "Empty List",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get target name and coordinates from DataConnector
        String targetName = db.getStar();
        double ra = db.getCenterRa();
        double dec = db.getCenterDec();
        double fov = db.getFieldSize() * 60.0; // Convert from degrees to arcminutes
        
        // Determine limiting magnitude from brightest and faintest in list
        double limitingMag = 14.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double vmag = (double)tableModel.getValueAt(i, 7);
            if (vmag > limitingMag) limitingMag = vmag;
        }
        limitingMag = Math.ceil(limitingMag);
        
        // Calculate field boundaries for db call URL
        double fovDeg = fov / 60.0;
        double fromRA = ra - fovDeg / Math.cos(Math.toRadians(dec));
        double toRA = ra + fovDeg / Math.cos(Math.toRadians(dec));
        double fromDec = dec - fovDeg;
        double toDec = dec + fovDeg;
        
        // Get export directory and create if needed
        String exportPath = exportPathField.getText();
        java.io.File exportDir = new java.io.File(exportPath);
        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                JOptionPane.showMessageDialog(this,
                    "Failed to create export directory: " + exportPath,
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        try {
            // Export all three formats
            exportVSDAdmin1(exportPath, targetName, ra, dec, fov, limitingMag, fromRA, toRA, fromDec, toDec);
            exportVSDAdmin2(exportPath, targetName, ra, dec, fov, limitingMag, fromRA, toRA, fromDec, toDec);
            exportVSDAdmin3(exportPath, targetName, ra, dec, fov, limitingMag, fromRA, toRA, fromDec, toDec);
            
            JOptionPane.showMessageDialog(this,
                "Exported 3 VSD format files to:\n" + exportPath + "\n\n" +
                targetName.replace(" ", "_") + "_seq_1.txt (VSDadmin1)\n" +
                targetName.replace(" ", "_") + "_seq_2.txt (VSDadmin2)\n" +
                targetName.replace(" ", "_") + "_seq_3.txt (VSDadmin3)",
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error exporting files: " + ex.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void exportVSDAdmin1(String exportPath, String targetName, double ra, double dec, double fov, 
                                  double limitingMag, double fromRA, double toRA, 
                                  double fromDec, double toDec) throws Exception {
        String filename = exportPath + java.io.File.separator + targetName.replace(" ", "_") + "_seq_1.txt";
        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename));
        
        // Write header
        writer.println("#TYPE=VSDadmin1");
        writer.printf(java.util.Locale.US, "#Data requested for: %s RA: %.5f DEC: %.5f Field size: %.0f Limiting mag: %.1f%n",
            targetName, ra, dec, fov, limitingMag);
        writer.printf(java.util.Locale.US, "# db call:, https://www.aavso.org/vsx/index.php?view=api.calib&fromra=%.6f&tora=%.6f&fromdec=%.6f&todec=%.6f&tomag=%.1f&source=29&limit=5000%n",
            fromRA, toRA, fromDec, toDec, limitingMag);
        writer.printf(java.util.Locale.US, "# chart:, https://www.aavso.org/apps/vsp/chart/?title=%s&ra=%.5f&dec=%.5f&fov=%.0f&maglimit=%.1f%n",
            targetName.replace(" ", "+"), ra, dec, fov, limitingMag);
        writer.printf(java.util.Locale.US, "# photometry:, https://www.aavso.org/apps/vsp/photometry/?title=%s&ra=%.5f&dec=%.5f&fov=%.0f&maglimit=%.1f&all=on%n",
            targetName.replace(" ", "+"), ra, dec, fov, limitingMag);
        writer.printf(java.util.Locale.US, "#TARGET=%s%n", targetName);
        writer.println("#Label,RA h,RA m,RA s,Dec d,Dec m,Dec s,V,Verr,B-V,B-Verr,U-B,U-Berr,V-R,V-Rerr,R-I,R-Ierr,V-I,V-Ierr,Source,# Comments");
        
        // Write data rows
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String label = (String)tableModel.getValueAt(i, 0);
            int raHH = ((Number)tableModel.getValueAt(i, 1)).intValue();
            int raMM = ((Number)tableModel.getValueAt(i, 2)).intValue();
            double raSS = ((Number)tableModel.getValueAt(i, 3)).doubleValue();
            int decDD = ((Number)tableModel.getValueAt(i, 4)).intValue();
            int decMM = ((Number)tableModel.getValueAt(i, 5)).intValue();
            double decSS = ((Number)tableModel.getValueAt(i, 6)).doubleValue();
            double v = ((Number)tableModel.getValueAt(i, 7)).doubleValue();
            Object vErrObj = tableModel.getValueAt(i, 8);
            
            // Handle color indices that may be "NA" strings or Numbers
            Object bvObj = tableModel.getValueAt(i, 9);
            double bv = (bvObj instanceof String) ? 99.999 : ((Number)bvObj).doubleValue();
            Object bvErrObj = tableModel.getValueAt(i, 10);
            Object ubObj = tableModel.getValueAt(i, 11);
            double ub = (ubObj instanceof String) ? 99.999 : (ubObj != null ? ((Number)ubObj).doubleValue() : Double.NaN);
            Object ubErrObj = tableModel.getValueAt(i, 12);
            Object vrObj = tableModel.getValueAt(i, 13);
            double vr = (vrObj instanceof String) ? 99.999 : (vrObj != null ? ((Number)vrObj).doubleValue() : Double.NaN);
            Object vrErrObj = tableModel.getValueAt(i, 14);
            Object riObj = tableModel.getValueAt(i, 15);
            double ri = (riObj instanceof String) ? 99.999 : (riObj != null ? ((Number)riObj).doubleValue() : Double.NaN);
            Object riErrObj = tableModel.getValueAt(i, 16);
            Object viObj = tableModel.getValueAt(i, 17);
            double vi = (viObj instanceof String) ? 99.999 : (viObj != null ? ((Number)viObj).doubleValue() : Double.NaN);
            Object viErrObj = tableModel.getValueAt(i, 18);
            int source = ((Number)tableModel.getValueAt(i, 19)).intValue();
            String comments = (String)tableModel.getValueAt(i, 20);
            
            double vErr = vErrObj != null ? ((Number)vErrObj).doubleValue() : 0.0;
            double bvErr = bvErrObj != null ? ((Number)bvErrObj).doubleValue() : 0.0;
            double ubErr = ubErrObj != null ? ((Number)ubErrObj).doubleValue() : 0.0;
            double vrErr = vrErrObj != null ? ((Number)vrErrObj).doubleValue() : 0.0;
            double riErr = riErrObj != null ? ((Number)riErrObj).doubleValue() : 0.0;
            double viErr = viErrObj != null ? ((Number)viErrObj).doubleValue() : 0.0;
            
            // Format the line with proper handling of NA values (replace 99.999 and 9.999 with "NA")
            String bvStr = (bv >= 9.999 || bv <= -9.999) ? "NA" : String.format(java.util.Locale.US, "%.3f", bv);
            String bvErrStr = (bvErr >= 9.999 || bvErr <= -9.999 || bvErr == 0.0) ? "NA" : String.format(java.util.Locale.US, "%.3f", bvErr);
            String ubStr = (Double.isNaN(ub) || ub >= 9.999 || ub <= -9.999) ? "NA" : String.format(java.util.Locale.US, "%.3f", ub);
            String ubErrStr = (Double.isNaN(ubErr) || ubErr >= 9.999 || ubErr <= -9.999 || ubErr == 0.0) ? "NA" : String.format(java.util.Locale.US, "%.3f", ubErr);
            String vrStr = (Double.isNaN(vr) || vr >= 9.999 || vr <= -9.999) ? "NA" : String.format(java.util.Locale.US, "%.3f", vr);
            String vrErrStr = (Double.isNaN(vrErr) || vrErr >= 9.999 || vrErr <= -9.999 || vrErr == 0.0) ? "NA" : String.format(java.util.Locale.US, "%.3f", vrErr);
            String riStr = (Double.isNaN(ri) || ri >= 9.999 || ri <= -9.999) ? "NA" : String.format(java.util.Locale.US, "%.3f", ri);
            String riErrStr = (Double.isNaN(riErr) || riErr >= 9.999 || riErr <= -9.999 || riErr == 0.0) ? "NA" : String.format(java.util.Locale.US, "%.3f", riErr);
            String viStr = (Double.isNaN(vi) || vi >= 9.999 || vi <= -9.999) ? "NA" : String.format(java.util.Locale.US, "%.3f", vi);
            String viErrStr = (Double.isNaN(viErr) || viErr >= 9.999 || viErr <= -9.999 || viErr == 0.0) ? "NA" : String.format(java.util.Locale.US, "%.3f", viErr);
            
            writer.printf(java.util.Locale.US, "%s,%d,%d,%.2f,%d,%d,%.1f,%.3f,%.3f,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,# %s%n",
                label, raHH, raMM, raSS, decDD, decMM, decSS,
                v, vErr,
                bvStr, bvErrStr,
                ubStr, ubErrStr,
                vrStr, vrErrStr,
                riStr, riErrStr,
                viStr, viErrStr,
                source, comments);
        }
        
        writer.close();
    }
    
    private void exportVSDAdmin2(String exportPath, String targetName, double ra, double dec, double fov, 
                                  double limitingMag, double fromRA, double toRA, 
                                  double fromDec, double toDec) throws Exception {
        String filename = exportPath + java.io.File.separator + targetName.replace(" ", "_") + "_seq_2.txt";
        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename));
        
        // Write header
        writer.println("#TYPE=VSDadmin2");
        writer.printf(java.util.Locale.US, "#Data requested for: %s RA: %.5f DEC: %.5f Field size: %.0f Limiting mag: %.1f%n",
            targetName, ra, dec, fov, limitingMag);
        writer.printf(java.util.Locale.US, "# db call:, https://www.aavso.org/vsx/index.php?view=api.calib&fromra=%.6f&tora=%.6f&fromdec=%.6f&todec=%.6f&tomag=%.1f&source=29&limit=5000%n",
            fromRA, toRA, fromDec, toDec, limitingMag);
        writer.printf(java.util.Locale.US, "# chart:, https://www.aavso.org/apps/vsp/chart/?title=%s&ra=%.5f&dec=%.5f&fov=%.0f&maglimit=%.1f%n",
            targetName.replace(" ", "+"), ra, dec, fov, limitingMag);
        writer.printf(java.util.Locale.US, "# photometry:, https://www.aavso.org/apps/vsp/photometry/?title=%s&ra=%.5f&dec=%.5f&fov=%.0f&maglimit=%.1f&all=on%n",
            targetName.replace(" ", "+"), ra, dec, fov, limitingMag);
        writer.printf(java.util.Locale.US, "#TARGET=%s%n", targetName);
        writer.println("#Label,RA h,RA m,RA s,Dec d,Dec m,Dec s,source,[filter triplets with name, mag, err]");
        
        // Write data rows - need to calculate magnitudes from colors
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String label = (String)tableModel.getValueAt(i, 0);
            int raHH = ((Number)tableModel.getValueAt(i, 1)).intValue();
            int raMM = ((Number)tableModel.getValueAt(i, 2)).intValue();
            double raSS = ((Number)tableModel.getValueAt(i, 3)).doubleValue();
            int decDD = ((Number)tableModel.getValueAt(i, 4)).intValue();
            int decMM = ((Number)tableModel.getValueAt(i, 5)).intValue();
            double decSS = ((Number)tableModel.getValueAt(i, 6)).doubleValue();
            double v = ((Number)tableModel.getValueAt(i, 7)).doubleValue();
            Object vErrObj = tableModel.getValueAt(i, 8);
            
            // Handle color indices that may be "NA" strings or Numbers
            Object bvObj = tableModel.getValueAt(i, 9);
            double bv = (bvObj instanceof String) ? 99.999 : ((Number)bvObj).doubleValue();
            Object bvErrObj = tableModel.getValueAt(i, 10);
            Object vrObj = tableModel.getValueAt(i, 13);
            double vr = (vrObj instanceof String) ? 99.999 : (vrObj != null ? ((Number)vrObj).doubleValue() : Double.NaN);
            Object vrErrObj = tableModel.getValueAt(i, 14);
            Object riObj = tableModel.getValueAt(i, 15);
            double ri = (riObj instanceof String) ? 99.999 : (riObj != null ? ((Number)riObj).doubleValue() : Double.NaN);
            Object riErrObj = tableModel.getValueAt(i, 16);
            int source = ((Number)tableModel.getValueAt(i, 19)).intValue();
            String comments = (String)tableModel.getValueAt(i, 20);
            
            // Calculate individual magnitudes from V and colors
            double b = (!Double.isNaN(bv) && bv < 9.999) ? v + bv : Double.NaN;
            double r = (!Double.isNaN(vr) && vr < 9.999) ? v - vr : Double.NaN;
            double iMag = (!Double.isNaN(ri) && ri < 9.999 && !Double.isNaN(r)) ? r - ri : Double.NaN;
            
            // Calculate Sloan magnitudes (simplified approximation)
            double sg = !Double.isNaN(bv) ? v + 0.464 * bv - 0.064 : Double.NaN;
            double sr = !Double.isNaN(vr) ? v - vr - 0.339 : Double.NaN;
            double si = !Double.isNaN(ri) && !Double.isNaN(vr) ? v - vr - ri - 0.439 : Double.NaN;
            double sz = !Double.isNaN(si) ? si - 0.01 : Double.NaN;
            
            // Error calculations
            double vErr = vErrObj != null ? ((Number)vErrObj).doubleValue() : 0.0;
            double bvErr = bvErrObj != null ? ((Number)bvErrObj).doubleValue() : 0.0;
            double bErr = bvErrObj != null && !Double.isNaN(bv) ? Math.sqrt(vErr*vErr + bvErr*bvErr) : 0.0;
            double sgErr = !Double.isNaN(sg) ? Math.sqrt(vErr*vErr + 0.464*0.464*(bErr*bErr)) : 0.0;
            double vrErr = vrErrObj != null ? ((Number)vrErrObj).doubleValue() : 0.0;
            double srErr = vrErrObj != null && !Double.isNaN(vr) ? Math.sqrt(vErr*vErr + vrErr*vrErr) : 0.0;
            double riErr = riErrObj != null ? ((Number)riErrObj).doubleValue() : 0.0;
            double siErr = riErrObj != null && !Double.isNaN(si) ? Math.sqrt(srErr*srErr + riErr*riErr) : 0.0;
            double rErr = srErr;
            double iErr = !Double.isNaN(iMag) ? Math.sqrt(rErr*rErr + (riErrObj != null ? riErr*riErr : 0)) : 0.0;
            double szErr = siErr;
            
            writer.printf(java.util.Locale.US, "%s,%d,%d,%.2f,%d,%d,%.1f,%d,",
                label, raHH, raMM, raSS, decDD, decMM, decSS, source);
            
            // Write filter triplets
            writer.printf(java.util.Locale.US, " V,%.3f,%.3f,", v, vErr);
            if (!Double.isNaN(b)) writer.printf(java.util.Locale.US, " B,%.3f,%.3f,", b, bErr);
            if (!Double.isNaN(sg)) writer.printf(java.util.Locale.US, " SG,%.3f,%.3f,", sg, sgErr);
            if (!Double.isNaN(sr)) writer.printf(java.util.Locale.US, " SR,%.3f,%.3f,", sr, srErr);
            if (!Double.isNaN(si)) writer.printf(java.util.Locale.US, " SI,%.3f,%.3f,", si, siErr);
            if (!Double.isNaN(r)) writer.printf(java.util.Locale.US, " R,%.3f,%.3f,", r, rErr);
            if (!Double.isNaN(iMag)) writer.printf(java.util.Locale.US, " I,%.3f,%.3f,", iMag, iErr);
            if (!Double.isNaN(sz)) writer.printf(java.util.Locale.US, " SZ,%.3f,%.3f,", sz, szErr);
            
            writer.printf(java.util.Locale.US, "%n");
        }
        
        writer.close();
    }
    
    private void exportVSDAdmin3(String exportPath, String targetName, double ra, double dec, double fov, 
                                  double limitingMag, double fromRA, double toRA, 
                                  double fromDec, double toDec) throws Exception {
        String filename = exportPath + java.io.File.separator + targetName.replace(" ", "_") + "_seq_3.txt";
        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename));
        
        // Write header
        writer.println("#TYPE=VSDadmin3");
        writer.printf(java.util.Locale.US, "#Data requested for: %s RA: %.5f DEC: %.5f Field size: %.0f Limiting mag: %.1f%n",
            targetName, ra, dec, fov, limitingMag);
        writer.printf(java.util.Locale.US, "# db call:, https://www.aavso.org/vsx/index.php?view=api.calib&fromra=%.6f&tora=%.6f&fromdec=%.6f&todec=%.6f&tomag=%.1f&source=29&limit=5000%n",
            fromRA, toRA, fromDec, toDec, limitingMag);
        writer.printf(java.util.Locale.US, "# chart:, https://www.aavso.org/apps/vsp/chart/?title=%s&ra=%.5f&dec=%.5f&fov=%.0f&maglimit=%.1f%n",
            targetName.replace(" ", "+"), ra, dec, fov, limitingMag);
        writer.printf(java.util.Locale.US, "# photometry:, https://www.aavso.org/apps/vsp/photometry/?title=%s&ra=%.5f&dec=%.5f&fov=%.0f&maglimit=%.1f&all=on%n",
            targetName.replace(" ", "+"), ra, dec, fov, limitingMag);
        writer.printf(java.util.Locale.US, "#TARGET=%s%n", targetName);
        writer.println("#Label,RA h,RA m,RA s,Dec d,Dec m,Dec s,source,B-V,U mag,U err,B mag,B err,V mag,V err,R mag,R err,I mag,I err,SU mag,SU err,SG mag,SG err,SR mag,SR err,SI mag,SI err,SZ mag,SZ err,Y mag,Y err,# Comments");
        
        // Write data rows
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String label = (String)tableModel.getValueAt(i, 0);
            int raHH = ((Number)tableModel.getValueAt(i, 1)).intValue();
            int raMM = ((Number)tableModel.getValueAt(i, 2)).intValue();
            double raSS = ((Number)tableModel.getValueAt(i, 3)).doubleValue();
            int decDD = ((Number)tableModel.getValueAt(i, 4)).intValue();
            int decMM = ((Number)tableModel.getValueAt(i, 5)).intValue();
            double decSS = ((Number)tableModel.getValueAt(i, 6)).doubleValue();
            double v = ((Number)tableModel.getValueAt(i, 7)).doubleValue();
            Object vErrObj = tableModel.getValueAt(i, 8);
            double bv = ((Number)tableModel.getValueAt(i, 9)).doubleValue();
            Object bvErrObj = tableModel.getValueAt(i, 10);
            Object vrObj = tableModel.getValueAt(i, 13);
            double vr = vrObj != null ? ((Number)vrObj).doubleValue() : Double.NaN;
            Object vrErrObj = tableModel.getValueAt(i, 14);
            Object riObj = tableModel.getValueAt(i, 15);
            double ri = riObj != null ? ((Number)riObj).doubleValue() : Double.NaN;
            Object riErrObj = tableModel.getValueAt(i, 16);
            int source = ((Number)tableModel.getValueAt(i, 19)).intValue();
            String comments = (String)tableModel.getValueAt(i, 20);
            
            // Calculate individual magnitudes
            double b = !Double.isNaN(bv) ? v + bv : Double.NaN;
            double r = !Double.isNaN(vr) ? v - vr : Double.NaN;
            double iMag = !Double.isNaN(ri) && !Double.isNaN(r) ? r - ri : Double.NaN;
            double sg = !Double.isNaN(bv) ? v + 0.464 * bv - 0.064 : Double.NaN;
            double sr = !Double.isNaN(vr) ? v - vr - 0.339 : Double.NaN;
            double si = !Double.isNaN(ri) && !Double.isNaN(vr) ? v - vr - ri - 0.439 : Double.NaN;
            double sz = !Double.isNaN(si) ? si - 0.01 : Double.NaN;
            
            // Error calculations
            double vErr = vErrObj != null ? ((Number)vErrObj).doubleValue() : 0.0;
            double bvErr = bvErrObj != null ? ((Number)bvErrObj).doubleValue() : 0.0;
            double bErr = bvErrObj != null && !Double.isNaN(bv) ? Math.sqrt(vErr*vErr + bvErr*bvErr) : 0.0;
            double vrErr = vrErrObj != null ? ((Number)vrErrObj).doubleValue() : 0.0;
            double rErr = vrErrObj != null && !Double.isNaN(vr) ? Math.sqrt(vErr*vErr + vrErr*vrErr) : 0.0;
            double riErr = riErrObj != null ? ((Number)riErrObj).doubleValue() : 0.0;
            double iErr = !Double.isNaN(iMag) && riErrObj != null ? Math.sqrt(rErr*rErr + riErr*riErr) : 0.0;
            double sgErr = !Double.isNaN(sg) && bvErrObj != null ? Math.sqrt(vErr*vErr + 0.464*0.464*bvErr*bvErr) : 0.0;
            double srErr = vrErrObj != null && !Double.isNaN(sr) ? Math.sqrt(vErr*vErr + vrErr*vrErr) : 0.0;
            double siErr = !Double.isNaN(si) && riErrObj != null && vrErrObj != null ? 
                Math.sqrt(vErr*vErr + vrErr*vrErr + riErr*riErr) : 0.0;
            double szErr = siErr;
            
            writer.printf(java.util.Locale.US, "%s,%d,%d,%.2f,%d,%d,%.1f,%d,%.3f,",
                label, raHH, raMM, raSS, decDD, decMM, decSS, source, 
                (!Double.isNaN(bv) ? bv : 0.0));
            
            // U mag and error - typically NA for APASS
            writer.print("NA,NA,");
            
            // B mag and error
            writer.printf(java.util.Locale.US, "%s,%s,", 
                !Double.isNaN(b) ? String.format(java.util.Locale.US, "%.3f", b) : "NA",
                !Double.isNaN(b) && bErr > 0 ? String.format(java.util.Locale.US, "%.3f", bErr) : "NA");
            
            // V mag and error
            writer.printf(java.util.Locale.US, "%.3f,%.3f,", v, vErr);
            
            // R mag and error
            writer.printf(java.util.Locale.US, "%s,%s,", 
                !Double.isNaN(r) ? String.format(java.util.Locale.US, "%.3f", r) : "NA",
                !Double.isNaN(r) && rErr > 0 ? String.format(java.util.Locale.US, "%.3f", rErr) : "NA");
            
            // I mag and error
            writer.printf(java.util.Locale.US, "%s,%s,", 
                !Double.isNaN(iMag) ? String.format(java.util.Locale.US, "%.3f", iMag) : "NA",
                !Double.isNaN(iMag) && iErr > 0 ? String.format(java.util.Locale.US, "%.3f", iErr) : "NA");
            
            // SU mag and error - typically NA
            writer.print("NA,NA,");
            
            // SG mag and error
            writer.printf(java.util.Locale.US, "%s,%s,", 
                !Double.isNaN(sg) ? String.format(java.util.Locale.US, "%.3f", sg) : "NA",
                !Double.isNaN(sg) && sgErr > 0 ? String.format(java.util.Locale.US, "%.3f", sgErr) : "NA");
            
            // SR mag and error
            writer.printf(java.util.Locale.US, "%s,%s,", 
                !Double.isNaN(sr) ? String.format(java.util.Locale.US, "%.3f", sr) : "NA",
                !Double.isNaN(sr) && srErr > 0 ? String.format(java.util.Locale.US, "%.3f", srErr) : "NA");
            
            // SI mag and error
            writer.printf(java.util.Locale.US, "%s,%s,", 
                !Double.isNaN(si) ? String.format(java.util.Locale.US, "%.3f", si) : "NA",
                !Double.isNaN(si) && siErr > 0 ? String.format(java.util.Locale.US, "%.3f", siErr) : "NA");
            
            // SZ mag and error
            writer.printf(java.util.Locale.US, "%s,%s,", 
                !Double.isNaN(sz) ? String.format(java.util.Locale.US, "%.3f", sz) : "NA",
                !Double.isNaN(sz) && szErr > 0 ? String.format(java.util.Locale.US, "%.3f", szErr) : "NA");
            
            // Y mag and error - typically NA
            writer.print("NA,NA,");
            
            // Comments
            writer.printf(java.util.Locale.US, "# %s%n", comments);
        }
        
        writer.close();
    }
    
    /**
     * Submit sequence directly to AAVSO Variable Star Database (VSD)
     */
    private void submitToVSD() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No stars in sequence list to submit.",
                "Empty List",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get target name and construct sequence file path
        String targetName = db.getStar();
        String seqFileName = targetName.replace(" ", "_") + "_seq_3.txt";
        java.io.File seqFile = new java.io.File("VSDseqs" + java.io.File.separator + seqFileName);
        
        // Check if sequence file exists
        if (!seqFile.exists()) {
            JOptionPane.showMessageDialog(this,
                "seq file does not exist",
                "File Not Found",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show confirmation dialog
        String message = "This will place your comps in VSD\nA good comp seq is a source of pride\nOnly proceed if you are sure prosperity will treat you kindly.";
        int confirmation = JOptionPane.showConfirmDialog(this,
            message,
            "Submit to VSD",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null);
            
        if (confirmation != JOptionPane.YES_OPTION) {
            return; // User clicked "Go Back" (No)
        }
        
        // Prompt for VSD credentials
        JPanel credPanel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        
        credPanel.add(new JLabel("VSD Username:"));
        credPanel.add(usernameField);
        credPanel.add(new JLabel("VSD Password:"));
        credPanel.add(passwordField);
        
        int credResult = JOptionPane.showConfirmDialog(this,
            credPanel,
            "VSD Credentials",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
            
        if (credResult != JOptionPane.OK_OPTION) {
            return; // User cancelled
        }
        
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username and password are required.",
                "Invalid Credentials",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Submit file to VSD in background thread
        SwingUtilities.invokeLater(() -> {
            try {
                submitFileToVSD(seqFile, username, password, targetName);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Error submitting to VSD: " + e.getMessage(),
                        "Submission Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }
    
    /**
     * Submit file to VSD using curl command
     */
    private void submitFileToVSD(java.io.File seqFile, String username, String password, String targetName) throws Exception {
        // Build curl command
        ProcessBuilder pb = new ProcessBuilder(
            "curl", "-u", username + ":" + password,
            "-F", "batchFile=@" + seqFile.getAbsolutePath(),
            "-F", "act=add",
            "https://www.aavso.org/vsd_admin/add/index.php"
        );
        
        pb.directory(new java.io.File("."));
        Process process = pb.start();
        
        // Read response
        java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(process.getInputStream())
        );
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        
        SwingUtilities.invokeLater(() -> {
            if (exitCode == 0) {
                // Check for success message in response
                String responseText = response.toString();
                if (responseText.contains("successfully added") || responseText.contains("Updated")) {
                    JOptionPane.showMessageDialog(this,
                        "Sequence successfully submitted to VSD for " + targetName + "!\n" +
                        "File: " + seqFile.getName(),
                        "VSD Submission Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Show partial response for debugging
                    String shortResponse = responseText.length() > 200 ? 
                        responseText.substring(0, 200) + "..." : responseText;
                    JOptionPane.showMessageDialog(this,
                        "Submission completed but response unclear:\n" + shortResponse,
                        "VSD Submission Status Unknown",
                        JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to submit to VSD. Exit code: " + exitCode,
                    "VSD Submission Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Inner class representing a sequence entry
     */
    private static class SequenceEntry {
        String label;  // Changed from 'comp' to 'label' - format "VXX" where XX = int(round(mag*10))
        int raHH, raMM;
        double raSS;
        int decDD, decMM;
        double decSS;
        double v, vErr;
        double bv, bvErr;
        double ub, ubErr;
        double vr, vrErr;
        double ri, riErr;
        double vi, viErr;
        int source;
        String comments;
    }
    
    /**
     * Table model for sequence list
     */
    private class SequenceTableModel extends AbstractTableModel {
        private ArrayList<SequenceEntry> entries = new ArrayList<>();
        
        @Override
        public int getRowCount() {
            return entries.size();
        }
        
        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }
        
        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 1 || column == 2 || column == 4 || column == 5 || column == 19) {
                return Integer.class;
            } else if (column == 0 || column == 20) {
                return String.class;  // Column 0 (Label) is now String, not Integer
            } else if (column == 9 || column == 11 || column == 13 || column == 15 || column == 17) {
                return Object.class;  // Color index columns can be String ("NA") or Double
            }
            return Double.class;
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 20; // Only comments column is editable
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            SequenceEntry entry = entries.get(row);
            
            switch (column) {
                case 0: return entry.label;
                case 1: return entry.raHH;
                case 2: return entry.raMM;
                case 3: return entry.raSS;
                case 4: return entry.decDD;
                case 5: return entry.decMM;
                case 6: return entry.decSS;
                case 7: return entry.v;
                case 8: return Double.isNaN(entry.vErr) ? null : entry.vErr;
                case 9: return (entry.bv >= 9.999 || entry.bv <= -9.999) ? "NA" : entry.bv;
                case 10: return Double.isNaN(entry.bvErr) ? null : entry.bvErr;
                case 11: return (entry.ub >= 9.999 || entry.ub <= -9.999) ? "NA" : entry.ub;
                case 12: return Double.isNaN(entry.ubErr) ? null : entry.ubErr;
                case 13: return (entry.vr >= 9.999 || entry.vr <= -9.999) ? "NA" : entry.vr;
                case 14: return Double.isNaN(entry.vrErr) ? null : entry.vrErr;
                case 15: return (entry.ri >= 9.999 || entry.ri <= -9.999) ? "NA" : entry.ri;
                case 16: return Double.isNaN(entry.riErr) ? null : entry.riErr;
                case 17: return (entry.vi >= 9.999 || entry.vi <= -9.999) ? "NA" : entry.vi;
                case 18: return Double.isNaN(entry.viErr) ? null : entry.viErr;
                case 19: return entry.source;
                case 20: return entry.comments;
                default: return null;
            }
        }
        
        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column == 20) { // Comments column
                entries.get(row).comments = (String)value;
                fireTableCellUpdated(row, column);
            }
        }
        
        public void addEntry(SequenceEntry entry) {
            entries.add(entry);
            sortEntries();
            fireTableDataChanged();
        }
        
        public void removeRow(int row) {
            entries.remove(row);
            fireTableDataChanged();
        }
        
        public void clearEntries() {
            entries.clear();
            fireTableDataChanged();
        }
        
        private void sortEntries() {
            // Sort by numeric value of label (e.g., "83" -> 83)
            Collections.sort(entries, Comparator.comparingInt(e -> {
                try {
                    // Parse entire label string as integer
                    return Integer.parseInt(e.label);
                } catch (Exception ex) {
                    return 0;  // Default if parsing fails
                }
            }));
        }
    }
}

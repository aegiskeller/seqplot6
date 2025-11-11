# Seqplot 6.0.0 Installation Guide

## Quick Installation

1. **Extract the archive** to your desired location
2. **Open a terminal** and navigate to the extracted directory
3. **Make scripts executable:**
   ```bash
   chmod +x *.sh
   ```
4. **Build the application:**
   ```bash
   ./build.sh
   ```
5. **Launch Seqplot:**
   ```bash
   ./run.sh
   ```

## Detailed Setup

### Prerequisites
- Java Runtime Environment (JRE) 8 or higher
- Terminal/Command prompt access

### Build Process
The `build.sh` script will:
- Clean any previous builds
- Compile all source files with proper classpath
- Generate optimized bytecode
- Validate all enhancements are included

### Running the Application
The `run.sh` script will:
- Check for compiled classes
- Auto-build if needed
- Launch with correct classpath
- Display feature status

### Backup Management
Create rollback points for safety:
```bash
./rollback.sh create    # Create backup
./rollback.sh list      # List backups
./rollback.sh restore <backup_name>  # Restore
```

### Troubleshooting

**Build Issues:**
- Ensure JDK (not just JRE) is installed for compilation
- Check that lib/ directory contains JFreeChart libraries

**Runtime Issues:**
- Verify Java version: `java -version`
- Ensure display supports color graphics

**Permission Issues:**
- Run: `chmod +x *.sh` to make scripts executable

### File Structure
```
seqplot-v6.0.0/
├── src/AAVSOtools/     # Enhanced source code
├── lib/                # Required libraries
├── build.sh           # Build script
├── run.sh             # Launch script
├── backup.sh          # Backup utility
├── rollback.sh        # Rollback manager
└── README.md          # Documentation
```

### Enhanced Features Verification
After installation, verify these features work:
- Warm pastel star colors (periwinkle, sage, coral, lavender, cream)
- Unified color edges and hover highlights
- Font size menu with 10-20 point validation
- Modern coordinate formatting in plot titles
- Version 6.0.0 in window title and about dialog

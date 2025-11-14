#!/bin/bash

# Seqplot v6.0.0 Distribution Package Creator
# Date: $(date +%Y-%m-%d)

VERSION="6.0.0"
DATE=$(date +%Y%m%d)
DIST_NAME="seqplot-v${VERSION}-${DATE}"
DIST_DIR="${DIST_NAME}"

echo "========================================="
echo "Creating Seqplot Distribution Package"
echo "========================================="

# First, create the executable JAR
echo "Step 1: Creating executable JAR..."
./create_jar.sh
if [ ! -f "Seqplot-${VERSION}.jar" ]; then
    echo "ERROR: Failed to create JAR file!"
    exit 1
fi

# Clean old distribution
if [ -d "$DIST_DIR" ]; then
    echo "Removing old distribution dir..."
    rm -rf "$DIST_DIR"
fi

# Create distribution directory structure
echo "Creating dir structure..."
mkdir -p "$DIST_DIR"
mkdir -p "$DIST_DIR/lib"
mkdir -p "$DIST_DIR/bin"
mkdir -p "$DIST_DIR/src"
mkdir -p "$DIST_DIR/docs"

# Copy the executable JAR (main distribution method)
echo "Copying executable JAR..."
cp "Seqplot-${VERSION}.jar" "$DIST_DIR/"

# Copy compiled classes (backup method)
echo "Copying compiled application..."
if [ -d "build" ]; then
    cp -r build/* "$DIST_DIR/bin/"
else
    echo "ERROR: build directory not found. Run ./build.sh first!"
    exit 1
fi

# Copy libraries (for backup method)
echo "Copying libraries..."
cp lib/*.jar "$DIST_DIR/lib/"

# Copy source code
echo "Copying source code..."
cp -r src/AAVSOtools "$DIST_DIR/src/"

# Copy documentation
echo "Copying documentation..."
cp README*.md "$DIST_DIR/docs/" 2>/dev/null || echo "No README files found"
cp *.md "$DIST_DIR/docs/" 2>/dev/null || true
cp WINDOWS_README.txt "$DIST_DIR/" 2>/dev/null || true
cp START_HERE.txt "$DIST_DIR/" 2>/dev/null || true

# Copy Windows launcher
echo "Copying Windows launcher..."
cp Seqplot.bat "$DIST_DIR/" 2>/dev/null || echo "Warning: Seqplot.bat not found"

# Create run script for Unix/Mac (updated to use JAR)
echo "Creating run script (Unix/Mac)..."
cat > "$DIST_DIR/run.sh" << 'RUNEOF'
#!/bin/bash
# Seqplot v6.0.0 Launch Script

cd "$(dirname "$0")"

# Try to run JAR first
if [ -f "Seqplot-6.0.0.jar" ]; then
    java -jar Seqplot-6.0.0.jar
else
    # Fallback to class files
    java -cp "bin:lib/jcommon-1.0.23.jar:lib/jfreechart-1.0.19.jar" AAVSOtools.Seqplot
fi
RUNEOF

chmod +x "$DIST_DIR/run.sh"

# Create run script for Windows (updated to use JAR)
echo "Creating run script (Windows)..."
cat > "$DIST_DIR/run.bat" << 'BATEOF'
@echo off
REM Seqplot v6.0.0 Launch Script for Windows

cd /d "%~dp0"

REM Try to run JAR first
if exist "Seqplot-6.0.0.jar" (
    java -jar Seqplot-6.0.0.jar
) else (
    REM Fallback to class files
    java -cp "bin;lib\jcommon-1.0.23.jar;lib\jfreechart-1.0.19.jar" AAVSOtools.Seqplot
)
pause
BATEOF

# Create README
echo "Creating README..."
cat > "$DIST_DIR/README.txt" << 'READMEEOF'
========================================
Seqplot v6.0.0 - AAVSO Sequence Plotter
========================================

Date: 2025-11-14

*** EASIEST WAY TO RUN ***
==========================
Just double-click: Seqplot-6.0.0.jar

If that doesn't work, see WINDOWS_README.txt for detailed instructions.

REQUIREMENTS:
-------------
- Java Runtime Environment (JRE) 11 or higher
- Internet connection for catalog queries

INSTALLATION:
-------------
1. Extract this archive to a directory of your choice
2. Ensure Java is installed:
   - Run: java -version
   - Should show version 11 or higher
   - If not installed, visit: https://adoptium.net/temurin/releases/

RUNNING SEQPLOT:
----------------
EASIEST:
  Double-click Seqplot-6.0.0.jar

Mac/Linux (alternative):
  ./run.sh

Windows (alternative):
  Double-click Seqplot.bat
  OR
  Double-click run.bat
  OR
  Open Command Prompt and run: java -jar Seqplot-6.0.0.jar

*** For Windows users having trouble, see WINDOWS_README.txt ***

FEATURES:
---------
- Multi-catalog support (APASS, Gaia DR2/DR3, PanSTARRS, SDSS, Tycho-2)
- VSP comparison star overlay with AAVSO photometry
- Interactive sky/points view with DSS2 background
- Export to CSV, TSV, AAVSO WebObs format
- VSX integration for variable star data
- Photometric comparison tools

NEW IN THIS VERSION:
--------------------
- VSP API integration for existing AAVSO comparison stars
- Visual overlay: rounded purple boxes with white labels
- Click VSP stars to view full photometry details
- "Check VSX" button to open star details in browser
- Help menu with Sequence Team Homepage link
- Improved coordinate handling and Y-axis correction
- Easier distribution with executable JAR file
- Only APASS catalog selected by default

GETTING STARTED:
----------------
1. Launch the application (double-click the JAR file)
2. Enter a star name (e.g., "EW Cru") or coordinates
3. Click "Find RA & Dec for Star" if using star name
4. Select catalogs to query (APASS is selected by default)
5. Click "Get Plot" to retrieve data
6. Use Sky View or Points View to visualize
7. Click stars to see details
8. Export data via File menu

SUPPORT:
--------
AAVSO Sequence Team Homepage:
https://www.aavso.org/sequence-team-homepage

AAVSO Update List:
Tools → View AAVSO Sequence Team Update List

CREDITS:
--------
Developed for the AAVSO Sequence Team
Libraries: JFreeChart 1.0.19, JCommon 1.0.23

========================================
READMEEOF

# Create a simple changelog
echo "Creating CHANGELOG..."
cat > "$DIST_DIR/CHANGELOG.txt" << 'CHANGEEOF'
CHANGELOG - Seqplot v6.0.0
==========================

2025-11-08 - Version 6.0.0
--------------------------
New Features:
  * VSP API integration for AAVSO comparison stars
  * Click detection for VSP labels with detailed photometry display
  * "Check VSX" button on Request Star dialog
  * Help menu with Sequence Team Homepage link
  * Automatic maglimit calculation (MinMag + 2) for VSP queries

Improvements:
  * Request Star dialog width increased to 600px
  * VSP labels: white text, 10pt font, rounded corners
  * Coordinate system fixes for tangent plane projection
  * X-axis negation for proper sky orientation
  * More transparent label backgrounds (alpha=150)

Bug Fixes:
  * Fixed Y-axis flip in Points View
  * Corrected VSP star positioning (radians→degrees conversion)
  * Improved coordinate projection accuracy

CHANGEEOF

# Create archive
echo "Creating distribution archive..."
tar -czf "${DIST_NAME}.tar.gz" "$DIST_DIR"

# Also create a zip for Windows users
if command -v zip &> /dev/null; then
    echo "Creating ZIP archive..."
    zip -r "${DIST_NAME}.zip" "$DIST_DIR" > /dev/null
    echo "Created: ${DIST_NAME}.zip"
fi

echo ""
echo "========================================="
echo "Distribution package created successfully!"
echo "========================================="
echo ""
echo "Created files:"
echo "  - ${DIST_NAME}.tar.gz (for Mac/Linux)"
if [ -f "${DIST_NAME}.zip" ]; then
    echo "  - ${DIST_NAME}.zip (for Windows)"
fi
echo ""
echo "Directory contents:"
ls -lh "$DIST_DIR"
echo ""
echo "To test locally:"
echo "  cd $DIST_DIR"
echo "  java -jar Seqplot-${VERSION}.jar"
echo "  OR"
echo "  ./run.sh"
echo ""
echo "Distribution includes:"
echo "  - Seqplot-${VERSION}.jar (all-in-one executable)"
echo "  - Seqplot.bat (Windows launcher with Java checker)"
echo "  - WINDOWS_README.txt (detailed Windows instructions)"
echo "  - README.txt (general instructions)"
echo "  - run.sh / run.bat (alternative launchers)"
echo "  - bin/ and lib/ (backup class files and libraries)"
echo ""
echo "To share with the bros:"
echo "  Send them the .tar.gz (Mac/Linux) or .zip (Windows) file"
echo ""

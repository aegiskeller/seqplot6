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

========================================
Seqplot v6.0.0 - AAVSO Sequence Plotter
========================================

EASY START GUIDE FOR WINDOWS USERS
===================================

STEP 1: Check if you have Java
-------------------------------
Open Command Prompt (press Windows key, type "cmd", press Enter)
Type: java -version
Press Enter

If you see a version number (like "17.0.9" or "21.0.1"), you're good!
If you see an error, go to Step 2.

STEP 2: Install Java (if needed)
---------------------------------
1. Visit: https://adoptium.net/temurin/releases/

2. Download the LATEST VERSION:
   - Operating System: Windows
   - Architecture: x64 (for most PCs)
   - Package Type: JRE (Java Runtime Environment)
   - Click the ".msi" download button

3. Run the installer
   - IMPORTANT: Check "Add to PATH" during installation!
   - Accept all defaults

4. Restart your computer (important!)

STEP 3: Run Seqplot
-------------------
METHOD 1 (Easiest - if you have the JAR file):
   Just double-click: Seqplot-6.0.0.jar

METHOD 2 (Alternative):
   Double-click: Seqplot.bat

METHOD 3 (If double-clicking doesn't work):
   1. Open Command Prompt
   2. Navigate to the Seqplot folder:
      cd C:\path\to\seqplot
   3. Type: java -jar Seqplot-6.0.0.jar
   4. Press Enter

TROUBLESHOOTING
===============

Problem: "java is not recognized as an internal or external command"
Solution: Java is not installed or not in PATH. Follow Step 2 above.

Problem: "Could not find or load main class"
Solution: Make sure you extracted ALL files from the ZIP archive.

Problem: JAR file opens with WinRAR/7-Zip instead of running
Solution: 
   1. Right-click the .jar file
   2. Choose "Open with" → "Java(TM) Platform SE Binary"
   3. Or use Method 3 above to run from command line

Problem: "UnsupportedClassVersionError" or "version 52.0"
Solution: Your Java is too old. You need Java 11 or newer.
   Follow Step 2 to install a newer version.

GETTING STARTED
===============
1. Launch the application (double-click JAR or run Seqplot.bat)
2. Enter a star name (e.g., "EW Cru") or coordinates
3. Click "Find RA & Dec for Star" if using star name
4. Select catalogs to query (APASS is selected by default)
5. Click "Get Plot" to retrieve data
6. Use Sky View or Points View to visualize
7. Export data via File menu

SUPPORT
=======
Stefan Keller: aegiskeller@gmail.com

For Java installation help:
https://adoptium.net/installation/

CREDITS
=======
Developed for the AAVSO Sequence Team
Libraries: JFreeChart 1.0.19, JCommon 1.0.23

========================================

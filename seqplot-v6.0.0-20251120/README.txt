# Seqplot v6.0.0 - Build 20251120

## Quick Start

### Windows Users (Recommended)
Double-click `run-jar.bat` to start Seqplot (uses single JAR file).
Alternatively, double-click `run.bat` (uses class files + libraries).

### Mac/Linux Users
Run `./run.sh` in a terminal.

## Requirements
- Java 11 or higher
- Download from: https://adoptium.net/temurin/releases/

## What's Fixed in This Build (2025-11-20)

### Locale/Internationalization Fixes
- **Fixed**: VSP chart URLs now correctly use periods (`.`) instead of commas (`,`) as decimal separators in all locales (German, French, Italian, etc.)
- **Fixed**: All API URL construction (VSX, DSS2, SDSS, Gaia, PanSTARRS) now works correctly regardless of system locale
- **Fixed**: Number formatting in UI displays consistently uses periods for decimals

### User Experience Improvements  
- **Added**: Warning dialog when limiting magnitude is too bright for the variable star's faintest magnitude
  - Recommends appropriate limiting magnitude based on star's MinMag
  - Prevents "no data found" errors by catching the issue before query
  
- **Fixed**: VSP chart request now uses the value from "Limiting mag" field (or defaults to 14.0 if empty)
  - Previously used complex VSX MinMag-based calculation
  - Now gives users direct control over magnitude limit

### Bug Fixes
- **Fixed**: Crash when clicking Cancel on file save dialogs (NullPointerException)
- **Fixed**: Crash when clicking "Next Star" button in StarPlotPanel mode (mainTitle null reference)

## File Structure
```
seqplot-v6.0.0-20251120/
├── run-jar.bat      # Windows launcher (JAR version - recommended)
├── run.bat          # Windows launcher (class files version)
├── run.sh           # Mac/Linux launcher  
├── Seqplot-6.0.0.jar # Executable JAR with all dependencies
├── bin/             # Compiled Java classes
├── lib/             # Required libraries (JFreeChart, JCommon)
├── src/             # Source code
└── docs/            # Documentation
```

## Running from Source
If you need to rebuild:
```bash
javac -d bin -cp "lib/jcommon-1.0.23.jar;lib/jfreechart-1.0.19.jar" src/AAVSOtools/*.java
```

## Support
For issues or questions, contact the AAVSO development team.

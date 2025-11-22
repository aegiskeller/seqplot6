# Seqplot v6.0.0 - Build 20251120 Distribution Package

## Package Contents

### Main Files
- **seqplot-v6.0.0-20251120.zip** (2.25 MB) - Complete distribution package
- **seqplot-v6.0.0-20251120/** - Uncompressed distribution folder

### What's Inside the Distribution
```
seqplot-v6.0.0-20251120/
├── run.bat              # Windows launcher - just double-click!
├── run.sh               # Mac/Linux launcher
├── README.txt           # Quick start guide
├── CHANGELOG.txt        # Detailed list of fixes and changes
├── bin/                 # Compiled application (52 classes)
│   └── AAVSOtools/      # Java classes
├── lib/                 # Required libraries
│   ├── jcommon-1.0.23.jar
│   └── jfreechart-1.0.19.jar
├── src/                 # Complete source code
│   └── AAVSOtools/      # 16 Java source files
└── docs/                # Documentation files
    ├── README.md
    ├── GAIA_TRANSFORMATIONS.md
    ├── PANSTARRS_TRANSFORMATIONS.md
    └── DISTRIBUTION_SUMMARY.md
```

## Installation Instructions

### For End Users
1. Extract `seqplot-v6.0.0-20251120.zip`
2. **Windows**: Double-click `run.bat`
3. **Mac/Linux**: Run `./run.sh` in terminal

### Requirements
- Java 11 or higher
- Download from: https://adoptium.net/temurin/releases/

## Key Fixes in This Build

### Critical Bug Fixes
✅ **Locale Issue**: Fixed decimal separator bug affecting international users
   - URLs now work correctly in German, French, Italian, and other locales
   - Was showing `maglimit=14,0` instead of `maglimit=14.0`

✅ **Crash Fixes**:
   - File save dialog cancellation no longer crashes
   - "Next Star" button works correctly in all modes

### User Experience Improvements
✅ **Smart Validation**: Warns when limiting magnitude is too bright
✅ **Better Control**: VSP chart uses your specified limiting magnitude

## Testing Performed
- ✅ Application launches successfully
- ✅ All class files present and correctly organized
- ✅ Libraries included and accessible
- ✅ Source code included for reference
- ✅ Documentation complete

## Distribution Stats
- Total files: 81
- Compressed size: 2.25 MB
- Build date: November 20, 2025
- Java package: AAVSOtools

## For Developers
To rebuild from source:
```bash
cd seqplot-v6.0.0-20251120
javac -d bin -cp "lib/jcommon-1.0.23.jar;lib/jfreechart-1.0.19.jar" src/AAVSOtools/*.java
```

## Support
For issues or questions, contact the AAVSO development team.

---
Created: November 20, 2025
Version: 6.0.0 Build 20251120

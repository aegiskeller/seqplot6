# Seqplot 6.0.0 Distribution - November 21, 2025

## Distribution Package Created

**Filename:** `Seqplot-6.0.0-20251121.zip`  
**Size:** 3.1 MB  
**Location:** `/Users/aegiskeller/Documents/seqplot6/dist/`

## Package Contents

```
Seqplot-6.0.0-20251121/
├── Seqplot-6.0.0-20251121.jar    (1.2 MB) - Main application
├── lib/
│   ├── jfreechart-1.0.19.jar     - Charting library
│   └── jcommon-1.0.23.jar        - Common utilities
├── seqplot.sh                     - macOS/Linux launcher
├── seqplot.bat                    - Windows launcher
├── seqplotDefault.ini             - Configuration file
├── README.md                      - Main documentation
├── LOCALE_FIXES.md                - Locale handling details
└── DISTRIBUTION_README.md         - Quick start guide
```

## How to Use

### Extract
```bash
unzip Seqplot-6.0.0-20251121.zip
cd Seqplot-6.0.0-20251121
```

### Run

**macOS/Linux:**
```bash
./seqplot.sh
```

**Windows:**
```
seqplot.bat
```

**Manual (any platform):**
```bash
java -jar Seqplot-6.0.0-20251121.jar
```

## Key Features in This Release

### Locale Safety (Critical Fix)
- ✅ All `System.out.printf()` calls now use `Locale.US`
- ✅ All `System.err.printf()` calls now use `Locale.US`
- ✅ All `String.format()` calls use `Locale.US`
- ✅ All `DecimalFormat` instances use US locale symbols
- ✅ All file exports use consistent formatting

**Impact:** Users in Argentina, Spain, Germany, and other countries that use comma as decimal separator will no longer experience formatting errors.

### Other Fixes
- ✅ VSX magnitude range bug fixed (arrays properly cleared)
- ✅ VSP comparison star display formatting corrected
- ✅ Unicode characters removed for better compatibility

## Requirements

- **Java:** Version 11 or later
- **Download:** https://adoptium.net/

## Verification

The JAR has been tested and confirmed to launch successfully with the bundled libraries.

## Distribution History

- **November 21, 2025** - Comprehensive locale fixes, updated manifest
- **November 20, 2025** - Initial locale fixes for Argentina users
- **November 3, 2025** - Version 6.0.0 release

## Files Generated

1. `Seqplot-6.0.0-20251121.jar` - Standalone JAR in project root
2. `dist/Seqplot-6.0.0-20251121/` - Complete distribution directory
3. `dist/Seqplot-6.0.0-20251121.zip` - Ready-to-distribute archive

## Next Steps

The distribution is ready for:
- Testing on different platforms (macOS, Windows, Linux)
- Distribution to users
- Upload to release repository
- Sending to Argentina user for locale verification

## Technical Notes

- Manifest correctly references `lib/jfreechart-1.0.19.jar` and `lib/jcommon-1.0.23.jar`
- JAR Class-Path is relative to JAR location
- Libraries must be in `lib/` subdirectory relative to JAR
- All launchers include Java version checking

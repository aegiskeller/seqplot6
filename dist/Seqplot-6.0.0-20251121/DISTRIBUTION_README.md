# Seqplot 6.0.0 Distribution
**Release Date:** November 21, 2025

## What's Included

- `Seqplot-6.0.0-20251121.jar` - Main application JAR file
- `lib/` - Required libraries (JFreeChart, JCommon)
- `seqplotDefault.ini` - Default configuration file
- `seqplot.sh` - macOS/Linux launcher script
- `seqplot.bat` - Windows launcher script
- `README.md` - Main documentation
- `LOCALE_FIXES.md` - Locale handling documentation

## Quick Start

### macOS/Linux
```bash
./seqplot.sh
```

### Windows
Double-click `seqplot.bat` or run from command prompt:
```
seqplot.bat
```

### Manual Launch (Any Platform)
```bash
java -jar Seqplot-6.0.0-20251121.jar
```

## Requirements

- Java 11 or later
- Download from: https://adoptium.net/

## Recent Changes (November 21, 2025)

### Comprehensive Locale Fixes
- Fixed all `System.out.printf()` and `System.err.printf()` calls to use `Locale.US`
- Ensures decimal points (.) instead of commas (,) regardless of system locale
- Fixes issues for users in Argentina, Spain, Germany, and other comma-decimal locales
- See `LOCALE_FIXES.md` for complete details

### Previous Fixes
- Fixed VSX magnitude range display (arrays now cleared properly)
- Fixed VSP comparison star formatting
- All String.format() and DecimalFormat operations use Locale.US
- Removed Unicode characters for better compatibility

## Features

- Query multiple astronomical catalogs (APASS9, Gaia DR3, PanSTARRS, SDSS, Tycho-2)
- VSX integration for variable star information
- VSP comparison star overlay
- DSS2 background images with WCS alignment
- Export comparison sequences to VSD format
- Interactive zoom and pan
- Multiple coordinate system support

## Support

For issues or questions, please refer to the main README.md file.

## Version Information

- **Version:** 6.0.0
- **Build Date:** November 21, 2025
- **Locale Safe:** Yes (all numeric formatting uses Locale.US)

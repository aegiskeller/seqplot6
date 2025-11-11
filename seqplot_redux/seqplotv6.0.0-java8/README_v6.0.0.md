# Seqplot 6.0.0 - Enhanced Astronomical Sequence Plotting Application

**Version:** 6.0.0  
**Release Date:** November 3, 2025  
**Status:** Enhanced with warm pastel colors and unified visual styling

## 🎨 Key Enhancements

### Warm Pastel Color Scheme
The application now uses a carefully designed warm pastel color palette that is:
- **Color-blind friendly** - Better accessibility for users with color vision deficiencies
- **Easier on the eyes** - Softer, warmer tones reduce eye strain during long viewing sessions
- **Professionally polished** - Modern astronomical display conventions

**Color Palette:**
- **Series 0 (Blue):** `(135, 170, 230)` - Warm pastel blue (periwinkle)
- **Series 1 (Green):** `(144, 215, 144)` - Warm pastel green (sage green)
- **Series 2 (Red):** `(255, 140, 140)` - Warm pastel red (coral pink)
- **Series 3 (Purple):** `(200, 140, 220)` - Warm pastel purple (lavender)
- **Series 4 (White):** `(250, 245, 235)` - Warm white (cream/ivory)

### Unified Visual Styling
- **Consistent star colors** - Star bodies, edges, and hover highlights now use the same color
- **No more harsh contrasts** - Eliminated jarring white edges and orange hover states
- **Updated legend** - Series names updated from "Yellow" to "Purple" for accuracy

### Enhanced User Interface
- **Improved font size menu** - Validation ensures font sizes between 10-20 points
- **Modern coordinate display** - RA format: `hh:mm:ss.s`, Dec format: `dd:mm:ss` (not "DEC")
- **Version 6.0.0 branding** - Updated window title and about dialog

## 🛠️ Building and Running

### Quick Start
```bash
# Build the application
./build.sh

# Run the application
./run.sh
```

### Manual Build
```bash
# Clean previous builds
rm -rf build/*

# Compile source files
javac -d build -cp "lib/jcommon-1.0.23.jar:lib/jfreechart-1.0.19.jar" src/AAVSOtools/*.java

# Run the application
java -cp "build:lib/jcommon-1.0.23.jar:lib/jfreechart-1.0.19.jar" AAVSOtools.Seqplot
```

## 📁 Project Structure

```
seqplotv6.0.0/
├── src/AAVSOtools/           # Source code (CLEAN - no decompiled artifacts)
│   ├── Seqplot.java         # Main application with 6.0.0 enhancements
│   ├── DataConnector.java   # Data management with coordinate formatting
│   ├── StarPlotPanel.java   # Primary renderer with warm pastel colors
│   ├── SimpleStarPlot.java  # Alternative renderer with unified colors
│   ├── AdvancedStarPlot.java # Secondary renderer with consistent styling
│   └── [other support files]
├── build/                   # Compiled classes (auto-generated)
├── lib/                     # JFreeChart libraries
│   ├── jcommon-1.0.23.jar
│   └── jfreechart-1.0.19.jar
├── build.sh                # Clean build script
├── run.sh                  # Launch script
└── README.md               # This file
```

## 🔧 Technical Details

### Color System Architecture
The application uses a three-tier color management system:
1. **Series Assignment** - Based on B-V color index of stars
2. **Color Mapping** - Warm pastel RGB values defined in multiple components
3. **Rendering** - Unified color application across all plot renderers

### Enhanced Components
- **StarPlotPanel.java** - Primary plotting engine with zoom/pan and interactive features
- **SimpleStarPlot.java** - Lightweight renderer for simple displays
- **AdvancedStarPlot.java** - Feature-rich renderer with advanced visualization
- **DataConnector.java** - Database interface with sexagesimal coordinate formatting
- **Seqplot.java** - Main application framework with enhanced menu system

### Coordinate Formatting
- **RA Format:** `hh:mm:ss.s` (hours:minutes:seconds.tenths)
- **Dec Format:** `dd:mm:ss` (degrees:minutes:seconds)
- **Plot Title:** Uses formatted coordinates for professional display

## 🚀 Features

### User Interface
- Enhanced font size validation (10-20 point range)
- Immediate UI updates when changing settings
- Modern astronomical display conventions
- Professional version 6.0.0 branding

### Plotting Capabilities
- Interactive zoom and pan
- Mouse hover highlighting with unified colors
- Multiple rendering engines for different use cases
- Legend with accurate color representation

### Data Management
- Comprehensive astronomical catalog integration
- Efficient star data processing
- Modern coordinate system support
- Professional output formatting

## 📝 Notes

- **Clean Source Code:** All source files have been cleaned of decompilation artifacts
- **Consistent Colors:** All rendering components use the same warm pastel palette
- **Version Control:** Version 6.0.0 with November 3, 2025 release date
- **Build Scripts:** Automated build and run scripts prevent compilation issues

## 🎯 Quality Assurance

This version has been thoroughly tested to ensure:
- ✅ All warm pastel colors are consistently applied
- ✅ Star edges and hover highlights use body colors
- ✅ Font size validation works properly
- ✅ Coordinate formatting is modern and accurate
- ✅ Version information is updated throughout
- ✅ Build process is clean and repeatable

---

**Contact:** AAVSO Technical Staff  
**Email:** sara@aavso.org, gsilvis@aavso.org, aavso@aavso.org
# Seqplot 6.0.0 Golden Master Release

**Release Date:** November 3, 2025  
**Version:** 6.0.0  
**Status:** Golden Master (Enhanced)

## 🎉 Major Enhancements

### 🎨 Warm Pastel Color Scheme
Revolutionary color-blind friendly palette designed for extended astronomical observation:
- **Series 0:** `(135, 170, 230)` - Warm pastel blue (periwinkle)
- **Series 1:** `(144, 215, 144)` - Warm pastel green (sage green)
- **Series 2:** `(255, 140, 140)` - Warm pastel red (coral pink)
- **Series 3:** `(200, 140, 220)` - Warm pastel purple (lavender)
- **Series 4:** `(250, 245, 235)` - Warm white (cream/ivory)

### 🎯 Unified Visual Styling
- **Consistent star colors** - Star bodies, edges, and hover highlights use identical colors
- **Eliminated harsh contrasts** - No more jarring white edges or orange hover states
- **Professional appearance** - Cohesive visual design throughout all rendering components

### 📝 Enhanced User Interface
- **Improved font size validation** - Restricts to 10-20 point range with immediate feedback
- **Modern coordinate display** - RA format: `hh:mm:ss.s`, Dec format: `dd:mm:ss`
- **Updated version branding** - Professional 6.0.0 identification throughout application
- **Updated legend accuracy** - Changed "Yellow" to "Purple" series name

## 🛠️ Technical Improvements

### Multi-Tier Rendering System
All rendering components updated with consistent warm pastel colors:
- **StarPlotPanel.java** - Primary interactive plotting engine
- **SimpleStarPlot.java** - Lightweight alternative renderer
- **AdvancedStarPlot.java** - Feature-rich visualization renderer

### Enhanced Coordinate Management
- **Sexagesimal formatting** - Professional astronomical coordinate display
- **Plot title integration** - Formatted coordinates in chart headers
- **Modern conventions** - Updated from legacy "DEC" to standard "Dec"

### Build Infrastructure
- **Automated build system** - Clean compilation with `./build.sh`
- **Smart launcher** - Auto-building application runner with `./run.sh`
- **Backup management** - Version control with `./backup.sh`
- **Rollback system** - Restore points with `./rollback.sh`

## 🚀 Quick Start

1. **Extract the release archive**
2. **Build the application:**
   ```bash
   ./build.sh
   ```
3. **Launch Seqplot 6.0.0:**
   ```bash
   ./run.sh
   ```

## 📋 System Requirements

- **Java Runtime Environment** (JRE 8 or higher)
- **Operating System:** macOS, Linux, Windows
- **Memory:** Minimum 512MB RAM
- **Display:** Color display recommended for optimal experience

## 🎯 Quality Assurance

This Golden Master release has been thoroughly tested to ensure:
- ✅ All warm pastel colors consistently applied across all renderers
- ✅ Unified visual styling with same-color edges and hover highlights
- ✅ Enhanced font size validation working correctly
- ✅ Modern coordinate formatting properly implemented
- ✅ Version 6.0.0 branding updated throughout application
- ✅ Clean build process without compilation errors
- ✅ Professional legend display with accurate series names

## 🔄 Rollback Support

This release includes comprehensive rollback management:
- **Create rollback points:** `./rollback.sh create`
- **List restore points:** `./rollback.sh list`
- **Restore previous version:** `./rollback.sh restore <backup_name>`

## 📞 Support

**Contact:** AAVSO Technical Staff  
**Email:** sara@aavso.org, gsilvis@aavso.org, aavso@aavso.org  
**Website:** https://www.aavso.org

---

**Enhanced for astronomical observation with warm, accessible colors and professional polish.**

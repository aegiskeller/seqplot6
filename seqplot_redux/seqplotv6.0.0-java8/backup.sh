#!/bin/bash
# Seqplot 6.0.0 Source Code Backup Script
# Creates a timestamped backup of the enhanced source code

echo "=== Seqplot 6.0.0 Source Backup ==="
echo "Creating backup of enhanced warm pastel version"
echo

# Set the base directory
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$BASE_DIR"

# Create timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="backup_v6.0.0_$TIMESTAMP"

echo "📦 Creating backup: $BACKUP_DIR"

# Create backup directory
mkdir -p "$BACKUP_DIR"

# Copy source files
echo "📁 Copying source files..."
cp -r src/ "$BACKUP_DIR/"

# Copy build scripts
echo "📄 Copying build scripts..."
cp build.sh "$BACKUP_DIR/"
cp run.sh "$BACKUP_DIR/"
cp README_v6.0.0.md "$BACKUP_DIR/"

# Copy libraries
echo "📚 Copying libraries..."
cp -r lib/ "$BACKUP_DIR/"

# Create backup info file
cat > "$BACKUP_DIR/BACKUP_INFO.txt" << EOF
Seqplot 6.0.0 Enhanced Version Backup
=====================================

Backup Date: $(date)
Version: 6.0.0
Release Date: November 3, 2025

Enhanced Features:
- Warm pastel color scheme for accessibility
- Unified star point colors (body, edges, hover)
- Enhanced font size validation (10-20 points)
- Modern coordinate formatting (hh:mm:ss.s and dd:mm:ss)
- Updated version branding and about dialog
- Professional color-blind friendly palette

Files Included:
- src/AAVSOtools/ - All enhanced source code
- lib/ - JFreeChart libraries
- build.sh - Clean compilation script
- run.sh - Application launcher
- README_v6.0.0.md - Comprehensive documentation

To restore this version:
1. Copy src/ directory to your workspace
2. Run ./build.sh to compile
3. Run ./run.sh to launch application

Color Palette:
- Series 0: (135, 170, 230) - Warm pastel blue (periwinkle)
- Series 1: (144, 215, 144) - Warm pastel green (sage green)
- Series 2: (255, 140, 140) - Warm pastel red (coral pink)
- Series 3: (200, 140, 220) - Warm pastel purple (lavender)
- Series 4: (250, 245, 235) - Warm white (cream/ivory)
EOF

echo "✅ Backup created successfully: $BACKUP_DIR"
echo "📋 Backup includes all enhanced source code and documentation"
echo "🎨 Preserves warm pastel color scheme and unified visual styling"
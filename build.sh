#!/bin/bash
# Seqplot 6.0.0 Build Script

echo "=== Seqplot 6.0.0 Build Script ==="
echo "Building Seqplot6 application"
echo

# Set the base directory
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$BASE_DIR"

# Clean previous builds
echo "Cleaning previous builds..."
rm -rf build/*
rm -f *.class
find . -name "*.class" -exec rm -f {} \; 2>/dev/null || true

# Create build directory if it doesn't exist
mkdir -p build

# Set classpath for JFreeChart libraries
CLASSPATH="lib/jcommon-1.0.23.jar:lib/jfreechart-1.0.19.jar"

echo "Compiling source files..."
echo "Source files in src/AAVSOtools/:"
ls -la src/AAVSOtools/*.java

# Compile with verbose output
javac -d build -cp "$CLASSPATH" -verbose src/AAVSOtools/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Copy resource files (icon)
    echo "📁 Copying resource files..."
    mkdir -p build/AAVSOtools
    if [ -f "src/AAVSOtools/seqplot_icon.png" ]; then
        cp src/AAVSOtools/seqplot_icon.png build/AAVSOtools/
        echo "  Copied seqplot_icon.png"
    else
        echo "  seqplot_icon.png not found in src/AAVSOtools/ (sad because it was cool)"
    fi
    
    echo
    echo "Enhanced Features Included:"
    echo "  • Seqplot 6.0.0 with November 3, 2025 release date"
    echo "  • Enhanced font size validation (10-20 point range)"
    echo
    echo "Ready to run with:"
    echo "   java -cp \"build:lib/jcommon-1.0.23.jar:lib/jfreechart-1.0.19.jar\" AAVSOtools.Seqplot"
else
    echo "Compilation failed! Drop Bears have attacked the build process!"
    exit 1
fi
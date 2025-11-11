#!/bin/bash
# Seqplot 6.0.0 Build Script
# Ensures clean compilation with enhanced warm pastel colors and unified styling

echo "=== Seqplot 6.0.0 Build Script ==="
echo "Building enhanced astronomical sequence plotting application"
echo

# Set the base directory
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$BASE_DIR"

# Clean previous builds
echo "🧹 Cleaning previous builds..."
rm -rf build/*
rm -f *.class
find . -name "*.class" -exec rm -f {} \; 2>/dev/null || true

# Create build directory if it doesn't exist
mkdir -p build

# Set classpath for JFreeChart libraries
CLASSPATH="lib/jcommon-1.0.23.jar:lib/jfreechart-1.0.19.jar"

echo "📦 Compiling source files..."
echo "Source files in src/AAVSOtools/:"
ls -la src/AAVSOtools/*.java

# Compile with verbose output
javac -d build -cp "$CLASSPATH" -verbose src/AAVSOtools/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo
    echo "📋 Enhanced Features Included:"
    echo "  • Seqplot 6.0.0 with November 3, 2025 release date"
    echo "  • Warm pastel color scheme (periwinkle, sage, coral, lavender, cream)"
    echo "  • Unified star point colors (body, edges, hover highlights)"
    echo "  • Enhanced font size validation (10-20 point range)"
    echo "  • Modern coordinate formatting (hh:mm:ss.s and dd:mm:ss)"
    echo "  • Updated legend with 'Purple' instead of 'Yellow'"
    echo
    echo "🚀 Ready to run with:"
    echo "   java -cp \"build:lib/jcommon-1.0.23.jar:lib/jfreechart-1.0.19.jar\" AAVSOtools.Seqplot"
else
    echo "❌ Compilation failed!"
    exit 1
fi
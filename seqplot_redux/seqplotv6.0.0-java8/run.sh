#!/bin/bash
# Seqplot 6.0.0 Run Script
# Launches the enhanced astronomical sequence plotting application

echo "=== Launching Seqplot 6.0.0 ==="
echo "Enhanced Astronomical Sequence Plotting Application"
echo "Features: Warm pastel colors, unified visual styling, modern coordinates"
echo

# Set the base directory
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$BASE_DIR"

# Check if compiled classes exist
if [ ! -d "build/AAVSOtools" ]; then
    echo "⚠️  Compiled classes not found. Building first..."
    ./build.sh
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
    echo
fi

# Set classpath for JFreeChart libraries
CLASSPATH="build:lib/jcommon-1.0.23.jar:lib/jfreechart-1.0.19.jar"

echo "🚀 Starting Seqplot 6.0.0..."
java -cp "$CLASSPATH" AAVSOtools.Seqplot

echo "✨ Seqplot session ended"
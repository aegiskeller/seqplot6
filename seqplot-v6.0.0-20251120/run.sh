#!/bin/bash
# Seqplot v6.0.0 Launcher for Unix/Linux/Mac
# Automatically runs with the correct classpath

echo "========================================"
echo "Seqplot v6.0.0 - Build 20251120"
echo "========================================"
echo

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in your PATH!"
    echo
    echo "Seqplot requires Java 11 or higher to run."
    echo
    echo "Please download and install Java from:"
    echo "  https://adoptium.net/temurin/releases/"
    echo
    exit 1
fi

echo "Starting Seqplot..."
echo

# Run Seqplot with the correct classpath
java -cp "bin:lib/jcommon-1.0.23.jar:lib/jfreechart-1.0.19.jar" AAVSOtools.Seqplot

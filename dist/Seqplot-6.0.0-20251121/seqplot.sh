#!/bin/bash
# Seqplot 6.0.0 - macOS/Linux Launcher
# Release: November 21, 2025

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Change to the script directory
cd "$SCRIPT_DIR"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or later to run Seqplot"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F. '{print $1}')
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "Error: Java 11 or later is required"
    echo "Current version: $(java -version 2>&1 | head -n 1)"
    exit 1
fi

# Run Seqplot
echo "Starting Seqplot 6.0.0 (2025-11-21)..."
java -jar Seqplot-6.0.0-20251121.jar "$@"

#!/bin/bash

# Create a runnable JAR file with all dependencies bundled

VERSION="6.0.0"
JAR_NAME="Seqplot-${VERSION}.jar"

echo "========================================="
echo "Creating Seqplot Executable JAR"
echo "========================================="

# Check if build exists
if [ ! -d "build" ]; then
    echo "ERROR: build directory not found. Running build.sh..."
    ./build.sh
fi

# Create temporary directory for JAR contents
TEMP_DIR="temp_jar_build"
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"

# Copy compiled classes
echo "Copying compiled classes..."
cp -r build/* "$TEMP_DIR/"

# Extract library JARs
echo "Extracting library dependencies..."
cd "$TEMP_DIR"
jar xf ../lib/jcommon-1.0.23.jar
jar xf ../lib/jfreechart-1.0.19.jar

# Remove unnecessary META-INF files (but keep our own)
rm -rf META-INF/*.SF META-INF/*.DSA META-INF/*.RSA

cd ..

# Create manifest
echo "Creating manifest..."
cat > "$TEMP_DIR/META-INF/MANIFEST.MF" << 'EOF'
Manifest-Version: 1.0
Main-Class: AAVSOtools.Seqplot
Created-By: Seqplot Build System
Implementation-Title: Seqplot
Implementation-Version: 6.0.0
Implementation-Vendor: AAVSO Sequence Team

EOF

# Create JAR
echo "Creating JAR file..."
cd "$TEMP_DIR"
jar cfm "../${JAR_NAME}" META-INF/MANIFEST.MF *
cd ..

# Cleanup
echo "Cleaning up..."
rm -rf "$TEMP_DIR"

# Make executable (on Unix systems)
chmod +x "${JAR_NAME}"

echo ""
echo "========================================="
echo "Success! Created: ${JAR_NAME}"
echo "========================================="
echo ""
echo "Size: $(ls -lh "${JAR_NAME}" | awk '{print $5}')"
echo ""
echo "To run:"
echo "  java -jar ${JAR_NAME}"
echo ""
echo "Or on most systems, just double-click the JAR file."
echo ""

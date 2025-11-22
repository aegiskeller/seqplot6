#!/bin/bash

echo "=== Seqplot 6.0.0 JAR Packaging ==="
echo "Creating standalone executable JAR with all dependencies"
echo "===Caution: Drop Bears may be present==="

# Clean and build first
echo "Building application..."
./build.sh

# Create JAR dir structure
echo "Preparing JAR structure..."
rm -rf jar_build
mkdir -p jar_build

# Copy compiled classes
echo "Copying compiled classes..."
cp -r build/* jar_build/

# Extract library JARs into jar_build
echo "Extracting libraries..."
cd jar_build
jar -xf ../lib/jcommon-1.0.23.jar
jar -xf ../lib/jfreechart-1.0.19.jar

# Remove META-INF from extracted libs (avoid conflicts)
rm -rf META-INF

# Create our manifest dir and file
echo "Creating manifest..."
cd ..
mkdir -p jar_build/META-INF
cat > jar_build/META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Main-Class: AAVSOtools.Seqplot
Created-By: Seqplot Build System
Implementation-Title: Seqplot
Implementation-Version: 6.0.0
Implementation-Vendor: AAVSO Tools
EOF

# Create the JAR
echo "Creating JAR file..."
cd jar_build
jar -cfm ../seqplot-6.0.0.jar META-INF/MANIFEST.MF .

# Verify the JAR
cd ..
echo "Verifyig JAR contents..."
echo "JAR file size: $(du -h seqplot-6.0.0.jar | cut -f1)"
echo "Main class check:"
jar -tf seqplot-6.0.0.jar | grep "AAVSOtools/Seqplot.class"
echo "Icon file check:"
jar -tf seqplot-6.0.0.jar | grep "seqplot_icon.png"

# Clean up
rm -rf jar_build

echo "JAR packaging complete!"
echo "File: seqplot-6.0.0.jar"
echo ""
echo "Run with: java -jar seqplot-6.0.0.jar"
echo ""
#!/bin/bash
# Seqplot v6.0.0 Launch Script

cd "$(dirname "$0")"

# Try to run JAR first
if [ -f "Seqplot-6.0.0.jar" ]; then
    java -jar Seqplot-6.0.0.jar
else
    # Fallback to class files
    java -cp "bin:lib/jcommon-1.0.23.jar:lib/jfreechart-1.0.19.jar" AAVSOtools.Seqplot
fi

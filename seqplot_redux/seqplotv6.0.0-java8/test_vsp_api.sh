#!/bin/bash

# Test VSP Photometry API
# RA: 12:10:35.80 (182.649167 degrees)
# Dec: -63:15:02.3 (-63.250639 degrees)
# FOV: 1 degree = 60 arcmin

echo "Testing AAVSO VSP Photometry API"
echo "================================="
echo ""
echo "Coordinates:"
echo "  RA:  12:10:35.80 (182.649167°)"
echo "  Dec: -63:15:02.3 (-63.250639°)"
echo "  FOV: 1 degree (60 arcmin)"
echo ""

# Convert to decimal degrees
RA_DEG=182.649167
DEC_DEG=-63.250639
FOV_ARCMIN=60
MAGLIMIT=14.0

# Test different formats
echo "----------------------------------------"
echo "Testing format: json (with redirect)"
echo "----------------------------------------"
URL="https://app.aavso.org/vsp/api/chart/?format=json&ra=${RA_DEG}&dec=${DEC_DEG}&fov=${FOV_ARCMIN}&maglimit=${MAGLIMIT}"
echo "URL: $URL"
echo ""
curl -sL "$URL" | head -100
echo ""
echo ""

echo "----------------------------------------"
echo "Testing photometry endpoint (with redirect)"
echo "----------------------------------------"
URL="https://app.aavso.org/vsp/photometry/?ra=${RA_DEG}&dec=${DEC_DEG}&fov=${FOV_ARCMIN}&maglimit=${MAGLIMIT}&all=on"
echo "URL: $URL"
echo ""
curl -sL "$URL" | head -50
echo ""
echo ""

echo "----------------------------------------"
echo "Testing with title parameter"
echo "----------------------------------------"
URL="https://app.aavso.org/vsp/photometry/?title=TEST&ra=${RA_DEG}&dec=${DEC_DEG}&fov=${FOV_ARCMIN}&maglimit=${MAGLIMIT}"
echo "URL: $URL"
echo ""
curl -sL "$URL" | head -50
echo ""

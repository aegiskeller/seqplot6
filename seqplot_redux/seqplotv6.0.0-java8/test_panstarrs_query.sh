#!/bin/bash

# Test script to time PanSTARRS ADQL query that's timing out
# This tests the exact query from the application

echo "=========================================="
echo "Testing PanSTARRS DR1 Query Performance"
echo "=========================================="
echo ""

# The exact ADQL query from the application
ADQL_QUERY='SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', 254.325542, 71.833139, 0.244141)) AND gmag IS NOT NULL AND e_gmag IS NOT NULL AND rmag IS NOT NULL AND e_rmag IS NOT NULL AND imag IS NOT NULL AND e_imag IS NOT NULL AND rmag <= 20.0 ORDER BY rmag'

TAP_URL="http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"

echo "Field Parameters:"
echo "  RA: 254.325542"
echo "  Dec: 71.833139"
echo "  Radius: 0.244141 degrees (14.65 arcmin)"
echo "  r-band limit: <= 20.0"
echo ""

echo "TAP Service: $TAP_URL"
echo ""

# URL encode the query
ENCODED_QUERY=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL_QUERY'''))")

FULL_URL="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED_QUERY}"

echo "Query URL length: ${#FULL_URL} characters"
echo ""

echo "ADQL Query:"
echo "$ADQL_QUERY"
echo ""

echo "=========================================="
echo "Starting query..."
echo "=========================================="

# Time the request with detailed output
START_TIME=$(date +%s)

curl -v \
  --max-time 120 \
  --connect-timeout 30 \
  -o test_panstarrs_result.xml \
  -w "\n\nConnection Time: %{time_connect}s\nTime to First Byte: %{time_starttransfer}s\nTotal Time: %{time_total}s\nDownload Speed: %{speed_download} bytes/sec\nHTTP Code: %{http_code}\nSize Downloaded: %{size_download} bytes\n" \
  "$FULL_URL"

EXIT_CODE=$?
END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo ""
echo "=========================================="
echo "Query Results"
echo "=========================================="
echo "Exit code: $EXIT_CODE"
echo "Total elapsed time: ${ELAPSED}s"

if [ $EXIT_CODE -eq 0 ]; then
    echo "Status: SUCCESS"
    
    # Check if file exists and has content
    if [ -f test_panstarrs_result.xml ]; then
        FILE_SIZE=$(wc -c < test_panstarrs_result.xml)
        echo "Result file size: $FILE_SIZE bytes"
        
        # Count rows in VOTable
        ROW_COUNT=$(grep -c "<TR>" test_panstarrs_result.xml || echo "0")
        echo "Number of rows returned: $ROW_COUNT"
        
        echo ""
        echo "First few lines of result:"
        head -20 test_panstarrs_result.xml
    else
        echo "ERROR: Result file not created"
    fi
elif [ $EXIT_CODE -eq 28 ]; then
    echo "Status: TIMEOUT"
    echo "The query exceeded the time limit"
elif [ $EXIT_CODE -eq 7 ]; then
    echo "Status: CONNECTION FAILED"
    echo "Could not connect to the server"
else
    echo "Status: ERROR (exit code $EXIT_CODE)"
fi

echo ""
echo "=========================================="
echo "Analysis"
echo "=========================================="

# Calculate expected number of stars (rough estimate)
RADIUS_DEG=0.244141
AREA_SQ_DEG=$(python3 -c "import math; print(math.pi * $RADIUS_DEG ** 2)")
echo "Search area: ${AREA_SQ_DEG} square degrees"

# PanSTARRS has ~3 billion sources, covering ~30,000 sq deg
# At high galactic latitude: ~100,000 sources per sq deg
ESTIMATED_SOURCES=$(python3 -c "print(int($AREA_SQ_DEG * 100000))")
echo "Estimated total PanSTARRS sources in area: ~${ESTIMATED_SOURCES}"

# With r<=20 magnitude limit (fairly deep)
ESTIMATED_BRIGHT=$(python3 -c "print(int($ESTIMATED_SOURCES * 0.15))")
echo "Estimated sources with r<=20: ~${ESTIMATED_BRIGHT}"

echo ""
echo "Possible issues:"
echo "1. Large result set - PanSTARRS is very dense"
echo "2. Multiple NOT NULL constraints (6 columns)"
echo "3. ORDER BY on large dataset"
echo "4. VizieR TAP service may be slow for this catalog"
echo ""
echo "Suggestions:"
echo "- Remove ORDER BY clause (not needed)"
echo "- Reduce constraints (maybe only require rmag NOT NULL)"
echo "- Consider using TOP N to limit results"
echo "- Try ESO TAP service instead of VizieR"

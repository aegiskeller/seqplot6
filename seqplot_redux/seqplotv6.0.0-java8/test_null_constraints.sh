#!/bin/bash

# Test parameters
RA=180.0
DEC=45.0
RADIUS=1.0  # degrees
MAG_LIMIT=20.0

TAP_URL="http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"

echo "=========================================="
echo "PanSTARRS Query: NULL Constraints Test"
echo "=========================================="
echo "Parameters:"
echo "  RA: $RA degrees"
echo "  Dec: $DEC degrees"
echo "  Radius: $RADIUS degrees (60 arcmin)"
echo "  Mag Limit: $MAG_LIMIT (r-band)"
echo ""

# Method 1: Current - only rmag NOT NULL
echo "=========================================="
echo "METHOD 1: Current (rmag IS NOT NULL only)"
echo "=========================================="

QUERY1="SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, objID FROM \"II/349/ps1\" WHERE 1=CONTAINS(POINT('ICRS', RAJ2000, DEJ2000), CIRCLE('ICRS', $RA, $DEC, $RADIUS)) AND rmag IS NOT NULL AND rmag <= $MAG_LIMIT ORDER BY rmag"

ENCODED_QUERY1=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$QUERY1'''))")
URL1="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED_QUERY1}"

echo "Constraints: rmag IS NOT NULL"
echo ""
echo "Executing query..."

time1_start=$(date +%s.%N)
curl -s -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\nSize: %{size_download} bytes\n" \
     -o /tmp/panstarrs_basic.xml \
     "$URL1"
time1_end=$(date +%s.%N)
time1=$(echo "$time1_end - $time1_start" | bc)

# Count results
rows1=$(grep -c "<TR>" /tmp/panstarrs_basic.xml 2>/dev/null || echo "0")
size1=$(wc -c < /tmp/panstarrs_basic.xml)

echo "Results: $rows1 stars"
echo "Download size: $size1 bytes"
echo "Total time: ${time1}s"
echo ""

# Method 2: Add all NOT NULL constraints
echo "=========================================="
echo "METHOD 2: All photometry NOT NULL"
echo "=========================================="

QUERY2="SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, objID FROM \"II/349/ps1\" WHERE 1=CONTAINS(POINT('ICRS', RAJ2000, DEJ2000), CIRCLE('ICRS', $RA, $DEC, $RADIUS)) AND gmag IS NOT NULL AND e_gmag IS NOT NULL AND rmag IS NOT NULL AND e_rmag IS NOT NULL AND imag IS NOT NULL AND e_imag IS NOT NULL AND rmag <= $MAG_LIMIT ORDER BY rmag"

ENCODED_QUERY2=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$QUERY2'''))")
URL2="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED_QUERY2}"

echo "Constraints: gmag, e_gmag, rmag, e_rmag, imag, e_imag all NOT NULL"
echo ""
echo "Executing query..."

time2_start=$(date +%s.%N)
curl -s -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\nSize: %{size_download} bytes\n" \
     -o /tmp/panstarrs_constrained.xml \
     "$URL2"
time2_end=$(date +%s.%N)
time2=$(echo "$time2_end - $time2_start" | bc)

# Count results
rows2=$(grep -c "<TR>" /tmp/panstarrs_constrained.xml 2>/dev/null || echo "0")
size2=$(wc -c < /tmp/panstarrs_constrained.xml)

echo "Results: $rows2 stars"
echo "Download size: $size2 bytes"
echo "Total time: ${time2}s"
echo ""

# Comparison
echo "=========================================="
echo "COMPARISON"
echo "=========================================="
echo "Method 1 (basic):       $rows1 stars in ${time1}s ($size1 bytes)"
echo "Method 2 (constrained): $rows2 stars in ${time2}s ($size2 bytes)"
echo ""
echo "Filtered out: $(($rows1 - $rows2)) stars ($(echo "scale=1; 100.0 * ($rows1 - $rows2) / $rows1" | bc)%)"
echo ""

if (( $(echo "$time1 > $time2" | bc -l) )); then
    speedup=$(echo "scale=2; $time1 / $time2" | bc -l)
    saved=$(echo "scale=2; $time1 - $time2" | bc -l)
    echo "Method 2 is FASTER by ${saved}s (${speedup}x speedup)"
elif (( $(echo "$time2 > $time1" | bc -l) )); then
    slowdown=$(echo "scale=2; $time2 / $time1" | bc -l)
    extra=$(echo "scale=2; $time2 - $time1" | bc -l)
    echo "Method 2 is SLOWER by ${extra}s (${slowdown}x slowdown)"
else
    echo "Same performance"
fi

echo ""
echo "Benefits of additional constraints:"
echo "  - Fewer rows returned (smaller download)"
echo "  - Pre-filtered data (no need to filter in Java)"
echo "  - Better query optimization potential"

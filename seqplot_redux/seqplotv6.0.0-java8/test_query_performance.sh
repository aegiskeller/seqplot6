#!/bin/bash

# Test parameters
RA=180.0
DEC=45.0
RADIUS=1.0  # degrees (for circle query)
FIELD_SIZE=1.0  # degrees (60 arcmin total width/height for box)
MAG_LIMIT=20.0

TAP_URL="http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"

echo "=========================================="
echo "PanSTARRS Query Performance Comparison"
echo "=========================================="
echo "Parameters:"
echo "  RA: $RA degrees"
echo "  Dec: $DEC degrees"
echo "  Field Size: $FIELD_SIZE degrees (60 arcmin total width/height)"
echo "  Mag Limit: $MAG_LIMIT (r-band)"
echo ""

# Method 1: CONTAINS/CIRCLE (current method)
echo "=========================================="
echo "METHOD 1: CONTAINS/CIRCLE (current)"
echo "=========================================="

QUERY1="SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, objID FROM \"II/349/ps1\" WHERE 1=CONTAINS(POINT('ICRS', RAJ2000, DEJ2000), CIRCLE('ICRS', $RA, $DEC, $RADIUS)) AND rmag IS NOT NULL AND rmag <= $MAG_LIMIT ORDER BY rmag"

ENCODED_QUERY1=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$QUERY1'''))")
URL1="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED_QUERY1}"

echo "Query: $QUERY1"
echo ""
echo "Executing query..."

time1_start=$(date +%s.%N)
curl -s -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\nSize: %{size_download} bytes\n" \
     -o /tmp/panstarrs_circle.xml \
     "$URL1"
time1_end=$(date +%s.%N)
time1=$(echo "$time1_end - $time1_start" | bc)

# Count results
rows1=$(grep -c "<TR>" /tmp/panstarrs_circle.xml 2>/dev/null || echo "0")
size1=$(wc -c < /tmp/panstarrs_circle.xml)

echo "Results: $rows1 stars"
echo "Download size: $size1 bytes"
echo "Total time: ${time1}s"
echo ""

# Method 2: BOX query with RA*cos(Dec) correction
echo "=========================================="
echo "METHOD 2: BOX with RA*cos(Dec)"
echo "=========================================="

# Calculate box boundaries
# Box should be FIELD_SIZE degrees in total width and height
# Half-width in Dec is straightforward
DEC_HALF=$(echo "$FIELD_SIZE / 2.0" | bc -l)

# Half-width in RA needs cos(Dec) correction
RA_HALF=$(echo "$FIELD_SIZE / 2.0 / c($DEC * 3.14159265359 / 180.0)" | bc -l)

RA_MIN=$(echo "$RA - $RA_HALF" | bc -l)
RA_MAX=$(echo "$RA + $RA_HALF" | bc -l)
DEC_MIN=$(echo "$DEC - $DEC_HALF" | bc -l)
DEC_MAX=$(echo "$DEC + $DEC_HALF" | bc -l)

QUERY2="SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, objID FROM \"II/349/ps1\" WHERE RAJ2000 BETWEEN $RA_MIN AND $RA_MAX AND DEJ2000 BETWEEN $DEC_MIN AND $DEC_MAX AND rmag IS NOT NULL AND rmag <= $MAG_LIMIT ORDER BY rmag"

ENCODED_QUERY2=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$QUERY2'''))")
URL2="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED_QUERY2}"

echo "Box: RA [$RA_MIN to $RA_MAX], Dec [$DEC_MIN to $DEC_MAX]"
echo "Query: $QUERY2"
echo ""
echo "Executing query..."

time2_start=$(date +%s.%N)
curl -s -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\nSize: %{size_download} bytes\n" \
     -o /tmp/panstarrs_box.xml \
     "$URL2"
time2_end=$(date +%s.%N)
time2=$(echo "$time2_end - $time2_start" | bc)

# Count results
rows2=$(grep -c "<TR>" /tmp/panstarrs_box.xml 2>/dev/null || echo "0")
size2=$(wc -c < /tmp/panstarrs_box.xml)

echo "Results: $rows2 stars"
echo "Download size: $size2 bytes"
echo "Total time: ${time2}s"
echo ""

# Comparison
echo "=========================================="
echo "COMPARISON"
echo "=========================================="
echo "Method 1 (CIRCLE): $rows1 stars in ${time1}s ($size1 bytes)"
echo "Method 2 (BOX):    $rows2 stars in ${time2}s ($size2 bytes)"
echo ""

if (( $(echo "$time1 > $time2" | bc -l) )); then
    speedup=$(echo "scale=2; $time1 / $time2" | bc -l)
    echo "BOX is ${speedup}x FASTER"
elif (( $(echo "$time2 > $time1" | bc -l) )); then
    speedup=$(echo "scale=2; $time2 / $time1" | bc -l)
    echo "CIRCLE is ${speedup}x FASTER"
else
    echo "Same performance"
fi

echo ""
echo "Note: BOX query may return slightly more stars (includes corners outside circle)"
echo "      Difference: $(($rows2 - $rows1)) stars"

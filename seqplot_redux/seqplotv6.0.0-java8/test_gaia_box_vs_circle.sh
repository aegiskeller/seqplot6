#!/bin/bash

# Test parameters for Gaia DR2
RA=180.0
DEC=45.0
FIELD_SIZE=1.0  # degrees

# Calculate box boundaries
RA_HALF=$(echo "$FIELD_SIZE / 2.0" | bc -l)
DEC_HALF=$(echo "$FIELD_SIZE / 2.0" | bc -l)
RA_MIN=$(echo "$RA - $RA_HALF" | bc -l)
RA_MAX=$(echo "$RA + $RA_HALF" | bc -l)
DEC_MIN=$(echo "$DEC - $DEC_HALF" | bc -l)
DEC_MAX=$(echo "$DEC + $DEC_HALF" | bc -l)

# Calculate radius for circle query (half diagonal of box)
RADIUS=$(echo "sqrt(($RA_HALF)^2 + ($DEC_HALF)^2)" | bc -l)

MAG_LIMIT=18.0

TAP_URL="https://gea.esac.esa.int/tap-server/tap/sync"

echo "=========================================="
echo "Gaia DR2: BOX vs CIRCLE Performance Test"
echo "=========================================="
echo "Parameters:"
echo "  RA: $RA degrees"
echo "  Dec: $DEC degrees"
echo "  Field: $FIELD_SIZE deg"
echo "  Box: RA $RA_MIN-$RA_MAX, Dec $DEC_MIN-$DEC_MAX"
echo "  Circle: Radius $RADIUS deg"
echo "  Mag Limit: $MAG_LIMIT (G-band)"
echo ""

# Method 1: BOX query (current method) with TOP 10000
echo "=========================================="
echo "METHOD 1: BOX query (TOP 10000)"
echo "=========================================="

QUERY1="SELECT TOP 10000 ra, dec, parallax, parallax_error, phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag, source_id FROM gaiadr2.gaia_source WHERE ra BETWEEN $RA_MIN AND $RA_MAX AND dec BETWEEN $DEC_MIN AND $DEC_MAX AND phot_bp_mean_mag IS NOT NULL AND phot_bp_mean_mag < 19 AND phot_g_mean_mag <= $MAG_LIMIT"

ENCODED_QUERY1=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$QUERY1'''))")
URL1="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable_plain&QUERY=${ENCODED_QUERY1}"

echo "Query: BOX with ra BETWEEN ... AND ... AND dec BETWEEN ..."
echo ""
echo "Executing query..."

time1_start=$(date +%s.%N)
curl -s -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\nSize: %{size_download} bytes\n" \
     -o /tmp/gaia_box.xml \
     "$URL1"
time1_end=$(date +%s.%N)
time1=$(echo "$time1_end - $time1_start" | bc)

# Count results
rows1=$(grep -c "<TR>" /tmp/gaia_box.xml 2>/dev/null || echo "0")
size1=$(wc -c < /tmp/gaia_box.xml)

echo "Results: $rows1 stars"
echo "Download size: $size1 bytes"
echo "Total time: ${time1}s"
echo ""

# Method 2: CIRCLE query with TOP 10000
echo "=========================================="
echo "METHOD 2: CIRCLE query (TOP 10000)"
echo "=========================================="

QUERY2="SELECT TOP 10000 ra, dec, parallax, parallax_error, phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag, source_id FROM gaiadr2.gaia_source WHERE 1=CONTAINS(POINT('ICRS', ra, dec), CIRCLE('ICRS', $RA, $DEC, $RADIUS)) AND phot_bp_mean_mag IS NOT NULL AND phot_bp_mean_mag < 19 AND phot_g_mean_mag <= $MAG_LIMIT"

ENCODED_QUERY2=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$QUERY2'''))")
URL2="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable_plain&QUERY=${ENCODED_QUERY2}"

echo "Query: CONTAINS(POINT, CIRCLE)"
echo ""
echo "Executing query..."

time2_start=$(date +%s.%N)
curl -s -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\nSize: %{size_download} bytes\n" \
     -o /tmp/gaia_circle.xml \
     "$URL2"
time2_end=$(date +%s.%N)
time2=$(echo "$time2_end - $time2_start" | bc)

# Count results
rows2=$(grep -c "<TR>" /tmp/gaia_circle.xml 2>/dev/null || echo "0")
size2=$(wc -c < /tmp/gaia_circle.xml)

echo "Results: $rows2 stars"
echo "Download size: $size2 bytes"
echo "Total time: ${time2}s"
echo ""

# Method 3: BOX query with TOP 5000 (original limit)
echo "=========================================="
echo "METHOD 3: BOX query (TOP 5000 - original)"
echo "=========================================="

QUERY3="SELECT TOP 5000 ra, dec, parallax, parallax_error, phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag, source_id FROM gaiadr2.gaia_source WHERE ra BETWEEN $RA_MIN AND $RA_MAX AND dec BETWEEN $DEC_MIN AND $DEC_MAX AND phot_bp_mean_mag IS NOT NULL AND phot_bp_mean_mag < 19 AND phot_g_mean_mag <= $MAG_LIMIT"

ENCODED_QUERY3=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$QUERY3'''))")
URL3="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable_plain&QUERY=${ENCODED_QUERY3}"

echo "Query: BOX with TOP 5000"
echo ""
echo "Executing query..."

time3_start=$(date +%s.%N)
curl -s -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\nSize: %{size_download} bytes\n" \
     -o /tmp/gaia_box5k.xml \
     "$URL3"
time3_end=$(date +%s.%N)
time3=$(echo "$time3_end - $time3_start" | bc)

# Count results
rows3=$(grep -c "<TR>" /tmp/gaia_box5k.xml 2>/dev/null || echo "0")
size3=$(wc -c < /tmp/gaia_box5k.xml)

echo "Results: $rows3 stars"
echo "Download size: $size3 bytes"
echo "Total time: ${time3}s"
echo ""

# Comparison
echo "=========================================="
echo "COMPARISON"
echo "=========================================="
echo "Method 1 (BOX 10K):    $rows1 stars in ${time1}s ($size1 bytes)"
echo "Method 2 (CIRCLE 10K): $rows2 stars in ${time2}s ($size2 bytes)"
echo "Method 3 (BOX 5K):     $rows3 stars in ${time3}s ($size3 bytes)"
echo ""

# Find fastest
if (( $(echo "$time1 <= $time2 && $time1 <= $time3" | bc -l) )); then
    echo "FASTEST: BOX with TOP 10000"
elif (( $(echo "$time2 <= $time1 && $time2 <= $time3" | bc -l) )); then
    echo "FASTEST: CIRCLE with TOP 10000"
else
    echo "FASTEST: BOX with TOP 5000 (original)"
fi

echo ""
echo "Note: Circle may return fewer stars (excludes box corners outside circle)"
if [ $rows1 -gt 0 ]; then
    diff=$(($rows1 - $rows2))
    echo "      Difference: $diff stars"
fi

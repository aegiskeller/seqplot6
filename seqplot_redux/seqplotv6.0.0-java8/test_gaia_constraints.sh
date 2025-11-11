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

MAG_LIMIT=18.0

TAP_URL="https://gea.esac.esa.int/tap-server/tap/sync"

echo "=========================================="
echo "Gaia DR2 Query: NULL Constraints Test"
echo "=========================================="
echo "Parameters:"
echo "  RA: $RA degrees"
echo "  Dec: $DEC degrees"
echo "  Field: $FIELD_SIZE deg (Box: RA $RA_MIN-$RA_MAX, Dec $DEC_MIN-$DEC_MAX)"
echo "  Mag Limit: $MAG_LIMIT (G-band)"
echo ""

# Method 1: Current - only BP mag NOT NULL
echo "=========================================="
echo "METHOD 1: Current (BP mag NOT NULL only)"
echo "=========================================="

QUERY1="SELECT TOP 5000 ra, dec, parallax, parallax_error, phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag, source_id FROM gaiadr2.gaia_source WHERE ra BETWEEN $RA_MIN AND $RA_MAX AND dec BETWEEN $DEC_MIN AND $DEC_MAX AND phot_bp_mean_mag IS NOT NULL AND phot_bp_mean_mag < 19 AND phot_g_mean_mag <= $MAG_LIMIT"

ENCODED_QUERY1=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$QUERY1'''))")
URL1="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable_plain&QUERY=${ENCODED_QUERY1}"

echo "Constraints: phot_bp_mean_mag IS NOT NULL AND < 19"
echo ""
echo "Executing query..."

time1_start=$(date +%s.%N)
curl -s -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\nSize: %{size_download} bytes\n" \
     -o /tmp/gaia_basic.xml \
     "$URL1"
time1_end=$(date +%s.%N)
time1=$(echo "$time1_end - $time1_start" | bc)

# Count results
rows1=$(grep -c "<TR>" /tmp/gaia_basic.xml 2>/dev/null || echo "0")
size1=$(wc -c < /tmp/gaia_basic.xml)

echo "Results: $rows1 stars"
echo "Download size: $size1 bytes"
echo "Total time: ${time1}s"
echo ""

# Method 2: Add all NOT NULL constraints for photometry
echo "=========================================="
echo "METHOD 2: All photometry NOT NULL"
echo "=========================================="

QUERY2="SELECT TOP 5000 ra, dec, parallax, parallax_error, phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag, source_id FROM gaiadr2.gaia_source WHERE ra BETWEEN $RA_MIN AND $RA_MAX AND dec BETWEEN $DEC_MIN AND $DEC_MAX AND phot_g_mean_mag IS NOT NULL AND phot_bp_mean_mag IS NOT NULL AND phot_rp_mean_mag IS NOT NULL AND phot_bp_mean_mag < 19 AND phot_g_mean_mag <= $MAG_LIMIT"

ENCODED_QUERY2=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$QUERY2'''))")
URL2="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable_plain&QUERY=${ENCODED_QUERY2}"

echo "Constraints: phot_g_mean_mag, phot_bp_mean_mag, phot_rp_mean_mag all NOT NULL"
echo ""
echo "Executing query..."

time2_start=$(date +%s.%N)
curl -s -w "\nHTTP Status: %{http_code}\nTime: %{time_total}s\nSize: %{size_download} bytes\n" \
     -o /tmp/gaia_constrained.xml \
     "$URL2"
time2_end=$(date +%s.%N)
time2=$(echo "$time2_end - $time2_start" | bc)

# Count results
rows2=$(grep -c "<TR>" /tmp/gaia_constrained.xml 2>/dev/null || echo "0")
size2=$(wc -c < /tmp/gaia_constrained.xml)

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

if [ $rows1 -gt 0 ]; then
    filtered=$(($rows1 - $rows2))
    percent=$(echo "scale=1; 100.0 * $filtered / $rows1" | bc)
    echo "Filtered out: $filtered stars (${percent}%)"
fi
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
echo "Note: Gaia DR2 has very complete photometry, so fewer stars should be filtered"

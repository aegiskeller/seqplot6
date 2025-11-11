#!/bin/bash

echo "=========================================================="
echo "PanSTARRS Query Performance: CIRCLE vs BOX"
echo "Field: RA=254.33°, Dec=71.83° (high declination)"
echo "Size: 29 arcmin (0.483 degrees)"
echo "=========================================================="
echo ""

# Test parameters
RA=254.325542
DEC=71.833139
RADIUS=0.241667  # 29 arcmin / 2 in degrees
BOXSIZE=0.483    # 29 arcmin in degrees

TAP_URL="http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"

echo "TEST 1: CIRCLE geometry (original)"
echo "-----------------------------------"
ADQL_CIRCLE='SELECT TOP 5000 RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', '${RA}', '${DEC}', '${RADIUS}')) AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL AND rmag <= 20.0'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL_CIRCLE'''))")
START=$(date +%s)

curl -s -m 30 -o test_circle.xml "${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}" 2>/dev/null
EXIT1=$?

END=$(date +%s)
TIME1=$((END - START))

if [ $EXIT1 -eq 0 ] && [ -f test_circle.xml ] && [ -s test_circle.xml ]; then
    ROWS=$(grep -c "<TR>" test_circle.xml || echo "0")
    SIZE=$(wc -c < test_circle.xml)
    echo "✓ SUCCESS: ${TIME1}s, ${ROWS} rows, ${SIZE} bytes"
else
    echo "✗ TIMEOUT or FAILED (${TIME1}s)"
fi
echo ""

echo "TEST 2: BOX geometry (optimized)"
echo "---------------------------------"
ADQL_BOX='SELECT TOP 5000 RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), BOX('\''ICRS'\'', '${RA}', '${DEC}', '${BOXSIZE}', '${BOXSIZE}')) AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL AND rmag <= 20.0'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL_BOX'''))")
START=$(date +%s)

curl -s -m 30 -o test_box.xml "${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}" 2>/dev/null
EXIT2=$?

END=$(date +%s)
TIME2=$((END - START))

if [ $EXIT2 -eq 0 ] && [ -f test_box.xml ] && [ -s test_box.xml ]; then
    ROWS=$(grep -c "<TR>" test_box.xml || echo "0")
    SIZE=$(wc -c < test_box.xml)
    RATE=$((ROWS / TIME2))
    echo "✓ SUCCESS: ${TIME2}s, ${ROWS} rows, ${SIZE} bytes (${RATE} rows/sec)"
else
    echo "✗ TIMEOUT or FAILED (${TIME2}s)"
fi
echo ""

echo "=========================================================="
echo "SUMMARY"
echo "=========================================================="
if [ $EXIT1 -eq 0 ] && [ -f test_circle.xml ] && [ -s test_circle.xml ]; then
    echo "CIRCLE: ✓ Works in ${TIME1}s"
else
    echo "CIRCLE: ✗ Timeout (${TIME1}s)"
fi

if [ $EXIT2 -eq 0 ] && [ -f test_box.xml ] && [ -s test_box.xml ]; then
    echo "BOX:    ✓ Works in ${TIME2}s"
    if [ $EXIT1 -eq 0 ]; then
        SPEEDUP=$((TIME1 / TIME2))
        echo ""
        echo "Result: BOX is ${SPEEDUP}x faster than CIRCLE"
    else
        echo ""
        echo "Result: BOX query fixed the timeout issue!"
    fi
else
    echo "BOX:    ✗ Timeout (${TIME2}s)"
fi
echo ""

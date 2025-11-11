#!/bin/bash

# Test with smaller field to verify query works at all

echo "Testing PanSTARRS queries with SMALLER field (5 arcmin = 0.083 degrees)"
echo "========================================================================"
echo ""

TAP_URL="http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"

# Test with 5 arcmin radius (much smaller)
RADIUS_DEG=0.083333

echo "TEST 1: Small field with ORDER BY"
echo "----------------------------------"
ADQL='SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', 254.325542, 71.833139, '${RADIUS_DEG}')) AND gmag IS NOT NULL AND e_gmag IS NOT NULL AND rmag IS NOT NULL AND e_rmag IS NOT NULL AND imag IS NOT NULL AND e_imag IS NOT NULL AND rmag <= 20.0 ORDER BY rmag'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL'''))")
START=$(date +%s)
curl -s -o test_small_order.xml "${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}"
END=$(date +%s)
TIME=$((END - START))

if [ -f test_small_order.xml ]; then
    ROWS=$(grep -c "<TR>" test_small_order.xml || echo "0")
    SIZE=$(wc -c < test_small_order.xml)
    echo "✓ ${TIME}s - ${ROWS} rows, ${SIZE} bytes"
fi
echo ""

echo "TEST 2: Small field WITHOUT ORDER BY"
echo "-------------------------------------"
ADQL='SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', 254.325542, 71.833139, '${RADIUS_DEG}')) AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL AND rmag <= 20.0'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL'''))")
START=$(date +%s)
curl -s -o test_small_noorder.xml "${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}"
END=$(date +%s)
TIME=$((END - START))

if [ -f test_small_noorder.xml ]; then
    ROWS=$(grep -c "<TR>" test_small_noorder.xml || echo "0")
    SIZE=$(wc -c < test_small_noorder.xml)
    echo "✓ ${TIME}s - ${ROWS} rows, ${SIZE} bytes"
fi
echo ""

echo "Now testing LARGE field (29 arcmin = 0.483 degrees) WITHOUT ORDER BY"
echo "====================================================================="
echo ""

RADIUS_LARGE=0.241667

echo "TEST 3: Large field WITHOUT ORDER BY (optimized)"
echo "-------------------------------------------------"
ADQL='SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', 254.325542, 71.833139, '${RADIUS_LARGE}')) AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL AND rmag <= 20.0'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL'''))")

echo "Starting query with 60s timeout..."
START=$(date +%s)

curl -s -o test_large_noorder.xml "${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}" &
CURL_PID=$!

WAITED=0
while kill -0 $CURL_PID 2>/dev/null && [ $WAITED -lt 60 ]; do
    sleep 5
    WAITED=$((WAITED + 5))
    echo "  ... ${WAITED}s elapsed"
done

if kill -0 $CURL_PID 2>/dev/null; then
    echo "✗ TIMEOUT after 60s"
    kill $CURL_PID 2>/dev/null
else
    wait $CURL_PID
    END=$(date +%s)
    TIME=$((END - START))
    
    if [ -f test_large_noorder.xml ]; then
        ROWS=$(grep -c "<TR>" test_large_noorder.xml || echo "0")
        SIZE=$(wc -c < test_large_noorder.xml)
        echo "✓ SUCCESS in ${TIME}s - ${ROWS} rows, ${SIZE} bytes"
    fi
fi

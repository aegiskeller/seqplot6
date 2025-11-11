#!/bin/bash

# Test optimized PanSTARRS queries to find what works

echo "=========================================="
echo "Testing PanSTARRS Query Optimizations"
echo "=========================================="
echo ""

TAP_URL="http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"
RA=254.325542
DEC=71.833139
RADIUS=0.244141
RMAG_LIMIT=20.0

# Test 1: Remove ORDER BY
echo "TEST 1: Remove ORDER BY clause"
echo "-------------------------------"
ADQL1='SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', 254.325542, 71.833139, 0.244141)) AND gmag IS NOT NULL AND e_gmag IS NOT NULL AND rmag IS NOT NULL AND e_rmag IS NOT NULL AND imag IS NOT NULL AND e_imag IS NOT NULL AND rmag <= 20.0'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL1'''))")
FULL_URL="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}"

echo "Starting query..."
START=$(date +%s)
timeout 30 curl -s -o test1.xml "$FULL_URL"
EXIT1=$?
END=$(date +%s)
TIME1=$((END - START))

if [ $EXIT1 -eq 0 ]; then
    ROWS=$(grep -c "<TR>" test1.xml || echo "0")
    SIZE=$(wc -c < test1.xml)
    echo "✓ SUCCESS: ${TIME1}s, ${ROWS} rows, ${SIZE} bytes"
elif [ $EXIT1 -eq 124 ]; then
    echo "✗ TIMEOUT after 30s"
else
    echo "✗ FAILED (exit $EXIT1)"
fi
echo ""

# Test 2: Reduce constraints - only require rmag and errors
echo "TEST 2: Reduce constraints (only rmag, gmag, imag required)"
echo "------------------------------------------------------------"
ADQL2='SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', 254.325542, 71.833139, 0.244141)) AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL AND rmag <= 20.0'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL2'''))")
FULL_URL="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}"

echo "Starting query..."
START=$(date +%s)
timeout 30 curl -s -o test2.xml "$FULL_URL"
EXIT2=$?
END=$(date +%s)
TIME2=$((END - START))

if [ $EXIT2 -eq 0 ]; then
    ROWS=$(grep -c "<TR>" test2.xml || echo "0")
    SIZE=$(wc -c < test2.xml)
    echo "✓ SUCCESS: ${TIME2}s, ${ROWS} rows, ${SIZE} bytes"
elif [ $EXIT2 -eq 124 ]; then
    echo "✗ TIMEOUT after 30s"
else
    echo "✗ FAILED (exit $EXIT2)"
fi
echo ""

# Test 3: Minimal query - only rmag constraint
echo "TEST 3: Minimal constraints (only rmag required)"
echo "-------------------------------------------------"
ADQL3='SELECT RAJ2000, DEJ2000, gmag, rmag, imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', 254.325542, 71.833139, 0.244141)) AND rmag IS NOT NULL AND rmag <= 20.0'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL3'''))")
FULL_URL="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}"

echo "Starting query..."
START=$(date +%s)
timeout 30 curl -s -o test3.xml "$FULL_URL"
EXIT3=$?
END=$(date +%s)
TIME3=$((END - START))

if [ $EXIT3 -eq 0 ]; then
    ROWS=$(grep -c "<TR>" test3.xml || echo "0")
    SIZE=$(wc -c < test3.xml)
    echo "✓ SUCCESS: ${TIME3}s, ${ROWS} rows, ${SIZE} bytes"
elif [ $EXIT3 -eq 124 ]; then
    echo "✗ TIMEOUT after 30s"
else
    echo "✗ FAILED (exit $EXIT3)"
fi
echo ""

# Test 4: Use TOP to limit results
echo "TEST 4: Use TOP 1000 to limit results"
echo "--------------------------------------"
ADQL4='SELECT TOP 1000 RAJ2000, DEJ2000, gmag, rmag, imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', 254.325542, 71.833139, 0.244141)) AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL AND rmag <= 20.0'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL4'''))")
FULL_URL="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}"

echo "Starting query..."
START=$(date +%s)
timeout 30 curl -s -o test4.xml "$FULL_URL"
EXIT4=$?
END=$(date +%s)
TIME4=$((END - START))

if [ $EXIT4 -eq 0 ]; then
    ROWS=$(grep -c "<TR>" test4.xml || echo "0")
    SIZE=$(wc -c < test4.xml)
    echo "✓ SUCCESS: ${TIME4}s, ${ROWS} rows, ${SIZE} bytes"
elif [ $EXIT4 -eq 124 ]; then
    echo "✗ TIMEOUT after 30s"
else
    echo "✗ FAILED (exit $EXIT4)"
fi
echo ""

echo "=========================================="
echo "Summary"
echo "=========================================="
echo "Test 1 (no ORDER BY): $([ $EXIT1 -eq 0 ] && echo SUCCESS || echo FAILED)"
echo "Test 2 (fewer constraints): $([ $EXIT2 -eq 0 ] && echo SUCCESS || echo FAILED)"
echo "Test 3 (minimal constraints): $([ $EXIT3 -eq 0 ] && echo SUCCESS || echo FAILED)"
echo "Test 4 (TOP 1000): $([ $EXIT4 -eq 0 ] && echo SUCCESS || echo FAILED)"
echo ""
echo "Recommendation: Use the fastest query that returns enough data"

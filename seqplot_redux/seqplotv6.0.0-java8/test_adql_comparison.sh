#!/bin/bash

# Simple timing test comparing original vs optimized PanSTARRS queries

echo "=========================================="
echo "PanSTARRS Query Performance Comparison"
echo "=========================================="
echo ""

TAP_URL="http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"

# Original query (with ORDER BY and 6 NOT NULL constraints)
echo "TEST 1: ORIGINAL Query (ORDER BY + 6 NOT NULL constraints)"
echo "-----------------------------------------------------------"
ADQL_ORIGINAL='SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', 254.325542, 71.833139, 0.244141)) AND gmag IS NOT NULL AND e_gmag IS NOT NULL AND rmag IS NOT NULL AND e_rmag IS NOT NULL AND imag IS NOT NULL AND e_imag IS NOT NULL AND rmag <= 20.0 ORDER BY rmag'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL_ORIGINAL'''))")
FULL_URL="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}"

echo "Starting query..."
START=$(date +%s)

# Run with 60 second timeout using background process
curl -s -o test_original.xml "$FULL_URL" &
CURL_PID=$!

# Wait up to 60 seconds
WAITED=0
while kill -0 $CURL_PID 2>/dev/null && [ $WAITED -lt 60 ]; do
    sleep 1
    WAITED=$((WAITED + 1))
    if [ $((WAITED % 10)) -eq 0 ]; then
        echo "  ... still waiting (${WAITED}s)"
    fi
done

# Check if still running
if kill -0 $CURL_PID 2>/dev/null; then
    echo "✗ TIMEOUT after 60s - killing process"
    kill $CURL_PID 2>/dev/null
    EXIT1=124
else
    wait $CURL_PID
    EXIT1=$?
fi

END=$(date +%s)
TIME1=$((END - START))

if [ $EXIT1 -eq 0 ] && [ -f test_original.xml ]; then
    ROWS=$(grep -c "<TR>" test_original.xml || echo "0")
    SIZE=$(wc -c < test_original.xml)
    echo "✓ SUCCESS: ${TIME1}s, ${ROWS} rows, ${SIZE} bytes"
else
    echo "✗ FAILED or TIMEOUT after ${TIME1}s"
fi
echo ""

# Optimized query (no ORDER BY, only 3 NOT NULL constraints)
echo "TEST 2: OPTIMIZED Query (no ORDER BY, 3 NOT NULL constraints)"
echo "--------------------------------------------------------------"
ADQL_OPTIMIZED='SELECT RAJ2000, DEJ2000, e_RAJ2000, e_DEJ2000, gmag, e_gmag, rmag, e_rmag, imag, e_imag, Ng, objID FROM "II/349/ps1" WHERE 1=CONTAINS(POINT('\''ICRS'\'', RAJ2000, DEJ2000), CIRCLE('\''ICRS'\'', 254.325542, 71.833139, 0.244141)) AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL AND rmag <= 20.0'

ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$ADQL_OPTIMIZED'''))")
FULL_URL="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED}"

echo "Starting query..."
START=$(date +%s)

# Run with 60 second timeout using background process
curl -s -o test_optimized.xml "$FULL_URL" &
CURL_PID=$!

# Wait up to 60 seconds
WAITED=0
while kill -0 $CURL_PID 2>/dev/null && [ $WAITED -lt 60 ]; do
    sleep 1
    WAITED=$((WAITED + 1))
    if [ $((WAITED % 5)) -eq 0 ]; then
        echo "  ... ${WAITED}s elapsed"
    fi
done

# Check if still running
if kill -0 $CURL_PID 2>/dev/null; then
    echo "✗ TIMEOUT after 60s - killing process"
    kill $CURL_PID 2>/dev/null
    EXIT2=124
else
    wait $CURL_PID
    EXIT2=$?
fi

END=$(date +%s)
TIME2=$((END - START))

if [ $EXIT2 -eq 0 ] && [ -f test_optimized.xml ]; then
    ROWS=$(grep -c "<TR>" test_optimized.xml || echo "0")
    SIZE=$(wc -c < test_optimized.xml)
    echo "✓ SUCCESS: ${TIME2}s, ${ROWS} rows, ${SIZE} bytes"
else
    echo "✗ FAILED or TIMEOUT after ${TIME2}s"
fi
echo ""

# Summary
echo "=========================================="
echo "Summary"
echo "=========================================="
echo ""
echo "Original query (ORDER BY + 6 NOT NULL):"
if [ $EXIT1 -eq 0 ]; then
    ROWS1=$(grep -c "<TR>" test_original.xml || echo "0")
    echo "  ✓ SUCCESS in ${TIME1}s - ${ROWS1} rows"
else
    echo "  ✗ TIMEOUT/FAILED (${TIME1}s)"
fi
echo ""
echo "Optimized query (no ORDER BY, 3 NOT NULL):"
if [ $EXIT2 -eq 0 ]; then
    ROWS2=$(grep -c "<TR>" test_optimized.xml || echo "0")
    echo "  ✓ SUCCESS in ${TIME2}s - ${ROWS2} rows"
    
    if [ $EXIT1 -eq 0 ]; then
        SPEEDUP=$(python3 -c "print(f'{$TIME1 / $TIME2:.1f}x')")
        echo "  → Speedup: ${SPEEDUP} faster"
    fi
else
    echo "  ✗ TIMEOUT/FAILED (${TIME2}s)"
fi
echo ""

if [ $EXIT2 -eq 0 ] && [ $EXIT1 -ne 0 ]; then
    echo "Result: Optimization fixed the timeout!"
elif [ $EXIT2 -eq 0 ]; then
    echo "Result: Both work, but optimized is faster"
else
    echo "Result: Both queries have issues"
fi

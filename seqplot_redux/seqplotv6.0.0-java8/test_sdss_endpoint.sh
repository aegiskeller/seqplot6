#!/bin/bash

# SDSS DR12 Endpoint Test Script
# Tests the SDSS SQL endpoint directly with configurable parameters

# Default parameters
RA=${1:-180.0}
DEC=${2:-60.0}
RADIUS=${3:-0.5}
MAG_LIMIT=${4:-15.0}

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}SDSS DR12 Endpoint Test${NC}"
echo -e "${BLUE}========================================${NC}\n"

echo -e "${YELLOW}Test Parameters:${NC}"
echo "  RA:          $RA degrees"
echo "  Dec:         $DEC degrees"
echo "  Search Radius: $RADIUS degrees"
echo "  Mag Limit:   $MAG_LIMIT"
echo ""

# Build the SQL query
SQL_QUERY=$(cat <<EOF
SELECT s.objid, s.ra, dbo.fHMS(s.ra) as HMSra, s.dec, dbo.fDMS(s.dec) as DMSdec, 
(s.u - 0.0316*(s.u - s.g) - 0.7487) as U, 
(s.u - 0.8116*(s.u - s.g) + 0.1313) as B, 
(s.g - 0.5784*(s.g - s.r) - 0.0038) as V, 
(s.r - 0.1837*(s.g - s.r) - 0.0971) as R, 
(s.r - 1.2444*(s.r - s.i) - 0.3820) as I, 
((s.g - 0.5784*(s.g - s.r) - 0.0038) - $MAG_LIMIT) as VmagDifference 
FROM star s, dbo.fGetNearbyObjEq($RA, $DEC, $RADIUS) f 
WHERE s.objid = f.objid 
AND (0.1884*s.u + 0.39*s.g - 0.5784*s.r + 0.1351) between 0.3 and 1 
AND (s.g - 0.5784*(s.g - s.r) - 0.0038) < 19 
ORDER BY ((s.g - 0.5784*(s.g - s.r) - 0.0038) - $MAG_LIMIT)
EOF
)

echo -e "${YELLOW}SQL Query:${NC}"
echo "$SQL_QUERY"
echo ""

# SDSS endpoint
SDSS_URL="http://skyserver.sdss.org/dr12/en/tools/search/sql.aspx"

echo -e "${YELLOW}Connecting to SDSS...${NC}"
echo "URL: $SDSS_URL"
echo ""

# URL encode the SQL query
ENCODED_SQL=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$SQL_QUERY'''))")

# Make the request with timing
echo -e "${YELLOW}Sending POST request...${NC}"
START_TIME=$(date +%s)

# Create temporary files for response and headers
RESPONSE_FILE="/tmp/sdss_response_$$.csv"
HEADERS_FILE="/tmp/sdss_headers_$$.txt"

# Use curl to send the POST request with proper headers
HTTP_CODE=$(curl -s -L -o "$RESPONSE_FILE" -w "%{http_code}" \
  -D "$HEADERS_FILE" \
  -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -X POST "$SDSS_URL" \
  -d "cmd=$ENCODED_SQL" \
  -d "format=csv")

END_TIME=$(date +%s)
ELAPSED_SEC=$((END_TIME - START_TIME))

echo ""
echo -e "${YELLOW}Response:${NC}"
echo "HTTP Code: $HTTP_CODE"
echo "Response Time: ${ELAPSED_SEC}s"
echo ""

# Check HTTP code
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Connection successful${NC}"
elif [ "$HTTP_CODE" = "400" ]; then
    echo -e "${RED}✗ Bad Request - SQL syntax error${NC}"
elif [ "$HTTP_CODE" = "500" ]; then
    echo -e "${RED}✗ Server Error - SDSS server issue${NC}"
else
    echo -e "${RED}✗ Connection failed with HTTP code $HTTP_CODE${NC}"
fi

echo ""
echo -e "${YELLOW}Response Preview (first 1000 characters):${NC}"
head -c 1000 "$RESPONSE_FILE"
echo ""
echo ""

# Parse and analyze response
echo -e "${YELLOW}Response Analysis:${NC}"

# Count lines
LINE_COUNT=$(wc -l < "$RESPONSE_FILE")
echo "Total lines: $LINE_COUNT"

# Get header (first line)
HEADER=$(head -1 "$RESPONSE_FILE")
echo "Header: $HEADER"

# Count data rows
DATA_ROWS=$((LINE_COUNT - 1))
echo "Data rows: $DATA_ROWS"

if [ "$DATA_ROWS" -gt 0 ]; then
    echo ""
    echo -e "${GREEN}✓ Query returned $DATA_ROWS results${NC}"
    echo ""
    echo -e "${YELLOW}First 3 data rows:${NC}"
    head -4 "$RESPONSE_FILE" | tail -3
else
    echo ""
    echo -e "${YELLOW}⚠ No data returned (query valid, but no matching objects)${NC}"
fi

echo ""
echo -e "${YELLOW}CSV Field Structure:${NC}"
echo "Expected fields:"
echo "  1. objid - SDSS object ID"
echo "  2. ra - Right Ascension (degrees)"
echo "  3. HMSra - RA in HMS format"
echo "  4. dec - Declination (degrees)"
echo "  5. DMSdec - Dec in DMS format"
echo "  6. U - U magnitude (Johnson-Cousins)"
echo "  7. B - B magnitude (Johnson-Cousins)"
echo "  8. V - V magnitude (Johnson-Cousins)"
echo "  9. R - R magnitude (Cousins)"
echo "  10. I - I magnitude (Cousins)"
echo "  11. VmagDifference - V magnitude offset from limit"

echo ""
echo -e "${YELLOW}Command to test with different parameters:${NC}"
echo "  bash test_sdss_endpoint.sh [RA] [Dec] [Radius] [MagLimit]"
echo ""
echo -e "${YELLOW}Examples:${NC}"
echo "  bash test_sdss_endpoint.sh 180.0 60.0 0.5 15.0    # High declination"
echo "  bash test_sdss_endpoint.sh 120.0 0.0 0.3 14.0     # Galactic equator (crowded)"
echo "  bash test_sdss_endpoint.sh 45.0 45.0 1.0 16.0     # Another region"
echo ""

# Save to file for reference
OUTPUT_FILE="/tmp/sdss_test_$(date +%Y%m%d_%H%M%S).csv"
cp "$RESPONSE_FILE" "$OUTPUT_FILE"
echo -e "${GREEN}✓ Full response saved to: $OUTPUT_FILE${NC}"

# Cleanup
rm -f "$RESPONSE_FILE" "$HEADERS_FILE"

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test Complete${NC}"
echo -e "${BLUE}========================================${NC}"

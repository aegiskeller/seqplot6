#!/bin/bash
# Test script for SDSS DR12 via VizieR TAP
# Tests the new VizieR catalog approach

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}SDSS DR12 VizieR TAP Test${NC}"
echo -e "${BLUE}========================================${NC}"

# Test parameters - using a known field with bright stars
# EE Tel coordinates: RA=19.84083, Dec=-54.95194
RA="${1:-19.84083}"
DEC="${2:--54.95194}"
BOX_SIZE="${3:-0.25}"  # degrees (field diameter)
MAG_LIMIT="${4:-15.0}"

echo -e "\n${YELLOW}Test Parameters:${NC}"
echo "  Target: RA=$RA, Dec=$DEC"
echo "  Box Size: ${BOX_SIZE}° x ${BOX_SIZE}°"
echo "  Magnitude Limit (g-band): $MAG_LIMIT"

# Use CDS Strasbourg mirror (most reliable)
TAP_URL="http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"

# Build ADQL query for SDSS DR12 (V/147/sdss12)
ADQL_QUERY="SELECT TOP 5000 RA_ICRS, DE_ICRS, umag, e_umag, gmag, e_gmag, rmag, e_rmag, imag, e_imag, zmag, e_zmag, objID FROM \"V/147/sdss12\" WHERE 1=CONTAINS(POINT('ICRS', RA_ICRS, DE_ICRS), BOX('ICRS', $RA, $DEC, $BOX_SIZE, $BOX_SIZE)) AND umag IS NOT NULL AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL AND gmag <= $MAG_LIMIT"

echo -e "\n${YELLOW}ADQL Query:${NC}"
echo "$ADQL_QUERY"

# URL encode the query
ENCODED_QUERY=$(printf '%s' "$ADQL_QUERY" | jq -sRr @uri)

# Build full query URL
QUERY_URL="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED_QUERY}"

echo -e "\n${YELLOW}Query URL:${NC}"
echo "$QUERY_URL"

# Create output file
OUTPUT_FILE="/tmp/sdss_vizier_test.xml"

echo -e "\n${BLUE}Executing query...${NC}"
START_TIME=$(date +%s)

# Execute query with curl
HTTP_CODE=$(curl -s -w "%{http_code}" -o "$OUTPUT_FILE" "$QUERY_URL")

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo -e "\n${YELLOW}Response Details:${NC}"
echo "  HTTP Status Code: $HTTP_CODE"
echo "  Response Time: ${ELAPSED}s"
echo "  Output saved to: $OUTPUT_FILE"

# Check HTTP status
if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "  Status: ${GREEN}✓ Success${NC}"
else
    echo -e "  Status: ${RED}✗ Failed${NC}"
    echo -e "\n${RED}HTTP Error - Response content:${NC}"
    cat "$OUTPUT_FILE"
    exit 1
fi

# Get file size
FILE_SIZE=$(stat -f%z "$OUTPUT_FILE" 2>/dev/null || stat -c%s "$OUTPUT_FILE" 2>/dev/null)
echo "  File Size: $FILE_SIZE bytes"

# Check if it's XML
FIRST_LINE=$(head -n 1 "$OUTPUT_FILE")
if [[ "$FIRST_LINE" == *"<?xml"* ]]; then
    echo -e "  Format: ${GREEN}✓ Valid XML${NC}"
else
    echo -e "  Format: ${RED}✗ Not XML${NC}"
    echo -e "\n${RED}First 200 chars of response:${NC}"
    head -c 200 "$OUTPUT_FILE"
    echo ""
    exit 1
fi

# Show XML structure
echo -e "\n${YELLOW}XML Structure (first 30 lines):${NC}"
head -n 30 "$OUTPUT_FILE"

# Count rows in TABLEDATA
ROW_COUNT=$(grep -c "<TR>" "$OUTPUT_FILE" 2>/dev/null || echo "0")
echo -e "\n${YELLOW}Data Analysis:${NC}"
echo "  Total rows found: $ROW_COUNT"

if [ "$ROW_COUNT" -gt 0 ]; then
    echo -e "  Status: ${GREEN}✓ Data retrieved successfully${NC}"
    
    # Show first data row
    echo -e "\n${YELLOW}First data row (raw XML):${NC}"
    grep -A 13 "<TR>" "$OUTPUT_FILE" | head -n 14
    
    # Try to extract and display first few stars
    echo -e "\n${YELLOW}First 5 stars (parsed):${NC}"
    echo "  RA          Dec         u      g      r      i      z      objID"
    echo "  ----------- ----------- ------ ------ ------ ------ ------ ------------------"
    
    # Use awk to parse the VOTable (basic parsing)
    awk '
        /<TR>/ { in_row=1; field=0; row_count++; }
        /<TD>/ { 
            if (in_row) {
                gsub(/<TD>|<\/TD>/, "");
                gsub(/^[[:space:]]+|[[:space:]]+$/, "");
                if (field == 0) printf "  %-11s ", $0;      # RA
                else if (field == 1) printf "%-11s ", $0;   # Dec
                else if (field == 2) printf "%-6s ", $0;    # umag
                else if (field == 4) printf "%-6s ", $0;    # gmag
                else if (field == 6) printf "%-6s ", $0;    # rmag
                else if (field == 8) printf "%-6s ", $0;    # imag
                else if (field == 10) printf "%-6s ", $0;   # zmag
                else if (field == 12) printf "%-18s", $0;   # objID
                field++;
            }
        }
        /<\/TR>/ { 
            if (in_row) {
                printf "\n";
                in_row=0;
                if (row_count >= 5) exit;
            }
        }
    ' "$OUTPUT_FILE"
    
    echo ""
    echo -e "\n${GREEN}✓ SDSS VizieR query successful!${NC}"
    echo -e "${GREEN}  Retrieved $ROW_COUNT stars from SDSS DR12${NC}"
    
else
    echo -e "  Status: ${RED}✗ No data found${NC}"
    echo -e "\n${YELLOW}Checking for error messages:${NC}"
    grep -i "error\|exception\|info" "$OUTPUT_FILE" || echo "  No error messages found"
    
    echo -e "\n${YELLOW}Full XML content:${NC}"
    cat "$OUTPUT_FILE"
fi

echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}Test Complete${NC}"
echo -e "${BLUE}========================================${NC}"

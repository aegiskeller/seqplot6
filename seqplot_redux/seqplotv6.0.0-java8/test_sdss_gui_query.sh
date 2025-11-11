#!/bin/bash
# Test script that mimics EXACTLY what the GUI does for SDSS queries
# This uses the same query structure as DataConnector.getSdssData()

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}SDSS DR12 GUI Query Test${NC}"
echo -e "${BLUE}Mimics DataConnector.getSdssData()${NC}"
echo -e "${BLUE}========================================${NC}"

# GUI parameters - typical values from EnterStar dialog
CENTRAL_RA="${1:-202.469}"      # getCentralRA()
CENTRAL_DEC="${2:-47.195}"      # getCentralDec()
FIELD_SIZE="${3:-0.25}"         # getFieldSize() - field diameter in degrees
LIMITING_MAG="${4:-16.0}"       # getLimitingMag()

# Calculate exactly as GUI does
BOX_SIZE_DEG="$FIELD_SIZE"      # boxSizeDeg = this.getFieldSize()
G_MAG_LIMIT=$(echo "$LIMITING_MAG + 1.0" | bc)  # gMagLimit = this.getLimitingMag() + 1.0

echo -e "\n${YELLOW}GUI Parameters (from DataConnector):${NC}"
echo "  getCentralRA(): $CENTRAL_RA"
echo "  getCentralDec(): $CENTRAL_DEC"
echo "  getFieldSize(): $FIELD_SIZE (field diameter in degrees)"
echo "  getLimitingMag(): $LIMITING_MAG"
echo ""
echo "  Calculated:"
echo "    boxSizeDeg = $BOX_SIZE_DEG"
echo "    gMagLimit = $G_MAG_LIMIT"

# TAP URL - exactly as CDSMirrorSelector returns
TAP_URL="http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"

# Build ADQL query EXACTLY as GUI does (from lines 2535-2546)
# Note: GUI uses %.6f for RA/Dec and %.1f for magnitude
read -r -d '' ADQL_QUERY <<EOF
SELECT TOP 5000 RA_ICRS, DE_ICRS, umag, e_umag, gmag, e_gmag, rmag, e_rmag, imag, e_imag, zmag, e_zmag, objID FROM "V/147/sdss12" WHERE 1=CONTAINS(POINT('ICRS', RA_ICRS, DE_ICRS), BOX('ICRS', $CENTRAL_RA, $CENTRAL_DEC, $BOX_SIZE_DEG, $BOX_SIZE_DEG)) AND umag IS NOT NULL AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL AND gmag <= $G_MAG_LIMIT
EOF

echo -e "\n${YELLOW}ADQL Query (exact GUI format):${NC}"
echo "$ADQL_QUERY"

# URL encode the query
ENCODED_QUERY=$(printf '%s' "$ADQL_QUERY" | jq -sRr @uri)

# Build full query URL exactly as GUI does (line 2549-2550)
QUERY_URL="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED_QUERY}"

echo -e "\n${YELLOW}Query URL:${NC}"
echo "${QUERY_URL:0:200}..."
echo "  (truncated for display)"

# Create output file
OUTPUT_FILE="/tmp/sdss_gui_test.xml"

echo -e "\n${BLUE}Executing query (as GUI does)...${NC}"
START_TIME=$(date +%s)

# Execute query with curl (GUI uses HttpURLConnection but result is same)
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
    echo -e "  Status: ${RED}✗ Failed (HTTP $HTTP_CODE)${NC}"
    echo -e "\n${RED}Response content:${NC}"
    head -50 "$OUTPUT_FILE"
    exit 1
fi

# Get file size
FILE_SIZE=$(stat -f%z "$OUTPUT_FILE" 2>/dev/null || stat -c%s "$OUTPUT_FILE" 2>/dev/null)
echo "  File Size: $FILE_SIZE bytes"

# Check if it's XML
FIRST_LINE=$(head -n 1 "$OUTPUT_FILE")
if [[ "$FIRST_LINE" == *"<?xml"* ]]; then
    echo -e "  Format: ${GREEN}✓ Valid XML VOTable${NC}"
else
    echo -e "  Format: ${RED}✗ Not XML${NC}"
    echo -e "\n${RED}Response (first 500 chars):${NC}"
    head -c 500 "$OUTPUT_FILE"
    echo ""
    exit 1
fi

# Count rows in TABLEDATA
ROW_COUNT=$(grep -c "<TR>" "$OUTPUT_FILE" 2>/dev/null || echo "0")
echo -e "\n${YELLOW}Data Analysis:${NC}"
echo "  Total <TR> rows found: $ROW_COUNT"

if [ "$ROW_COUNT" -gt 0 ]; then
    echo -e "  Status: ${GREEN}✓ Data retrieved successfully${NC}"
    
    # Show first 3 stars with full details
    echo -e "\n${YELLOW}First 3 stars (as GUI would parse):${NC}"
    echo "  #  RA_ICRS     DE_ICRS     u      g      r      i      z      objID"
    echo "  -- ----------- ----------- ------ ------ ------ ------ ------ ------------------"
    
    awk '
        /<TR>/ { 
            in_row=1; 
            field=0; 
            row_count++; 
            printf "  %-2d ", row_count;
        }
        /<TD>/ { 
            if (in_row) {
                gsub(/<TD>|<\/TD>/, "");
                gsub(/^[[:space:]]+|[[:space:]]+$/, "");
                if (field == 0) printf "%-11s ", $0;       # RA_ICRS
                else if (field == 1) printf "%-11s ", $0;  # DE_ICRS
                else if (field == 2) printf "%-6s ", $0;   # umag
                else if (field == 4) printf "%-6s ", $0;   # gmag
                else if (field == 6) printf "%-6s ", $0;   # rmag
                else if (field == 8) printf "%-6s ", $0;   # imag
                else if (field == 10) printf "%-6s ", $0;  # zmag
                else if (field == 12) printf "%-18s", $0;  # objID
                field++;
            }
        }
        /<\/TR>/ { 
            if (in_row) {
                printf "\n";
                in_row=0;
                if (row_count >= 3) exit;
            }
        }
    ' "$OUTPUT_FILE"
    
    echo ""
    
    # Simulate GUI photometric transformation for first star
    echo -e "\n${YELLOW}Photometric Transformation (first star):${NC}"
    awk '
        /<TR>/ { in_row=1; field=0; row_count++; }
        /<TD>/ { 
            if (in_row && row_count == 1) {
                gsub(/<TD>|<\/TD>/, "");
                gsub(/^[[:space:]]+|[[:space:]]+$/, "");
                data[field++] = $0;
            }
        }
        /<\/TR>/ { 
            if (in_row && row_count == 1) {
                u = data[2];
                g = data[4];
                r = data[6];
                i = data[8];
                
                # SDSS to Johnson-Cousins transformations (from parseSdssVOTable)
                u_g = u - g;
                g_r = g - r;
                r_i = r - i;
                
                U_jc = u - 0.0316 * u_g - 0.7487;
                B_jc = u - 0.8116 * u_g + 0.1313;
                V_jc = g - 0.5784 * g_r - 0.0038;
                R_jc = r - 0.1837 * g_r - 0.0971;
                I_jc = r - 1.2444 * r_i - 0.3820;
                
                BV = B_jc - V_jc;
                
                printf "  SDSS magnitudes: u=%.3f g=%.3f r=%.3f i=%.3f\n", u, g, r, i;
                printf "  Color indices: (u-g)=%.3f (g-r)=%.3f (r-i)=%.3f\n", u_g, g_r, r_i;
                printf "  Transformed to Johnson-Cousins:\n";
                printf "    U=%.3f B=%.3f V=%.3f R=%.3f I=%.3f\n", U_jc, B_jc, V_jc, R_jc, I_jc;
                printf "    B-V=%.3f\n", BV;
                
                exit;
            }
        }
    ' "$OUTPUT_FILE"
    
    echo ""
    echo -e "\n${GREEN}✓ SDSS VizieR query successful!${NC}"
    echo -e "${GREEN}  Retrieved $ROW_COUNT stars from SDSS DR12${NC}"
    echo -e "${GREEN}  This is what the GUI would receive and process${NC}"
    
else
    echo -e "  Status: ${RED}✗ No data found (empty TABLEDATA)${NC}"
    
    # Check for coverage issue
    if (( $(echo "$CENTRAL_DEC < -10" | bc -l) )); then
        echo -e "\n${YELLOW}⚠ Coverage Issue Detected:${NC}"
        echo "  Field center Dec=$CENTRAL_DEC° is south of SDSS coverage"
        echo "  SDSS primarily covers northern hemisphere (Dec > -10°)"
        echo "  This matches the GUI's coverage check logic"
    fi
    
    echo -e "\n${YELLOW}Checking VOTable for INFO messages:${NC}"
    grep -i "<INFO" "$OUTPUT_FILE" | head -5
    
    echo -e "\n${YELLOW}TABLEDATA section:${NC}"
    grep -A 3 "<TABLEDATA>" "$OUTPUT_FILE"
fi

echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}Test Complete${NC}"
echo -e "${BLUE}========================================${NC}"

#!/bin/bash
# Test SDSS DR12 query for M51 field
# Parameters: RA=202.469, Dec=47.195, Field=0.25°, Mag Limit=16.0

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}SDSS DR12 Query: M51 Field${NC}"
echo -e "${BLUE}========================================${NC}"

# M51 parameters
RA="202.469"
DEC="47.195"
FIELD_SIZE="0.25"
MAG_LIMIT="16.0"
G_MAG_LIMIT="17.0"  # Add 1.0 buffer as GUI does

echo -e "\n${YELLOW}Field Parameters:${NC}"
echo "  Object: M51 (Whirlpool Galaxy)"
echo "  RA: $RA degrees"
echo "  Dec: $DEC degrees"
echo "  Field Size: $FIELD_SIZE degrees (box)"
echo "  Magnitude Limit (V): $MAG_LIMIT"
echo "  G-band Limit: $G_MAG_LIMIT (with buffer)"

# TAP endpoint
TAP_URL="http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync"

# Build ADQL query exactly as GUI does
ADQL_QUERY="SELECT TOP 5000 RA_ICRS, DE_ICRS, umag, e_umag, gmag, e_gmag, rmag, e_rmag, imag, e_imag, zmag, e_zmag, objID FROM \"V/147/sdss12\" WHERE 1=CONTAINS(POINT('ICRS', RA_ICRS, DE_ICRS), BOX('ICRS', $RA, $DEC, $FIELD_SIZE, $FIELD_SIZE)) AND umag IS NOT NULL AND gmag IS NOT NULL AND rmag IS NOT NULL AND imag IS NOT NULL AND gmag <= $G_MAG_LIMIT"

echo -e "\n${YELLOW}ADQL Query:${NC}"
echo "$ADQL_QUERY"

# URL encode
ENCODED_QUERY=$(printf '%s' "$ADQL_QUERY" | jq -sRr @uri)
QUERY_URL="${TAP_URL}?REQUEST=doQuery&LANG=ADQL&FORMAT=votable&QUERY=${ENCODED_QUERY}"

OUTPUT_FILE="/tmp/sdss_m51_test.xml"

echo -e "\n${BLUE}Executing query...${NC}"
START_TIME=$(date +%s)

HTTP_CODE=$(curl -s -w "%{http_code}" -o "$OUTPUT_FILE" "$QUERY_URL")

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo -e "\n${YELLOW}Response:${NC}"
echo "  HTTP Status: $HTTP_CODE"
echo "  Time: ${ELAPSED}s"
echo "  Output: $OUTPUT_FILE"

if [ "$HTTP_CODE" -ne 200 ]; then
    echo -e "  ${RED}✗ Failed${NC}"
    head -50 "$OUTPUT_FILE"
    exit 1
fi

FILE_SIZE=$(stat -f%z "$OUTPUT_FILE" 2>/dev/null || stat -c%s "$OUTPUT_FILE" 2>/dev/null)
echo "  Size: $FILE_SIZE bytes"

# Check format
if head -n 1 "$OUTPUT_FILE" | grep -q "<?xml"; then
    echo -e "  Format: ${GREEN}✓ Valid XML${NC}"
else
    echo -e "  Format: ${RED}✗ Not XML${NC}"
    exit 1
fi

# Count stars
ROW_COUNT=$(grep -c "<TR>" "$OUTPUT_FILE" 2>/dev/null || echo "0")

echo -e "\n${YELLOW}Results:${NC}"
echo "  Stars found: $ROW_COUNT"

if [ "$ROW_COUNT" -gt 0 ]; then
    echo -e "  Status: ${GREEN}✓ Success${NC}"
    
    # Parse and display stars
    echo -e "\n${YELLOW}Stars in M51 Field (SDSS DR12):${NC}"
    echo "  #  RA          Dec         u      g      r      i      z      U(JC)  B(JC)  V(JC)  R(JC)  I(JC)  B-V    objID"
    echo "  -- ----------- ----------- ------ ------ ------ ------ ------ ------ ------ ------ ------ ------ ------ ------------------"
    
    # Parse VOTable and compute transformations
    awk '
        BEGIN { star_num = 0; }
        /<TR>/ { in_row=1; field=0; }
        /<TD>/ { 
            if (in_row) {
                gsub(/<TD>|<\/TD>/, "");
                gsub(/^[[:space:]]+|[[:space:]]+$/, "");
                data[field++] = $0;
            }
        }
        /<\/TR>/ { 
            if (in_row && field >= 13) {
                star_num++;
                
                # Extract SDSS magnitudes
                ra = data[0];
                dec = data[1];
                u = data[2];
                g = data[4];
                r = data[6];
                i = data[8];
                z = data[10];
                objid = data[12];
                
                # Compute color indices
                u_g = u - g;
                g_r = g - r;
                r_i = r - i;
                
                # Transform to Johnson-Cousins (Jester et al. 2005)
                U_jc = u - 0.0316 * u_g - 0.7487;
                B_jc = u - 0.8116 * u_g + 0.1313;
                V_jc = g - 0.5784 * g_r - 0.0038;
                R_jc = r - 0.1837 * g_r - 0.0971;
                I_jc = r - 1.2444 * r_i - 0.3820;
                
                BV = B_jc - V_jc;
                
                printf "  %-2d %-11s %-11s %-6s %-6s %-6s %-6s %-6s %-6.2f %-6.2f %-6.2f %-6.2f %-6.2f %-6.2f %s\n",
                    star_num, ra, dec, u, g, r, i, z, U_jc, B_jc, V_jc, R_jc, I_jc, BV, objid;
                
                # Store for summary
                if (star_num == 1) {
                    v_min = V_jc; v_max = V_jc;
                    bv_min = BV; bv_max = BV;
                } else {
                    if (V_jc < v_min) v_min = V_jc;
                    if (V_jc > v_max) v_max = V_jc;
                    if (BV < bv_min) bv_min = BV;
                    if (BV > bv_max) bv_max = BV;
                }
            }
            in_row=0;
        }
        END {
            if (star_num > 0) {
                printf "\n";
                printf "  Summary:\n";
                printf "    Total stars: %d\n", star_num;
                printf "    V magnitude range: %.2f to %.2f\n", v_min, v_max;
                printf "    B-V color range: %.2f to %.2f\n", bv_min, bv_max;
            }
        }
    ' "$OUTPUT_FILE"
    
    echo ""
    echo -e "${GREEN}✓ Query successful!${NC}"
    echo -e "${GREEN}  Found $ROW_COUNT stars in M51 field${NC}"
    
else
    echo -e "  Status: ${RED}✗ No data found${NC}"
    echo -e "\n${YELLOW}TABLEDATA section:${NC}"
    grep -A 2 "<TABLEDATA>" "$OUTPUT_FILE"
fi

echo -e "\n${BLUE}========================================${NC}"

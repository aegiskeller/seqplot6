# SDSS-DR12 Query Debugging

## Changes Made for Debugging

### 1. Source Number Updated
- Changed SDSS source number from 51 to **21** in EnterStar.java (line 202)
- Label now shows: "SDSS-DR12 (21)"

### 2. Enhanced Debug Output in getSdssData() Method

The query method now shows:

1. **SQL Query Details**
   - Displays the full SQL query before URL encoding
   - Shows POST URL and parameters
   - Shows encoded SQL length

2. **Response Analysis**
   - Prints HTTP response code
   - Shows first 500 characters of SDSS response
   - Helps identify errors or empty results

3. **CSV Parsing Details**
   - Shows response length in characters
   - Displays first 300 characters of response
   - Shows header line
   - Prints total lines in response
   - For each data line: shows field count and first 80 characters

### 3. Debug Console Output When Running SDSS Query

You will see output like:

```
========== SDSS DR12 CATALOG LOADING ==========
Service: SDSS DR12 SQL
Field: RA=179.500000, Dec=+60.000000, Radius=60.00 arcmin, MagLimit=15.0

SQL Query (before encoding):
SELECT s.objid, s.ra, dbo.fHMS(s.ra) as HMSra, s.dec, dbo.fDMS(s.dec) as DMSdec, 
(s.u - 0.0316*(s.u - s.g) - 0.7487) as U, ...
[rest of query]

POST data being sent:
  URL: http://skyserver.sdss.org/dr12/en/tools/search/sql.aspx
  Method: POST
  Parameters: cmd=[encoded SQL]&format=csv
  Encoded SQL length: 1234 characters

Response code: 200

First 500 chars of SDSS response:
[response preview]

=== PARSING SDSS CSV RESPONSE ===
Total response length: 5432 characters
First 300 chars of response:
[response preview]
---
Total lines in response: 45
Header line: objid,ra,HMSra,dec,DMSdec,U,B,V,R,I,VmagDifference
Line 2: 11 fields - [first 80 chars of data]
...
```

## Testing SDSS Query

To test SDSS-DR12:

1. Open the Request Star dialog
2. Enter a location (e.g., RA=180, Dec=60)
3. Set Field Size (e.g., 2 degrees)
4. Select **SDSS-DR12 (21)** checkbox
5. Click "Search"
6. Check the console output for debugging information

## Common Issues & Solutions

### No targets returned

Possible causes:
1. **Field location has no SDSS coverage** - SDSS doesn't cover the entire sky
2. **SQL syntax error** - Check the displayed SQL query
3. **Magnitude constraints too restrictive** - Query filters on specific color range
4. **SDSS server down** - Try again later

### Check the displayed SQL query

The exact SQL being sent to SDSS is printed to console. You can:
- Copy it and test directly on SDSS website
- Verify coordinate format (RA/Dec in degrees)
- Check magnitude constraints
- Verify function names (dbo.fGetNearbyObjEq, etc.)

## SQL Query Details

The current SDSS query:

```sql
SELECT s.objid, s.ra, dbo.fHMS(s.ra) as HMSra, s.dec, dbo.fDMS(s.dec) as DMSdec, 
(s.u - 0.0316*(s.u - s.g) - 0.7487) as U, 
(s.u - 0.8116*(s.u - s.g) + 0.1313) as B, 
(s.g - 0.5784*(s.g - s.r) - 0.0038) as V, 
(s.r - 0.1837*(s.g - s.r) - 0.0971) as R, 
(s.r - 1.2444*(s.r - s.i) - 0.3820) as I, 
((s.g - 0.5784*(s.g - s.r) - 0.0038) - {minMagnitude}) as VmagDifference 
FROM star s, dbo.fGetNearbyObjEq({RA}, {Dec}, {radius}) f 
WHERE s.objid = f.objid 
AND (0.1884*s.u + 0.39*s.g - 0.5784*s.r + 0.1351) between 0.3 and 1 
AND (s.g - 0.5784*(s.g - s.r) - 0.0038) < 19 
ORDER BY ((s.g - 0.5784*(s.g - s.r) - 0.0038) - {minMagnitude})
```

Key points:
- Uses **T-SQL** (not ADQL)
- Searches within a radius using `dbo.fGetNearbyObjEq()`
- Applies color constraints: star color index between 0.3 and 1
- Applies magnitude constraint: V < 19
- Orders results by magnitude

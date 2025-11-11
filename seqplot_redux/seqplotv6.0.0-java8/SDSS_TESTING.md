# SDSS-DR12 Testing Guide

## Current Status

✅ **SDSS-DR12 integration is complete and compiled**
- Source number: **21** (updated from 51)
- Debug logging: **Enhanced** with detailed SQL query and response debugging
- Method: Uses SDSS SQL endpoint at http://skyserver.sdss.org/dr12/

## How to Test SDSS-DR12

### Step 1: Open the Application
The app should already be running. If not:
```bash
cd /Users/aegiskeller/Documents/seqplot_redux/seqplotv6.0.0
bash run.sh > /tmp/seqplot_debug.log 2>&1 &
```

### Step 2: Open Request Star Dialog
- Click on "Tools" menu
- Look for "Request Star" or open the star search dialog

### Step 3: Select SDSS-DR12
- Find the checkbox labeled **"SDSS-DR12 (21)"**
- Check the checkbox to enable SDSS as a data source

### Step 4: Enter Search Parameters
Example 1 - Well-covered region (easier to get results):
- Star: Leave empty or enter coordinates manually
- Central RA: **180.0** degrees (12h00m)
- Central Dec: **60.0** degrees
- Field Size: **2.0** degrees (120 arcmin)
- Limiting Magnitude: **15.0**

Example 2 - Another region:
- Central RA: **120.0** degrees (08h00m)
- Central Dec: **-30.0** degrees
- Field Size: **1.0** degree
- Limiting Magnitude: **14.0**

### Step 5: Execute Query
- Click "Search" or equivalent button

### Step 6: Monitor Debug Output
Watch `/tmp/seqplot_debug.log` for debugging output:

```bash
# In a new terminal:
tail -f /tmp/seqplot_debug.log
```

You should see output like:

```
========== SDSS DR12 CATALOG LOADING ==========
Service: SDSS DR12 SQL
Field: RA=180.000000, Dec=60.000000, Radius=60.00 arcmin, MagLimit=15.0

SQL Query (before encoding):
SELECT s.objid, s.ra, dbo.fHMS(s.ra) as HMSra, s.dec, dbo.fDMS(s.dec) as DMSdec, 
(s.u - 0.0316*(s.u - s.g) - 0.7487) as U, 
...

POST data being sent:
  URL: http://skyserver.sdss.org/dr12/en/tools/search/sql.aspx
  Method: POST
  Parameters: cmd=[encoded SQL]&format=csv
  Encoded SQL length: 1234 characters

Response code: 200

First 500 chars of SDSS response:
objid,ra,HMSra,dec,DMSdec,U,B,V,R,I,VmagDifference
...

=== PARSING SDSS CSV RESPONSE ===
Total response length: 5432 characters
Total lines in response: 45
Line 2: 11 fields - 1237662062657110016,180.12345,12:00:49.65,+60.23456,+60:14:04.42,11.5,12.3,13.1,13.8,14.2,1.9
...
```

## Debugging Output Explained

### Response Code
- **200** = Success (query was processed)
- **4xx** = Client error (invalid query syntax, bad parameters)
- **5xx** = Server error (SDSS server having issues)

### CSV Response
- **First line** = Column headers
- **Subsequent lines** = Data rows
- **11 fields per row** = objid, ra, HMSra, dec, DMSdec, U, B, V, R, I, VmagDifference

### Common Scenarios

#### ✅ Success - Data Returned
```
Response code: 200
Total lines in response: 45
Line 2: 11 fields - [data]
Line 3: 11 fields - [data]
...
```
→ SDSS found ~44 stars in the field

#### ❌ No Data - Empty Result
```
Response code: 200
Total lines in response: 1
```
→ Query was valid but found no objects (check field location, may be outside SDSS coverage)

#### ❌ Error - Invalid Query
```
Response code: 400
First 500 chars of SDSS response:
Error: SQL parsing error near 'objid'
...
```
→ SQL syntax error - check the displayed SQL query

#### ❌ Server Error
```
Response code: 503
```
→ SDSS server is down, try again later

## Field Coverage Notes

SDSS DR12 covers:
- **RA**: 0° to 360°
- **Dec**: -5° to +90° (roughly)
- **Not covered**: Southern hemisphere (Dec < -5°)

Good test coordinates:
- RA=180°, Dec=60° (North Galactic Pole region)
- RA=120°, Dec=0° (Equator region)
- RA=45°, Dec=45° (High declination)

Avoid:
- Dec < -10° (outside coverage)
- Galactic plane (may be saturated or truncated)

## SQL Query Details

The query searches for:
- Objects within a specified radius
- With valid u, g, r, i photometry
- With color index in range [0.3, 1]
- With V magnitude < 19
- Transformed to Johnson-Cousins system

See SDSS_DEBUG_NOTES.md for full query details.

## Next Steps if No Data

1. **Check the displayed SQL query** in the log
2. **Copy the SQL** and test on [SDSS website](http://skyserver.sdss.org/dr12/en/tools/search/sql.aspx)
3. **Verify coordinates** are in degrees
4. **Check coverage** - may need different field center
5. **Adjust magnitude constraints** in the query
6. **Check SDSS server status** - may be temporarily down

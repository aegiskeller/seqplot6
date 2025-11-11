# Debug Log Reference

## Current Debug Log Status

**Location:** `/tmp/seqplot_debug.log`
**Size:** 25 lines
**Last Update:** After "AQ Peg" VSX query

## Current Log Content

```
=== Launching Seqplot 6.0.0 ===
Enhanced Astronomical Sequence Plotting Application
Features: Warm pastel colors, unified visual styling, modern coordinates

🚀 Starting Seqplot 6.0.0...

========== VSX API RESPONSE FOR: aq peg ==========
  Category: Variable
  Period: 5.5485028
  Epoch: 2441222.7048
  OID: 25192
  EclipseDuration: 12
  ProperMotionDec: -5.6930
  Name: AQ Peg
  AUID: 000-BCP-096
  Declination2000: 13.47458
  RA2000: 324.33713
  Constellation: Peg
  SpectralType: A2e+G5
  VariabilityType: EA/SD
  MaxMag: 10.39 V
  ProperMotionRA: -1.4110
  MinMag: 12.85 V
===============================================
```

## What This Means

✅ **Application started successfully**
- Seqplot 6.0.0 is running
- All startup features loaded

✅ **VSX lookup working**
- User searched for "AQ Peg"
- VSX database returned full star information
- Variable star classification: EA/SD (Eclipsing Binary)

⏳ **Awaiting SDSS Test**
- No SDSS queries have been executed yet
- SDSS will only run when user:
  1. Opens Request Star dialog
  2. Checks "SDSS-DR12 (21)" checkbox
  3. Clicks Search

## Expected Output When SDSS Query Runs

Once SDSS query is triggered, the log will include:

```
========== SDSS DR12 CATALOG LOADING ==========
Service: SDSS DR12 SQL
Field: RA=..., Dec=..., Radius=... arcmin, MagLimit=...

SQL Query (before encoding):
SELECT s.objid, s.ra, dbo.fHMS(s.ra) as HMSra, ...
[rest of query]

POST data being sent:
  URL: http://skyserver.sdss.org/dr12/en/tools/search/sql.aspx
  Method: POST
  Parameters: cmd=[encoded SQL]&format=csv
  Encoded SQL length: XXXX characters

Connecting to SDSS... Response code: 200
✓ Downloaded X,XXX bytes in X.XX sec (X.XX KB/s)

First 500 chars of SDSS response:
objid,ra,HMSra,dec,DMSdec,U,B,V,R,I,VmagDifference
1237662062657110016,180.12345,...

=== PARSING SDSS CSV RESPONSE ===
Total response length: XXXX characters
First 300 chars of response:
objid,ra,HMSra,dec,DMSdec,U,B,V,R,I,VmagDifference
...

Total lines in response: XX
Header line: objid,ra,HMSra,dec,DMSdec,U,B,V,R,I,VmagDifference
Line 2: 11 fields - 1237662062657110016,180.12345,...
Line 3: 11 fields - ...
...
DEBUG: Found XX data lines in SDSS response
✓ Found XX stars in X.XX sec
Converting photometry and preparing plot... ✓ Completed in X.XX sec
TOTAL TIME: X.XX sec
============================================
```

## How to Monitor Log

### Option 1: Live tail (recommended)
```bash
tail -f /tmp/seqplot_debug.log
```
Stops with Ctrl+C

### Option 2: View entire log
```bash
cat /tmp/seqplot_debug.log
```

### Option 3: Search for SDSS output
```bash
grep -i "sdss\|parsing\|response code" /tmp/seqplot_debug.log
```

### Option 4: Check file size over time
```bash
while true; do echo "$(date): $(wc -l < /tmp/seqplot_debug.log) lines"; sleep 5; done
```

## Interpretation Guide

### Response Code 200
✅ Success - SQL query was accepted and processed

### Response Shows Column Headers
✅ Success - SDSS returned data in expected format

### Response Shows Multiple Rows
✅ Success - Found stars in the field

### Single Line Response (just header)
⚠️ No data - Field may be outside SDSS coverage or no matching objects

### Response Code 400+
❌ Error - Check SQL query syntax or parameters

### Connection timeout
❌ Network issue - SDSS server may be slow or down

## Next Test Steps

1. Keep log monitoring: `tail -f /tmp/seqplot_debug.log` in one terminal
2. In application: Select SDSS-DR12 (21) and enter coordinates
3. Watch log for SDSS output in real-time
4. Review the debug information to verify query and response

## Known Good Coordinates for Testing

### High Declination (Good Coverage)
- RA: 180.0 degrees (12h00m)
- Dec: 60.0 degrees
- Field: 2.0 degrees
- Expected: 20-40 stars

### Galactic Equator
- RA: 120.0 degrees (08h00m)
- Dec: 0.0 degrees
- Field: 1.0 degree
- Expected: 30-50 stars (crowded)

### Avoid (Outside Coverage)
- Dec < -10 degrees (Southern hemisphere)
- Heavily saturated Galactic plane areas

## Files for Reference

- `SDSS_TESTING.md` - Complete testing guide
- `SDSS_DEBUG_NOTES.md` - Debug output explanation
- `SDSS_INTEGRATION_STATUS.md` - Implementation details

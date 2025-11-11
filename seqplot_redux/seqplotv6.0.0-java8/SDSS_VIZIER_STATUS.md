# SDSS VizieR Conversion Status

## Completed
- ✅ Converted getSdssData() to use VizieR TAP (V/147/sdss12)
- ✅ Replaced parseSdssCSV() with parseSdssVOTable()
- ✅ Updated loadSdssSecondary() to use VizieR TAP
- ✅ Added coverage warning for southern hemisphere
- ✅ CLI test script confirms VizieR works (7 stars from M51)

## Issue Found
CLI test with M51 (RA=202.469, Dec=47.195) returns 7 stars.
GUI may be generating different query - need to test with actual GUI query.

## Next Steps
1. Run GUI and capture actual ADQL query from debug output
2. Test that exact query in CLI script
3. Compare differences between CLI and GUI queries

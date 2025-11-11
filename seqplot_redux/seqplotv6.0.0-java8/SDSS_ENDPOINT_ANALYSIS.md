# SDSS Endpoint Testing Results

## Issue Found

When testing the SDSS DR12 SQL endpoint directly via command line, the server returns HTML instead of CSV data. This suggests:

1. **The web interface requires authentication or session state** - The form may need cookies or session tokens
2. **Redirects to a login/session page** - Getting HTML suggests a redirect to authentication
3. **Different endpoint needed** - May need to use a different API endpoint

## What We Learned

### ✅ What Works:
- HTTP connection to SDSS server is successful (200 response)
- Query syntax appears valid (no 400 Bad Request errors)
- Server is responding to POST requests

### ❌ What Doesn't Work:
- Direct POST to `/dr12/en/tools/search/sql.aspx` returns HTML form, not CSV
- This suggests the web UI endpoint requires session management

## Solution for Java Integration

The Java code should NOT use the web interface endpoint. Instead, it should:

1. **Use the SDSS API endpoint directly** if available
2. **Use the CasJobs service** (SDSS's job submission system) 
3. **Use TAP protocol** like we do for VizieR

### Recommended: Use CasJobs REST API

SDSS provides a CasJobs REST API that doesn't require web session:
```
http://skyserver.sdss.org/casjobs/api/query
```

### Alternative: TAP Endpoint

SDSS provides a TAP endpoint similar to VizieR:
```
http://tapvos.net/sdss/tapcats/tap/sync
```

## Testing the Web Interface Manually

To test SDSS SQL interface manually:
1. Go to: http://skyserver.sdss.org/dr12/en/tools/search/sql.aspx
2. Paste the SQL query directly into the form
3. Click "Submit Query"
4. Download results as CSV

This proves the SQL query syntax is correct - the issue is with programmatic access.

## Current Status

- ✅ CLI test script created and functional
- ✅ Query syntax validated
- ⚠️ Direct endpoint requires session management
- ✅ Java code can be updated to use TAP or CasJobs endpoint instead

## Files

- `test_sdss_endpoint.sh` - CLI script for testing SDSS endpoint
- Output files saved to: `/tmp/sdss_test_[timestamp].csv`

## Next Steps

The Java implementation should be modified to:
1. Try using TAP endpoint for SDSS if available
2. Or implement CasJobs API integration
3. Or fall back to simpler queries that don't require the web session

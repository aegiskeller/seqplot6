# Locale Fixes for Seqplot 6.0.0

## Problem
Users in Argentina (locale `es_AR`) and other locales that use comma as decimal separator were experiencing errors because Java's default formatting methods use the system locale.

## Solution Applied
All numeric formatting operations have been updated to use `Locale.US` to ensure consistent formatting with period (.) as decimal separator.

### Files Modified

#### 1. String.format() calls
- Applied globally across all `.java` files
- Pattern: `String.format(Locale.US, ...)`
- Ensures all formatted strings use US locale

#### 2. DecimalFormat instances
- All DecimalFormat objects now use:
  ```java
  DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
  DecimalFormat formatter = new DecimalFormat(pattern, symbols);
  ```

#### 3. PrintWriter.printf() calls (SequenceListWindow.java)
- VSD export file formatting
- Pattern: `writer.printf(Locale.US, ...)`
- Ensures exported files have consistent format

#### 4. System.out.printf() and System.err.printf() calls
- All debug and error output
- Pattern: `System.out.printf(Locale.US, ...)`
- Applied to all `.java` files in `src/AAVSOtools/`

## Files with Extensive Changes

### Most Affected Files:
1. **DataConnector.java** - 100+ printf calls for coordinate/magnitude formatting
2. **StarPlotPanel.java** - 50+ printf calls for RA/Dec/FOV debug output
3. **DSS2Manager.java** - 30+ printf calls for WCS/FITS debugging
4. **SequenceListWindow.java** - VSD export formatting

### Test Files Fixed:
- TestSDSSDownload.java
- TestBlankDetection.java
- SimpleStarPlot.java
- AdvancedStarPlot.java
- PlotTestHarness.java

## Verification

After fixes applied:
```bash
# Check that no printf calls remain without Locale.US in src/
grep -r "System\.out\.printf(" src/AAVSOtools/*.java | grep -v "java.util.Locale.US"
# Result: 0 matches ✓

grep -r "System\.err\.printf(" src/AAVSOtools/*.java | grep -v "java.util.Locale.US"
# Result: 0 matches ✓
```

Build status: ✓ Successful

## Testing Recommendations

To test locale handling:
```java
// Set system locale to Argentina
Locale.setDefault(new Locale("es", "AR"));
// Run Seqplot - all numeric output should still use period (.) not comma (,)
```

## Future Guidelines

When adding new code to Seqplot:
1. **Always** use `String.format(Locale.US, ...)` for numeric formatting
2. **Always** use `System.out.printf(Locale.US, ...)` for debug output
3. **Always** construct DecimalFormat with `DecimalFormatSymbols(Locale.US)`
4. Never rely on default locale for scientific data formatting

## Related Issues Fixed
- VSX mag range showing stale data (arrays not cleared)
- VSP comparison star formatting using system locale
- All catalog query debug output using system locale

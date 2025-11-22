# Gaia DR2 Photometric Transformations

## Overview
This document describes the photometric transformations applied to Gaia DR2 data to convert from the Gaia photometric system (G, BP, RP) to the standard Johnson-Cousins photometric system (V, B, R, I).

## Transformation Equations

The transformations are based on **Evans et al. 2018** (A&A 616, A4, 2018), Section A2, which provides polynomial relationships for converting Gaia DR2 magnitudes to Johnson-Cousins magnitudes.

Let X = BP - RP (the Gaia color index)

### V Magnitude
```
V = G - (-0.01760 - 0.006860·X - 0.1732·X²)
V = G + 0.01760 + 0.006860·X + 0.1732·X²
```

### R Magnitude  
```
R = G - (-0.003226 + 0.3833·X - 0.1345·X²)
R = G + 0.003226 - 0.3833·X + 0.1345·X²
```

### I Magnitude
```
I = G - (-0.02085 + 0.7419·X - 0.09631·X²)
I = G + 0.02085 - 0.7419·X + 0.09631·X²
```

## Color Indices
After transformation, the following standard color indices are calculated and stored:
- **V-I**: Primary/preferred color index for Gaia (stored in V-I field)
- **V-R**: Red color index (stored in V-R field)
- **R-I**: Near-infrared color index (stored in R-I field)
- **B-V**: Not directly available (set to 99.999/NA) since Gaia has no B-band photometry

## Error Propagation
Errors are propagated through the transformations using standard error propagation formulas:

For V magnitude:
```
σ_V² = σ_G² + (∂f/∂X)² · σ_X²
where ∂f/∂X = 0.006860 + 2·0.1732·X
```

Similar formulas apply for R, I, and color indices.

## Implementation
The transformations are implemented in `DataConnector.java` in the `parseGaiaDR2VOTable()` and `parseGaiaDR3VOTable()` methods. When Gaia DR2/DR3 catalog data is retrieved:

1. Raw G, BP, RP magnitudes are extracted from the Gaia TAP query
2. Transformations are applied using the equations above
3. Transformed V, R, I magnitudes are calculated (B magnitude is NOT calculated - no B-band data)
4. Color indices V-R, R-I, and V-I are calculated and stored in their respective fields
5. V-I is the preferred color index for Gaia
6. Source number 48 identifies Gaia DR2 data, 49 identifies Gaia DR3 data

## Validity Range
- **BP magnitude limit**: BP < 19 mag (as specified in query constraint)
- **V magnitude range**: Approximately 10 - 18.8 V mag
- **Color range**: Best results for 0.2 < BP-RP < 2.0 (main sequence stars)

## Display
When displaying Gaia DR2/DR3 data:
- **Magnitude**: Shows transformed V magnitude with error
- **Preferred Color**: V-I is shown as the primary color index in the Points View
- **V-R Color**: Also available and displayed in the Comparison Star Table
- **B-V**: Shows NA (99.999) since Gaia has no B-band photometry
- **Source**: 48 identifies Gaia DR2 data, 49 identifies Gaia DR3 data
- Stars are labeled with "Gaia_" prefix followed by source_id

### Preferred Color Logic
The application uses a "preferred color" concept to display the most appropriate color index:
- **Gaia DR2 (48), Gaia DR3 (49), PanSTARRS (46)**: V-I is the preferred color
- **All other catalogs (AAVSO, APASS, Tycho-2, etc.)**: B-V is the preferred color

This ensures optimal color information display based on the photometric system of each catalog.

## References
- Evans, D. W., et al. 2018, "Gaia Data Release 2: Photometric content and validation", A&A 616, A4
- Gaia DR2 VizieR Catalog: I/345/gaia2

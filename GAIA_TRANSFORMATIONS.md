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
After transformation, the following standard color indices are calculated:
- **V-I**: Primary color index for Gaia (stored in B-V field)
- **V-R**: Calculated from transformed V and R
- **R-I**: Calculated from transformed R and I

## Error Propagation
Errors are propagated through the transformations using standard error propagation formulas:

For V magnitude:
```
σ_V² = σ_G² + (∂f/∂X)² · σ_X²
where ∂f/∂X = 0.006860 + 2·0.1732·X
```

Similar formulas apply for R, I, and color indices.

## Implementation
The transformations are implemented in `DataConnector.java` in the `parseGaiaDR2VOTable()` method. When Gaia DR2 catalog data is retrieved:

1. Raw G, BP, RP magnitudes are extracted from the VizieR TAP query
2. Transformations are applied using the equations above
3. Transformed V, R, I magnitudes are calculated (B magnitude is NOT calculated)
4. V-I is calculated and stored as the primary color index (in the B-V field)
5. Source number 902 identifies Gaia DR2 data

## Validity Range
- **BP magnitude limit**: BP < 19 mag (as specified in query constraint)
- **V magnitude range**: Approximately 10 - 18.8 V mag
- **Color range**: Best results for 0.2 < BP-RP < 2.0 (main sequence stars)

## Display
When displaying Gaia DR2 data:
- **Magnitude**: Shows transformed V magnitude with error
- **Color**: Shows V-I color (stored in the B-V field for compatibility)
- Stars are labeled with "Gaia_" prefix followed by source_id

## References
- Evans, D. W., et al. 2018, "Gaia Data Release 2: Photometric content and validation", A&A 616, A4
- Gaia DR2 VizieR Catalog: I/345/gaia2

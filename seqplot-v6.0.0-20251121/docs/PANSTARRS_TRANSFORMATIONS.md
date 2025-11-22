# PanSTARRS DR1 Photometric Transformations

## Overview
This document describes the photometric transformations applied to PanSTARRS DR1 data to convert from the PanSTARRS photometric system (g, r, i) to the standard Johnson-Cousins photometric system (V, B, R, I).

## Transformation Equations

The transformations convert PanSTARRS Sloan-like magnitudes to Johnson-Cousins magnitudes using empirically derived relationships.

Let:
- g = PanSTARRS g magnitude
- r = PanSTARRS r magnitude  
- i = PanSTARRS i magnitude

### V Magnitude
```
V = g - 0.59·(g - r) - 0.01
```

### B-V Color
```
B-V = (g - r) + 0.22
```

### V-R Color
```
V-R = 1.09·(r - i) + 0.22
```

### R-I Color
```
R-I = (r - i) + 0.21
```

### V-I Color (Primary)
```
V-I = (V-R) + (R-I)
V-I = 1.09·(r - i) + 0.22 + (r - i) + 0.21
V-I = 2.09·(r - i) + 0.43
```

## Color Indices
After transformation, the following standard color indices are calculated:
- **V-I**: Primary color index for PanSTARRS (stored in B-V field)
- **V-R**: Red color index
- **R-I**: Near-infrared color index
- **B-V**: Blue color index (calculated but not used as primary)

## Error Propagation
Errors are propagated through the transformations using standard error propagation formulas:

For V magnitude:
```
σ_V² = σ_g² + (0.59)² · (σ_g² + σ_r²)
```

For V-I color:
```
σ_V-I = √[(∂V-I/∂r)² · σ_r² + (∂V-I/∂i)² · σ_i²]
σ_V-I = 2.09 · √(σ_r² + σ_i²)
```

Similar formulas apply for other color indices.

## Implementation
The transformations are implemented in `DataConnector.java` in the `parsePanstarrsVOTable()` method. When PanSTARRS DR1 catalog data is retrieved:

1. Raw g, r, i magnitudes are extracted from the VizieR TAP query
2. Transformations are applied using the equations above
3. Transformed V magnitude is calculated and stored
4. Color indices V-R, R-I, and V-I are calculated
5. V-I is stored as the primary color index (in the B-V field for compatibility)
6. Source number 903 identifies PanSTARRS DR1 data

## Query Constraints
The PanSTARRS DR1 query includes the following constraints to ensure data quality:
- **gmag IS NOT NULL** - g magnitude must exist
- **rmag IS NOT NULL** - r magnitude must exist
- **imag IS NOT NULL** - i magnitude must exist
- **o_gmag > 2** - At least 3 observations in g band
- **o_rmag > 2** - At least 3 observations in r band
- **o_imag > 2** - At least 3 observations in i band

## Validity Range
- **Magnitude range**: Approximately 12 - 21 mag
- **Color range**: Best results for stars with -0.5 < g-r < 2.0
- **Accuracy**: Transformations are empirical and work best for main sequence stars

## Display
When displaying PanSTARRS DR1 data:
- **Magnitude**: Shows transformed V magnitude with error
- **Color**: Shows V-I color (stored in the B-V field for compatibility)
- **Source**: 903 identifies PanSTARRS DR1 data
- Stars are labeled with "PS1_" prefix followed by objID

## Comparison with Gaia
Both Gaia DR2 and PanSTARRS DR1 use:
- **V magnitude** as primary magnitude (after transformation)
- **V-I color** as primary color index
- This ensures consistency across different catalog sources

## References
- PanSTARRS DR1 VizieR Catalog: II/349/ps1
- Chambers, K. C., et al. 2016, "The Pan-STARRS1 Surveys", arXiv:1612.05560
- Tonry, J. L., et al. 2012, "The Pan-STARRS1 Photometric System", ApJ 750, 99

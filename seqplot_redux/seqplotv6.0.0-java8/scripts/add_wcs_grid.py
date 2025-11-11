#!/usr/bin/env python3
"""
Add WCS coordinate grid overlay to DSS2 FITS images using astropy and matplotlib.
This script reads a FITS file, renders a proper WCS grid using WCSAxes, and saves
the result as a PNG image with the grid overlay.
"""

import sys
import numpy as np
from astropy.io import fits
from astropy.wcs import WCS
from astropy.visualization import make_lupton_rgb, ZScaleInterval, ImageNormalize, LinearStretch
import matplotlib
matplotlib.use('Agg')  # Non-interactive backend
import matplotlib.pyplot as plt
from matplotlib import patheffects

def add_wcs_grid(fits_path, output_path, invert=False):
    """
    Add WCS coordinate grid to FITS image and save as PNG.
    
    Parameters:
    -----------
    fits_path : str
        Path to input FITS file
    output_path : str
        Path to output PNG file
    invert : bool
        Whether to invert the image colors (default: False)
    """
    try:
        # Open FITS file
        with fits.open(fits_path) as hdul:
            # Get image data and WCS
            data = hdul[0].data
            wcs = WCS(hdul[0].header)
            
            # Handle NaN values
            data = np.nan_to_num(data, nan=0.0)
            
            # Normalize the image data using ZScale (astronomical standard)
            interval = ZScaleInterval()
            vmin, vmax = interval.get_limits(data)
            
            # Invert if requested
            if invert:
                data = vmax - data + vmin
            
            # Create figure with WCS projection
            fig = plt.figure(figsize=(10, 10), dpi=150)
            ax = plt.subplot(projection=wcs)
            
            # Display the image
            im = ax.imshow(data, cmap='gray', origin='lower', 
                          vmin=vmin, vmax=vmax, aspect='equal')
            
            # Configure the coordinate grid
            ax.coords.grid(color='lime', alpha=0.5, linestyle='--', linewidth=1)
            
            # Configure RA axis
            ra = ax.coords[0]
            ra.set_axislabel('Right Ascension (J2000)', color='lime', fontsize=10)
            ra.set_major_formatter('hh:mm:ss')
            ra.set_ticks(color='lime', size=5)
            ra.set_ticklabel(color='lime', fontsize=9)
            ra.set_separator(('$^{\mathrm{h}}$', '$^{\mathrm{m}}$', '$^{\mathrm{s}}$'))
            
            # Configure Dec axis
            dec = ax.coords[1]
            dec.set_axislabel('Declination (J2000)', color='lime', fontsize=10)
            dec.set_major_formatter('dd:mm:ss')
            dec.set_ticks(color='lime', size=5)
            dec.set_ticklabel(color='lime', fontsize=9)
            dec.set_separator(('°', '′', '″'))
            
            # Adjust tick density based on image size
            # WCSAxes automatically handles high declination convergence
            ra.set_ticks(number=6)
            dec.set_ticks(number=6)
            
            # Add compass indicator (N-E arrows)
            add_compass(ax, wcs, data.shape)
            
            # Remove extra whitespace
            plt.tight_layout(pad=0.5)
            
            # Save as PNG
            plt.savefig(output_path, dpi=150, bbox_inches='tight', 
                       facecolor='black', edgecolor='none')
            plt.close()
            
            print(f"SUCCESS: Grid overlay saved to {output_path}")
            return 0
            
    except Exception as e:
        print(f"ERROR: {str(e)}", file=sys.stderr)
        return 1

def add_compass(ax, wcs, shape):
    """
    Add a compass rose showing North and East directions.
    
    Parameters:
    -----------
    ax : matplotlib axes
        The axes to draw on
    wcs : astropy.wcs.WCS
        The WCS information
    shape : tuple
        Shape of the image (height, width)
    """
    # Position in lower left corner (in pixel coordinates)
    base_x, base_y = 50, 50
    arrow_length = 40
    
    # Convert base position to world coordinates and back to understand orientation
    center_world = wcs.pixel_to_world(shape[1]/2, shape[0]/2)
    north_world = wcs.pixel_to_world(shape[1]/2, shape[0]/2 + 10)
    east_world = wcs.pixel_to_world(shape[1]/2 + 10, shape[0]/2)
    
    # Calculate North direction in pixel space
    center_pix = wcs.world_to_pixel(center_world)
    north_pix = wcs.world_to_pixel(north_world)
    
    north_dx = north_pix[0] - center_pix[0]
    north_dy = north_pix[1] - center_pix[1]
    north_len = np.sqrt(north_dx**2 + north_dy**2)
    
    if north_len > 0:
        north_dx = (north_dx / north_len) * arrow_length
        north_dy = (north_dy / north_len) * arrow_length
        
        # East is 90 degrees counterclockwise from North
        east_dx = -north_dy
        east_dy = north_dx
        
        # Draw arrows
        arrow_props = dict(arrowstyle='->', lw=2, color='lime', alpha=0.8)
        
        # North arrow
        ax.annotate('', xy=(base_x, base_y + north_dy), xycoords='axes pixels',
                   xytext=(base_x, base_y), textcoords='axes pixels',
                   arrowprops=arrow_props)
        ax.text(base_x, base_y + north_dy + 10, 'N', color='lime', fontsize=12,
               weight='bold', ha='center', va='bottom',
               transform=ax.transAxes, 
               path_effects=[patheffects.withStroke(linewidth=2, foreground='black')])
        
        # East arrow
        ax.annotate('', xy=(base_x + east_dx, base_y + east_dy), xycoords='axes pixels',
                   xytext=(base_x, base_y), textcoords='axes pixels',
                   arrowprops=arrow_props)
        ax.text(base_x + east_dx + 10, base_y + east_dy, 'E', color='lime', fontsize=12,
               weight='bold', ha='left', va='center',
               transform=ax.transAxes,
               path_effects=[patheffects.withStroke(linewidth=2, foreground='black')])

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print("Usage: add_wcs_grid.py <input.fits> <output.png> [invert]")
        sys.exit(1)
    
    fits_path = sys.argv[1]
    output_path = sys.argv[2]
    invert = len(sys.argv) > 3 and sys.argv[3].lower() in ('true', '1', 'yes')
    
    sys.exit(add_wcs_grid(fits_path, output_path, invert))

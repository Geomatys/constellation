/*
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.image.io.mosaic;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.spi.ImageReaderSpi;

/**
 * Generates tiles for a specific raster.
 * 
 * @source $URL$
 * @author Cédric Briançon
 */
public class TileGenerator {
    /**
     * Create tiles which have a constant size, but cover more and more superficy
     * region of the original raster as we progress into overviews level.
     * 
     * @param spi The image provider.
     * @param raster The whole raster.
     * @param tileSize The size of tiles.
     * @param step The factor by which the size of tiles is multiplicated.
     * @return A {@linkplain TileManager} that contains all tiles generated.
     */
    public TileManager createTiles(final ImageReaderSpi spi, final Rectangle raster, 
         final Dimension tileSize, final Dimension step) 
    {
        List<Tile> tiles = new ArrayList<Tile>();
        Rectangle wholeRaster = raster;
        final Rectangle tileRect = new Rectangle(tileSize);
        Dimension subSampling = new Dimension(1,1);
        // Iterator used for the file name.
        int overview = 1, x = 1, y = 1;
        while (!tileRect.contains(wholeRaster)) {
            // Current values for the y coordinates
            int yMin = 0;
            int yMax = tileSize.height;
            while (yMin < wholeRaster.height) {
                /* Verify that we are not trying to generate a tile outside the original
                 * raster bounds, for the height.
                 * If it is the case, we take the width bound of the original raster as
                 * the width bound for the new tile.
                 */
                if (yMax > wholeRaster.height) {
                    yMax = wholeRaster.height;
                }
                // Current values for the x coordinates
                int xMin = 0;
                int xMax = tileSize.width;
                while (xMin < wholeRaster.width) {
                    /* Verify that we are not trying to generate a tile outside the original
                     * raster bounds, for the width.
                     * If it is the case, we take the height bound of the original raster as
                     * the height bound for the new tile.
                     */
                    if (xMax > wholeRaster.width) {
                        xMax = wholeRaster.width;
                    }
                    Rectangle currentRect = new Rectangle(xMin, yMin, xMax, yMax);
                    // Creates a specific file for this tile.
                    File inputTile = new File(generateName(overview, x, y) + ".png");
                    Tile tile = new Tile(spi, inputTile, 0, currentRect, subSampling);
                    tiles.add(tile);
                    x++;
                    xMin += tileSize.height;
                    xMax += tileSize.height;
                }
                y++;
                yMin += tileSize.width;
                yMax += tileSize.width;
            }
            // Change to next level of overview.
            overview++;
            subSampling.setSize(subSampling.width  * step.width, 
                                subSampling.height * step.height);
            wholeRaster.setSize(wholeRaster.width  / step.width, 
                                wholeRaster.height / step.height);
        }
        TileManager tileManager = new TileManagerFactory(null).createGeneric((Tile[]) tiles.toArray());
        return tileManager;
    }
    
    /**
     * Generate a name, for the current tile, based on the position of this tile 
     * in the raster. For example, a tile at the first level of overview, which
     * is localized on the 5th column and 2nd row will have the name "TileA52".
     * 
     * @param overview The level of overview.
     * @param x The index of columns.
     * @param y The index of rows.
     * @return A name based on the position of the tile in the whole raster.
     */
    protected String generateName(final int overview, final int x, final int y) {
        final StringBuilder buffer = new StringBuilder("Tile");
        buffer.append(overview).append('_').append((char) ('A' + x)).append(y);
        return buffer.toString();
    }
}

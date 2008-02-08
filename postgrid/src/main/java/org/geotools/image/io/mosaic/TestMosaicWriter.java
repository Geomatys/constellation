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
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Creates tiles for a specific raster, and write them in different output files.
 *
 * @source $URL$
 * @author Cédric Briançon
 */
public class TestMosaicWriter {
    /**
     * The raster that we wish to tiled.
     */
    private static final File INPUT = new File("C:\\BlueMarble\\Topo_bathy_png\\world.topo.bathy.200407.3x21600x21600.A1.png");

    /**
     * The output folder where tiles will be written.
     */
    private static final File OUTPUT = new File("C:\\test\\A1");

    /**
     * The wished size for each tile.
     */
    private static final Dimension TILESIZE = new Dimension(200,200);

    /**
     * The factor between two consecutive overviews.
     */
    private static final Dimension STEP = new Dimension(2,2);

    /**
     * The minimum tile size that we want to have at the end of the process.
     */
    private static final Dimension MINTILE = new Dimension(70, 70);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        org.geotools.resources.image.ImageUtilities.allowNativeCodec("PNG", javax.imageio.spi.ImageReaderSpi.class, false);
        try {
            if (!OUTPUT.exists()) {
                OUTPUT.mkdirs();
            }
            TileBuilder builder = new TileBuilder();
            builder.setTileDirectory(OUTPUT);
            builder.setTileLayout(TileLayout.CONSTANT_GEOGRAPHIC_AREA);
            builder.setTileSize(MINTILE);
            builder.setPreferredSubsampling(STEP);
            TileManager tileManager = builder.writeFromUntiledImage(INPUT, 0);
        } catch (IOException ex) {
            Logger.getLogger(TestMosaicWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

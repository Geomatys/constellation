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

import java.io.File;
import java.io.IOException;


/**
 * Creates tiles for a specific raster, and write them in different output files.
 *
 * @source $URL$
 * @author Cédric Briançon
 */
public class TestMosaicWriter {
    public static void main(String[] args) throws IOException {
        org.geotools.resources.image.ImageUtilities.allowNativeCodec("PNG", javax.imageio.spi.ImageReaderSpi.class, false);
        TileBuilder builder = new TileBuilder();
        builder.setTileDirectory(new File("/home/desruisseaux/Données/PostGRID/Monde/BlueMarble/test"));
        builder.setTileLayout(TileLayout.CONSTANT_GEOGRAPHIC_AREA);
        TileManager tileManager = builder.writeFromUntiledImage(new File("/home/desruisseaux/Données/PostGRID/Monde/BlueMarble/Tile1_A1.png"), 0);
        System.out.println(tileManager);
    }
}

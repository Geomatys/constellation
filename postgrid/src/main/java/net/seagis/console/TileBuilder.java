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
package net.seagis.console;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.spi.ImageReaderSpi;

import org.geotools.geometry.Envelope2D;
import org.geotools.image.io.mosaic.Tile;
import org.geotools.image.io.mosaic.TileManager;
import org.geotools.image.io.mosaic.MosaicBuilder;

import net.seagis.catalog.Database;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.WritableGridCoverageTable;


/**
 * Creates tiles and write the entries in the database.
 *
 * @source $URL$
 * @author Cédric Briançon
 * @author Martin Desruisseaux
  */
public class TileBuilder {
    public static void main(String[] args) throws IOException, CatalogException, SQLException {
        org.geotools.util.logging.Logging.GEOTOOLS.forceMonolineConsoleOutput(Level.FINE);
        org.geotools.resources.image.ImageUtilities.allowNativeCodec("PNG", javax.imageio.spi.ImageReaderSpi.class, false);
        Logger.getLogger("org.geotools.image.io").fine("Lancement...");
        ImageReaderSpi spi = new com.sun.imageio.plugins.png.PNGImageReaderSpi();
        File directory = new File("/home/desruisseaux/Données/PostGRID/Monde/BlueMarble");
        Tile[] tiles = new Tile[] {
            new Tile(spi, new File(directory, "Tile1_A1.png"), 0, new Rectangle(21600*0, 21600*0, 21600, 21600)),
            new Tile(spi, new File(directory, "Tile1_B1.png"), 0, new Rectangle(21600*1, 21600*0, 21600, 21600)),
            new Tile(spi, new File(directory, "Tile1_C1.png"), 0, new Rectangle(21600*2, 21600*0, 21600, 21600)),
            new Tile(spi, new File(directory, "Tile1_D1.png"), 0, new Rectangle(21600*3, 21600*0, 21600, 21600)),
            new Tile(spi, new File(directory, "Tile1_A2.png"), 0, new Rectangle(21600*0, 21600*1, 21600, 21600)),
            new Tile(spi, new File(directory, "Tile1_B2.png"), 0, new Rectangle(21600*1, 21600*1, 21600, 21600)),
            new Tile(spi, new File(directory, "Tile1_C2.png"), 0, new Rectangle(21600*2, 21600*1, 21600, 21600)),
            new Tile(spi, new File(directory, "Tile1_D2.png"), 0, new Rectangle(21600*3, 21600*1, 21600, 21600))
        };
        MosaicBuilder builder = new MosaicBuilder();
        builder.setTileSize(new Dimension(960,960));
        builder.setTileDirectory(new File("/home/desruisseaux/Données/PostGRID/Monde/BlueMarble/S960"));
        builder.setMosaicEnvelope(new Envelope2D(null, -180, -90, 360, 180));
        TileManager tileManager = builder.createTileManager(tiles, 0, false);
        System.out.println(tileManager);

        final Database database = new Database();
        final WritableGridCoverageTable table = new WritableGridCoverageTable(database.getTable(WritableGridCoverageTable.class));
        table.setCanInsertNewLayers(true);
        table.setLayer("BlueMarble");
        table.addEntries(tileManager.getTiles(), 0);
        database.close();
    }
}

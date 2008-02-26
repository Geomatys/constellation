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

import java.awt.Point;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;

import org.geotools.util.logging.Logging;
import org.geotools.geometry.Envelope2D;
import org.geotools.image.io.mosaic.Tile;
import org.geotools.image.io.mosaic.TileManager;
import org.geotools.image.io.mosaic.MosaicBuilder;
import org.geotools.resources.Arguments;
import org.geotools.resources.image.ImageUtilities;

import net.seagis.catalog.Database;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.WritableGridCoverageTable;


/**
 * Creates tiles and write the entries in the database.
 *
 * @author Cédric Briançon
 * @author Martin Desruisseaux
  */
public class TileBuilder {
    /**
     * Runs from the command line.
     */
    public static void main(String[] args) {
        Logging.ALL.forceMonolineConsoleOutput();
        ImageUtilities.allowNativeCodec("PNG", ImageReaderSpi.class, false);
        ImageUtilities.allowNativeCodec("PNG", ImageWriterSpi.class, false);
        final Arguments  arguments = new Arguments(args);
        final boolean writeToDisk  = arguments.getFlag("--write-to-disk");
        final boolean fillDatabase = arguments.getFlag("--fill-database");

        args = arguments.getRemainingArguments(1);
        if (args.length != 1) {
            arguments.err.println("Missing argument: properties file");
            return;
        }
        final Properties properties = new Properties();
        try {
            final InputStream in = new FileInputStream(args[0]);
            properties.load(in);
            in.close();
        } catch (IOException e) {
            arguments.err.println(e);
            return;
        }
        final File sourceDirectory;
        final File targetDirectory;
        final Envelope2D envelope;
        final Point tileSize;
        try {
            sourceDirectory = getDirectory(properties, "SourceDirectory");
            targetDirectory = getDirectory(properties, "TargetDirectory");
            envelope        = getEnvelope (properties, "MosaicEnvelope" );
            tileSize        = getPoint    (properties, "TileSize"       );
        } catch (NumberFormatException e) {
            arguments.err.println(e);
            return;
        } catch (IllegalArgumentException e) {
            arguments.err.println(e);
            return;
        }
        final String series = (String) properties.remove("Series");
        final Set<Tile> tiles = new HashSet<Tile>();
        for (final Map.Entry<?,?> entry : properties.entrySet()) {
            final String file  = (String) entry.getKey();
            final String value = (String) entry.getValue();
            final Point origin;
            try {
                origin = getPoint(null, value);
            } catch (RuntimeException e) {
                arguments.err.println(e);
                return;
            }
            final Tile tile = new Tile(null, new File(sourceDirectory, file), 0, origin, null);
            tiles.add(tile);
        }
        MosaicBuilder builder = new MosaicBuilder();
        if (tileSize != null) {
            builder.setTileSize(new Dimension(tileSize.x, tileSize.y));
        }
        builder.setTileDirectory(targetDirectory);
        builder.setMosaicEnvelope(envelope);
        try {
            final TileManager tileManager = builder.createTileManager(tiles, 0, writeToDisk);
            System.out.println(tileManager);
            if (fillDatabase) try {
                final Database database = new Database();
                final WritableGridCoverageTable table = new WritableGridCoverageTable(
                        database.getTable(WritableGridCoverageTable.class));
                table.setCanInsertNewLayers(true);
                table.setLayer(series);
                table.addEntries(tileManager.getTiles(), 0);
                database.close();
            } catch (SQLException e) {
                arguments.err.println(e);
                return;
            } catch (CatalogException e) {
                arguments.err.println(e);
                return;
            }
        } catch (IOException e) {
            arguments.err.println(e);
            return;
        }
    }

    /**
     * Returns a directory from the given set of properties, or {@code null} if none.
     * The key will be <strong>removed</strong> from the given set of properties.
     *
     * @param  properties The set of properties.
     * @param  key The key to read in the properties.
     * @return The directory, or {@code null} if none.
     * @throws IllegalArgumentException If the file is not a directory.
     */
    private static File getDirectory(final Properties properties, final String key)
            throws IllegalArgumentException
    {
        final String value = (String) properties.remove(key);
        if (value == null) {
            return null;
        }
        final File directory = new File(value);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory.getPath() + " is not a directory.");
        }
        return directory;
    }

    /**
     * Parses a text from the given properties as a point in pixel coordinates.
     * The key will be <strong>removed</strong> from the given set of properties.
     *
     * @param  properties The set of properties.
     * @param  key The key to read in the properties.
     * @return The point, or {@code null} if none.
     * @throws NumberFormatException if a number can't be parsed.
     * @throws IllegalArgumentException If the point doesn't have the expected dimension.
     */
    private static Point getPoint(final Properties properties, final String key)
            throws NumberFormatException, IllegalArgumentException
    {
        final String text = (properties != null) ? (String) properties.remove(key) : key;
        if (text == null) {
            return null;
        }
        int i = 0;
        final Point point = new Point();
        final StringTokenizer tokens = new StringTokenizer(text);
        while (tokens.hasMoreTokens()) {
            final int value = Integer.parseInt(tokens.nextToken());
            switch (i++) {
                case 0:  point.x = value; break;
                case 1:  point.y = value; break;
                default: break; // An exception will be thrown at the end of this method.
            }
        }
        if (i != 2) {
            throw new IllegalArgumentException("Expected 2 values but found \"" + text + '"');
        }
        return point;
    }

    /**
     * Parses a text from the given properties as an envelope in "real world" coordinates.
     * The key will be <strong>removed</strong> from the given set of properties.
     *
     * @param  properties The set of properties.
     * @param  key The key to read in the properties.
     * @return The envelope, or {@code null} if none.
     * @throws NumberFormatException if a number can't be parsed.
     * @throws IllegalArgumentException If the envelope doesn't have the expected dimension.
     */
    private static Envelope2D getEnvelope(final Properties properties, final String key)
            throws IllegalArgumentException, NumberFormatException
    {
        final String text = (String) properties.remove(key);
        if (text == null) {
            return null;
        }
        int i = 0;
        final Envelope2D envelope = new Envelope2D();
        final StringTokenizer tokens = new StringTokenizer(text);
        while (tokens.hasMoreTokens()) {
            final double value = Double.parseDouble(tokens.nextToken());
            switch (i++) {
                case 0:  envelope.x = value; break;
                case 1:  envelope.y = value; break;
                case 2:  envelope.width  = value - envelope.x; break;
                case 3:  envelope.height = value - envelope.y; break;
                default: break; // An exception will be thrown at the end of this method.
            }
        }
        if (i != 4) {
            throw new IllegalArgumentException("Expected 4 values but found \"" + text + '"');
        }
        return envelope;
    }
}

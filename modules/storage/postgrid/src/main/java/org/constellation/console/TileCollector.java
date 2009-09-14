/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.console;

import java.io.*;
import java.util.*;
import java.sql.SQLException;
import java.sql.DriverManager;

import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.image.io.mosaic.Tile;
import org.geotoolkit.image.io.mosaic.TileManager;
import org.geotoolkit.util.collection.FrequencySortedSet;
import org.geotoolkit.console.CommandLine;
import org.geotoolkit.console.Option;
import org.geotoolkit.console.Action;

import org.constellation.catalog.Database;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.NoSuchRecordException;
import org.constellation.coverage.catalog.WritableGridCoverageEntry;
import org.constellation.coverage.catalog.WritableGridCoverageTable;


/**
 * Write the tiles entries in the database. The {@linkplain #main main} method expects a
 * {@code .serialized} file produced by the Geotoolkit.org wizard. For processing to the
 * actual database update, one of the following actions must be specified:
 * <p>
 * <ul>
 *   <li>{@code insert}  inserts the tiles metadata in the database.</li>
 *   <li>{@code pretend} prints the SQL statement instead of executing them.</li>
 * </ul>
 * <p>
 * This utility accepts the following options:
 * <p>
 * <ul>
 *   <li>{@code --database} The URL to the database (mandatory).</li>
 *   <li>{@code --series}   The series for the tiles to be inserted in the database (mandatory).</li>
 *   <li>{@code --tiles}    The directory which contains tiles. Used for inserting only the existing tiles.</li>
 *   <li>{@code --srid}     The horizontal SRID to declare in the grid geometry table.</li>
 * </ul>
 *
 * @author Cédric Briançon
 * @author Martin Desruisseaux
  */
public class TileCollector extends CommandLine {
    /**
     * The URL to the database (mandatory).
     */
    @Option(mandatory = true)
    private String database;

    /**
     * The series for the tiles to be inserted in the database.
     */
    @Option(mandatory = true)
    private String series;

    /**
     * Directory of tiles to insert, or {@code null} if unspecified.
     */
    @Option(name = "tiles")
    private File tileDirectory;

    /**
     * The horizontal SRID, or {@code 0} if unspecified.
     */
    @Option(examples = "4326")
    private int srid;

    /**
     * {@code true} if this command run in "pretend" mode.
     */
    private boolean pretend;

    /**
     * Creates a new builder. In case of failure, a message is printed to the
     * {@link #err standard error stream} and this method invokes {@link System#exit}.
     *
     * @param args The command line arguments.
     */
    protected TileCollector(final String[] args) {
        super(null, args);
    }

    /**
     * Like {@link #insert}, but prints the SQL statement without executing them.
     */
    @Action(minimalArgumentCount=1, maximalArgumentCount=1)
    protected void pretend() {
        pretend = true;
        insert();
    }

    /**
     * Insert the tile entries in the database. If this method fails, a message is printed
     * to the {@link System#err standard error stream} and {@link System#exit} is invoked.
     */
    @Action(minimalArgumentCount=1, maximalArgumentCount=1)
    protected void insert() {
        Registry.setDefaultCodecPreferences();
        final TileManager manager;
        try {
            final ObjectInputStream in = new ObjectInputStream(new FileInputStream(arguments[0]));
            manager = (TileManager) in.readObject();
            in.close();
        } catch (Exception e) {
            err.println(e);
            exit(IO_EXCEPTION_EXIT_CODE);
            return;
        }
        Collection<Tile> tiles;
        /*
         * Keep only the tiles associated to existing files.
         */
        try {
            tiles = manager.getTiles();
        } catch (IOException e) {
            err.println(e);
            exit(IO_EXCEPTION_EXIT_CODE);
            return;
        }
        if (tileDirectory != null) {
            final ArrayList<Tile> filtered = new ArrayList<Tile>(tiles.size());
            for (final Tile tile : tiles) {
                final Object input = tile.getInput();
                if (input instanceof File) {
                    File file = (File) input;
                    if (!file.isAbsolute()) {
                        file = new File(tileDirectory, file.getPath());
                    }
                    if (!file.isFile()) {
                        out.println("Tile " + file.getPath() + " does not exist.");
                        continue;
                    }
                }
                filtered.add(tile);
            }
            filtered.trimToSize();
            tiles = filtered;
        }
        out.flush();
        /*
         * Creates a global tiles which cover the whole area.
         * We will use the most frequent file suffix for this tile.
         *
         * TODO: probably a bad idea - WritableGridCoverageTable will no accept arbitrary suffix,
         *       but only the suffix expected by the series. We will need to revisit this policy.
         */
        final SortedSet<String> suffixes = new FrequencySortedSet<String>(true);
        for (final Tile tile : tiles) {
            final Object input = tile.getInput();
            if (input instanceof File) {
                final String file = ((File) input).getName();
                final int split = file.lastIndexOf('.');
                if (split >= 0) {
                    suffixes.add(file.substring(split));
                }
            }
        }
        String name = series;
        if (!suffixes.isEmpty()) {
            name += suffixes.first();
        }
        final Tile global;
        try {
            global = manager.createGlobalTile(null, name, 0);
        } catch (IOException e) {
            err.println(e);
            exit(IO_EXCEPTION_EXIT_CODE);
            return;
        }
        /*
         * Fills the database if requested by the user. The tiles entries will be inserted in the
         * "Tiles" table while the global entry will be inserted into the "GridCoverages" table.
         */
        final Properties properties = new Properties();
        properties.put("ReadOnly", "false");
        try {
            final Database database = new Database(DriverManager.getConnection(this.database), properties);
            if (pretend) {
                database.setUpdateSimulator(out);
            }
            WritableGridCoverageTable table = database.getTable(WritableGridCoverageTable.class);
            table = new WritableGridCoverageTable(table) {
                @Override
                protected WritableGridCoverageEntry createEntry(final Tile tile) throws IOException {
                    return new Entry(tile);
                }
            };
            table.setCanInsertNewLayers(true);
            table.setLayer(series); // TODO: we currently assume the same name than the series.
            try {
                table.setSeries(series);
            } catch (NoSuchRecordException e) {
                // Ignore... We will let the WritableGridCoverageTable selects a series.
            }
            table.addEntry(global);
            table.addTiles(tiles);
            database.close();
        } catch (IOException e) {
            err.println(e);
            exit(IO_EXCEPTION_EXIT_CODE);
        } catch (SQLException e) {
            err.println(e);
            exit(SQL_EXCEPTION_EXIT_CODE);
        } catch (CatalogException e) {
            err.println(e);
            exit(SQL_EXCEPTION_EXIT_CODE);
        }
        out.flush();
        err.flush();
    }

    /**
     * The entry for a tile to be added.
     */
    private final class Entry extends WritableGridCoverageEntry {
        public Entry(final Tile tile) throws IOException {
            super(tile);
        }

        @Override
        public int getHorizontalSRID() throws IOException, CatalogException {
            return (srid != 0) ? srid : super.getHorizontalSRID();
        }
    }

    /**
     * Runs from the command line.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        final TileCollector collector = new TileCollector(args);
        collector.run();
    }
}

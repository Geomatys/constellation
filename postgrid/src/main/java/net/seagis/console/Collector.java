/*
 * (C) 2007, IFREMER
 * (C) 2007, Geomatys
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
 */
package net.seagis.console;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import javax.imageio.ImageReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.Aggregation;
import ucar.nc2.ncml.NcMLReader;

import org.geotools.resources.Arguments;
import org.geotools.image.io.netcdf.NetcdfImageReader;

import net.seagis.catalog.Database;
import net.seagis.catalog.UpdatePolicy;
import net.seagis.catalog.ServerException;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.WritableGridCoverageTable;
import static net.seagis.catalog.UpdatePolicy.*;


/**
 * Adds new records to the specified database.
 *
 * @version $Id: Collector.java 90 2008-01-02 13:23:18Z cb1ebc7 $
 * @author Martin Desruisseaux
 */
public class Collector {
    /**
     * Database connection.
     */
    private final Database database;

    /**
     * The table where to write new records.
     */
    private final WritableGridCoverageTable table;

    /**
     * Whatever new records should replace the old ones, or if old records should be keeped as-is.
     */
    private UpdatePolicy updatePolicy = UpdatePolicy.SKIP_EXISTING;

    /**
     * An output writer for debugging messages, or {@code null} for the standard output stream.
     */
    private PrintWriter out;

    /**
     * Creates a new collector which will adds entries in the default database.
     *
     * @throws CatalogException if the connection failed.
     */
    public Collector() throws CatalogException {
        this(null);
    }

    /**
     * Creates a new collector which will adds entries in the specified database.
     *
     * @param database The database connection.
     * @throws CatalogException if the connection failed.
     */
    public Collector(final Database database) throws CatalogException {
        if (database != null) {
            this.database = database;
        } else {
            try {
                this.database = new Database();
            } catch (IOException e) {
                throw new CatalogException(e);
            }
        }
        table = new WritableGridCoverageTable(this.database.getTable(WritableGridCoverageTable.class));
    }

    /**
     * Returns the database connection.
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Returns the output printer for debugging messages.
     * This method never returns {@code null}.
     */
    public PrintWriter getPrinter() {
        if (out == null) {
            out = new PrintWriter(System.out, true);
        }
        return out;
    }

    /**
     * Sets the output writer for debugging messages. If this method is never invoked,
     * then the default is the {@linkplain System#out standard output stream}.
     * <p>
     * Debugging messages are typically the {@code INSERT} SQL instructions that would
     * be emitted. Those instructions are printed only if {@link #setPretend} has been
     * invoked with value {@code true}.
     */
    public void setPrinter(final PrintWriter out) {
        if (out != null) {
            out.flush();
        }
        this.out = out;
    }

    /**
     * If {@code true}, prints {@code INSERT} statements to the {@linkplain #getPrinter output printer}
     * rather than executing them. This is useful for testing purpose.
     */
    public void setPretend(final boolean pretend) {
        database.setUpdateSimulator(pretend ? getPrinter() : null);
    }

    /**
     * Whatever new records should replace the old ones, or if old records should be keeped as-is.
     */
    public void setPolicy(final UpdatePolicy policy) {
        updatePolicy = policy;
    }

    /**
     * Proceed to the insertion of new records for the specified layer.
     *
     * @param  layer The layer to update.
     * @throws CatalogException If insertion failed.
     */
    public void process(final String layer) throws CatalogException {
        final int count;
        try {
            table.setLayer(layer);
            count = table.updateLayer(true, updatePolicy);
            database.flush();
        } catch (SQLException e) {
            throw new ServerException(e);
        } catch (IOException e) {
            throw new CatalogException(e);
        }
        final PrintWriter out = getPrinter();
        if (out != null) {
            // TODO: localize
            out.print(count);
            out.print(" images ajoutées");
            if (!SKIP_EXISTING.equals(updatePolicy)) {
                out.print(" ou remplacées");
            }
            out.print(" pour la couche \"");
            out.print(layer);
            out.println("\".");
        }
    }

    /**
     * Proceed to the insertion of new records for the specified layer from a NetCDF file.
     *
     * @param  layer The layer to update.
     * @param  ncmlPath Path to the NcML file.
     * @throws CatalogException If insertion failed.
     */
    public void processNcML(final String layer, final String ncmlPath, final boolean allowsNewLayer)
            throws CatalogException
    {
        table.setCanInsertNewLayers(allowsNewLayer);
        try {
            table.setLayer(layer);
            final ImageReader reader;
            reader = new NetcdfImageReader.Spi().createReaderInstance();
            final NetcdfDataset netcdfData = NcMLReader.readNcML(ncmlPath, getNetcdfElement(ncmlPath), null);
            final List<Aggregation.Dataset> data = netcdfData.getAggregation().getNestedDatasets();
            for (final Aggregation.Dataset aggrData: data) {
                String netcdfPath = aggrData.getLocation();
                final Object input;
                if (netcdfPath.indexOf("://") >= 0) {
                    input = new URI(netcdfPath);
                } else {
                    input = new File(netcdfPath);
                }
                reader.setInput(input);
                table.addEntry(reader);
            }
            database.flush();
        } catch (URISyntaxException ex) {
            throw new ServerException(ex);
        } catch (SQLException e) {
            throw new ServerException(e);
        } catch (IOException e) {
            throw new CatalogException(e);
        }
    }

    /**
     * Returns the XML {@code <netcdf>} elements for the NetCDF file at the given path.
     *
     * @param  ncmlPath Path to the NcML file.
     * @return The XML {@code <netcdf>} element.
     * @throws CatalogException If the element can't be obtained.
     */
    private static Element getNetcdfElement(final String ncmlPath) throws CatalogException {
        final Namespace netcdf = Namespace.getNamespace("http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");
        final Document doc;
        try {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(ncmlPath);
        } catch (JDOMException e) {
            throw new CatalogException(e);
        } catch (IOException e) {
            throw new CatalogException(e);
        }
        final Element root = doc.getRootElement();
        if (root.getName().equals("netcdf")) {
            return root;
        } else {
            Element elemDataset = root.getChild("dataset", root.getNamespace());
            return elemDataset.getChild("netcdf", netcdf);
        }
    }

    /**
     * Dispose collector resources.
     *
     * @throws CatalogException If an error occured while disposing the resources.
     */
    public void close() throws CatalogException {
        try {
            database.close();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        if (out != null) {
            out.flush();
        }
    }

    /**
     * Runs from the command line.
     */
    public static void main(String[] args) throws CatalogException {
        final Arguments arguments = new Arguments(args);
        final boolean pretend = arguments.getFlag("-pretend");
        final boolean replace = arguments.getFlag("-replace");
        final boolean clear   = arguments.getFlag("-clear");
        final String  layer   = arguments.getRequiredString("-layer");
        args = arguments.getRemainingArguments(0);
        final Collector collector = new Collector();
        collector.setPrinter(arguments.out);
        collector.setPretend(pretend);
        collector.setPolicy(clear ? CLEAR_BEFORE_UPDATE : replace ? REPLACE_EXISTING : SKIP_EXISTING);
        collector.process(layer);
        collector.close();
    }
}

/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007 Geomatys
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.seagis.catalog.Database;
import net.seagis.catalog.UpdatePolicy;
import net.seagis.catalog.ServerException;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.WritableGridCoverageTable;
import net.seagis.ncml.NcmlGridCoverageTable;
import net.seagis.ncml.NcmlNetcdfElement;
import net.seagis.ncml.NcmlReading;
import net.seagis.ncml.NcmlTimeValues;
import static net.seagis.catalog.UpdatePolicy.*;

import org.geotools.resources.Arguments;
import org.geotools.image.io.netcdf.NetcdfImageReader;
import org.jdom.Element;

import ucar.nc2.ncml.Aggregation;
import ucar.nc2.ncml.Aggregation.Type;
import ucar.nc2.ncml.AggregationExisting;


/**
 * Adds new records to the specified database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Cédric Briançon
 */
public class Collector {
    /**
     * Database connection.
     */
    private final Database database;

    /**
     * The table where to write new records.
     */
    protected final WritableGridCoverageTable table;

    /**
     * Whatever new records should replace the old ones, or if old records should be keeped as-is.
     */
    private UpdatePolicy updatePolicy = UpdatePolicy.SKIP_EXISTING;

    /**
     * An output writer for debugging messages, or {@code null} for the standard output stream.
     */
    private PrintWriter out;

    /**
     * Creates a new collector which will adds entries in the specified database.
     *
     * @param database The database connection, or {@code null} for the default.
     * @throws CatalogException if the connection failed.
     */
    protected Collector(final Database database) throws CatalogException {
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
     * @param  layer    The layer to update.
     * @param  variable The NetCDF variable to collect.
     * @param  ncml     Path to the NcML file.
     * @param  allowsNewLayer If {@code true}, the parser is allowed to create a new layer.
     *         New layers are created only if no suitable later already exists in the database.
     * @throws CatalogException If insertion failed.
     */
    public void processNcML(final String layer, String variable, final File ncml,
                            final boolean allowsNewLayer) throws CatalogException
    {
        variable = variable.toLowerCase().trim();
        final NcmlGridCoverageTable ncmlTable = new NcmlGridCoverageTable(database);
        ncmlTable.setCanInsertNewLayers(allowsNewLayer);
        final Set<NcmlNetcdfElement> netcdfTags = new HashSet<NcmlNetcdfElement>();
        try {
            final List<Aggregation> aggregations = NcmlReading.getNestedAggregations(ncml);
            // If we have an aggregation of type "joint" for the whole file, without any other
            // nested aggregation, then we know that the <netcdf> tags will contain the "location"
            // parameter, and we get it back directly in order to specify it to the reader.
            // Otherwise we have to browse all aggregations and their childs in order to get
            // the "location" parameters of these <netcdf> tags.
            if (aggregations.size() == 1 && aggregations.get(0).getType().equals(Type.JOIN_EXISTING)) {
                final AggregationExisting aggrExist = (AggregationExisting) aggregations.get(0);
                @SuppressWarnings("unchecked")
                final List<Aggregation.Dataset> datasets = aggrExist.getNestedDatasets();
                for (final Aggregation.Dataset dataset : datasets) {
                    @SuppressWarnings("unchecked")
                    final Collection<String> variables = aggrExist.getVariables();
                    for (final String var : variables) {
                        if (variable.startsWith(var.toLowerCase())) {
                            final NcmlNetcdfElement netcdfElement = new NcmlNetcdfElement(new URI(dataset.getLocation()), null);
                            netcdfTags.add(netcdfElement);
                        }
                    }
                }
            } else {
                // Browse all <netcdf location="..."> for this aggregation.
                final Collection<Element> nested = NcmlReading.getNestedNetcdfElement(ncml);
                for (final Element netcdfWithLocationParam : nested) {
                    // Verify that the variable to collect, specified by user, is really present
                    // among variables found in the NcML file for the current <netcdf> tags.
                    // If it is the case, the NetCDF file is added to the list of files to handle,
                    // otherwise it is skipped.
                    if (NcmlReading.getVariableElement(variable, netcdfWithLocationParam) == null) {
                        continue;
                    }
                    final URI location = new URI(netcdfWithLocationParam.getAttributeValue("location"));
                    final Element timeElement = NcmlReading.getVariableElement("time", netcdfWithLocationParam);
                    if (timeElement == null) {
                        addToLayer(layer, location, ncmlTable);
                        continue;
                    }
                    final Element timeValues = timeElement.getChild("values", NcmlReading.NETCDFNS);
                    if (timeValues == null) {
                        continue;
                    }
                    final long startTime = Math.round(Double.valueOf(timeValues.getAttributeValue("start")));
                    final long increment = Math.round(Double.valueOf(timeValues.getAttributeValue("increment")));
                    final int npts = Integer.valueOf(timeValues.getAttributeValue("npts"));
                    final NcmlTimeValues ncmlValue = new NcmlTimeValues(startTime, increment, npts);
                    final NcmlNetcdfElement netcdfElement = new NcmlNetcdfElement(location, ncmlValue);
                    netcdfTags.add(netcdfElement);
                }
            }
            // Do the adding of the selected NetCDF files into the database.
            final int size = netcdfTags.size();
            final NcmlNetcdfElement[] netcdfTagsArray = new NcmlNetcdfElement[size];
            netcdfTags.toArray(netcdfTagsArray);
            for (int i=0; i<size; i++) {
                if (i + 1 < size) {
                    addToLayer(layer, netcdfTagsArray[i], ncmlTable, netcdfTagsArray[i + 1]);
                } else {
                    if (size > 1) {
                        addToLayer(layer, netcdfTagsArray[i], ncmlTable, netcdfTagsArray[i - 1]);
                    }
                    else {
                        addToLayer(layer, netcdfTagsArray[i], ncmlTable, null);
                    }
                }
            }
            getDatabase().flush();
        } catch (SQLException e) {
            throw new ServerException(e);
        } catch (IOException e) {
            throw new CatalogException(e);
        } catch (URISyntaxException e) {
            throw new CatalogException(e);
        }
    }

    /**
     * Try to add the data red from the NcML file for the wished layer. If an SQL error
     * occurs, it could comes from a try to add data already in the database.
     * At this moment, we catch it and let the process continue.
     * This a temporary workaround that has to be replaced by a genuine test before the
     * adding of this record in the database.
     *
     * @param layer The layer to consider.
     * @param location The Netcdf path.
     * @param table The table in which the entry will be inserted.
     * @throws CatalogException
     * @throws SQLException If a SQL error occurs, other than a doublon.
     * @throws IOException
     */
    private void addToLayer(final String layer, final URI location, final WritableGridCoverageTable table)
            throws CatalogException, SQLException, IOException
    {
        table.setLayer(layer);
        final NetcdfImageReader reader = addInputToReader(location.toString());
        try {
            table.addEntry(reader);
        } catch (SQLException sql) {
            // If the error code is "23505", we know that it is a postgresql error which
            // indicates an adding of a record already present into the database.
            // In this case, we do nothing because the record is already present.
            // Otherwise we throw this exception, which could have occured for a different
            // reason.
            if (!sql.getSQLState().equals("23505")) {
                throw sql;
            }
        }
    }

    /**
     * Try to add the data red from the NcML file for the wished layer. If an SQL error
     * occurs, it could comes from a try to add data already in the database.
     * At this moment, we catch it and let the process continue.
     * This a temporary workaround that has to be replaced by a genuine test before the
     * adding of this record in the database.
     *
     * @param layer The layer to consider.
     * @param element An element that represents the current <netcdf> tags in the NcML file.
     * @param table The table in which the entry will be inserted.
     * @param nextElement An element that represents the next <netcdf> tags in the NcML file.
     * @throws CatalogException
     * @throws SQLException If a SQL error occurs, other than a doublon.
     * @throws IOException
     */
    private void addToLayer(final String layer, final NcmlNetcdfElement element,
            final NcmlGridCoverageTable table, final NcmlNetcdfElement nextElement)
            throws CatalogException, SQLException, IOException
    {
        table.setLayer(layer);
        final NcmlTimeValues timeValues = element.getTimeValues();
        final NetcdfImageReader reader = addInputToReader(element.getLocation().toString());
        try {
            table.setIncrement(timeValues.getIncrement());
            table.setStartTime(timeValues.getStartTime());
            table.setNpts(timeValues.getNpts());
            if (nextElement != null) {
                table.setNextItemStart(nextElement.getTimeValues().getStartTime());
            }
            table.addEntry(reader);
        } catch (SQLException sql) {
            // If the error code is "23505", we know that it is a postgresql error which
            // indicates an adding of a record already present into the database.
            // In this case, we do nothing because the record is already present.
            // Otherwise we throw this exception, which could have occured for a different
            // reason.
            if (!sql.getSQLState().equals("23505")) {
                throw sql;
            }
        }
    }

    /**
     * Creates an {@code ImageReader} for the NetCDF file specified, and returns it.
     *
     * @param netcdf The path for the NetCDF file red from the NcML file. It could
     *               contains a protocol. In this case it will be considered as an URI.
     * @return The {@code ImageReader} for the NetCDF file specified, or {@code null} if
     *         an error occurs in the generating process of the URI.
     * @throws IOException If the creation of the reader has failed.
     */
    private NetcdfImageReader addInputToReader(final String netcdf) throws IOException {
        final NetcdfImageReader reader = createNetcdfImageReader(netcdf);
        final Object input;
        if (netcdf.indexOf("://") >= 0) {
            try {
                input = new URI(netcdf);
            } catch (URISyntaxException ex) {
                throw new IOException(ex.getLocalizedMessage());
            }
        } else {
            input = new File(netcdf);
        }
        reader.setInput(input);
        return reader;
    }

    /**
     * Creates an instance of Netcdf reader for the given file. The default implementation
     * creates the default NetCDF reader for all files. Subclasses may override this method
     * for returning specialized readers.
     *
     * @param  file The NetCDF file to be read.
     * @return An instance of Netcdf reader.
     * @throws IOException if the NetCDF reader can not be created.
     */
    protected NetcdfImageReader createNetcdfImageReader(final String file) throws IOException {
        return (NetcdfImageReader) new NetcdfImageReader.Spi().createReaderInstance();
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
        final Collector collector = new Collector(null);
        collector.setPrinter(arguments.out);
        collector.setPretend(pretend);
        collector.setPolicy(clear ? CLEAR_BEFORE_UPDATE : replace ? REPLACE_EXISTING : SKIP_EXISTING);
        collector.process(layer);
        collector.close();
    }
}

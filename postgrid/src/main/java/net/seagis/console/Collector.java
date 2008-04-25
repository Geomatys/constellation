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
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import org.geotools.console.CommandLine;
import org.geotools.console.Option;
import org.geotools.image.io.netcdf.NetcdfImageReader;
import org.jdom.Element;
import ucar.nc2.ncml.Aggregation;
import ucar.nc2.ncml.Aggregation.Type;
import ucar.nc2.ncml.AggregationExisting;
import static net.seagis.catalog.UpdatePolicy.*;


/**
 * Adds new records to the specified database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Cédric Briançon
 */
public class Collector extends CommandLine {
    /**
     * Database connection.
     */
    private Database database;

    /**
     * The table where to write new records.
     */
    protected WritableGridCoverageTable table;

    /**
     * Whatever new records should replace the old ones, or if old records should be keeped as-is.
     */
    private UpdatePolicy updatePolicy = UpdatePolicy.SKIP_EXISTING;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Print the SQL statements rather than executing them (for debugging only).")
    private boolean pretend;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Replace the existing records.")
    private boolean replace;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Clear records before adding new ones.")
    private boolean clear;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="The layer to consider.", mandatory=true)
    protected String layer;

    /**
     * Flag specified on the command lines.
     */
    @Option(name="newlayer", description="True if the process can add a new layer.")
    private boolean newLayer = true;

    /**
     * Flag specified on the command lines.
     */
    @Option(name="ncmlpath", description="The path to the NcML file.")
    private String ncmlPath;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="The variable to consider.")
    private String variable;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="The type of process to launch.", mandatory=true)
    protected String type;

    /**
     * Creates a new collector which will adds entries in the specified database.
     *
     * @param args An array of arguments.
     */
    public Collector(final String[] args) {
        super(args, 0);
        updatePolicy = clear ? CLEAR_BEFORE_UPDATE : replace ? REPLACE_EXISTING : SKIP_EXISTING;
    }

    /**
     * Returns a set of possible choices for the process to launch.
     */
    protected Set<String> getValidTypes() {
        Set<String> set = new HashSet<String>();
        set.add("ncml");
        set.add("opendap");
        return set;
    }

    /**
     *
     * @param database The database connection, or {@code null} for the default.
     * @throws CatalogException if the connection failed
     */
    public void run(final Database database) throws CatalogException, SQLException {
        connect(database);
        this.database.setReadOnly(false);
        if (!getValidTypes().contains(type.toLowerCase())) {
            throw new IllegalArgumentException("Le type de moissonnage spécifié n'est pas" +
                    " connu : " + type);
        }
        if (type.toLowerCase().equals("ncml")) {
            processNcML();
        } else {
            process();
        }
        close();
    }

    /**
     * Creates a new collector which will adds entries in the specified database.
     *
     * @param database The database connection, or {@code null} for the default.
     * @throws CatalogException if the connection failed.
     */
    protected void connect(Database database) throws CatalogException {
        if (database == null) try {
            database = new Database();
        } catch (IOException e) {
            throw new CatalogException(e);
        }
        database.setUpdateSimulator(pretend ? out : null);
        this.database = database;
        table = new WritableGridCoverageTable(database.getTable(WritableGridCoverageTable.class));
    }

    /**
     * Returns the database connection.
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Returns the layer name.
     */
    public String getLayer() {
        return layer;
    }

    /**
     * Proceed to the insertion of new records for the specified layer.
     *
     * @throws CatalogException If insertion failed.
     */
    private void process() throws CatalogException {
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
     * @throws CatalogException If insertion failed.
     */
    private void processNcML() throws CatalogException
    {
        /*
         * Ensures both {@code -ncmlpath} and {@code -variable} arguments have been specified
         * by the user, in order to begin the process.
         */
        if (ncmlPath == null) {
            throw new IllegalArgumentException("The argument -ncmlpath was not specified.");
        }
        if (variable == null) {
            throw new IllegalArgumentException("The argument -variable was not specified.");
        }
        final File ncml = new File(ncmlPath);
        if (!ncml.exists()) {
            err.println("Path invalid to NcML file : " + ncmlPath);
            System.exit(ILLEGAL_ARGUMENT_EXIT_CODE);
        }
        final NcmlGridCoverageTable ncmlTable = new NcmlGridCoverageTable(database);
        ncmlTable.setCanInsertNewLayers(newLayer);
        final Set<NcmlNetcdfElement> netcdfTags = new LinkedHashSet<NcmlNetcdfElement>();
        try {
            final List<Aggregation> aggregations = NcmlReading.getNestedAggregations(ncml);
            variable = variable.toLowerCase().trim();
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
                            final NcmlNetcdfElement netcdfElement =
                                    new NcmlNetcdfElement(new URI(dataset.getLocation()), null);
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
                    final Element timeElement = 
                            NcmlReading.getVariableElement("time", netcdfWithLocationParam);
                    if (timeElement == null) {
                        addToLayer(layer, location, ncmlTable);
                        continue;
                    }
                    final Element timeValues = timeElement.getChild("values", NcmlReading.NETCDFNS);
                    if (timeValues == null) {
                        continue;
                    }
                    final long startTime = 
                            Math.round(Double.valueOf(timeValues.getAttributeValue("start")));
                    final long increment = 
                            Math.round(Double.valueOf(timeValues.getAttributeValue("increment")));
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
    private void addToLayer(final String layer, final URI location,
                            final WritableGridCoverageTable table)
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
     * @param element An element that represents the current {@code &lt;netcdf>} tags in the NcML file.
     * @param table The table in which the entry will be inserted.
     * @param nextElement An element that represents the next {@code &lt;netcdf>} tags in the NcML file.
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
    public static void main(String[] args) throws CatalogException, SQLException {
        final Collector collector = new Collector(args);
        collector.run(null);
    }
}

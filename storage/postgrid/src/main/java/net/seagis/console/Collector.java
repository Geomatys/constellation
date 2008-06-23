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
import java.util.LinkedHashSet;
import java.util.Set;

import java.util.logging.Logger;
import net.seagis.catalog.Database;
import net.seagis.catalog.UpdatePolicy;
import net.seagis.catalog.ServerException;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.WritableGridCoverageTable;
import org.geotools.console.CommandLine;
import org.geotools.console.Option;
import org.geotools.image.io.netcdf.NetcdfImageReader;
import org.jdom.Element;
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
     * The default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("net.seagis.console");
    
    /**
     * Database connection.
     */
    protected Database database;

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
    @Option(description="Print SQL statements rather than executing them (for debugging only).")
    private boolean pretend;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Replace existing records.")
    private boolean replace;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Clear records before adding new ones.")
    private boolean clear;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Path where to collect data.")
    protected String path;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Variable in Netcdf files to consider.")
    private String variable;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Layer to consider.", mandatory=true)
    protected String layer;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Serie's format.", mandatory=true)
    protected String format;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Type of process to launch. Should be : \"ncml\", \"opendap\" or \"caraibes\".")
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
     * Connects to the given database and invokes {@link #process(String)} for the
     * type given on the command line.
     *
     * @param database The database connection, or {@code null} for the default.
     * @throws CatalogException if an error occured while inserting the data.
     */
    public void run(Database database) throws CatalogException {
        if (database == null) try {
            database = new Database();
        } catch (IOException e) {
            throw new CatalogException(e);
        }
        try {
            database.setReadOnly(false);
        } catch (SQLException e) {
            throw new CatalogException(e);
        }
        database.setUpdateSimulator(pretend ? out : null);
        this.database = database; // Set only after success.
        table = new WritableGridCoverageTable(database.getTable(WritableGridCoverageTable.class));
        if (!process(type)) {
            close();
            throw new IllegalArgumentException("Le type de moissonnage spécifié n'est pas connu : "
                    + type);
        }
        close();
    }

    /**
     * If this method knows the given type, executes the process for this type and
     * returns {@code true}. Otherwise returns {@code false}.
     *
     * @throws CatalogException if an error occured while inserting the data.
     */
    protected boolean process(final String type) throws CatalogException {
        // By default if not precised.
        if (type == null || type.equals("opendap")) {
            processLayer();
            return true;
        }
        if (type.equalsIgnoreCase("ncml")) {
            processNcML();
            return true;
        }
        return false;
    }

    /**
     * Proceed with the insert of new records for the specified layer.
     *
     * @throws CatalogException If insertion fails.
     */
    private void processLayer() throws CatalogException {
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
            out.print(" image(s) ajoutée(s)");
            if (!SKIP_EXISTING.equals(updatePolicy)) {
                out.print(" ou remplacée(s)");
            }
            out.print(" pour la couche \"");
            out.print(layer);
            out.println("\".");
        }
    }

    /**
     * Proceed with the insert of new records defined in an NcML file.
     *
     * @throws CatalogException If insertion fails.
     */
    private void processNcML() throws CatalogException {
        /*
         * Ensures both {@code -path} and {@code -variable} arguments have been specified
         * by the user, in order to begin the process.
         */
        if (path == null) {
            throw new IllegalArgumentException("The argument -path was not specified.");
        }
        if (variable == null) {
            throw new IllegalArgumentException("The argument -variable was not specified.");
        }
        final File ncml = new File(path);
        if (!ncml.exists()) {
            err.println("Path invalid to NcML file : " + path);
            System.exit(ILLEGAL_ARGUMENT_EXIT_CODE);
        }
        final NcmlGridCoverageTable ncmlTable = new NcmlGridCoverageTable(database, format);
        ncmlTable.setCanInsertNewLayers(true);
        final Set<NcmlNetcdfElement> netcdfTags = new LinkedHashSet<NcmlNetcdfElement>();
        variable = variable.toLowerCase().trim();
        try {
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
                final NcmlTimeValues ncmlValue = NcmlReading.createNcmlTimeValues(timeElement);
                final NcmlNetcdfElement netcdfElement = new NcmlNetcdfElement(location, ncmlValue);
                netcdfTags.add(netcdfElement);
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
            database.flush();
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
     * @throws CatalogException if an error occured while inserting the data.
     * @throws SQLException If a SQL error occurs, other than a duplicated value.
     * @throws IOException If an I/O error occured.
     */
    private void addToLayer(final String layer, final URI location,
                            final WritableGridCoverageTable table)
                            throws CatalogException, SQLException, IOException
    {
        table.setLayer(layer);
        final NetcdfImageReader reader = addInputToReader(location.toString());
        reader.setVariables(new String[] {variable});
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
            LOGGER.warning(sql.getLocalizedMessage());
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
            reader.setVariables(new String[] {variable});
            table.addEntry(reader);
        } catch (SQLException sql) {
            // If the error code is "23505", we know that it is a postgresql error which
            // indicates the addition of a record already present into the database.
            // In this case, we do nothing because the record is already present.
            // Otherwise we throw this exception, which could have occured for a different
            // reason.
            if (!sql.getSQLState().equals("23505")) {
                throw sql;
            }
            LOGGER.warning(sql.getLocalizedMessage());
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
        final NetcdfImageReader reader = 
                (NetcdfImageReader) new NetcdfImageReader.Spi().createReaderInstance();
        if (variable != null) {
            reader.setVariables(new String[] {variable});
        }
        return reader;
    }

    /**
     * Disposes collector resources.
     *
     * @throws CatalogException If an error occured while disposing the resources.
     */
    private void close() throws CatalogException {
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

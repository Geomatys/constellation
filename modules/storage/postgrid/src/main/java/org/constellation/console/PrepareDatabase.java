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

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.geotoolkit.console.CommandLine;
import org.geotoolkit.console.Option;
import org.geotoolkit.util.logging.Logging;


/**
 * Prepare the database for a future data collecting (with the {@link Collector}).
 * It fills the {@code Layers} table with some new records, necessary for the
 * {@link Collector} to be launched.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class PrepareDatabase extends CommandLine {
    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logging.getLogger(PrepareDatabase.class);

    private static final String NULL = "NULL";

    /**
     * Layer name.
     */
    @Option(name="layer", /*description="Layer name.",*/ mandatory=true)
    private String layerName;

    /**
     * Thematic for this layer.
     */
    @Option(/*description="Layer thematic."*/)
    private String thematic;

    /**
     * Procedure for this layer.
     */
    @Option(/*description="Layer procedure."*/)
    private String procedure;

    /**
     * Period for this layer.
     */
    @Option(/*description="Layer period."*/)
    private Double period;

    /**
     * Thematic for this layer.
     */
    @Option(/*description="Layer description."*/)
    private String description;

    /**
     * Identifier for the serie.
     */
    @Option(/*description="Serie identifier."*/)
    private String identifier;

    /**
     * Identifier for the serie.
     */
    @Option(/*description="File extension for this serie."*/)
    private String extension;

    /**
     * Identifier for the serie.
     */
    @Option(name="pathname"/*, description="Serie path on the server"*/)
    private String pathName;

    /**
     * Identifier for the serie.
     */
    @Option(/*description="Serie format. This value should be contained in the 'Formats' table."*/)
    private String format;

    /**
     * Database connection.
     */
    private Connection connection;

    /**
     * Prepare the database to be filled by the {@link Collector}, using the arguments
     * given.
     */
    public PrepareDatabase(String[] args) {
        super(null, args);
    }

    /**
     * Returns the string given in argument formated as for using it into a SQL request,
     * that means with quotes doubled if present in the string, or {@code NULL} if the
     * parameter does not have a value.
     *
     * @param text The text to format.
     */
    private static String escapeString(final String text) {
        if (text == null) {
            return NULL;
        }
        return '\'' + text.replaceAll("'", "''") + '\'';
    }

    /**
     * Verify whether a layer with the specified name is already present into the database.
     *
     * @param name The name of a layer to test the existence into the database.
     * @return {@code True} if the layer is already present into the database. {@code False} otherwise.
     * @throws SQLException
     */
    private boolean isLayerAlreadyPresent(final String name) throws SQLException {
        final String sql = "SELECT count(name) from \"Layers\" " +
                           "WHERE name='"+ name +"';";
        int numLayer = 0;
        Statement stmt = null;
        ResultSet res = null;
        try {
            stmt = connection.createStatement();
            res  = stmt.executeQuery(sql);

            while (res.next()) {
                numLayer = res.getInt(1);
            }
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        if (numLayer == 0) {
            return false;
        }
        return true;
    }

    /**
     * Inserts a dummy record in the {@code GridCoverage} table. It is necessary in order to
     * collect OpenDAP records from an URL. This dummy record will be deleted as soon as the
     * collector is launched.
     *
     * @param urlFile The last part of the OpenDAP URL.
     * @throws SQLException
     */
    private void processGridCoverage(final String urlFile) throws SQLException {
        final String selectExtent = "SELECT DISTINCT identifier from \"GridGeometries\"";
        Statement stmt = null;
        ResultSet res  = null;
        try {
            stmt = connection.createStatement();
            res  = stmt.executeQuery(selectExtent);
            final String extent = (res.next()) ? res.getString("identifier") : NULL;
            stmt.close();
            final StringBuilder sql = new StringBuilder(
                    "INSERT INTO \"GridCoverages\" (series, filename, extent) VALUES (");
            sql.append(escapeString(identifier)); sql.append(", ");
            sql.append(escapeString(urlFile)); sql.append(", ");
            sql.append(escapeString(extent)); sql.append(")");
            stmt.execute(sql.toString());
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * Inserts the values into the {@code Layers} table.
     *
     * @throws SQLException
     */
    private void processLayer() throws SQLException {
        final StringBuilder sql = new StringBuilder(
                "INSERT INTO \"Layers\" (name, thematic, procedure, period, " +
                "description) VALUES (");
        sql.append(escapeString(layerName)); sql.append(", ");
        sql.append(escapeString(thematic));  sql.append(", ");
        sql.append(escapeString(procedure)); sql.append(", ");
        if (period == null || Double.isNaN(period)) {
            sql.append(NULL);
        } else {
            sql.append(period);
        }
        sql.append(", ");
        sql.append(escapeString(description));
        sql.append(");");

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute(sql.toString());
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * Inserts the values into the {@code Series} table.
     *
     * @throws SQLException
     */
    private void processSerie() throws SQLException {
        final String dods = "dods";
        String urlFile = "";
        if (pathName.startsWith(dods)) {
            if (pathName.endsWith("/")) {
                //Suppress the / character at the end of the url.
                pathName = pathName.substring(0, pathName.length() - 1);
            }
            int indSlash = pathName.lastIndexOf('/');
            urlFile = pathName.substring(indSlash + 1);
            pathName = pathName.substring(0, indSlash);
        }
        final StringBuilder sql = new StringBuilder(
                "INSERT INTO \"Series\" (identifier, layer, pathname, extension, format) " +
                "VALUES (");
        sql.append(escapeString(identifier)); sql.append(", ");
        sql.append(escapeString(layerName));  sql.append(", ");
        sql.append(escapeString(pathName));   sql.append(", ");
        sql.append(escapeString(extension));  sql.append(", ");
        sql.append(escapeString(format));     sql.append(");");

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute(sql.toString());
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }

        if (pathName.startsWith(dods)) {
            processGridCoverage(urlFile);
        }
    }

    /**
     * Runs the process of inserting into the {@code Layers} table new values.
     *
     * @throws CatalogException
     */
    protected void run(Database database) throws CatalogException, SQLException {
        if (database == null) try {
            database = new Database();
        } catch (IOException e) {
            throw new CatalogException(e);
        }
        database.setReadOnly(false);
        database.setUpdateSimulator(null);
        connection = database.getConnection();
        run();
        if (!isLayerAlreadyPresent(layerName)) {
            processLayer();
            LOGGER.info("La couche \""+ layerName +"\" a été insérée.");
        }
        if (identifier != null) {
            processSerie();
            LOGGER.info("La série \""+ identifier +"\" a été insérée.");
        }
        connection.close();
    }

    /**
     * Redefine this method in order to handle the case if the action parameter is {@code null}.
     *
     * @param action The action to execute.
     *
     * @see CommandLine#unknownAction(String)
     */
    @Override
    protected void unknownAction(String action) {
        if (action == null) {
            return;
        }
        super.unknownAction(action);
    }

    /**
     * @param args The command line arguments.
     * @throws CatalogException
     * @throws SQLException
     */
    public static void main(String[] args) throws CatalogException, SQLException {
        final PrepareDatabase prepareDB = new PrepareDatabase(args);
        prepareDB.run(null);
    }
}

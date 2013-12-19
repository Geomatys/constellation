/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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


package org.constellation.admin.util;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SQLExecuter implements Closeable {

    /**
     * Logger used for debugging and event notification.
     */
    private static final Logger LOGGER = Logging.getLogger(SQLExecuter.class);

    /**
     * Wrapper database {@link Connection} instance.
     */
    private final Connection connect;

    /**
     * Create a new {@link Session} instance.
     *
     * @param connect   the {@link Connection} instance
     */
    public SQLExecuter(final Connection connect) {
        this.connect   = connect;
    }


    public Statement createStatement() throws SQLException {
        return connect.createStatement();
    }
    /**
     * Close the session. {@link Session} instance should not be used after this.
     */
    @Override
    public void close() {
        try {
            connect.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while closing database connection.", ex);
        }
    }
}

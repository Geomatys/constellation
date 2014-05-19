/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

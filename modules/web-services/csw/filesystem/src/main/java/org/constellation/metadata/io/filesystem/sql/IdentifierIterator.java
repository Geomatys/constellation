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

package org.constellation.metadata.io.filesystem.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.util.collection.CloseableIterator;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class IdentifierIterator implements CloseableIterator<String> {

    private static final Logger LOGGER = Logging.getLogger(IdentifierIterator.class);

    private final Session session;

    private final ResultSet rs;

    private String current = null;

    public IdentifierIterator(final Session session) {
        this.session = session;
        try {
            session.setReadOnly(true);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "error whiel setting cconenction to read only", ex);
        }
        this.rs = session.getRecordIterator();
    }

    @Override
    public boolean hasNext() {
        findNext();
        return current != null;
    }

    @Override
    public String next() {
       findNext();
       final String id = current;
       current = null;
       return id;
    }

    private void findNext(){
        if (current!=null) return;
        try {
            if (rs.next()) {
                current = rs.getString(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error while iterating resultSet", e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported in this iterator.");
    }


    @Override
    public void close() {
        try {
            rs.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "error while closing resultSet", ex);
        }
        session.close();
    }
}

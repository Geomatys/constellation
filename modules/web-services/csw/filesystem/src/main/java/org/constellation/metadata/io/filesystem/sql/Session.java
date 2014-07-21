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

import org.apache.sis.util.logging.Logging;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class Session implements Closeable {

    private static final Logger LOGGER = Logging.getLogger(Session.class);
    
    private final Connection con;

    private PreparedStatement insertStatement = null;
    
    private PreparedStatement updateStatement = null;

    public Session(final Connection con) {
        this.con = con;
    }

    public boolean needAnalyze() {
        boolean result  = true;
        try {
            final PreparedStatement stmt = con.prepareStatement("SELECT count(\"identifier\") FROM \"csw\".\"records\"");
            final ResultSet rs = stmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            result = (count == 0);
            rs.close();
            stmt.close();

        } catch (SQLException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while reading in csw database schema.", unexpected);
        }
        return result;
    }

    public void setReadOnly(final boolean readOnly) throws SQLException {
        con.setReadOnly(readOnly);
    }

    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        con.setAutoCommit(autoCommit);
    }

    public void commit() throws SQLException {
        con.commit();
    }

    public void putRecord(final String identifier, final String path) throws SQLException {
        try {
            if (insertStatement == null) {
                insertStatement = con.prepareStatement("INSERT INTO \"csw\".\"records\" VALUES (?,?)");
            }
            insertStatement.setString(1, identifier);
            insertStatement.setString(2, path);
            insertStatement.executeUpdate();

        } catch (SQLException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while inserting in csw database schema(id=" + identifier + " path=" + path +")", unexpected);
        }
    }
    
    public void updateRecord(final String identifier, final String path) throws SQLException {
        try {
            if (updateStatement == null) {
                updateStatement = con.prepareStatement("UPDATE \"csw\".\"records\" SET \"path\" = ? WHERE \"identifier\"=?");
            }
            updateStatement.setString(1, path);
            updateStatement.setString(2, identifier);
            updateStatement.executeUpdate();

        } catch (SQLException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while inserting in csw database schema(id=" + identifier + " path=" + path +")", unexpected);
        }
    }

    public void removeRecord(final String identifier) throws SQLException {
        try {
            final PreparedStatement stmt = con.prepareStatement("DELETE FROM \"csw\".\"records\" WHERE \"identifier\"=?");
            stmt.setString(1, identifier);
            stmt.executeUpdate();
            stmt.close();

        } catch (SQLException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while inserting in csw database schema.", unexpected);
        }
    }
    
    public int getCount() throws SQLException {
        int count = 0;
        try {
            final PreparedStatement stmt = con.prepareStatement("SELECT COUNT(*) FROM \"csw\".\"records\"");
            final ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            stmt.close();
        } catch (SQLException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while looking for metadata count.", unexpected);
        }
        return count;
    }

    public String getPathForRecord(final String identifier) throws SQLException {
        String result  = null;
        try {
            final PreparedStatement stmt = con.prepareStatement("SELECT \"path\" FROM \"csw\".\"records\" WHERE \"identifier\"=?");
            stmt.setString(1, identifier);
            final ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = rs.getString(1);
            }
            rs.close();
            stmt.close();

        } catch (SQLException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while reading in csw database schema.", unexpected);
        } 
        return result;
    }

    public boolean existRecord(final String identifier) throws SQLException {
        boolean result  = false;
        try {
            final PreparedStatement stmt = con.prepareStatement("SELECT \"path\" FROM \"csw\".\"records\" WHERE \"identifier\"=?");
            stmt.setString(1, identifier);
            final ResultSet rs = stmt.executeQuery();
            result = rs.next();
            rs.close();
            stmt.close();
        } catch (SQLException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while reading in csw database schema.", unexpected);
        }
        return result;
    }

    public List<String> getRecordList() throws SQLException {
        final List<String> results  = new ArrayList<>();
        try {
            final PreparedStatement stmt = con.prepareStatement("SELECT \"identifier\" FROM \"csw\".\"records\"");
            final ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(rs.getString(1));
            }
            rs.close();
            stmt.close();

        } catch (SQLException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while reading in csw database schema.", unexpected);
        }
        return results;
    }

    public ResultSet getRecordIterator() {
        try {
            final PreparedStatement stmt = con.prepareStatement("SELECT \"identifier\" FROM \"csw\".\"records\"");
            final ResultSet rs = stmt.executeQuery();
            return rs;

        } catch (SQLException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while reading in csw database schema.", unexpected);
        }
        return null;
    }

    public ResultSet getPathIterator() {
        try {
            final PreparedStatement stmt = con.prepareStatement("SELECT \"path\" FROM \"csw\".\"records\"");
            final ResultSet rs = stmt.executeQuery();
            return rs;

        } catch (SQLException unexpected) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while reading in csw database schema.", unexpected);
        }
        return null;
    }

    @Override
    public void close() {
        try {
            if (insertStatement != null) {
                insertStatement.close();
            }
            if (updateStatement != null) {
                updateStatement.close();
            }
            con.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while closing connection in csw database schema.", ex);
        }
    }
}

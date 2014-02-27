/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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

package org.constellation.metadata.io.filesystem.sql;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class Session implements Closeable {

    private static final Logger LOGGER = Logging.getLogger(Session.class);
    
    private final Connection con;

    private PreparedStatement insertStatement = null;

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
            LOGGER.log(Level.WARNING, "Unexpected error occurred while inserting in csw database schema.", unexpected);
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
            con.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Unexpected error occurred while closing connection in csw database schema.", ex);
        }
    }
}

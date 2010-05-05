/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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

package org.constellation.generic;

import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.geotoolkit.internal.sql.StatementEntry;

/**
 *
 * @author guilhem
 */
public final class LockedPreparedStatement extends StatementEntry {

        private String sql;

        public LockedPreparedStatement(PreparedStatement stmt, String sql) {
            super(stmt);
            this.sql    = sql;
        }

        public void setString(int index , String value) throws SQLException {
            statement.setString(index, value);
        }

        public void setInt(int index , int value) throws SQLException {
            statement.setInt(index, value);
        }

        public void setBoolean(int index , boolean value) throws SQLException {
            statement.setBoolean(index, value);
        }

        public void setDate(int index , Date value) throws SQLException {
            statement.setDate(index, value);
        }

        public void setNull(int index , int value) throws SQLException {
            statement.setNull(index, value);
        }


        public void execute() throws SQLException {
            statement.execute();
        }

        public void executeUpdate() throws SQLException {
            statement.executeUpdate();
        }

        public ResultSet executeQuery() throws SQLException {
            return statement.executeQuery();
        }

        public ParameterMetaData getParameterMetaData() throws SQLException {
            return statement.getParameterMetaData();
        }
        
        public String getSql() {
            return sql;
        }
    }

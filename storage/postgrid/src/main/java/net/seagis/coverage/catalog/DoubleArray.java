/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
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
package net.seagis.coverage.catalog;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;


/**
 * A temporary implementation of the {@link Array} interface. To be deleted when we
 * will be allowed to compile for J2SE 1.6 and the PostgreSQL driver will implement
 * the {@link Connection#createArrayOf} method.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
final class DoubleArray implements Array {
    private final Double[] data;

    public DoubleArray(final Double[] data) {
        this.data = data;
    }

    public String getBaseTypeName() {
        return "float8";
    }

    public int getBaseType() {
        return Types.DOUBLE;
    }

    public Object getArray() {
        return data;
    }

    public Object getArray(final Map<String, Class<?>> map) {
        return getArray();
    }

    public Object getArray(final long index, final int count) {
        final Double[] copy = new Double[count];
        System.arraycopy(data, (int) index, copy, 0, count);
        return copy;
    }

    public Object getArray(final long index, final int count, final Map<String, Class<?>> map) {
        return getArray(index, count);
    }

    public ResultSet getResultSet() throws SQLException {
        throw new SQLException("Not supported yet.");
    }

    public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException {
        return getResultSet();
    }

    public ResultSet getResultSet(long index, int count) throws SQLException {
        throw new SQLException("Not supported yet.");
    }

    public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) throws SQLException {
        return getResultSet(index, count);
    }

    public void free() {
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        char separator = '{';
        for (int i=0; i<data.length; i++) {
            buffer.append(separator).append(data[i]);
            separator = ',';
        }
        return buffer.append('}').toString();
    }
}

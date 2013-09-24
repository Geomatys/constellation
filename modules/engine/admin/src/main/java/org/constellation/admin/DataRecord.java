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

package org.constellation.admin;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Administration database record for {@code Data} table.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class DataRecord implements Serializable {

    final int id;
    final String name;
    final int provider;
    final String type;
    final String owner;

    public DataRecord(final ResultSet rs) throws SQLException {
        this.id         = rs.getInt(1);
        this.name       = rs.getString(2);
        this.provider   = rs.getInt(3);
        this.type       = rs.getString(4);
        this.owner      = rs.getString(5);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getOwner() {
        return owner;
    }
}

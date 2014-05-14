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

package org.constellation.admin.dao;

import java.io.InputStream;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.constellation.admin.EmbeddedDatabase;

/**
 *
 * @author guilhem
 */
public class SensorRecord extends Record {
    
    private Session session;
    
    final int id;
    private String identifier;
    //identifier of parent provider
    private String type;
    private String parent;
    private String owner;

    SensorRecord(final Session session, final int id, final String identifier, final String type, final String parent, final String owner) {
        this.session    = session;
        this.id         = id;
        this.identifier = identifier;
        this.type       = type;
        this.parent     = parent;
        this.owner      = owner;
    }

    public SensorRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5));
    }
    
    @Override
    protected void ensureConnectionNotClosed() throws SQLException {
        if (session.isClosed()) {
            session = EmbeddedDatabase.createSession();
        }
    }
    
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) throws SQLException {
        this.identifier = identifier;
        ensureConnectionNotClosed();
        session.updateSensor(id, identifier, type, parent, owner);
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) throws SQLException {
        this.type = type;
        ensureConnectionNotClosed();
        session.updateSensor(id, identifier, type, parent, owner);
    }
    
    public String getParentIdentifier() {
        return parent;
    }

    public void setParentIdentifier(String parent) throws SQLException {
        this.parent = parent;
        ensureConnectionNotClosed();
        session.updateSensor(id, identifier, type, parent, owner);
    }

    public String getOwnerLogin() {
        return owner;
    }
    
    public InputStream getMetadata() throws SQLException {
        ensureConnectionNotClosed();
        return session.readSensorMetadata(id);
    }

    public boolean hasMetadata() throws SQLException {
        ensureConnectionNotClosed();
        return session.hasSensorMetadata(id);
    }

    public void setMetadata(final StringReader metadata) throws SQLException {
        ensureConnectionNotClosed();
        session.updateSensorMetadata(id, metadata);
    }
}

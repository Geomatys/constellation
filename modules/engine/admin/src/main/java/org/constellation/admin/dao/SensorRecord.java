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

package org.constellation.admin.dao;

import java.io.InputStream;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.bind.annotation.XmlTransient;
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


    public String getType() {
        return type;
    }

    public String getOwnerLogin() {
        return owner;
    }
    
    @XmlTransient
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

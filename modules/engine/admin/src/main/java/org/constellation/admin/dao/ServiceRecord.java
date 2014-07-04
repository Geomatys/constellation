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

import org.constellation.ServiceDef.Specification;
import org.constellation.admin.EmbeddedDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class ServiceRecord extends Record {

    private Session session;

    final int id;
    private String identifier;
    private Specification type;
    private final Date date;
    private String owner;
    private String metadataId;

    ServiceRecord(final Session session, final int id, final String identifier, final Specification type,
                  final Date date, final String owner, final String metadataId) {
        this.session     = session;
        this.id          = id;
        this.identifier  = identifier;
        this.type        = type;
        this.date        = date;
        this.owner       = owner;
        this.metadataId  = metadataId;
    }

    public ServiceRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getInt(1),
                rs.getString(2),
                Specification.valueOf(rs.getString(3).toUpperCase()),
                new Date(rs.getLong(4)),
                rs.getString(5),
                rs.getString(6));
    }

    /**
     * {@inheritDoc}
     */
    public void ensureConnectionNotClosed() throws SQLException {
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
        session.updateService(id, identifier, type, owner);
    }

    public Specification getType() {
        return type;
    }

    public void setType(final Specification type) throws SQLException {
        this.type = type;
        ensureConnectionNotClosed();
        session.updateService(id, identifier, type, owner);
    }

    public Date getDate() {
        return date;
    }

    
    public InputStream getConfig() throws SQLException {
        ensureConnectionNotClosed();
        return session.readServiceConfig(id);
    }

    @Deprecated
    public void setConfig(final StringReader config) throws SQLException {
        ensureConnectionNotClosed();
        session.updateServiceConfig(id, config);
    }

    public InputStream getExtraFile(final String fileName) throws SQLException {
        ensureConnectionNotClosed();
        return session.readExtraServiceConfig(id, fileName);
    }

    @Deprecated
    public void setExtraFile(final String fileName, final StringReader config) throws SQLException {
        ensureConnectionNotClosed();
        final InputStream is = session.readExtraServiceConfig(id, fileName);
        if (is == null) {
            session.writeServiceExtraConfig(identifier, type, config, fileName);
        } else {
            session.updateServiceExtraConfig(id, fileName, config);
        }
    }

    public InputStream getMetadata(final String lang) throws SQLException {
        ensureConnectionNotClosed();
        return session.readServiceMetadata(id, lang);
    }
    
    public InputStream getIsoMetadata() throws SQLException {
        ensureConnectionNotClosed();
        return session.readServiceIsoMetadata(id);
    }

    public void setMetadata(final String lang, final StringReader metadata) throws SQLException {
        ensureConnectionNotClosed();
        final InputStream is = session.readServiceMetadata(id, lang);
        if (is == null) {
            session.writeServiceMetadata(identifier, type, metadata, lang);
        } else {
            session.updateServiceMetadata(id, lang, metadata);
        }
    }
    
    public void setIsoMetadata(final String metadataId, final StringReader isoMetadata) throws SQLException {
        ensureConnectionNotClosed();
        this.metadataId = metadataId;
        session.writeServiceIsoMetadata(identifier, type, metadataId, isoMetadata);
    }
    
    public boolean hasIsoMetadata() throws IOException, SQLException {
        ensureConnectionNotClosed();
        return session.hasServiceIsoMetadata(id);
    }

    public String getOwnerLogin() {
        return owner;
    }

    public String getMetadataId() {
        return metadataId;
    }
}

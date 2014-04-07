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

package org.constellation.admin.dao;

import java.io.IOException;
import java.io.InputStream;
import org.constellation.ServiceDef.Specification;
import org.constellation.admin.EmbeddedDatabase;

import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;

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
    private final int title;
    private final int description;
    private String owner;

    ServiceRecord(final Session session, final int id, final String identifier, final Specification type,
                  final Date date, final int title, final int description, final String owner) {
        this.session     = session;
        this.id          = id;
        this.identifier  = identifier;
        this.type        = type;
        this.date        = date;
        this.title       = title;
        this.description = description;
        this.owner       = owner;
    }

    public ServiceRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getInt(1),
                rs.getString(2),
                Specification.valueOf(rs.getString(3)),
                new Date(rs.getLong(4)),
                rs.getInt(5),
                rs.getInt(6),
                rs.getString(7));
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

    public String getTitle(final Locale locale) throws SQLException {
        ensureConnectionNotClosed();
        return session.readI18n(title, locale);
    }

    public void setTitle(final Locale locale, final String value) throws SQLException {
        ensureConnectionNotClosed();
        session.updateI18n(title, locale, value);
    }

    public String getDescription(final Locale locale) throws SQLException {
        ensureConnectionNotClosed();
        return session.readI18n(description, locale);
    }

    public void setDescription(final Locale locale, final String value) throws SQLException {
        ensureConnectionNotClosed();
        session.updateI18n(description, locale, value);
    }

    public InputStream getConfig() throws SQLException {
        ensureConnectionNotClosed();
        return session.readServiceConfig(id);
    }

    public void setConfig(final StringReader config) throws SQLException {
        ensureConnectionNotClosed();
        session.updateServiceConfig(id, config);
    }

    public InputStream getExtraFile(final String fileName) throws SQLException {
        ensureConnectionNotClosed();
        return session.readExtraServiceConfig(id, fileName);
    }

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
    
    public InputStream getIsoMetadata(final String lang) throws SQLException {
        ensureConnectionNotClosed();
        return session.readServiceIsoMetadata(id, lang);
    }

    public void setMetadata(final String lang, final StringReader metadata, final StringReader isoMetadata) throws SQLException {
        ensureConnectionNotClosed();
        final InputStream is = session.readServiceMetadata(id, lang);
        if (is == null) {
            session.writeServiceMetadata(identifier, type, metadata, isoMetadata, lang);
        } else {
            session.updateServiceMetadata(id, lang, metadata, isoMetadata);
        }
    }
    
    public boolean hasIsoMetadata() throws IOException, SQLException {
        ensureConnectionNotClosed();
        return session.hasServiceIsoMetadata(id);
    }

    public String getOwnerLogin() {
        return owner;
    }

 
}

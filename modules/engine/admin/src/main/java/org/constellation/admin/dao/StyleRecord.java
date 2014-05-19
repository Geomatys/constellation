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

import org.constellation.admin.EmbeddedDatabase;
import org.geotoolkit.style.MutableStyle;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class StyleRecord extends Record {

    public static enum StyleType {
        ALL,
        VECTOR,
        COVERAGE,
        SENSOR
    }

    private Session session;

    final int id;
    private String name;
    private int provider;
    private StyleType type;
    private final Date date;
    private final int title;
    private final int description;
    private String owner;

    StyleRecord(final Session session, final int id, final String name, final int provider, final StyleType type,
                final Date date, final int title, final int description, final String owner) {
        this.session     = session;
        this.id          = id;
        this.name        = name;
        this.provider    = provider;
        this.type        = type;
        this.date        = date;
        this.title       = title;
        this.description = description;
        this.owner       = owner;
    }

    public StyleRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getInt(1),
                rs.getString(2),
                rs.getInt(3),
                StyleType.valueOf(rs.getString(4)),
                new Date(rs.getLong(5)),
                rs.getInt(6),rs.getInt(7),
                rs.getString(8));
    }

    /**
     * {@inheritDoc}
     */
    public void ensureConnectionNotClosed() throws SQLException {
        if (session.isClosed()) {
            session = EmbeddedDatabase.createSession();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) throws SQLException {
        this.name = name;
        ensureConnectionNotClosed();
        session.updateStyle(id, name, provider, type, owner);
    }

    public ProviderRecord getProvider() throws SQLException {
        ensureConnectionNotClosed();
        return session.readProvider(provider);
    }

    public void setProvider(final ProviderRecord provider) throws SQLException {
        this.provider = provider.id;
        ensureConnectionNotClosed();
        session.updateStyle(id, name, provider.id, type, owner);
    }

    public StyleType getType() {
        return type;
    }

    public void setType(final StyleType type) throws SQLException {
        this.type = type;
        ensureConnectionNotClosed();
        session.updateStyle(id, name, provider, type, owner);
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

    public InputStream getBody() throws SQLException {
        ensureConnectionNotClosed();
        return session.readStyleBody(id);
    }

    public void setBody(final MutableStyle style) throws SQLException, IOException {
        ensureConnectionNotClosed();
        session.updateStyleBody(id, style);
    }

    public String getOwnerLogin() {
        return owner;
    }

  

    public List<DataRecord> getLinkedData() throws SQLException {
        ensureConnectionNotClosed();
        return session.readData(this);
    }

    public void linkToData(final DataRecord data) throws SQLException {
        ensureConnectionNotClosed();
        session.writeStyledData(this, data);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StyleRecord that = (StyleRecord) o;

        if (id != that.id) return false;
        if (provider != that.provider) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + provider;
        return result;
    }
}

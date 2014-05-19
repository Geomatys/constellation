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
public final class LayerRecord extends Record {

    private Session session;

    final int id;
    private String name;
    private String namespace;
    private String alias;
    private int service;
    private int data;
    private Date date;
    private int title;
    private int description;
    private String owner;

    LayerRecord(final Session session, final int id, final String name, final String namespace, final String alias, final int service, final int data,
                       final Date date, final int title, final int description, final String owner) {
        this.session     = session;
        this.id          = id;
        this.name        = name;
        this.namespace   = namespace;
        this.alias       = alias;
        this.service     = service;
        this.data        = data;
        this.date        = date;
        this.title       = title;
        this.description = description;
        this.owner       = owner;
    }

    public LayerRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getInt(5),
                rs.getInt(6),
                new Date(rs.getLong(7)),
                rs.getInt(8),
                rs.getInt(9),
                rs.getString(10));
    }

    public void ensureConnectionNotClosed() throws SQLException {
        if (session.isClosed()) {
            session = EmbeddedDatabase.createSession();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) throws SQLException  {
        this.name = name;
        ensureConnectionNotClosed();
        session.updateLayer(id, name, namespace, alias, service, data, owner);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) throws SQLException  {
        this.namespace = namespace;
        ensureConnectionNotClosed();
        session.updateLayer(id, name, namespace, alias, service, data, owner);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(final String alias) throws SQLException {
        this.alias = alias;
        ensureConnectionNotClosed();
        session.updateLayer(id, name, namespace, alias, service, data, owner);
    }

    public ServiceRecord getService() throws SQLException {
        ensureConnectionNotClosed();
        return session.readService(service);
    }

    public void setService(final ServiceRecord service) throws SQLException {
        this.service = service.id;
        ensureConnectionNotClosed();
        session.updateLayer(id, name, namespace, alias, service.id, data, owner);
    }

    public Date getDate() {
        return date;
    }

    public DataRecord getData() throws SQLException {
        ensureConnectionNotClosed();
        return session.readData(data);
    }

    public void setData(final DataRecord data) throws SQLException {
        this.data = data.id;
        ensureConnectionNotClosed();
        session.updateLayer(id, name, namespace, alias, service, data.id, owner);
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

    public Object getConfig() throws SQLException {
        ensureConnectionNotClosed();
        return session.readLayerConfig(id);
    }

    public void setConfig(final StringReader config) throws SQLException {
        ensureConnectionNotClosed();
        session.updateLayerConfig(id, config);
    }

    public String getOwnerLogin() {
        return owner;
    }

   
}

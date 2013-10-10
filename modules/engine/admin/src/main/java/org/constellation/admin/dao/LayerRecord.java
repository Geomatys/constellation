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
public final class LayerRecord implements Record {

    private final Session session;

    final int id;
    private String alias;
    private int service;
    private int data;
    private Date date;
    private int title;
    private int description;
    private String owner;

    LayerRecord(final Session session, final int id, final String alias, final int service, final int data,
                       final Date date, final int title, final int description, final String owner) {
        this.session     = session;
        this.id          = id;
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
                rs.getInt(3),
                rs.getInt(4),
                new Date(rs.getLong(5)),
                rs.getInt(6),
                rs.getInt(7),
                rs.getString(8));
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(final String alias) throws SQLException {
        this.alias = alias;
        session.updateLayer(id, alias, service, data, owner);
    }

    public ServiceRecord getService() throws SQLException {
        return session.readService(service);
    }

    public void setService(final ServiceRecord service) throws SQLException {
        this.service = service.id;
        session.updateLayer(id, alias, service.id, data, owner);
    }

    public Date getDate() {
        return date;
    }

    public DataRecord getData() throws SQLException {
        return session.readData(data);
    }

    public void setData(final DataRecord data) throws SQLException {
        this.data = data.id;
        session.updateLayer(id, alias, service, data.id, owner);
    }

    public String getTitle(final Locale locale) throws SQLException {
        return session.readI18n(title, locale);
    }

    public void setTitle(final Locale locale, final String value) throws SQLException {
        session.updateI18n(title, locale, value);
    }

    public String getDescription(final Locale locale) throws SQLException {
        return session.readI18n(description, locale);
    }

    public void setDescription(final Locale locale, final String value) throws SQLException {
        session.updateI18n(description, locale, value);
    }

    public Object getConfig() throws SQLException {
        return session.readLayerConfig(id);
    }

    public void setConfig(final StringReader config) throws SQLException {
        session.updateLayerConfig(id, config);
    }

    public String getOwnerLogin() {
        return owner;
    }

    public UserRecord getOwner() throws SQLException {
        return session.readUser(owner);
    }

    public void setOwner(final UserRecord owner) throws SQLException {
        this.owner = owner.getLogin();
        session.updateLayer(id, alias, service, data, owner.getLogin());
    }
}

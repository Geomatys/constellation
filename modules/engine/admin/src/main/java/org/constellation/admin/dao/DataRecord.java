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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.xml.namespace.QName;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class DataRecord implements Record {

    public static enum DataType {
        VECTOR,
        COVERAGE,
        SENSOR
    }

    private final Session session;

    final int id;
    private String name;
    private String namespace;
    private int provider;
    private DataType type;
    private final Date date;
    private final int title;
    private final int description;
    private String owner;

    DataRecord(final Session session, final int id, final String name, final String namespace, final int provider, final DataType type,
               final Date date, final int title, final int description, final String owner) {
        this.session     = session;
        this.id          = id;
        this.name        = name;
        this.namespace   = namespace;
        this.provider    = provider;
        this.type        = type;
        this.date        = date;
        this.title       = title;
        this.description = description;
        this.owner       = owner;
    }

    public DataRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getInt(4),
                DataType.valueOf(rs.getString(5)),
                new Date(rs.getLong(6)),
                rs.getInt(7),
                rs.getInt(8),
                rs.getString(9));
    }

    public QName getCompleteName() {
        return new QName(namespace, name);
    }
    
    public String getName() {
        return name;
    }

    public void setName(final String name) throws SQLException {
        this.name = name;
        session.updateData(id, name, namespace, provider, type, owner);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) throws SQLException {
        this.namespace = namespace;
        session.updateData(id, name, namespace, provider, type, owner);
    }

    public ProviderRecord getProvider() throws SQLException {
        return session.readProvider(provider);
    }

    public void setProvider(final ProviderRecord provider) throws SQLException {
        this.provider = provider.id;
        session.updateData(id, name, namespace, provider.id, type, owner);
    }

    public DataType getType() {
        return type;
    }

    public void setType(final DataType type) throws SQLException {
        this.type = type;
        session.updateData(id, name, namespace, provider, type, owner);
    }

    public Date getDate() {
        return date;
    }

    public String getTitle(final Locale locale) throws SQLException {
        return session.readI18n(title, locale);
    }

    public void setTitle(final Locale locale, final String value) throws SQLException {
        final String title = session.readI18n(this.title, locale);
        if(title !=null){
            session.updateI18n(this.title, locale, value);
        }
        else{
            session.writeI18n(this.title, locale, value);
        }
    }

    public String getDescription(final Locale locale) throws SQLException {
        return session.readI18n(description, locale);
    }

    public void setDescription(final Locale locale, final String value) throws SQLException {
        final String title = session.readI18n(this.description, locale);
        if(title !=null){
            session.updateI18n(this.description, locale, value);
        }
        else{
            session.writeI18n(this.description, locale, value);
        }
    }

    public String getOwnerLogin() {
        return owner;
    }

    public UserRecord getOwner() throws SQLException {
        return session.readUser(owner);
    }

    public void setOwner(final UserRecord owner) throws SQLException {
        this.owner = owner.getLogin();
        session.updateData(id, name, namespace, provider, type, owner.getLogin());
    }

    public List<StyleRecord> getLinkedStyles() throws SQLException {
        return session.readStyles(this);
    }

    public void linkToStyle(final StyleRecord style) throws SQLException {
        session.writeStyledData(style, this);
    }


}
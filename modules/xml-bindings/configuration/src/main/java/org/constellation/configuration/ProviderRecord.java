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

package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Administration database record for {@code Provider} table.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class ProviderRecord implements Serializable {

    public static enum ProviderType {
        LAYER,
        STYLE
    }

    private String identifier;
    private ProviderType type;
    private String impl;
    private String owner;

    ProviderRecord() {
    }

    public ProviderRecord(final ResultSet rs) throws SQLException {
        this.identifier = rs.getString(1);
        this.type       = ProviderType.valueOf(rs.getString(2));
        this.impl       = rs.getString(3);
        this.owner      = rs.getString(4);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public ProviderType getType() {
        return type;
    }

    public void setType(final ProviderType type) {
        this.type = type;
    }

    public String getImpl() {
        return impl;
    }

    public void setImpl(final String impl) {
        this.impl = impl;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }
}

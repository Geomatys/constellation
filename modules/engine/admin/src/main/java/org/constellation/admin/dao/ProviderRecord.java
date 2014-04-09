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

import org.constellation.admin.EmbeddedDatabase;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.constellation.admin.ConfigurationEngine;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class ProviderRecord extends Record {

    public static enum ProviderType {
        LAYER,
        STYLE
    }

    private Session session;

    final int id;
    private String identifier;
    //identifier of parent provider
    private String parent;
    private ProviderType type;
    private String impl;
    private String owner;

    ProviderRecord(final Session session, final int id, final String identifier, final String parent,
            final ProviderType type, final String impl, final String owner) {
        this.session    = session;
        this.id         = id;
        this.identifier = identifier;
        this.parent     = parent;
        this.type       = type;
        this.impl       = impl;
        this.owner      = owner;
    }

    public ProviderRecord(final Session s, final ResultSet rs) throws SQLException {
        this(s, rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                ProviderType.valueOf(rs.getString(4)),
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
        session.updateProvider(id, identifier, parent, type, impl, owner);
    }

    public String getParentIdentifier() {
        return parent;
    }

    public void setParentIdentifier(String parent) {
        this.parent = parent;
    }

    public ProviderRecord getParentProvider(){
        if(parent==null || parent.isEmpty()){
            return null;
        }
        return ConfigurationEngine.getProvider(parent);
    }
    
    public List<ProviderRecord> getChildrenProviders(){
        return ConfigurationEngine.getProvidersFromParent(identifier);
    }
    
    public ProviderType getType() {
        return type;
    }

    public void setType(final ProviderType type) throws SQLException {
        this.type = type;
        ensureConnectionNotClosed();
        session.updateProvider(id, identifier, parent, type, impl, owner);
    }

    public String getImpl() {
        return impl;
    }

    public void setImpl(final String impl) throws SQLException {
        this.impl = impl;
        ensureConnectionNotClosed();
        session.updateProvider(id, identifier, parent, type, impl, owner);
    }

    public GeneralParameterValue getConfig(final GeneralParameterDescriptor descriptor) throws SQLException, IOException {
        ensureConnectionNotClosed();
        return session.readProviderConfig(id, descriptor);
    }

    public void setConfig(final ParameterValueGroup config) throws SQLException, IOException {
        ensureConnectionNotClosed();
        session.updateProviderConfig(id, config);
    }

    public String getOwnerLogin() {
        return owner;
    }

    public InputStream getMetadata() throws IOException, SQLException {
        ensureConnectionNotClosed();
        return session.readProviderMetadata(id);
    }

    public boolean hasMetadata() throws IOException, SQLException {
        ensureConnectionNotClosed();
        return session.hasProviderMetadata(id);
    }

    public void setMetadata(final String metadataId, final StringReader metadata) throws IOException, SQLException {
        ensureConnectionNotClosed();
        session.updateProviderMetadata(id, metadataId, metadata);
    }

    public List<StyleRecord> getStyles() throws SQLException {
        ensureConnectionNotClosed();
        return session.readStyles(this);
    }

    public List<DataRecord> getData() throws SQLException {
        ensureConnectionNotClosed();
        return session.readData(this);
    }

    /**
     * If this provider points on a pyramid which contains conform tiles compared to the
     * original data, then returns {@code true}.
     *
     * @return {@code True} if the tiles pointed by this provider are conform compared to
     * the original data, {@code false} otherwise.
     */
    public boolean isPyramidConformProvider() {
        return identifier.startsWith("conform_");
    }
}
